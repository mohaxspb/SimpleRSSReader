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
import java.sql.SQLException;
import java.util.ArrayList;

import ru.kuchanov.simplerssreader.R;
import ru.kuchanov.simplerssreader.adapter.PagerAdapterMain;
import ru.kuchanov.simplerssreader.db.ArticleRssChanel;
import ru.kuchanov.simplerssreader.utils.MyRoboSpiceDatabaseHelper;
import ru.kuchanov.simplerssreader.db.RssChanel;
import ru.kuchanov.simplerssreader.fragment.FragmentDialogAddRss;
import ru.kuchanov.simplerssreader.robospice.MySpiceManager;
import ru.kuchanov.simplerssreader.robospice.SingltonRoboSpice;
import ru.kuchanov.simplerssreader.utils.DataBaseFileSaver;

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
    private int currentSelectedNavItemsId = 0;
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
                }

                public void onDrawerOpened(View drawerView)
                {
                }
            };
            mDrawerToggle.setDrawerIndicatorEnabled(true);

            drawerLayout.setDrawerListener(mDrawerToggle);
        }

        setUpNavigationViewAndTabs();
    }

    public ArrayList<RssChanel> getAllRssChanels()
    {
        MyRoboSpiceDatabaseHelper databaseHelper = new MyRoboSpiceDatabaseHelper(ctx, MyRoboSpiceDatabaseHelper.DB_NAME, MyRoboSpiceDatabaseHelper.DB_VERSION);
        ArrayList<RssChanel> chanels = new ArrayList<>();
        try
        {
            chanels.addAll(databaseHelper.getDaoCategory().queryForAll());
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return chanels;
    }

    private void setUpNavigationViewAndTabs()
    {
        final ArrayList<Integer> menuIds = new ArrayList<>();
        for (int i = 0; i < getAllRssChanels().size(); i++)
        {
            menuIds.add(100 * i);
            navigationView.getMenu().add(0, menuIds.get(i), i, getAllRssChanels().get(i).getTitle());
        }
        //add btn for adding rssChanels
        menuIds.add(menuIds.size() * 100);
        navigationView.getMenu().add(1, menuIds.get(menuIds.size() - 1), menuIds.size(), "Добавить Rss-ленту");
        navigationView.getMenu().setGroupCheckable(0, true, true);

        pager.setAdapter(new PagerAdapterMain(getSupportFragmentManager(), getAllRssChanels()));
        onPageChangeListener = new ViewPager.SimpleOnPageChangeListener()
        {
            @Override
            public void onPageSelected(int position)
            {
                Log.d(LOG, "onPageSelected with position: " + position);
                toolbar.setTitle(getAllRssChanels().get(position).getTitle());
                collapsingToolbarLayout.setTitle(getAllRssChanels().get(position).getTitle());
                currentSelectedNavItemsId = menuIds.get(position);
                navigationView.setCheckedItem(currentSelectedNavItemsId);
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
                Log.d(LOG, "item.getItemId(): " + item.getItemId());
                if (item.getItemId() == menuIds.get(menuIds.size() - 1))
                {
                    Log.d(LOG, item.getTitle().toString());
                    drawerLayout.closeDrawer(GravityCompat.START);
                    //TODO
//                    new MaterialDialog.Builder(ctx)
//                            .title("Добавить RSS-ленту")
//                            .input("введите url-адрес rss-ленты", "", false, new MaterialDialog.InputCallback()
//                            {
//                                @Override
//                                public void onInput(MaterialDialog dialog, CharSequence input)
//                                {
//                                    Log.d(LOG, "input: " + input);
//                                    //TODO testLink
//                                    FragmentDialogAddRss.newInstance(input.toString()).show(getFragmentManager(), FragmentDialogAddRss.LOG);
//                                }
//                            })
//                            .inputType(InputType.TYPE_TEXT_VARIATION_URI)
//                            .show();
                    FragmentDialogAddRss.newInstance().show(getFragmentManager(), FragmentDialogAddRss.LOG);
                    return false;
                }
                currentSelectedNavItemsId = item.getItemId();
                Log.d(LOG, "item.getItemId(): " + currentSelectedNavItemsId);
                //if not last element (which is add new rss
                //we must simple change viewPagers fragment
                int indexInRSSList = menuIds.indexOf(currentSelectedNavItemsId);
                onPageChangeListener.onPageSelected(indexInRSSList);
                pager.setCurrentItem(indexInRSSList, true);
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
                return true;
            case R.id.delete_arts:
                MyRoboSpiceDatabaseHelper helper = new MyRoboSpiceDatabaseHelper(ctx, MyRoboSpiceDatabaseHelper.DB_NAME, MyRoboSpiceDatabaseHelper.DB_VERSION);
                ArticleRssChanel.deleteSomeArts(5, "http://www.vestifinance.ru/yandex.xml", helper);
                break;
            case R.id.get_db:
                DataBaseFileSaver.copyDatabase(ctx, MyRoboSpiceDatabaseHelper.DB_NAME);
                break;
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
