package com.udp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.udp.interfaces.SocketInterface;
import com.udp.utils.SocketDataUtils;
import com.udp.utils.WxReceiveAndSendUtils;
import com.udp.utils.WxReceiveUtils;
import com.udp.utils.WxSendUtils;

/**
 * UDP 通信发送方必须指定接收方ip，如果是本机app就写ip：127.0.0.1
 */
public class MainActivity extends AppCompatActivity implements SocketInterface.SendSocketInterface, SocketInterface.ReceivedSocketInterface{
  TextView send_result;
  TextView received_result;
  TextView received2send_result;
  TextView ip;
  Button button;
  private String TAG = "MainActivity";

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
    ip.setText("Constant ip : " + SocketDataUtils.getIpAddress());

    WxSendUtils.setSendSocketInterface(this);

    WxReceiveUtils.setReceivedSocketInterface(this);
    WxReceiveUtils.receiverMessage();
//    WxReceiveAndSendUtils.setReceivedSocketInterface(this);
//    WxReceiveAndSendUtils.setSendSocketInterface(this);
//    WxReceiveAndSendUtils.receivedAndSend();

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
        byte[] bytes = SocketDataUtils.getStringByte(data);
        int crc32 = SocketDataUtils.getCRC32(bytes);
        WxSendUtils.sendMessage(SocketDataUtils.createBytes(data, ttl, crc32));
      }
    });
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
    WxReceiveAndSendUtils.dispose();
  }
}
