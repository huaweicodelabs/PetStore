
package com.huawei.hmspetstore.ui.address;

import android.annotation.SuppressLint;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SwitchCompat;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.common.ResolvableApiException;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.LocationCallback;
import com.huawei.hms.location.LocationRequest;
import com.huawei.hms.location.LocationResult;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.location.LocationSettingsRequest;
import com.huawei.hms.location.LocationSettingsResponse;
import com.huawei.hms.location.LocationSettingsStatusCodes;
import com.huawei.hms.location.SettingsClient;
import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.bean.AddressBean;
import com.huawei.hmspetstore.constant.SPConstants;
import com.huawei.hmspetstore.util.SPUtil;
import com.huawei.hmspetstore.util.ToastUtil;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import static java.util.Locale.SIMPLIFIED_CHINESE;

/**
 * 添加收货地址界面
 */
public class AddressAct extends AppCompatActivity {

    private static final int GETLOCATIONINFO = 0;

    public static final String LOCATION = "currentLocation";

    public static final String TAG = "AddressAct";

//    public static final String AUTO_LOCATION = "自动定位";

    private FusedLocationProviderClient fusedLocationProviderClient;

    private LocationRequest mLocationRequest;

    private LocationCallback mLocationCallback;

    //输入姓名
    private EditText mEtName;
    //输入电话号码
    private EditText mEtPhone;
    //请输入地址
    private EditText mEtAddress;
    //自动定位
    private TextView mTvAutoLocation;
    //默认地址
    private SwitchCompat mDefaultAddress;
    //保存地址
    private AppCompatButton mSaveAddress;
    private String mAddress;

    private List<AddressBean> addressList;

    private AddressBean addressBean;

