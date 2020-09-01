package com.huawei.hmspetstore.ui.push.innermessage;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.ui.push.PushConst;

/**
 * 功能描述
 */
public class InnerMessage {
    private String title;
    private String content;
    private String date;
    private Drawable icon;

    public InnerMessage(Context context, String title, String content) {
        String[] inTitle = title.split(PushConst.PUSH_SPLIT);
        this.title = inTitle[0];
        this.content = content;
        this.date = inTitle[1];
        this.icon = context.getDrawable(R.drawable.ic_inner_message);
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getDate() {
        return date;
    }

    public Drawable getIcon() {
        return icon;
    }
}