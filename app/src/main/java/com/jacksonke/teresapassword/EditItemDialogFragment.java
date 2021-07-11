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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class EditItemDialogFragment extends DialogFragment implements View.OnClickListener{
  EditText mEditText;
  private RadioGroup passTypeGroup = null;
  private SiteEntity target = null;
  private static final String TAG = "EditItemDialogFragment";
  private Handler mHandler = null;

  public void setHandler(Handler handler){
    mHandler = handler;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_edit_item, container, false);
    setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog);

    mEditText = v.findViewById(R.id.et_site_name);
    mEditText.setText(target.name);

    v.findViewById(R.id.dialog_update).setOnClickListener(this);
    v.findViewById(R.id.dialog_cancel).setOnClickListener(this);

    passTypeGroup = v.findViewById(R.id.pass_type_group);

    RadioButton letterButton = v.findViewById(passTypeToResid(target.type));
    letterButton.setChecked(true);

    return v;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public void onClick(View v) {
    int vid = v.getId();
    if (vid == R.id.dialog_update){
      String siteName = mEditText.getText().toString();
      Log.d(TAG, "siteName:" + siteName);

      int passType = passTypeAdapter(passTypeGroup.getCheckedRadioButtonId());

      target.ver++;
      target.type = passType;
      DBHelper.getInstance().updateSiteEntity(target);

      Toast.makeText(getContext(), R.string.toast_pwd_clipboard, Toast.LENGTH_SHORT).show();
      if (mHandler!=null){
        Message msg = mHandler.obtainMessage(Constant.MSG_ITEM_UPDATED);
        msg.obj = siteName;
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

  int passTypeToResid(int type){
    int resId = R.id.radio_btn_letter;
    switch (type){
      case Constant.TYPE_ABC:
        break;
      case Constant.TYPE_NUMBER:
        resId = R.id.radio_btn_number;
        break;
      case Constant.TYPE_NUMBER_ABC:
        resId = R.id.radio_btn_letter_number;
        break;
      case Constant.TYPE_NUMBER_ABC_SYMBOL:
        resId = R.id.radio_btn_letter_number_symbol;
        break;
      default:
        break;
    }

    return resId;
  }

  public void setTarget(SiteEntity entity){
    target = entity;
  }

}
