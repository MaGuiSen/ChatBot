package lib.third.speech;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

public class IflyTTSModule{
    private  Context context;
    // 语音合成对象
    private SpeechSynthesizer mTts;
    // 默认发音人
    private String voicer = "xiaoyan";
    private String speed = "50";
    // 缓冲进度
    private int bufferProgress = 0;
    // 播放进度
    private int playProgress = 0;

    private static IflyTTSModule iflyTTSModule = null;
    public synchronized static IflyTTSModule getInstance(Context context){
        if(iflyTTSModule == null){
            iflyTTSModule = new IflyTTSModule(context);
        }
        return iflyTTSModule;
    }

    private IflyTTSModule(Context context) {
        this.context = context;
    }

    /**
     * 参数设置
     */
    private void setParam() {
        if(mTts == null){
            return;
        }
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置在线合成发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
        //设置合成语速
        mTts.setParameter(SpeechConstant.SPEED, speed);
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, "50");
        //设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME, "50");
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/tts.wav");
    }

    /**
     * @param text  发音内容
     * @param voicer:发音的人
     *               小燕:女青、中英、普通话 xiaoyan
     *               小峰—男青、中英、普通话 vixf
     *               凯瑟琳—女青、英 vimary
     *               玛丽—男青、英 henry
     * @param speed:播放速度
     */
    public void startSpeaking(String text, String voicer, String speed) {
        // 设置参数
        if(mTts == null){
            mTts = SpeechSynthesizer.createSynthesizer(context, mInitListener);
        }
        if(TextUtils.isEmpty(voicer)){
            voicer = "xiaoyan";
        }
        if(TextUtils.isEmpty(speed)){
            speed = "50";
        }
        setParam();
        int code = mTts.startSpeaking(text, mTtsListener);
        if (code != ErrorCode.SUCCESS) {
            showTip("语音合成失败,错误码: " + code);
            if(listener != null){
                listener.onError("初始化失败，错误码：" + code);
            }
        }
    }

    public void stopSpeaking() {
        // 取消合成
        if(mTts != null) {
            mTts.stopSpeaking();
        }
    }

    public void pauseSpeaking() {
        // 暂停播放
        if(mTts != null) {
            mTts.pauseSpeaking();
        }
    }

    public void resumeSpeaking() {
        // 继续播放
        if(mTts != null) {
            mTts.resumeSpeaking();
        }
    }

    public void remove() {
        if( null != mTts ){
            mTts.stopSpeaking();
            // 退出时释放连接
            mTts.destroy();
            mTts = null;
        }
    }

    /**
     * 初始化监听器
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
        if (code != ErrorCode.SUCCESS) {
            if(listener != null){
                listener.onError("初始化失败，错误码：" + code);
            }
            showTip("初始化失败，错误码：" + code);
        }
        }
    };

    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            showTip("开始播放");
            if(listener != null){
                listener.onSpeakBegin();
            }
        }

        @Override
        public void onSpeakPaused() {
            showTip("暂停播放");
            if(listener != null){
                listener.onSpeakPaused();
            }
        }

        @Override
        public void onSpeakResumed() {
            showTip("继续播放");
            if(listener != null){
                listener.onSpeakResumed();
            }
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,String info) {
            //
            bufferProgress = percent;
            showTip("缓存进度:"+bufferProgress+"播放进度:"+playProgress);
            if(listener != null){
                listener.onProgressChange(bufferProgress, playProgress);
            }
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
            playProgress = percent;
            showTip("缓存进度:"+bufferProgress+"播放进度:"+playProgress);
            if(listener != null){
                listener.onProgressChange(bufferProgress, playProgress);
            }
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                showTip("播放完成");
                if(listener != null){
                    listener.onCompleted();
                }
            } else if (error != null) {
                showTip(error.getPlainDescription(true));
                if(listener != null){
                    listener.onError(error.getPlainDescription(true)+":"+error.getErrorCode());
                }
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    private void showTip(final String str) {
        Log.e("++++++++++++++++++++",str);
    }

    public IflyTTSModule setListener(IflyTTSListener listener) {
        this.listener = listener;
        return this;
    }

    private IflyTTSListener listener;
    public class IflyTTSListener{

        //开始播放
        public void onSpeakBegin() {}

        //暂停播放
        public void onSpeakPaused() {}

        //继续播放
        public void onSpeakResumed() {}

        //进度变化
        public void onProgressChange(int bufferProgress, int playProgress) {}

        //播放完成
        public void onCompleted() {}

        //播放出错
        public void onError(String errMsg) {}
    }
}
