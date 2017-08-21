package lib.api;

/**
 * Bot客户端访问api
 */
public class BotClient {

    private static void successCallBack(Listener listener, String object){
        if(listener != null){
            listener.success(object);
        }
    }

    private static void failCallBack(Listener listener, int code, String msg){
        if(listener != null){
            listener.fail(code, msg);
        }
    }

    public interface Listener<T>{
        void success(Object obj);
        void fail(int code, String msg);
    }
}
