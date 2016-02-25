package ru.kuchanov.simplerssreader.utils.anim;

import android.animation.Animator;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

import ru.kuchanov.simplerssreader.R;
import ru.kuchanov.simplerssreader.utils.SingltonUIL;

/**
 * Created by Юрий on 25.02.2016 22:29.
 * For SimpleRSSReader.
 */
public class MyAnimationUtils
{
    public static void changeImageWithAlphaAnimation(final View coverThatChangesAlpha, final ImageView toolbarImage, final String imageUrl)
    {
        coverThatChangesAlpha.animate().alpha(1).setDuration(600).setListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
                coverThatChangesAlpha.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                if (imageUrl != null)
                {
                    SingltonUIL.getInstance().displayImage(imageUrl, toolbarImage);//, DisplayImageOptions.createSimple());
                }
                else
                {
                    toolbarImage.setImageResource(R.drawable.ic_rss_feed_blue_grey_500_48dp);
                }

                coverThatChangesAlpha.animate().alpha(0).setDuration(600).setListener(new Animator.AnimatorListener()
                {
                    @Override
                    public void onAnimationStart(Animator animation)
                    {
//                        myView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation)
                    {
                        coverThatChangesAlpha.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation)
                    {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation)
                    {

                    }
                });

            }

            @Override
            public void onAnimationCancel(Animator animation)
            {

            }

            @Override
            public void onAnimationRepeat(Animator animation)
            {

            }
        });
    }
}
