package ru.kuchanov.simplerssreader.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

import ru.kuchanov.simplerssreader.db.RssChanel;
import ru.kuchanov.simplerssreader.fragment.FragmentArticlesList;

/**
 * Created by Юрий on 12.02.2016 18:20.
 * For SimpleRSSReader.
 */
public class PagerAdapterMain extends FragmentStatePagerAdapter
{
    ArrayList<RssChanel> urls;

    public PagerAdapterMain(FragmentManager fm, ArrayList<RssChanel> urls)
    {
        super(fm);
        this.urls = urls;
    }

    @Override
    public Fragment getItem(int position)
    {
        return FragmentArticlesList.newInstance(urls.get(position).getUrl());
    }

    @Override
    public int getCount()
    {
        return urls.size();
    }

    /**
     * need it to prevent not destroing fragment after redusing num of rssChanels;
     */
    @Override
    public int getItemPosition(Object object)
    {
        return POSITION_NONE;
    }
}