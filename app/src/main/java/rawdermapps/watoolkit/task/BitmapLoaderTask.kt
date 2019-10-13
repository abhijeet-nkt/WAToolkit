package rawdermapps.watoolkit.task

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.os.AsyncTask
import android.provider.MediaStore
import rawdermapps.watoolkit.adapter.MediaFilesAdapter

/* An async task for loading picture/video thumbnails on worker thread
 * and avoid blocking the UI thread.
 * The onFinish() callback is called when the task is done and bitmap result is ready */

class BitmapLoaderTask(
    private val file: String,
    private val type: MediaFilesAdapter.MediaType,
    private val onFinish: (Bitmap) -> Unit
) :
    AsyncTask<Unit, Unit, Bitmap>() {

    override fun doInBackground(vararg params: Unit): Bitmap {
        val result =
            if (type == MediaFilesAdapter.MediaType.PICTURES)
                BitmapFactory.decodeFile(file)
            else
                ThumbnailUtils.createVideoThumbnail(
                    file,
                    MediaStore.Images.Thumbnails.MICRO_KIND
                )

        onFinish(result)
        return result
    }
}