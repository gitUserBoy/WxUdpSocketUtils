package com.udp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.udp.interfaces.SocketInterface;
import com.udp.utils.SocketDataUtils;
import com.udp.utils.WxReceiveUtils;
import com.udp.utils.WxSendUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity implements SocketInterface.SendSocketInterface, SocketInterface.ReceivedSocketInterface{
  TextView send_result;
  TextView received_result;
  TextView received2send_result;
  TextView ip;
  Button button;
  private String TAG = "MainActivity";
  /**
   * IP
   */
  public static String getIp(){
    String localip=null;
    String netip=null;
    try {
      Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
      InetAddress ip = null;
      boolean finded=false;
      while(netInterfaces.hasMoreElements() && !finded){
        NetworkInterface ni=netInterfaces.nextElement();
        Enumeration<InetAddress> address=ni.getInetAddresses();
        while(address.hasMoreElements()){
          ip=address.nextElement();
          if( !ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":")==-1){
            netip=ip.getHostAddress();
            finded=true;
            break;
          }else if(ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":")==-1){
            localip=ip.getHostAddress();
          }
        }
      }
    } catch (SocketException e) {
      e.printStackTrace();
    }
    if(netip!=null && !"".equals(netip)){
      return netip;
    }else{
      return localip;
    }
  }
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ip = findViewById(R.id.ip);
    send_result = findViewById(R.id.send_result);
    received_result = findViewById(R.id.received_result);
    received2send_result = findViewById(R.id.received2send_result);
    button = findViewById(R.id.send);

    //192.168.43.156  // 小米：192.168.10.191  //华为 192.168.43.129
    ip.setText("Constant ip : " + getIp());

    WxSendUtils.setSendSocketInterface(this);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (send_result.getText().equals("发送完成")) {
          send_result.setText("发送数据");
          received_result.setText("接收数据结果");
          return;
        }

        String data = "测试数据";
        byte ttl = 2;
        int crc32 = SocketDataUtils.getCRC32(data.getBytes());
        WxSendUtils.sendMessage(SocketDataUtils.createBytes(data, ttl, crc32));
      }
    });

    WxReceiveUtils.setReceivedSocketInterface(this);
    WxReceiveUtils.receiverMessage();

  }

  @Override
  public void sendFinish() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        send_result.setText("发送完成");
      }
    });
  }

  @Override
  public void receiveFinish(final String data) {
    Log.e(TAG, "receiveFinish: " + data);
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        received_result.setText(data);
      }
    });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    WxReceiveUtils.dispose();
    WxSendUtils.dispose();
  }
}
