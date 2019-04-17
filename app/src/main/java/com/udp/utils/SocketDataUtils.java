package com.udp.utils;


import com.udp.constant.Constant;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;

/**
 * @author: wu.xu
 * @data: 2019/4/15/015.
 * <p>
 * 毫无BUG
 * <p>
 * v2v数据包结构：
 * 字段  字节数  描述
 * <p>
 * 0xEAEA  2     包头，内容为0xEAEA
 * <p>
 * ttl     1     生存期。每次转发之前减一，如果结果为零则不转发。主车app发送时ttl设为2。
 * <p>
 * length  2     载荷数据长度
 * <p>
 * crc     4     载荷数据的crc32码
 * <p>
 * data  length  json数据
 * <p>
 * 字节序为 big-endian   字节序：长字节（正向放置二进制数）和短字节（反向放置二进制数）
 */


public class SocketDataUtils {

  public static byte[] createBytes(String data, byte ttl, int crc32) {
    byte[] bytes = getStringByte(data);
    short length = (short) data.length();
    return getRealBytes(bytes, ttl, length, crc32);
  }

  /**
   * 判断接收数据是否完整
   *
   * @param map
   * @return
   */
  public static boolean dataIntegrity(Map map) {
    if (map != null) {
      String head = (String) map.get(Constant.KEY_HEAD);
      int crc32 = (int) map.get(Constant.KEY_CRC32);
      String data = (String) map.get(Constant.KEY_DATA);

      if (head.equals(Constant.HEAD) && crc32 == getCRC32(getStringByte(data))) {
        return true;
      }
    }
    return false;
  }

  public static byte[] getRealBytes(byte[] data, byte ttl, short length, int crc32) {
    HashMap<String, Object> stringStringHashMap = new HashMap<>();
    stringStringHashMap.put(Constant.KEY_HEAD, Constant.HEAD);
    stringStringHashMap.put(Constant.KEY_TTL, ttl);
    stringStringHashMap.put(Constant.KEY_LENGTH, length);
    stringStringHashMap.put(Constant.KEY_CRC32, crc32);
    return SocketDataUtils.assembleBytes(data, stringStringHashMap);
  }

  public static Map<String, Object> analyseBytes(byte[] data) {
    if (data == null || data.length < 9) {
      return null;
    }
    Map<String, Object> map = new HashMap<>();
    byte[] headBytes = new byte[Constant.BYTE_LENGTH_HEAD];
    byte[] ttlBytes = new byte[Constant.BYTE_LENGTH_TTL];
    byte[] lengthBytes = new byte[Constant.BYTE_LENGTH_LENGTH];
    byte[] crc32Bytes = new byte[Constant.BYTE_LENGTH_CRC32];
    byte[] realDataBytes = new byte[3072];

    for (int i = 0; i < data.length; i++) {
      //0 1 两个字节
      if (i > -1 && i < 2) {
        headBytes[i] = data[i];
      } else if (i == 2) {
        ttlBytes[0] = data[i];
      } else if (i > 2 && i < 5) {
        lengthBytes[i - 3] = data[i];
      } else if (i > 4 && i < 9) {
        crc32Bytes[i - 5] = data[i];
      } else {
        realDataBytes[i - 9] = data[i];
      }
    }
//    //这个速度不如上面的。执行多次循环
//    Map<String, String> map = new HashMap<>();
//    byte[] headBytes = new byte[2];
//    byte[] ttlBytes = new byte[1];
//    byte[] bytes = new byte[2];
//    byte[] bytes = new byte[4];
//    byte[] realDataBytes = new byte[3072];
//
//    byte[] headBytes = Arrays.copyOfRange(data, 0, 2);
//    byte[] ttlBytes = Arrays.copyOfRange(data, 2, 3);
//    byte[] lengthBytes = Arrays.copyOfRange(data, 3, 5);
//    byte[] crc32Bytes = Arrays.copyOfRange(data, 5, 9);
//    byte[] realDataBytes = Arrays.copyOfRange(data, 9, data.length);

    StringBuffer sb = new StringBuffer(getString(realDataBytes));
    short length = bytesToShort(lengthBytes);

    map.put(Constant.KEY_HEAD, getString(headBytes));
    map.put(Constant.KEY_TTL, ttlBytes[0]);
    map.put(Constant.KEY_LENGTH, length);
    map.put(Constant.KEY_CRC32, bytesToInt(crc32Bytes));
    map.put(Constant.KEY_DATA, sb.substring(0, length));

    return map;
  }


