package com.jacksonke.teresapassword;

import android.content.Intent;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {



    private static final String TAG = "MainActivity";

    private int mCurrentPage = -1;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        if (intent != null){
//            mSecretKey = intent.getStringExtra(TeresaConstant.KeySecret);
        }

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        gotoPasswordPage();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }


    @OnClick(R.id.tab_item_one)
    public void gotoPasswordPage(){

        if (mCurrentPage == 0){
            Toast.makeText(this, "aaa", Toast.LENGTH_SHORT).show();
            return;
        }

        gotoPage(getTabid(R.id.tab_item_one));
    }

    @OnClick(R.id.tab_item_two)
    public void gotoSettingPage(){
        gotoPage(getTabid(R.id.tab_item_two));
    }


    private int getTabid(int viewId){
        if (viewId == R.id.tab_item_one){
            return 0;
        }

        if (viewId == R.id.tab_item_two){
            return 1;
        }

        return -1;
    }

    private Fragment createFragmentByTabid(int tabid){
        if (tabid == 0){
            return new ActionBarFragment();
//            return new FragmentPasswordFactory();
        }

        if (tabid == 1){
            return new FragmentSettings();
        }

        return null;
    }

    private void gotoPage(int tabid){
        FragmentManager fmgr = getSupportFragmentManager();

        Fragment fragment = createFragmentByTabid(tabid);
        if (fragment != null){
            if (fmgr.findFragmentById(R.id.page_container) == null){
                fmgr.beginTransaction().add(R.id.page_container, fragment).commit();
            } else {
                fmgr.beginTransaction().replace(R.id.page_container, fragment).commit();
            }

            mCurrentPage = tabid;
        }
    }
}
