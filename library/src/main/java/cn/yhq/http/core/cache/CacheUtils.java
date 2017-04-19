package cn.yhq.http.core.cache;

/**
 * Created by Yanghuiqiang on 2017/4/19.
 */

public class CacheUtils {

    /**
     * 判断缓存的String数据是否到期
     *
     * @param str
     * @return true：到期了 false：还没有到期
     */
    public static boolean isDue(String str) {
        return isDue(str.getBytes());
    }

    /**
     * 判断缓存的byte数据是否到期
     *
     * @param data
     * @return true：到期了 false：还没有到期
     */
    public static boolean isDue(byte[] data) {
        String[] strs = getDateInfoFromDate(data);
        if (strs != null && strs.length == 2) {
            String saveTimeStr = strs[0];
            while (saveTimeStr.startsWith("0")) {
                saveTimeStr = saveTimeStr
                        .substring(1, saveTimeStr.length());
            }
            long saveTime = Long.valueOf(saveTimeStr);
            long deleteAfter = Long.valueOf(strs[1]);
            if (System.currentTimeMillis() > saveTime + deleteAfter * 1000) {
                return true;
            }
        }
        return false;
    }

    public static String newStringWithDateInfo(int second, String strInfo) {
        return createDateInfo(second) + strInfo;
    }

    public static byte[] newByteArrayWithDateInfo(int second, byte[] data2) {
        byte[] data1 = createDateInfo(second).getBytes();
        byte[] retdata = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, retdata, 0, data1.length);
        System.arraycopy(data2, 0, retdata, data1.length, data2.length);
        return retdata;
    }

    public static String clearDateInfo(String strInfo) {
        if (strInfo != null && hasDateInfo(strInfo.getBytes())) {
            strInfo = strInfo.substring(strInfo.indexOf(mSeparator) + 1,
                    strInfo.length());
        }
        return strInfo;
    }

    public static byte[] clearDateInfo(byte[] data) {
        if (hasDateInfo(data)) {
            return copyOfRange(data, indexOf(data, mSeparator) + 1,
                    data.length);
        }
        return data;
    }

    public static boolean hasDateInfo(byte[] data) {
        return data != null && data.length > 15 && data[13] == '-'
                && indexOf(data, mSeparator) > 14;
    }

    public static String[] getDateInfoFromDate(byte[] data) {
        if (hasDateInfo(data)) {
            String saveDate = new String(copyOfRange(data, 0, 13));
            String deleteAfter = new String(copyOfRange(data, 14,
                    indexOf(data, mSeparator)));
            return new String[]{saveDate, deleteAfter};
        }
        return null;
    }

    public static int indexOf(byte[] data, char c) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] == c) {
                return i;
            }
        }
        return -1;
    }

    public static byte[] copyOfRange(byte[] original, int from, int to) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        byte[] copy = new byte[newLength];
        System.arraycopy(original, from, copy, 0,
                Math.min(original.length - from, newLength));
        return copy;
    }

    private static final char mSeparator = ' ';

    public static String createDateInfo(int second) {
        String currentTime = System.currentTimeMillis() + "";
        while (currentTime.length() < 13) {
            currentTime = "0" + currentTime;
        }
        return currentTime + "-" + second + mSeparator;
    }

}
