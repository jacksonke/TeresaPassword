package com.jacksonke.teresapassword;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;

import java.util.List;

public class PatternActivity extends AppCompatActivity {

    private static final String TAG = "PatternActivity";
    PatternLockView mPatternLockView;

    private PatternLockViewListener mPatternLockViewListener = new PatternLockViewListener() {
        @Override
        public void onStarted() {
            Log.d(TAG, "Pattern drawing started");
        }

        @Override
        public void onProgress(List<PatternLockView.Dot> progressPattern) {
            Log.d(TAG, "Pattern progress: " +
                    PatternLockUtils.patternToString(mPatternLockView, progressPattern));
        }

        @Override
        public void onComplete(List<PatternLockView.Dot> pattern) {
            Log.d(TAG, "Pattern complete: " +
                    PatternLockUtils.patternToString(mPatternLockView, pattern));

            Intent intent = new Intent(PatternActivity.this, MainActivity.class);
            String secret = PatternLockUtils.patternToString(mPatternLockView, pattern);
            intent.putExtra(Constant.KeySecret, secret);

            if (secret != null){
                Generator.instance().setSecret(secret);
            }

            startActivity(intent);
            finish();
        }

        @Override
        public void onCleared() {
            Log.d(TAG, "Pattern has been cleared");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern);

        mPatternLockView = (PatternLockView) findViewById(R.id.pattern_lock_view);
        mPatternLockView.addPatternLockListener(mPatternLockViewListener);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");

        mPatternLockView.removePatternLockListener(mPatternLockViewListener);

        super.onDestroy();
    }
}
