package com.huawei.hmspetstore.ui.petstore;

import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huawei.hmf.tasks.OnCompleteListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.location.Geofence;
import com.huawei.hms.location.GeofenceRequest;
import com.huawei.hms.location.GeofenceService;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.maps.CameraUpdate;
import com.huawei.hms.maps.CameraUpdateFactory;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.MapView;
import com.huawei.hms.maps.OnMapReadyCallback;
import com.huawei.hms.maps.model.BitmapDescriptorFactory;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.LatLngBounds;
import com.huawei.hms.maps.model.MapStyleOptions;
import com.huawei.hms.maps.model.Marker;
import com.huawei.hms.maps.model.MarkerOptions;
import com.huawei.hms.maps.model.Polyline;
import com.huawei.hms.maps.model.PolylineOptions;
import com.huawei.hms.maps.util.AssetBitmapDescriptor;
import com.huawei.hms.maps.util.LogM;
import com.huawei.hms.site.api.SearchResultListener;
import com.huawei.hms.site.api.SearchService;
import com.huawei.hms.site.api.SearchServiceFactory;
import com.huawei.hms.site.api.model.DetailSearchRequest;
import com.huawei.hms.site.api.model.DetailSearchResponse;
import com.huawei.hms.site.api.model.SearchStatus;
import com.huawei.hms.site.api.model.Site;
import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.bean.Place;
import com.huawei.hmspetstore.constant.SPConstants;
import com.huawei.hmspetstore.network.NetClient;
import com.huawei.hmspetstore.network.NetworkRequestManager;
import com.huawei.hmspetstore.ui.petstore.receiver.GeoFenceBroadcastReceiver;
import com.huawei.hmspetstore.util.SystemUtil;
import com.huawei.hmspetstore.util.ToastUtil;
import com.huawei.hmspetstore.util.Utils;
import com.huawei.hmspetstore.view.MapStyleExpandView;
import com.huawei.hmspetstore.view.RoutePlanningExpandView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_WIFI_STATE;

/**
 * PetStore details
 */
public class PetStoreSearchDetailActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {
    private static final String TAG = "PetStoreSearchDetail";

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    /**
     * 路径规划-成功
     */
    private static final int ROUTE_PLANNING_COMPLETE_SUCCESS = 0x01;

    /**
     * 路径规划-失败
     */
    private static final int ROUTE_PLANNING_COMPLETE_FAILED = 0x02;

    /**
     * 搜索服务
     */
    private SearchService searchService;

    /**
     * widget
     */
    private ImageView mIvBack;

    private MapView mMapView;

    private TextView mTvPetStoreName;

    private TextView mTvPetStoreCollection;

    private TextView mTvPetStoreDescription;

    private ImageView mIvSearchNearby;

    private RoutePlanningExpandView mViewRoutePlanning;

    private MapStyleExpandView mViewMapStyle;

    // 获取PendingIntent及回调结果的方式可自定义，这里以广播为例进行说明
    private PendingIntent pendingIntent;

    /**
     * 华为地图对象
     */
    private HuaweiMap hMap;

    /**
     * 当多地址进入时，mPlacesList接收Intent传值
     */
    private List<Place> mPlacesList;

    /**
     * 围栏服务客户端
     */
    private GeofenceService geofenceService;

    /**
     * 收藏按钮点击状态
     */
    private boolean isClickedCollection = false;

    /**
     * 围栏列表
     */
    private ArrayList<Geofence> geofenceList = new ArrayList<>();

    // 保存地理围栏id信息
    private ArrayList<String> idList;

    /**
     * 当单地址进入时，mPlace接收Intent传值
     */
    private Place mPlace;

    /**
     * 路径起点位置的Marker
     */
    private Marker mMarkerOrigin;

    /**
     * 路径数据
     */
    private List<List<LatLng>> mPaths = new ArrayList<>();

    /**
     * 路径折线集合
     */
    private List<Polyline> mPolylines = new ArrayList<>();

