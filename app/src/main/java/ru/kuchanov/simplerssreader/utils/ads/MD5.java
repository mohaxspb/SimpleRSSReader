package ru.kuchanov.simplerssreader.utils.ads;

/**
 * Created by Юрий on 02.03.2016 3:27.
 * For SimpleRSSReader.
 */
public class MD5
{
    public static String convert(String md5)
    {
        try
        {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b: array)
            {
                sb.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
