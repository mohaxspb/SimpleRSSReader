package ru.kuchanov.simplerssreader.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ru.kuchanov.simplerssreader.R;
import ru.kuchanov.simplerssreader.db.Article;


/**
 * Created by Юрий on 16.02.2016 17:47.
 * For SimpleRSSReader.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder>
{
    public static final String LOG = RecyclerAdapter.class.getSimpleName();

    private ArrayList<Article> articles;

    public RecyclerAdapter(ArrayList<Article> dataset)
    {
        articles = dataset;
    }

    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_article_card, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        holder.mTextView.setText(articles.get(position).getTitle());
    }

    @Override
    public int getItemCount()
    {
        return articles.size();
    }

    public void notifyRemoveEach()
    {
        for (int i = 0; i < articles.size(); i++)
        {
            notifyItemRemoved(i);
        }
    }

    public void notifyAddEach()
    {
        for (int i = 0; i < articles.size(); i++)
        {
            notifyItemInserted(i);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView mTextView;

        public ViewHolder(View v)
        {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.title);
        }
    }
}