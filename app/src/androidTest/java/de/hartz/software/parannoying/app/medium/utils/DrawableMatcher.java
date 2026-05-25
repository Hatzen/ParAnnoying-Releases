package de.hartz.software.parannoying.app.medium.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.ImageView;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

// https://stackoverflow.com/a/35222984/8524651
public class DrawableMatcher extends TypeSafeMatcher<View> {

    private Bitmap previous = null;

    public DrawableMatcher() {
        super(View.class);
    }

    @Override
    protected boolean matchesSafely(View target) {
        if (!(target instanceof ImageView)){
            return false;
        }
        ImageView imageView = (ImageView) target;

        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        Bitmap tmp = previous;
        previous = bitmap;
        return bitmap.sameAs(tmp);
    }


    @Override
    public void describeTo(Description description) {
        description.appendText("with drawable from resource id: ");
    }
}