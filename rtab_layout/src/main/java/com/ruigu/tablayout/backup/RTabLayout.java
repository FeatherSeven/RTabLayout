/*
package com.ruigu.tablayout.backup;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.ruigu.tablayout.MsgView;
import com.ruigu.tablayout.R;
import com.ruigu.tablayout.TabEntity;

import java.util.ArrayList;

public class RTabLayout extends com.google.android.material.tabs.TabLayout {

    private Context mContext;


    private int animDuration;
    //决定segment是否有动画，有动画则四角都有圆角，动画效果滚动，无则仅两端有圆角，内部边框皆为矩形
    //因为无法从子类修改Tablayout的动画，故不能通过自定义属性来设定，只能通过TabLayout原生的动画时间来控制这个选项
    */
/*Segment部分*//*

    private boolean isSegment;
    private int segmentStrokeColor;
    private int segmentStrokeWidth;
    private boolean segmentRadiusHalfHeight;
    private int segmentRadius;
    private int segmentBackgroundColor;
    private int tabIndicatorColor;
    private int tabIndicatorHeight;
    private int iconGravity;
    private int iconWidth;
    private int iconHeight;
    private int iconMarginToText;
    private float msgTextSize;
    private int msgTextColor;
    private int msgBackGroundColor;
    private int msgCornerRadius;
    private int msgDotRadius;
    private int marginTopOfContent;
    private int marginTopOfMsg;
    private int indentationOfMsg;
    */
/**
     * 用于绘制Indicator
     *//*

    private GradientDrawable mIndicatorDrawable = new GradientDrawable();
    GradientDrawable mBackgroundDrawable = new GradientDrawable();
    private float[] mRadiusArr = new float[8];


    public RTabLayout(Context context) {
        this(context, null);
    }

    public RTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;
        TypedArray typeArray = context.obtainStyledAttributes(attrs, R.styleable.RTabLayout, defStyleAttr, R.style.Widget_Design_TabLayout);

        this.animDuration = typeArray.getInt(R.styleable.TabLayout_tabIndicatorAnimationDuration, 300);
        tabIndicatorHeight = typeArray.getDimensionPixelSize(R.styleable.TabLayout_tabIndicatorHeight, -1);
        tabIndicatorColor = typeArray.getColor(R.styleable.RTabLayout_tabIndicatorColor, 0);


        this.iconGravity = typeArray.getInt(R.styleable.RTabLayout_tabIconGravity, 0);
        this.iconWidth = typeArray.getDimensionPixelSize(R.styleable.RTabLayout_iconWidth, -1);
        this.iconHeight = typeArray.getDimensionPixelSize(R.styleable.RTabLayout_iconHeight, -1);
        this.iconMarginToText = typeArray.getDimensionPixelSize(R.styleable.RTabLayout_iconMarginToText, 0);

        this.msgTextSize = (float) typeArray.getDimensionPixelSize(R.styleable.RTabLayout_msgTextSize, sp2px(11));
        this.msgTextColor = typeArray.getColor(R.styleable.RTabLayout_msgTextColor, 0);
        this.msgBackGroundColor = typeArray.getColor(R.styleable.RTabLayout_msgBackGroundColor, 0);
        this.msgCornerRadius = typeArray.getDimensionPixelSize(R.styleable.RTabLayout_msgCornerRadius, -1);
        this.msgDotRadius = typeArray.getDimensionPixelSize(R.styleable.RTabLayout_msgDotRadius, dp2px(3));
        this.marginTopOfContent = typeArray.getDimensionPixelSize(R.styleable.RTabLayout_marginTopOfContent, 0);
        this.marginTopOfMsg = typeArray.getDimensionPixelSize(R.styleable.RTabLayout_marginTopOfMsg, 0);
        this.indentationOfMsg = typeArray.getDimensionPixelSize(R.styleable.RTabLayout_msgIndentation, dp2px(4));


        this.isSegment = typeArray.getBoolean(R.styleable.RTabLayout_isSegment, false);
        if (isSegment) {//这些属性如果不是segment就没必要使用了
            this.segmentBackgroundColor = typeArray.getColor(R.styleable.RTabLayout_segmentBackgroundColor, getResources().getColor(R.color.white));
            this.segmentStrokeColor = typeArray.getColor(R.styleable.RTabLayout_segmentStrokeColor, tabIndicatorColor);//边框默认用indicator填充色
            this.segmentStrokeWidth = typeArray.getDimensionPixelSize(R.styleable.RTabLayout_segmentStrokeWidth, dp2px(1));
            this.segmentRadiusHalfHeight = typeArray.getBoolean(R.styleable.RTabLayout_segmentRadiusHalfHeight, true);
            this.segmentRadius = typeArray.getDimensionPixelSize(R.styleable.RTabLayout_segmentRadius, -1);
        }
        typeArray.recycle();

        setTabRippleColor(null);//去除原生TabLayout水波纹效果


        addOnTabSelectedListener(new OnTabSelectedListener() {
            @Override
            public void onTabSelected(Tab tab) {
                drawIndicator();
//                hideMsg(tab.getPosition());逻辑上不应该自动去除红点
            }

            @Override
            public void onTabUnselected(Tab tab) {

            }

            @Override
            public void onTabReselected(Tab tab) {

            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        if (isSegment && getBackground() == null) {
            if (tabIndicatorHeight == -1)
                tabIndicatorHeight = getHeight();

            */
