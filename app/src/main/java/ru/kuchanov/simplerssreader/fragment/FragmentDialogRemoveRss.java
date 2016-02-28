package ru.kuchanov.simplerssreader.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import ru.kuchanov.simplerssreader.R;
import ru.kuchanov.simplerssreader.db.MyRoboSpiceDataBaseHelper;
import ru.kuchanov.simplerssreader.db.RssChanel;
import ru.kuchanov.simplerssreader.utils.Favorites;

/**
 * Created by Юрий on 21.02.2016 20:34.
 * For SimpleRSSReader.
 */
public class FragmentDialogRemoveRss extends DialogFragment
{
    public final static String LOG = FragmentDialogRemoveRss.class.getSimpleName();
    public static final String KEY_RSS_CHANELS = "KEY_RSS_CHANELS";
    private static final String KEY_SELECTED_INDICES = "KEY_SELECTED_INDICES";
    private Context ctx;
    private ArrayList<RssChanel> rssChanels = new ArrayList<>();
    //    private MySpiceManager spiceManager;
//    private ArticlesListRequestListener requestListener = new ArticlesListRequestListener();
    private MyRoboSpiceDataBaseHelper helper;
    private MaterialDialog dialog;
    private Integer[] selectedIndices;

    public static FragmentDialogRemoveRss newInstance()
    {
        return new FragmentDialogRemoveRss();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        Log.i(LOG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(KEY_RSS_CHANELS, rssChanels);
        if (selectedIndices != null)
        {
            int[] unBoxedIntegerArray = new int[selectedIndices.length];
            for (int i = 0; i < selectedIndices.length; i++)
            {
                Integer integer = selectedIndices[i];
                unBoxedIntegerArray[i] = integer;
            }
            outState.putIntArray(KEY_SELECTED_INDICES, unBoxedIntegerArray);
        }
        else
        {
            outState.putIntArray(KEY_SELECTED_INDICES, null);
        }
    }

    @Override
    public void onCreate(Bundle savedState)
    {
        Log.i(LOG, "onCreate");
        super.onCreate(savedState);
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
            int[] unBoxedIntegerArray = savedInstanceState.getIntArray(KEY_SELECTED_INDICES);
            if (unBoxedIntegerArray != null)
            {
                selectedIndices = new Integer[unBoxedIntegerArray.length];
                for (int i = 0; i < unBoxedIntegerArray.length; i++)
                {
                    selectedIndices[i] = unBoxedIntegerArray[i];
                }
            }
            Log.d(LOG, Arrays.toString(selectedIndices));
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
                .alwaysCallMultiChoiceCallback()
                .itemsCallbackMultiChoice(selectedIndices, new MaterialDialog.ListCallbackMultiChoice()
                {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text)
                    {
                        Log.d(LOG, Arrays.toString(text));
                        selectedIndices = which;
                        Log.d(LOG, "selectedIndices: " + Arrays.toString(selectedIndices));
                        return true;
                    }
                })
                .autoDismiss(false)
                .neutralText(R.string.remove_selected)
                .onNeutral(new MaterialDialog.SingleButtonCallback()
                {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
                    {
                        Log.d(LOG, "selectedIndices: " + Arrays.toString(selectedIndices));
                        ArrayList<RssChanel> rssChanelsToDelete = new ArrayList<>();
                        for (int index : selectedIndices)
                        {
                            rssChanelsToDelete.add(rssChanels.get(index));
                        }
                        try
                        {
                            int deletedNum = helper.getDaoRssChanel().delete(rssChanelsToDelete);
                            Log.d(LOG, "deletedNum: " + deletedNum);
                        }
                        catch (SQLException e)
                        {
                            e.printStackTrace();
                        }
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

        dialog = builder.build();

//        Integer[] selectedIndices = dialog.getSelectedIndices();
        Log.d(LOG, "selectedIndices: " + Arrays.toString(selectedIndices));

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