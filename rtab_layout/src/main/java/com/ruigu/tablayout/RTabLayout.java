package com.ruigu.tablayout;


import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build.VERSION;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.util.Pools.Pool;
import androidx.core.util.Pools.SimplePool;
import androidx.core.util.Pools.SynchronizedPool;
import androidx.core.view.PointerIconCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.TextViewCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.DecorView;
import androidx.viewpager.widget.ViewPager.OnAdapterChangeListener;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

import com.google.android.material.animation.AnimationUtils;

import java.lang.ref.WeakReference;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;

import static android.util.TypedValue.COMPLEX_UNIT_PX;

@DecorView
public class RTabLayout extends HorizontalScrollView {
    private static final Pool<Tab> tabPool = new SynchronizedPool(16);

    private final ArrayList<Tab> tabs;
    private Tab selectedTab;
    private final RectF tabViewContentBounds;
    private final SlidingTabIndicator slidingTabIndicator;
    int tabPaddingStart;
    int tabPaddingTop;
    int tabPaddingEnd;
    int tabPaddingBottom;
    int tabTextAppearance;
    ColorStateList tabTextColors;

    //绘制相关
//    int specWidthMode;

    float tabTextSize;
    boolean isSelectedBold;
    private final int requestedTabMinWidth;
    private final int requestedTabMaxWidth;
    boolean rtabTabAverageWidth;
    int tabIndicatorAnimationDuration;

    boolean inlineLabel;
    boolean tabIndicatorFullWidth;
    private BaseOnTabSelectedListener selectedListener;
    private final ArrayList<BaseOnTabSelectedListener> selectedListeners;
    private BaseOnTabSelectedListener currentVpSelectedListener;
    private ValueAnimator scrollAnimator;
    ViewPager viewPager;
    private PagerAdapter pagerAdapter;
    private DataSetObserver pagerAdapterObserver;
    private TabLayoutOnPageChangeListener pageChangeListener;
    private AdapterChangeListener adapterChangeListener;
    private boolean setupViewPagerImplicitly;
    private final Pool<TabView> tabViewPool;

    //icon相关
    private int iconGravity;
    private int iconSize;
    private int iconWidth;
    private int iconHeight;
    private int iconMarginToText;
    /**
     * bottom0 center1 top2 stretch3
     */
    private int tabIndicatorGravity;

    private Drawable tabSelectedIndicator;
    private int tabIndicatorColor;
    private int tabIndicatorRadius;
    private int tabIndicatorHeight;
    private int tabIndicatorWidth;

    /*Segment部分*/
    private boolean isSegment;
    private int segmentStrokeColor;
    private int segmentBackgroundColor;
    private int segmentStrokeWidth;
    private boolean segmentRadiusHalfHeight;
    private int segmentRadius;

    private GradientDrawable indicatorGradientDrawable = new GradientDrawable();
    private float[] mRadiusArr = new float[8];

    //msg相关
    private int msgTextColor;
    private int msgTextSize;
    private int msgBackGroundColor;
    private int msgHeight;
    private int msgCornerRadius;
    private int msgDotSize;
    private int msgIndentation;
    private int msgHigher;


    public RTabLayout(Context context) {
        this(context, null);
    }

    public RTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setHorizontalScrollBarEnabled(false);

        tabs = new ArrayList<>();
        tabViewContentBounds = new RectF();
        selectedListeners = new ArrayList<>();

        tabViewPool = new SimplePool(12);

        slidingTabIndicator = new SlidingTabIndicator(context);

        super.addView(slidingTabIndicator, 0, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

        TypedArray typeArray = context.obtainStyledAttributes(attrs, R.styleable.RTabLayout, defStyleAttr, R.style.Widget_Design_TabLayout);

        //indicator
        tabSelectedIndicator = typeArray.getDrawable(R.styleable.RTabLayout_rtabIndicator);
        tabIndicatorHeight = typeArray.getDimensionPixelSize(R.styleable.RTabLayout_rtabIndicatorHeight, dpToPx(2));
        tabIndicatorRadius = typeArray.getDimensionPixelSize(R.styleable.RTabLayout_rtabIndicatorRadius, 0);
        tabIndicatorWidth = typeArray.getDimensionPixelSize(R.styleable.RTabLayout_rtabIndicatorWidth, 0);
        tabIndicatorColor = typeArray.getColor(R.styleable.RTabLayout_rtabIndicatorColor, 0);
        tabIndicatorFullWidth = typeArray.getBoolean(R.styleable.RTabLayout_rtabIndicatorFullWidth, tabIndicatorWidth == 0);
        tabIndicatorGravity = typeArray.getInt(R.styleable.RTabLayout_rtabIndicatorGravity, 0);

        //icon
        iconGravity = typeArray.getInt(R.styleable.RTabLayout_rtabIconGravity, 0);
        iconSize = typeArray.getDimensionPixelSize(R.styleable.RTabLayout_iconSize, dpToPx(30));//TODO:随便设个默认图标大小
        iconWidth = typeArray.getDimensionPixelSize(R.styleable.RTabLayout_iconWidth, iconSize);
        iconHeight = typeArray.getDimensionPixelSize(R.styleable.RTabLayout_iconHeight, iconSize);
        iconMarginToText = typeArray.getDimensionPixelSize(R.styleable.RTabLayout_iconMarginToText, 0);

        //msg
        msgTextSize = typeArray.getInt(R.styleable.RTabLayout_msgTextSize, 11);
        msgTextColor = typeArray.getColor(R.styleable.RTabLayout_msgTextColor, getResources().getColor(R.color.white));
        msgBackGroundColor = typeArray.getColor(R.styleable.RTabLayout_msgBackGroundColor, Color.parseColor("#ff0d0d"));//default red
        msgHeight = typeArray.getDimensionPixelSize(R.styleable.RTabLayout_msgHeight, -2);
        msgCornerRadius = typeArray.getDimensionPixelSize(R.styleable.RTabLayout_msgCornerRadius, msgHeight > 0 ? msgHeight / 2 : dpToPx(5));
        msgDotSize = typeArray.getDimensionPixelSize(R.styleable.RTabLayout_msgDotSize, dpToPx(6));
        msgIndentation = typeArray.getDimensionPixelSize(R.styleable.RTabLayout_msgIndentation, dpToPx(3));
        msgHigher = typeArray.getDimensionPixelSize(R.styleable.RTabLayout_msgHigher, -1);

        //segment
        isSegment = typeArray.getBoolean(R.styleable.RTabLayout_isSegment, false);
        segmentStrokeColor = typeArray.getColor(R.styleable.RTabLayout_segmentStrokeColor, 0);//不设置则无边框
        segmentBackgroundColor = typeArray.getColor(R.styleable.RTabLayout_segmentBackgroundColor, getResources().getColor(R.color.white));//默认白色
        segmentStrokeWidth = typeArray.getDimensionPixelSize(R.styleable.RTabLayout_segmentStrokeWidth, dpToPx(1));
        segmentRadiusHalfHeight = typeArray.getBoolean(R.styleable.RTabLayout_segmentRadiusHalfHeight, true);
        segmentRadius = typeArray.getDimensionPixelSize(R.styleable.RTabLayout_segmentRadius, -1);


//        tabPaddingStart = tabPaddingTop = tabPaddingEnd = tabPaddingBottom = typeArray.getDimensionPixelSize(R.styleable.RTabLayout_rtabPadding, 0);//TODO:如果widthpadding顺利再把height的默认值也搞定
        tabPaddingStart = typeArray.getDimensionPixelSize(R.styleable.RTabLayout_rtabPaddingStart, dpToPx(20));
        tabPaddingTop = typeArray.getDimensionPixelSize(R.styleable.RTabLayout_rtabPaddingTop, tabPaddingTop);
        tabPaddingEnd = typeArray.getDimensionPixelSize(R.styleable.RTabLayout_rtabPaddingEnd, dpToPx(20));
        tabPaddingBottom = typeArray.getDimensionPixelSize(R.styleable.RTabLayout_rtabPaddingBottom, tabPaddingBottom);
        isSelectedBold = typeArray.getBoolean(R.styleable.RTabLayout_rtabIsSelectedBold, false);
        tabTextAppearance = typeArray.getResourceId(R.styleable.RTabLayout_rtabTextAppearance, R.style.tabTextAppearance);
        if (tabTextAppearance != 0) {
            TypedArray ta = context.obtainStyledAttributes(tabTextAppearance, R.styleable.TextAppearance);
            try {
                tabTextSize = (float) ta.getDimensionPixelSize(R.styleable.TextAppearance_android_textSize, 0);
                tabTextColors = createColorStateList(ta.getColor(R.styleable.TextAppearance_android_textColor, 0));
            } finally {
                ta.recycle();
            }
        }

        if (typeArray.hasValue(R.styleable.RTabLayout_rtabTextSize))
            tabTextSize = (float) typeArray.getDimensionPixelSize(R.styleable.RTabLayout_rtabTextSize, 0);

        if (typeArray.hasValue(R.styleable.RTabLayout_rtabTextColor)) {
            tabTextColors = createColorStateList(typeArray.getColor(R.styleable.RTabLayout_rtabTextColor, 0));
        }
        if (typeArray.hasValue(R.styleable.RTabLayout_rtabSelectedTextColor)) {
            tabTextColors = createColorStateList(tabTextColors.getDefaultColor(),
                    typeArray.getColor(R.styleable.RTabLayout_rtabSelectedTextColor, 0));
        }

        tabIndicatorAnimationDuration = typeArray.getInt(R.styleable.RTabLayout_rtabIndicatorAnimationDuration, 300);
        requestedTabMinWidth = typeArray.getDimensionPixelSize(R.styleable.RTabLayout_rtabMinWidth, -1);
        requestedTabMaxWidth = typeArray.getDimensionPixelSize(R.styleable.RTabLayout_rtabMaxWidth, -1);//TODO:max对measure的影响
        rtabTabAverageWidth = typeArray.getBoolean(R.styleable.RTabLayout_rtabTabAverageWidth, false);
        typeArray.recycle();
    }


