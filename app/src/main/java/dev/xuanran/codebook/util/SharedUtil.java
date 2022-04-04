package dev.xuanran.codebook.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created By XuanRan on 2022/4/4
 * 封装基础数据
 */
public class SharedUtil {
    /**
     * 检查是否存储了数据
     */
    public static final String HAS_DATA = "has_data";
    public static final String PASS_MD5 = "pass_md5";

    public static SharedPreferences sharedPreferences;

    public static void init(Context ctx){
        sharedPreferences = ctx.getSharedPreferences("Settings", Context.MODE_PRIVATE);
    }

    public static SharedPreferences getSharedPreferences(){
        return sharedPreferences;
    }

    public static SharedPreferences.Editor getSharedEdit(){
        return sharedPreferences.edit();
    }
}
