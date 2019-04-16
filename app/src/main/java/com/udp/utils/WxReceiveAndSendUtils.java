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

  public static void setReceivedSocketInterface(SocketInterface.ReceivedSocketInterface receivedSocketInterface) {
    socketInterface2 = receivedSocketInterface;
  }

  public static void setSendSocketInterface(SocketInterface.SendSocketInterface sendSocketInterface){
    socketInterface = sendSocketInterface;
  }

  public static void receivedAndSend() {
    //初始化socket
    new Thread() {
      @Override
      public void run() {
        try {
          if (socket == null) {
            socket = new DatagramSocket();
          }
          if (mAddress == null) {
            mAddress = InetAddress.getByName(Constant.IP);
          }

          /*接收消息*/
          while (isRunning) {
            byte[] bytes2 = new byte[3072];
            DatagramPacket packet2 = new DatagramPacket(bytes2, bytes2.length, mAddress, Constant.PORT);
            socket.receive(packet2);
            Map<String, Object> map = SocketDataUtils.analyseBytes(packet2.getData());
            /**
             *接收的数据包如果完整处理数据,判断TTL是否转发
             */
            if (SocketDataUtils.dataIntegrity(map)) {
              String data = (String) map.get(Constant.KEY_DATA);
              if (socketInterface2 != null) {
                socketInterface2.receiveFinish(data);
              }

              byte ttl = (byte) map.get(Constant.KEY_TTL);
              byte newTtl = (byte) (ttl - 1);

              if (newTtl > 0) {
                map.put(Constant.KEY_TTL, newTtl);
                /*发送消息*/
                byte[] realBytes = SocketDataUtils.assembleBytes(data.getBytes(), map);
                DatagramPacket packet = new DatagramPacket(realBytes, realBytes.length, mAddress, Constant.PORT);
                socket.send(packet);
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
