package pers.lxt.sduinspection.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {

    /**
     * Get MD5 digest of a String.
     * @param str The string to be digested.
     * @return MD5 sequence.
     */
    public static String getMD5(String str) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ignored) {
            // Not suppose to enter this field.
            return null;
        }

        byte[] byteArray = messageDigest.digest();

        StringBuilder md5StrBuff = new StringBuilder();

        for (byte aByteArray : byteArray) {
            if (Integer.toHexString(0xFF & aByteArray).length() == 1)
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & aByteArray));
            else
                md5StrBuff.append(Integer.toHexString(0xFF & aByteArray));
        }
        return md5StrBuff.toString().toUpperCase();
    }
}
