package rawdermapps.watoolkit.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import rawdermapps.watoolkit.MediaType
import rawdermapps.watoolkit.R
import rawdermapps.watoolkit.activity.MainActivity
import rawdermapps.watoolkit.activity.MediaPreviewActivity
import rawdermapps.watoolkit.adapter.MediaFilesAdapter
import rawdermapps.watoolkit.model.MediaItem
import rawdermapps.watoolkit.util.PreferenceManager

class StatusListFragment : Fragment() {

    companion object {
        const val PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 22
        private const val ARG_MEDIA_TYPE = "rawdermapps.watoolkit.fragment.SatausListFragment.ARG_MEDIA_TYPE"

        fun newInstance(type: MediaType) :StatusListFragment {
            return StatusListFragment().apply {
                val args = Bundle()
                args.putInt(ARG_MEDIA_TYPE, type.ordinal)
                arguments = args
            }
        }
    }

    private lateinit var mediaType: MediaType
    private lateinit var noPermissionLayout :LinearLayout
    private lateinit var noPermissionDescTv :TextView
    private lateinit var givePermissionButton: Button
    private lateinit var mAdapter: MediaFilesAdapter
    private lateinit var mRecycler : RecyclerView
    private lateinit var mParentActivity :MainActivity

    /* Only inflate and return view */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.items_list_layout, container, false)

    /* Obvious use */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mParentActivity = activity as MainActivity
        mediaType = MediaType.values()[arguments!!.getInt(ARG_MEDIA_TYPE)]

        noPermissionLayout = view.findViewById(R.id.no_permission_layout)
        noPermissionDescTv = view.findViewById(R.id.tv_permission_desc)
        givePermissionButton = view.findViewById(R.id.button_give_permission)

        givePermissionButton.setOnClickListener { onClickGivePermission() }

        mRecycler = view.findViewById(R.id.recycler)
        mRecycler.layoutManager = GridLayoutManager(context, 2)
        mAdapter = MediaFilesAdapter(mediaType) { onItemClick(it) }
        mRecycler.adapter = mAdapter

        if (hasPermission)
            //We got permission, just continue
            loadRecyclerItems()
        else
            requestPermission() //This function handles rest
    }

    /* Called by the parent activity */
    fun onPermissionResult(permitted : Boolean) {
        if (permitted) {
            //We got the permission, yay!
            if (view != null) { //If view was created
                noPermissionLayout.visibility = View.GONE
                loadRecyclerItems()
            }

        } else { //Permission was rejected

            noPermissionLayout.visibility = View.VISIBLE

            if (canAskPermission) { //Can show a permission request dialog
                noPermissionDescTv.text = getString(R.string.info_storage_permission)
                givePermissionButton.text = getString(R.string.act_give_permission)

            } else { //Need to send user to settings
                noPermissionDescTv.text = getString(R.string.info_storage_permission_denied)
                givePermissionButton.text = getString(R.string.act_give_permission_from_settings)
            }
        }
    }

    /* Called when give permission button is clicked */
    private fun onClickGivePermission() {
        if (canAskPermission)
            requestPermission()
        else {
            //Take user to settings
            startActivity(
                Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", mParentActivity.packageName, null)
                }
            )
        }
    }

    /* Called when permissions are satisfied for recycler to start loading items
     * This can be used as for 'refresh' in future */
    private fun loadRecyclerItems() {
        mRecycler.visibility = View.VISIBLE
        mAdapter.loadList()

        //No items indicator
        if (mAdapter.isEmpty)
            view?.findViewById<TextView>(R.id.tv_no_items)
                ?.visibility = View.VISIBLE

        if (!PreferenceManager(mParentActivity).isPreviewThroughCompleted) {
            mRecycler.addOnChildAttachStateChangeListener(object :

                RecyclerView.OnChildAttachStateChangeListener {
                override fun onChildViewAttachedToWindow(firstView: View) {

                    //We just want to do it once
                    mRecycler.removeOnChildAttachStateChangeListener(this)

                    TapTargetSequence(mParentActivity)
                        .target(
                            TapTarget.forView(
                                firstView,
                                "Preview status",
                                "Tap a status thumbnail to get it's preview!"
                            )
                                .drawShadow(true)
                                .targetCircleColor(R.color.colorAppBlue)
                                .targetCircleColor(R.color.white)
                                .outerCircleAlpha(.9f)
                                .icon(
                                    ContextCompat.getDrawable(
                                        mParentActivity,
                                        R.drawable.ic_touch
                                    )
                                )
                                .descriptionTextAlpha(1f)
                        )
                        .listener(object : TapTargetSequence.Listener {
                            override fun onSequenceFinish() {
                                mRecycler.findViewHolderForLayoutPosition(0)
                                    ?.itemView?.callOnClick()
                                PreferenceManager(mParentActivity).isPreviewThroughCompleted = true
                            }

                            //Not used
                            override fun onSequenceCanceled(lastTarget: TapTarget?) {}
                            override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {}
                        })
                        .start()
                }

                //Not used
                override fun onChildViewDetachedFromWindow(view: View) {}
            })
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

    private val hasPermission
        get() = ActivityCompat.checkSelfPermission(mParentActivity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun requestPermission() =
        ActivityCompat.requestPermissions(
            mParentActivity,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE)

    /* False if user has clicked don't ask again in permission dialog */
    private val canAskPermission
        get() = ActivityCompat.shouldShowRequestPermissionRationale(
            mParentActivity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
}