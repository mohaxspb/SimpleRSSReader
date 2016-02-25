package ru.kuchanov.simplerssreader.robospice;

import android.app.Application;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.ormlite.InDatabaseObjectPersisterFactory;

import java.util.ArrayList;
import java.util.List;

import ru.kuchanov.simplerssreader.R;
import ru.kuchanov.simplerssreader.db.Article;
import ru.kuchanov.simplerssreader.db.ArticlesList;
import ru.kuchanov.simplerssreader.db.MyRoboSpiceDatabaseHelper;

/**
 * Created by Юрий on 15.02.2016 16:25.
 * For SimpleRSSReader.
 */
public class HtmlSpiceService extends SpiceService
{
    @Override
    public CacheManager createCacheManager(Application application) throws CacheCreationException
    {
        CacheManager cacheManager = new CacheManager();

        List<Class<?>> classCollection = new ArrayList<>();

        // add persisted classes to class collection
        classCollection.add(Article.class);
        classCollection.add(ArticlesList.class);

        // init
        MyRoboSpiceDatabaseHelper databaseHelper = new MyRoboSpiceDatabaseHelper(application, MyRoboSpiceDatabaseHelper.DB_NAME, MyRoboSpiceDatabaseHelper.DB_VERSION);
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