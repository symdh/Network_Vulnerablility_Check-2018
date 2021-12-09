package com.test.networkvulnerablilitycheck;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.test.networkvulnerablilitycheck.Adapter.MainListAdapter;
import com.test.networkvulnerablilitycheck.Util.LogHistory;
import com.test.networkvulnerablilitycheck.Util.PwdCheck;
import com.test.networkvulnerablilitycheck.Util.RouterCheck;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


public class MainActivity extends AppCompatActivity {

  private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
  Wificheck2 wifichek;
  static public LogHistory logHistory;
  static public WifiManager wifiManager;
  ImageButton button;
  EditText input;
  static public String wifiSSID;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    //로그 넣는 부분
    logHistory = new LogHistory(getFilesDir().getAbsolutePath());
    input = new EditText(MainActivity.this);
    wifichek = new Wificheck2();
    int limitload = 4;
    final String[] loadlog = logHistory.loadDir(limitload);
    ListView list;
    String[] date = new String[limitload];
    String[] router = new String[limitload];
    String[] amount = new String[limitload];

    //검사내역이 적거나 없을경우
    if (loadlog.length / 2 < limitload) {
      limitload = loadlog.length / 2;
    }

    for (int i = 0; i < limitload; i++) {
      String str_loadlog = loadlog[i * 2 + 1];
      String[] strarray = str_loadlog.split("//%%//");
      date[i] = strarray[0];
      router[i] = strarray[1];
      if (strarray.length == 2) {
        amount[i] = "중단된 검사기록";
      } else {
        amount[i] = strarray[2] + "개";
      }
    }

