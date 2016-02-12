package ru.kuchanov.simplerssreader.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;

import ru.kuchanov.simplerssreader.fragment.FragmentArticlesList;

/**
 * Created by Юрий on 12.02.2016 18:20.
 * For SimpleRSSReader.
 */
public class PagerAdapterMain extends FragmentStatePagerAdapter
{
    ArrayList<String> urls;

    public PagerAdapterMain(FragmentManager fm, ArrayList<String> urls)
    {
        super(fm);
        this.urls = urls;
    }

    @Override
    public Fragment getItem(int position)
    {
        return FragmentArticlesList.newInstance(urls.get(position));
    }

    @Override
    public int getCount()
    {
        return urls.size();
    }
}
