package com.tedkim.android.api.requestclient;

import com.tedkim.android.api.interfaces.APIResponseListener;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by Ted
 */
public class APIRequestManager<T> {

    private static APIRequestManager mInstance;
    public HashMap<String, APIRequestVO<T>> mHashRequest;

    private boolean isRequestRefreshToken = false;

    public static APIRequestManager getInstance() {
        if (mInstance == null) {
            mInstance = new APIRequestManager();
        }

        return mInstance;
    }

    public APIRequestManager() {
        mHashRequest = new HashMap<>();
    }

    /**
     * Add a request call to the stack
     *
     * @param code unique code
     * @param item Retrofit Item
     */
    public void addRequestCall(String code, APIRequestVO item) {
        mHashRequest.put(code, item);
        item.getCall().enqueue(item.getCallback());
    }

    /**
     * Remove a request call to the stack
     *
     * @param UniqueID Unique ID
     */
    public void removeRequestCall(String UniqueID) {
        mHashRequest.remove(UniqueID);
    }

    /**
     * Remove all request client
     *
     * @param isStackRemove delete or not on stack
     */
    public void cancelAllRequest(boolean isStackRemove) {
        isRequestRefreshToken = false;
        for (Object obj : mHashRequest.entrySet()) {
            Map.Entry entry = (Map.Entry) obj;
            APIRequestVO item = (APIRequestVO) entry.getValue();
            item.getCall().cancel();
        }

        if (isStackRemove) {
            mHashRequest.clear();
        }
    }

    /**
     * RequestCall again request after request refresh token
     */
    public void retryAPIRequest() {
        isRequestRefreshToken = false;
        if (mHashRequest.size() > 0) {
            // API Call 복제
            HashMap<String, APIRequestVO<T>> newRequest = new HashMap<>();
            for (Object obj : mHashRequest.entrySet()) {
                Map.Entry entry = (Map.Entry) obj;
                String key = (String) entry.getKey();
                APIRequestVO<T> item = (APIRequestVO) entry.getValue();
                APIRequestVO<T> newItem = new APIRequestVO<T>();
                newItem.call = item.call.clone();
                newItem.callback = item.callback;

                newRequest.put(key, newItem);
            }

            mHashRequest.clear();
            mHashRequest.putAll(newRequest);

            // API Call 다시 실행
            for (Object obj : mHashRequest.entrySet()) {
                Map.Entry entry = (Map.Entry) obj;
                APIRequestVO<T> item = (APIRequestVO) entry.getValue();
                item.getCall().enqueue(item.getCallback());
            }
        }
    }

    /**
     * Request success response
     *
     * @param uniqueID unique
     * @param type     API type
     * @param vo       response vo
     * @param listener APIResponseListener
     */
    public void successResponse(String uniqueID, Object vo, APIResponseListener listener) {
        if (!isRequestRefreshToken) {
            listener.getData(vo);
            removeRequestCall(uniqueID);
        }
    }

    /**
     * Request error response
     */
    public void responseTokenError() {
        isRequestRefreshToken = true;
    }
}