    MainListAdapter listAdapter = new
            MainListAdapter(MainActivity.this, date, router, amount);
    list = (ListView) findViewById(R.id.mainlist);
    list.setAdapter(listAdapter);

    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
    }

    wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

    button = (ImageButton) findViewById(R.id.check_start);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        wificheck();
      }
    });


    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(MainActivity.this, ResultActivity.class);
        if (position < loadlog.length - 1) {
          intent.putExtra("Dirname", loadlog[position * 2]);
          startActivity(intent);
        }
      }
    });
  }

  public void onClickLog(View view) {
    Intent intent = new Intent(this, LogActivity.class);
    startActivity(intent);
  }

  public void wificheck() {
    ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

    if (mWifi.isConnected()) {
      final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
      builder.setTitle("와이파이 비밀번호 입력");
      builder.setMessage("정확한 검사를 위해 필요합니다!");

      LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.MATCH_PARENT,
              LinearLayout.LayoutParams.WRAP_CONTENT);
      input.setTransformationMethod(PasswordTransformationMethod.getInstance());
      input.setLayoutParams(lp);
      builder.setView(input);

      builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          wifichek.execute();
        }
      });
      builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          dialog.cancel();
        }
      });

      builder.create();
      builder.show();
    } else {
      AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
      builder.setTitle("경고");
      builder.setMessage("와이파이를 연결해주세요!");
      builder.setNegativeButton("확인", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          dialog.cancel();
        }
      });
      builder.create();
      builder.show();
    }
  }

  private class Wificheck2 extends AsyncTask<String, Void, Boolean> {

    private ProgressDialog progress = new ProgressDialog(MainActivity.this);


    protected void onPreExecute() {

      this.progress.setMessage("와이파이 비밀번호 검사중..");
      this.progress.setCanceledOnTouchOutside(false);
      this.progress.setCancelable(true);
      this.progress.show();
    }

    protected Boolean doInBackground(final String... args) {
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      String WifiPwd = "";
      if (input.getText().toString().length() != 0) {
        WifiPwd = input.getText().toString();

      } else {
        WifiPwd = "";
      }
      String WifiPwdCheckResult;
      RouterCheck routerCheck = RouterCheck.getInstance();
      if (routerCheck.isConnectWIFI(getApplicationContext())) {
        routerCheck.setPwd(WifiPwd);
        if (routerCheck.WifiPwdCheck(getApplicationContext())) {

          if (routerCheck.networkId != routerCheck.networkId_4) {
            Log.i("테스트", "네트워크 변경이 감지되었습니다. 확인해주세요"); //알림
            progress.cancel();
          } else {

            String WifiEncryptChResult = "";

            //권한 확인
            int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
              Log.i("테스트", "권한없음");
              WifiEncryptChResult = "확인필요. 위치 권한이 없어 Wifi 통신 암호 형식을 확인할 수 없습니다.";
            } else {
              Log.i("테스트", "권한있음");
              WifiEncryptChResult = routerCheck.EncryptCheck(getApplicationContext());
            }

            TimeZone time;
            Date mDate = new Date();
            SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");

            time = TimeZone.getTimeZone("Asia/Seoul");
            mSimpleDateFormat.setTimeZone(time);
            String mTime = mSimpleDateFormat.format(mDate);

            if (routerCheck.networkId_3 == -1 && (permissionCheck == PackageManager.PERMISSION_DENIED)) {
              //위치권한 거부되어 있을떄, -1이 뜨는경우
              Log.i("테스t", "위치 권한이 없어, 오픈된 와이파이인지 확인할 수 없음");
              wifiSSID = routerCheck.mSSID;

              logHistory.mklog(mTime + "//%%//" + wifiSSID);
              logHistory.saveLog(wifiSSID + "(공유기)", WifiEncryptChResult);
              logHistory.saveLog(wifiSSID + "(공유기)", "확인필요. 위치 권한이 없어, 오픈된 와이파이인지 확인할 수 없습니다. 정확한 검사를 위해 위치권한 확인이 필요합니다.");
              logHistory.saveLog(wifiSSID + "(공유기)", routerCheck.isHiddenSSID(getApplicationContext()));

              startActivity(new Intent(MainActivity.this, SearchDeviceActivity.class));
              progress.cancel();
            } else if (routerCheck.networkId_3 == -1 && WifiEncryptChResult.contains("확인필요")) {
              Log.i("테스t", "오픈된 wifi, wifi의 비밀번호가 설정되어 있지 않음");
              wifiSSID = routerCheck.mSSID;

              logHistory.mklog(mTime + "//%%//" + wifiSSID);
              logHistory.saveLog(wifiSSID + "(공유기)", WifiEncryptChResult);
              logHistory.saveLog(wifiSSID + "(공유기)", "경고. 오픈된 wifi로 wifi의 비밀번호가 설정되어 있지 않습니다");
              logHistory.saveLog(wifiSSID + "(공유기)", routerCheck.isHiddenSSID(getApplicationContext()));

              startActivity(new Intent(MainActivity.this, SearchDeviceActivity.class));
              progress.cancel();
            } else if (routerCheck.networkId_3 == -1 && !WifiEncryptChResult.contains("확인필요")) {
              Log.i("테스트", "wifi의 비밀번호 입력안됨");
              wifiSSID = routerCheck.mSSID;

              logHistory.mklog(mTime + "//%%//" + routerCheck.mSSID);
              logHistory.saveLog(wifiSSID + "(공유기)", WifiEncryptChResult);
              logHistory.saveLog(wifiSSID + "(공유기)", "확인필요. wifi의 비밀번호가 틀렸습니다");
              logHistory.saveLog(wifiSSID + "(공유기)", routerCheck.isHiddenSSID(getApplicationContext()));

              startActivity(new Intent(MainActivity.this, SearchDeviceActivity.class));
              progress.cancel();
            } else {
              Log.i("테스트", "비밀번호가 정상, 계속 진행");

              PwdCheck pwdCheck = new PwdCheck();
              WifiPwdCheckResult = pwdCheck.verify(WifiPwd);
              WifiPwdCheckResult = WifiPwdCheckResult + "\r\n" + pwdCheck.Pwd10000(getApplicationContext(), WifiPwd);
              wifiSSID = routerCheck.mSSID;

              logHistory.mklog(mTime + "//%%//" + wifiSSID);
              logHistory.saveLog(wifiSSID + "(공유기)", WifiEncryptChResult);
              logHistory.saveLog(wifiSSID + "(공유기)", WifiPwdCheckResult);
              logHistory.saveLog(wifiSSID + "(공유기)", routerCheck.isHiddenSSID(getApplicationContext()));

              startActivity(new Intent(MainActivity.this, SearchDeviceActivity.class));
              progress.cancel();
            }
          }
        } else {
          Log.i("테스트", "비밀번호가 맞지 않습니다.");
          progress.cancel();
        }
      } else {
        Log.i("테스트", "Wifi 연결이 정확하지 않음");
        progress.cancel();
      }
      return null;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
      progress.cancel();
      if (progress.isShowing()) {
        progress.dismiss();
      }

    }


  }
}

