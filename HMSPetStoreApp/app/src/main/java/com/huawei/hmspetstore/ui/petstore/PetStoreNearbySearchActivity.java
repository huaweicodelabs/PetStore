package com.huawei.hmspetstore.ui.petstore;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.huawei.hms.maps.CameraUpdateFactory;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.MapView;
import com.huawei.hms.maps.OnMapReadyCallback;
import com.huawei.hms.maps.model.BitmapDescriptorFactory;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.LatLngBounds;
import com.huawei.hms.maps.model.Marker;
import com.huawei.hms.maps.model.MarkerOptions;
import com.huawei.hms.maps.util.AssetBitmapDescriptor;
import com.huawei.hms.site.api.SearchResultListener;
import com.huawei.hms.site.api.SearchService;
import com.huawei.hms.site.api.SearchServiceFactory;
import com.huawei.hms.site.api.model.Coordinate;
import com.huawei.hms.site.api.model.NearbySearchRequest;
import com.huawei.hms.site.api.model.NearbySearchResponse;
import com.huawei.hms.site.api.model.SearchStatus;
import com.huawei.hms.site.api.model.Site;
import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.bean.Place;
import com.huawei.hmspetstore.util.SystemUtil;
import com.huawei.hmspetstore.util.ToastUtil;
import com.huawei.hmspetstore.util.Utils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;

import org.jetbrains.annotations.NotNull;

/**
 * 宠物商店-搜周边
 */
