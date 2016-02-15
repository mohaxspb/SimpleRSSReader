package ru.kuchanov.simplerssreader.robospice;

import android.app.Application;
import android.util.Log;

/**
 * Created by Юрий on 15.02.2016 16:34.
 * For SimpleRSSReader.
 */
public class SingltonRoboSpice extends Application
{
    private static final String LOG = SingltonRoboSpice.class.getSimpleName();

    private static SingltonRoboSpice ourInstance = new SingltonRoboSpice();

    private MySpiceManager spiceManager = new MySpiceManager(HtmlSpiceService.class);
    private MySpiceManager spiceManagerOffline = new MySpiceManager(HtmlSpiceServiceOffline.class);

    public static void initInstance()
    {
        Log.d(LOG, "SingltonRoboSpice#initInstance()");
        if (ourInstance == null)
        {
            ourInstance = new SingltonRoboSpice();
        }
    }

    public static SingltonRoboSpice getInstance()
    {
        return ourInstance;
    }

    public MySpiceManager getSpiceManager()
    {
        return spiceManager;
    }

    public MySpiceManager getSpiceManagerOffline()
    {
        return spiceManagerOffline;
    }
}