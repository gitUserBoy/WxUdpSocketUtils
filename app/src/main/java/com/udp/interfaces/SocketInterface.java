package com.udp.interfaces;

/**
 * @author: wu.xu
 * @data: 2019/4/15/015.
 * <p>
 * 毫无BUG
 */

public interface SocketInterface {
   interface SendSocketInterface {
    void sendFinish();
  }


  interface ReceivedSocketInterface {
    void receiveFinish(String data);
  }
}
