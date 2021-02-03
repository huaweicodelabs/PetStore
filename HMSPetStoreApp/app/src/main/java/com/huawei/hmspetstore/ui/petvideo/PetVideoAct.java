package com.huawei.hmspetstore.ui.petvideo;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.ui.petvideo.fragment.CollectionFragment;
import com.huawei.hmspetstore.ui.petvideo.fragment.NewFragment;
import com.huawei.hmspetstore.ui.petvideo.fragment.RecommendFragment;
import com.huawei.hmspetstore.util.ToastUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 宠物视频
 */
public class PetVideoAct extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.petvideo_act);
        // 初始化View
        initView();
    }

    /**
     * 初始化View
     */
    private void initView() {
        // 顶部返回
        ImageView mIvBack = findViewById(R.id.title_back);
        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 返回
                finish();
            }
        });
        initFragment();
    }

    /**
     * 初始化Fragment
     */
    private void initFragment() {
        TabLayout tabLayout = findViewById(R.id.petvideo_tabLayout);
        ViewPager viewPager = findViewById(R.id.petvideo_viewPager);
        VideoPagerAdapter myPagerAdapter = new VideoPagerAdapter(getSupportFragmentManager());
        myPagerAdapter.add(CollectionFragment.newInstance());
        myPagerAdapter.add(RecommendFragment.newInstance());
        myPagerAdapter.add(NewFragment.newInstance());

        viewPager.setAdapter(myPagerAdapter);
        VideoChangeListener mPageChangeListener = new VideoChangeListener();
        viewPager.addOnPageChangeListener(mPageChangeListener);
        tabLayout.setupWithViewPager(viewPager);

        String first_name = getString(R.string.pet_video_collection);
        Objects.requireNonNull(tabLayout.getTabAt(0)).setText(first_name);

        String second_name = getString(R.string.pet_video_recommend);
        Objects.requireNonNull(tabLayout.getTabAt(1)).setText(second_name);

        String third_name = getString(R.string.pet_video_new);
        Objects.requireNonNull(tabLayout.getTabAt(2)).setText(third_name);
    }

    static class VideoPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> mFragments = new ArrayList<>();

        public VideoPagerAdapter(FragmentManager fm) {
            super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        public void add(Fragment fm) {
            mFragments.add(fm);
        }

        public void clear() {
            if (mFragments != null && mFragments.size() > 0) {
                mFragments.clear();
            }
        }

        @NotNull
        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return super.getPageTitle(position);
        }
    }

    static class VideoChangeListener implements ViewPager.OnPageChangeListener {
        private int position = 0;

        public int getPosition() {
            return position;
        }

        public void onPageSelected(int position) {
            this.position = position;
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }
    }
}