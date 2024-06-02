package dev.xuanran.codebook.bean;

public class Constants {
    /**
     * 生物识别在设备安全硬件中的Key
     */
    public static final String CIPHER_KEYSTORE_ALIAS = "codebook-aes";

    /**
     * 指纹验证方式过期时间
     */
    public static final int FINGERPRINT_AUTH_EXPIRED = 30;

    /**
     * 加密算法/模式/填充方式
     */
    public static final String TRANSFORMATION = "AES/GCM/NoPadding";

    /**
     * 初始化向量（IV）的大小。对于 GCM 模式，推荐使用 12 字节（96 位）的 IV。
     */
    public static final int IV_SIZE = 12; // GCM recommended IV size

    /**
     * GCM Tag 大小 对于 GCM 模式，推荐使用 128 位的认证标签。
     */
    public static final int TAG_SIZE = 128; // GCM recommended tag size
}
