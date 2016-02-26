package ru.kuchanov.simplerssreader.robospice;

import android.app.Application;
import android.content.Context;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.networkstate.NetworkStateChecker;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.ormlite.InDatabaseObjectPersisterFactory;

import java.util.ArrayList;
import java.util.List;

import ru.kuchanov.simplerssreader.R;
import ru.kuchanov.simplerssreader.db.Article;
import ru.kuchanov.simplerssreader.db.ArticlesList;
import ru.kuchanov.simplerssreader.db.MyRoboSpiceDataBaseHelper;

/**
 * Created by Юрий on 15.02.2016 16:28.
 * For SimpleRSSReader.
 */
public class HtmlSpiceServiceOffline extends SpiceService
{
    @Override
    protected NetworkStateChecker getNetworkStateChecker()
    {
        return new NetworkStateChecker()
        {
            @Override
            public boolean isNetworkAvailable(Context context)
            {
                return true;
            }

            @Override
            public void checkPermissions(Context context)
            {
                //do noting
            }
        };
    }

    @Override
    public CacheManager createCacheManager(Application application) throws CacheCreationException
    {
        CacheManager cacheManager = new CacheManager();

        List<Class<?>> classCollection = new ArrayList<>();

        // add persisted classes to class collection
        classCollection.add(Article.class);
        classCollection.add(ArticlesList.class);

        // init
        MyRoboSpiceDataBaseHelper databaseHelper = new MyRoboSpiceDataBaseHelper(application, MyRoboSpiceDataBaseHelper.DB_NAME, MyRoboSpiceDataBaseHelper.DB_VERSION);
        InDatabaseObjectPersisterFactory inDatabaseObjectPersisterFactory = new InDatabaseObjectPersisterFactory(application, databaseHelper, classCollection);
        cacheManager.addPersister(inDatabaseObjectPersisterFactory);

        return cacheManager;
    }

    @Override
    public int getThreadCount()
    {
        return this.getResources().getInteger(R.integer.roboSpiceThreadCount);
    }
}