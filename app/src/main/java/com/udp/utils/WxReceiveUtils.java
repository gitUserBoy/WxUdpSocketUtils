package com.udp.utils;

import com.udp.constant.Constant;
import com.udp.interfaces.SocketInterface;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * @author: wu.xu
 * @data: 2019/4/15/015.
 * <p>
 * 毫无BUG
 *
 * 只能接收数据
 */

public class WxReceiveUtils {
  private static DatagramSocket socket;

  private static SocketInterface.ReceivedSocketInterface socketInterface;

  private static boolean isRunning = true;

  public static void setReceivedSocketInterface(SocketInterface.ReceivedSocketInterface receivedSocketInterface) {
    socketInterface = receivedSocketInterface;
  }

  public static void receiverMessage() {
    new Thread() {
      @Override
      public void run() {
        try {
          if (socket == null) {
            socket = new DatagramSocket(Constant.RECEIVED_PORT);
          }
          while (isRunning) {
            byte[] bytes = new byte[3072];
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
            socket.receive(packet);

            Map<String, Object> map = SocketDataUtils.analyseBytes(packet.getData());
            if (map!=null){
              int  crc32 = (int) map.get(Constant.KEY_CRC32);
              String data = (String) map.get(Constant.KEY_DATA);
              if (socketInterface != null) {
                socketInterface.receiveFinish("crc32-"+crc32+data);
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
  }
}
