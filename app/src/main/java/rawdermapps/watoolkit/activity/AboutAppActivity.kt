package rawdermapps.watoolkit.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.franmontiel.attributionpresenter.AttributionPresenter
import com.franmontiel.attributionpresenter.entities.Attribution
import com.franmontiel.attributionpresenter.entities.License
import kotlinx.android.synthetic.main.activity_about_app.*
import kotlinx.android.synthetic.main.appbar.*
import rawdermapps.watoolkit.AppConstants
import rawdermapps.watoolkit.R

class AboutAppActivity :AppCompatActivity() {

    private lateinit var listAdapter :ArrayAdapter<String>
    private val listItems = arrayOf("Email Us", "Open Source Licences")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_app)


        setSupportActionBar(appbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tv_app_ver.text = getString(R.string.app_name) + AppConstants.APP_VERSION

        listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems)
        about_list.adapter = listAdapter
        about_list.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> {
                    Intent(Intent.ACTION_SENDTO).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_EMAIL, AppConstants.DEVELOPER_EMAIL)
                        startActivity(Intent.createChooser(this, "Send using"))
                    }
                }

                1 -> { showLicences() }
            }
        }
    }

    private fun showLicences() {
        val presenter = AttributionPresenter.Builder(this)

            .addAttributions(
            Attribution.Builder("AttributionPresenter")
                .addCopyrightNotice("Copyright 2017 Francisco José Montiel Navarro")
                .addLicense(License.APACHE)
                .setWebsite("https://github.com/franmontiel/AttributionPresenter")
                .build())

            .addAttributions(
                Attribution.Builder("FABProgressCircle")
                    .addCopyrightNotice("Copyright 2015 Jorge Castillo Pérez")
                    .addLicense(License.APACHE)
                    .setWebsite("https://github.com/JorgeCastilloPrz/FABProgressCircle")
                    .build())

            .addAttributions(
                Attribution.Builder("Country Code Picker Library")
                    .addCopyrightNotice("Copyright (C) 2016 Harsh Bhakta")
                    .addLicense(License.APACHE)
                    .setWebsite("https://github.com/hbb20/CountryCodePickerProject")
                    .build())

            .build()
    }
}