package ru.kuchanov.simplerssreader.utils;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.text.style.QuoteSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;

import ru.kuchanov.simplerssreader.R;

/**
 * Created by Юрий on 17.02.2016 16:23.
 * For SimpleRSSReader.
 */
public class MakeLinksClicable
{
    private final static String LOG = MakeLinksClicable.class.getSimpleName();

    /**
     * makes inks clickable. Also allows to style quote spans
     */
    public static SpannableStringBuilder reformatText(Context ctx, CharSequence text)
    {
        int end = text.length();
        Spannable sp = (Spannable) text;

        //restyling quotes
        replaceQuoteSpans(ctx, sp);

        URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
        SpannableStringBuilder style = new SpannableStringBuilder(text);
        for (URLSpan url : urls)
        {
            style.removeSpan(url);
            SpanTextClick click = new SpanTextClick(url.getURL());
            style.setSpan(click, sp.getSpanStart(url), sp.getSpanEnd(url),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return style;
    }

    //quotes
    //see http://stackoverflow.com/a/29114976/3212712
    private static void replaceQuoteSpans(Context ctx, Spannable spannable)
    {
        int colorBackground = AttributeGetter.getColor(ctx, R.attr.windowBackgroundDark);
        int colorStripe = AttributeGetter.getColor(ctx, R.attr.colorAccent);

        QuoteSpan[] quoteSpans = spannable.getSpans(0, spannable.length(), QuoteSpan.class);

        for (QuoteSpan quoteSpan : quoteSpans)
        {
            int start = spannable.getSpanStart(quoteSpan);
            int end = spannable.getSpanEnd(quoteSpan);
            int flags = spannable.getSpanFlags(quoteSpan);
            spannable.removeSpan(quoteSpan);
            spannable.setSpan(new CustomQuoteSpan(
                            colorBackground,
                            colorStripe,
                            5,
                            10),
                    start,
                    end,
                    flags);
        }
    }

    public static class SpanTextClick extends ClickableSpan
    {
        String url;

        public SpanTextClick(String url)
        {
            this.url = url;
        }

        @Override
        public void onClick(View widget)
        {
            Log.i(LOG, "url clicked: " + this.url);
        }
    }
}