    public void setSelectedTabIndicatorColor(@ColorInt int color) {
        tabIndicatorColor = color;
        ViewCompat.postInvalidateOnAnimation(this.slidingTabIndicator);
    }


    public void setIndicatorHeight(int height) {
        tabIndicatorHeight = height;
        ViewCompat.postInvalidateOnAnimation(this.slidingTabIndicator);
        //android中实现view的更新有两组方法，一组是invalidate，另一组是postInvalidate，其中前者是在UI线程自身中使用，而后者在非UI线程中使用
    }

    public void setScrollPosition(int position, float positionOffset, boolean updateSelectedText) {
        this.setScrollPosition(position, positionOffset, updateSelectedText, true);
    }

    void setScrollPosition(int position, float positionOffset, boolean updateSelectedText, boolean updateIndicatorPosition) {
        int roundedPosition = Math.round((float) position + positionOffset);
        if (roundedPosition >= 0 && roundedPosition < this.slidingTabIndicator.getChildCount()) {
            if (updateIndicatorPosition) {
                this.slidingTabIndicator.setIndicatorPositionFromTabPosition(position);
            }

            if (this.scrollAnimator != null && this.scrollAnimator.isRunning()) {
                this.scrollAnimator.cancel();
            }

            this.scrollTo(this.calculateScrollXForTab(position, positionOffset), 0);
            if (updateSelectedText) {
                this.setSelectedTabView(roundedPosition);
            }

        }
    }

    public void addTab(@NonNull Tab tab) {
        this.addTab(tab, tabs.isEmpty());//利用tabs判空，设定第一个添加的tab为选中状态
    }

    public void addTab(@NonNull Tab tab, int position) {
        this.addTab(tab, position, tabs.isEmpty());
    }

    public void addTab(@NonNull Tab tab, boolean setSelected) {
        this.addTab(tab, tabs.size(), setSelected);
    }

