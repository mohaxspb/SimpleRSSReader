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
import ru.kuchanov.simplerssreader.db.MyRoboSpiceDataBaseHelper;
import ru.kuchanov.simplerssreader.db.RssChanel;
import ru.kuchanov.simplerssreader.utils.RssParser;

/**
 * Created by Юрий on 15.02.2016 17:18.
 * For SimpleRSSReader.
 */
public class RequestRssFeed extends SpiceRequest<ArticlesList>
{
    private String LOG = RequestRssFeed.class.getSimpleName();
    private MyRoboSpiceDataBaseHelper databaseHelper;
    private String url;
    private boolean isTestRequest = false;

    public RequestRssFeed(Context ctx, String rssUrl)
    {
        super(ArticlesList.class);

        this.url = rssUrl;
        this.LOG += "#" + url;
        databaseHelper = new MyRoboSpiceDataBaseHelper(ctx, MyRoboSpiceDataBaseHelper.DB_NAME, MyRoboSpiceDataBaseHelper.DB_VERSION);
    }

    public void setTestRssChanel()
    {
        this.isTestRequest = true;
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
            ArticlesList articles = new ArticlesList();

            articleArrayList = RssParser.parseRssFeed(document, databaseHelper);

            if (articleArrayList.size() == 0)
            {
                //so there is no arts in html...
                //May be wrong url?
                //so return null as artsList
                articles.setResult(null);
                return articles;
            }
            else
            {
                //check if there is RssChanel with given url in DB
                //by checking rssChanel for null
                //if it's null and we have some arts
                //we must create new rss chanel in DB
                if (rssChanel == null && !isTestRequest)
                {
                    rssChanel = new RssChanel();
                    rssChanel.setUrl(url);
                    String title = document.title();
                    rssChanel.setTitle(title);
                    databaseHelper.getDaoRssChanel().create(rssChanel);
                }
            }

            //write arts to DB if it's not test request
            if (!isTestRequest)
            {
                //write to Article table
                articleArrayList = Article.writeArtsToDB(articleArrayList, databaseHelper);

                //write to articleRss table
                int numOfNewArts = ArticleRssChanel.writeToArtRssFeedTable(articleArrayList, rssChanel, databaseHelper);

                articles.setResult(articleArrayList);
                articles.setNumOfNewArts(numOfNewArts);

                //update refreshed date of RssChanel
                rssChanel.setRefreshed(new Date(System.currentTimeMillis()));
                databaseHelper.getDaoRssChanel().update(rssChanel);

                return articles;
            }

            articles.setResult(articleArrayList);

            //add rssTitle to ArticlesList obj
            //we can need it while adding new rss-feed
            articles.setRssChanelTitle(document.title());

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