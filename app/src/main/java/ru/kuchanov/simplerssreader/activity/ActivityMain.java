package ru.kuchanov.simplerssreader.activity;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
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
import android.widget.SimpleCursorAdapter;

import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.squareup.otto.Subscribe;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import ru.kuchanov.simplerssreader.R;
import ru.kuchanov.simplerssreader.adapter.PagerAdapterMain;
import ru.kuchanov.simplerssreader.db.Article;
import ru.kuchanov.simplerssreader.db.ArticleRssChanel;
import ru.kuchanov.simplerssreader.db.MyRoboSpiceDataBaseHelper;
import ru.kuchanov.simplerssreader.db.RssChanel;
import ru.kuchanov.simplerssreader.fragment.FragmentDialogAddRss;
import ru.kuchanov.simplerssreader.fragment.FragmentDialogRemoveRss;
import ru.kuchanov.simplerssreader.fragment.FragmentDialogTextAppearance;
import ru.kuchanov.simplerssreader.otto.EventArtsReceived;
import ru.kuchanov.simplerssreader.otto.EventShowImage;
import ru.kuchanov.simplerssreader.otto.SingltonOtto;
import ru.kuchanov.simplerssreader.robospice.MySpiceManager;
import ru.kuchanov.simplerssreader.robospice.SingltonRoboSpice;
import ru.kuchanov.simplerssreader.utils.Const;
import ru.kuchanov.simplerssreader.utils.DataBaseFileSaver;
import ru.kuchanov.simplerssreader.utils.VKUtils;
import ru.kuchanov.simplerssreader.utils.ads.MD5;
import ru.kuchanov.simplerssreader.utils.anim.MyAnimationUtils;

public class ActivityMain extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private final static String LOG = ActivityMain.class.getSimpleName();
    private final static String KEY_CUR_NAV_ITEM_SELECTED_ID = "KEY_CUR_NAV_ITEM_SELECTED_ID";
    private IInAppBillingService mService;
    private ServiceConnection mServiceConn = new ServiceConnection()
    {
        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            mService = IInAppBillingService.Stub.asInterface(service);
            Log.d(LOG, "onServiceConnected");
        }
    };
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Context ctx;
    private SharedPreferences pref;
    //    private AppBarLayout appBar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView navigationView;
    private ViewPager pager;
    private ViewPager.OnPageChangeListener onPageChangeListener;
    private int currentSelectedNavItemsId = 0;
    //    private CoordinatorLayout coordinatorLayout;
    private View cover;
    private ImageView toolbarImage;
    private Map<String, ArrayList<Article>> allArticles = new HashMap<>();
    private MySpiceManager spiceManager = SingltonRoboSpice.getInstance().getSpiceManager();
    private MySpiceManager spiceManagerOffline = SingltonRoboSpice.getInstance().getSpiceManagerOffline();
    private ArrayList<RssChanel> chanels = new ArrayList<>();

    @Subscribe
    public void updateAllArtsMap(EventArtsReceived eventArtsReceived)
    {
        Log.d(LOG, "updateAllArtsMap called");
        String rssChanelUrl = eventArtsReceived.getRssChanelUrl();
        ArrayList<Article> articles = eventArtsReceived.getArts();
        Log.d(LOG, "articles.size() " + articles.size());
        this.allArticles.put(rssChanelUrl, articles);
    }

    @Subscribe
    public void updateImage(EventShowImage eventShowImage)
    {
//        Log.d(LOG, "updateImage called");
        String imageUrl = eventShowImage.getImageUrl();
        MyAnimationUtils.changeImageWithAlphaAnimation(cover, toolbarImage, imageUrl);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_CUR_NAV_ITEM_SELECTED_ID, currentSelectedNavItemsId);
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

        MyAnimationUtils.startTranslateAnimation(ctx, toolbarImage);

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
        //connect InAppBillService
        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        //catch prefs changed events
        this.pref.registerOnSharedPreferenceChangeListener(this);
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
            if (requestCode == 1001)
            {
                int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
                String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
                String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

                if (resultCode == RESULT_OK)
                {
                    try
                    {
                        JSONObject jo = new JSONObject(purchaseData);
                        String sku = jo.getString("productId");
                        Log.d(LOG, "You have bought the " + sku + ". Excellent choice, adventurer !");
                    }
                    catch (JSONException e)
                    {
                        Log.d(LOG, "Failed to parse purchase data.");
                        e.printStackTrace();
                    }
                }
            }
            else
            {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private void buySubscription()
    {
        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                Log.i(LOG, "run called");
                try
                {
                    Bundle buyIntentBundle = mService.getBuyIntent(3,
                            getPackageName(),
                            "full_version_subscription",
                            "subs",
                            String.valueOf(System.currentTimeMillis()));

                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

                    if (pendingIntent != null)
                    {
                        startIntentSenderForResult(pendingIntent.getIntentSender(), 1001, new Intent(), 0, 0, 0);
                    }
                }
                catch (RemoteException | IntentSender.SendIntentException e)
                {
                    e.printStackTrace();
                }
            }
        };

        thread.start();

    }

    private void getPricesFromGP()
    {
        ArrayList<String> skuList = new ArrayList<>();
        skuList.add("full_version_subscription");
        final Bundle querySkus = new Bundle();
        querySkus.putStringArrayList("ITEM_ID_LIST", skuList);

        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                Log.i(LOG, "run called");
                try
                {
                    Bundle skuDetails = mService.getSkuDetails(3, getPackageName(), "subs", querySkus);

                    int response = skuDetails.getInt("RESPONSE_CODE");
                    if (response == 0)
                    {
                        ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");
                        String mPremiumUpgradePrice;

                        Log.i(LOG, "responseList: " + Arrays.toString(responseList.toArray()));

                        for (String thisResponse : responseList)
                        {

                            Log.d(LOG, thisResponse);
                            JSONObject object = new JSONObject(thisResponse);
                            String sku = object.getString("productId");
                            String price = object.getString("price");
                            if (sku.equals("premiumUpgrade"))
                            {
                                mPremiumUpgradePrice = price;
                                Log.d(LOG, "mPremiumUpgradePrice: " + mPremiumUpgradePrice);
                            }
                        }
                    }
                    else
                    {
                        Log.d(LOG, "response: " + response);
                    }
                }
                catch (RemoteException | JSONException e)
                {
                    e.printStackTrace();
                }
            }
        };

        thread.start();
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
        //        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);
