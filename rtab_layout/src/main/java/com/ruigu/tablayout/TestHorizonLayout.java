package com.ruigu.tablayout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.HorizontalScrollView;

public class TestHorizonLayout extends HorizontalScrollView {
    public TestHorizonLayout(Context context) {
        super(context);
    }

    public TestHorizonLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TestHorizonLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TestHorizonLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        switch (MeasureSpec.getMode(widthMeasureSpec)) {
            case MeasureSpec.AT_MOST:
                Log.e("", "1");
                break;
            case MeasureSpec.UNSPECIFIED:
                Log.e("", "2");
                break;
            case MeasureSpec.EXACTLY:
                Log.e("", "3");
                break;
        }

        switch (MeasureSpec.getMode(heightMeasureSpec)) {
            case MeasureSpec.AT_MOST:
                Log.e("", "1");
                break;
            case MeasureSpec.UNSPECIFIED:
                Log.e("", "2");
                break;
            case MeasureSpec.EXACTLY:
                Log.e("", "3");
                break;
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }
}