    private boolean isDefault = false;
    //收货地址
    private String deliveryAddress;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NotNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == GETLOCATIONINFO) {
                mAddress = msg.obj.toString();
                Log.i(TAG, mAddress);
                mTvAutoLocation.setText(mAddress);
                mEtAddress.setText(mAddress);
                SPUtil.put(getApplicationContext(), LOCATION, mAddress);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.address_act);
        // 提示进入的Kit
        ToastUtil.getInstance().showShort(this, R.string.toast_location);
        //获取地址管理数据
        addressList = SPUtil.getDataList(AddressAct.this, SPConstants.TAG_ADDRESS_LIST, AddressBean.class);
        // 初始化View
        initView();
        //点击事件
        clickEvent();
        mAddress = (String) SPUtil.get(getApplicationContext(), LOCATION, getString(R.string.auto_location));
        mTvAutoLocation.setText(mAddress);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setNumUpdates(1);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(false);
        builder.setNeedBle(false);
        LocationSettingsRequest locationSettingsRequest = builder.build();
        mLocationCallback = new LocationCallback() {
            // 定位结果回调
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Log.i(TAG, "onLocationResult locationResult is not null");
                    // 获取位置信息
                    List<Location> locations = locationResult.getLocations();
                    if (!locations.isEmpty()) {
                        // 获取最新的位置信息
                        Log.i(TAG, "onLocationResult location is not empty");
                        Location location = locations.get(0);
                        Log.i(TAG, "location[Longitude,Latitude,Accuracy]:" + location.getLongitude() + ","
                                + location.getLatitude() + "," + location.getAccuracy());
                        // 逆地理编码获取地址
                        final Geocoder geocoder = new Geocoder(AddressAct.this, SIMPLIFIED_CHINESE);
                        // 启用子线程调用逆地理编码能力，获取位置信息
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    List<Address> addrs =
                                            geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    // 地址信息更新成功之后，利用handler更新UI界面
                                    for (Address address : addrs) {
                                        Message msg = new Message();
                                        msg.what = GETLOCATIONINFO;
                                        msg.obj = addrs.get(0).getAddressLine(0);
                                        handler.sendMessage(msg);
                                    }

                                } catch (IOException e) {
                                    Log.e(TAG, "reverseGeocode wrong ");
                                }
                            }
                        }).start();
                    }
                }
            }
        };
        // 检查设备定位设置
        settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        // 满足定位条件
                        Log.i(TAG, "checkLocationSettings successful");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // 设置不满足定位条件
                        int statusCode = ((ApiException) e).getStatusCode();
                        if (statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                            try {
                                ResolvableApiException rae = (ResolvableApiException) e;
                                // 调用startResolutionForResult可以弹窗提示用户打开相应权限
                                rae.startResolutionForResult(AddressAct.this, 0);
                            } catch (IntentSender.SendIntentException sie) {
                                // 拉起引导页面失败
                                Log.e(TAG, "start activity failed");
                            }
                        }
                    }
                });
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
        mEtName = findViewById(R.id.et_name);
        mEtPhone = findViewById(R.id.et_phone);
        mEtAddress = findViewById(R.id.et_address);
        mTvAutoLocation = findViewById(R.id.auto_location);
        mDefaultAddress = findViewById(R.id.default_address);
        mSaveAddress = findViewById(R.id.save_address);
    }

    /**
     * 点击事件
     */
    private void clickEvent() {
        //自动定位的点击事件
        mTvAutoLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonClick();
            }
        });

        //保存地址
        mSaveAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mEtName.getText().toString().trim();
                String phone = mEtPhone.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    ToastUtil.getInstance().showShort(AddressAct.this, R.string.toast_address_input);
                    return;
                }
                if (TextUtils.isEmpty(phone)) {
                    ToastUtil.getInstance().showShort(AddressAct.this, R.string.toast_address_input);
                    return;
                }
                String inputAddress = mEtAddress.getText().toString().trim();
                if (TextUtils.isEmpty(inputAddress)) {
                    deliveryAddress = mAddress;
                } else {
                    deliveryAddress = inputAddress;
                }
                String autoLocation = getString(R.string.auto_location);
                if (!TextUtils.isEmpty(deliveryAddress) && autoLocation.equals(deliveryAddress)) {
                    ToastUtil.getInstance().showShort(AddressAct.this, R.string.toast_please_add_address);
                    return;
                }
                saveAddress(name, phone, deliveryAddress);
                Log.i(TAG, "name=" + name);
                Log.i(TAG, "phone=" + phone);
                Log.i(TAG, "deliveryAddress=" + deliveryAddress);
            }
        });

        mDefaultAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isDefault = true;
                } else {
                    isDefault = false;
                }
            }
        });
    }

    /**
     * 保存地址
     */
    private void saveAddress(String name, String phone, String address) {
        addressBean = new AddressBean();
        addressBean.setPhoneNum(phone);
        addressBean.setName(name);
        addressBean.setAddress(address);
        addressBean.setDefault(isDefault);
        if (isDefault) {
            if (addressList.size() > 0) {
                for (int i = 0; i < addressList.size(); i++) {
                    addressList.get(i).setDefault(false);
                }
            }
            addressList.add(0, addressBean);
        } else {
            addressList.add(addressBean);
        }
        SPUtil.saveDataList(this, SPConstants.TAG_ADDRESS_LIST, addressList);
        finish();
    }

    private void onButtonClick() {
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // 接口调用成功的处理
                        Log.i(TAG, "onLocationResult onSuccess");
                    }
                });
    }

    @Override
    protected void onDestroy() {
        removeLocationUpdatesWithCallback();
        super.onDestroy();
    }

    /**
     * 删除回调
     */
    private void removeLocationUpdatesWithCallback() {
        new Thread() {
            @Override
            public void run() {
                try {
                    //移除位置更新请求
                    fusedLocationProviderClient.removeLocationUpdates(mLocationCallback)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.i(TAG, "removeLocationUpdatesWithCallback onSuccess");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(Exception e) {
                                    Log.i(TAG, "removeLocationUpdatesWithCallback onFailure:" + e.getMessage());
                                }
                            });

                } catch (Exception e) {
                    Log.e(TAG, "removeLocationUpdatesWithCallback exception:");
                }
            }
        }.start();
        Log.i(TAG, "call removeLocationUpdatesWithCallback success.");
    }
}