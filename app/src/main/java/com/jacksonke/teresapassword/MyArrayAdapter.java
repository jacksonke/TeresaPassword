package com.jacksonke.teresapassword;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

public class MyArrayAdapter<T> extends ArrayAdapter {

  private static final String TAG = "MyArrayAdapter";

  private OnItemSubViewClickListener itemSubViewClickListener = null;

  public MyArrayAdapter(@NonNull Context context, int resource) {
    super(context, resource);
  }

  public MyArrayAdapter(@NonNull Context context, int resource, int textViewResourceId) {
    super(context, resource, textViewResourceId);
  }

  public MyArrayAdapter(@NonNull Context context, int resource, @NonNull Object[] objects) {
    super(context, resource, objects);
  }

  public MyArrayAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull Object[] objects) {
    super(context, resource, textViewResourceId, objects);
  }

  public MyArrayAdapter(@NonNull Context context, int resource, @NonNull List objects) {
    super(context, resource, objects);
  }

  public MyArrayAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List objects) {
    super(context, resource, textViewResourceId, objects);
  }

  public void setOnItemSubViewClickListener(OnItemSubViewClickListener onItemSubViewClickListener){
    itemSubViewClickListener = onItemSubViewClickListener;
  }

  @NonNull
  @Override
  public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    View view = super.getView(position, convertView, parent);

    view.findViewById(R.id.btn_del).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (itemSubViewClickListener != null){
          itemSubViewClickListener.onItemSubViewDelClicked(position, v);
        }
      }
    });

    view.findViewById(R.id.btn_edit).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (itemSubViewClickListener != null){
          itemSubViewClickListener.onItemSubViewEditClicked(position, v);
        }
      }
    });
    return view;
  }

  public static interface OnItemSubViewClickListener{
    void onItemSubViewDelClicked(int position, @NonNull View view);
    void onItemSubViewEditClicked(int position, @NonNull View view);
  }

}
