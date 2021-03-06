package one.mixin.android.vo

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import com.google.gson.annotations.SerializedName

@Entity(tableName = "stickers", primaryKeys = ["album_id", "name"])
data class Sticker(
    @SerializedName("album_id")
    @ColumnInfo(name = "album_id")
    val albumId: String,
    @SerializedName("name")
    @ColumnInfo(name = "name")
    val name: String,
    @SerializedName("asset_url")
    @ColumnInfo(name = "asset_url")
    val assetUrl: String,
    @SerializedName("asset_type")
    @ColumnInfo(name = "asset_type")
    val assetType: String,
    @SerializedName("asset_width")
    @ColumnInfo(name = "asset_width")
    val assetWidth: Int,
    @SerializedName("asset_height")
    @ColumnInfo(name = "asset_height")
    val assetHeight: Int,
    @ColumnInfo(name = "last_use_at")
    var lastUseAt: String?
)
