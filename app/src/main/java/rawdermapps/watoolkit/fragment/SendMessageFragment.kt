package rawdermapps.watoolkit.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.hbb20.CountryCodePicker
import kotlinx.android.synthetic.main.frag_send_message.view.*
import rawdermapps.watoolkit.R

class SendMessageFragment : Fragment() {

    lateinit var sendButton: Button
    lateinit var editPhone: EditText
    lateinit var editMessage: EditText
    lateinit var ccp: CountryCodePicker

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.frag_send_message, container, false).also {
            sendButton = it.button_send
            editPhone = it.edit_phone
            editMessage = it.edit_message
            ccp = it.ccp

            ccp.registerCarrierNumberEditText(editPhone)

            sendButton.setOnClickListener { onClickSend() }
        }

    private fun onClickSend() {
        if (!ccp.isValidFullNumber) {
            editPhone.error = getString(R.string.warn_invalid_phone_number)
            return
        }

        val phone = ccp.fullNumberWithPlus
        val message = editMessage.text.toString()

        startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("http://api.whatsapp.com/send?phone=$phone&text=$message")
            }
        )
    }
}