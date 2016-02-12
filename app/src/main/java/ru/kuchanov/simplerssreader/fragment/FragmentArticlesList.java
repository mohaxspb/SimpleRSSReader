package ru.kuchanov.simplerssreader.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import ru.kuchanov.simplerssreader.R;
import ru.kuchanov.simplerssreader.utils.customization.SpacesItemDecoration;

/**
 * Created by Юрий on 12.02.2016 18:07.
 * For SimpleRSSReader.
 */
public class FragmentArticlesList extends Fragment
{
    private static final String KEY_RSS_URL = "KEY_RSS_URL";
    private final static String LOG = FragmentArticlesList.class.getSimpleName();

    private Context ctx;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    public static Fragment newInstance(String url)
    {
        FragmentArticlesList fragmentArticlesList = new FragmentArticlesList();
        Bundle args = new Bundle();
        args.putString(KEY_RSS_URL, url);
        fragmentArticlesList.setArguments(args);
        return fragmentArticlesList;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_articles_list, container, false);

        swipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh);
        recyclerView = (RecyclerView) root.findViewById(R.id.recycler);

        recyclerView.addItemDecoration(new SpacesItemDecoration(0));

        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

        recyclerView.setItemAnimator(new SlideInUpAnimator(new OvershootInterpolator(1f)));
        recyclerView.getItemAnimator().setAddDuration(500);
        recyclerView.getItemAnimator().setRemoveDuration(500);
        recyclerView.getItemAnimator().setMoveDuration(500);
        recyclerView.getItemAnimator().setChangeDuration(500);

        return root;
    }

    @Override
    public void onAttach(Context context)
    {
//        Log.i(LOG, "onAttach called");
        super.onAttach(context);
        this.ctx = this.getActivity();
    }

}
