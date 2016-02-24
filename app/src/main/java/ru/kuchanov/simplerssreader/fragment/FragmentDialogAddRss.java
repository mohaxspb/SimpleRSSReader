package ru.kuchanov.simplerssreader.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import ru.kuchanov.simplerssreader.R;
import ru.kuchanov.simplerssreader.utils.MyRoboSpiceDatabaseHelper;
import ru.kuchanov.simplerssreader.db.RssChanel;

/**
 * Created by Юрий on 21.02.2016 20:34.
 * For SimpleRSSReader.
 */
public class FragmentDialogAddRss extends DialogFragment
{
    public final static String LOG = FragmentDialogAddRss.class.getSimpleName();
    public static final String KEY_RSS_CHANEL_TO_TEST = "KEY_RSS_CHANEL_TO_TEST";
    private EditText editText;
    private ProgressBar progressBar;
    private SharedPreferences pref;
    private Context ctx;
    private RssChanel rssChanelToTEst;


    public static FragmentDialogAddRss newInstance()
    {
        return new FragmentDialogAddRss();
    }

    @Override
    public void onCreate(Bundle savedState)
    {
        super.onCreate(savedState);
        Log.i(LOG, "onCreate");
        this.ctx = this.getActivity();

        this.pref = PreferenceManager.getDefaultSharedPreferences(ctx);
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

//        float artTextScale = pref.getFloat(ctx.getString(R.string.pref_design_key_text_size_article), 0.75f);
//        int textSizePrimary = ctx.getResources().getDimensionPixelSize(R.dimen.text_size_primary);
//        float scaledTextSizePrimary = artTextScale * textSizePrimary;

        final MaterialDialog dialog;

        MaterialDialog.Builder builder = new MaterialDialog.Builder(ctx);
        builder.title("Добавить RSS-ленту")
                .positiveText(R.string.add)
                .neutralText(R.string.test_url)
                .onNeutral(new MaterialDialog.SingleButtonCallback()
                {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
                    {
//TODO test
                        if (!TextUtils.isEmpty(editText.getText()))
                        {
                            String enteredUrl = editText.getText().toString();
                            //check for starting with http://
                            if (!enteredUrl.startsWith("http://"))
                            {
                                Toast.makeText(ctx, "Адрес должен начинаться с \"http://\"", Toast.LENGTH_SHORT).show();
                                editText.requestFocus();
                                return;
                            }
                            //try find it in DB
                            MyRoboSpiceDatabaseHelper helper = new MyRoboSpiceDatabaseHelper(ctx);
                            RssChanel rssChanelInDB = RssChanel.getRssChanelByUrl(enteredUrl, helper);
                            if (rssChanelInDB != null)
                            {
                                Toast.makeText(ctx, "Эта лента (" + rssChanelInDB.getTitle() + ") уже добавлена!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            //TODO load rss;
                            Log.d(LOG, "Try load rss from " + editText.getText());
                        }
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback()
                {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
                    {
                        Toast.makeText(ctx, "RSS-лента добавлена", Toast.LENGTH_SHORT).show();
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
        progressBar = (ProgressBar) customView.findViewById(R.id.progress);


        return dialog;
    }
}