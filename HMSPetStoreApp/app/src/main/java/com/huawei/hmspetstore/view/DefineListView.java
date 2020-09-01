package com.huawei.hmspetstore.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class DefineListView extends ListView {
    public DefineListView(Context context) {
        super(context);
    }

    public DefineListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DefineListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DefineListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}