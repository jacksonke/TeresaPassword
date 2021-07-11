package com.jacksonke.teresapassword;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

public class AddItemDialogFragment extends DialogFragment implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {
  private static final String TAG = "AddItemDialogFragment";
  EditText mEditText;
  private RadioGroup passTypeGroup = null;
  private Handler mHandler = null;

  public void setHandler(Handler handler){
    mHandler = handler;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_add_item, container, false);
    setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog);

    mEditText = v.findViewById(R.id.et_site_name);
    v.findViewById(R.id.dialog_ok).setOnClickListener(this);
    v.findViewById(R.id.dialog_cancel).setOnClickListener(this);

    passTypeGroup = v.findViewById(R.id.pass_type_group);
    passTypeGroup.setOnCheckedChangeListener(this);

    return v;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public void onClick(View v) {
    int vid = v.getId();
    if (vid == R.id.dialog_ok){
      String siteName = mEditText.getText().toString();
      Log.d(TAG, "siteName:" + siteName);

      int passType = passTypeAdapter(passTypeGroup.getCheckedRadioButtonId());
      Log.d(TAG, "onClick: type=" + passType);

      SiteEntity siteEntity = new SiteEntity(siteName);
      siteEntity.ver = 1;
      siteEntity.type = passType;

//      Log.d(TAG, "onClick: befor . entity=" + siteEntity.getDebugString());
      DBHelper.getInstance().insertSiteEntity(siteEntity);
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
      if (mHandler!=null){
        Message msg = mHandler.obtainMessage(Constant.MSG_ITEM_ADDED);
        msg.obj = siteName;
        msg.arg1 = passType;
        mHandler.sendMessage(msg);
      }
      dismiss();
    }
    else if (vid == R.id.dialog_cancel){
      dismiss();
    }
  }

  int passTypeAdapter(int resId){
    int type = Constant.TYPE_NUMBER_ABC;
    switch (resId){
      case R.id.radio_btn_letter:
        type = Constant.TYPE_ABC;
        break;
      case R.id.radio_btn_number:
        type = Constant.TYPE_NUMBER;
        break;
      case R.id.radio_btn_letter_number:
        type = Constant.TYPE_NUMBER_ABC;
        break;
      case R.id.radio_btn_letter_number_symbol:
        type = Constant.TYPE_NUMBER_ABC_SYMBOL;
        break;
      default:
          break;
    }

    return type;
  }

  @Override
  public void onCheckedChanged(RadioGroup group, int checkedId) {

  }
}
