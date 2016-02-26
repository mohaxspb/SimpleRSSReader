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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import ru.kuchanov.simplerssreader.R;
import ru.kuchanov.simplerssreader.activity.ActivityMain;
import ru.kuchanov.simplerssreader.db.Article;
import ru.kuchanov.simplerssreader.db.ArticlesList;
import ru.kuchanov.simplerssreader.db.MyRoboSpiceDataBaseHelper;
import ru.kuchanov.simplerssreader.db.RssChanel;
import ru.kuchanov.simplerssreader.robospice.SingltonRoboSpice;
import ru.kuchanov.simplerssreader.robospice.request.RequestRssFeed;
import ru.kuchanov.simplerssreader.utils.RssParser;

/**
 * Created by Юрий on 21.02.2016 20:34.
 * For SimpleRSSReader.
 */
public class FragmentDialogRemoveRss extends DialogFragment
{
    public final static String LOG = FragmentDialogRemoveRss.class.getSimpleName();
    public static final String KEY_RSS_CHANELS = "KEY_RSS_CHANELS";
    private Context ctx;
    private ArrayList<RssChanel> rssChanels = new ArrayList<>();
    //    private MySpiceManager spiceManager;
//    private ArticlesListRequestListener requestListener = new ArticlesListRequestListener();
    private MyRoboSpiceDataBaseHelper helper;
    private MaterialDialog dialog;

    public static FragmentDialogRemoveRss newInstance()
    {
        return new FragmentDialogRemoveRss();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(KEY_RSS_CHANELS, rssChanels);
    }

    @Override
    public void onCreate(Bundle savedState)
    {
        super.onCreate(savedState);
        Log.i(LOG, "onCreate");
        this.ctx = this.getActivity();
        this.helper = new MyRoboSpiceDataBaseHelper(ctx);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Log.i(LOG, "onCreateDialog");

        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_RSS_CHANELS))
        {
            rssChanels = savedInstanceState.getParcelableArrayList(KEY_RSS_CHANELS);
        }
        else
        {
            try
            {
                rssChanels.addAll(helper.getDaoRssChanel().queryForAll());
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }

        MaterialDialog.Builder builder = new MaterialDialog.Builder(ctx);
        builder.title("Удалить RSS-ленту")
                .positiveText(R.string.close)
                .items(rssChanels)
                .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice()
                {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text)
                    {
                        Log.d(LOG, Arrays.toString(text));
                        return false;
                    }
                })
                .autoDismiss(false)
                .neutralText(R.string.remove_selected)
                .onNeutral(new MaterialDialog.SingleButtonCallback()
                {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
                    {
                        Integer[] selectedIndices = dialog.getSelectedIndices();
                        Log.d(LOG, Arrays.toString(selectedIndices));
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback()
                {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
                    {
                        dismiss();
                    }
                });
//                .customView(R.layout.fragment_dialog_test_rss_url, true);

        dialog = builder.build();

//        View customView = dialog.getCustomView();
//        if (customView == null)
//        {
//            return dialog;
//        }

        Integer[] selectedIndices = dialog.getSelectedIndices();
        Log.d(LOG, Arrays.toString(selectedIndices));

        if (selectedIndices == null)
        {
            dialog.getActionButton(DialogAction.NEUTRAL).setEnabled(false);
            dialog.getActionButton(DialogAction.NEUTRAL).setActivated(false);
        }
        else
        {
            dialog.getActionButton(DialogAction.NEUTRAL).setEnabled(true);
            dialog.getActionButton(DialogAction.NEUTRAL).setActivated(true);
        }

        return dialog;
    }
}