  //Socket传输数据字段拼接。可防止丢包
  public static byte[] assembleBytes(byte[] data, Map<String, Object> params) {
    String head = (String) params.get(Constant.KEY_HEAD);
    byte ttl = (byte) params.get(Constant.KEY_TTL);
    short length = (short) params.get(Constant.KEY_LENGTH);
    int crc32 = (int) params.get(Constant.KEY_CRC32);

    byte[] headBytes = head.getBytes();
    byte[] ttlBytes = new byte[]{ttl};
    byte[] lengthBytes = shortToByte(length);
    byte[] crc32Bytes = intToBytes(crc32);

    byte[] bytes1 = SocketDataUtils.appendBytes(headBytes, ttlBytes);
    byte[] bytes2 = SocketDataUtils.appendBytes(bytes1, lengthBytes);
    byte[] bytes3 = SocketDataUtils.appendBytes(bytes2, crc32Bytes);
    byte[] bytes4 = SocketDataUtils.appendBytes(bytes3, data);

//    StringBuffer s= new StringBuffer();
//    s.append("dataBytes : ");
//    for (byte i:bytes4){
//      s.append(i);
//    }
////
////    final String result = new String(headBytes);
//    Log.e("WxSendUtils","-----"+bytesToShort(lengthBytes)+"-----"+lengthBytes.length);
//    Log.e("WxSendUtils","-----"+bytesToInt(crc32Bytes)+"-----"+crc32Bytes.length);
//    Log.e("WxSendUtils","-----"+bytes3+"-----"+bytes3.length);
    return bytes4;
  }

  /**
   * @param data1
   * @param data2
   * @return data1 与 data2拼接的结果
   */
  public static byte[] appendBytes(byte[] data1, byte[] data2) {
    byte[] data3 = new byte[data1.length + data2.length];
    System.arraycopy(data1, 0, data3, 0, data1.length);
    System.arraycopy(data2, 0, data3, data1.length, data2.length);
    return data3;
  }

  /**
   * 得到本机IP
   */
  public static String getIpAddress() {
    String localip = null;
    String netip = null;
    try {
      Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
      InetAddress ip = null;
      boolean finded = false;
      while (netInterfaces.hasMoreElements() && !finded) {
        NetworkInterface ni = netInterfaces.nextElement();
        Enumeration<InetAddress> address = ni.getInetAddresses();
        while (address.hasMoreElements()) {
          ip = address.nextElement();
          if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {
            netip = ip.getHostAddress();
            finded = true;
            break;
          } else if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {
            localip = ip.getHostAddress();
          }
        }
      }
    } catch (SocketException e) {
      e.printStackTrace();
    }
    if (netip != null && !"".equals(netip)) {
      return netip;
    } else {
      return localip;
    }
  }

  /**
   * int转byte[] 高位在前，大端字符序
   */
  public static byte[] intToBytes(int value) {
    byte[] bytes = new byte[4];
    bytes[0] = (byte) (value >> 24);
    bytes[1] = (byte) (value >> 16);
    bytes[2] = (byte) (value >> 8);
    bytes[3] = (byte) (value >> 0);
    return bytes;
  }

  /**
   * short转byte[] 高位在前，大端字符序
   */
  public static byte[] shortToByte(short value) {
    byte[] bytes = new byte[2];
    bytes[0] = (byte) (value >> 8);
    bytes[1] = (byte) (value >> 0);
    return bytes;
  }

  /**
   * byte[]转int 高位在前，大端字符序
   */
  public static int bytesToInt(byte[] bytes) {
    return (int) ((((bytes[0] & 0xff) << 24)
            | ((bytes[1] & 0xff) << 16)
            | ((bytes[2] & 0xff) << 8) | ((bytes[3] & 0xff) << 0)));
  }

  /**
   * byte[]转short 高位在前，大端字符序
   */
  public static short bytesToShort(byte[] bytes) {
    return (short) (((bytes[0] << 8) | bytes[1] & 0xff));
  }

  /**
   * 得到CRC32校验码
   */
  public static int getCRC32(byte[] bytes) {
//    Log.e("getCRC32", "onClick: " + new String(bytes));
    CRC32 crc32 = new CRC32();
    crc32.update(bytes);
    return (int) crc32.getValue();
  }

  public static byte[] getStringByte(String data) {
    try {
      return data.getBytes("utf-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return data.getBytes();
    }
  }

  public static String getString(byte[] bytes) {
    try {
      return new String(bytes,"utf-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return new String(bytes);
    }
  }
}
