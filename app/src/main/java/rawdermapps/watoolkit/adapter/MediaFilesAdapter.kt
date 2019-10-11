package rawdermapps.watoolkit.adapter

import android.content.Context
import android.os.Environment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import rawdermapps.watoolkit.model.MediaItem
import rawdermapps.watoolkit.R
import java.io.File
import java.lang.ref.WeakReference
import android.util.Log

class MediaFilesAdapter(private val context :Context, mediaType: MediaType, private val onClick : (MediaItem) -> Unit):
    RecyclerView.Adapter<MediaFilesAdapter.ViewHolder>() {

    enum class MediaType {PICTURES, VIDEOS}

    inner class ViewHolder(itemView: CardView) : RecyclerView.ViewHolder(itemView) {
        private val thumbnailView: ImageView = itemView.findViewById(R.id.thumbnail)

        fun bind(pos :Int) {
            itemView.setOnClickListener {onClick(items[pos])}
            items[pos].loadBitmap(WeakReference(thumbnailView))
        }
    }

    private val items = ArrayList<MediaItem>()

    /* True if there are no files */
    var isEmpty :Boolean = false
        private set

    init {
        val extension = when (mediaType) {
            MediaType.PICTURES -> ".jpg"
            MediaType.VIDEOS -> ".mp4"
        }

        val statusDir = File(Environment.getExternalStorageDirectory(), "/WhatsApp/Media/.Statuses")
        Log.d("Adapter", "statusDir: $statusDir")

        val files = statusDir.listFiles()
        isEmpty = files.isEmpty()

        Thread {
            if (files == null)
                Log.e("FilesAdapter", "files == null !")
            else {
                for (file in files) {
                    if (file.name.endsWith(extension, true))
                        items.add(MediaItem(file, mediaType))
                    Log.d("Adapter", "file added: ${file.name}")
                }
            }
        }.start()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            (LayoutInflater.from(parent.context)
                .inflate(R.layout.media_item, parent, false) as CardView)
        )

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(position)
}