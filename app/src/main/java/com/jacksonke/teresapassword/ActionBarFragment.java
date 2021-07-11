/***
  Copyright (c) 2008-2013 CommonsWare, LLC
  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain	a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS,	WITHOUT	WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.
  
  Covered in detail in the book _The Busy Coder's Guide to Android Development_
    https://commonsware.com/Android
 */

package com.jacksonke.teresapassword;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class ActionBarFragment extends ListFragment implements
    SearchView.OnQueryTextListener, SearchView.OnCloseListener, MyArrayAdapter.OnItemSubViewClickListener{
  private static final String STATE_QUERY="q";
  private static final String STATE_MODEL="m";
  private static final String TAG = "ActionBarFragment";
  private ArrayAdapter<SiteEntity> adapter=null;
  private CharSequence initialQuery=null;
  private SearchView sv=null;

  static class MyHandler extends Handler{
    WeakReference<ActionBarFragment> fragmentRef = new WeakReference<>(null);

    MyHandler(ActionBarFragment fragment){
      fragmentRef = new WeakReference<>(fragment);
    }

    @Override
    public void handleMessage(Message msg) {
      if (msg.what == Constant.MSG_ITEM_ADDED){
        Log.d(TAG, "handleMessage: new item added");
        ActionBarFragment fragment = fragmentRef.get();
        if (fragment == null){
          return;
        }

        String siteName =  (String)msg.obj;
        SiteEntity entity = DBHelper.getInstance().queryBySite(siteName);
        Log.d(TAG, "handleMessage: MSG_ITEM_ADDED: entity=" + entity);

        fragment.adapter.add(entity);
        fragment.adapter.notifyDataSetChanged();
      } else if (msg.what == Constant.MSG_ITEM_UPDATED){
        Log.d(TAG, "handleMessage: item updated");
        ActionBarFragment fragment = fragmentRef.get();
        if (fragment == null){
          return;
        }

        fragment.adapter.notifyDataSetChanged();
      }
    }
  }

  Handler mHandler = new MyHandler(this);


  @Override
  public void onPause() {
    Log.d(TAG, "onPause: ");
    super.onPause();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setHasOptionsMenu(true);
  }

  @Override
  public void onViewCreated(View v, Bundle savedInstanceState) {
    super.onViewCreated(v, savedInstanceState);

    if (savedInstanceState == null) {
      initAdapter();
    }
    else {
      initAdapter();
      initialQuery=savedInstanceState.getCharSequence(STATE_QUERY);
    }
  }

  @Override
  public void onResume() {
    Log.d(TAG, "onResume: ");
    super.onResume();
  }

  @Override
  public void onSaveInstanceState(Bundle state) {
    Log.d(TAG, "onSaveInstanceState: ");
    super.onSaveInstanceState(state);

    if (!sv.isIconified()) {
      state.putCharSequence(STATE_QUERY, sv.getQuery());
    }

//    state.putStringArrayList(STATE_MODEL, words);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.actions, menu);

    configureSearchView(menu);

    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Log.d(TAG, "onOptionsItemSelected: ");
    if (item.getItemId() == R.id.menu_id_add){
      FragmentTransaction ft = getFragmentManager().beginTransaction();
      Fragment prev = getFragmentManager().findFragmentByTag("dialog");
      if (prev != null) {
        ft.remove(prev);
      }
      ft.addToBackStack(null);

      // Create and show the dialog.
      AddItemDialogFragment newFragment = new AddItemDialogFragment();
      newFragment.setHandler(mHandler);
      newFragment.show(ft, "dialog");
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

    @Override
  public boolean onQueryTextChange(String newText) {
    if (TextUtils.isEmpty(newText)) {
      adapter.getFilter().filter("");
    }
    else {
      adapter.getFilter().filter(newText.toString());
    }

    return(true);
  }

  @Override
  public boolean onQueryTextSubmit(String query) {
    return(false);
  }

  @Override
  public boolean onClose() {
    adapter.getFilter().filter("");

    return(true);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    String pass = Generator.instance()
        .generate(adapter.getItem(position));
    Toast.makeText(getActivity(), pass,
                   Toast.LENGTH_LONG).show();
  }

  private void configureSearchView(Menu menu) {
    MenuItem search=menu.findItem(R.id.search);

    sv=(SearchView) MenuItemCompat.getActionView(search);
    sv.setOnQueryTextListener(this);
    sv.setOnCloseListener(this);
    sv.setSubmitButtonEnabled(false);
    sv.setIconifiedByDefault(true);

    if (initialQuery != null) {
      sv.setIconified(false);
      search.expandActionView();
      sv.setQuery(initialQuery, true);
    }
  }

  private void initAdapter() {
    MyArrayAdapter<SiteEntity> myArrayAdapter = new MyArrayAdapter<SiteEntity>(getActivity(),
        R.layout.list_item_view,
        R.id.text1,
        DBHelper.getInstance().queryAll());
    myArrayAdapter.setOnItemSubViewClickListener(this);
    adapter= myArrayAdapter;
    setListAdapter(adapter);
  }

  @Override
  public void onItemSubViewDelClicked(int position, @NonNull View view) {
    if (view.getId() == R.id.btn_del){
      SiteEntity siteEntity = adapter.getItem(position);
      DBHelper.getInstance().rmSiteEntity(siteEntity.name);
      adapter.remove(siteEntity);

      adapter.notifyDataSetChanged();
    }
  }

  @Override
  public void onItemSubViewEditClicked(int position, @NonNull View view) {
    if (view.getId() == R.id.btn_edit){
      FragmentTransaction ft = getFragmentManager().beginTransaction();
      Fragment prev = getFragmentManager().findFragmentByTag("dialog");
      if (prev != null) {
        ft.remove(prev);
      }
      ft.addToBackStack(null);

      // Create and show the dialog.
      EditItemDialogFragment newFragment = new EditItemDialogFragment();
      newFragment.setTarget(adapter.getItem(position));
      newFragment.setHandler(mHandler);

      newFragment.show(ft, "dialog");
    }
  }

}
