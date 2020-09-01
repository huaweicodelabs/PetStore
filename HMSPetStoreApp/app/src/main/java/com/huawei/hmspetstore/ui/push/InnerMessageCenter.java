package com.huawei.hmspetstore.ui.push;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.ui.push.innermessage.InnerMessage;
import com.huawei.hmspetstore.ui.push.innermessage.InnerMessageAdapter;
import com.huawei.hmspetstore.util.DateUtil;
import com.huawei.hmspetstore.util.SPUtil;
import com.huawei.hmspetstore.util.ToastUtil;

import java.util.LinkedList;
import java.util.Map;

import static android.view.View.VISIBLE;

public class InnerMessageCenter extends AppCompatActivity {

    private RecyclerView lvInnerMessage;
    InnerMessageAdapter innerMessageAdapter;
    LinkedList<InnerMessage> messageList = new LinkedList<>();

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
        lvInnerMessage.setAdapter(innerMessageAdapter);
        innerMessageAdapter.notifyDataSetChanged();
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