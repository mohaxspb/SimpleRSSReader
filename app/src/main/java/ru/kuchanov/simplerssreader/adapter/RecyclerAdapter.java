package ru.kuchanov.simplerssreader.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import ru.kuchanov.simplerssreader.R;
import ru.kuchanov.simplerssreader.activity.ActivityArticle;
import ru.kuchanov.simplerssreader.db.Article;
import ru.kuchanov.simplerssreader.fragment.FragmentArticlesList;
import ru.kuchanov.simplerssreader.utils.AttributeGetter;
import ru.kuchanov.simplerssreader.utils.Const;
import ru.kuchanov.simplerssreader.utils.DipToPx;
import ru.kuchanov.simplerssreader.utils.MyTextUtils;
import ru.kuchanov.simplerssreader.utils.SingltonUIL;

/**
 * Created by Юрий on 16.02.2016 17:47.
 * For SimpleRSSReader.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder>
{
    public static final String LOG = RecyclerAdapter.class.getSimpleName();

    private ArrayList<Article> articles;
    private String rssUrl;

    public RecyclerAdapter(ArrayList<Article> dataset, String rssUrl)
    {
        articles = dataset;
        this.rssUrl = rssUrl;
    }

    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_article_card, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position)
    {
//        holder.title.setText(articles.get(position).getTitle());
        final Context ctx = holder.cardView.getContext();
        final Article curArt = articles.get(position);

        int textSizeLarge;
        int textSizePrimary;
        int textSizeSecondary;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        float uiTextScale = pref.getFloat(ctx.getString(R.string.pref_design_key_text_size_ui), 0.75f);
        textSizeLarge = ctx.getResources().getDimensionPixelSize(R.dimen.text_size_large);
        textSizePrimary = ctx.getResources().getDimensionPixelSize(R.dimen.text_size_primary);
        textSizeSecondary = ctx.getResources().getDimensionPixelSize(R.dimen.text_size_secondary);

        holder.title.setTextSize(TypedValue.COMPLEX_UNIT_PX, uiTextScale * textSizeLarge);
        holder.preview.setTextSize(TypedValue.COMPLEX_UNIT_PX, uiTextScale * textSizePrimary);
        holder.date.setTextSize(TypedValue.COMPLEX_UNIT_PX, uiTextScale * textSizeSecondary);

        //image
        final LinearLayout.LayoutParams paramsImage = (LinearLayout.LayoutParams) holder.image.getLayoutParams();
        if (curArt.getImageUrls() != null)
        {
            String imageUrlFirst = curArt.getImageUrls().split(Const.DIVIDER)[0];
//            Log.d(LOG, "curArt.getImageUrls(): " + curArt.getImageUrls());
//            Log.d(LOG, "imageUrlFirst: " + imageUrlFirst);

            //set loading indicator
            holder.image.setScaleType(ImageView.ScaleType.CENTER);
            paramsImage.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            holder.image.setLayoutParams(paramsImage);
            int renewIconAdress = AttributeGetter.getDrawableId(ctx, R.attr.renewIcon);
            holder.image.setImageResource(renewIconAdress);

            SingltonUIL.getInstance().displayImage(imageUrlFirst, holder.image, new SimpleImageLoadingListener()
            {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
                {
                    super.onLoadingComplete(imageUri, view, loadedImage);

                    float width = ctx.getResources().getDisplayMetrics().widthPixels;

                    //minusing paddings
                    float topLevelMargin = ctx.getResources().getDimension(R.dimen.recycler_top_item_margin);
                    width -= DipToPx.convert((int) (topLevelMargin * 2), ctx);

                    float scale = width / loadedImage.getWidth();
                    float height = (scale) * loadedImage.getHeight();

                    paramsImage.height = (int) height;

                    holder.image.setLayoutParams(paramsImage);
                }
            });
        }
        else
        {
            holder.image.setImageDrawable(null);
            paramsImage.height = 0;
            holder.image.setLayoutParams(paramsImage);
        }
        //title
        MyTextUtils.setTextToTextView(holder.title, curArt.getTitle());
        //date
        DateFormat dateFormat = new SimpleDateFormat("HH:mm, d MMM yyyy", Locale.getDefault());
        String date = dateFormat.format(curArt.getPubDate());
        holder.date.setText(date);
        //preview
        MyTextUtils.setTextToTextView(holder.preview, curArt.getPreview());

        //start Article Activity
        holder.rootView.setOnClickListener(new StartNewArticleOnClickListener(articles, position, rssUrl));
        holder.title.setOnClickListener(new StartNewArticleOnClickListener(articles, position, rssUrl));
    }

    @Override
    public int getItemCount()
    {
        return articles.size();
    }

    private static class StartNewArticleOnClickListener implements View.OnClickListener
    {
        private ArrayList<Article> articles;
        private int position;
        private String rssUrl;

        public StartNewArticleOnClickListener(ArrayList<Article> articles, int position, String rssUrl)
        {
            this.articles = articles;
            this.position = position;
            this.rssUrl = rssUrl;
        }

        @Override
        public void onClick(View v)
        {
            Log.d(LOG, articles.get(position).getTitle() + " clicked!");
            Context ctx = v.getContext();
            Intent intent = new Intent(ctx, ActivityArticle.class);
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(Article.LOG, articles);
            bundle.putString(FragmentArticlesList.KEY_RSS_URL, rssUrl);
            intent.putExtras(bundle);
            ctx.startActivity(intent);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public CardView cardView;
        public LinearLayout rootView;
        public TextView title;
        public TextView date;
        public TextView preview;
        public ImageView image;

        public ViewHolder(View v)
        {
            super(v);
            cardView = (CardView) v;
            rootView = (LinearLayout) v.findViewById(R.id.art_card_main_lin);
            title = (TextView) v.findViewById(R.id.title);
            date = (TextView) v.findViewById(R.id.date);
            preview = (TextView) v.findViewById(R.id.preview);
            image = (ImageView) v.findViewById(R.id.image);
        }
    }
}