package com.udp.constant;

/**
 * @author: wu.xu
 * @data: 2019/4/15/015.
 * <p>
 * 数据包结构：这样可以防止丢包，导致数据错误解析
 *
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

public class Constant {
  public static final String IP="127.0.0.1";
  public static final int PORT = 30300;

  public static final String HEAD = "EA";

  /*字节解析 Map所用key*/
  public static final String KEY_HEAD = "KEY_HEAD";
  public static final String KEY_TTL = "KEY_TTL";//转发次数
  public static final String KEY_LENGTH = "KEY_LENGTH";
  public static final String KEY_CRC32 = "KEY_CRC32";
  public static final String KEY_DATA = "KEY_DATA";

  public static final int BYTE_LENGTH_HEAD = 2;
  public static final int BYTE_LENGTH_TTL = 1;
  public static final int BYTE_LENGTH_LENGTH = 2;
  public static final int BYTE_LENGTH_CRC32 = 4;
}