    /**
     * 路径规划所在的矩形区域
     */
    private LatLngBounds mLatLngBounds;
    /**
     * 时间和距离的布局
     */
    private LinearLayout mTimeDistance;
    /**
     * 时间
     */
    private TextView mTime;
    /**
     * 距离
     */
    private TextView mDistance;

    /**
     * 两点之间的距离
     */
    private String distanceText;

    /**
     * 两点之间时间
     */
    private String durationText;

    private Handler mHandler = new RefreshHandler(this);

    private static class RefreshHandler extends Handler {
        private WeakReference<PetStoreSearchDetailActivity> activityHolder;

        public RefreshHandler(PetStoreSearchDetailActivity activity) {
            activityHolder = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            PetStoreSearchDetailActivity activity = activityHolder.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }

            switch (msg.what) {
                case ROUTE_PLANNING_COMPLETE_SUCCESS:
                    activity.renderRoute();
                    //显示距离和时间的布局
                    activity.showTimeDistanceLayout();
                    break;
                case ROUTE_PLANNING_COMPLETE_FAILED:
                    Bundle bundle = msg.getData();
                    String errorMsg = bundle.getString("errorMsg");
                    ToastUtil.getInstance().showShort(activity, errorMsg);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 搜索回调
     */
    private SearchResultListener<DetailSearchResponse> resultListener =
            new SearchResultListener<DetailSearchResponse>() {
                @Override
                public void onSearchResult(DetailSearchResponse results) {
                    Site site;
                    if (results == null || (site = results.getSite()) == null) {
                        return;
                    }
                    mTvPetStoreName.setText(site.getName());
                    mTvPetStoreDescription.setText(site.getFormatAddress());
                }

                @Override
                public void onSearchError(SearchStatus status) {
                    LogM.e(TAG, "failed " + status.getErrorCode() + " " + status.getErrorMessage());
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogM.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_petstore_detail);
        NetClient.getNetClient().init(this.getApplicationContext());
        searchService = SearchServiceFactory.create(this, SystemUtil.getApiKey(this));

        initIntent();
        initView();
        initListener();
        initMapView(savedInstanceState);
        geofenceService = LocationServices.getGeofenceService(this);

        // 提示进入的Kit
        ToastUtil.getInstance().showShort(this, R.string.toast_site_map_location);
    }

    private void initIntent() {
        if (null != getIntent()) {
            mPlace = getIntent().getParcelableExtra("place_item");
            if (mPlace == null) {
                mPlacesList = getIntent().getParcelableArrayListExtra("place_list");
            }
        }
    }

    /**
     * 地图初始化
     *
     * @param savedInstanceState 存储的数据对象
     */
    private void initMapView(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);
    }


    /**
     * 控件绑定
     */
    private void initView() {
        mIvBack = findViewById(R.id.title_back);
        mMapView = findViewById(R.id.mapview_mapviewdemo);
        mTvPetStoreName = findViewById(R.id.petstore_name);
        mIvSearchNearby = findViewById(R.id.img_search_nearby);
        mTvPetStoreCollection = findViewById(R.id.petstore_collection);
        mTvPetStoreDescription = findViewById(R.id.petstore_description);
        mViewRoutePlanning = findViewById(R.id.view_route_planning);
        mViewMapStyle = findViewById(R.id.view_map_style);
        mTimeDistance = findViewById(R.id.ll_time_distance);
        mTime = findViewById(R.id.tv_time);
        mDistance = findViewById(R.id.tv_distance);
    }

