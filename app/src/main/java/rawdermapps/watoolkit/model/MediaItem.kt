package rawdermapps.watoolkit.model

import android.graphics.Bitmap
import android.widget.ImageView
import rawdermapps.watoolkit.adapter.MediaFilesAdapter
import rawdermapps.watoolkit.fragment.MediaType
import rawdermapps.watoolkit.task.BitmapLoaderTask
import java.io.File
import java.lang.ref.WeakReference

class MediaItem(val file: File, val type: MediaType) {

    private var bitmapCache: Bitmap? = null

    fun loadBitmap(imageView: WeakReference<ImageView?>) {
        if (bitmapCache == null)
            BitmapLoaderTask(file.absolutePath, type) {
                imageView.get()?.apply { post { setImageBitmap(it) } }
            }.execute()

        //There's already a bitmap cached, so use it rather
        else imageView.get()?.setImageBitmap(bitmapCache)
    }
}