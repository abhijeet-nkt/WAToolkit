package rawdermapps.watoolkit.activity

import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import rawdermapps.watoolkit.GoogleAdsHelper
import rawdermapps.watoolkit.R
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class MediaPreviewActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_FILE_PATH = "rawdermapps.watoolkit.activity.MediaPreviewActivity.EXTRA_FILE_PATH"
        const val EXTRA_MEDIA_TYPE = "rawdermapps.watoolkit.activity.MediaPreviewActivity.EXTRA_MEDIA_TYPE"

        const val MEDIA_TYPE_PICTURE = 1
        const val MEDIA_TYPE_VIDEO = 2

        const val REQUEST_WRITE_EXTERNAL_STORAGE = 20
    }

    //True when fab is once pressed by user
    private var fabPressed = false

    private var exoPlayer :SimpleExoPlayer? = null

    private var mMediaType :Int = 0
    private var mFilePath :String? = null
    private var mUri :Uri? = null

    private lateinit var mInterstitialAd: InterstitialAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        setSupportActionBar(findViewById(R.id.appbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mMediaType = intent.getIntExtra(EXTRA_MEDIA_TYPE, 0)
        mFilePath = intent.getStringExtra(EXTRA_FILE_PATH)

        mUri = Uri.fromFile(File(mFilePath))

        if (mMediaType == MEDIA_TYPE_PICTURE) {
            picture_view.visibility = View.VISIBLE
            previewPicture()
        } else if (mMediaType == MEDIA_TYPE_VIDEO) {
            exo_player_view.visibility = View.VISIBLE
            previewVideo()
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

        //Set up interstitial ads
        MobileAds.initialize(this)
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = GoogleAdsHelper.TEST_INTERSTITIAL_UNIT_ID
        mInterstitialAd.loadAd(AdRequest.Builder().build())
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
    }

    private fun previewVideo() {
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this)
        exo_player_view.player = exoPlayer

        val dataSourceFactory = DefaultDataSourceFactory(this,
            Util.getUserAgent(this, "WAToolkit"))

        val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(mUri)

        exoPlayer?.prepare(videoSource)
        exoPlayer?.playWhenReady = true
    }

    private fun previewPicture() =
        picture_view.setImageURI(mUri)

    private fun saveFile() = Thread {
        //Selected status file
        val source = File(mFilePath)

        //The folder where we save file
        val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Status Saver")
        dir.mkdir() //Create the folder if it doesn't exists

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

        fabCircle.beginFinalAnimation()

        inputStream.close()
        outputStream.flush()
        outputStream.close()
        addToGallery(dest.absolutePath)
    }.run()

    //Scans the file so as to make it visible in the gallery
    private fun addToGallery(path :String) =
        MediaScannerConnection.scanFile(this, arrayOf(path), null) { _,_ -> }

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