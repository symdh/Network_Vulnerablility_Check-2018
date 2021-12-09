package com.test.networkvulnerablilitycheck;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.test.networkvulnerablilitycheck.Adapter.CustomExpandableListViewAdapter;
import com.test.networkvulnerablilitycheck.List.ChildListData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.test.networkvulnerablilitycheck.MainActivity.logHistory;


public class ResultActivity extends AppCompatActivity {
  public ExpandableListView expandableListView; // ExpandableListView 변수 선언
  public CustomExpandableListViewAdapter mCustomExpListViewAdapter; // 위 ExpandableListView를 받을 CustomAdapter(2번 class에 해당)를 선언
  public ArrayList<String> parentList; // ExpandableListView의 Parent 항목이 될 List 변수 선언
  public ArrayList<ChildListData> fruit; // ExpandableListView의 Child 항목이 될 List를 각각 선언
  public ArrayList<ChildListData> vegetables;
  public ArrayList<ChildListData> etc;
  public HashMap<String, ArrayList<ChildListData>> childList; // 위 ParentList와 ChildList를 연결할 HashMap 변수 선언

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.result_activity); // activity_main.xml을 MainActivity에 연결

    Intent intent = getIntent();

    String[][] asLog = logHistory.loadLog(intent.getStringExtra("Dirname"));

    parentList = new ArrayList<String>();
    childList = new HashMap<String, ArrayList<ChildListData>>();

    int isinfo = 0;
    int iswarn = 0;
    for (int i = 1; i < asLog.length; ++i) {
      parentList.add(asLog[i][0]);
      fruit = new ArrayList<ChildListData>();


      for (int j = 2; j < asLog[i].length; ++j) {

        ChildListData apple;
        String[] strarray = asLog[i][j].split("\\. "); //나누어서 무엇인지 확인
        switch (strarray[0]) {
          case "안전":
            asLog[i][j] = asLog[i][j].substring(4);
            apple = new ChildListData(getResources().getDrawable(R.drawable.pass), asLog[i][j]);
            fruit.add(apple);
            break;
          case "확인필요":
            asLog[i][j] = asLog[i][j].substring(6);
            apple = new ChildListData(getResources().getDrawable(R.drawable.warn), asLog[i][j]);
            fruit.add(apple);
            isinfo++;
            break;

          case "경고":
            asLog[i][j] = asLog[i][j].substring(4);
            apple = new ChildListData(getResources().getDrawable(R.drawable.weak), asLog[i][j]);
            fruit.add(apple);
            iswarn++;
            break;
        }
      }
      childList.put(parentList.get(i - 1), fruit);
    }

    logHistory.addInfo(Integer.toString(iswarn));

    TextView warm = (TextView) findViewById(R.id.weakness);
    warm.setText(iswarn + "개 취약점, " + isinfo + "개 주의사항 발견!");

    expandableListView = (ExpandableListView) findViewById(R.id.iotresult);
    mCustomExpListViewAdapter = new CustomExpandableListViewAdapter(this, parentList, childList);
    expandableListView.setAdapter(mCustomExpListViewAdapter);
    expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
      @Override
      public void onGroupExpand(int groupPosition) {
      }
    });
    expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
      @Override
      public void onGroupCollapse(int groupPosition) {
      }
    });

    expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
      @Override
      public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        //각 항목 클릭했을때
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v1 = inflater.inflate(R.layout.detail_activity, null);
        TextView txt = (TextView) v1.findViewById(R.id.detail_1);

        //groupPosition 0은 항상 공유기로 저장되므로 참고할것
        ArrayList<ChildListData> test = childList.get(parentList.get(groupPosition));
        String showResult = test.get(childPosition).getText();
        //Log.i("테스트", "string은 : " + showResult) ;

        if(Pattern.compile("(" + "현재 비밀번호는 적절한 길이" + ")").matcher(showResult).find()) {
          txt.setText("9자리 이상의 적절한 길이의 비밀번호입니다.");
        } else if(Pattern.compile("(" + "현재 비밀번호는 강력한 조합" + ")").matcher(showResult).find()) {
          txt.setText("비밀번호 조합이 강력합니다.");
        } else if(Pattern.compile("(" + "현재 비밀번호는 아주 강력한 조합" + ")").matcher(showResult).find()) {
          txt.setText("비밀번호 조합이 아주 강력합니다.");
        } else if(Pattern.compile("(" + "WPA2은 보안수준이 높습니다" + ")").matcher(showResult).find()) {
          txt.setText("WiFi 암호형식인 WPA2는 안전합니다.");
        } else if(Pattern.compile("(" + "보안수준이 높습니다" + ")").matcher(showResult).find()) {
          txt.setText("WiFi 암호형식인 WPA/WPA2는 안전합니다.");
        } else if(Pattern.compile("(" + "네트워크 이름이 비공개" + ")").matcher(showResult).find()) {
          txt.setText("네트워크 이름이 비공개 되어있어 안전합니다.");
        } else if(Pattern.compile("(" + "접속되는 포트가 없습니다" + ")").matcher(showResult).find()) {
          txt.setText("해당 기기에서 응답하는 포트가 없어 안전합니다.");
        } else if(Pattern.compile("(" + "포트 ssl 보안 접속 확인됨" + ")").matcher(showResult).find()) {
          txt.setText("해당 포트는 SSL 보안 접속이 가능하여 도청위험이 없어 안전합니다.");
        } else if(Pattern.compile("(" + "접속되는 포트" + ")").matcher(showResult).find()) {
          txt.setText("응답하는 포트가 있습니다. 해당 포트를 열어놓은 목적을 확인하여 열어놓을 필요가 없는 포트라면 닫는것이 안전합니다.");
        } else if(Pattern.compile("(" + "네트워크 이름은 비공개" + ")").matcher(showResult).find()) {
          txt.setText("공유기 설정에서 네트워크 이름을 비공개 처리한다면 비인가자의 WiFi 목록에 검색되지 없습니다.");
        } else if(Pattern.compile("(" + "암호형식이 발견되지 않았습니다" + ")").matcher(showResult).find()) {
          txt.setText("검사시 암호형식이 감지되지 않았습니다. 공유기 설정에서 무선 암호형식을 확인해 주시기 바랍니다.");
        } else if(Pattern.compile("(" + "기기 비밀번호 체크 필요" + ")").matcher(showResult).find()) {
          txt.setText("기기 비밀번호 복잡성 확인을 위해 검사시 비밀번호를 입력하셔야 합니다.");
        } else if(Pattern.compile("(" + "특수문자, 숫자, 영문자의 조합을 권장" + ")").matcher(showResult).find()) {
          txt.setText("비밀번호는 특수문자, 숫자, 영문자의 조합을 권장합니다.");
        } else if(Pattern.compile("(" + "비밀번호는 9자리 이상" + ")").matcher(showResult).find()) {
          txt.setText("비밀번호는 최소한 9자리 이상으로 권장합니다.");
        } else if(Pattern.compile("(" + "wifi의 비밀번호가 틀렸습니다" + ")").matcher(showResult).find()) {
          txt.setText("정확한 비밀번호를 입력하여 보안성을 확인하십시오.");
        } else if(Pattern.compile("(" + "위치 권한이 없어 오픈된" + ")").matcher(showResult).find()) {
          txt.setText("어플리케이션 위치 권한을 수락해야 정확한 검사가 가능합니다.");
        } else if(Pattern.compile("(" + "위치 권한이 없어 Wifi 통신" + ")").matcher(showResult).find()) {
          txt.setText("어플리케이션 위치 권한을 수락해야 정확한 검사가 가능합니다.");
        } else if(Pattern.compile("(" + "비밀번호가 설정되어 있지 않습니다" + ")").matcher(showResult).find()) {
          txt.setText("비밀번호가 없는 WiFi 입니다. 이 경우 네트워크 공격에 취약합니다. 즉시 비밀번호를 설정하는 것이 좋습니다.");
        } else if(Pattern.compile("(" + "비밀번호 중 하나랑 일치합니다" + ")").matcher(showResult).find()) {
          txt.setText("자주사용하는 1만개의 비밀번호 중 하나랑 일치합니다. 즉시 비밀번호 변경이 필요합니다.");
        } else if(Pattern.compile("(" + "비밀번호가 너무 짧습니다" + ")").matcher(showResult).find()) {
          txt.setText("너무 짧은 비밀번호는 해킹에 취약합니다. 즉시 변경이 필요합니다.");
        } else if(Pattern.compile("(" + "비밀번호를 특수문자, 숫자, 영문자의 조합으로 변경해주세요" + ")").matcher(showResult).find()) {
          txt.setText("비밀번호가 너무 단순합니다. 특수문자, 숫자, 영문자의 조합으로 변경해주세요");
        } else if(Pattern.compile("(" + "보안수준이 낮습니다" + ")").matcher(showResult).find()) {
          txt.setText("WiFi 암호형식인 WPA는 보안수준이 취약합니다. 변경이 필요합니다.");
        } else if(Pattern.compile("(" + "보안수준이 매우 낮습니다" + ")").matcher(showResult).find()) {
          txt.setText("WiFi 암호형식인 WPA는 보안수준이 취약합니다. 즉시 변경이 필요합니다.");
        } else if(Pattern.compile("(" + "포트 ssl 보안 접속 확인안됨" + ")").matcher(showResult).find()) {
          txt.setText("ssl 보안 통신이 확인 안됩니다. 통신이 암호화 되어있지 않으면 중간에 통신 내용이 도청당할 수 있습니다.");
        }

        final AlertDialog.Builder dialog = new AlertDialog.Builder(ResultActivity.this);
        dialog.setView(v1);
        dialog.show();

        return false;
      }
    });


  }

  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      if (getIntent().getStringExtra("activity") != null && !getIntent().getStringExtra("activity").equals("log")) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(ResultActivity.this);
        builder.setTitle("알림");
        builder.setMessage("메인 페이지로 돌아가시겠습니까?");

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            Intent go = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(go);
            dialog.cancel();
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
        return true;
      }
    }

    return super.onKeyDown(keyCode, event);
  }
}