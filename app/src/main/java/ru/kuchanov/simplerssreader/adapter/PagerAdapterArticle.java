package ru.kuchanov.simplerssreader.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import java.util.ArrayList;

import ru.kuchanov.simplerssreader.db.Article;
import ru.kuchanov.simplerssreader.db.RssChanel;
import ru.kuchanov.simplerssreader.fragment.FragmentArticle;
import ru.kuchanov.simplerssreader.fragment.FragmentArticlesList;

/**
 * Created by Юрий on 12.02.2016 18:20.
 * For SimpleRSSReader.
 */
public class PagerAdapterArticle extends FragmentStatePagerAdapter
{
    private static final String LOG = PagerAdapterArticle.class.getSimpleName();
    ArrayList<Article> articles;

    public PagerAdapterArticle(FragmentManager fm, ArrayList<Article> articles)
    {
        super(fm);
//        Log.d(LOG, "constructor called");
        this.articles = articles;
//        Log.d(LOG, "articles.size(): " + articles.size());
    }

    @Override
    public Fragment getItem(int position)
    {
        return FragmentArticle.newInstance(articles.get(position));
    }

    @Override
    public int getCount()
    {
        return articles.size();
    }

    /**
     * need it to prevent not destroying fragment after reducing num of rssChanels;
     */
    @Override
    public int getItemPosition(Object object)
    {
        return POSITION_NONE;
    }
}