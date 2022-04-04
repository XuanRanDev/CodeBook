package dev.xuanran.codebook.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created By XuanRan on 2022/4/4
 */
public class MD5Util {

    public static String md5(String input) throws NoSuchAlgorithmException {
        byte[] bytes = MessageDigest.getInstance("MD5").digest(input.getBytes());
        return printHexBinary(bytes);
    }

    public static String printHexBinary(byte[] data) {
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(String.format("%02X", new Integer(b & 0xFF)));
        }
        return r.toString();
    }
}

