package com.jacksonke.teresapassword

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import com.jacksonke.teresapassword.databinding.FragmentAddEntityBinding

class AddItemDialogFragment : DialogFragment(), View.OnClickListener,
    RadioGroup.OnCheckedChangeListener {

    private var _binding: FragmentAddEntityBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private var mHandler: Handler? = null
    fun setHandler(handler: Handler?) {
        mHandler = handler
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEntityBinding.inflate(inflater, container, false)

        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Dialog)

        binding.dialogOk.setOnClickListener(this)
        binding.dialogCancel.setOnClickListener(this)
        binding.passTypeGroup.setOnCheckedChangeListener(this)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(v: View) {
        val vid = v.id
        if (vid == R.id.dialog_ok) {
            val siteName = binding.etSiteName.text.toString()
            Log.d(TAG, "siteName:$siteName")
            val passType = passTypeAdapter(binding.passTypeGroup.checkedRadioButtonId)
            Log.d(TAG, "onClick: type=$passType")
            val siteEntity = SiteEntity(siteName)
            siteEntity.ver = 1
            siteEntity.type = passType

//      Log.d(TAG, "onClick: befor . entity=" + siteEntity.getDebugString());
            DBHelper.instance.insertSiteEntity(siteEntity)
            //      Log.d(TAG, "onClick: after . entity=" + siteEntity.getDebugString());

            // todo: if siteName has already existed?
//      DBHelper.getInstance().insertSiteEntity(siteName);

//      // Gets a handle to the clipboard service.
//      ClipboardManager clipboard = (ClipboardManager)
//          getContext().getSystemService(Context.CLIPBOARD_SERVICE);
//
//      // Creates a new text clip to put on the clipboard
//      ClipData clip = ClipData.newPlainText("simple text", pass);

//      Toast.makeText(getContext(), R.string.toast_pwd_clipboard, Toast.LENGTH_SHORT).show();
            if (mHandler != null) {
                val msg = mHandler!!.obtainMessage(Constants.MSG_ITEM_ADDED)
                msg.obj = siteName
                msg.arg1 = passType
                mHandler!!.sendMessage(msg)
            }
            dismiss()
        } else if (vid == R.id.dialog_cancel) {
            dismiss()
        }
    }

    private fun passTypeAdapter(resId: Int): Int {
        val type = when (resId) {
            R.id.radio_btn_letter -> Constants.TYPE_ABC
            R.id.radio_btn_number -> Constants.TYPE_NUMBER
            R.id.radio_btn_letter_number -> Constants.TYPE_NUMBER_ABC
            R.id.radio_btn_letter_number_symbol -> Constants.TYPE_NUMBER_ABC_SYMBOL
            else -> {Constants.TYPE_NUMBER_ABC}
        }
        return type
    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {}

    companion object {
        private const val TAG = "AddItemDialogFragment"
    }
}