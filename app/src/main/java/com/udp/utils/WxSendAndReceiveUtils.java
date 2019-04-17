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
 * 既可以发送，又可以接收。
 */

public class WxSendAndReceiveUtils {
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

  public static void sendMessage(final  byte[] realBytes) {
    //初始化socket
    new Thread() {
      @Override
      public void run() {
        try {
          if (socket == null) {
            socket = new DatagramSocket(Constant.PORT);
          }
          if (mAddress == null) {
            mAddress = InetAddress.getByName(Constant.IP);
          }

          DatagramPacket packet = new DatagramPacket(realBytes, realBytes.length, mAddress, Constant.PORT);
          socket.send(packet);
          if (socketInterface != null) {
            socketInterface.sendFinish();
          }
          while (isRunning) {
            byte[] bytes2 = new byte[3072];
            DatagramPacket packet2 = new DatagramPacket(bytes2, bytes2.length, mAddress, Constant.PORT);
            socket.receive(packet2);
            Map<String, Object> map = SocketDataUtils.analyseBytes(packet.getData());
            if (SocketDataUtils.dataIntegrity(map)) {
              String data = (String) map.get(Constant.KEY_DATA);
              if (socketInterface2 != null) {
                socketInterface2.receiveFinish(data);
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

    if (mAddress!=null){
      mAddress = null;
    }
  }
}