    public void addTab(@NonNull Tab tab, int position, boolean setSelected) {
        if (tab.parent != this)
            throw new IllegalArgumentException("Tab belongs to a different TabLayout.");
        else {
            configureTab(tab, position);
            addTabView(tab);
            if (setSelected)
                tab.select();
        }
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


    public void addOnTabSelectedListener(@NonNull BaseOnTabSelectedListener listener) {
        if (!selectedListeners.contains(listener)) {
            selectedListeners.add(listener);
        }

    }

    public void removeOnTabSelectedListener(@NonNull BaseOnTabSelectedListener listener) {
        this.selectedListeners.remove(listener);
    }

    public void clearOnTabSelectedListeners() {
        this.selectedListeners.clear();
    }

    @NonNull
    public Tab newTab() {
        Tab tab = createTabFromPool();
        tab.parent = this;
        tab.view = createTabView(tab);
        return tab;
    }

    protected Tab createTabFromPool() {
        Tab tab = tabPool.acquire();
        if (tab == null) {
            tab = new Tab();
        }

        return tab;
    }

    protected boolean releaseFromTabPool(Tab tab) {
        return tabPool.release(tab);
    }

    public int getTabCount() {
        return tabs.size();
    }

    @Nullable
    public Tab getTabAt(int index) {
        return index >= 0 && index < getTabCount() ? tabs.get(index) : null;
    }

    public int getSelectedTabPosition() {
        return this.selectedTab != null ? this.selectedTab.getPosition() : -1;
    }

    public void removeTab(Tab tab) {
        if (tab.parent != this) {
            throw new IllegalArgumentException("Tab does not belong to this TabLayout.");
        } else {
            this.removeTabAt(tab.getPosition());
        }
    }

    public void removeTabAt(int position) {
        int selectedTabPosition = this.selectedTab != null ? this.selectedTab.getPosition() : 0;
        this.removeTabViewAt(position);
        Tab removedTab = (Tab) this.tabs.remove(position);
        if (removedTab != null) {
            removedTab.reset();
            this.releaseFromTabPool(removedTab);
        }

        int newTabCount = this.tabs.size();

        for (int i = position; i < newTabCount; ++i) {
            ((Tab) this.tabs.get(i)).setPosition(i);
        }

        if (selectedTabPosition == position) {
            this.selectTab(this.tabs.isEmpty() ? null : (Tab) this.tabs.get(Math.max(0, position - 1)));
        }

    }

    public void removeAllTabs() {
        for (int i = this.slidingTabIndicator.getChildCount() - 1; i >= 0; --i) {
            this.removeTabViewAt(i);
        }

        Iterator i = this.tabs.iterator();

        while (i.hasNext()) {
            Tab tab = (Tab) i.next();
            i.remove();
            tab.reset();
            this.releaseFromTabPool(tab);
        }

        this.selectedTab = null;
    }


    public void setSelectedTabIndicatorGravity(int indicatorGravity) {
        if (tabIndicatorGravity != indicatorGravity) {
            tabIndicatorGravity = indicatorGravity;
            ViewCompat.postInvalidateOnAnimation(slidingTabIndicator);
        }

    }

    public int getTabIndicatorGravity() {
        return this.tabIndicatorGravity;
    }

    public void setTabIndicatorFullWidth(boolean tabIndicatorFullWidth) {
        this.tabIndicatorFullWidth = tabIndicatorFullWidth;
        ViewCompat.postInvalidateOnAnimation(this.slidingTabIndicator);
    }

    public boolean isTabIndicatorFullWidth() {
        return this.tabIndicatorFullWidth;
    }


    public boolean isInlineLabel() {
        return this.inlineLabel;
    }


    public void setTabTextColors(@Nullable ColorStateList textColor) {
        if (tabTextColors != textColor) {
            tabTextColors = textColor;
            updateAllTabs();
        }

    }

    @Nullable
    public ColorStateList getTabTextColors() {
        return this.tabTextColors;
    }

    public void setTabTextColors(int normalColor, int selectedColor) {
        this.setTabTextColors(createColorStateList(normalColor, selectedColor));
    }

    @Nullable
    public Drawable getTabSelectedIndicator() {
        return this.tabSelectedIndicator;
    }

    public void setSelectedTabIndicator(@Nullable Drawable tabSelectedIndicator) {
        if (this.tabSelectedIndicator != tabSelectedIndicator) {
            this.tabSelectedIndicator = tabSelectedIndicator;
            ViewCompat.postInvalidateOnAnimation(slidingTabIndicator);
        }

    }

    public void setSelectedTabIndicator(@DrawableRes int tabSelectedIndicatorResourceId) {
        if (tabSelectedIndicatorResourceId != 0) {
            this.setSelectedTabIndicator(AppCompatResources.getDrawable(getContext(), tabSelectedIndicatorResourceId));
        } else {
            this.setSelectedTabIndicator(null);
        }

    }

    public void setupWithViewPager(@Nullable ViewPager viewPager) {
        this.setupWithViewPager(viewPager, true);
    }

    public void setupWithViewPager(@Nullable ViewPager viewPager, boolean autoRefresh) {
        this.setupWithViewPager(viewPager, autoRefresh, false);
    }

    private void setupWithViewPager(@Nullable ViewPager viewPager, boolean autoRefresh, boolean implicitSetup) {
        if (this.viewPager != null) {
            if (this.pageChangeListener != null) {
                this.viewPager.removeOnPageChangeListener(this.pageChangeListener);
            }

            if (this.adapterChangeListener != null) {
                this.viewPager.removeOnAdapterChangeListener(this.adapterChangeListener);
            }
        }

        if (this.currentVpSelectedListener != null) {
            this.removeOnTabSelectedListener(this.currentVpSelectedListener);
            this.currentVpSelectedListener = null;
        }

        if (viewPager != null) {
            this.viewPager = viewPager;
            if (this.pageChangeListener == null) {
                this.pageChangeListener = new TabLayoutOnPageChangeListener(this);
            }

            this.pageChangeListener.reset();
            viewPager.addOnPageChangeListener(this.pageChangeListener);
            this.currentVpSelectedListener = new ViewPagerOnTabSelectedListener(viewPager);
            this.addOnTabSelectedListener(this.currentVpSelectedListener);
            PagerAdapter adapter = viewPager.getAdapter();
            if (adapter != null) {
                this.setPagerAdapter(adapter, autoRefresh);
            }

            if (this.adapterChangeListener == null) {
                this.adapterChangeListener = new AdapterChangeListener();
            }

            this.adapterChangeListener.setAutoRefresh(autoRefresh);
            viewPager.addOnAdapterChangeListener(this.adapterChangeListener);
            this.setScrollPosition(viewPager.getCurrentItem(), 0.0F, true);
        } else {
            this.viewPager = null;
            this.setPagerAdapter(null, false);
        }

        this.setupViewPagerImplicitly = implicitSetup;
    }

    public boolean shouldDelayChildPressedState() {
        return this.getTabScrollRange() > 0;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.viewPager == null) {
            ViewParent vp = this.getParent();
            if (vp instanceof ViewPager) {
                setupWithViewPager((ViewPager) vp, true, true);
            }
        }

    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.setupViewPagerImplicitly) {
            this.setupWithViewPager(null);
            this.setupViewPagerImplicitly = false;
        }

    }

    private int getTabScrollRange() {
        return Math.max(0, this.slidingTabIndicator.getWidth() - this.getWidth() - this.getPaddingLeft() - this.getPaddingRight());
    }

    void setPagerAdapter(@Nullable PagerAdapter adapter, boolean addObserver) {
        if (this.pagerAdapter != null && this.pagerAdapterObserver != null) {
            this.pagerAdapter.unregisterDataSetObserver(this.pagerAdapterObserver);
        }

        this.pagerAdapter = adapter;
        if (addObserver && adapter != null) {
            if (this.pagerAdapterObserver == null) {
                this.pagerAdapterObserver = new PagerAdapterObserver();
            }

            adapter.registerDataSetObserver(this.pagerAdapterObserver);
        }

        this.populateFromPagerAdapter();
    }

    void populateFromPagerAdapter() {
        this.removeAllTabs();
        if (this.pagerAdapter != null) {
            int adapterCount = this.pagerAdapter.getCount();

            int curItem;
            for (curItem = 0; curItem < adapterCount; ++curItem) {
                this.addTab(this.newTab().setText(this.pagerAdapter.getPageTitle(curItem)), false);
            }

            if (this.viewPager != null && adapterCount > 0) {
                curItem = this.viewPager.getCurrentItem();
                if (curItem != this.getSelectedTabPosition() && curItem < this.getTabCount()) {
                    this.selectTab(this.getTabAt(curItem));
                }
            }
        }

    }

    private void updateAllTabs() {
        int i = 0;
        for (int z = this.tabs.size(); i < z; ++i) {
            tabs.get(i).updateView();
        }

    }

    private TabView createTabView(@NonNull Tab tab) {
        TabView tabView = tabViewPool != null ? tabViewPool.acquire() : null;
        if (tabView == null) {
            tabView = new TabView(getContext());
        }

        tabView.setTab(tab);
        tabView.setFocusable(true);
        tabView.setMinimumWidth(this.getTabMinWidth());

        return tabView;
    }


    /**
     * 更新tabs内位于新tab后的tab的index
     *
     * @param tab
     * @param position
     */
    private void configureTab(Tab tab, int position) {
        tab.setPosition(position);
        tabs.add(position, tab);
        int count = this.tabs.size();

        for (int i = position + 1; i < count; ++i) {
            this.tabs.get(i).setPosition(i);
        }

    }

    private void addTabView(Tab tab) {
        TabView tabView = tab.view;
        this.slidingTabIndicator.addView(tabView, tab.getPosition(), createLayoutParamsForTabs());
    }

    private android.widget.LinearLayout.LayoutParams createLayoutParamsForTabs() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.updateTabViewLayoutParams(lp);
        //specWidthMode在onMeasure时更新，但平常在onResume（这在onMeasure之前）中addTabView，无法得到正确的LayoutParams，暂时放弃在addView时修改layoutParams
        return lp;
    }

    private void updateTabViewLayoutParams(android.widget.LinearLayout.LayoutParams lp) {
        if (rtabTabAverageWidth) {
            //1.wrap_content 且 总宽大于max*count的时候weight才能正确生效
            //2.EXACTLY 强制weight=1均分
            lp.width = 0;
            lp.weight = 1.0F;
        } else {
            lp.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            lp.weight = 0.0F;
        }

    }

    int dpToPx(@Dimension(unit = 0) int dps) {
        return Math.round(this.getResources().getDisplayMetrics().density * (float) dps);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int idealHeight = dpToPx(getDefaultHeight()) + getPaddingTop() + getPaddingBottom();//TODO：测试带图标时高度
//        switch (MeasureSpec.getMode(heightMeasureSpec)) {
//            case MeasureSpec.AT_MOST:
//                heightMeasureSpec = MeasureSpec.makeMeasureSpec(Math.min(idealHeight, MeasureSpec.getSize(heightMeasureSpec)), MeasureSpec.EXACTLY);
//                break;
//            case MeasureSpec.UNSPECIFIED:
//                heightMeasureSpec = MeasureSpec.makeMeasureSpec(idealHeight, MeasureSpec.EXACTLY);
//                break;
//            default:
//                break;
//        }

        if (isSegment) {
            tabIndicatorHeight = getHeight();
            //segment background
            if (segmentRadiusHalfHeight || segmentRadius < 0 || segmentRadius > tabIndicatorHeight / 2)
                segmentRadius = tabIndicatorHeight / 2;

            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setCornerRadius(segmentRadius);
            if (segmentStrokeColor != 0)
                gradientDrawable.setStroke(segmentStrokeWidth, segmentStrokeColor);
            gradientDrawable.setColor(segmentBackgroundColor);
            setBackground(gradientDrawable);
            if (segmentStrokeWidth > 0) {
                //segment有边框的话要防止颜色冲突时覆盖（正常人大概不会设置成不同颜色..
                setPaddingRelative(segmentStrokeWidth, segmentStrokeWidth, segmentStrokeWidth, segmentStrokeWidth);
                tabIndicatorHeight -= 2 * segmentStrokeWidth;
            }
        }

        int specWidth = MeasureSpec.getSize(widthMeasureSpec);

        int specWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (rtabTabAverageWidth) {//1.match_parent 或者固定dp 2.wrap_content时，horizonScrollView会measure两次，第一次atmost，第二次exactly
            setFillViewport(true);//效果：使子LinearLayout的子View的weight生效（推测为修改了onMeasure）
//            slidingTabIndicator.setGravity(Gravity.CENTER_HORIZONTAL);
            updateTabViews();
        } else if (specWidthMode == MeasureSpec.AT_MOST) {
            //WRAP_CONTENT情况下为AT_MOST
        } else {//TODO:什么情况下是UNSPECIFIED  测试结果：1.嵌套在另一个scrollView中，嵌套先不管了，本身能滑动外面没必要再套了吧...
//            tabMaxWidth = requestedTabMaxWidth > 0 ? requestedTabMaxWidth : specWidth - dpToPx(56);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        if (getChildCount() == 1) {
//            View child = getChildAt(0);
//            boolean remeasure = child.getMeasuredWidth() < getMeasuredWidth();
//
//            if (remeasure) {
//                int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, getPaddingTop() + getPaddingBottom(), child.getLayoutParams().height);
//                int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY);
//                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
//            }
//        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    private void removeTabViewAt(int position) {
        TabView view = (TabView) this.slidingTabIndicator.getChildAt(position);
        this.slidingTabIndicator.removeViewAt(position);
        if (view != null) {
            view.reset();
            this.tabViewPool.release(view);
        }

        this.requestLayout();
    }

    private void animateToTab(int newPosition) {
        if (newPosition != -1) {
            if (getWindowToken() != null && ViewCompat.isLaidOut(this) && !slidingTabIndicator.childrenNeedLayout()) {
                int startScrollX = getScrollX();
                int targetScrollX = calculateScrollXForTab(newPosition, 0.0F);
                if (startScrollX != targetScrollX) {
                    ensureScrollAnimator();
                    scrollAnimator.setIntValues(startScrollX, targetScrollX);
                    scrollAnimator.start();
                }

                slidingTabIndicator.animateIndicatorToPosition(newPosition, tabIndicatorAnimationDuration);
            } else {
                setScrollPosition(newPosition, 0.0F, true);
            }
        }
    }

    private void ensureScrollAnimator() {
        if (this.scrollAnimator == null) {
            this.scrollAnimator = new ValueAnimator();
            this.scrollAnimator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
            this.scrollAnimator.setDuration((long) this.tabIndicatorAnimationDuration);
            this.scrollAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animator) {
                    scrollTo((Integer) animator.getAnimatedValue(), 0);
                }
            });
        }

    }

    void setScrollAnimatorListener(AnimatorListener listener) {
        this.ensureScrollAnimator();
        this.scrollAnimator.addListener(listener);
    }

    private void setSelectedTabView(int position) {
        int tabCount = this.slidingTabIndicator.getChildCount();
        if (position < tabCount) {
            for (int i = 0; i < tabCount; ++i) {
                View child = this.slidingTabIndicator.getChildAt(i);
                child.setSelected(i == position);
                child.setActivated(i == position);
            }
        }

    }

    void selectTab(Tab tab) {
        this.selectTab(tab, true);
    }

    void selectTab(Tab tab, boolean updateIndicator) {
        Tab currentTab = this.selectedTab;
        if (currentTab == tab) {
            if (currentTab != null) {
                this.dispatchTabReselected(tab);
                this.animateToTab(tab.getPosition());//TODO：重选为什么也要动画效果？
            }
        } else {
            int newPosition = tab != null ? tab.getPosition() : -1;
            if (updateIndicator) {
                if ((currentTab == null || currentTab.getPosition() == -1) && newPosition != -1) {
                    this.setScrollPosition(newPosition, 0.0F, true);
                } else {
                    this.animateToTab(newPosition);
                }

                if (newPosition != -1) {
                    this.setSelectedTabView(newPosition);
                }
            }

            this.selectedTab = tab;
            if (currentTab != null) {
                this.dispatchTabUnselected(currentTab);
            }

            if (tab != null) {
                this.dispatchTabSelected(tab);
            }
        }

    }

    private void dispatchTabSelected(@NonNull Tab tab) {
        for (int i = this.selectedListeners.size() - 1; i >= 0; --i) {
            selectedListeners.get(i).onTabSelected(tab);
        }

    }

    private void dispatchTabUnselected(@NonNull Tab tab) {
        for (int i = this.selectedListeners.size() - 1; i >= 0; --i) {
            selectedListeners.get(i).onTabUnselected(tab);
        }

    }

    private void dispatchTabReselected(@NonNull Tab tab) {
        for (int i = this.selectedListeners.size() - 1; i >= 0; --i) {
            selectedListeners.get(i).onTabReselected(tab);
        }

    }

    private int calculateScrollXForTab(int position, float positionOffset) {
        View selectedChild = this.slidingTabIndicator.getChildAt(position);
        View nextChild = position + 1 < this.slidingTabIndicator.getChildCount() ? this.slidingTabIndicator.getChildAt(position + 1) : null;
        int selectedWidth = selectedChild != null ? selectedChild.getWidth() : 0;
        int nextWidth = nextChild != null ? nextChild.getWidth() : 0;
        int scrollBase = selectedChild.getLeft() + selectedWidth / 2 - this.getWidth() / 2;
        int scrollOffset = (int) ((float) (selectedWidth + nextWidth) * 0.5F * positionOffset);
        return scrollBase + scrollOffset;
    }


    void updateTabViews() {
        for (int i = 0; i < slidingTabIndicator.getChildCount(); ++i) {
            View child = slidingTabIndicator.getChildAt(i);
            updateTabViewLayoutParams((LinearLayout.LayoutParams) child.getLayoutParams());
        }
    }

    private static ColorStateList createColorStateList(int defaultColor) {
        int[][] states = new int[1][];
        int[] colors = new int[1];
        states[0] = EMPTY_STATE_SET;
        colors[0] = defaultColor;
        return new ColorStateList(states, colors);
    }

    private static ColorStateList createColorStateList(int defaultColor, int selectedColor) {
        int[][] states = new int[2][];
        int[] colors = new int[2];
        states[0] = SELECTED_STATE_SET;
        colors[0] = selectedColor;
        states[1] = EMPTY_STATE_SET;
        colors[1] = defaultColor;
        return new ColorStateList(states, colors);
    }

    /**
     * 默认高度，无图标48，有图标72
     */
    private int getDefaultHeight() {
        boolean hasIconAndText = false;
        int i = 0;

        for (int count = tabs.size(); i < count; ++i) {
            Tab tab = tabs.get(i);
            if (tab != null && tab.icon != 0 && !TextUtils.isEmpty(tab.getText())) {
                hasIconAndText = true;
                break;
            }
        }

        return hasIconAndText ? 72 : 48;
    }

    private int getTabMinWidth() {
        return requestedTabMinWidth != -1 ? requestedTabMinWidth : 0;
    }

    int getTabMaxWidth() {
        return requestedTabMaxWidth;
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return generateDefaultLayoutParams();
    }

