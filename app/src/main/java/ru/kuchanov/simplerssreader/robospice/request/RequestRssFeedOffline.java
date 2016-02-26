package ru.kuchanov.simplerssreader.robospice.request;

import android.content.Context;
import android.util.Log;

import com.octo.android.robospice.request.SpiceRequest;

import java.util.ArrayList;

import ru.kuchanov.simplerssreader.db.Article;
import ru.kuchanov.simplerssreader.db.ArticleRssChanel;
import ru.kuchanov.simplerssreader.db.ArticlesList;
import ru.kuchanov.simplerssreader.db.MyRoboSpiceDataBaseHelper;
import ru.kuchanov.simplerssreader.db.RssChanel;

/**
 * Created by Юрий on 15.02.2016 17:18.
 * For SimpleRSSReader.
 */
public class RequestRssFeedOffline extends SpiceRequest<ArticlesList>
{
    private String LOG = RequestRssFeedOffline.class.getSimpleName();
    private MyRoboSpiceDataBaseHelper databaseHelper;
    private String url;

    public RequestRssFeedOffline(Context ctx, String rssUrl)
    {
        super(ArticlesList.class);

        this.url = rssUrl;
        this.LOG += "#" + url;
        databaseHelper = new MyRoboSpiceDataBaseHelper(ctx, MyRoboSpiceDataBaseHelper.DB_NAME, MyRoboSpiceDataBaseHelper.DB_VERSION);
    }

    @Override
    public ArticlesList loadDataFromNetwork() throws Exception
    {
        Log.i(LOG, "loadDataFromNetwork called");

        RssChanel rssChanel = RssChanel.getRssChanelByUrl(url, databaseHelper);

        ArrayList<Article> articleArrayList;
        try
        {
            ArrayList<ArticleRssChanel> articleRssChanels = ArticleRssChanel.getArtRssByRssChanel(rssChanel, databaseHelper);

            articleArrayList = ArticleRssChanel.getArticlesFromArtRss(articleRssChanels, databaseHelper);

            ArticlesList articles = new ArticlesList();
            articles.setResult(articleArrayList);
            //set -1 to prevent showing any SnackBar/Toast
            articles.setNumOfNewArts(-1);

            return articles;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}