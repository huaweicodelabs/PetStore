package com.huawei.hmspetstore.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.util.SystemUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MapStyleExpandView extends FrameLayout implements View.OnClickListener {
    private Context mContext;
    private View mImgStyleDefault;
    private View mImgStyleSimple;
    private View mImgStyleNight;
    private View mViewRoot;
    private boolean mIsShowing = false;
    private float dp = 50;
    private View[] mViewsArr;

    public MapStyleExpandView(@NonNull Context context) {
        this(context, null);
    }

    public MapStyleExpandView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MapStyleExpandView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    private void initView() {
        View mContentView = View.inflate(mContext, R.layout.layout_map_style, this);
        mViewRoot = mContentView.findViewById(R.id.view_root);
        mImgStyleDefault = mContentView.findViewById(R.id.img_style_default);
        mImgStyleSimple = mContentView.findViewById(R.id.img_style_simple);
        mImgStyleNight = mContentView.findViewById(R.id.img_style_night);

        mViewRoot.setOnClickListener(this);
        mImgStyleDefault.setOnClickListener(this);
        mImgStyleSimple.setOnClickListener(this);
        mImgStyleNight.setOnClickListener(this);

        mViewsArr = new View[]{mImgStyleNight, mImgStyleSimple, mImgStyleDefault};
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_root:
                switchView();
                break;
            case R.id.img_style_default:
                updateBackground(R.id.img_style_default);
                if (onMapStyleSelectedListener != null) {
                    onMapStyleSelectedListener.mapStyle(-1);
                }
                break;
            case R.id.img_style_simple:
                updateBackground(R.id.img_style_simple);
                if (onMapStyleSelectedListener != null) {
                    onMapStyleSelectedListener.mapStyle(R.raw.mapstyle_simple);
                }
                break;
            case R.id.img_style_night:
                updateBackground(R.id.img_style_night);
                if (onMapStyleSelectedListener != null) {
                    onMapStyleSelectedListener.mapStyle(R.raw.mapstyle_night);
                }
                break;
            default:
                break;
        }
    }

    private void updateBackground(int id) {
        mImgStyleNight.setSelected(id == R.id.img_style_night);
        mImgStyleSimple.setSelected(id == R.id.img_style_simple);
        mImgStyleDefault.setSelected(id == R.id.img_style_default);
    }

    /**
     * 切换状态
     */
    public void switchView() {
        if (mIsShowing) {
            closeView();
        } else {
            openView();
        }
        mIsShowing = !mIsShowing;
    }

    private void openView() {
        for (int i = 0; i < mViewsArr.length; i++) {
            AnimatorSet set = new AnimatorSet();
            double x = -SystemUtil.dp2px(mContext, dp) * (i + 1);
            set.playTogether(ObjectAnimator.ofFloat(mViewsArr[i], "translationX", 0, (float) x)
                    , ObjectAnimator.ofFloat(mViewsArr[i], "alpha", 0, 1).setDuration(1000L));
            set.setInterpolator(new DecelerateInterpolator());
            set.setDuration(300L).setStartDelay(50L * i);
            set.start();
        }
    }

    private void closeView() {
        for (int i = 0; i < mViewsArr.length; i++) {
            AnimatorSet set = new AnimatorSet();
            double x = -SystemUtil.dp2px(mContext, dp) * (i + 1);
            set.playTogether(ObjectAnimator.ofFloat(mViewsArr[i], "translationX", (float) x, 0)
                    , ObjectAnimator.ofFloat(mViewsArr[i], "alpha", 1, 0).setDuration(1000L));
            set.setInterpolator(new DecelerateInterpolator());
            set.setDuration(300L).setStartDelay(50L * i);
            set.start();
        }
    }

    private OnMapStyleSelectedListener onMapStyleSelectedListener;

    public void setOnMapStyleSelectedListener(OnMapStyleSelectedListener listener) {
        this.onMapStyleSelectedListener = listener;
    }

    public interface OnMapStyleSelectedListener {
        void mapStyle(int resourceId);
    }
}