public class PetStoreNearbySearchActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "PetStoreNearbySearchActivity";
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    private HuaweiMap hMap;
    private SearchService mSearchService;
    private BottomSheetBehavior mBehavior;

    private ImageView mImgBack;
    private SearchView mSearchView;
    private MapView mMapView;

    private CoordinatorLayout mCoordinatorLayout;
    private NestedScrollView mBottomSheet;
    private ListView mListView;

    private BaseAdapter mBaseAdapter;

    private Place mCurrentPlace;
    private Site mNearbySite;
    private Marker mNearbyMarker;
    private String mSearchText = "";

    private InputMethodManager mImm;

    /**
     * mCoordinatorLayout的最大高度，即mMapView的最大高度
     */
    private int mMaxHeight = 0;
    /**
     * 上一次mBehavior状态为STATE_COLLAPSED时，mBehavior的peekHeight
     */
    private int mLastPeekHeight = 0;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (null != mNearbySite) {
                        addMarker(mNearbySite);
                        mNearbySite = null;
                    }
                    break;
                case 1:
                    if (null != hMap && null != mCurrentPlace) {
                        hMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentPlace.getLatLng(), 15));
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_petstore_searbysearch);
        mSearchService = SearchServiceFactory.create(this, SystemUtil.getApiKey(this));
        mCurrentPlace = getIntent().getParcelableExtra("place_item");

        initView(savedInstanceState);
        setSearchViewEvents();

        // 提示进入的Kit
        ToastUtil.getInstance().showShort(this, R.string.toast_site_map);
    }

    private void initView(Bundle savedInstanceState) {
        mImgBack = findViewById(R.id.img_title_back);
        mSearchView = findViewById(R.id.searchView);
        mMapView = findViewById(R.id.mapview_petstore_nearbysearch);
        mCoordinatorLayout = findViewById(R.id.coordinatorLayout);
        mBottomSheet = findViewById(R.id.ll_content_bottom_sheet);
        mListView = findViewById(R.id.listView);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);

        mImm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);

        mCoordinatorLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mCoordinatorLayout.getHeight() > mMaxHeight) {
                    mMaxHeight = mCoordinatorLayout.getHeight();
                } else if (mCoordinatorLayout.getHeight() < mMaxHeight) {
                    if (mImm.isActive()) {
                        if (mBehavior.getPeekHeight() > 0 && mBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                            mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }
                    }
                }
            }
        });

        mBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBehavior.setSkipCollapsed(false);
        mBehavior.setHideable(false);
        mBehavior.setPeekHeight(0);
        mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    if (mLastPeekHeight == 0 && mBehavior.getPeekHeight() > 0) {
                        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mMapView.getLayoutParams();
                        params.height = mMaxHeight / 2;
                        mMapView.setLayoutParams(params);
                    } else if (mLastPeekHeight > 0 && mBehavior.getPeekHeight() == 0) {
                        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mMapView.getLayoutParams();
                        params.height = CoordinatorLayout.LayoutParams.MATCH_PARENT;
                        mMapView.setLayoutParams(params);
                        mHandler.sendEmptyMessageDelayed(1, 100);
                    }
                    mHandler.sendEmptyMessageDelayed(0, 100);
                    mLastPeekHeight = mBehavior.getPeekHeight();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        mImgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onMapReady(HuaweiMap huaweiMap) {
        hMap = huaweiMap;
        setCurrentPlace();
        nearbySearch(null);
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private void setSearchViewEvents() {
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // 当点击搜索按钮时触发该方法
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.equals(mSearchText)) {
                    return false;
                }
                mSearchText = query;
                if (TextUtils.isEmpty(query)) {
                    // 展示底层布局，隐藏listview
                    hideBottomSheet();
                    return false;
                }

                nearbySearch(query);
                return false;
            }

            // 当搜索内容改变时触发该方法
            @Override
            public boolean onQueryTextChange(final String newText) {
                mSearchText = newText;
                if (TextUtils.isEmpty(newText)) {
                    // 展示底层布局，隐藏listview
                    hideBottomSheet();
                    return false;
                }
                nearbySearch(newText);
                return false;
            }
        });
    }

    private void nearbySearch(final String query) {
        if (null == mCurrentPlace || (null != query && TextUtils.isEmpty(query))) {
            return;
        }
        NearbySearchRequest request = new NearbySearchRequest();
        LatLng latLng = mCurrentPlace.getLatLng();
        double lat = latLng.latitude;
        double lng = latLng.longitude;
        Coordinate location = new Coordinate(lat, lng);
        request.setLocation(location);
        if (!TextUtils.isEmpty(query)) {
            request.setQuery(query);
        }

        SearchResultListener<NearbySearchResponse> resultListener = new SearchResultListener<NearbySearchResponse>() {
            // Return search results upon a successful search.
            @Override
            public void onSearchResult(NearbySearchResponse results) {
                if (null != query && (TextUtils.isEmpty(mSearchText) || !query.equals(mSearchText))) {
                    return;
                }

                List<Site> sites;
                if (null != results && null != (sites = results.getSites()) && sites.size() > 0) {
                    resolveResult(sites, query);
                } else {
                    hideBottomSheet();
                }
            }

            // Return the result code and description upon a search exception.
            @Override
            public void onSearchError(SearchStatus status) {
                if (null == query || TextUtils.isEmpty(mSearchText) || !query.equals(mSearchText)) {
                    return;
                }
                hideBottomSheet();
            }
        };

        // Call the nearby place search API.
        mSearchService.nearbySearch(request, resultListener);
    }

    private void resolveResult(final List<Site> sites, String query) {
        mBaseAdapter = new BaseAdapter() {

            @Override
            public int getCount() {
                return null == sites ? 0 : sites.size();
            }

            @Override
            public Object getItem(int position) {
                return sites.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                @SuppressLint("ViewHolder") View layout = View.inflate(PetStoreNearbySearchActivity.this, R.layout.petstore_nearby_search_item, null);
                TextView tvName = layout.findViewById(R.id.petstore_item_name);
                TextView tvDesc = layout.findViewById(R.id.petstore_item_desc);

                Site site = sites.get(position);
                tvName.setText(site.getName());
                tvDesc.setText(site.getFormatAddress());
                return layout;
            }
        };

        mListView.setAdapter(mBaseAdapter);
        mBehavior.setPeekHeight(mMaxHeight / 2);
        if (null == query) {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mMapView.getLayoutParams();
            params.height = mMaxHeight / 2;
            mMapView.setLayoutParams(params);
            mLastPeekHeight = mBehavior.getPeekHeight();
        } else {
            mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                hideSoftKeyboard(PetStoreNearbySearchActivity.this);
                mNearbySite = sites.get(position);

                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        if (mBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {//不会触发onStateChanged()方法
                            mHandler.sendEmptyMessageDelayed(0, 100);
                        }

                        mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                }, 100);
            }
        });
    }

    /**
     * set current place
     */
    private void setCurrentPlace() {
        if (null != mCurrentPlace) {
            LatLng latLng = mCurrentPlace.getLatLng();
            hMap.addMarker(getMarkerOptions(latLng, mCurrentPlace.getName())).setTag(mCurrentPlace.getSiteId());
            hMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }
    }

    private MarkerOptions getMarkerOptions(LatLng position, String name) {
        return new MarkerOptions()
                .position(position)
                .anchor(0.5f, 0.9f)
                .anchorMarker(0.5f, 0.9f)
                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmap(name)));
    }

    /**
     * 获取自定义Marker的Bitmap对象
     *
     * @param name
     * @return
     */
    private Bitmap getMarkerBitmap(String name) {
        View mView = getLayoutInflater().inflate(R.layout.custom_marker, null);
        setMarkerTextView(mView, Utils.getRightString(name));
        return convertViewToBitmap(mView);
    }

    private void setMarkerTextView(View view, String markerTitle) {
        TextView titleView = null;

        Object object = view.findViewById(R.id.txtv_titlee);
        ImageView image = view.findViewById(R.id.imgv_marker);
        if (object instanceof TextView) {
            titleView = (TextView) object;
        }
        if (titleView != null) {
            if (TextUtils.isEmpty(markerTitle)) {
                titleView.setText("");
            } else {
                titleView.setText(markerTitle);
            }
        }
        // 保证Marker尺寸和sdk默认的一致
        Bitmap bitmap = (new AssetBitmapDescriptor("images/marker_blue.png")).generateBitmap(this);
        image.setImageBitmap(bitmap);
    }

    /**
     * view转bitmap
     */
    public static Bitmap convertViewToBitmap(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        return view.getDrawingCache();
    }

    /**
     * add marker
     */
    private void addMarker(Site site) {
        if (null != site) {
            removeMarker();
            Coordinate coordinate = site.getLocation();
            LatLng latLng = new LatLng(coordinate.getLat(), coordinate.getLng());
            mNearbyMarker = hMap.addMarker(new MarkerOptions().position(latLng).title(site.getName()).snippet(site.getFormatAddress()));
            mNearbyMarker.setTag(site.getSiteId());

            LatLngBounds.Builder builder = new LatLngBounds.Builder();//存放起点和终点的经纬度
            builder.include(latLng);
            builder.include(mCurrentPlace.getLatLng());
            hMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), SystemUtil.dp2px(PetStoreNearbySearchActivity.this, 80)));
        }
    }

    private void removeMarker() {
        if (null != mNearbyMarker) {
            mNearbyMarker.remove();
            mNearbyMarker = null;
        }
    }

    private void hideBottomSheet() {
        removeMarker();

        if (mBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED && mLastPeekHeight > 0) {//不会触发onStateChanged()方法
            mLastPeekHeight = 0;
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mMapView.getLayoutParams();
            params.height = CoordinatorLayout.LayoutParams.MATCH_PARENT;
            mMapView.setLayoutParams(params);
            mHandler.sendEmptyMessageDelayed(1, 200);
        }

        mBehavior.setPeekHeight(0);
        mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    /**
     * 关闭键盘输入法
     */
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View v = activity.getWindow().peekDecorView();
        if (null != v) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
}