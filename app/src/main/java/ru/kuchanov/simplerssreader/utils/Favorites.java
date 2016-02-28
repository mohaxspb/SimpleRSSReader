package ru.kuchanov.simplerssreader.utils;

import android.util.Log;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Юрий on 28.02.2016 17:07.
 * For SimpleRSSReader.
 */
public class Favorites
{
    public final static String LOG = Favorites.class.getSimpleName();

    public static void uploadFavorites()
    {
        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                String answer = null;

                String url = "http://kuchanov.ru/scp/upload.php";

                Request.Builder request = new Request.Builder();

                String catsFavs = "авлоалво";
                String artsFavs = "врылвра";
                RequestBody formBody = new FormBody.Builder()
                        .add("vk_id", "219694783")
                        .add("urls", catsFavs)
                        .add("titles", artsFavs)
                        .build();

                request.post(formBody);
                request.addHeader("Content-Type", "text/json; Charset=UTF-8");

                request.url(url);
                try
                {
                    OkHttpClient client = new OkHttpClient();
                    Response response = client.newCall(request.build()).execute();
                    answer = response.body().string();
                    Log.d(LOG, answer);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        };

        thread.start();
    }
}
