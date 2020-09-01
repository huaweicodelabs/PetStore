/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  2020.1.3-Changed modify the import classes type and add some marker clustering demos.
 *                  Huawei Technologies Co., Ltd.
 *
 */

package com.huawei.hmspetstore.ui.petstore;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;

import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.util.LogM;
import com.huawei.hms.site.api.SearchResultListener;
import com.huawei.hms.site.api.SearchService;
import com.huawei.hms.site.api.SearchServiceFactory;
import com.huawei.hms.site.api.model.Coordinate;
import com.huawei.hms.site.api.model.QuerySuggestionRequest;
import com.huawei.hms.site.api.model.QuerySuggestionResponse;
import com.huawei.hms.site.api.model.SearchStatus;
import com.huawei.hms.site.api.model.Site;
import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.bean.Place;
import com.huawei.hmspetstore.common.IRecyclerItemListener;
import com.huawei.hmspetstore.constant.SPConstants;
import com.huawei.hmspetstore.ui.petstore.adapter.PetStoreSearchAdapter;
import com.huawei.hmspetstore.util.SystemUtil;
import com.huawei.hmspetstore.util.ToastUtil;
import com.huawei.hmspetstore.util.Utils;
import com.huawei.hmspetstore.view.DividerItemDecoration;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PetStoreSearchActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "PetStoreSearchActivity";

    private static final int GETAUTOCOMPLETE_SUCCESS = 0;

    /**
     * 搜索服务
     */
    private SearchService mSearchService;

    /**
     * 搜索半径，默认1km
     */
    private String mQueryRadius;

    /**
     * 搜索结果
     */
    private List<Place> mPlaces = new ArrayList<>();

    /**
     * 店铺列表适配器
     */
    private PetStoreSearchAdapter mPetStoreAdapter;

    /**
     * widget
     */
    private RecyclerView mRecyclerView;
    private SearchView mSearchView;
    private ImageView mImgBack;
    private ContentLoadingProgressBar mPbLoading;

    private Handler mHandler = new RefreshHandler(this);

    private static class RefreshHandler extends Handler {
        private WeakReference<PetStoreSearchActivity> activityHolder;

        public RefreshHandler(PetStoreSearchActivity activity) {
            activityHolder = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            PetStoreSearchActivity activity = activityHolder.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }

            if (msg.what == GETAUTOCOMPLETE_SUCCESS) {
                activity.updatePetstoreList();
            }
        }
    }

    /**
     * 搜索回调
     */
    private SearchResultListener searchResultListener = new SearchResultListener<QuerySuggestionResponse>() {
        @Override
        public void onSearchResult(QuerySuggestionResponse results) {
            if (results != null) {
                List<Site> sites = results.getSites();
                generatePlaces(sites);
            }
        }

        @Override
        public void onSearchError(SearchStatus status) {
            LogM.e(TAG, "failed " + status.getErrorCode() + " " + status.getErrorMessage());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_petstore_search);
        // 提示进入的Kit
        ToastUtil.getInstance().showShort(this, R.string.toast_site);
        mSearchService = SearchServiceFactory.create(this, SystemUtil.getApiKey(this));
        initView();
        initRecyclerView();
        setSearchViewEvents();
        mSearchView.setQuery("petstore", false);//增加默认搜索关键字
    }

    /**
     * 控件绑定
     */
    private void initView() {
        mRecyclerView = findViewById(R.id.petstore_recyclerView);
        mSearchView = findViewById(R.id.mainPage_searchView);
        mImgBack = findViewById(R.id.mainPage_back);
        mPbLoading = findViewById(R.id.pb_loading);
        mPbLoading.setVisibility(View.VISIBLE);
        mPbLoading.show();

        mRecyclerView.setVisibility(View.GONE);
        mImgBack.setOnClickListener(this);

        mQueryRadius = getQueryRadiusStr(0);
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));//设置布局管理器
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this));//设置分隔线
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mPetStoreAdapter = new PetStoreSearchAdapter();//设置Adapter
        mRecyclerView.setAdapter(mPetStoreAdapter);
        mPetStoreAdapter.setListener(new IRecyclerItemListener() {
            @Override
            public void onItemClick(View view, int position) {
                Place place = mPetStoreAdapter.getItem(position);
                if (place == null) {
                    return;
                }
                try {
                    Intent intent = new Intent(PetStoreSearchActivity.this, PetStoreSearchDetailActivity.class);
                    intent.putExtra("place_item", place);
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Log.e(TAG, "PetStoreSearchActivity onItemClick ActivityNotFoundException");
                }
            }
        });
    }

    /**
     * 返回所选半径
     *
     * @param position position
     * @return radius
     */
    private String getQueryRadiusStr(int position) {
        String[] spinnerArr = getResources().getStringArray(R.array.spinner_list);
        return spinnerArr[position];
    }

    /**
     * 搜索监听回调
     */
    private void setSearchViewEvents() {
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // 当点击搜索按钮时触发该方法
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (TextUtils.isEmpty(query)) {
                    return false;
                }

                try {
                    Intent intent = new Intent(PetStoreSearchActivity.this, PetStoreSearchDetailActivity.class);
                    intent.putParcelableArrayListExtra("place_list", (ArrayList<? extends Parcelable>) mPlaces);
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Log.e(TAG, "PetStoreSearchActivity onQueryTextSubmit ActivityNotFoundException");
                }
                return false;
            }

            // 当搜索内容改变时触发该方法
            @Override
            public boolean onQueryTextChange(final String newText) {
                if (TextUtils.isEmpty(newText)) {
                    // 展示底层布局，隐藏listview
                    mRecyclerView.setVisibility(View.GONE);
                    return false;
                }
                Log.e(TAG, "PetStoreSearchActivity onQueryTextChange: ");
                getAutoCompleteResult(newText);
                return false;
            }
        });
    }

    /**
     * 搜索发起
     *
     * @param input 搜索关键字
     */
    private void getAutoCompleteResult(String input) {
        mPbLoading.setVisibility(View.VISIBLE);
        mPbLoading.show();
        QuerySuggestionRequest request = new QuerySuggestionRequest();
        request.setQuery(input);//搜索关键字
        Integer radiusValue = Utils.parseInt(mQueryRadius.replace("km", ""));
        LogM.e(TAG, "radiusValue: " + radiusValue);
        request.setRadius(radiusValue == null ? 1000 : radiusValue * 1000);//搜索半径
        request.setLocation(SPConstants.COORDINATE);//当前定位位置经纬度

        mSearchService.querySuggestion(request, searchResultListener);
    }

    private void generatePlaces(List<Site> resultList) {
        mPlaces.clear();
        for (Site bean : resultList) {
            Place place = new Place();
            place.setSiteId(bean.getSiteId());
            place.setName(bean.getName());
            place.setFormatAddress(bean.getFormatAddress());
            place.setDistance(bean.getDistance());
            Coordinate coordinate = bean.getLocation();
            place.setLatLng(new LatLng(coordinate.getLat(), coordinate.getLng()));
            mPlaces.add(place);
        }

        // 通知主线程更新UI
        Message message = Message.obtain();
        message.what = GETAUTOCOMPLETE_SUCCESS;
        mHandler.sendMessage(message);
    }

    /**
     * 更新数据并刷新Adapter
     */
    private void updatePetstoreList() {
        mPetStoreAdapter.replaceAll(mPlaces);
        mRecyclerView.setVisibility(View.VISIBLE);
        mPbLoading.hide();
        mPbLoading.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.mainPage_back) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSearchService = null;
    }
}