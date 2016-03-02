package ru.kuchanov.simplerssreader;

import android.app.Application;
import android.util.Log;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;

import ru.kuchanov.simplerssreader.otto.SingltonOtto;
import ru.kuchanov.simplerssreader.robospice.SingltonRoboSpice;
import ru.kuchanov.simplerssreader.utils.SingltonUIL;

/**
 * Created by Юрий on 15.02.2016 16:40.
 * For SimpleRSSReader.
 */
public class MyApplication extends Application
{
    private static final String LOG = MyApplication.class.getSimpleName();

    VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker()
    {
        @Override
        public void onVKAccessTokenChanged(VKAccessToken oldToken, VKAccessToken newToken)
        {
            if (newToken == null)
            {
// VKAccessToken is invalid
            }
        }
    };

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d(LOG, "onCreate called");

        SingltonRoboSpice.initInstance();
        SingltonUIL.initInstance(this);
        SingltonOtto.initInstance();

        //VK SDK
//        vkAccessTokenTracker.startTracking();
//        VKSdk.initialize(getApplicationContext());
    }
}