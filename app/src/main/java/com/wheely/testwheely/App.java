package com.wheely.testwheely;

import android.app.Application;
import android.content.Intent;

import com.orm.SugarContext;

/**
 * Created by isinotov on 17/03/2016.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        startService(new Intent(this, ServerService.class));
        SugarContext.init(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        stopService(new Intent(this, ServerService.class));
    }
}
