package com.test.networkvulnerablilitycheck.Util;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.test.networkvulnerablilitycheck.ResultActivity;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import static com.test.networkvulnerablilitycheck.MainActivity.logHistory;
import static com.test.networkvulnerablilitycheck.MainActivity.wifiSSID;
import static com.test.networkvulnerablilitycheck.ProgressActivity.progressText;

public class SearchPort extends AsyncTask<Void, Void, Void> {
  private boolean select = true;
  private Selector selector;
  String[] ipAddr;
  int port_start = 0;
  int port_end;
  int port_sum;
  Activity activity;
  public static int progress;
  final Handler handler;
  static int percentage = 0;
  static int handler_num = 0;
  static String mac_addr;
  static boolean isConnetPort;

  public SearchPort(Activity activity, String[] ip_addr_arr, int port_end) {
    this.activity = activity;
    Log.i("테스트", "AsyncPortscan 생성자 실행됨");
    this.port_end = port_end;
    ipAddr = ip_addr_arr;

    port_sum = port_end - port_start + 2;

    handler = new Handler() {
      public void handleMessage(Message msg) {
        Log.i("테스트", "progress는 " + Integer.toString(handler_num + 1) + "번째");
        Log.i("테스트", "ip :" + ipAddr[handler_num]);

        String[] strarray0 = ipAddr[handler_num].split("\n");
        String[] strarray = strarray0[0].split("\\.");
        if (strarray[strarray.length - 1].equals("1")) {
          progressText.setText(wifiSSID + mac_addr + "\n\n 포트 검사중...");
        } else {
          progressText.setText(ipAddr[handler_num] + "\n\n 포트 검사중...");
        }

        progress = percentage;
        handler_num++;
      }
    };
  }

  @Override //포트검사 진행
  protected Void doInBackground(Void... params) {
    Log.i("테스트", "AsyncPortscan의 doInBackground() 실행됨");
    try {
      int step = 100; //100개씩 검사, 이상하면 에러생김.
      for (int ii = 0; ii < ipAddr.length; ii++) {
        isConnetPort = false;

        int amount;
        amount = 100 / ipAddr.length;

        percentage = ii * amount;
        Message msg = handler.obtainMessage();
        handler.sendMessage(msg);

        String[] strarray0 = ipAddr[ii].split("\n");
        mac_addr = strarray0[1];
        InetAddress ina = InetAddress.getByName(strarray0[0]);
        if (port_sum > step) {
          for (int i = port_start; i <= port_end - step; i = i + step + 1) {
            if (select) {
              if (i + step <= port_end - step) {
                start(ina, i, i + step);
              } else {
                start(ina, i, i + port_end - i);
              }
            }
          }
        } else {
          start(ina, port_start, port_end);
        }

        if (!isConnetPort) {
          String[] strarray = ina.toString().split("\\.");
          if (strarray[strarray.length - 1].equals("1")) {
            logHistory.saveLog(wifiSSID + "(공유기)", "안전. 접속되는 포트가 없습니다.");
          } else {
            logHistory.saveLog(ina.toString().substring(1) + "\n" + mac_addr, "안전. 접속되는 포트가 없습니다.");
          }
        }
      }
    } catch (UnknownHostException e) {
      Log.e("테스트", "에러 " + e.getMessage());
    }
    progress = 100;
    final Intent intent = new Intent(activity, ResultActivity.class);
    intent.putExtra("Dirname", LogHistory.sDName);
    intent.putExtra("activity", "searchport");
    activity.startActivity(intent);
    return null;
  }

