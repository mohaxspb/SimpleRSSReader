package ru.kuchanov.simplerssreader.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.PendingRequestListener;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

import ru.kuchanov.simplerssreader.R;
import ru.kuchanov.simplerssreader.activity.ActivityMain;
import ru.kuchanov.simplerssreader.db.Article;
import ru.kuchanov.simplerssreader.db.ArticlesList;
import ru.kuchanov.simplerssreader.db.MyRoboSpiceDataBaseHelper;
import ru.kuchanov.simplerssreader.db.RssChanel;
import ru.kuchanov.simplerssreader.robospice.MySpiceManager;
import ru.kuchanov.simplerssreader.robospice.SingltonRoboSpice;
import ru.kuchanov.simplerssreader.robospice.request.RequestRssFeed;

/**
 * Created by Юрий on 21.02.2016 20:34.
 * For SimpleRSSReader.
 */
public class FragmentDialogAddRss extends DialogFragment
{
    public final static String LOG = FragmentDialogAddRss.class.getSimpleName();
    public static final String KEY_RSS_CHANEL_TO_TEST = "KEY_RSS_CHANEL_TO_TEST";
    private EditText editText;
    private TextView titleTV;
    private TextView numOfArtsTV;
    private ProgressBar progressBar;
    //    private SharedPreferences pref;
    private Context ctx;
    private RssChanel rssChanelToTEst;
    private MySpiceManager spiceManager;
    private ArticlesListRequestListener requestListener = new ArticlesListRequestListener();
    private MyRoboSpiceDataBaseHelper helper;
    private MaterialDialog dialog;

