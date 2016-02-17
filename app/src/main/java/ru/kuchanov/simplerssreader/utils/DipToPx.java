package ru.kuchanov.simplerssreader.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Created by Юрий on 17.02.2016 16:10.
 * For SimpleRSSReader.
 */
public class DipToPx
{
    /**
     *
     * @param dip
     * @param ctx
     * @return convert given dip to px
     */
    public static float convert(int dip, Context ctx)
    {
        //convert given pid to px
        Resources r = ctx.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
        return px;
    }
}
