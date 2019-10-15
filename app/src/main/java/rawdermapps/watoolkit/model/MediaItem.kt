package rawdermapps.watoolkit.model

import android.media.MediaMetadataRetriever
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import rawdermapps.watoolkit.MediaType
import java.io.File

class MediaItem(val file: File, private val type: MediaType) {

    fun loadBitmap(imageView: ImageView) {

        when (type) {
            MediaType.PICTURE -> {
                Glide.with(imageView)
                    .asBitmap()
                    .thumbnail(0.1f)
                    .load(file)
                    .into(imageView)
            }

            MediaType.VIDEO -> {
                MediaMetadataRetriever().also {
                    it.setDataSource(file.absolutePath)
                    val duration = it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong()
                    it.release()

                    Glide.with(imageView)
                        .asBitmap()
                        .thumbnail(0.1f)
                        .load(file)
                        .apply(RequestOptions().frame(duration/2 * 1000))
                        .into(imageView)
                }
            }
        }
    }
}