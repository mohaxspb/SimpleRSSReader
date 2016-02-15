package ru.kuchanov.simplerssreader.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;

import ru.kuchanov.simplerssreader.R;
import ru.kuchanov.simplerssreader.adapter.PagerAdapterMain;
import ru.kuchanov.simplerssreader.db.MyRoboSpiceDatabaseHelper;
import ru.kuchanov.simplerssreader.db.RssChanel;
import ru.kuchanov.simplerssreader.robospice.MySpiceManager;
import ru.kuchanov.simplerssreader.robospice.SingltonRoboSpice;

public class ActivityMain extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private final static String LOG = ActivityMain.class.getSimpleName();
    private final static String KEY_CUR_NAV_ITEM_SELECTED_ID = "KEY_CUR_NAV_ITEM_SELECTED_ID";

    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Context ctx;
    private SharedPreferences pref;

    private AppBarLayout appBar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView navigationView;
    private NavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener;
    private ViewPager pager;
    private ViewPager.OnPageChangeListener onPageChangeListener;
    private int currentSelectedNavItemsId;
    private CoordinatorLayout coordinatorLayout;

    private MySpiceManager spiceManager = SingltonRoboSpice.getInstance().getSpiceManager();
    private MySpiceManager spiceManagerOffline = SingltonRoboSpice.getInstance().getSpiceManagerOffline();

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
    }

    private void restoreState(Bundle savedInstanceState, Bundle intentExtras)
    {
        if (savedInstanceState == null)
        {
            //TODO
        }
        else
        {
            currentSelectedNavItemsId = savedInstanceState.getInt(KEY_CUR_NAV_ITEM_SELECTED_ID);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(LOG, "onCreate");

        this.ctx = this;

        //get default settings to get all settings later
        PreferenceManager.setDefaultValues(this, R.xml.pref_design, true);
        PreferenceManager.setDefaultValues(this, R.xml.pref_system, true);
//        PreferenceManager.setDefaultValues(this, R.xml.pref_notification, true);
//        PreferenceManager.setDefaultValues(this, R.xml.pref_about, true);
        this.pref = PreferenceManager.getDefaultSharedPreferences(this);
        //set theme before super and set content to apply it
        int themeId = (pref.getBoolean(getString(R.string.pref_design_key_night_mode), false)) ? R.style.My_Theme_Dark : R.style.My_Theme_Light;
        this.setTheme(themeId);
        //call super after setTheme to set it 0_0
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //find views by id
        initializeViews();
        //setup drawer, toolbar, drawerToggle and navigationView listener
        setUpNavigationDrawer();

        this.pref.registerOnSharedPreferenceChangeListener(this);
    }

    private void initializeViews()
    {

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        appBar = (AppBarLayout) findViewById(R.id.app_bar_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        pager = (ViewPager) findViewById(R.id.pager);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);

//        fab = (FloatingActionButton) findViewById(R.id.fab);
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
//                    drawerOpened = false;
                }

                public void onDrawerOpened(View drawerView)
                {
//                    drawerOpened = true;
                }
            };
            mDrawerToggle.setDrawerIndicatorEnabled(true);

            drawerLayout.setDrawerListener(mDrawerToggle);
        }

        setUpNavigationViewAndTabs();
    }

    private void setUpNavigationViewAndTabs()
    {
//        Set<String> values = pref.getStringSet(getString(R.string.pref_system_key_rss_urls), null);
//        ArrayList<String> rssUrls;
//        if (values == null)
//        {
//            return;
//        }
//        rssUrls = new ArrayList<>(values);
//        final ArrayList<String> rssTitles = new ArrayList<>(rssUrls);//TODO get them from DB
        MyRoboSpiceDatabaseHelper databaseHelper = new MyRoboSpiceDatabaseHelper(ctx, MyRoboSpiceDatabaseHelper.DB_NAME, MyRoboSpiceDatabaseHelper.DB_VERSION);
        final ArrayList<RssChanel> chanels = new ArrayList<>();
        try
        {
            chanels.addAll(databaseHelper.getDaoCategory().queryForAll());
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        final ArrayList<Integer> menuIds = new ArrayList<>();
        for (int i = 0; i < chanels.size(); i++)
        {
//            String url = rssUrls.get(i);
            menuIds.add(100 * i);
            navigationView.getMenu().add(0, menuIds.get(i), i, chanels.get(i).getTitle());
        }
        navigationView.getMenu().setGroupCheckable(0, true, true);

        pager.setAdapter(new PagerAdapterMain(getSupportFragmentManager(), chanels));
        onPageChangeListener = new ViewPager.SimpleOnPageChangeListener()
        {
            @Override
            public void onPageSelected(int position)
            {
                Log.d(LOG, "onPageSelected with position: " + position);
                toolbar.setTitle(chanels.get(position).getTitle());
                collapsingToolbarLayout.setTitle(chanels.get(position).getTitle());
                super.onPageSelected(position);
            }
        };
        pager.addOnPageChangeListener(onPageChangeListener);


        onNavigationItemSelectedListener = new NavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(MenuItem item)
            {
                Log.i(LOG, "onNavigationItemSelected");
                currentSelectedNavItemsId = item.getItemId();
                Log.d(LOG, "item.getItemId(): " + currentSelectedNavItemsId);
                //if not last element (which is add new rss
                //we must simple change viewPagers fragment
                int indexInRSSList = menuIds.indexOf(currentSelectedNavItemsId);
                if (indexInRSSList == 0)
                {
                    onPageChangeListener.onPageSelected(0);
                }
                else
                {
                    pager.setCurrentItem(indexInRSSList);
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        };

        navigationView.setNavigationItemSelectedListener(onNavigationItemSelectedListener);
//        navigationView.setCheckedItem(currentSelectedNavItemsId);
        onNavigationItemSelectedListener.onNavigationItemSelected(navigationView.getMenu().findItem(currentSelectedNavItemsId));
    }

    private TextView getActionBarTextView()
    {
        TextView titleTextView = null;

        try
        {
            Field f = toolbar.getClass().getDeclaredField("mTitleTextView");
            f.setAccessible(true);
            titleTextView = (TextView) f.get(toolbar);
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            Log.e(LOG, e.toString());
        }

        return titleTextView;
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Log.d(LOG, "onOptionsItemSelected");

        int id = item.getItemId();

        boolean nightModeIsOn = this.pref.getBoolean(getString(R.string.pref_design_key_night_mode), false);
//        boolean isGridManager = pref.getBoolean(ctx.getString(R.string.pref_design_key_list_style), false);

        switch (id)
        {
//            case R.id.action_settings:
//                Intent intent = new Intent(this, ActivitySettings.class);
//                this.startActivity(intent);
//                return true;
            case android.R.id.home:
                this.drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.night_mode_switcher:
                this.pref.edit().putBoolean(getString(R.string.pref_design_key_night_mode), !nightModeIsOn).commit();
//                this.recreate();
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
}