/*segment background*//*

            if (segmentRadiusHalfHeight || segmentRadius < 0 || segmentRadius > tabIndicatorHeight / 2)
                segmentRadius = tabIndicatorHeight / 2;
            mBackgroundDrawable.setCornerRadius(segmentRadius);
            mBackgroundDrawable.setStroke(segmentStrokeWidth, segmentStrokeColor);
            mBackgroundDrawable.setColor(segmentBackgroundColor);
            setBackground(mBackgroundDrawable);

            setSelectedTabIndicatorHeight(tabIndicatorHeight);
            drawIndicator();
        }


    }

    private void drawIndicator() {
        */
/*indicator*//*

        if (isSegment)
            setSelectedTabIndicatorGravity(INDICATOR_GRAVITY_CENTER);
        mIndicatorDrawable.setColor(tabIndicatorColor);
        calIndicator();
        mIndicatorDrawable.setCornerRadii(mRadiusArr);
        setSelectedTabIndicator(mIndicatorDrawable);
    }


    private void calIndicator() {
        if (animDuration <= 0) {
            if (getSelectedTabPosition() == 0) {
                */
/**The corners are ordered top-left, top-right, bottom-right, bottom-left*//*

                mRadiusArr[0] = segmentRadius;
                mRadiusArr[1] = segmentRadius;
                mRadiusArr[2] = 0;
                mRadiusArr[3] = 0;
                mRadiusArr[4] = 0;
                mRadiusArr[5] = 0;
                mRadiusArr[6] = segmentRadius;
                mRadiusArr[7] = segmentRadius;
            } else if (getSelectedTabPosition() == getTabCount() - 1) {
                */
/**The corners are ordered top-left, top-right, bottom-right, bottom-left*//*

                mRadiusArr[0] = 0;
                mRadiusArr[1] = 0;
                mRadiusArr[2] = segmentRadius;
                mRadiusArr[3] = segmentRadius;
                mRadiusArr[4] = segmentRadius;
                mRadiusArr[5] = segmentRadius;
                mRadiusArr[6] = 0;
                mRadiusArr[7] = 0;
            } else {
                */
/**The corners are ordered top-left, top-right, bottom-right, bottom-left*//*

                mRadiusArr[0] = 0;
                mRadiusArr[1] = 0;
                mRadiusArr[2] = 0;
                mRadiusArr[3] = 0;
                mRadiusArr[4] = 0;
                mRadiusArr[5] = 0;
                mRadiusArr[6] = 0;
                mRadiusArr[7] = 0;
            }
        } else {
            */
