package com.jacksonke.teresapassword

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.jacksonke.teresapassword.databinding.FragmentEditItemBinding


class EditItemDialogFragment : DialogFragment(), View.OnClickListener {

    private var _binding:FragmentEditItemBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var target: SiteEntity? = null
    private var mHandler: Handler? = null
    fun setHandler(handler: Handler?) {
        mHandler = handler
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEditItemBinding.inflate(inflater, container, false)

        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog)

        binding.etSiteName.setText(target!!.name)
        binding.dialogUpdate.setOnClickListener(this)
        binding.dialogCancel.setOnClickListener(this)
        binding.passTypeGroup.check(passTypeToResid(target!!.type))

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onClick(v: View) {
        val vid = v.id
        if (vid == R.id.dialog_update) {
            val siteName = binding.etSiteName.text.toString()
            Log.d(TAG, "siteName:$siteName")
            val passType = passTypeAdapter(binding.passTypeGroup.checkedRadioButtonId)
            target!!.ver++
            target!!.type = passType
            DBHelper.instance.updateSiteEntity(target!!)
            Toast.makeText(context, R.string.toast_pwd_clipboard, Toast.LENGTH_SHORT).show()
            if (mHandler != null) {
                val msg: Message = mHandler!!.obtainMessage(Constants.MSG_ITEM_UPDATED)
                msg.obj = siteName
                mHandler!!.sendMessage(msg)
            }
            dismiss()
        } else if (vid == R.id.dialog_cancel) {
            dismiss()
        }
    }

    private fun passTypeAdapter(resId: Int): Int {
        var type = Constants.TYPE_NUMBER_ABC
        when (resId) {
            R.id.radio_btn_letter -> type = Constants.TYPE_ABC
            R.id.radio_btn_number -> type = Constants.TYPE_NUMBER
            R.id.radio_btn_letter_number -> type = Constants.TYPE_NUMBER_ABC
            R.id.radio_btn_letter_number_symbol -> type = Constants.TYPE_NUMBER_ABC_SYMBOL
            else -> {}
        }
        return type
    }

    private fun passTypeToResid(type: Int): Int {
        return when (type) {
            Constants.TYPE_ABC -> R.id.radio_btn_letter
            Constants.TYPE_NUMBER -> R.id.radio_btn_number
            Constants.TYPE_NUMBER_ABC -> R.id.radio_btn_letter_number
            Constants.TYPE_NUMBER_ABC_SYMBOL -> R.id.radio_btn_letter_number_symbol
            else -> R.id.radio_btn_letter
        }
    }

    fun setTarget(entity: SiteEntity?) {
        target = entity
    }

    companion object {
        private const val TAG = "EditItemDialogFragment"
    }
}