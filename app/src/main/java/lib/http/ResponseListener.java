package lib.http;


public interface ResponseListener<T> {
	void onSuccess(T object);
	void onFailure(int errCode, String errMsg);
}
