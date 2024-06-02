package dev.xuanran.codebook.callback;

/**
 * 指纹认证回调
 */
public interface FingerprintCallback {

    /**
     * 指纹认证回调
     * @param code 0 成功
     *             -1 失败
     *             2
     * @param success 是否成功
     */
    void onFingerprint(boolean success, int code, String msg);
}
