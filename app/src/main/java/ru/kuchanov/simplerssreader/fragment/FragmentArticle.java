package ru.kuchanov.simplerssreader.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.kuchanov.simplerssreader.R;
import ru.kuchanov.simplerssreader.db.Article;
import ru.kuchanov.simplerssreader.utils.AttributeGetter;
import ru.kuchanov.simplerssreader.utils.MyTextUtils;

/**
 * Created by Юрий on 12.02.2016 18:07.
 * For SimpleRSSReader.
 */
public class FragmentArticle extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener
{
    //    private static final String KEY_URL = "KEY_URL";
    private static final String KEY_IS_LOADING = "KEY_IS_LOADING";
    TextView articleTextView;
    private String LOG = FragmentArticle.class.getSimpleName() + "#" + "NOT_SET_YET";
    private Context ctx;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NestedScrollView nestedScrollView;

//    private MySpiceManager spiceManager;
//    private MySpiceManager spiceManagerOffline;
    private LinearLayout mainLinear;
    //    private String url;
    private Article article;

    private boolean isLoading = false;
    private int textSizeLarge;
    private int textSizePrimary;
    private int textSizeSecondary;
//    private Timer timer;
//    private TimerTask timerTask;

    public static Fragment newInstance(Article article)
    {
        FragmentArticle fragmentArticlesList = new FragmentArticle();
        Bundle args = new Bundle();
        args.putParcelable(Article.LOG, article);
        fragmentArticlesList.setArguments(args);
        return fragmentArticlesList;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putParcelable(Article.LOG, article);
        outState.putBoolean(KEY_IS_LOADING, isLoading);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
//        Log.d(LOG, "onCreate called");
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        article = args.getParcelable(Article.LOG);
        LOG = getClass().getSimpleName() + "#" + article.getUrl();

        if (savedInstanceState != null)
        {
            article = savedInstanceState.getParcelable(Article.LOG);
            isLoading = savedInstanceState.getBoolean(KEY_IS_LOADING);
        }

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        pref.registerOnSharedPreferenceChangeListener(this);

        textSizeLarge = ctx.getResources().getDimensionPixelSize(R.dimen.text_size_large);
        textSizePrimary = ctx.getResources().getDimensionPixelSize(R.dimen.text_size_primary);
        textSizeSecondary = ctx.getResources().getDimensionPixelSize(R.dimen.text_size_secondary);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.d(LOG, "onCreateView called");
        View root = inflater.inflate(R.layout.fragment_article_scroll_view, container, false);

        swipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                performRequest();
            }
        });

        nestedScrollView = (NestedScrollView) root.findViewById(R.id.nested_scroll_view);

        mainLinear = (LinearLayout) root.findViewById(R.id.main_container);

        if (article.getText() != null)
        {
            //TODO show text
//            recyclerView.setAdapter(new RecyclerAdapter(articles));

            setContent(root, inflater);
        }

        setLoading(isLoading);

        return root;
    }

    private void setContent(View root, LayoutInflater layoutInflater)
    {
        if (article.getVideoUrl() != null)
        {
            //TODO show video

        }

        articleTextView = (TextView) layoutInflater.inflate(R.layout.article_text_view, mainLinear, false);
        mainLinear.addView(articleTextView);

        MyTextUtils.setTextToTextView(articleTextView, Html.fromHtml(article.getText()).toString());
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
    }

    @Override
    public void onPause()
    {
//        Log.d(LOG, "onPause called");
        super.onPause();
    }

    @Override
    public void onResume()
    {
//        Log.d(LOG, "onResume called");
        super.onResume();
        //make request for it
        if (article.getText() == null)
        {
            performRequest();
        }
    }

//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser)
//    {
//        Log.d(LOG, "setUserVisibleHint isVisibleToUser: " + isVisibleToUser);
//        super.setUserVisibleHint(isVisibleToUser);
//    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (!isAdded())
        {
            return;
        }
        if (key.equals(getString(R.string.pref_design_key_text_size_article)))
        {
            float uiTextScale = sharedPreferences.getFloat(ctx.getString(R.string.pref_design_key_text_size_article), 0.75f);
            articleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, uiTextScale * textSizeLarge);
        }
    }

    private void performRequest()
    {
        setLoading(true);
        //TODO start loading
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
}