    public static FragmentDialogAddRss newInstance()
    {
        return new FragmentDialogAddRss();
    }
//    private ArrayList<Article> loadedArtilces;

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putParcelable(KEY_RSS_CHANEL_TO_TEST, rssChanelToTEst);
    }

    @Override
    public void onCreate(Bundle savedState)
    {
        super.onCreate(savedState);
        Log.i(LOG, "onCreate");
        this.ctx = this.getActivity();
        this.helper = new MyRoboSpiceDataBaseHelper(ctx);

//        this.pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        if (savedState != null)
        {
            rssChanelToTEst = savedState.getParcelable(KEY_RSS_CHANEL_TO_TEST);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Log.i(LOG, "onCreateDialog");

        MaterialDialog.Builder builder = new MaterialDialog.Builder(ctx);
        builder.title("Добавить RSS-ленту")
                .positiveText(R.string.add)
                .neutralText(R.string.test_url)
                .onNeutral(new MaterialDialog.SingleButtonCallback()
                {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
                    {
                        if (!TextUtils.isEmpty(editText.getText()))
                        {
                            String enteredUrl = editText.getText().toString();
                            //check for starting with http:// or https://
                            if (enteredUrl.startsWith("https://"))
                            {
                                Log.e(LOG, "enteredUrl: enteredUrl.startsWith(\"https://\")");
                            }
                            if (!enteredUrl.startsWith("http://") && !enteredUrl.startsWith("https://"))
                            {
                                Log.e(LOG, "enteredUrl: " + enteredUrl);
                                Toast.makeText(ctx, "Адрес должен начинаться с \"http://\"", Toast.LENGTH_SHORT).show();
                                editText.requestFocus();
                                return;
                            }
                            //try find it in DB
                            RssChanel rssChanelInDB = RssChanel.getRssChanelByUrl(enteredUrl, helper);
                            if (rssChanelInDB != null)
                            {
                                Toast.makeText(ctx, "Эта лента (" + rssChanelInDB.getTitle() + ") уже добавлена!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            //try load rss;
                            Log.d(LOG, "Try load rss from " + enteredUrl);
                            rssChanelToTEst = new RssChanel();
                            rssChanelToTEst.setUrl(enteredUrl);

                            performRequest(rssChanelToTEst.getUrl());
                        }
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback()
                {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
                    {
//                        Toast.makeText(ctx, "RSS-лента добавлена", Toast.LENGTH_SHORT).show();
                        //write checked rssChanel to DB
                        try
                        {
                            helper.getDaoRssChanel().create(rssChanelToTEst);
                            Toast.makeText(ctx, "RSS-лента добавлена", Toast.LENGTH_SHORT).show();
                            //redraw navigationView
                            //TODO swith by activity
                            try
                            {
                                ActivityMain activity = (ActivityMain) ctx;
                                activity.setUpNavigationViewAndViewPager();
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }

                        }
                        catch (SQLException e)
                        {
                            e.printStackTrace();
                        }
                        materialDialog.dismiss();
                    }
                })
                .autoDismiss(false)
                .customView(R.layout.fragment_dialog_test_rss_url, true);

        dialog = builder.build();

        View customView = dialog.getCustomView();
        if (customView == null)
        {
            return dialog;
        }

        editText = (EditText) customView.findViewById(R.id.edit);
        progressBar = (ProgressBar) customView.findViewById(R.id.progress);
        titleTV = (TextView) customView.findViewById(R.id.title);
        numOfArtsTV = (TextView) customView.findViewById(R.id.num_of_arts);

        if (TextUtils.isEmpty(editText.getText()))
        {
            dialog.getActionButton(DialogAction.NEUTRAL).setEnabled(false);
            dialog.getActionButton(DialogAction.NEUTRAL).setActivated(false);
        }
        else
        {
            dialog.getActionButton(DialogAction.NEUTRAL).setEnabled(true);
            dialog.getActionButton(DialogAction.NEUTRAL).setActivated(true);
        }

        //activate/disactivate add chanel btn
        //if title is set for rssChanel
        //it's set only if we have success on request
        //and receive some arts and title as well;
        if (rssChanelToTEst != null)
        {
            if (rssChanelToTEst.getTitle() == null)
            {
                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                dialog.getActionButton(DialogAction.POSITIVE).setActivated(false);
            }
            else
            {
                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                dialog.getActionButton(DialogAction.POSITIVE).setActivated(true);

                titleTV.setText(Html.fromHtml("<b>Заголовок:</b>\n"));
                titleTV.append(rssChanelToTEst.getTitle());
//                numOfArtsTV.setText(Html.fromHtml("<b>Статей:</b> "));
//                numOfArtsTV.append(String.valueOf(loadedArticles.size()));
            }
        }
        else
        {
            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
            dialog.getActionButton(DialogAction.POSITIVE).setActivated(false);
        }

        editText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                if (TextUtils.isEmpty(s))
                {
                    dialog.getActionButton(DialogAction.NEUTRAL).setEnabled(false);
                    dialog.getActionButton(DialogAction.NEUTRAL).setActivated(false);
                }
                else
                {
                    dialog.getActionButton(DialogAction.NEUTRAL).setEnabled(true);
                    dialog.getActionButton(DialogAction.NEUTRAL).setActivated(true);
                }
            }
        });


        return dialog;
    }

    @Override
    public void onStart()
    {
//        Log.i(LOG, "onStart called");
        super.onStart();

        spiceManager = SingltonRoboSpice.getInstance().getSpiceManager();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        spiceManager.addListenerIfPending(ArticlesList.class, LOG, requestListener);
    }

    private void performRequest(String url)
    {
        RequestRssFeed requestRssFeed = new RequestRssFeed(ctx, url);
        requestRssFeed.setTestRssChanel();
        spiceManager.execute(requestRssFeed, LOG, DurationInMillis.ALWAYS_EXPIRED, requestListener);

        //update dialog
        editText.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        titleTV.setText(null);
        numOfArtsTV.setText(null);

        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
        dialog.getActionButton(DialogAction.POSITIVE).setActivated(false);
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

            progressBar.setVisibility(View.INVISIBLE);
            editText.setEnabled(true);

            Toast.makeText(ctx, "Ошибка соединения", Toast.LENGTH_SHORT).show();
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

                Log.d(LOG, "articles.size() " + loadedArticles.size());

                //update dialog
                progressBar.setVisibility(View.INVISIBLE);
                if (articlesList.getRssChanelTitle() != null && loadedArticles.size() != 0)
                {
                    rssChanelToTEst.setTitle(articlesList.getRssChanelTitle());
                    titleTV.setText(Html.fromHtml("<b>Заголовок:</b>\n"));
                    titleTV.append(rssChanelToTEst.getTitle());
                    numOfArtsTV.setText(Html.fromHtml("<b>Статей:</b> "));
                    numOfArtsTV.append(String.valueOf(loadedArticles.size()));

                    dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                    dialog.getActionButton(DialogAction.POSITIVE).setActivated(true);
                }
                editText.setEnabled(true);
            }
            else
            {
                //no error, but articlesList is null,
                Log.d(LOG, "No error but no articles too... So maybe it's not rss?..");
                Toast.makeText(ctx, "Похоже, что это не RSS-лента. Попробуйте другой адрес", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
                editText.setEnabled(true);
            }
        }
    }
}