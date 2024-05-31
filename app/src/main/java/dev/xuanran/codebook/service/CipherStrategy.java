package dev.xuanran.codebook.service;

/**
 * 密码加密策略接口类
 */
public interface CipherStrategy {

    /**
     * 加密数据
     * @param data 待加密数据
     * @return 加密结果
     * @throws Exception 加密错误
     */
    String encryptData(String data) throws Exception;

    /**
     * 解密数据
     * @param encryptedData 待解密数据
     * @return 解密结果
     * @throws Exception 解密错误
     */
    String decryptData(String encryptedData) throws Exception;
}
