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

public class RoutePlanningExpandView extends FrameLayout implements View.OnClickListener {
    private Context mContext;
    private View mViewRouteWalking;
    private View mViewRouteBicycling;
    private View mViewRouteDriving;
    private View mViewRoot;
    private boolean mIsShowing = false;
    private float dp = 50;
    private View[] mViewsArr;

    public RoutePlanningExpandView(@NonNull Context context) {
        this(context, null);
    }

    public RoutePlanningExpandView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoutePlanningExpandView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    private void initView() {
        View mContentView = View.inflate(mContext, R.layout.layout_route_planning, this);
        mViewRoot = mContentView.findViewById(R.id.view_root);
        mViewRouteWalking = mContentView.findViewById(R.id.img_route_walking);
        mViewRouteBicycling = mContentView.findViewById(R.id.img_route_bicycling);
        mViewRouteDriving = mContentView.findViewById(R.id.img_route_driving);

        mViewRoot.setOnClickListener(this);
        mViewRouteWalking.setOnClickListener(this);
        mViewRouteBicycling.setOnClickListener(this);
        mViewRouteDriving.setOnClickListener(this);

        mViewsArr = new View[]{mViewRouteDriving, mViewRouteBicycling, mViewRouteWalking};
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_root:
                switchView();
                break;
            case R.id.img_route_walking:
                updateBackground(R.id.img_route_walking);
                if (onSelectedListener != null) {
                    onSelectedListener.routeWalking();
                }
                break;
            case R.id.img_route_bicycling:
                updateBackground(R.id.img_route_bicycling);
                if (onSelectedListener != null) {
                    onSelectedListener.routeBicycling();
                }
                break;
            case R.id.img_route_driving:
                updateBackground(R.id.img_route_driving);
                if (onSelectedListener != null) {
                    onSelectedListener.routeDriving();
                }
                break;
            default:
                break;
        }
    }

    private void updateBackground(int id) {
        mViewRouteWalking.setSelected(id == R.id.img_route_walking);
        mViewRouteBicycling.setSelected(id == R.id.img_route_bicycling);
        mViewRouteDriving.setSelected(id == R.id.img_route_driving);
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

    private OnSelectedListener onSelectedListener;

    public void setOnSelectedListener(OnSelectedListener listener) {
        this.onSelectedListener = listener;
    }

    public interface OnSelectedListener {
        void routeWalking();

        void routeBicycling();

        void routeDriving();
    }
}