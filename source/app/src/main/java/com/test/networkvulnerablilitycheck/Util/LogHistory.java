

package com.test.networkvulnerablilitycheck.Util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogHistory {

  String sPath; //저장위치
  static public String sDName; //로그 저장할 디렉토리 이름
  String sDPath; //sDirPath + sDirName

  public LogHistory(String sDirPath) { //생성자 실행시 폴더생성
    sPath = sDirPath; //앱 저장주소 초기화
  }

  public void mklog(String wifiname) { //로그 폴더를 생성
    Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
    sDName = "userLog_" + timeStamp.getTime() / 1000;
    sDPath = sPath + "/" + sDName;

    File file = new File(sDPath);
    file.mkdirs();
    saveLog("Info", wifiname);
  }


  public void saveLog(String sFileName, String sContents) {
    try {
      File file = new File(sDPath + "/" + sFileName);
      BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true));
      bufferedWriter.newLine();
      bufferedWriter.write(sContents);
      bufferedWriter.close();

    } catch (Exception e) { //에러 떳을때 로그삭제
      deleteDir();
    }

    return;
  }

  public void addInfo(String num) { //마지막에 취약점 갯수 추가

    if (sDName == null) {
      //새로 추가된 로그일때만 진행
      return;
    }

    try {
      File file = new File(sDPath + "/" + "Info");
      BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true));
      bufferedWriter.write("//%%//" + num);
      bufferedWriter.close();
      sDName = null;
    } catch (Exception e) { //에러 떳을때 로그삭제
      deleteDir();
    }
  }


  public String[][] loadLog(String sLoadDirName) {

    File fPath = new File(sPath + "/" + sLoadDirName);
    File[] fileList = fPath.listFiles();
    ArrayList<String> sArrayList = new ArrayList<>();
    for (File tempFile : fileList) {
      if (tempFile.isFile()) {
        sArrayList.add(tempFile.getName());
      }
    }


    String[] sFileName = new String[sArrayList.size()];
    for (int i = 0; i < sFileName.length; i++) {
      sFileName[i] = sArrayList.get(i);
    }

    String[][] result = new String[sArrayList.size()][];

    for (int i = 0; i < sFileName.length; i++) { //모두 파일 다 불러옴
      try {
        FileInputStream fileInputStream = new FileInputStream(sPath + "/" + sLoadDirName + "/" + sFileName[i]);
        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(fileInputStream));
        String temp = "";
        sArrayList = new ArrayList<>();
        while ((temp = bufferReader.readLine()) != null) {
          sArrayList.add(temp);
        }

        result[i] = new String[sArrayList.size() + 1];
        result[i][0] = sFileName[i];

        for (int j = 0; j < result[i].length; j++) {
          result[i][j + 1] = sArrayList.get(j);
        }

      } catch (Exception e) {
      }
    }

    return result;

  }

  public String[] loadDir(int limitload) { //limitload -1이면 전체 불러오기
    //불러올 정보의 파일이름과 몇번째까지 불러올 것인지

    String sDirList = "";
    File fDir = new File(sPath);
    File[] listDir = fDir.listFiles();
    for (File tempFile : listDir) {
      if (tempFile.isDirectory()) {
        sDirList += tempFile.getName();
      }
    }

    ArrayList<Integer> mArrayList = new ArrayList<Integer>();
    Pattern pattern_1 = Pattern.compile("userLog_\\d{10,10}");
    Matcher match_1 = pattern_1.matcher(sDirList);

    while (match_1.find()) {
      String match_1_result = match_1.group();
      Pattern pattern_2 = Pattern.compile("\\d{10,10}");
      Matcher match_2 = pattern_2.matcher(match_1_result);

      while (match_2.find()) {
        mArrayList.add(Integer.parseInt(match_2.group()));
      }
    }

    Collections.reverse(mArrayList); //내림차순 정렬

    String[] saDirList;
    if (limitload != -1) { //제한된 리스트만 불러옴
      if (limitload < mArrayList.size()) {
        saDirList = new String[limitload * 2];
      } else {
        saDirList = new String[mArrayList.size() * 2];
      }
    } else {
      saDirList = new String[mArrayList.size() * 2];
    }

    for (int i = 0, j = 0; i < saDirList.length; ++i, ++j) {
      Log.i("TTTT", Integer.toString(i));
      saDirList[i] = "userLog_" + Integer.toString(mArrayList.get(j).intValue());

      try {
        FileInputStream fileInputStream = new FileInputStream(sPath + "/" + saDirList[i] + "/" + "Info");
        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(fileInputStream));
        ++i;
        bufferReader.readLine();
        saDirList[i] = bufferReader.readLine();
      } catch (Exception e) {
      }
    }

    return saDirList;
  }


  public void deleteDir() { //폴더 및 파일 삭제
    File file = new File(sDPath); //폴더내 파일을 배열로 가져옴
    File[] fileList = file.listFiles();

    for (int i = 0; i < fileList.length; i++) {
      File file2 = fileList[i];
      if (file2.isFile()) {
        file2.delete();
      }
    }

    file.delete();
  }

}
