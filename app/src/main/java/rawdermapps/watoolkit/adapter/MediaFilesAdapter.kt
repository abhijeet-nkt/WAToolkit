package rawdermapps.watoolkit.adapter

import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import rawdermapps.watoolkit.R
import rawdermapps.watoolkit.MediaType
import rawdermapps.watoolkit.model.MediaItem
import java.io.File

class MediaFilesAdapter(
    private val mediaType: MediaType,
    private val onClick: (MediaItem) -> Unit
) :
    RecyclerView.Adapter<MediaFilesAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: CardView) : RecyclerView.ViewHolder(itemView) {
        private val thumbnailView: ImageView = itemView.findViewById(R.id.thumbnail)

        fun bind(pos: Int) {
            itemView.setOnClickListener { onClick(items[pos]) }
            items[pos].loadBitmap(thumbnailView)
        }
    }

    private val items = ArrayList<MediaItem>()

    /* True if there are no files */
    var isEmpty: Boolean = true
        private set

    /* Starts populating the recycler view by loading the files */
    fun loadList() {

        /* Get the extension based on the media type */
        val extension = when (mediaType) {
            MediaType.PICTURE -> ".jpg"
            MediaType.VIDEO -> ".mp4"
        }

        val statusDir = File(Environment.getExternalStorageDirectory(), "/WhatsApp/Media/.Statuses")
        val files = statusDir.listFiles()
        isEmpty = files.isEmpty()

        Thread {
            if (files == null)
                Log.e("FilesAdapter", "files == null !")

            else for (file in files)
                if (file.name.endsWith(extension, true))
                    items.add(MediaItem(file, mediaType))
        }.start()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.media_item, parent, false) as CardView
        )

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(position)
}