package ru.kuchanov.simplerssreader.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKList;
import com.vk.sdk.util.VKUtil;

import ru.kuchanov.simplerssreader.R;

/**
 * Created by Юрий on 02.03.2016 1:40.
 * For SimpleRSSReader.
 */
public class VKUtils
{
    private static final String LOG = VKUtils.class.getSimpleName();

    public static void showLoginDialog(final Context ctx, String messege)
    {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(ctx)
                .customView(R.layout.unloged_dialog, true)
                .title(R.string.unloged_dialog_title)
                .positiveText("Закрыть");
        final MaterialDialog dialog = builder.build();
        View customView = dialog.getCustomView();
        if (customView != null)
        {
            View auth = customView.findViewById(R.id.login);
            auth.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    dialog.dismiss();
                    VKSdk.login((AppCompatActivity) ctx);
                }
            });
            TextView info = (TextView) dialog.getCustomView().findViewById(R.id.info);
            info.setText(messege);
        }
        dialog.show();
    }

    public static void printCertificateFingerprint(AppCompatActivity act)
    {
        String[] fingerprints = VKUtil.getCertificateFingerprint(act, act.getPackageName());
        for(String fingerprint:fingerprints)
        {
            Log.d(LOG, "fingerprint: " + fingerprint);
        }
    }

    public static void checkVKAuth(final AppCompatActivity activity, final NavigationView navigationView)
    {
        if (VKSdk.isLoggedIn())
        {
            /*Удаление банера*/
            View banner = activity.findViewById(R.id.adView);
//            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) banner.getLayoutParams();
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) banner.getLayoutParams();
            params.height = 0;
            banner.setLayoutParams(params);
            for (int i = 0; i < navigationView.getHeaderCount(); i++)
            {
                navigationView.removeHeaderView(navigationView.getHeaderView(i));
            }
            View headerlogined = LayoutInflater.from(activity).inflate(R.layout.drawer_header_logined, navigationView, false);
            navigationView.addHeaderView(headerlogined);
            final TextView name = (TextView) headerlogined.findViewById(R.id.vk_name);
            final ImageView avatar = (ImageView) headerlogined.findViewById(R.id.vk_avatar);
            VKApi.users().get(VKParameters.from(VKApiConst.FIELDS, "photo_200")).executeWithListener(new VKRequest.VKRequestListener()
            {
                @Override
                public void onComplete(VKResponse response)
                {
                    VKApiUser user = ((VKList<VKApiUser>) response.parsedModel).get(0);
                    String fullName = user.first_name + " " + user.last_name;
                    name.setText(fullName);
                    SingltonUIL.getInstance().displayImage(user.photo_200, avatar, SingltonUIL.getRoundVKAvatarOptions(activity));
                    SharedPreferences prefVK = activity.getSharedPreferences(activity.getString(R.string.pref_vk), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefVK.edit();
                    editor.putString(activity.getString(R.string.pref_vk_name), user.first_name);
                    editor.putString(activity.getString(R.string.pref_vk_surname), user.last_name);
                    editor.putString(activity.getString(R.string.pref_vk_avatar), user.photo_200);
                    editor.apply();
                }

                @Override
                public void onError(VKError error)
                {
                    super.onError(error);
                    SharedPreferences prefVK = activity.getSharedPreferences(activity.getString(R.string.pref_vk), Context.MODE_PRIVATE);
                    String firstName = prefVK.getString(activity.getString(R.string.pref_vk_name), "");
                    String lastName = prefVK.getString(activity.getString(R.string.pref_vk_surname), "");
                    String avatarUrl = prefVK.getString(activity.getString(R.string.pref_vk_avatar), "");
                    String fullName = firstName + " " + lastName;
                    name.setText(fullName);
                    SingltonUIL.getInstance().displayImage(avatarUrl, avatar, SingltonUIL.getRoundVKAvatarOptions(activity));
                }
            });

            ImageView logout = (ImageView) headerlogined.findViewById(R.id.logout);
            logout.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    VKSdk.logout();
                    checkVKAuth(activity, navigationView);
                }
            });
        }
        else
        {
            View banner = activity.findViewById(R.id.adView);
//            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) banner.getLayoutParams();
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) banner.getLayoutParams();
            params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            for (int i = 0; i < navigationView.getHeaderCount(); i++)
            {
                navigationView.removeHeaderView(navigationView.getHeaderView(i));
            }
            View headerUnlogined = LayoutInflater.from(activity).inflate(R.layout.drawer_header_unlogined, navigationView, false);
            navigationView.addHeaderView(headerUnlogined);
            TextView login = (TextView) headerUnlogined.findViewById(R.id.login);
            login.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    VKSdk.login(activity);
                }
            });
            ImageView info = (ImageView) headerUnlogined.findViewById(R.id.info);
            info.setOnClickListener(new View.OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    new MaterialDialog.Builder(activity)
                            .content(R.string.login_advantages)
                            .title("Преимущества авторизации")
                            .positiveText("Ok")
                            .show();
                }
            });
        }
    }
}
