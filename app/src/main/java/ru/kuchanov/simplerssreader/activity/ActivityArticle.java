package ru.kuchanov.simplerssreader.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.squareup.otto.Subscribe;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;

import ru.kuchanov.simplerssreader.R;
import ru.kuchanov.simplerssreader.adapter.PagerAdapterArticle;
import ru.kuchanov.simplerssreader.db.Article;
import ru.kuchanov.simplerssreader.db.MyRoboSpiceDataBaseHelper;
import ru.kuchanov.simplerssreader.db.RssChanel;
import ru.kuchanov.simplerssreader.fragment.FragmentArticlesList;
import ru.kuchanov.simplerssreader.fragment.FragmentDialogAddRss;
import ru.kuchanov.simplerssreader.fragment.FragmentDialogRemoveRss;
import ru.kuchanov.simplerssreader.fragment.FragmentDialogTextAppearance;
import ru.kuchanov.simplerssreader.otto.EventArtsReceived;
import ru.kuchanov.simplerssreader.otto.EventShowImage;
import ru.kuchanov.simplerssreader.otto.SingltonOtto;
import ru.kuchanov.simplerssreader.robospice.MySpiceManager;
import ru.kuchanov.simplerssreader.robospice.SingltonRoboSpice;
import ru.kuchanov.simplerssreader.utils.VKUtils;
import ru.kuchanov.simplerssreader.utils.ads.MD5;
import ru.kuchanov.simplerssreader.utils.anim.MyAnimationUtils;

