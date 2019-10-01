package rawdermapps.watoolkit.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import rawdermapps.watoolkit.R
import rawdermapps.watoolkit.activity.MediaPreviewActivity
import rawdermapps.watoolkit.adapter.MediaFilesAdapter
import rawdermapps.watoolkit.model.MediaItem

class VideoStatusFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.frag_video_status, container, false)

    /* Adapter is set here to make sure that the UI thread does not blocks
     * to load thumbnails while fragment is being attached */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.apply {
            this as RecyclerView
            layoutManager = GridLayoutManager(context, 2)
            adapter = MediaFilesAdapter(view.context, MediaFilesAdapter.MediaType.VIDEOS) { onItemClick(it) }
        }
    }

    /* Called when an item from recycler view is clicked */
    private fun onItemClick(item: MediaItem) {
        Intent(context, MediaPreviewActivity::class.java).apply {
            putExtra(MediaPreviewActivity.EXTRA_FILE_PATH, item.file.absolutePath)
            putExtra(MediaPreviewActivity.EXTRA_MEDIA_TYPE, MediaPreviewActivity.MEDIA_TYPE_VIDEO)
            startActivity(this)
        }
    }
}