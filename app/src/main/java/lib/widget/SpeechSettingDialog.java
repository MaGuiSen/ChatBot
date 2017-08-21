package lib.widget;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.eping.chatbot.R;

public class SpeechSettingDialog {

    public BottomInDialog BottomInDialog;
    View dialogContentView;
    Context context;
    boolean isBoy = true;
    TextView txt_boy, txt_girl;

    public SpeechSettingDialog(Context context) {
        this.context = context;
        init();
    }

    public void show() {
        BottomInDialog.show();
    }

    public void dismiss() {
        BottomInDialog.dismiss();
    }

    public void init() {
        dialogContentView = View.inflate(context, R.layout.layout_speech_setting, null);
        BottomInDialog = new BottomInDialog(context, dialogContentView, true, true);
        Button btn_commit = (Button) dialogContentView.findViewById(R.id.btn_commit);
        final SeekBar seek_bar = (SeekBar) dialogContentView.findViewById(R.id.seek_bar);
        final Switch switch_open = (Switch) dialogContentView.findViewById(R.id.switch_open);
        final TextView txt_boy = (TextView) dialogContentView.findViewById(R.id.txt_boy);
        final TextView txt_girl = (TextView) dialogContentView.findViewById(R.id.txt_girl);
        txt_boy.setOnClickListener(clickListener);
        txt_girl.setOnClickListener(clickListener);
        btn_commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null){
                    listener.commit(isBoy, seek_bar.getProgress(), switch_open.isChecked());
                }
            }
        });
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            isBoy = v.getId() == R.id.txt_boy;
            refresh();
        }
    };

    public void refresh(){
        if(isBoy){
            txt_boy.setTextColor(0xff6c8ad9);
            txt_girl.setTextColor(0xff808080);
            txt_boy.setBackgroundResource(R.drawable.round_rect_white_a_border_blue_a);
            txt_girl.setBackgroundColor(0xffffffff);
        }else{
            txt_boy.setTextColor(0xff808080);
            txt_girl.setTextColor(0xff6c8ad9);
            txt_boy.setBackgroundColor(0xffffffff);
            txt_girl.setBackgroundResource(R.drawable.round_rect_white_a_border_blue_a);
        }
    }

    /**
     * 对话框点击
     */
    DialogClickListener listener;

    public void setDialogClickListener(DialogClickListener listener) {
        this.listener = listener;
    }

    public interface DialogClickListener {
        void commit(boolean isBoy, int speed, boolean isOpen);
    }
}
