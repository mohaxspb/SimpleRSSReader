package ru.kuchanov.simplerssreader.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.octo.android.robospice.persistence.ormlite.RoboSpiceDatabaseHelper;

import java.sql.SQLException;
import java.util.ArrayList;

import ru.kuchanov.simplerssreader.R;

/**
 * Created by Юрий on 13.02.2016 21:56.
 * For SimpleRSSReader.
 */
public class MyRoboSpiceDatabaseHelper1 extends RoboSpiceDatabaseHelper
{
    public static final String LOG = MyRoboSpiceDatabaseHelper1.class.getSimpleName();

    public final static String DB_NAME = "simple_rss_reader_db";
    public final static int DB_VERSION = 1;

    Context context;

    public MyRoboSpiceDatabaseHelper1(Context context, String databaseName, int databaseVersion)
    {
        super(context, databaseName, databaseVersion);
        this.context = context;
    }

    public MyRoboSpiceDatabaseHelper1(Context ctx)
    {
        super(ctx, DB_NAME, DB_VERSION);
        this.context = ctx;
    }


    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion)
    {
        // override if needed
        try
        {
            TableUtils.dropTable(connectionSource, RssChanel.class, true);
            TableUtils.dropTable(connectionSource, Article.class, true);
            TableUtils.dropTable(connectionSource, ArticleRssChanel.class, true);
            TableUtils.dropTable(connectionSource, ArticlesList.class, true);

            this.onCreate(database, connectionSource);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource)
    {
        try
        {
            Log.i(LOG, "onCreate");
            TableUtils.createTableIfNotExists(connectionSource, RssChanel.class);
            TableUtils.createTableIfNotExists(connectionSource, Article.class);
            TableUtils.createTableIfNotExists(connectionSource, ArticleRssChanel.class);
            TableUtils.createTableIfNotExists(connectionSource, ArticlesList.class);
            Log.i(LOG, "all tables have been created");

            //fill with initial data
            fillTables();
        }
        catch (SQLException e)
        {
            Log.e(LOG, "Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    private void fillTables()
    {
        //write initial info for tags and cats
        String[] catsTitle = context.getResources().getStringArray(R.array.rss_default_titles);
        String[] catsUrl = context.getResources().getStringArray(R.array.rss_default_urls);

        ArrayList<RssChanel> cats = new ArrayList<>();
        for (int i = 0; i < catsTitle.length; i++)
        {
            String title = catsTitle[i];
            String url = catsUrl[i];
            RssChanel c = new RssChanel();
            c.setTitle(title);
            c.setUrl(url);
            cats.add(c);
        }

        try
        {
            for (RssChanel c : cats)
            {
                getDaoRssChanel().create(c);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void recreateDB()
    {
        Log.i(LOG, "recreateDB called");
        try
        {
            TableUtils.dropTable(connectionSource, Article.class, true);
            TableUtils.dropTable(connectionSource, ArticlesList.class, true);

            TableUtils.dropTable(connectionSource, ArticleRssChanel.class, true);

            TableUtils.dropTable(connectionSource, RssChanel.class, true);

            this.onCreate(this.getWritableDatabase(), connectionSource);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public Dao<ArticleRssChanel, Integer> getDaoArtRssChanel()
    {
        Dao<ArticleRssChanel, Integer> daoArtCat = null;
        try
        {
            daoArtCat = this.getDao(ArticleRssChanel.class);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return daoArtCat;
    }

    public Dao<Article, Integer> getDaoArticle()
    {
        Dao<Article, Integer> daoArticle = null;
        try
        {
            daoArticle = this.getDao(Article.class);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return daoArticle;
    }

    public Dao<RssChanel, Integer> getDaoRssChanel()
    {
        Dao<RssChanel, Integer> daoCategory = null;
        try
        {
            daoCategory = this.getDao(RssChanel.class);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return daoCategory;
    }
}