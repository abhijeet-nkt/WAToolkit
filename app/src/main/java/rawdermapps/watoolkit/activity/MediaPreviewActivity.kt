package rawdermapps.watoolkit.activity

import android.graphics.RectF
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.activity_preview.*
import rawdermapps.watoolkit.BuildConfig
import rawdermapps.watoolkit.util.GoogleAdsHelper
import rawdermapps.watoolkit.R
import rawdermapps.watoolkit.MediaType
import rawdermapps.watoolkit.util.PreferenceManager
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

class MediaPreviewActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_FILE_PATH =
            "rawdermapps.watoolkit.activity.MediaPreviewActivity.EXTRA_FILE_PATH"
        const val EXTRA_MEDIA_TYPE =
            "rawdermapps.watoolkit.activity.MediaPreviewActivity.EXTRA_MEDIA_TYPE"
    }

    //True when fab is once pressed by user
    private var fabPressed = false

    private var exoPlayer: SimpleExoPlayer? = null

    private lateinit var mMediaType: MediaType
    private lateinit var mFilePath: String
    private lateinit var mUri: Uri
    private lateinit var mInterstitialAd: InterstitialAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        setSupportActionBar(findViewById(R.id.appbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mMediaType = MediaType.values()[intent.getIntExtra(EXTRA_MEDIA_TYPE, 0)]
        mFilePath = intent.getStringExtra(EXTRA_FILE_PATH)

        mUri = Uri.fromFile(File(mFilePath))

        when (mMediaType ) {
            MediaType.PICTURE -> {
                picture_view.visibility = View.VISIBLE
                previewPicture()
            }

            MediaType.VIDEO -> {
                exo_player_view.visibility = View.VISIBLE
                previewVideo()
            }
        }

        fabCircle.attachListener {
            Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
            fabCircle.hide()
            showAd()
        }

        fab.setOnClickListener {
            //Only one click is allowed!
            if (!fabPressed) {
                fabPressed = true
                fabCircle.show()
                saveFile()
            }
        }

        if (!PreferenceManager(this).isSaveWalkThroughCompleted) {
            TapTargetSequence(this)
                .target(
                    TapTarget.forView(
                        fab,
                        "One more last step!",
                        "Tap on the action button to save this status to your gallery!")
                        .drawShadow(true)
                        .targetCircleColor(R.color.colorAppBlue)
                        .targetCircleColor(R.color.white)
                        .icon(ContextCompat.getDrawable(this, R.drawable.ic_save))
                        .descriptionTextAlpha(1f)
                        .cancelable(false))

                .listener(object : TapTargetSequence.Listener {
                    override fun onSequenceFinish() {
                        fab.callOnClick()
                        PreferenceManager(this@MediaPreviewActivity).isSaveWalkThroughCompleted = true
                    }

                    //Not used
                    override fun onSequenceCanceled(lastTarget: TapTarget?) {}
                    override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {}
                })
                .start()
        }

        //Set up interstitial ads
        MobileAds.initialize(this)
        mInterstitialAd = InterstitialAd(this).apply {
            adUnitId =
                if (BuildConfig.DEBUG)
                    GoogleAdsHelper.TEST_INTERSTITIAL_UNIT_ID
                else GoogleAdsHelper.INTERSTITIAL_UNIT_ID
            loadAd(AdRequest.Builder().build())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
    }

    private fun previewVideo() {
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this)
        exo_player_view.player = exoPlayer

        val dataSourceFactory = DefaultDataSourceFactory(
            this,
            Util.getUserAgent(this, "WAToolkit")
        )

        val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(mUri)

        exoPlayer?.prepare(videoSource)
        exoPlayer?.playWhenReady = true
    }

    private fun previewPicture() = picture_view.setImageURI(mUri)

    private fun saveFile() = Thread {
        //Selected status file
        val source = File(mFilePath)

        //The folder where we save file
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            getString(R.string.app_name))

        dir.mkdir() //Create the folder, as it may not exist

        val dest = File(dir, source.name) //This is where we save file
        val inputStream = FileInputStream(source)
        val outputStream = FileOutputStream(dest)

        val buffer = ByteArray(512)

        while (true) {
            val chunkLen = inputStream.read(buffer)
            if (chunkLen == -1)
                break
            outputStream.write(buffer, 0, chunkLen)
        }

        inputStream.close()
        outputStream.flush()
        outputStream.close()
        addToGallery(dest.absolutePath)

        fabCircle.beginFinalAnimation()
    }.run()

    /* Scans the file so as to make it visible in the gallery */
    private fun addToGallery(path: String) =
        MediaScannerConnection.scanFile(this, arrayOf(path), null) { _, _ -> }

    /* Shows an interstitial ad */
    private fun showAd() {
        if (mInterstitialAd.isLoaded)
            runOnUiThread { mInterstitialAd.show() }
        else
            mInterstitialAd.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    runOnUiThread { mInterstitialAd.show() }
                }
            }
    }
}