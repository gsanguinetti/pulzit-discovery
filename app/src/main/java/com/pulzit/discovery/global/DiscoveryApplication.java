package com.pulzit.discovery.global;

import android.app.Application;

import com.pulzit.discovery.services.PulzitService;

/**
 * Created by gastonsanguinetti on 03/05/16.
 */
public class DiscoveryApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PulzitService.init(this);
    }

    @Override
    public void onTerminate() {
        PulzitService.getInstance().destroy();
        super.onTerminate();
    }
}
