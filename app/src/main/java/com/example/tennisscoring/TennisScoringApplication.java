package com.example.tennisscoring;

import android.app.Application;

public class TennisScoringApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ThemeUtils.applyTheme(this);
    }
}
