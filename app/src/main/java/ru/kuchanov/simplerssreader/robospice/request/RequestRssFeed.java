package ru.kuchanov.simplerssreader.robospice.request;

import android.content.Context;
import android.util.Log;

import com.octo.android.robospice.request.SpiceRequest;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Calendar;

import ru.kuchanov.simplerssreader.db.Article;
import ru.kuchanov.simplerssreader.db.ArticlesList;
import ru.kuchanov.simplerssreader.db.MyRoboSpiceDatabaseHelper;
import ru.kuchanov.simplerssreader.utils.Const;
import ru.kuchanov.simplerssreader.utils.RssParser;

/**
 * Created by Юрий on 15.02.2016 17:18.
 * For SimpleRSSReader.
 */
public class RequestRssFeed extends SpiceRequest<ArticlesList>
{
    public static final String LOG = RequestRssFeed.class.getSimpleName();
    private Context ctx;
    private MyRoboSpiceDatabaseHelper databaseHelper;
    private String url;

    public RequestRssFeed(Context ctx, String rssUrl)
    {
        super(ArticlesList.class);

        this.ctx = ctx;
        this.url = rssUrl;
        databaseHelper = new MyRoboSpiceDatabaseHelper(ctx, MyRoboSpiceDatabaseHelper.DB_NAME, MyRoboSpiceDatabaseHelper.DB_VERSION);
    }

    @Override
    public ArticlesList loadDataFromNetwork() throws Exception
    {
//        Log.i(LOG, "loadDataFromNetwork called");
        ArticlesList articles = new ArticlesList();

        String responseBody = makeRequest();
        Document document = Jsoup.parse(responseBody);

        ArrayList<Article> articleArrayList = RssParser.parseRssFeed(document);

//        articles.setNumOfNewArts(newArtsQuont);
//        articles.setResult(list);
        //check if we receive less then Const.NUM_OF_ARTS_ON_PAGE, and if so make last artCat/artTag isBottom
        //and set value to Articles obj
        //TODO test
//        Log.d(LOG, "list.size(): " + list.size());
//
//        if (list.size() < Const.NUM_OF_ARTS_ON_PAGE)
//        {
//            articles.setContainsBottomArt(true);
//        }

        return articles;
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