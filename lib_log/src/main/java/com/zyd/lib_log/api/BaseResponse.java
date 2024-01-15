package com.zyd.lib_log.api;

public class BaseResponse<T> {

    private T data;
    private int code;
    private String msg;
    private boolean success;

    public BaseResponse() {
    }

    public static <T> BaseResponse<T> createSuccessBean(T data) {
        BaseResponse<T> result = new BaseResponse<>();
        result.setSuccess(true);
        result.setData(data);
        return result;
    }

    public static <T> BaseResponse<T> createFailBean(String errorMsg) {
        BaseResponse<T> result = new BaseResponse<>();
        result.setSuccess(false);
        result.setMsg(errorMsg);
        return result;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
