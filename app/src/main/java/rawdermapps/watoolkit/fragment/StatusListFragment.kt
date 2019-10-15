package rawdermapps.watoolkit.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import rawdermapps.watoolkit.MediaType
import rawdermapps.watoolkit.R
import rawdermapps.watoolkit.activity.MediaPreviewActivity
import rawdermapps.watoolkit.adapter.MediaFilesAdapter
import rawdermapps.watoolkit.model.MediaItem

class StatusListFragment : Fragment() {

    companion object {

        private const val ARG_MEDIA_TYPE = "rawdermapps.watoolkit.fragment.SatausListFragment.ARG_MEDIA_TYPE"

        fun newInstance(type: MediaType) :StatusListFragment {
            return StatusListFragment().apply {
                val args = Bundle()
                args.putInt(ARG_MEDIA_TYPE, type.ordinal)
                arguments = args
            }
        }
    }

    private lateinit var adapter: MediaFilesAdapter
    private lateinit var mediaType: MediaType

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.items_list_layout, container, false)

    /* Adapter is set here to make sure that the UI thread does not blocks
     * to load thumbnails while fragment is being attached */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mediaType = MediaType.values()[arguments!!.getInt(ARG_MEDIA_TYPE)]

        view.apply {
            val recycler = findViewById<RecyclerView>(R.id.recycler)
            recycler.layoutManager = GridLayoutManager(context, 2)
            adapter = MediaFilesAdapter(mediaType) { onItemClick(it) }
            recycler.adapter = adapter

            if (adapter.isEmpty) findViewById<TextView>(R.id.tv_no_items).visibility = View.VISIBLE
        }
    }

    /* Called when an item from recycler view is clicked */
    private fun onItemClick(item: MediaItem) {
        Intent(context, MediaPreviewActivity::class.java).apply {
            putExtra(MediaPreviewActivity.EXTRA_FILE_PATH, item.file.absolutePath)
            putExtra(MediaPreviewActivity.EXTRA_MEDIA_TYPE, mediaType.ordinal)
            startActivity(this)
        }
    }
}