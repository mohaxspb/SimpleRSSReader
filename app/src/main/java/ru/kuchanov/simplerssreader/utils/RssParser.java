package ru.kuchanov.simplerssreader.utils;


import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import ru.kuchanov.simplerssreader.db.Article;

/**
 * Created by Юрий on 15.02.2016 16:23.
 * For SimpleRSSReader.
 */
public class RssParser
{
    private static final String TAG_ITEM = "item";
    private static final String TAG_TITLE = "title";
    private static final String TAG_LINK = "link";
    private static final String TAG_ENCLOSURE = "enclosure";
    private static final String TAG_DESCRIPTION = "description";
    private static final String TAG_PUB_DATE = "pubDate";
    //TODO test ":"
    private static final String TAG_FULL_TEXT = "yandex:full-text";
    private static final String TAG_CONTENT_ENCODED = "content:encoded";
    private static final String TAG_CREATOR = "dc:creator";

    private static final String TAG_CATEGORY = "category";


    public static ArrayList<Article> parseRssFeed(Document document) throws Exception
    {
        ArrayList<Article> articleArrayList = new ArrayList<>();

        Elements items = document.getElementsByTag(TAG_ITEM);
        for (Element item : items)
        {
            String title = item.getElementsByTag(TAG_TITLE).first().text();
            String link = item.getElementsByTag(TAG_LINK).first().text();
            String preview = item.getElementsByTag(TAG_DESCRIPTION).first().text();
            String pubDateString = item.getElementsByTag(TAG_PUB_DATE).first().text();
            Date pubDate = new Date(0);
            try
            {
                //I.e. // Sun, 14 Feb 2016 09:55:00 +0300
                DateFormat df = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
                pubDate = df.parse(pubDateString);
//                Log.i(LOG, sdf.format(pubDate));//prints date in the format sdf
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
            String imagesUrls = "";
            Elements enclosures = item.getElementsByTag(TAG_ENCLOSURE);
            for (int i = 0; i < enclosures.size(); i++)
            {
                Element enclosure = enclosures.get(i);
                imagesUrls += enclosure.attr("url");
                if (i < enclosures.size() - 1)
                {
                    imagesUrls += Const.DIVIDER;
                }
            }
            Element authorTag = item.getElementsByTag(TAG_CREATOR).first();
            String author = (authorTag != null) ? authorTag.text() : null;
            String categories = "";
            Elements cats = item.getElementsByTag(TAG_CATEGORY);
            for (int i = 0; i < cats.size(); i++)
            {
                Element cat = cats.get(i);
                categories += cat.text();
                if (i < cats.size() - 1)
                {
                    categories += Const.DIVIDER;
                }
            }
            //full text if is
            String articleText = null;
            Element fullTextEl = item.getElementsByTag(TAG_FULL_TEXT).first();
            if (fullTextEl != null)
            {
                articleText = fullTextEl.text();
            }
            Element content = item.getElementsByTag(TAG_CONTENT_ENCODED).first();
            if (content != null)
            {
                articleText = content.text();
            }
            Article article = new Article();
            article.setTitle(title);
            article.setUrl(link);
            article.setPreview(preview);
            article.setPubDate(pubDate);
            article.setImageUrls(imagesUrls);
            if (articleText != null)
            {
                article.setText(articleText);
            }
            //TODO add categories field to article class
            articleArrayList.add(article);
        }
        return articleArrayList;
    }
}
