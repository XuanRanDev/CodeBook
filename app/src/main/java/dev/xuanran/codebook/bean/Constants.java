package dev.xuanran.codebook.bean;

public class Constants {
    /**
     * 生物识别在设备安全硬件中的Key
     */
    public static final String CIPHER_KEYSTORE_ALIAS = "codebook-aes";

    /**
     * 指纹验证方式过期时间
     */
    public static final int FINGERPRINT_AUTH_EXPIRED = 90;

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

    /**
     * Shared 配置名称
     */
    public static final String PREFS_NAME = "codebook-config";

    /**
     * 加密类型Key
     */
    public static final String KEY_ENCRYPTION_TYPE = "encryption_type";

    /**
     * 验证密码Key
     */
    public static final String KEY_VALIDATE = "validate";

    /**
     * 加密类型值-指纹
     */
    public static final String ENCRYPTION_TYPE_FINGERPRINT = "fingerprint";

    /**
     * 加密类型值-密码
     */
    public static final String ENCRYPTION_TYPE_PASSWORD = "password";

    public static final String KEY_USER_RULE_AGREE_STATUS = "user_rule_agree_status";

    public static final String KEY_USER_RULE_AGREE_DATE = "user_rule_agree_date";


    /**
     * 盐值 Key
     */
    public static final String KEY_SALT = "salt";

    /**
     * 导入导出时，密码与盐值的分割
     */
    public static final String EXPORT_IMPORT_PASS_SPILT = "-&&-";


}
