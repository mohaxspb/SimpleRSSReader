package ru.kuchanov.simplerssreader.utils.customization;


import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Юрий on 12.02.2016 18:13.
 * For SimpleRSSReader.
 */
public class SpacesItemDecoration extends RecyclerView.ItemDecoration
{
    private int space;

    public SpacesItemDecoration(int space)
    {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
    {
        outRect.bottom = space;
    }
}