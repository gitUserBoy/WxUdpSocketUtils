package com.udp.utils;


import com.udp.constant.Constant;
import com.udp.interfaces.SocketInterface;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * @author: wu.xu
 * @data: 2019/4/15/015.
 * <p>
 * 毫无BUG
 * <p>
 * 接收数据后可进行处理并且判断是否发送。
 */

public class WxReceiveAndSendUtils {
  private static InetAddress mAddress;
  private static DatagramSocket socket;

  private static SocketInterface.SendSocketInterface socketInterface;

  private static SocketInterface.ReceivedSocketInterface socketInterface2;

  private static boolean isRunning = true;

  private static int lastDataCrc32 = -1;

  public static void setSendSocketInterface(SocketInterface.SendSocketInterface sendSocketInterface) {
    socketInterface = sendSocketInterface;
  }

  public static void setReceivedSocketInterface(SocketInterface.ReceivedSocketInterface receivedSocketInterface) {
    socketInterface2 = receivedSocketInterface;
  }

  public static void receivedAndSend() {
    //初始化socket
    new Thread() {
      @Override
      public void run() {
        try {
          if (mAddress == null) {
            mAddress = InetAddress.getByName(Constant.IP);//接收内容的Ip地址
          }
          if (socket == null) {
            socket = new DatagramSocket(Constant.RECEIVED_PORT);//这里初始化，传入端口号，绑定一个通信地址接口.具体看内部源码
          }
          /*接收消息*/
          while (isRunning) {
            byte[] bytes = new byte[3072];
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
            socket.receive(packet);
            Map<String, Object> map = SocketDataUtils.analyseBytes(packet.getData());
            /**
             *接收的数据包如果完整处理数据,判断TTL是否转发  , 这里因为ip固定是本机。所以发出后自己仍会收到。
             */
            if (SocketDataUtils.dataIntegrity(map)) {
              /**
               * 对比上个包的crc32和这个包的crc32是否相同，相同就不继续做处理
               */
              int crc32 = (int) map.get(Constant.KEY_CRC32);
              if (crc32 == lastDataCrc32) {//上次的包的crc32相同
                continue;
              }

//              Log.e("getCRC32", "getCRC32: "+ crc32);

              lastDataCrc32 = crc32;
              String data = (String) map.get(Constant.KEY_DATA);
              if (socketInterface2 != null) {
                socketInterface2.receiveFinish(data);
              }

              byte ttl = (byte) map.get(Constant.KEY_TTL);
              byte newTtl = (byte) (ttl - 1);

              if (newTtl > 0) {
                map.put(Constant.KEY_TTL, newTtl);
                /*发送消息*/
                byte[] realBytes = SocketDataUtils.assembleBytes(SocketDataUtils.getStringByte(data), map);
                DatagramPacket packet2 = new DatagramPacket(realBytes, realBytes.length, mAddress, Constant.SEND_PORT);
                socket.send(packet2);
                if (socketInterface != null) {
                  socketInterface.sendFinish();
                }
              }
            }
          }
        } catch (UnknownHostException e) {
          e.printStackTrace();
        } catch (SocketException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }.start();
  }


  public static void dispose() {
    isRunning = false;
    if (socket != null && socket.isConnected()) {
      socket.disconnect();
      socket.close();
      socket = null;
    }

    if (mAddress != null) {
      mAddress = null;
    }
  }
}