  //검색시작
  private void start(final InetAddress ina, final int PORT_START, final int PORT_END) {
    Log.i("테스트", "AsyncPortscan의 start() 실행됨");
    select = true;
    try {
      selector = Selector.open(); //selector로 여러개 소캣 연결
      for (int j = PORT_START; j <= PORT_END; j++) {
        try {
          //소켓 설정
          SocketChannel socket = SocketChannel.open();
          socket.configureBlocking(false);
          socket.connect(new InetSocketAddress(ina, j));
          Data data = new Data();
          data.port = j;
          socket.register(selector, SelectionKey.OP_CONNECT, data);
        } catch (IOException e) {
          Log.e("테스트", "에러 " + e.getMessage());
        }
      }

      while (select && selector.keys().size() > 0) {
        if (selector.select(1500) > 0) { //타임아웃 설정
          synchronized (selector.selectedKeys()) {
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
              SelectionKey key = (SelectionKey) iterator.next();
              try {
                if (!key.isValid()) {
                  continue;
                }
                // data 불러옴 (port 읽기위함)
                final Data data = (Data) key.attachment();

                //각 상황에 따른 처리
                if (key.isConnectable()) { //연결시도시 연결될때
                  if (((SocketChannel) key.channel()).finishConnect()) {
                    Log.i("테스트", ina.toString());
                    Log.i("테스트", "연결가능한 포트 :" + Integer.toString(data.port));

                    isConnetPort = true;

                    if (IoTCheck.checkEncrypt(ina.toString().substring(1), data.port)) {
                      String[] strarray = ina.toString().split("\\.");
                      if (strarray[strarray.length - 1].equals("1")) {
                        logHistory.saveLog(wifiSSID + "(공유기)", "확인필요. 접속되는 포트 : " + Integer.toString(data.port));
                      } else {
                        logHistory.saveLog(ina.toString().substring(1) + "\n" + mac_addr, "확인필요. 접속되는 포트 : " + Integer.toString(data.port));
                        logHistory.saveLog(ina.toString().substring(1) + "\n" + mac_addr, "안전. " + Integer.toString(data.port) + "번 포트 ssl 보안 접속 확인됨");
                      }
                    } else {
                      String[] strarray = ina.toString().split("\\.");
                      if (strarray[strarray.length - 1].equals("1")) {
                        logHistory.saveLog(wifiSSID + "(공유기)", "확인필요. 접속되는 포트 : " + Integer.toString(data.port));
                      } else {
                        logHistory.saveLog(ina.toString().substring(1) + "\n" + mac_addr, "확인필요. 접속되는 포트 : " + Integer.toString(data.port));
                        logHistory.saveLog(ina.toString().substring(1) + "\n" + mac_addr, "경고. " + Integer.toString(data.port) + "번 포트 ssl 보안 접속 확인안됨 ");
                      }
                    }

                  }
                } else if (key.isReadable() || key.isWritable()) { //이미 연결되어있는 상태일때
                  Log.i("테스트", "연결가능한 포트 :" + Integer.toString(data.port));

                  isConnetPort = true;

                  if (IoTCheck.checkEncrypt(ina.toString().substring(1), data.port)) {
                    String[] strarray = ina.toString().split("\\.");
                    if (strarray[strarray.length - 1].equals("1")) {
                      logHistory.saveLog(wifiSSID + "(공유기)", "확인필요. 접속되는 포트 : " + Integer.toString(data.port));
                    } else {
                      logHistory.saveLog(ina.toString().substring(1) + "\n" + mac_addr, "확인필요. 접속되는 포트 : " + Integer.toString(data.port));
                      logHistory.saveLog(ina.toString().substring(1) + "\n" + mac_addr, "안전. " + Integer.toString(data.port) + "번 포트 ssl 보안 접속 확인됨");
                    }
                  } else {
                    String[] strarray = ina.toString().split("\\.");
                    if (strarray[strarray.length - 1].equals("1")) {
                      logHistory.saveLog(wifiSSID + "(공유기)", "확인필요. 접속되는 포트 : " + Integer.toString(data.port));
                    } else {
                      logHistory.saveLog(ina.toString().substring(1) + "\n" + mac_addr, "확인필요. 접속되는 포트 : " + Integer.toString(data.port));
                      logHistory.saveLog(ina.toString().substring(1) + "\n" + mac_addr, "경고. " + Integer.toString(data.port) + "번 포트 ssl 보안 접속 확인안됨 ");
                    }
                  }
                }

              } catch (ConnectException e) {
                closeSelection(key);
              } catch (Exception e) {
                closeSelection(key);
                Log.e("테스트", "에러 " + e.getMessage());
              } finally {
                iterator.remove();
              }
            }
          }
        } else {
          //select 연결안되면 종료
          final Iterator<SelectionKey> iterator = selector.keys().iterator();
          while (iterator.hasNext()) {
            final SelectionKey key = (SelectionKey) iterator.next();
            closeSelection(key);
          }
        }
      }
    } catch (IOException e) {
      Log.e("테스트", "에러 " + e.getMessage());
    } finally {

      //selector 종료
      try {
        if (selector.isOpen()) {
          synchronized (selector.keys()) {
            Iterator<SelectionKey> iterator = selector.keys().iterator();
            while (iterator.hasNext()) {
              closeSelection((SelectionKey) iterator.next());
            }
            selector.close();
          }
        }
      } catch (IOException e) {
        Log.e("테스트", "종료에러 " + e.getMessage());
      }
    }
  }

  private void closeSelection(SelectionKey key) {
    synchronized (key) {
      if (key == null || !key.isValid()) {
        return;
      }
      if (key.channel() instanceof SocketChannel) {
        Socket socket = ((SocketChannel) key.channel()).socket();
        try {
          if (!socket.isInputShutdown()) socket.shutdownInput();
        } catch (IOException ex) {
        }
        try {
          if (!socket.isOutputShutdown()) socket.shutdownOutput();
        } catch (IOException ex) {
        }
        try {
          socket.close();
        } catch (IOException ex) {
        }
      }
      try {
        key.channel().close();
      } catch (IOException ex) {
      }
    }
  }

  private class Data {
    public int port;
  }
}