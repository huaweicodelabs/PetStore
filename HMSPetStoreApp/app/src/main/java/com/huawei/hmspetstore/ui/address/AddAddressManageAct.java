package com.huawei.hmspetstore.ui.address;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.bean.AddressBean;
import com.huawei.hmspetstore.constant.SPConstants;
import com.huawei.hmspetstore.ui.address.adapter.AddAddressManageAdapter;
import com.huawei.hmspetstore.util.SPUtil;

import java.util.List;

/**
 * 地址管理界面
 */
public class AddAddressManageAct extends AppCompatActivity {
    public static final String TAG = "AddAddressManageAct";
    private AddAddressManageAdapter addressManagementAdapter;
    // 地址列表
    private RecyclerView mRecyclerView;
    private List<AddressBean> addressList;
    private ImageView mTitleBack;
    private TextView mTvRight;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_manage);
        addressList = SPUtil.getDataList(this, SPConstants.TAG_ADDRESS_LIST, AddressBean.class);
        if (addressList.size() == 0) {
            Intent intent = new Intent(AddAddressManageAct.this, AddressAct.class);
            startActivity(intent);
        }
        //初始化控件
        initViews();
        //点击事件
        clickEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        addressList = SPUtil.getDataList(this, SPConstants.TAG_ADDRESS_LIST, AddressBean.class);
        addressManagementAdapter = new AddAddressManageAdapter(addressList);
        mRecyclerView.setAdapter(addressManagementAdapter);
    }

    /**
     * 点击事件
     */
    private void clickEvent() {
        mTitleBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddAddressManageAct.this, AddressAct.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 初始化控件
     */
    private void initViews() {
        // 初始化RecyclerView
        mRecyclerView = findViewById(R.id.address_list);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        // 设置布局管理器
        mRecyclerView.setLayoutManager(manager);
        // 设置为垂直布局，这也是默认的
        manager.setOrientation(RecyclerView.VERTICAL);
        //返回键
        mTitleBack = findViewById(R.id.title_back);
        mTvRight = findViewById(R.id.title_right);
    }
}