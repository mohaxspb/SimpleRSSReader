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
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import ru.kuchanov.simplerssreader.R;
import ru.kuchanov.simplerssreader.adapter.PagerAdapterMain;

public class ActivityMain extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private final static String LOG = ActivityMain.class.getSimpleName();
    private final static String KEY_CUR_NAV_ITEM_SELECTED_ID = "KEY_CUR_NAV_ITEM_SELECTED_ID";

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Context ctx;
    private SharedPreferences pref;

    private AppBarLayout appBar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView navigationView;
    private ViewPager pager;
    private ViewPager.OnPageChangeListener onPageChangeListener;
    private int currentSelectedNavItemsId;
    private CoordinatorLayout coordinatorLayout;

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
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);

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
        Set<String> values = pref.getStringSet(getString(R.string.pref_system_key_rss_urls), null);
        ArrayList<String> rssUrls;
        if (values == null)
        {
            return;
        }
        rssUrls = new ArrayList<>(values);
        final ArrayList<String> rssTitles = new ArrayList<>(rssUrls);//TODO get them from DB
        final ArrayList<Integer> menuIds = new ArrayList<>();

        for (int i = 0; i < rssUrls.size(); i++)
        {
//            String url = rssUrls.get(i);
            menuIds.add(100 * i);
            navigationView.getMenu().add(0, menuIds.get(i), i, rssTitles.get(i));
            tabLayout.addTab(tabLayout.newTab().setText(rssTitles.get(i)));
        }

        pager.setAdapter(new PagerAdapterMain(getSupportFragmentManager(), rssUrls));
        onPageChangeListener = new ViewPager.SimpleOnPageChangeListener()
        {
            @Override
            public void onPageSelected(int position)
            {
                Log.d(LOG, "onPageSelected with position: " + position);
                collapsingToolbarLayout.setTitle(rssTitles.get(position));
                super.onPageSelected(position);
            }
        };
        pager.addOnPageChangeListener(onPageChangeListener);

        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                int position = tab.getPosition();
                if (position == 0)
                {
                    onPageChangeListener.onPageSelected(0);
                }
                else
                {
                    pager.setCurrentItem(position);
                }
                navigationView.setCheckedItem(menuIds.get(position));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
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
        });
        navigationView.setCheckedItem(currentSelectedNavItemsId);
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
