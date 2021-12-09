package com.test.networkvulnerablilitycheck;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.test.networkvulnerablilitycheck.Adapter.MainListAdapter;

import java.util.ArrayList;

import static com.test.networkvulnerablilitycheck.MainActivity.logHistory;


public class LogActivity extends Activity {

  public void onCreate(Bundle savedInstanceSate) {
    super.onCreate(savedInstanceSate);
    setContentView(R.layout.log);

    final String[] asLog = logHistory.loadDir(-1);

    String[] date = new String[asLog.length / 2];
    String[] router = new String[asLog.length / 2];
    String[] amount = new String[asLog.length / 2];
    ListView list;

    for (int i = 1, j = 0; i < asLog.length; i += 2) {
      String[] strarray = asLog[i].split("//%%//");
      if (strarray.length == 2) {
        asLog[i] = strarray[0] + " " + strarray[1] + " 중지된 검사기록";
      } else {
        asLog[i] = strarray[0] + " " + strarray[1] + " " + strarray[2] + "개";
      }

      date[j] = strarray[0];
      router[j] = strarray[1];
      if (strarray.length == 2) {
        amount[j] = "중단된 검사기록";
      } else {
        amount[j] = strarray[2] + "개";
      }
      j++;
    }

    MainListAdapter listAdapter = new
            MainListAdapter(LogActivity.this, date, router, amount);
    list = (ListView) findViewById(R.id.loglist);
    list.setAdapter(listAdapter);

    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(LogActivity.this, ResultActivity.class);
        if (position < asLog.length - 1) {
          intent.putExtra("Dirname", asLog[position * 2]);
          intent.putExtra("activity", "log");
          startActivity(intent);
        }
      }
    });
  }
}
