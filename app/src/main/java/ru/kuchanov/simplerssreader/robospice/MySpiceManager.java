package ru.kuchanov.simplerssreader.robospice;

import android.util.Log;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.SpiceService;

import roboguice.util.temp.Ln;

/**
 * Created by Юрий on 15.02.2016 16:30.
 * For SimpleRSSReader.
 */
public class MySpiceManager extends SpiceManager
{
    /**
     * Creates a {@link SpiceManager}. Typically this occurs in the construction
     * of an Activity or Fragment. This method will check if the service to bind
     * to has been properly declared in AndroidManifest.
     *
     * @param spiceServiceClass the service class to bind to.
     */
    public MySpiceManager(Class<? extends SpiceService> spiceServiceClass)
    {
        super(spiceServiceClass);
        Ln.getConfig().setLoggingLevel(Log.ERROR);
    }
}