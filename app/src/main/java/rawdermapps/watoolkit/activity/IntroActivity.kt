package rawdermapps.watoolkit.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntroFragment
import com.github.paolorotolo.appintro.model.SliderPage
import rawdermapps.watoolkit.R
import rawdermapps.watoolkit.util.PreferenceManager

class IntroActivity : AppIntro() {

    companion object {
        const val REQUEST_WRITE_EXTERNAL_STORAGE = 66
    }

    private var storagePermitted = false

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)

        setZoomAnimation()

        val welcomeSlide = SliderPage().apply {
            title = "Welcome to WhatsNery!"
            description = "WhatsNery helps you save pictures " +
                    "and videos statuses from WhatsApp with ease."
            bgColor = ActivityCompat.getColor(this@IntroActivity, R.color.colorAppBlue)
        }

        addSlide(AppIntroFragment.newInstance(welcomeSlide))

        val messageSlide = SliderPage().apply {
            title = "Add chats to WhatsApp without adding them to your Contacts !"
            description = "Just type-in the phone number of the person to text and" +
                    " hit the send message button!"
            bgColor = ActivityCompat.getColor(this@IntroActivity, R.color.colorPink)
        }

        addSlide(AppIntroFragment.newInstance(messageSlide))

        val statusSlide = SliderPage().apply {
            title = "Save your favorite WhatsApp statuses!"
            description = "Use the tabs at the bottom to choose between Images and Videos. " +
                    "Tap the one you want to save. Saving is just a click away!"
            bgColor = ActivityCompat.getColor(this@IntroActivity, R.color.colorYellow)
        }

        addSlide(AppIntroFragment.newInstance(statusSlide))

        storagePermitted = ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

        if (!storagePermitted) {

            val permissionSlide = SliderPage().apply {
                title = "Just one last step to get you started!"
                description = "We need permission to access your storage space " +
                        "to let you save your statues"
                bgColor = ActivityCompat.getColor(this@IntroActivity, R.color.colorGreen)
            }

            addSlide(AppIntroFragment.newInstance(permissionSlide))
            skipButtonEnabled = false
        }
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)

        PreferenceManager(this).declareIntroPlayed()
        finish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)

        if (storagePermitted) {
            PreferenceManager(this).declareIntroPlayed()
            finish()
        } else {

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_WRITE_EXTERNAL_STORAGE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {

        when (requestCode) {
            REQUEST_WRITE_EXTERNAL_STORAGE -> {
                // Either accepted or denied, we let user go through this
                PreferenceManager(this).declareIntroPlayed()
                finish()
            }

            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}