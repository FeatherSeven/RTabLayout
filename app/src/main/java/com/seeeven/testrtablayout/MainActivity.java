package com.seeeven.testrtablayout;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.ruigu.tablayout.RTabLayout;

//import com.ruigu.tablayout.RTabLayout;

public class MainActivity extends AppCompatActivity {


    RTabLayout rtl_test;
    private String[] mTitles = {"首页", "分类", "发现", "购物车车车车车", "我的"};
    //    private String[] mTitles = {"首页", "分类", "发现", "购物车车车车车", "我的", "分类", "发现", "购物车车车车车", "我的"};
    private int[] mIconUnselectIds = {
            R.mipmap.tab1_off, R.mipmap.tab2_off, R.mipmap.tab3_off,
            R.mipmap.tab4_off, R.mipmap.tab5_off};
    private int[] mIconSelectIds = {
            R.mipmap.tab1_on, R.mipmap.tab2_on, R.mipmap.tab3_on,
            R.mipmap.tab4_on, R.mipmap.tab5_on};

    private int[] msgs = new int[]{0, 1, -1, 100, 99};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rtl_test = findViewById(R.id.rtl_test);


//        showLayoutTree(rtl_test, "└");

    }

    @Override
    protected void onResume() {
        super.onResume();
//        rtl_test.setData(mTitles,mIconUnselectIds,mIconSelectIds);
        rtl_test.setData(mTitles);

//        rtl_test.showMsg(0, -1);
//        rtl_test.showMsg(1, 3);
//        rtl_test.showMsg(2, 100);
//        rtl_test.showMsg(3, 0);
//        rtl_test.showMsg(4, 0);

        rtl_test.addOnTabSelectedListener(new RTabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(RTabLayout.Tab tab) {
                rtl_test.showMsg(tab.getPosition(), 100);
            }

            @Override
            public void onTabUnselected(RTabLayout.Tab var1) {

            }

            @Override
            public void onTabReselected(RTabLayout.Tab var1) {

            }
        });
    }

    private int getHeight(View view) {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        return view.getMeasuredHeight();
    }

    public void showLayoutTree(ViewGroup viewGroup, String level) {
        Log.e("viewTree", level + viewGroup.getClass().getName());
        level = " " + level;
        if (viewGroup.getChildCount() != 0) {
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                if (viewGroup.getChildAt(i) instanceof ViewGroup)
                    showLayoutTree((ViewGroup) viewGroup.getChildAt(i), level);
                else
                    Log.e("viewTree", level + viewGroup.getChildAt(i).getClass().getName());
            }
        }
    }
}
