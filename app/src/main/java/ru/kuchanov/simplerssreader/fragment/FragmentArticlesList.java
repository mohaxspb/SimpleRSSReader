package ru.kuchanov.simplerssreader.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.PendingRequestListener;

import java.util.ArrayList;
import java.util.Collections;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import ru.kuchanov.simplerssreader.R;
import ru.kuchanov.simplerssreader.adapter.RecyclerAdapter;
import ru.kuchanov.simplerssreader.db.Article;
import ru.kuchanov.simplerssreader.db.ArticlesList;
import ru.kuchanov.simplerssreader.robospice.MySpiceManager;
import ru.kuchanov.simplerssreader.robospice.SingltonRoboSpice;
import ru.kuchanov.simplerssreader.robospice.request.RequestRssFeed;
import ru.kuchanov.simplerssreader.utils.customization.SpacesItemDecoration;

/**
 * Created by Юрий on 12.02.2016 18:07.
 * For SimpleRSSReader.
 */
public class FragmentArticlesList extends Fragment
{
    private static final String KEY_RSS_URL = "KEY_RSS_URL";
    //    private static final String KEY_CURRENT = "KEY_RSS_URL";
    private String LOG = FragmentArticlesList.class.getSimpleName();

    private Context ctx;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    private MySpiceManager spiceManager;
    private MySpiceManager spiceManagerOffline;

    private String url;

    private ArrayList<Article> articles = new ArrayList<>();

    public static Fragment newInstance(String url)
    {
        FragmentArticlesList fragmentArticlesList = new FragmentArticlesList();
        Bundle args = new Bundle();
        args.putString(KEY_RSS_URL, url);
        fragmentArticlesList.setArguments(args);
        return fragmentArticlesList;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(Article.LOG, articles);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        url = args.getString(KEY_RSS_URL);
        LOG += url;

        if (savedInstanceState != null)
        {
            this.articles = savedInstanceState.getParcelableArrayList(Article.LOG);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.d(LOG, "onCreateView called");
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

        if (articles.size() != 0)
        {
            recyclerView.setAdapter(new RecyclerAdapter(articles));
        }

        return root;
    }

    @Override
    public void onAttach(Context context)
    {
//        Log.i(LOG, "onAttach called");
        super.onAttach(context);
        this.ctx = this.getActivity();
    }

    @Override
    public void onStart()
    {
//        Log.i(LOG, "onStart called");
        super.onStart();

        spiceManager = SingltonRoboSpice.getInstance().getSpiceManager();
        spiceManager.addListenerIfPending(ArticlesList.class, LOG, new ArticlesListRequestListener());

        spiceManagerOffline = SingltonRoboSpice.getInstance().getSpiceManagerOffline();
        spiceManagerOffline.addListenerIfPending(ArticlesList.class, LOG, new ArticlesListRequestListener());
    }

    @Override
    public void onResume()
    {
//        Log.d(LOG, "onResume called");
        super.onResume();

        spiceManager.addListenerIfPending(ArticlesList.class, LOG, new ArticlesListRequestListener());
        spiceManagerOffline.addListenerIfPending(ArticlesList.class, LOG, new ArticlesListRequestListener());
        //make request for it
        if (articles.size() == 0)
        {
            performRequest(false);
        }
    }

    private void performRequest(boolean forceLoad)
    {
        //TODO
        if (!forceLoad)
        {
            RequestRssFeed requestRssFeed = new RequestRssFeed(ctx, url);
            spiceManager.execute(requestRssFeed, LOG, DurationInMillis.ALWAYS_EXPIRED, new ArticlesListRequestListener());
        }
        else
        {

        }
    }

    //TODO
    private class ArticlesListRequestListener implements PendingRequestListener<ArticlesList>
    {
        @Override
        public void onRequestNotFound()
        {

        }

        @Override
        public void onRequestFailure(SpiceException spiceException)
        {
            Log.d(LOG, "onRequestFailure called");
            Log.d(LOG, "spiceException: " + spiceException.toString());
        }

        @Override
        public void onRequestSuccess(ArticlesList articlesList)
        {
            Log.d(LOG, "onRequestSuccess called");
            if (!isAdded())
            {
                return;
            }
            if (articlesList != null)
            {
                ArrayList<Article> loadedArticles = new ArrayList<Article>(articlesList.getResult());
                Collections.sort(loadedArticles, new Article.PubDateComparator());
                articles.clear();
                articles.addAll(loadedArticles);

                if (recyclerView.getAdapter() == null)
                {
                    RecyclerAdapter adapter = new RecyclerAdapter(articles);
                    recyclerView.setAdapter(adapter);
                }
                else
                {
                    recyclerView.getAdapter().notifyItemRangeInserted(0, articles.size());
                }

                //test
//                for (Article a : articles)
//                {
//                    Log.d(LOG, a.getTitle());
//                }
            }
        }
    }
}