public class ActivityArticle extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private final static String LOG = ActivityArticle.class.getSimpleName();
    private final static String KEY_CUR_NAV_ITEM_SELECTED_ID = "KEY_CUR_NAV_ITEM_SELECTED_ID";

    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Context ctx;
    private SharedPreferences pref;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView navigationView;
    private ViewPager pager;
    private ViewPager.OnPageChangeListener onPageChangeListener;
    private int currentSelectedNavItemsId = 0;
    private View cover;
    private ImageView toolbarImage;

    private ArrayList<Article> allArticles = new ArrayList<>();
    private String rssUrl;

    private MySpiceManager spiceManager = SingltonRoboSpice.getInstance().getSpiceManager();
    private MySpiceManager spiceManagerOffline = SingltonRoboSpice.getInstance().getSpiceManagerOffline();
    private ArrayList<RssChanel> rssChanels = new ArrayList<>();


    @Subscribe
    public void updateAllArts(EventArtsReceived eventArtsReceived)
    {
        Log.d(LOG, "updateAllArtsMap called");
        ArrayList<Article> articles = eventArtsReceived.getArts();
        Log.d(LOG, "articles.size() " + articles.size());
        this.allArticles.addAll(articles);
    }

    @Subscribe
    public void updateImage(EventShowImage eventShowImage)
    {
//        Log.d(LOG, "updateImage called");
        String imageUrl = eventShowImage.getImageUrl();
        MyAnimationUtils.changeImageWithAlphaAnimation(cover, toolbarImage, imageUrl);
    }

    @Override
    protected void onStop()
    {
//        Log.d(LOG, "onStop called!");
        super.onStop();
        SingltonOtto.getInstance().unregister(this);
    }

    @Override
    protected void onStart()
    {
//        Log.d(LOG, "onStart called!");
        super.onStart();

        if (!spiceManager.isStarted())
        {
            spiceManager.start(ctx);
        }
        if (!spiceManagerOffline.isStarted())
        {
            spiceManagerOffline.start(ctx);
        }

        SingltonOtto.getInstance().register(this);
    }

    @Override
    protected void onPause()
    {
//        Log.d(LOG, "onPause called!");
        super.onPause();

        if (spiceManager.isStarted())
        {
            spiceManager.shouldStop();
        }
        if (spiceManagerOffline.isStarted())
        {
            spiceManagerOffline.shouldStop();
        }
    }

    @Override
    protected void onResume()
    {
//        Log.d(LOG, "onResume called!");
        super.onResume();

        if (!spiceManager.isStarted())
        {
            spiceManager.start(ctx);
        }
        if (!spiceManagerOffline.isStarted())
        {
            spiceManagerOffline.start(ctx);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CUR_NAV_ITEM_SELECTED_ID, currentSelectedNavItemsId);
        outState.putParcelableArrayList(Article.LOG, allArticles);
        outState.putString(FragmentArticlesList.KEY_RSS_URL, rssUrl);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(LOG, "onCreate");

        this.ctx = this;

        //get default settings to get all settings later
        PreferenceManager.setDefaultValues(this, R.xml.pref_design, true);
        PreferenceManager.setDefaultValues(this, R.xml.pref_system, true);
        this.pref = PreferenceManager.getDefaultSharedPreferences(this);
        //set theme before super and set content to apply it
        int themeId = (pref.getBoolean(getString(R.string.pref_design_key_night_mode), false)) ? R.style.My_Theme_Dark : R.style.My_Theme_Light;
        this.setTheme(themeId);
        //call super after setTheme to set it 0_0
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null)
        {
            this.allArticles = getIntent().getExtras().getParcelableArrayList(Article.LOG);
            this.rssUrl = getIntent().getExtras().getString(FragmentArticlesList.KEY_RSS_URL);
        }
        else
        {
            this.allArticles = savedInstanceState.getParcelableArrayList(Article.LOG);
            this.rssUrl = savedInstanceState.getString(FragmentArticlesList.KEY_RSS_URL);
        }

        setContentView(R.layout.activity_main);

        //find views by id
        initializeViews();
        //setup drawer, toolbar, drawerToggle and navigationView listener
        setUpNavigationDrawer();

        MyAnimationUtils.startTranslateAnimation(ctx, toolbarImage);

        String allImages = allArticles.get(pager.getCurrentItem()).getImageUrls();
        String imageUrl = Article.getRandomImageUrlFromString(allImages);
        updateImage(new EventShowImage(imageUrl));

        VKUtils.checkVKAuth((AppCompatActivity) ctx, navigationView);
        //admob
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest.Builder builder;
        builder = new AdRequest.Builder();
        String androidId = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
        String deviceId = MD5.convert(androidId);
        if (deviceId != null)
        {
            builder.addTestDevice(deviceId.toUpperCase());
        }
        AdRequest adRequest = builder.build();
        boolean isTestDevice = adRequest.isTestDevice(ctx);
        Log.v(LOG, "is Admob Test Device ? " + deviceId + " " + isTestDevice);
        if (mAdView != null)
        {
            mAdView.loadAd(adRequest);
        }

        this.pref.registerOnSharedPreferenceChangeListener(this);
    }

    private void initializeViews()
    {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        pager = (ViewPager) findViewById(R.id.pager);

        toolbarImage = (ImageView) findViewById(R.id.toolbar_image);
        cover = findViewById(R.id.cover);
    }

    private void setUpNavigationDrawer()
    {
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayShowTitleEnabled(false);

            actionBar.setDisplayHomeAsUpEnabled(true);

            mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.app_name, R.string.app_name)
            {
                public void onDrawerClosed(View view)
                {
                    supportInvalidateOptionsMenu();
                }

                public void onDrawerOpened(View drawerView)
                {
                }
            };
            mDrawerToggle.setDrawerIndicatorEnabled(false);

            drawerLayout.setDrawerListener(mDrawerToggle);
        }

        setUpNavigationViewAndViewPager();
    }

    public void updateAllRssChanels()
    {
        MyRoboSpiceDataBaseHelper databaseHelper = new MyRoboSpiceDataBaseHelper(ctx, MyRoboSpiceDataBaseHelper.DB_NAME, MyRoboSpiceDataBaseHelper.DB_VERSION);
        rssChanels.clear();
        try
        {
            rssChanels.addAll(databaseHelper.getDaoRssChanel().queryForAll());
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void setUpNavigationViewAndViewPager()
    {
        final ArrayList<Integer> menuIds = new ArrayList<>();
        //clear menu
        navigationView.getMenu().clear();
        //set items
        //getChanels from DB
        updateAllRssChanels();
        for (int i = 0; i < rssChanels.size(); i++)
        {
            menuIds.add(100 * i);
            navigationView.getMenu().add(0, menuIds.get(i), i, rssChanels.get(i).getTitle());
            if (rssChanels.get(i).getUrl().equals(rssUrl))
            {
                currentSelectedNavItemsId = menuIds.get(i);
            }
        }
        navigationView.getMenu().setGroupCheckable(0, true, true);
        navigationView.setCheckedItem(currentSelectedNavItemsId);
        //add btn for adding rssChanels
        menuIds.add(menuIds.size() * 100);
        navigationView.getMenu().add(1, menuIds.get(menuIds.size() - 1), menuIds.size(), "Добавить Rss-ленту");
        //add btn for removing rssChanels
        menuIds.add(menuIds.size() * 100);
        navigationView.getMenu().add(1, menuIds.get(menuIds.size() - 1), menuIds.size(), "Удалить Rss-ленту");


        onPageChangeListener = new ViewPager.SimpleOnPageChangeListener()
        {
            @Override
            public void onPageSelected(int position)
            {
                Log.d(LOG, "onPageSelected with position: " + position);

                toolbar.setTitle(allArticles.get(position).getTitle());
                collapsingToolbarLayout.setTitle(allArticles.get(position).getTitle());

                String allImages = allArticles.get(position).getImageUrls();
                String imageUrl = Article.getRandomImageUrlFromString(allImages);
                updateImage(new EventShowImage(imageUrl));

                super.onPageSelected(position);
            }
        };
        pager.addOnPageChangeListener(onPageChangeListener);

        if (pager.getAdapter() == null)
        {
            pager.setAdapter(new PagerAdapterArticle(getSupportFragmentManager(), allArticles));
        }
        else
        {
            pager.getAdapter().notifyDataSetChanged();
        }

        NavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener;
        onNavigationItemSelectedListener = new NavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(MenuItem item)
            {
                Log.i(LOG, "onNavigationItemSelected");
                Log.d(LOG, "item.getItemId(): " + item.getItemId());
                //show add RSS dialog
                if (item.getItemId() == menuIds.get(menuIds.size() - 2))
                {
                    Log.d(LOG, item.getTitle().toString());
                    drawerLayout.closeDrawer(GravityCompat.START);
                    FragmentDialogAddRss.newInstance().show(getFragmentManager(), FragmentDialogAddRss.LOG);
                    return false;
                }
                //show remove RSS dialog
                if (item.getItemId() == menuIds.get(menuIds.size() - 1))
                {
                    Log.d(LOG, item.getTitle().toString());
                    drawerLayout.closeDrawer(GravityCompat.START);
                    FragmentDialogRemoveRss.newInstance().show(getFragmentManager(), FragmentDialogRemoveRss.LOG);
                    return false;
                }
                currentSelectedNavItemsId = item.getItemId();
                Log.d(LOG, "item.getItemId(): " + currentSelectedNavItemsId);
                //if not last element (which is add new rss
                //we must simple change viewPagers fragment
//                int indexInRSSList = menuIds.indexOf(currentSelectedNavItemsId);
//                onPageChangeListener.onPageSelected(indexInRSSList);
//                pager.setCurrentItem(indexInRSSList, true);
                //TODO start ActivityMain
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        };

        navigationView.setNavigationItemSelectedListener(onNavigationItemSelectedListener);
        navigationView.setCheckedItem(currentSelectedNavItemsId);
        onNavigationItemSelectedListener.onNavigationItemSelected(navigationView.getMenu().findItem(currentSelectedNavItemsId));
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    //workaround from http://stackoverflow.com/a/30337653/3212712 to show menu icons
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu)
    {
        if (menu != null)
        {
            if (menu.getClass().getSimpleName().equals("MenuBuilder"))
            {
                try
                {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                }
                catch (Exception e)
                {
                    Log.e(getClass().getSimpleName(), "onMenuOpened...unable to set icons for overflow menu", e);
                }
            }

            boolean nightModeIsOn = this.pref.getBoolean(getString(R.string.pref_design_key_night_mode), false);
            MenuItem themeMenuItem = menu.findItem(R.id.night_mode_switcher);
            if (nightModeIsOn)
            {
                themeMenuItem.setChecked(true);
            }
        }
        return super.onPrepareOptionsPanel(view, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
//        Log.d(LOG, "onCreateOptionsMenu called");
        getMenuInflater().inflate(R.menu.menu_article, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Log.d(LOG, "onOptionsItemSelected");

        int id = item.getItemId();

        boolean nightModeIsOn = this.pref.getBoolean(getString(R.string.pref_design_key_night_mode), false);

        switch (id)
        {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.night_mode_switcher:
                this.pref.edit().putBoolean(getString(R.string.pref_design_key_night_mode), !nightModeIsOn).commit();
                return true;
            case R.id.text_size:
                FragmentDialogTextAppearance textAppearance = FragmentDialogTextAppearance.newInstance();
                textAppearance.show(getFragmentManager(), FragmentDialogTextAppearance.LOG);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.equals(this.getString(R.string.pref_design_key_night_mode)))
        {
            this.recreate();
        }
    }

    @Override
    public void onBackPressed()
    {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>()
        {
            @Override
            public void onResult(VKAccessToken res)
            {
                // Пользователь успешно авторизовался
                VKUtils.checkVKAuth((AppCompatActivity) ctx, navigationView);
//                new Targeting_402.SocialNetworkAccess().passTokenData(Targeting_402.SocialNetworkAccess.SocialNetwork.VK, res.accessToken, res.userId);
            }

            @Override
            public void onError(VKError error)
            {
                // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
                VKUtils.checkVKAuth((AppCompatActivity) ctx, navigationView);
            }
        }))
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}