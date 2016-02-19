package ru.kuchanov.simplerssreader.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

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
import ru.kuchanov.simplerssreader.robospice.request.RequestRssFeedOffline;
import ru.kuchanov.simplerssreader.utils.AttributeGetter;
import ru.kuchanov.simplerssreader.utils.customization.SpacesItemDecoration;

/**
 * Created by Юрий on 12.02.2016 18:07.
 * For SimpleRSSReader.
 */
public class FragmentArticlesList extends Fragment
{
    private static final String KEY_RSS_URL = "KEY_RSS_URL";
    private static final String KEY_IS_LOADING = "KEY_IS_LOADING";
    private String LOG = FragmentArticlesList.class.getSimpleName();

    private Context ctx;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    private MySpiceManager spiceManager;
    private MySpiceManager spiceManagerOffline;

    private String url;

    private ArrayList<Article> articles = new ArrayList<>();
    private ArticlesListRequestListener requestListener = new ArticlesListRequestListener();
    private boolean isLoading = false;

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
        outState.putBoolean(KEY_IS_LOADING, isLoading);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        url = args.getString(KEY_RSS_URL);
        LOG += "#" + url;

        if (savedInstanceState != null)
        {
            articles = savedInstanceState.getParcelableArrayList(Article.LOG);
            isLoading = savedInstanceState.getBoolean(KEY_IS_LOADING);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.d(LOG, "onCreateView called");
        View root = inflater.inflate(R.layout.fragment_articles_list, container, false);

        swipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                performRequest(true);
            }
        });

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

        setLoading(isLoading);

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
//        spiceManager.addListenerIfPending(ArticlesList.class, LOG, new ArticlesListRequestListener());

        spiceManagerOffline = SingltonRoboSpice.getInstance().getSpiceManagerOffline();
//        spiceManagerOffline.addListenerIfPending(ArticlesList.class, LOG, new ArticlesListRequestListener());
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onResume()
    {
//        Log.d(LOG, "onResume called");
        super.onResume();

        spiceManager.addListenerIfPending(ArticlesList.class, LOG, requestListener);
        spiceManagerOffline.addListenerIfPending(ArticlesList.class, LOG, requestListener);
        //make request for it
        if (articles.size() == 0)
        {
            performRequest(false);
        }
    }

    private void performRequest(boolean forceLoad)
    {
        setLoading(true);
        if (forceLoad)
        {
            RequestRssFeed requestRssFeed = new RequestRssFeed(ctx, url);
            spiceManager.execute(requestRssFeed, LOG, DurationInMillis.ALWAYS_EXPIRED, requestListener);
        }
        else
        {
            RequestRssFeedOffline requestRssFeedOffline = new RequestRssFeedOffline(ctx, url);
            spiceManagerOffline.execute(requestRssFeedOffline, LOG, DurationInMillis.ALWAYS_EXPIRED, requestListener);
        }
    }

    private void setLoading(final boolean isLoading)
    {
        this.isLoading = isLoading;

        if (this.isLoading && swipeRefreshLayout.isRefreshing())
        {
//            Log.i(LOG, "isLoading and  swipeRefreshLayout.isRefreshing() are both TRUE, so RETURN!!!");
            return;
        }

        int actionBarSize = AttributeGetter.getDimentionPixelSize(ctx, android.R.attr.actionBarSize);
        swipeRefreshLayout.setProgressViewEndTarget(false, actionBarSize);

        //workaround from http://stackoverflow.com/a/26910973/3212712
        swipeRefreshLayout.post(new Runnable()
        {
            @Override
            public void run()
            {
                swipeRefreshLayout.setRefreshing(isLoading);
            }
        });
    }

    private class ArticlesListRequestListener implements PendingRequestListener<ArticlesList>
    {
        @Override
        public void onRequestNotFound()
        {
//            Log.d(LOG, "onRequestNotFound called");
        }

        @Override
        public void onRequestFailure(SpiceException spiceException)
        {
            Log.d(LOG, "onRequestFailure called");
            Log.d(LOG, "spiceException: " + spiceException.toString());

            setLoading(false);

            Snackbar snackbar;
            View snackBarView;
            int colorId = AttributeGetter.getColor(ctx, R.attr.colorPrimaryDark);
            snackbar = Snackbar.make(recyclerView, "Ошибка соединения", Snackbar.LENGTH_SHORT);
            snackBarView = snackbar.getView();
            snackBarView.setBackgroundColor(colorId);
            if (getUserVisibleHint())
            {
                snackbar.show();
            }

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
                ArrayList<Article> loadedArticles = new ArrayList<>(articlesList.getResult());
                Collections.sort(loadedArticles, new Article.PubDateComparator());


                Log.d(LOG, "articles.size() " + articles.size());

                if (recyclerView.getAdapter() == null)
                {
                    RecyclerAdapter adapter = new RecyclerAdapter(articles);
                    recyclerView.setAdapter(adapter);

                    articles.clear();
                    articles.addAll(loadedArticles);

                    recyclerView.getAdapter().notifyItemRangeInserted(0, articles.size());
                }
                else
                {
                    recyclerView.getAdapter().notifyItemRangeRemoved(0, recyclerView.getAdapter().getItemCount());

                    articles.clear();
                    articles.addAll(loadedArticles);

                    recyclerView.getAdapter().notifyItemRangeInserted(0, articles.size());
                }

                Log.d(LOG, "new arts count is: " + articlesList.getNumOfNewArts());
                Snackbar snackbar;
                View snackBarView;
                int colorId = AttributeGetter.getColor(ctx, R.attr.colorPrimaryDark);
                switch (articlesList.getNumOfNewArts())
                {
                    case -1:
                        //first loading, do nothing
                        break;
                    case 0:
                        snackbar = Snackbar.make(recyclerView, "Новых статей не обнаружено!", Snackbar.LENGTH_SHORT);
                        snackBarView = snackbar.getView();
                        snackBarView.setBackgroundColor(colorId);
                        if (getUserVisibleHint())
                        {
                            snackbar.show();
                        }
                        break;
                    default:
                        snackbar = Snackbar.make(recyclerView, "Обнаружено " + articlesList.getNumOfNewArts() + " новых статей!", Snackbar.LENGTH_SHORT);
                        snackBarView = snackbar.getView();
                        snackBarView.setBackgroundColor(colorId);
                        if (getUserVisibleHint())
                        {
                            snackbar.show();
                        }
                        break;
                }
                setLoading(false);
            }
            else
            {
                //no error, but articlesList is null,
                //so there is no data in cache
                //so start loading from web
                Log.d(LOG, "No data in cache");
                setLoading(true);
                performRequest(true);
            }
        }
    }
}
