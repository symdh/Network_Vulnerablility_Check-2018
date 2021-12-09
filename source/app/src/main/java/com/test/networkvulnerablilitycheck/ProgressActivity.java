package com.test.networkvulnerablilitycheck;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.test.networkvulnerablilitycheck.Util.SearchPort;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static com.test.networkvulnerablilitycheck.MainActivity.logHistory;

public class ProgressActivity extends AppCompatActivity {

  Handler handler = new Handler();
  int value = 0; // progressBar 값
  int add = 1; // 증가량, 방향

  public static TextView progressText;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.progress_activity);

    progressText = (TextView) findViewById(R.id.progress);

    ImageView glass = (ImageView) findViewById(R.id.gif);
    GlideDrawableImageViewTarget gifImage = new GlideDrawableImageViewTarget(glass);
    Glide.with(this).load(R.drawable.hourglass).into(gifImage);

    final Intent intent = new Intent(this, ResultActivity.class);
    final ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);

    Intent getintent = getIntent();
    String[] asIP = getintent.getStringArrayExtra("ip");

    final SearchPort asyncportscan = new SearchPort(this, asIP, 1024);

    Thread t = new Thread(new Runnable() {

      boolean go = true;

      @Override
      public void run() { // Thread 로 작업할 내용을 구현
        asyncportscan.execute();

        while (go) {
          handler.post(new Runnable() {
            @Override
            public void run() { // 화면에 변경하는 작업을 구현
              pb.setProgress(SearchPort.progress);
              if (SearchPort.progress == 100) {
                go = false;
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                Thread.currentThread().interrupt();
              }
            }
          });

          try {
            Thread.sleep(100); // 시간지연
          } catch (InterruptedException e) {
          }
        } // end of while
      }
    });

    t.start(); // 쓰레드 시작
  }// end of oncreate
}


