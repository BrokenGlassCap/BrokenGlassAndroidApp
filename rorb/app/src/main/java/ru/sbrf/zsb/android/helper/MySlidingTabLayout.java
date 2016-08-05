package ru.sbrf.zsb.android.helper;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

import ru.sbrf.zsb.android.rorb.R;

/**
 * Created by Администратор on 05.08.2016.
 */
public class MySlidingTabLayout extends SlidingTabLayout {
    private static final int TAB_VIEW_PADDING_DIPS = 16;
    private static final int TAB_VIEW_TEXT_SIZE_SP = 12;



    public MySlidingTabLayout(Context context) {
        super(context);
    }

    public MySlidingTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MySlidingTabLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected TextView createDefaultTabView(Context context) {
        TextView textView = super.createDefaultTabView(context);
        return textView;
    }
}
