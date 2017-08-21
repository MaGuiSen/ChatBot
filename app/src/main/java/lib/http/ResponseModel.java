package lib.http;

/**
 * Created by zhengzhangmin on 16/3/9.
 */
public class ResponseModel<T> {

    private int code;
    private String msg;

    private T model;

    public ResponseModel() {
    }

    public ResponseModel(int code, String msg, T model) {
        this.code = code;
        this.msg = msg;
        this.model = model;
    }

    public int getCode() {
        return code;
    }

    public ResponseModel setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public ResponseModel setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public T getModel() {
        return model;
    }

    public ResponseModel setModel(T model) {
        this.model = model;
        return this;
    }
}