/**The corners are ordered top-left, top-right, bottom-right, bottom-left*//*

            mRadiusArr[0] = segmentRadius;
            mRadiusArr[1] = segmentRadius;
            mRadiusArr[2] = segmentRadius;
            mRadiusArr[3] = segmentRadius;
            mRadiusArr[4] = segmentRadius;
            mRadiusArr[5] = segmentRadius;
            mRadiusArr[6] = segmentRadius;
            mRadiusArr[7] = segmentRadius;
        }
    }

    public void setData(String[] titleList) {
        ArrayList<TabEntity> dataList = new ArrayList<>();
        if (titleList != null) {
            for (String s : titleList) {
                dataList.add(new TabEntity(s));
            }
            setData(dataList);
        }
    }

    public MsgView getMsgViewAt(int position) {
        if (position < 0 || position >= getTabCount())
            return null;
        MsgView msgView = getTabAt(position).getCustomView().findViewById(R.id.mv_msg);
        return msgView;
    }

    public void hideMsg(int position) {
        if (position < 0 || position >= getTabCount())
            return;
        MsgView msgView = getTabAt(position).getCustomView().findViewById(R.id.mv_msg);
        msgView.setVisibility(GONE);
    }

    public void showDot(int position) {
        showMsg(position, 0, null);
    }

    public void showMsg(int position, int num) {
        showMsg(position, num, null);
    }

    public void showMsg(int position, String str) {
        showMsg(position, -1, str);
    }

    private void showMsg(int position, int num, @Nullable String msg) {
        if (position < 0 || position >= getTabCount())
            return;
        View customView = getTabAt(position).getCustomView();
        MsgView msgView = customView.findViewById(R.id.mv_msg);

        if (msgTextSize != 0) msgView.setTextSize(0, msgTextSize);
        if (msgTextColor != 0) msgView.setTextColor(msgTextColor);
        if (msgBackGroundColor != 0) msgView.setBackgroundColor(msgBackGroundColor);
        if (msgCornerRadius != 0) msgView.setCornerRadius(msgCornerRadius);

        LinearLayout ll_main_rtab = customView.findViewById(R.id.ll_main_rtab);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) ll_main_rtab.getLayoutParams();
        layoutParams.leftMargin = showMsg(msgView, num, msg);
        ll_main_rtab.setLayoutParams(layoutParams);

    }


    public void setData(ArrayList<TabEntity> dataList) {
        if (dataList != null) {
            for (TabEntity tabEntity : dataList) {
                Tab tab = newTab();
                switch (iconGravity) {
                    case 1:
                        tab.setCustomView(R.layout.tab_icon_bottom);
                        break;
                    case 2:
                        tab.setCustomView(R.layout.tab_icon_left);
                        break;
                    case 3:
                        tab.setCustomView(R.layout.tab_icon_right);
                        break;
                    case 0:
                    default:
                        tab.setCustomView(R.layout.tab_icon_top);
                        break;

                }
                TextView tv_rtab = tab.getCustomView().findViewById(R.id.tv_rtab);
                tv_rtab.setText(tabEntity.getText());
                tv_rtab.setTextColor(getTabTextColors());
                if (tabEntity.getIcon() != 0) {
                    ImageView imageView = tab.getCustomView().findViewById(R.id.iv_rtab);
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) imageView.getLayoutParams();
                    layoutParams.height = iconHeight < 0 ? RelativeLayout.LayoutParams.WRAP_CONTENT : iconHeight;
                    layoutParams.width = iconWidth < 0 ? RelativeLayout.LayoutParams.WRAP_CONTENT : iconWidth;
                    if (iconMarginToText != 0) {
                        switch (iconGravity) {
                            case 1:
                                layoutParams.topMargin = iconMarginToText;
                                break;
                            case 2:
                                layoutParams.rightMargin = iconMarginToText;
                                break;
                            case 3:
                                layoutParams.leftMargin = iconMarginToText;
                                break;
                            case 0:
                            default:
                                layoutParams.bottomMargin = iconMarginToText;
                                break;
                        }
                    }
                    StateListDrawable drawable = new StateListDrawable();
                    if (tabEntity.getSelectedIcon() != 0) {
                        Drawable selected = mContext.getResources().getDrawable(tabEntity.getSelectedIcon());
                        drawable.addState(new int[]{android.R.attr.state_selected}, selected);
                    }
                    Drawable normal = mContext.getResources().getDrawable(tabEntity.getIcon());
                    drawable.addState(new int[]{-android.R.attr.state_selected}, normal);
                    imageView.setImageDrawable(drawable);
                    imageView.setVisibility(VISIBLE);
                }
                if (tabEntity.getTag() != null)
                    tab.setTag(tabEntity.getTag());
                LinearLayout ll_main_rtab = tab.getCustomView().findViewById(R.id.ll_main_rtab);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) ll_main_rtab.getLayoutParams();
                if (marginTopOfContent != 0) {
                    layoutParams.topMargin = marginTopOfContent;
                }
                addTab(tab);

            }
            if (getLayoutParams().height == -2)
                getLayoutParams().height = getHeight(getTabAt(0).view);
        }

    }

    protected int dp2px(float dp) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    protected int sp2px(float sp) {
        final float scale = this.mContext.getResources().getDisplayMetrics().scaledDensity;
        return (int) (sp * scale + 0.5f);
    }


    private int showMsg(MsgView msgView, int num, String msg) {
        if (msgView == null) {
            return -1;
        }
        int balanceMarginLeft;
        int edge = dp2px(4);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) msgView.getLayoutParams();
        if (msg != null) {
            msgView.setPadding(edge, 0, edge, 0);
            msgView.setText(msg);
            layoutParams.leftMargin = -indentationOfMsg;
            balanceMarginLeft = getWidth(msgView) - indentationOfMsg;
        } else if (num == 0) {//圆点,设置默认宽高
            msgView.setStrokeWidth(0);
            msgView.setText("");
            setSize(msgView, msgDotRadius * 2);
            balanceMarginLeft = msgDotRadius * 2;
        } else {
            if (num < 100) {//圆角矩形,圆角是高度的一半,设置默认padding
                msgView.setPadding(edge, 0, edge, 0);
                msgView.setText(String.valueOf(num));
            } else {//数字超过两位,显示99+
                msgView.setPadding(edge, 0, edge, 0);
                msgView.setText("99+");
            }
            balanceMarginLeft = getWidth(msgView) - indentationOfMsg;
            layoutParams.leftMargin = -indentationOfMsg;
        }
        layoutParams.topMargin = marginTopOfMsg;
        msgView.setVisibility(View.VISIBLE);
        return balanceMarginLeft;
    }


    //此法对于TextView这类只能获取内容宽高，不能获取整体
//    实际自测效果与上述查得结论矛盾，获取的宽度也包括paddingLeft和paddingRight
    private int getWidth(View view) {
        view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        return view.getMeasuredWidth();
    }

    private int getHeight(View view) {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        return view.getMeasuredHeight();
    }

    public void setSize(MsgView rtv, int dp) {
        if (rtv == null) {
            return;
        }
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) rtv.getLayoutParams();
        lp.width = dp;
        lp.height = dp;
        rtv.setLayoutParams(lp);
    }
}
*/
