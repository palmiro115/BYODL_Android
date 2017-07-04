package com.byodl.activities.splash;

import android.os.Bundle;

import com.byodl.AppConstants;
import com.byodl.R;
import com.byodl.activities.base.BaseActivity;
import com.byodl.activities.home.HomeActivity;
import com.byodl.utils.Sync;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Sync(new Runnable() {
            @Override
            public void run() {
                toNextScreen();
            }

        }).next(AppConstants.Config.SPLASH_DURATION);
    }

    private void toNextScreen() {
        if (!isFinishing())
            toHomeScreen();
    }

    private void toHomeScreen() {
        finish();
        HomeActivity.show(this);
    }
}
