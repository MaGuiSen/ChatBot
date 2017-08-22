package com.eping.chatbot.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.eping.chatbot.R;

/**
 * 消息输入控件
 */
public class ChatBotInputView extends LinearLayout {
    private EditText edit_msg;
    private Button btn_send;
    private Listener listener;

    public ChatBotInputView(Context context) {
        this(context, null);
    }

    public ChatBotInputView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        LinearLayout contentView = (LinearLayout) View.inflate(getContext(), R.layout.layout_chatbot_input, null);
        edit_msg = (EditText) contentView.findViewById(R.id.edit_msg);
        btn_send = (Button) contentView.findViewById(R.id.btn_send);
        btn_send.setOnClickListener(clickListener);
        addView(contentView);
    }

    private void sendTextMsg(String msg){
        if(listener!=null){
            listener.sendTextMsg(msg);
        }
    }

    OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btn_send:
                    String msg = edit_msg.getText().toString();
                    if(TextUtils.isEmpty(msg)){
                        Toast.makeText(getContext(), "请输入内容", Toast.LENGTH_LONG).show();
                        return;
                    }
                    sendTextMsg(msg);
                    edit_msg.setText("");
                    break;
            }
        }
    };

    public ChatBotInputView setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * 外部消息回调
     */
    public interface Listener{
        void sendTextMsg(String msg);
    }
}
