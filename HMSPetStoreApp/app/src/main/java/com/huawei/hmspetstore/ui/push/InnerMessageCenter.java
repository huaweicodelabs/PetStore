package com.huawei.hmspetstore.ui.push;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.ui.order.adapter.OrderAdapter;
import com.huawei.hmspetstore.ui.push.innermessage.InnerMessage;
import com.huawei.hmspetstore.ui.push.innermessage.InnerMessageAdapter;
import com.huawei.hmspetstore.util.DateUtil;
import com.huawei.hmspetstore.util.SPUtil;
import com.huawei.hmspetstore.util.ToastUtil;
import com.huawei.hmspetstore.view.DividerItemDecoration;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static android.view.View.VISIBLE;

public class InnerMessageCenter extends AppCompatActivity {

    private RecyclerView lvInnerMessage;
    InnerMessageAdapter innerMessageAdapter;
    List<InnerMessage> messageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inner_message_center);
        // 提示进入的Kit
        ToastUtil.getInstance().showShort(this, R.string.toast_push);
        // 初始化View
        initView();
        initMessage();
        updateMessageList();
    }

    /**
     * 初始化View
     */
    private void initView() {
        // 顶部返回
        ImageView mIvBack = findViewById(R.id.title_back);
        // 顶部右侧按钮
        ImageView ivClear = findViewById(R.id.title_right);
        ivClear.setImageDrawable(getDrawable(R.drawable.ic_inner_message_clear));
        ivClear.setVisibility(VISIBLE);
        lvInnerMessage = findViewById(R.id.lvInnerMessage);

        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 返回
                finish();
            }
        });
        ivClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PushSharedPreferences.clearMessage(getApplicationContext());
                innerMessageAdapter.clear();
            }
        });
    }

    private void updateMessageList() {
        Map<String, String> data = PushSharedPreferences.readMessage(getApplicationContext());
        Context context = getApplicationContext();
        for (String key : data.keySet()) {
            InnerMessage innerMessage = new InnerMessage(context, key, data.get(key));
            messageList.add(innerMessage);
        }
        innerMessageAdapter = new InnerMessageAdapter(messageList);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        //设置布局管理器
        lvInnerMessage.setLayoutManager(manager);
        //设置为垂直布局，这也是默认的
        manager.setOrientation(RecyclerView.VERTICAL);
        lvInnerMessage.setAdapter(innerMessageAdapter);
    }

    private void initMessage() {
        //默认保存一条消息到消息中心
        boolean isSaved = (boolean) SPUtil.get(this, "is_saved_push", false);
        if (isSaved) {
            return;
        }
        PushSharedPreferences.saveMessage(this, getString(R.string.message_default_title)
                        + PushConst.PUSH_SPLIT + DateUtil.getCurrentFormatDate(),
                getString(R.string.message_default_desc));
        SPUtil.put(this, "is_saved_push", true);
    }
}