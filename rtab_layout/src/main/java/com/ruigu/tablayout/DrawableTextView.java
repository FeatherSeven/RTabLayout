package com.ruigu.tablayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.core.content.ContextCompat;

public class DrawableTextView extends androidx.appcompat.widget.AppCompatTextView {

    private int iconGravity;
    private int iconWidth;
    private int iconHeight;
    private int iconMarginToText;
    private Drawable drawable;

    public static final int ICON_GRAVITY_TOP = 0;
    public static final int ICON_GRAVITY_BOTTOM = 1;
    public static final int ICON_GRAVITY_LEFT = 2;
    public static final int ICON_GRAVITY_RIGHT = 3;

    public DrawableTextView(Context context) {
        this(context, null);
    }

    public DrawableTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        obtainAttributes(context, attrs);
        refreshDrawable();
    }

    private void obtainAttributes(Context context, AttributeSet attrs) {
        TypedArray typeArray = context.obtainStyledAttributes(attrs, R.styleable.DrawableTextView);

        iconGravity = typeArray.getInt(R.styleable.DrawableTextView_textIconGravity, ICON_GRAVITY_TOP);
        int iconSize = typeArray.getDimensionPixelSize(R.styleable.DrawableTextView_iconSize, dp2px(20));
        iconWidth = typeArray.getDimensionPixelSize(R.styleable.DrawableTextView_iconWidth, iconSize);
        iconHeight = typeArray.getDimensionPixelSize(R.styleable.DrawableTextView_iconHeight, iconWidth);
        iconMarginToText = typeArray.getDimensionPixelSize(R.styleable.DrawableTextView_iconMarginToText, 0);
        drawable = typeArray.getDrawable(R.styleable.DrawableTextView_iconSrc);
        typeArray.recycle();
    }

    public void setIcon(int iconRes) {
        drawable = ContextCompat.getDrawable(getContext(), iconRes);
        refreshDrawable();
    }

    public void setIcon(Drawable drawable) {
        this.drawable = drawable;
        refreshDrawable();
    }

    public void setIconGravity(int gravity) {
        iconGravity = gravity;
        refreshDrawable();
    }

    public void setIconSize(int size) {
        iconHeight = iconWidth = size;
        refreshDrawable();
    }

    public void setIconSize(int height, int width) {
        iconHeight = height;
        iconWidth = width;
        refreshDrawable();
    }

    public void setIconMarginToText(int margin) {
        iconMarginToText = margin;
        refreshDrawable();
    }

    private void refreshDrawable() {
        if (drawable != null) {
            drawable.setBounds(0, 0, iconWidth, iconHeight);
            switch (iconGravity) {
                case 0:
                    setCompoundDrawables(null, drawable, null, null);
                    break;
                case 1:
                    setCompoundDrawables(null, null, null, drawable);
                    break;
                case 2:
                    setCompoundDrawables(drawable, null, null, null);
                    break;
                case 3:
                    setCompoundDrawables(null, null, drawable, null);
                    break;
            }
            setCompoundDrawablePadding(iconMarginToText);
        } else
            setCompoundDrawables(null, null, null, null);
    }

    protected int dp2px(float dp) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public boolean hasIcon() {
        return drawable != null;
    }
}
