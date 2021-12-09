package com.test.networkvulnerablilitycheck;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.test.networkvulnerablilitycheck.Util.PwdCheck;
import com.test.networkvulnerablilitycheck.Util.RouterCheck;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.test.networkvulnerablilitycheck.MainActivity.logHistory;

public class SearchDeviceActivity extends AppCompatActivity {

  TextView tvScanning;
  ArrayList<InetAddress> inetAddresses;
  ArrayList<String> items;
  ListView tvResult;
  ArrayAdapter adapter;
  Intent intent;
  String[] asIP;
  ArrayList<String> sIp = new ArrayList<String>();
  int pList = 0;
  public int end = 0;
  public int size = 0;
  public int start = 0;
  public int i;
  public static int j;
  ArrayList<String> canonicalHostNames;
  SearchIp searchIp;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

    String net = RouterCheck.getIpAddress();
    Log.i("테스트", "현재 연결된 ip주소 :" + net);
    String[] a = net.split("\\.");
    searchIp = new SearchIp(a[0] + "." + a[1] + "." + a[2] + ".", 1, 255);

    intent = new Intent(this, ProgressActivity.class);

    tvScanning = (TextView) findViewById(R.id.Scanning);

    items = new ArrayList<String>();
    adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, items);
    if (inetAddresses == null) {
      inetAddresses = new ArrayList<>();
    }
    inetAddresses.clear();

    if (canonicalHostNames == null) {
      canonicalHostNames = new ArrayList<>();
    }
    canonicalHostNames.clear();

    searchIp.execute(); //실행 ㄱㄱ
  }


  public class SearchIp extends AsyncTask<Void, Void, Void> {

    private final static int THREADS = 20; //뜨레드 부분
    private ExecutorService executor;
    protected String ip;
    protected int start;
    protected int end;

    public SearchIp(String ip, int start, int end) {
      super();
      this.ip = ip;
      this.start = start;
      this.end = end;
      Log.i("테스트", "DefaultDiscovery onCreate 시작");
    }

    ProgressDialog asyncDialog = new ProgressDialog(SearchDeviceActivity.this);

    @Override
    protected void onPreExecute() {

      asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
      asyncDialog.setMessage("디바이스 검색중..");
      asyncDialog.setCanceledOnTouchOutside(false);
      asyncDialog.setCancelable(true);
      asyncDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
          if (keyCode == KeyEvent.KEYCODE_BACK) {
            searchIp.cancel(true);
            finish();
            dialog.dismiss();
          }
          return false;
        }
      });

      asyncDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {
          searchIp.cancel(true);
        }
      });

      // show dialog
      asyncDialog.show();

      super.onPreExecute();
    }

    @Override
    protected void onCancelled() {
      super.onCancelled();
    }

    @Override
    protected void onPostExecute(Void aVoid) {

      for (int i = 0; i < inetAddresses.size(); i++) {
        items.add(canonicalHostNames.get(i));
      }

      adapter.notifyDataSetChanged();
      asyncDialog.cancel();

      LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.iotcheck_activity, null);
      setContentView(linearLayout);

      tvResult = (ListView) findViewById(R.id.Result);
      tvResult.setAdapter(adapter);

      Button confButton = (Button) findViewById(R.id.confirm);

      confButton.setOnClickListener(new Button.OnClickListener() {
        public void onClick(View view) {
          SparseBooleanArray checkedItems = tvResult.getCheckedItemPositions();
          int counter = 0;
          if (checkedItems != null) {
            int length = checkedItems.size();
            for (int i = 0; i < length; i++) {
              if (checkedItems.get(checkedItems.keyAt(i))) {
                counter++;
              }
            }
          }

          int count = counter;
          asIP = new String[count];

          for (int i = 0, j = 0; i < items.size(); i++) {
            if (checkedItems.get(i) != false) {
              asIP[j] = items.get(i);
              j++;
            }
          }

          for (int i = checkedItems.size() - 1; i >= 0; i--) {
            Log.i("테스트", "선택한 ip :" + asIP[i]);
            final AlertDialog.Builder builder = new AlertDialog.Builder(SearchDeviceActivity.this);

            String[] strarray0 = asIP[i].toString().split("\n");
            String[] strarray = strarray0[0].split("\\.");
            Pattern pattern1 = Pattern.compile("(" + "검사 진행중인 스마트폰" + ")");
            Matcher match1 = pattern1.matcher(strarray0[1]);
            if (!strarray[strarray.length - 1].equals("1") && !match1.find()) {
              builder.setTitle(asIP[i]);
              builder.setMessage("IOT 기기 비밀번호를 입력해 주세요!");
              final EditText input = new EditText(SearchDeviceActivity.this);
              LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                      LinearLayout.LayoutParams.MATCH_PARENT,
                      LinearLayout.LayoutParams.MATCH_PARENT);
              input.setTransformationMethod(PasswordTransformationMethod.getInstance());
              input.setLayoutParams(lp);
              builder.setView(input);

              sIp.add(asIP[i]);
              if (i != checkedItems.size() - 1) {

                builder.setPositiveButton("다음", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    String WifiPwdCheckResult;
                    PwdCheck pwdCheck = new PwdCheck();
                    WifiPwdCheckResult = pwdCheck.verify(input.getText().toString());
                    WifiPwdCheckResult = WifiPwdCheckResult + "\r\n" + pwdCheck.Pwd10000(getApplicationContext(), input.getText().toString());
                    logHistory.saveLog(sIp.get(pList++), WifiPwdCheckResult);
                  }
                });

                builder.setNegativeButton("넘어가기", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    logHistory.saveLog(sIp.get(pList++), "확인필요. 기기 비밀번호 체크 필요합니다.");
                  }
                });


              } else {
                builder.setPositiveButton("검사시작", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    String WifiPwdCheckResult;
                    PwdCheck pwdCheck = new PwdCheck();
                    WifiPwdCheckResult = pwdCheck.verify(input.getText().toString());
                    WifiPwdCheckResult = WifiPwdCheckResult + "\r\n" + pwdCheck.Pwd10000(getApplicationContext(), input.getText().toString());
                    logHistory.saveLog(sIp.get(pList++), WifiPwdCheckResult);

                    intent.putExtra("ip", asIP);
                    startActivity(intent);
                    dialog.cancel();
                  }
                });

                builder.setNegativeButton("넘어가기", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    logHistory.saveLog(sIp.get(pList++), "확인필요. 기기 비밀번호 체크 필요합니다.");

                    intent.putExtra("ip", asIP);
                    startActivity(intent);
                    dialog.cancel();
                  }
                });
              }

              builder.create();
              builder.show();
            }
          }


          // 모든 선택 상태 초기화.
          tvResult.clearChoices();
          adapter.notifyDataSetChanged();
        }
      });

      Button cancButton = (Button) findViewById(R.id.cancel);

      cancButton.setOnClickListener(new Button.OnClickListener() {
        public void onClick(View view) {
          finish();
        }
      });

      super.onPostExecute(aVoid);
    }

    @Override
    protected Void doInBackground(Void... params) {
      Log.i("테스트", "DefaultDiscovery의 doInBackground() 실행됨");
      executor = Executors.newFixedThreadPool(THREADS);

      for (int i = start; i < end; i++) {
        if (!executor.isShutdown()) {
          executor.execute(new CheckRunnable(ip + Integer.toString(i)));
        }
      }

      executor.shutdown();
      try {
        if (!executor.awaitTermination(3600, TimeUnit.SECONDS)) {
          executor.shutdownNow();
        }
      } catch (InterruptedException e) {
        Log.e("테스트", "강제종료 에러뜸 : " + e.getMessage());
        executor.shutdownNow();
        Thread.currentThread().interrupt();
      }

      return null;
    }

    //ip 검사 진행
    private class CheckRunnable implements Runnable {
      private String addr;

      CheckRunnable(String addr) {
        this.addr = addr;
      }


      public void run() {

        //주소는 addr로 저장됨
        String[] ip_addr_arr = addr.split("\\.");
        int a = Integer.parseInt(ip_addr_arr[0]);
        int b = Integer.parseInt(ip_addr_arr[1]);
        int c = Integer.parseInt(ip_addr_arr[2]);
        int d = Integer.parseInt(ip_addr_arr[3]);
        byte[] ip_addr_byte = {(byte) a, (byte) b, (byte) c, (byte) d};

        String mac_addr = getMacAddress(addr);

        InetAddress checkAddress = null;
        try {
          checkAddress = InetAddress.getByAddress(ip_addr_byte);
        } catch (UnknownHostException e) {
        }
        //검사진행 부분 try, catch부분 - 네트워크 응답에 기다리는 시간때문에 apr 지속체크 해야함
        if ("00:00:00:00:00:00".equals(mac_addr)) {
          try {
            if (checkAddress.isReachable(500)) {
              inetAddresses.add(checkAddress);
              mac_addr = getMacAddress(addr);

              if (d == 1) {
                canonicalHostNames.add(checkAddress.getCanonicalHostName() + "\n (공유기)");
              } else if ("00:00:00:00:00:00".equals(mac_addr)) {
                canonicalHostNames.add(checkAddress.getCanonicalHostName() + "\n (검사 진행중인 스마트폰)");
              } else {
                canonicalHostNames.add(checkAddress.getCanonicalHostName() + "\n (맥주소 : " + mac_addr + ")");
              }


              Log.i("테스트", addr + "의 발견된 mac address : " + mac_addr);
            } else {
              Thread.sleep(100);
              mac_addr = getMacAddress(addr);
              if (!"00:00:00:00:00:00".equals(mac_addr)) {
                inetAddresses.add(checkAddress);
                canonicalHostNames.add(checkAddress.getCanonicalHostName() + "\n (맥주소 : " + mac_addr + ")");
                Log.i("테스트", addr + "의 발견된 mac address : " + mac_addr);
              } else {
                Thread.sleep(100);
                mac_addr = getMacAddress(addr);
                if (!"00:00:00:00:00:00".equals(mac_addr)) {
                  inetAddresses.add(checkAddress);
                  canonicalHostNames.add(checkAddress.getCanonicalHostName() + "\n (맥주소 : " + mac_addr + ")");
                  Log.i("테스트", addr + "의 발견된 mac address : " + mac_addr);
                }
              }
            }

          } catch (UnknownHostException e) {
          } catch (IOException e) {
          } catch (InterruptedException e) {
          }

        } else {
          inetAddresses.add(checkAddress);
          if (d == 1) {
            canonicalHostNames.add(checkAddress.getCanonicalHostName() + "\n (공유기)");
          } else {
            canonicalHostNames.add(checkAddress.getCanonicalHostName() + "\n (맥주소 : " + mac_addr + ")");
          }
          Log.i("테스트", addr + "의 저장된 mac address : " + mac_addr);
        }
      }
    }

    //저장되어 있는 mac 주소 가져옴
    public String getMacAddress(String ip) {
      String result_macaddr = "00:00:00:00:00:00";
      int BUF = 8 * 1024;

      BufferedReader bufferedReader = null;
      try {
        if (ip != null) {
          String format = String.format("^%s\\s+0x1\\s+0x2\\s+([:0-9a-fA-F]+)\\s+\\*\\s+\\w+$", ip.replace(".", "\\."));
          Pattern pattern = Pattern.compile(format);
          bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"), BUF);
          String line;
          Matcher matcher;
          while ((line = bufferedReader.readLine()) != null) {
            matcher = pattern.matcher(line);
            if (matcher.matches()) {
              result_macaddr = matcher.group(1);
              break;
            }
          }
        } else {
          Log.e("테스트", "ip is null");
        }
      } catch (IOException e) {
        Log.e("테스트", "ARP 파일을 열 수 없음: " + e.getMessage());
        return result_macaddr;
      } finally {
        try {
          if (bufferedReader != null) {
            bufferedReader.close();
          }
        } catch (IOException e) {
          Log.e("테스트", "닫히지 않음" + e.getMessage());
        }
      }
      return result_macaddr;
    }
  }


}