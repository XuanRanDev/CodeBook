package dev.xuanran.codebook.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created By XuanRan on 2022/4/4
 */
public class FileUtil {

    /**
     * 从Assets/image中读取图片
     * @param fileName 文件名
     */
    public static Bitmap getImageFromAssetsFile(Context context, String fileName ) {
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        try {
            InputStream is = am.open("image/"+fileName+".png");
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    public static String readTxtFromAssetsFile(Context ctx,String fileName){
        byte[] bytes = new byte[0];
        try {
            AssetManager assets = ctx.getAssets();
            InputStream open = assets.open(fileName);
            int size = open.available();

            bytes = new byte[size];
            open.read(bytes);
            open.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(bytes,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
