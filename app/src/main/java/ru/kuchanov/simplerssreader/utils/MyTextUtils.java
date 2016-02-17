package ru.kuchanov.simplerssreader.utils;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

/**
 * Created by Юрий on 17.02.2016 16:16.
 * For SimpleRSSReader.
 */
public class MyTextUtils
{
    /**
     * sets text to TextView via Html.fromHtml <br>
     * also makes lins clickable and sets a listener for clicks <br>
     * also ads imageLoader for images inside text <br>
     * also adds supporting for list tags
     */
    public static void setTextToTextView(TextView textView, String textToSet)
    {
//        Log.i(LOG, textToSet);
        Context ctx = textView.getContext();

        textView.setLinksClickable(true);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        Spanned spannable = Html.fromHtml(textToSet, new UILImageGetter(textView, ctx), new MyHtmlTagHandler(ctx));
        MakeLinksClicable.reformatText(ctx, spannable);

        textView.setText(spannable);
    }
}