//    int getTabMaxWidth() {
//        return tabMaxWidth;
//    }

    private class AdapterChangeListener implements OnAdapterChangeListener {
        private boolean autoRefresh;

        AdapterChangeListener() {
        }

        public void onAdapterChanged(@NonNull ViewPager viewPager, @Nullable PagerAdapter oldAdapter, @Nullable PagerAdapter newAdapter) {
            if (RTabLayout.this.viewPager == viewPager) {
                RTabLayout.this.setPagerAdapter(newAdapter, this.autoRefresh);
            }

        }

        void setAutoRefresh(boolean autoRefresh) {
            this.autoRefresh = autoRefresh;
        }
    }

    private class PagerAdapterObserver extends DataSetObserver {
        PagerAdapterObserver() {
        }

        public void onChanged() {
            RTabLayout.this.populateFromPagerAdapter();
        }

        public void onInvalidated() {
            RTabLayout.this.populateFromPagerAdapter();
        }
    }

    public static class ViewPagerOnTabSelectedListener implements OnTabSelectedListener {
        private final ViewPager viewPager;

        public ViewPagerOnTabSelectedListener(ViewPager viewPager) {
            this.viewPager = viewPager;
        }

        public void onTabSelected(Tab tab) {
            this.viewPager.setCurrentItem(tab.getPosition());
        }

        public void onTabUnselected(Tab tab) {
        }

        public void onTabReselected(Tab tab) {
        }
    }

    public static class TabLayoutOnPageChangeListener implements OnPageChangeListener {
        private final WeakReference<RTabLayout> tabLayoutRef;
        private int previousScrollState;
        private int scrollState;

        public TabLayoutOnPageChangeListener(RTabLayout tabLayout) {
            this.tabLayoutRef = new WeakReference(tabLayout);
        }

        public void onPageScrollStateChanged(int state) {
            this.previousScrollState = this.scrollState;
            this.scrollState = state;
        }

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            RTabLayout tabLayout = (this.tabLayoutRef.get());
            if (tabLayout != null) {
                boolean updateText = this.scrollState != 2 || this.previousScrollState == 1;
                boolean updateIndicator = this.scrollState != 2 || this.previousScrollState != 0;
                tabLayout.setScrollPosition(position, positionOffset, updateText, updateIndicator);
            }

        }

        public void onPageSelected(int position) {
            RTabLayout tabLayout = (this.tabLayoutRef.get());
            if (tabLayout != null && tabLayout.getSelectedTabPosition() != position && position < tabLayout.getTabCount()) {
                boolean updateIndicator = this.scrollState == 0 || this.scrollState == 2 && this.previousScrollState == 0;
                tabLayout.selectTab(tabLayout.getTabAt(position), updateIndicator);
            }

        }

        void reset() {
            this.previousScrollState = this.scrollState = 0;
        }
    }

    private class SlidingTabIndicator extends LinearLayout {

        int selectedPosition = -1;
        //        float selectionOffset;//TODO:indicator偏移，应该是一个0-1的值，且建立在每个tabView宽度均等的基础上，用处不大，考虑删除
        //控制canvas的bounds
        private int indicatorLeft = -1;
        private int indicatorRight = -1;
        private ValueAnimator indicatorAnimator;

        SlidingTabIndicator(Context context) {
            super(context);
            this.setWillNotDraw(false);
        }

        boolean childrenNeedLayout() {
            int i = 0;

            for (int z = getChildCount(); i < z; ++i) {
                View child = getChildAt(i);
                if (child.getWidth() <= 0) {
                    return true;
                }
            }

            return false;
        }

        void setIndicatorPositionFromTabPosition(int position) {
            if (this.indicatorAnimator != null && this.indicatorAnimator.isRunning()) {
                this.indicatorAnimator.cancel();
            }

            selectedPosition = position;
            updateIndicatorPosition();
        }


        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int mode = MeasureSpec.getMode(widthMeasureSpec);
            if (mode != MeasureSpec.EXACTLY) {
                // HorizontalScrollView will first measure use with UNSPECIFIED, and then with
                // EXACTLY. Ignore the first call since anything we do will be overwritten anyway
                return;
            }
//            if (specWidthMode == MeasureSpec.AT_MOST && rtabTabAverageWidth) {//总宽固定（match_parent||xxdp）不考虑二次处理，设法使tabView宽度不超过平均值
//                int count = this.getChildCount();
//                int largestTabWidth = 0;
//
//                for (int i = 0; i < count; ++i) {
//                    View child = this.getChildAt(i);
//                    if (child.getVisibility() == VISIBLE) {
//                        largestTabWidth = Math.max(largestTabWidth, child.getMeasuredWidth());
//                    }
//                }
//
//                if (largestTabWidth <= 0) {
//                    return;
//                }
//
//                if (largestTabWidth * count > getMeasuredWidth())
//                    for (int i = 0; i < count; ++i) {
//                        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) this.getChildAt(i).getLayoutParams();
//                        if (lp.width != largestTabWidth || lp.weight != 0.0F) {
//                            lp.width = largestTabWidth;
//                            lp.weight = 0.0F;
//                        }
//                    }
//
//                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//
//            }
//            if (rtabTabAverageWidth) {
//                //maxWidth*count 小于既定宽度，则使weight=1以实现等宽
//                //else 强行设定每个tabView宽度为最大，同样实现等宽
//                int count = this.getChildCount();
//                int largestTabWidth = 0;
//
//                for (int i = 0; i < count; ++i) {
//                    View child = this.getChildAt(i);
//                    if (child.getVisibility() == VISIBLE) {
//                        largestTabWidth = Math.max(largestTabWidth, child.getMeasuredWidth());
//                    }
//                }
//
//                if (largestTabWidth <= 0) {
//                    return;
//                }
//
//                boolean remeasure = false;
//
//                if (largestTabWidth * count > getMeasuredWidth()) {
//                    updateTabViews();
//                    remeasure = true;
//                } else {
//                    for (int i = 0; i < count; ++i) {
//                        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) this.getChildAt(i).getLayoutParams();
//                        if (lp.width != largestTabWidth || lp.weight != 0.0F) {
//                            lp.width = largestTabWidth;
//                            lp.weight = 0.0F;
//                            remeasure = true;
//                        }
//                    }
//                }
//
//                if (remeasure) {
//                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//                }
//            }
        }

        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            if (indicatorAnimator != null && indicatorAnimator.isRunning()) {
                indicatorAnimator.cancel();
                long duration = indicatorAnimator.getDuration();
                //需要layout时强制取消动画，并使其改变动画终点？
                animateIndicatorToPosition(selectedPosition, Math.round((1.0F - indicatorAnimator.getAnimatedFraction()) * (float) duration));
            } else {
                updateIndicatorPosition();
            }

        }

        private void updateIndicatorPosition() {
            View selectedView = getChildAt(selectedPosition);
            int left;
            int right;
            if (selectedView != null && selectedView.getWidth() > 0) {
                calculateIndicatorBounds((TabView) selectedView, tabViewContentBounds);
                left = (int) tabViewContentBounds.left;
                right = (int) tabViewContentBounds.right;
            } else {
                right = -1;
                left = -1;
            }

            setIndicatorPosition(left, right);
        }

        void setIndicatorPosition(int left, int right) {
            if (left != indicatorLeft || right != indicatorRight) {
                indicatorLeft = left;
                indicatorRight = right;
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }

        void animateIndicatorToPosition(final int position, int duration) {
            if (indicatorAnimator != null && indicatorAnimator.isRunning()) {
                indicatorAnimator.cancel();
            }

            View targetView = getChildAt(position);
            if (targetView == null) {
                updateIndicatorPosition();
            } else {
                int targetLeft;
                int targetRight;
                calculateIndicatorBounds((TabView) targetView, tabViewContentBounds);
                targetLeft = (int) tabViewContentBounds.left;
                targetRight = (int) tabViewContentBounds.right;

                final int startLeft = indicatorLeft;
                final int startRight = indicatorRight;
                if (startLeft != targetLeft || startRight != targetRight) {
                    ValueAnimator animator = indicatorAnimator = new ValueAnimator();
                    animator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
                    animator.setDuration(duration);
                    animator.setFloatValues(0.0F, 1.0F);
                    final int finalTargetLeft = targetLeft;
                    final int finalTargetRight = targetRight;
                    animator.addUpdateListener(new AnimatorUpdateListener() {
                        public void onAnimationUpdate(ValueAnimator animator) {
                            float fraction = animator.getAnimatedFraction();
                            setIndicatorPosition(lerp(startLeft, finalTargetLeft, fraction), lerp(startRight, finalTargetRight, fraction));
                        }
                    });
                    animator.addListener(new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animator) {
                            selectedPosition = position;
                        }
                    });
                    animator.start();
                }

            }
        }

        public int lerp(int startValue, int endValue, float fraction) {
            return startValue + Math.round(fraction * (float) (endValue - startValue));
        }


        private void calculateIndicatorBounds(TabView tabView, RectF contentBounds) {
            int left;
            int right;
            left = tabView.getLeft();
            right = tabView.getRight();
            if (tabIndicatorWidth > 0) {//设定了tabIndicatorWidth时使用这个宽度
                int center = (left + right) / 2;
                int indicatorWidth;
                if (right - left < tabIndicatorWidth)//indicatorWidth大于tabView宽？（right-left<indicatorWidth)
                    indicatorWidth = right - left;
                else indicatorWidth = tabIndicatorWidth;
                left = center - indicatorWidth / 2;
                right = center + indicatorWidth / 2;
            } else if (!tabIndicatorFullWidth) {//不设定tabIndicatorWidth，但设定tabIndicatorFullWidth为false，则宽度为tab的content的宽度
                int tabViewContentWidth = tabView.getContentWidth();
                if (tabViewContentWidth < getTabMinWidth()) {//TODO:关于indicator最小宽度的设置
                    tabViewContentWidth = getTabMinWidth();
                }
                int tabViewCenter = (tabView.getLeft() + tabView.getRight()) / 2;
                left = tabViewCenter - tabViewContentWidth / 2;
                right = tabViewCenter + tabViewContentWidth / 2;
            }

            contentBounds.set((float) left, 0.0F, (float) right, 0.0F);
        }

        public void draw(Canvas canvas) {
            drawIndicator();

            int indicatorHeight = 0;
            if (tabSelectedIndicator != null) {
                indicatorHeight = tabSelectedIndicator.getIntrinsicHeight();
            }
            if (tabIndicatorHeight >= 0) {
                indicatorHeight = tabIndicatorHeight;
            }

            int indicatorTop = 0;
            int indicatorBottom = 0;
            switch (tabIndicatorGravity) {//TODO：tabIndicatorGravity默认为bottom，不排除日后需要使用的但相关方法予以保留
                case 0://bottom
                    indicatorTop = getHeight() - indicatorHeight;
                    indicatorBottom = getHeight();
                    break;
                case 1://center
                    indicatorTop = (getHeight() - indicatorHeight) / 2;
                    indicatorBottom = (getHeight() + indicatorHeight) / 2;
                    break;
                case 2://top
                    indicatorTop = 0;
                    indicatorBottom = indicatorHeight;
                    break;
                case 3://stretch
                    indicatorTop = 0;
                    indicatorBottom = getHeight();
            }

            if (indicatorLeft >= 0 && indicatorRight > indicatorLeft) {
                Drawable selectedIndicator = DrawableCompat.wrap(tabSelectedIndicator != null ? tabSelectedIndicator : indicatorGradientDrawable);
                selectedIndicator.setBounds(indicatorLeft, indicatorTop, indicatorRight, indicatorBottom);
                selectedIndicator.draw(canvas);
            }

            super.draw(canvas);
        }
    }

    class TabView extends RelativeLayout {
        private DrawableTextView drawableTextView;
        private MsgView msgView;
        private Tab tab;
        private boolean needFixPosition;//MsgView相对高度修正

        public TabView(Context context) {
            super(context);
//            ViewCompat.setPaddingRelative(this, tabPaddingStart, tabPaddingTop, tabPaddingEnd, tabPaddingBottom);
            setClickable(true);
            //触控笔之类的点击图标
            ViewCompat.setPointerIcon(this, PointerIconCompat.getSystemIcon(this.getContext(), PointerIconCompat.TYPE_HAND));
            setMinimumWidth(requestedTabMinWidth);
        }

        private void refreshTabIcon() {
            StateListDrawable drawable = new StateListDrawable();
            if (tab.selectedIcon != 0) {
                Drawable selected = getContext().getResources().getDrawable(tab.selectedIcon);
                drawable.addState(new int[]{android.R.attr.state_selected}, selected);
            }
            Drawable normal = getContext().getResources().getDrawable(tab.icon);
            drawable.addState(new int[]{-android.R.attr.state_selected}, normal);
            drawableTextView.setIcon(drawable);
        }

        @Override
        protected void drawableStateChanged() {
            super.drawableStateChanged();
            boolean changed = false;
            int[] state = this.getDrawableState();
            if (drawableTextView.getDrawable() != null && drawableTextView.getDrawable().isStateful()) {
                changed = drawableTextView.getDrawable().setState(state);
            }

            if (changed) {
                invalidate();
            }
        }

        @Override
        public boolean performClick() {
            boolean handled = super.performClick();
            if (tab != null) {
                if (!handled) {
                    playSoundEffect(0);
                }

                tab.select();
                return true;
            } else {
                return handled;
            }
        }

        public void setSelected(boolean selected) {
            boolean changed = this.isSelected() != selected;
            super.setSelected(selected);
            if (changed && selected && VERSION.SDK_INT < 16) {
                this.sendAccessibilityEvent(4);
            }

            if (drawableTextView != null) {
                drawableTextView.setSelected(selected);
                if (isSelectedBold) {
                    TextPaint textPaint = drawableTextView.getPaint();
                    textPaint.setFakeBoldText(selected);
                }
            }

        }

        void setTab(@Nullable Tab tab) {
            if (tab != this.tab) {
                this.tab = tab;
                update();
            }
        }

        void reset() {
            setTab(null);
            setSelected(false);
        }

        final void update() {
            Tab tab = this.tab;

            if (drawableTextView == null) {
                drawableTextView = new DrawableTextView(getContext());
                LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                drawableTextView.setMaxLines(1);
                drawableTextView.setSingleLine();
                drawableTextView.setId(View.generateViewId());
                TextViewCompat.setTextAppearance(drawableTextView, tabTextAppearance);
                drawableTextView.setTextSize(COMPLEX_UNIT_PX, tabTextSize);
                if (tabTextColors != null) {
                    drawableTextView.setTextColor(tabTextColors);
                }
                addView(drawableTextView,layoutParams);
            }

            if (tab != null && tab.icon != 0) {
                drawableTextView.setIconGravity(iconGravity);
                drawableTextView.setIconSize(iconHeight, iconWidth);
                drawableTextView.setIconMarginToText(iconMarginToText);
                //各种set在使用后结合实际再优化
                refreshTabIcon();
            }

            if (tab != null && !TextUtils.isEmpty(tab.text))
                drawableTextView.setText(tab.text);

            if (msgView == null) {
                msgView = new MsgView(getContext());
                msgView.setGravity(Gravity.CENTER);
                msgView.setIncludeFontPadding(false);
                msgView.setTextColor(msgTextColor);
                msgView.setTextSize(msgTextSize);
                msgView.setMaxLines(1);
                msgView.setSingleLine(true);
                msgView.setBackgroundColor(msgBackGroundColor);
                msgView.setCornerRadius(msgCornerRadius);
                msgView.setVisibility(INVISIBLE);
                LayoutParams layoutParams = (LayoutParams) msgView.getLayoutParams();
                layoutParams.height = msgHeight;
                layoutParams.width = LayoutParams.WRAP_CONTENT;
//                layoutParams.addRule(END_OF, drawableTextView.getId());
//                layoutParams.addRule(ALIGN_TOP, drawableTextView.getId());
//                TODO:放弃通过RelativeLayout的相对属性来定位MsgView，在Measure一次之后直接通过DrawableTextView测量结果来定位
                //END_OF时leftMargin设为负值无效 drawableText设为CenterHorizon也无法正常显示
                addView(msgView, layoutParams);

            }

            if (tab != null && msgView != null) {
                if (tab.msg == -1) {
                    //msg=-1 则消失
                    msgView.setVisibility(GONE);
                } else if (tab.msg == 0) {
                    setRelativeChildSize(msgView, msgDotSize, msgDotSize);
                    ViewCompat.setPaddingRelative(msgView, 0, 0, 0, 0);
                    msgView.setText("");
                    msgView.setVisibility(VISIBLE);
                } else {
                    setRelativeChildSize(msgView, msgHeight, LayoutParams.WRAP_CONTENT);
                    ViewCompat.setPaddingRelative(msgView, msgIndentation, 0, msgIndentation, 0);
                    msgView.setText(tab.msg > 99 ? "99+" : String.valueOf(tab.msg));
                    msgView.setVisibility(VISIBLE);
                }
                needFixPosition = true;
            }

            setSelected(tab != null && tab.isSelected());
        }

        @Override
        protected void onMeasure(int origWidthMeasureSpec, int heightMeasureSpec) {
            int specWidthSize = MeasureSpec.getSize(origWidthMeasureSpec);
            int specWidthMode = MeasureSpec.getMode(origWidthMeasureSpec);
            int widthMeasureSpec;

            if (requestedTabMaxWidth > 0 && (specWidthMode == MeasureSpec.UNSPECIFIED || specWidthSize > requestedTabMaxWidth)) {
                // If we have a max width and a given spec which is either unspecified or
                // larger than the max width, update the width spec using the same mode
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(requestedTabMaxWidth, MeasureSpec.AT_MOST);
            } else {
                // Else, use the original width spec
                widthMeasureSpec = origWidthMeasureSpec;
            }

            // Now lets measure
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            specWidthMode = MeasureSpec.getMode(widthMeasureSpec);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(drawableTextView.getMeasuredWidth() + tabPaddingStart + tabPaddingEnd, specWidthMode);
            setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);


            if (needFixPosition) {
                if (tab.msg != -1) {
                    int higher = msgHigher;
                    if (higher == -1)
                        higher = -msgView.getMeasuredHeight() / 2;
                    setRelativeChildMargin(msgView, -(msgView.getMeasuredWidth() / 2), higher);
                }
                needFixPosition = false;
                ViewCompat.postInvalidateOnAnimation(this);
            }
//            TODO：更新centerInParent的控件位置
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
        }

        /**
         * 用于计算TabView内容宽度（不是TabView占用的总宽度）
         *
         * @return
         */
        private int getContentWidth() {
            boolean initialized = false;
            int left = 0;
            int right = 0;
            View[] contentViews = new View[]{drawableTextView, msgView};
            //TODO:这里是遍历TabView里头预设的几个view来获得最大宽度，之后TabView应该要改成默认的一个ViewGroup，这里也要做相应修改

            for (View view : contentViews) {
                if (view != null) {
                    left = initialized ? Math.min(left, view.getLeft()) : view.getLeft();
                    right = initialized ? Math.max(right, view.getRight()) : view.getRight();
                    initialized = true;
                }
            }

            return right - left;
        }

        public Tab getTab() {
            return this.tab;
        }
    }

    public void setRelativeChildSize(MsgView child, int height, int width) {
        if (child == null) {
            return;
        }
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) child.getLayoutParams();
        lp.width = width;
        lp.height = height;
        child.setLayoutParams(lp);
    }

    public void setRelativeChildMargin(MsgView child, int leftMargin, int topMargin) {
        if (child == null) {
            return;
        }
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) child.getLayoutParams();
        lp.leftMargin = leftMargin;
        lp.topMargin = topMargin;
        child.setLayoutParams(lp);
    }

    public static class Tab {
        private int position = -1;
        RTabLayout parent;
        TabView view;
        CharSequence text;
        int msg = -1;
        int selectedIcon;
        int icon;
        Object tag;

        public Tab() {
        }

        public int getMsg() {
            return msg;
        }

        void setMsg(int msg) {
            this.msg = msg;
            updateView();
        }

        @Nullable
        public Object getTag() {
            return this.tag;
        }

        @NonNull
        public Tab setTag(@Nullable Object tag) {
            this.tag = tag;
            return this;
        }

        public boolean hasIcon() {
            return icon != 0 && selectedIcon != 0;
        }

        public int getPosition() {
            return this.position;
        }

        void setPosition(int position) {
            this.position = position;
        }

        @Nullable
        public CharSequence getText() {
            return text;
        }

        //TODO:TabLayout实际运用中可能有从网络获取icon资源的情况，应另有一系列直接设定Drawable的方法
        @NonNull
        public Tab setIcon(@DrawableRes int icon, @DrawableRes int selectedIcon) {
            this.icon = icon;
            this.selectedIcon = selectedIcon;
            updateView();
            return this;
        }

        @NonNull
        public Tab setText(@Nullable CharSequence text) {
            this.text = text;
            updateView();
            return this;
        }

        @NonNull
        public Tab setText(@StringRes int resId) {
            if (parent == null) {
                throw new IllegalArgumentException("Tab not attached to a TabLayout");
            } else {
                return this.setText(parent.getResources().getText(resId));
            }
        }

        public void select() {
            if (parent == null) {
                throw new IllegalArgumentException("Tab not attached to a TabLayout");
            } else {
                parent.selectTab(this);
            }
        }

        public boolean isSelected() {
            if (parent == null) {
                throw new IllegalArgumentException("Tab not attached to a TabLayout");
            } else {
                return parent.getSelectedTabPosition() == this.position;
            }
        }

        void updateView() {
            if (view != null) {
                view.update();
            }
        }

        void reset() {
            parent = null;
            view = null;
            tag = null;
            icon = 0;
            selectedIcon = 0;
            text = null;
            position = -1;
        }
    }

    public interface OnTabSelectedListener extends BaseOnTabSelectedListener<Tab> {
    }

    public interface BaseOnTabSelectedListener<T extends Tab> {
        void onTabSelected(T var1);

        void onTabUnselected(T var1);

        void onTabReselected(T var1);
    }


    private void drawIndicator() {
        /*indicator*/
        if (isSegment) {
            calIndicator();
            indicatorGradientDrawable.setColor(tabIndicatorColor);
            indicatorGradientDrawable.setCornerRadii(mRadiusArr);
            tabSelectedIndicator = null;
        } else {
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setColor(tabIndicatorColor);
            gradientDrawable.setCornerRadius(tabIndicatorRadius);
            tabSelectedIndicator = gradientDrawable;
        }
    }


    private void calIndicator() {
        int segmentRadius = this.segmentRadius;
        if (segmentStrokeWidth > 0) {
            segmentRadius -= segmentStrokeWidth;
        }
        if (tabIndicatorAnimationDuration <= 0) {
            //此版本规则：如果动画时间为0则segment只在左右两侧为圆角，中间为矩形边
            if (getSelectedTabPosition() == 0) {
                /**The corners are ordered top-left, top-right, bottom-right, bottom-left*/
                mRadiusArr[0] = segmentRadius;
                mRadiusArr[1] = segmentRadius;
                mRadiusArr[2] = 0;
                mRadiusArr[3] = 0;
                mRadiusArr[4] = 0;
                mRadiusArr[5] = 0;
                mRadiusArr[6] = segmentRadius;
                mRadiusArr[7] = segmentRadius;
            } else if (getSelectedTabPosition() == getTabCount() - 1) {
                /**The corners are ordered top-left, top-right, bottom-right, bottom-left*/
                mRadiusArr[0] = 0;
                mRadiusArr[1] = 0;
                mRadiusArr[2] = segmentRadius;
                mRadiusArr[3] = segmentRadius;
                mRadiusArr[4] = segmentRadius;
                mRadiusArr[5] = segmentRadius;
                mRadiusArr[6] = 0;
                mRadiusArr[7] = 0;
            } else {
                /**The corners are ordered top-left, top-right, bottom-right, bottom-left*/
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
            /**The corners are ordered top-left, top-right, bottom-right, bottom-left*/
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

    public void hideMsg(int position) {
        showMsg(position, -1);
    }

    public void showDot(int position) {
        showMsg(position, 0);
    }

    public void showMsg(int position, int num) {
        if (position < 0 || position >= getTabCount())
            return;
        getTabAt(position).setMsg(num);
    }

    public void setData(String[] titleList) {
        removeAllTabs();
        if (titleList != null) {
            for (String s : titleList) {
                addTab(newTab().setText(s));
            }
        }
    }

    public void setData(String[] titleList, int[] iconList) {
        if (titleList != null && iconList != null) {
            if (titleList.length != iconList.length)
                throw new InvalidParameterException("标题与图标数组长度必须一致");
            removeAllTabs();
            for (int i = 0; i < titleList.length; i++) {
                addTab(newTab().setText(titleList[i]).setIcon(iconList[i], 0));
            }
        }
    }

    public void setData(String[] titleList, int[] iconList, int[] selectedIconList) {
        if (titleList != null && iconList != null && selectedIconList != null) {
            if (titleList.length != iconList.length || titleList.length != selectedIconList.length)
                throw new InvalidParameterException("标题与图标数组长度必须一致");
            removeAllTabs();
            for (int i = 0; i < titleList.length; i++) {
                addTab(newTab().setText(titleList[i]).setIcon(iconList[i], selectedIconList[i]));
            }
        }
    }

}
