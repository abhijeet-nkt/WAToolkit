package rawdermapps.watoolkit.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NavUtils
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_content.*
import kotlinx.android.synthetic.main.activity_preview.*
import kotlinx.android.synthetic.main.appbar.*
import kotlinx.android.synthetic.main.appbar.appbar
import kotlinx.android.synthetic.main.frag_send_message.*
import rawdermapps.watoolkit.BuildConfig
import rawdermapps.watoolkit.R
import rawdermapps.watoolkit.MediaType
import rawdermapps.watoolkit.fragment.StatusListFragment
import rawdermapps.watoolkit.fragment.SendMessageFragment
import rawdermapps.watoolkit.util.AppConstants
import rawdermapps.watoolkit.util.GoogleAdsHelper
import rawdermapps.watoolkit.util.PreferenceManager
import rawdermapps.watoolkit.util.getTabViewAt

/* This activity doesn't handles much of the app's logic
 * It just links the fragments to the bottom navigation bar
 * The individual fragments independently handle their functionality */
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        const val KEY_CURRENT_TAB = "rawdermapps.watoolkit.activity.MainActivity.KEY_CURRENT_TAB"
        const val TAB_MESSAGE = 1
        const val TAB_IMAGE_STATUS = 2
        const val TAB_VIDEO_STATUS = 3
    }

    private lateinit var currentFragment: Fragment
    private var currentTab = TAB_MESSAGE

    private lateinit var mFragmentManager : FragmentManager

    private val sendMessageFragment = SendMessageFragment()
    private val imageStatusFragment = StatusListFragment.newInstance(MediaType.PICTURE)
    private val videoStatusFragment = StatusListFragment.newInstance(MediaType.VIDEO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Appbar
        setSupportActionBar(appbar)
        if (savedInstanceState == null)
            supportActionBar?.title = ""

        //Fragment manager
        mFragmentManager = supportFragmentManager
        currentFragment = sendMessageFragment
        mFragmentManager.beginTransaction()
            .replace(R.id.container, currentFragment)
            .commit()

        //Bottom navigation
        bottom_navgation.setOnNavigationItemSelectedListener(mBottomNavigationItemListener)

        //App drawer
        val toggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            appbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)

        if (!PreferenceManager(this).isMainWalkThroughCompleted) {
            bottom_navgation.post {

                val imageTab = bottom_navgation.getTabViewAt(1)
                val videoTab = bottom_navgation.getTabViewAt(2)

                val sendButtonCoords = intArrayOf(0, 0)
                button_send.getLocationInWindow(sendButtonCoords)
                val sendButtonBounds = Rect(sendButtonCoords[0],
                    sendButtonCoords[1],
                    sendButtonCoords[0] + button_send.width,
                    sendButtonCoords[1] + button_send.height)

                TapTargetSequence(this)
                    .targets(

                        TapTarget.forBounds(sendButtonBounds,
                            "Welcome to WhatsNery!",
                            "Add chats to WhatsApp without saving their phone number!" +
                                    "\nJust fill in the phone number in the given field and tap " +
                                    "this button.")
                            .drawShadow(true)
                            .targetCircleColor(R.color.colorAppBlue)
                            .targetCircleColor(R.color.white)
                            .icon(ContextCompat.getDrawable(this, R.drawable.ic_touch))
                            .descriptionTextAlpha(1f),

                        TapTarget.forView(
                            imageTab,
                            "Image status",
                            "Use this tab to see all your Images in WhatsApp statuses!"
                        )
                            .drawShadow(true)
                            .targetCircleColor(R.color.colorAppBlue)
                            .targetCircleColor(R.color.white)
                            .icon(ContextCompat.getDrawable(this, R.drawable.ic_image))
                            .descriptionTextAlpha(1f),
                        TapTarget.forView(
                            videoTab,
                            "Your videos are here!",
                            "Video statuses are arranged here separately for your convenience"
                        )
                            .drawShadow(true)
                            .targetCircleColor(R.color.colorAppBlue)
                            .targetCircleColor(R.color.white)
                            .icon(ContextCompat.getDrawable(this, R.drawable.ic_video))
                            .descriptionTextAlpha(1f)
                        )

                    .listener(object : TapTargetSequence.Listener {
                        override fun onSequenceFinish() {
                            imageTab.callOnClick()
                            PreferenceManager(this@MainActivity).isMainWalkThroughCompleted =
                                true
                        }

                        //Not used
                        override fun onSequenceCanceled(lastTarget: TapTarget?) {}

                        override fun onSequenceStep(
                            lastTarget: TapTarget?,
                            targetClicked: Boolean
                        ) {
                        }
                    })
                    .start()
            }
        }

        //Set-up banner ads
        MobileAds.initialize(this) {}
        AdView(this).apply {
            adSize = AdSize.BANNER

            adUnitId =
                if (BuildConfig.DEBUG)
                    GoogleAdsHelper.TEST_BANNER_UNIT_ID
                else
                    GoogleAdsHelper.BANNER_UNIT_ID

            loadAd(AdRequest.Builder().build())
            bannerAdHolder.addView(this)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_CURRENT_TAB, currentTab)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentTab = savedInstanceState.getInt(KEY_CURRENT_TAB)
        bottom_navgation.selectedItemId = currentNavigationId
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.nav_rate -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AppConstants.APP_MARKET_LINK)))

            R.id.nav_share -> {
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT,
                        "${getString(R.string.share_text)}\n${AppConstants.APP_SHARE_LINK}"
                    )
                    startActivity(Intent.createChooser(this, "Share with..."))
                }
            }

            R.id.nav_play_intro -> {
                PreferenceManager(this).apply {
                    isSaveWalkThroughCompleted = false
                    isMainWalkThroughCompleted = false
                    isPreviewThroughCompleted = false
                    recreate()
                }
            }

            R.id.nav_about -> startActivity(Intent(this, AboutAppActivity::class.java))
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    /* Returns the id of the navigation item based on the value of `currentTab` */
    private val currentNavigationId: Int
        get() =
            when (currentTab) {
                TAB_IMAGE_STATUS -> R.id.navigation_image_status
                TAB_VIDEO_STATUS -> R.id.navigation_video_status
                else -> R.id.navigation_send_message
            }

    private val mBottomNavigationItemListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->

            val transaction = mFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)

            when (item.itemId) {
                // 'Send message' tab
                R.id.navigation_send_message -> {
                    currentFragment = sendMessageFragment
                    transaction
                        .replace(R.id.container, currentFragment)
                        .commit()
                    supportActionBar?.title = ""
                    return@OnNavigationItemSelectedListener true
                }

                // 'Picture status' tab
                R.id.navigation_image_status -> {
                    currentFragment = imageStatusFragment
                    transaction
                        .replace(R.id.container, currentFragment)
                        .commit()
                    supportActionBar?.title = getString(R.string.app_name)
                    return@OnNavigationItemSelectedListener true
                }

                // 'Video status' tab
                R.id.navigation_video_status -> {
                    currentFragment = videoStatusFragment
                    transaction
                        .replace(R.id.container, currentFragment)
                        .commit()
                    supportActionBar?.title = getString(R.string.app_name)
                    return@OnNavigationItemSelectedListener true
                }

                else -> return@OnNavigationItemSelectedListener false
            }
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {

        when (requestCode) {
            StatusListFragment.PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE -> {
                if (currentFragment is StatusListFragment)
                    (currentFragment as StatusListFragment)
                        .onPermissionResult(
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
            }

            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}
