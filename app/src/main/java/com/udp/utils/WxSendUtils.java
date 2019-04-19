package com.udp.utils;


import com.udp.constant.Constant;
import com.udp.interfaces.SocketInterface;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * @author: wu.xu
 * @data: 2019/4/15/015.
 * <p>
 * 毫无BUG
 *
 * 只能发送数据
 */

public class WxSendUtils {
  private static InetAddress mAddress;
  private static DatagramSocket socket;

  private static SocketInterface.SendSocketInterface socketInterface;

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
            socket = new DatagramSocket();
          }

          if (mAddress == null) {
            mAddress = InetAddress.getByName(Constant.IP);
          }

          DatagramPacket packet = new DatagramPacket(realBytes, realBytes.length, mAddress, Constant.SEND_PORT);
          socket.send(packet);

          if (socketInterface!=null){
            socketInterface.sendFinish();
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


  public static void  dispose(){
    if (socket!=null && socket.isConnected()){
      socket.disconnect();
      socket.close();
      socket = null;
    }

    if (mAddress!=null){
      mAddress = null;
    }
  }
}
