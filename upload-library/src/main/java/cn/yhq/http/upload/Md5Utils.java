package cn.yhq.http.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Utils {

  /**
   * 默认的密码字符串组合，用来将字节转换�?16 进制表示的字�?apache校验下载的文件的正确性用的就是默认的这个组合
   */
  protected static char hexDigits[] =
      {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

  protected static MessageDigest messagedigest = null;
  static {
    try {
      messagedigest = MessageDigest.getInstance("Md5Utils");
    } catch (NoSuchAlgorithmException nsaex) {
      System.err.println(Md5Utils.class.getName() + "初始化失败，MessageDigest不支持MD5");
      nsaex.printStackTrace();
    }
  }

  /**
   * 生成字符串的md5校验�?
   *
   * @param s
   * @return
   */
  public static String getMD5String(String s) {
    return getMD5String(s.getBytes());
  }

  /**
   * 判断字符串的md5校验码是否与�?��已知的md5码相匹配
   *
   * @param password 要校验的字符�?
   * @param md5PwdStr 已知的md5校验�?
   * @return
   */
  public static boolean checkPassword(String password, String md5PwdStr) {
    String s = getMD5String(password);
    return s.equals(md5PwdStr);
  }

  /**
   * 生成文件的md5校验�?
   *
   * @param file
   * @return
   * @throws IOException
   */
  public static String getFileMD5String(File file) throws IOException {
    String value = null;
    FileInputStream in = new FileInputStream(file);
    try {
      MappedByteBuffer byteBuffer =
          in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
      MessageDigest md5 = MessageDigest.getInstance("Md5Utils");
      md5.update(byteBuffer);
      BigInteger bi = new BigInteger(1, md5.digest());
      value = bi.toString(16);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (null != in) {
        try {
          in.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return value;
  }

  public static String getMD5String(byte[] bytes) {
    messagedigest.update(bytes);
    BigInteger bi = new BigInteger(1, messagedigest.digest());
    return bi.toString(16);
  }

  private static String bufferToHex(byte bytes[]) {
    return bufferToHex(bytes, 0, bytes.length);
  }

  private static String bufferToHex(byte bytes[], int m, int n) {
    StringBuffer stringbuffer = new StringBuffer(2 * n);
    int k = m + n;
    for (int l = m; l < k; l++) {
      appendHexPair(bytes[l], stringbuffer);
    }
    return stringbuffer.toString();
  }

  private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
    char c0 = hexDigits[(bt & 0xf0) >> 4];// 取字节中�?4 位的数字转换, >>> 为�?辑右移，将符号位�?��右移,此处未发现两种符号有何不�?
    char c1 = hexDigits[bt & 0xf];// 取字节中�?4 位的数字转换
    stringbuffer.append(c0);
    stringbuffer.append(c1);
  }

  public static void main(String[] args) throws IOException {
    long begin = System.currentTimeMillis();

    String md5 = getMD5String("a");

    long end = System.currentTimeMillis();
    System.out.println("md5:" + md5 + " time:" + ((end - begin) / 1000) + "s");
  }

}
