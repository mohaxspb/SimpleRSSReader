package ru.kuchanov.simplerssreader;

import android.app.Application;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;

import ru.kuchanov.simplerssreader.robospice.SingltonRoboSpice;

/**
 * Created by Юрий on 15.02.2016 16:40.
 * For SimpleRSSReader.
 */
public class MyApplication extends Application
{
    static final String LOG = MyApplication.class.getSimpleName();

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d(LOG, "onCreate called");

        SingltonRoboSpice.initInstance();
    }
}