    /**
     * 监听器设置
     */
    private void initListener() {
        mIvBack.setOnClickListener(this);
        mIvSearchNearby.setOnClickListener(this);
        mTvPetStoreCollection.setOnClickListener(this);

        // 自定义样式
        mViewMapStyle.setOnMapStyleSelectedListener(new MapStyleExpandView.OnMapStyleSelectedListener() {
            @Override
            public void mapStyle(int resourceId) {
                MapStyleOptions style = resourceId == -1 ? null
                        : MapStyleOptions.loadRawResourceStyle(PetStoreSearchDetailActivity.this, resourceId);
                hMap.setMapStyle(style);
            }
        });

        // 路径规划
        mViewRoutePlanning.setOnSelectedListener(new RoutePlanningExpandView.OnSelectedListener() {
            @Override
            public void routeWalking() { // 步行路径规划
                if (mPlace == null) {
                    return;
                }
                LatLng latLngOrigin = new LatLng(SPConstants.COORDINATE_WALKING.getLat(), SPConstants.COORDINATE_WALKING.getLng());
                LatLng latLngDestination = mPlace.getLatLng();
                LogM.e(TAG,
                        "latLngOrigin: " + latLngOrigin.toString() + " latLngDestination:" + latLngDestination.toString());
                getWalkingRouteResult(latLngOrigin, latLngDestination);
            }

            @Override
            public void routeBicycling() { // 骑行路径规划
                if (mPlace == null) {
                    return;
                }
                LatLng latLngOrigin = new LatLng(SPConstants.COORDINATE_BICYCLING.getLat(), SPConstants.COORDINATE_BICYCLING.getLng());
                LatLng latLngDestination = mPlace.getLatLng();
                LogM.e(TAG,
                        "latLngOrigin: " + latLngOrigin.toString() + " latLngDestination:" + latLngDestination.toString());
                getBicyclingRouteResult(latLngOrigin, latLngDestination);
            }

            @Override
            public void routeDriving() { // 驾车路径规划
                if (mPlace == null) {
                    return;
                }
                LatLng latLngOrigin = new LatLng(SPConstants.COORDINATE.getLat(), SPConstants.COORDINATE.getLng());
                LatLng latLngDestination = mPlace.getLatLng();
                LogM.e(TAG,
                        "latLngOrigin: " + latLngOrigin.toString() + " latLngDestination:" + latLngDestination.toString());
                getDrivingRouteResult(latLngOrigin, latLngDestination);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.title_back: // 返回
                finish();
                break;
            case R.id.petstore_collection: // 收藏
                if (mPlace == null) {
                    Log.i(TAG, "onClick:mPlace is null");
                    return;
                }
                if (!isClickedCollection) {
                    requestGeoFenceWithNewIntent();
                } else {
                    removeGeoFenceWithID();
                }

                break;
            case R.id.img_search_nearby: // 搜周边
                if (mPlace == null) {
                    return;
                }
                try {
                    Intent intent = new Intent(PetStoreSearchDetailActivity.this, PetStoreNearbySearchActivity.class);
                    intent.putExtra("place_item", mPlace);
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    LogM.e(TAG, "ActivityNotFoundException nearby");
                }
                break;
            default:
                break;
        }
    }

    @RequiresPermission(allOf = {ACCESS_FINE_LOCATION, ACCESS_WIFI_STATE})
    @Override
    public void onMapReady(HuaweiMap map) {
        LogM.i(TAG, "onMapReady: ");
        if (map == null) {
            return;
        }
        hMap = map;
        hMap.getUiSettings().setMyLocationButtonEnabled(true);
        hMap.setMyLocationEnabled(true);
        addMarker();
    }

    /**
     * 发送添加地理围栏请求
     */
    private void requestGeoFenceWithNewIntent() {
        ToastUtil.getInstance().showShort(PetStoreSearchDetailActivity.this, getString(R.string.toast_collection));
        // 点击按钮之后修改按钮颜色
        isClickedCollection = true;
        mTvPetStoreCollection.setTextColor(getResources().getColor(R.color.Blue_600));
        Log.i(TAG, "begin to create Geofence");
        pendingIntent = getPendingIntent();
        geofenceList = new ArrayList<>();
        double latitude = mPlace.latLng.latitude;
        double longitude = mPlace.latLng.longitude;
        // 围栏半径1 km
        float radius = 1000;
        // 将当前收藏地点的位置下发围栏
        geofenceList.add(new Geofence.Builder().setUniqueId(mPlace.getSiteId())
                .setValidContinueTime(1000000)
                // 传入宠物商店信息，圆形地理围栏半径（单位:米）
                .setRoundArea(latitude, longitude, radius)
                // 进入围栏时触发回调
                .setConversions(Geofence.ENTER_GEOFENCE_CONVERSION)
                .build());
        geofenceService.createGeofenceList(getAddGeofenceRequest(), pendingIntent)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.i(TAG, "add geofence success！");
                        } else {
                            Log.w(TAG, "add geofence failed : " + task.getException().getMessage());
                        }
                    }
                });
    }

    private void removeGeoFenceWithID() {
        Log.i(TAG, "have clicked collection button" + isClickedCollection);
        ToastUtil.getInstance().showShort(PetStoreSearchDetailActivity.this, getString(R.string.toast_cancel_collection));
        // 取消收藏时，将收藏按钮恢复成正常状态
        mTvPetStoreCollection.setTextColor(getResources().getColor(R.color.Grey_700));
        isClickedCollection = false;
        // 移除地理围栏
        idList = new ArrayList<>();
        idList.add(mPlace.getSiteId());
        geofenceService.deleteGeofenceList(idList).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, "delete geofence with ID success！");
                } else {
                    Log.w(TAG, "delete geofence with ID failed ");
                }
            }
        });
    }

    /**
     * 创建地理围栏请求
     */
    private GeofenceRequest getAddGeofenceRequest() {
        GeofenceRequest.Builder builder = new GeofenceRequest.Builder();
        // 当用户在围栏中时，添加围栏后立即触发回调
        builder.setInitConversions(GeofenceRequest.ENTER_INIT_CONVERSION);
        builder.createGeofenceList(geofenceList);
        return builder.build();
    }

    /**
     * 创建PendingIntent
     *
     * @return PendIntent
     */
    private PendingIntent getPendingIntent() {
        // GeoFenceBroadcastReceiver类为自定义静态广播类，详细的实现方法可以参照示例代码。
        Intent intent = new Intent(this, GeoFenceBroadcastReceiver.class);
        intent.setAction(GeoFenceBroadcastReceiver.ACTION_PROCESS_LOCATION);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * 店铺地点绘制
     */
    private void addMarker() {
        if (mPlace != null) {
            // 单marker处理
            LatLng latLng = mPlace.getLatLng();
            hMap.addMarker(getMarkerOptions(latLng, mPlace.getName())).setTag(mPlace.getSiteId());
            hMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
            // 获取地址详情
            getPlaceDetail(mPlace.getSiteId());
        } else {
            // 多marker处理
            if (hMap != null && mPlacesList != null && mPlacesList.size() > 0) {
                LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();// 存放所有点的经纬度
                for (Place p : mPlacesList) {
                    LogM.e(TAG, "Place detail：" + p.toString());
                    LatLng latLng = p.getLatLng();
                    hMap.addMarker(getMarkerOptions(latLng, p.getName())).setTag(p.getSiteId());
                    boundsBuilder.include(latLng);// 把所有点都include进去（LatLng类型）
                }
                int padding = SystemUtil.dp2px(PetStoreSearchDetailActivity.this, 50);
                hMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), padding));// 第二个参数为边距padding
            }
        }
    }

    /**
     * 获取一个MarkerOptions
     *
     * @param position Marker坐标点
     * @param name     店铺名称
     * @return MarkerOptions
     */
    private MarkerOptions getMarkerOptions(LatLng position, String name) {
        MarkerOptions markerOptions = new MarkerOptions().position(position)
                .anchor(0.5f, 0.9f)
                .anchorMarker(0.5f, 0.9f)
                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmap(name)));
        return markerOptions;
    }

    /**
     * 获取自定义Marker的Bitmap对象
     *
     * @param name 地点名称
     * @return Bitmap
     */
    private Bitmap getMarkerBitmap(String name) {
        View mView = getLayoutInflater().inflate(R.layout.custom_marker, null);
        setMarkerTextView(mView, Utils.getRightString(name));
        return convertViewToBitmap(mView);
    }

    /**
     * view转bitmap
     *
     * @param view View
     * @return Bitmap
     */
    public static Bitmap convertViewToBitmap(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        return view.getDrawingCache();
    }

    /**
     * 设置自定义Marker信息
     *
     * @param view        自定义Marker布局
     * @param markerTitle Marker标题
     */
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
        Bitmap bitmap = (new AssetBitmapDescriptor("images/hwmap_marker_default.png")).generateBitmap(this);
        image.setImageBitmap(bitmap);
    }

    /**
     * 获取地点详情
     *
     * @param id 地点id
     */
    private void getPlaceDetail(String id) {
        DetailSearchRequest request = new DetailSearchRequest();
        request.setSiteId(id);
        searchService.detailSearch(request, resultListener);
    }

    // *****************************************路径规划内容-start *****************************************

    /**
     * 发起请求-步行路径规划
     * 仅提供100km以内的路径规划能力
     * 起点坐标与终点坐标的距离超过100KM，接口会报错
     *
     * @param latLngOrigin      起点坐标
     * @param latLngDestination 终点坐标
     */
    private void getWalkingRouteResult(LatLng latLngOrigin, LatLng latLngDestination) {
        removePolylines();
        NetworkRequestManager.getWalkingRoutePlanningResult(latLngOrigin, latLngDestination,
                new NetworkRequestManager.OnNetworkListener() {
                    @Override
                    public void requestSuccess(String result) {
                        Log.i(TAG, "Walking result=" + result);
                        generateRoute(result);
                    }

                    @Override
                    public void requestFail(String errorMsg) {
                        Log.i(TAG, "errorMsg=" + errorMsg);
                        Message msg = Message.obtain();
                        Bundle bundle = new Bundle();
                        bundle.putString("errorMsg", errorMsg);
                        msg.what = ROUTE_PLANNING_COMPLETE_FAILED;
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                    }
                });
    }

    /**
     * 发起请求-骑行路径规划
     * 仅提供100km以内的路径规划能力
     * 起点坐标与终点坐标的距离超过100KM，接口会报错
     *
     * @param latLngOrigin      起点坐标
     * @param latLngDestination 终点坐标
     */
    private void getBicyclingRouteResult(LatLng latLngOrigin, LatLng latLngDestination) {
        removePolylines();
        NetworkRequestManager.getBicyclingRoutePlanningResult(latLngOrigin, latLngDestination,
                new NetworkRequestManager.OnNetworkListener() {
                    @Override
                    public void requestSuccess(String result) {
                        generateRoute(result);
                    }

                    @Override
                    public void requestFail(String errorMsg) {
                        Message msg = Message.obtain();
                        Bundle bundle = new Bundle();
                        bundle.putString("errorMsg", errorMsg);
                        msg.what = ROUTE_PLANNING_COMPLETE_FAILED;
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                    }
                });
    }

    /**
     * 发起请求-驾车路径规划
     * 支持一次请求返回多条路线，最多支持3条路线
     * 最多支持5个途经点
     *
     * @param latLngOrigin      起点坐标
     * @param latLngDestination 终点坐标
     */
    private void getDrivingRouteResult(LatLng latLngOrigin, LatLng latLngDestination) {
        removePolylines();
        NetworkRequestManager.getDrivingRoutePlanningResult(latLngOrigin, latLngDestination,
                new NetworkRequestManager.OnNetworkListener() {
                    @Override
                    public void requestSuccess(String result) {
                        generateRoute(result);
                    }

                    @Override
                    public void requestFail(String errorMsg) {
                        Message msg = Message.obtain();
                        Bundle bundle = new Bundle();
                        bundle.putString("errorMsg", errorMsg);
                        msg.what = ROUTE_PLANNING_COMPLETE_FAILED;
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                    }
                });
    }


    /**
     * 路径规划请求返回数据解析处理
     *
     * @param json 返回数据json字符串
     */
    private void generateRoute(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray routes = jsonObject.optJSONArray("routes");
            if (null == routes || routes.length() == 0) {
                return;
            }
            JSONObject route = routes.getJSONObject(0);

            // get route bounds
            JSONObject bounds = route.optJSONObject("bounds");
            if (null != bounds && bounds.has("southwest") && bounds.has("northeast")) {
                JSONObject southwest = bounds.optJSONObject("southwest");
                JSONObject northeast = bounds.optJSONObject("northeast");
                assert southwest != null;
                LatLng sw = new LatLng(southwest.optDouble("lat"), southwest.optDouble("lng"));
                assert northeast != null;
                LatLng ne = new LatLng(northeast.optDouble("lat"), northeast.optDouble("lng"));
                mLatLngBounds = new LatLngBounds(sw, ne);
            }

            // get paths
            JSONArray paths = route.optJSONArray("paths");
            assert paths != null;
            for (int i = 0; i < paths.length(); i++) {
                JSONObject path = paths.optJSONObject(i);
                List<LatLng> mPath = new ArrayList<>();

                JSONArray steps = path.optJSONArray("steps");
                distanceText = path.getString("distanceText");
                durationText = path.getString("durationText");

                Log.i(TAG, "distanceText=" + distanceText);
                Log.i(TAG, "durationText=" + durationText);
                assert steps != null;
                for (int j = 0; j < steps.length(); j++) {
                    JSONObject step = steps.optJSONObject(j);

                    JSONArray polyline = step.optJSONArray("polyline");
                    assert polyline != null;
                    for (int k = 0; k < polyline.length(); k++) {
                        if (j > 0 && k == 0) {
                            continue;
                        }
                        JSONObject line = polyline.getJSONObject(k);
                        double lat = line.optDouble("lat");
                        double lng = line.optDouble("lng");
                        LatLng latLng = new LatLng(lat, lng);
                        mPath.add(latLng);
                    }
                }
                mPaths.add(i, mPath);
            }
            mHandler.sendEmptyMessage(ROUTE_PLANNING_COMPLETE_SUCCESS);

        } catch (JSONException e) {
            LogM.e(TAG, "generateRoute JSONException");
        }
    }

    /**
     * 显示距离和时间的布局
     */
    private void showTimeDistanceLayout() {
        mTimeDistance.setVisibility(View.VISIBLE);
        mTime.setText(durationText);
        mDistance.setText(distanceText);
    }

    /**
     * 准备绘制路径
     */
    private void renderRoute() {
        renderRoute(mPaths, mLatLngBounds);
    }

    /**
     * Render the route planning result
     *
     * @param paths        路径
     * @param latLngBounds Marker矩形区域
     */
    private void renderRoute(List<List<LatLng>> paths, LatLngBounds latLngBounds) {
        if (null == paths || paths.size() <= 0 || paths.get(0).size() <= 0) {
            return;
        }

        for (int i = 0; i < paths.size(); i++) {
            List<LatLng> path = paths.get(i);
            PolylineOptions options = new PolylineOptions().color(Color.BLUE).width(5);
            for (LatLng latLng : path) {
                options.add(latLng);
            }

            Polyline polyline = hMap.addPolyline(options);
            mPolylines.add(i, polyline);
        }

        // 绘制起点Marker
        addOriginMarker(paths.get(0).get(0));
        if (null != latLngBounds) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, 80);
            hMap.moveCamera(cameraUpdate);
        } else {
            LatLng latLngOrigin = new LatLng(SPConstants.COORDINATE.getLat(), SPConstants.COORDINATE.getLng());
            LatLng latLngDestination = mPlace.getLatLng();
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();// 存放起点和终点的经纬度
            boundsBuilder.include(latLngOrigin);// include起点
            boundsBuilder.include(latLngDestination);// include终点
            int padding = SystemUtil.dp2px(PetStoreSearchDetailActivity.this, 80);
            hMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), padding));// 第二个参数为边距padding
        }
    }

    /**
     * 绘制路径起点坐标
     *
     * @param latLng 起点经纬度
     */
    private void addOriginMarker(LatLng latLng) {
        if (null != mMarkerOrigin) {
            mMarkerOrigin.remove();
        }
        mMarkerOrigin =
                hMap.addMarker(new MarkerOptions().position(latLng).anchor(0.5f, 0.9f).anchorMarker(0.5f, 0.9f));
    }

    /**
     * 移除所有路径数据
     */
    private void removePolylines() {
        for (Polyline polyline : mPolylines) {
            polyline.remove();
        }

        mPolylines.clear();
        mPaths.clear();
        mLatLngBounds = null;
    }

    // *****************************************路径规划内容-end *****************************************

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
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
        searchService = null;
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}