//        appBar = (AppBarLayout) findViewById(R.id.app_bar_layout);
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

        setUpNavigationViewAndViewPager();
    }

    public ArrayList<RssChanel> getAllRssChanels()
    {
        MyRoboSpiceDataBaseHelper databaseHelper = new MyRoboSpiceDataBaseHelper(ctx, MyRoboSpiceDataBaseHelper.DB_NAME, MyRoboSpiceDataBaseHelper.DB_VERSION);
        chanels.clear();
        try
        {
            chanels.addAll(databaseHelper.getDaoRssChanel().queryForAll());
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return chanels;
    }

    public void setUpNavigationViewAndViewPager()
    {
        final ArrayList<Integer> menuIds = new ArrayList<>();
        //clear menu
        navigationView.getMenu().clear();
        //set items
        for (int i = 0; i < getAllRssChanels().size(); i++)
        {
            menuIds.add(100 * i);
            navigationView.getMenu().add(0, menuIds.get(i), i, getAllRssChanels().get(i).getTitle());
        }
        navigationView.getMenu().setGroupCheckable(0, true, true);
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
                toolbar.setTitle(getAllRssChanels().get(position).getTitle());
                collapsingToolbarLayout.setTitle(getAllRssChanels().get(position).getTitle());
                currentSelectedNavItemsId = menuIds.get(position);
                navigationView.setCheckedItem(currentSelectedNavItemsId);

                String imageUrl = Article.getRandomImageUrlFromArticlesList(allArticles.get(getAllRssChanels().get(position).getUrl()));
                updateImage(new EventShowImage(imageUrl));

                super.onPageSelected(position);
            }
        };
        pager.addOnPageChangeListener(onPageChangeListener);

        if (pager.getAdapter() == null)
        {
            pager.setAdapter(new PagerAdapterMain(getSupportFragmentManager(), getAllRssChanels()));
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
                MyRoboSpiceDataBaseHelper helper = new MyRoboSpiceDataBaseHelper(ctx, MyRoboSpiceDataBaseHelper.DB_NAME, MyRoboSpiceDataBaseHelper.DB_VERSION);
                ArticleRssChanel.deleteSomeArts(5, "http://www.vestifinance.ru/yandex.xml", helper);
                break;
            case R.id.get_db:
                DataBaseFileSaver.copyDatabase(ctx, MyRoboSpiceDataBaseHelper.DB_NAME);
                break;
            case R.id.call_in_app:
                getPricesFromGP();
                break;
            case R.id.buy_subs:
                buySubscription();
                break;
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
        protected void onStop ()
        {
//        Log.d(LOG, "onStop called!");
            super.onStop();

            SingltonOtto.getInstance().unregister(this);
        }

        @Override
        protected void onStart ()
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
        protected void onPause ()
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
        protected void onResume ()
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
        protected void onDestroy ()
        {
            super.onDestroy();
            if (mService != null)
            {
                unbindService(mServiceConn);
            }
        }
    }