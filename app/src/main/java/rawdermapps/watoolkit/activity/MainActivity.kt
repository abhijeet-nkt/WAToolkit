package rawdermapps.watoolkit.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NavUtils
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_content.*
import kotlinx.android.synthetic.main.appbar.*
import rawdermapps.watoolkit.BuildConfig
import rawdermapps.watoolkit.R
import rawdermapps.watoolkit.fragment.MediaType
import rawdermapps.watoolkit.fragment.StatusListFragment
import rawdermapps.watoolkit.fragment.SendMessageFragment
import rawdermapps.watoolkit.util.GoogleAdsHelper
import rawdermapps.watoolkit.util.PreferenceManager

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

        if (!PreferenceManager(this).isIntroPlayed)
            startActivity(Intent(this, IntroActivity::class.java))

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
            R.id.nav_about -> startActivity(Intent(this, AboutAppActivity::class.java))
            R.id.nav_rate -> {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=rawdermapps.watoolkit")
                    )
                )
            }
            R.id.nav_share -> {
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "http://play.google.com/store/apps/details?id=rawdermapps.watoolkit"
                    )
                    startActivity(Intent.createChooser(this, "Share with..."))
                }
            }
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

            when (item.itemId) {
                // 'Send message' tab
                R.id.navigation_send_message -> {
                    currentFragment = sendMessageFragment
                    mFragmentManager.beginTransaction()
                        .replace(R.id.container, currentFragment)
                        .commit()
                    supportActionBar?.title = ""
                    return@OnNavigationItemSelectedListener true
                }

                // 'Picture status' tab
                R.id.navigation_image_status -> {
                    currentFragment = imageStatusFragment
                    storagePermissionFlow() //This handles the replacing of fragment
                    supportActionBar?.title = getString(R.string.app_name)
                    return@OnNavigationItemSelectedListener true
                }

                // 'Video status' tab
                R.id.navigation_video_status -> {
                    currentFragment = videoStatusFragment
                    storagePermissionFlow() //This handles the replacing of fragment
                    supportActionBar?.title = getString(R.string.app_name)
                    return@OnNavigationItemSelectedListener true
                }

                else -> return@OnNavigationItemSelectedListener false
            }
        }

    /* Simply requests the permission
     * onRequestPermissionResult() is instantly called after this */
    private fun storagePermissionFlow() {
        //Request if there's no permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                MediaPreviewActivity.REQUEST_WRITE_EXTERNAL_STORAGE
            )

        } else {
            //We have the permission
            //Replace the fragment

            mFragmentManager.beginTransaction()
                .replace(R.id.container, currentFragment)
                .commit()
        }
    }

    /* Shows a proper dialog for telling user about permission
     * This function should only be called when user denied the permission request */
    private fun showStoragePermissionRequestDialog() {

        //User clicked 'deny'
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            AlertDialog.Builder(this)
                .setMessage("We need permission in order to save your status")
                .setPositiveButton("Okay") { dialog, _ ->
                    dialog.dismiss()
                    ActivityCompat.requestPermissions(
                        this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        MediaPreviewActivity.REQUEST_WRITE_EXTERNAL_STORAGE
                    )
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                    //Change to message tab (User canceled)
                    bottom_navgation.selectedItemId = R.id.navigation_send_message
                }.setCancelable(false).create().show()

            //User clicked 'deny' with 'do not ask again'
        } else {
            AlertDialog.Builder(this)
                .setMessage(
                    "We need permission in order to save your status./n" +
                            "You'll be sent to settings to turn on permissions"
                )
                .setPositiveButton("Okay") { dialog, _ ->
                    dialog.dismiss()
                    //Send user to app settings page
                    Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", packageName, null)
                        startActivity(this)
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                    //Change to message tab (User canceled)
                    bottom_navgation.selectedItemId = R.id.navigation_send_message
                }.setCancelable(false).create().show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            MediaPreviewActivity.REQUEST_WRITE_EXTERNAL_STORAGE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Reload the fragment
                    mFragmentManager.beginTransaction()
                        .replace(R.id.container, currentFragment)
                        .commit()
                } else {
                    //Keep trying until user allows the permission, lol
                    showStoragePermissionRequestDialog()
                }
            }

            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}
