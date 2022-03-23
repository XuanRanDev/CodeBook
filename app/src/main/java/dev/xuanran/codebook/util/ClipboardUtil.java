package dev.xuanran.codebook.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

/**
 * Created By XuanRan on 2022/3/23
 */
public class ClipboardUtil {

    public static void setTextToClipboard(Context ctx, String data) {
        ClipboardManager clipboardManager = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("text", data);
        clipboardManager.setPrimaryClip(mClipData);
    }

    public static String getTextFromClipboard(Context ctx) {
        ClipboardManager clipboard = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = clipboard.getPrimaryClip();
        if (clipData != null && clipData.getItemCount() > 0) {
            // 从数据集中获取（粘贴）第一条文本数据
            CharSequence text = clipData.getItemAt(0).getText();
            return (String) text;
        }
        return "";
    }
}
