package com.huawei.hmspetstore.ui.petvideo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.bean.VideoBean;
import com.huawei.hmspetstore.common.IRecyclerItemListener;
import com.huawei.hmspetstore.constant.SPConstants;
import com.huawei.hmspetstore.ui.petvideo.VideoPlayAct;
import com.huawei.hmspetstore.ui.petvideo.adapter.PetVideoAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * New
 */
public class NewFragment extends Fragment {

    // 列表
    private RecyclerView mRecyclerView = null;

    // 列表数据
    private List<VideoBean> mItemData = new ArrayList<>();

    public static NewFragment newInstance() {
        return new NewFragment();
    }

    public NewFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.petvideo_item_fg, container, false);
        mRecyclerView = view.findViewById(R.id.petvideo_item_recyclerView);
        // 设置列表数据
        initRecyclerData();
        // 初始化RecyclerView
        initRecyclerView();
        return view;
    }

    /**
     * 设置列表数据
     */
    private void initRecyclerData() {
        mItemData.clear();
        VideoBean videoBean = new VideoBean(getString(R.string.video_collection_1), "");
        mItemData.add(videoBean);
        VideoBean videoBean2 = new VideoBean(getString(R.string.video_collection_2), "");
        mItemData.add(videoBean2);
        VideoBean videoBean3 = new VideoBean(getString(R.string.video_collection_3), "");
        mItemData.add(videoBean3);
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        //设置布局管理器
        mRecyclerView.setLayoutManager(manager);
        //设置为垂直布局，这也是默认的
        manager.setOrientation(RecyclerView.VERTICAL);
        //设置Adapter
        PetVideoAdapter mPetVideoAdapter = new PetVideoAdapter(mItemData);
        mRecyclerView.setAdapter(mPetVideoAdapter);
        mPetVideoAdapter.setListener(new IRecyclerItemListener() {
            @Override
            public void onItemClick(View view, int position) {
                onAdapterItemClick(position);
            }
        });
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    /**
     * 列表点击事件
     */
    private void onAdapterItemClick(int position) {
        VideoBean videoBean = mItemData.get(position);
        Bundle bundle = new Bundle();
        bundle.putSerializable(SPConstants.BUNDLE_DATA, videoBean);
        Intent intent = new Intent(getContext(), VideoPlayAct.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}