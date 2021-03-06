package one.mixin.android.ui.wallet

import android.os.Bundle
import com.uber.autodispose.kotlin.autoDisposable
import kotlinx.android.synthetic.main.fragment_pin_bottom_sheet.view.*
import one.mixin.android.R
import one.mixin.android.ui.common.PinBottomSheetDialogFragment
import one.mixin.android.util.ErrorHandler
import one.mixin.android.vo.Address
import one.mixin.android.widget.PinView
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class PinAddrBottomSheetDialogFragment : PinBottomSheetDialogFragment() {

    companion object {
        const val TAG = "PinAddrBottomSheetDialogFragment"

        const val ADD = 0
        const val DELETE = 1
        const val MODIFY = 2

        const val ARGS_ASSET_ID = "args_asset_id"
        const val ARGS_LABEL = "args_label"
        const val ARGS_PUBLIC_KEY = "args_public_key"
        const val ARGS_TYPE = "args_type"
        const val ARGS_ADDRESS_ID = "args_address_id"

        fun newInstance(
            assetId: String? = null,
            label: String? = null,
            publicKey: String? = null,
            addressId: String? = null,
            type: Int = ADD
        ) = PinAddrBottomSheetDialogFragment().apply {
            val b = bundleOf(
                ARGS_ASSET_ID to assetId,
                ARGS_LABEL to label,
                ARGS_PUBLIC_KEY to publicKey,
                ARGS_ADDRESS_ID to addressId,
                ARGS_TYPE to type
            )
            arguments = b
        }
    }

    private val assetId: String? by lazy { arguments!!.getString(ARGS_ASSET_ID) }
    private val label: String? by lazy { arguments!!.getString(ARGS_LABEL) }
    private val publicKey: String? by lazy { arguments!!.getString(ARGS_PUBLIC_KEY) }
    private val addressId: String? by lazy { arguments!!.getString(ARGS_ADDRESS_ID) }
    private val type: Int by lazy { arguments!!.getInt(ARGS_TYPE) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        contentView.tip_tv.setText(when (type) {
            ADD -> R.string.withdrawal_addr_pin_add
            DELETE -> R.string.withdrawal_addr_pin_delete
            MODIFY -> R.string.withdrawal_addr_pin_modify
            else -> R.string.withdrawal_addr_pin_add
        })
        contentView.pin.setListener(object : PinView.OnPinListener {
            override fun onUpdate(index: Int) {
                if (index != contentView.pin.getCount()) return

                contentView.pin_va?.displayedChild = PinBottomSheetDialogFragment.POS_PB
                val observable = if (type == ADD || type == MODIFY) {
                    bottomViewModel.syncAddr(assetId!!, publicKey!!, label!!, contentView.pin.code())
                } else {
                    bottomViewModel.deleteAddr(addressId!!, contentView.pin.code())
                }
                observable.autoDisposable(scopeProvider).subscribe({ r ->
                    if (r.isSuccess) {
                        doAsync {
                            if (type == ADD || type == MODIFY) {
                                bottomViewModel.saveAddr(r.data as Address)
                            } else {
                                bottomViewModel.deleteLocalAddr(addressId!!)
                            }

                            uiThread {
                                contentView.pin_va?.displayedChild = PinBottomSheetDialogFragment.POS_PIN
                                callback?.onSuccess()
                                dismiss()
                            }
                        }
                    } else {
                        contentView.pin_va?.displayedChild = PinBottomSheetDialogFragment.POS_PIN
                        contentView.pin?.clear()
                        ErrorHandler.handleMixinError(r.errorCode)
                    }
                }, { t ->
                    contentView.pin_va?.displayedChild = PinBottomSheetDialogFragment.POS_PIN
                    contentView.pin.clear()
                    ErrorHandler.handleError(t)
                })
            }
        })
    }

    private var callback: Callback? = null

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    interface Callback {
        fun onSuccess()
    }
}