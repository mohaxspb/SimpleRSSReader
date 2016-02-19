package ru.kuchanov.simplerssreader.robospice.request;

import android.content.Context;
import android.util.Log;

import com.octo.android.robospice.request.SpiceRequest;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import java.util.ArrayList;
import java.util.Date;

import ru.kuchanov.simplerssreader.db.Article;
import ru.kuchanov.simplerssreader.db.ArticleRssChanel;
import ru.kuchanov.simplerssreader.db.ArticlesList;
import ru.kuchanov.simplerssreader.db.MyRoboSpiceDatabaseHelper;
import ru.kuchanov.simplerssreader.db.RssChanel;
import ru.kuchanov.simplerssreader.utils.RssParser;

/**
 * Created by Юрий on 15.02.2016 17:18.
 * For SimpleRSSReader.
 */
public class RequestRssFeed extends SpiceRequest<ArticlesList>
{
    private String LOG = RequestRssFeed.class.getSimpleName();
    private MyRoboSpiceDatabaseHelper databaseHelper;
    private String url;

    public RequestRssFeed(Context ctx, String rssUrl)
    {
        super(ArticlesList.class);

        this.url = rssUrl;
        this.LOG += "#" + url;
        databaseHelper = new MyRoboSpiceDatabaseHelper(ctx, MyRoboSpiceDatabaseHelper.DB_NAME, MyRoboSpiceDatabaseHelper.DB_VERSION);
    }

    @Override
    public ArticlesList loadDataFromNetwork() throws Exception
    {
        Log.i(LOG, "loadDataFromNetwork called");

        RssChanel rssChanel = RssChanel.getRssChanelByUrl(url, databaseHelper);

        String responseBody = makeRequest();
        Document document = Jsoup.parse(responseBody, "", Parser.xmlParser());

        ArrayList<Article> articleArrayList;
        try
        {
            articleArrayList = RssParser.parseRssFeed(document, databaseHelper);

            //write to Article table
            articleArrayList = Article.writeArtsToDB(articleArrayList, databaseHelper);

            //write to articleRss table
            int numOfNewArts = ArticleRssChanel.writeToArtRssFeedTable(articleArrayList, rssChanel, databaseHelper);

            ArticlesList articles = new ArticlesList();
            articles.setResult(articleArrayList);
            articles.setNumOfNewArts(numOfNewArts);

            //update refreshed date of RssChanel
            rssChanel.setRefreshed(new Date(System.currentTimeMillis()));
            databaseHelper.getDaoCategory().update(rssChanel);

            return articles;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private String makeRequest() throws Exception
    {
        OkHttpClient client = new OkHttpClient();

        Request.Builder request = new Request.Builder();
        request.url(this.url);

        Response response = client.newCall(request.build()).execute();

        return response.body().string();
    }
}