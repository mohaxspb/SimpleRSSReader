package ru.kuchanov.simplerssreader.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.utils.L;

import ru.kuchanov.simplerssreader.R;
import ru.kuchanov.simplerssreader.robospice.SingltonRoboSpice;


/**
 * Created by Юрий on 17.02.2016 16:08.
 * For SimpleRSSReader.
 */
public class SingltonUIL
{
    private static final String LOG = SingltonUIL.class.getSimpleName();

    private static ImageLoader ourInstance;

    public static void initInstance(Context ctx)
    {
        Log.d(LOG, "SingltonUIL#initInstance()");
        if (ourInstance == null)
        {
            ourInstance = get(ctx);
        }
    }

    public static ImageLoader getInstance()
    {
        return ourInstance;
    }

    private static ImageLoader get(Context act)
    {
        int roundedCornersInPX = (int) DipToPx.convert(3, act);

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .displayer(new RoundedBitmapDisplayer(roundedCornersInPX))
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        //switch to true if you want logging
        L.writeLogs(false);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(act)
                .defaultDisplayImageOptions(options)
                .build();

        ImageLoader imageLoader = ImageLoader.getInstance();

        if (!imageLoader.isInited())
        {
            imageLoader.init(config);
        }

        return imageLoader;

    }

    public static DisplayImageOptions getSimple()
    {
        return new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }
}