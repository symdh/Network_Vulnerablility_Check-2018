package com.test.networkvulnerablilitycheck.Util;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PwdCheck {

  public PwdCheck() {
  }

  public String Pwd10000(Context context, String Pwd) {
    AssetManager am = context.getResources().getAssets();
    InputStream is = null;

    //특수문자 처리
    String[] metaCharacters = {"\\", "^", "{", "}", "[", "]", "(", ")"};
    String[] metaCharacters2 = {"$", "*", "+", "|"};

    for (int i = 0; i < metaCharacters.length; i++) {
      if (Pwd.contains(metaCharacters[i])) {
        Pwd = Pwd.replace(metaCharacters[i], "\\" + metaCharacters[i]);
      }
    }
    for (int i = 0; i < metaCharacters2.length; i++) {
      if (Pwd.contains(metaCharacters2[i])) {
        Pwd = Pwd.replace(metaCharacters2[i], "[" + metaCharacters2[i] + "]");
      }
    }
    Log.i("테스t", "특수문자 escape 처리결과 : " + Pwd);

    try {
      is = am.open("pwd10000.txt");
      byte[] c = new byte[1024];
      int readChars = 0;

      String text = "";
      while ((readChars = is.read(c)) != -1) {
        text = new String(c);
        Pattern pwd = Pattern.compile("(" + Pwd + "\\r\\n" + ")");
        Matcher match = pwd.matcher(text);
        if (match.find()) {
          Log.i("테스t", "일치합니다.");
          return "경고. 자주 사용하는 1만개의 비밀번호 중 하나랑 일치합니다. 변경이 즉시 필요합니다.";
        }
      }
      is.close();

    } catch (Exception e) {

    }
    return "";
  }

  public String verify(String Pwd) {
    String result = "";
    int Pwdlength = Pwd.length();
    if (Pwdlength < 7) {
      result = "경고. 비밀번호가 너무 짧습니다. 9자리 이상으로 변경이 즉시 필요합니다.";
    } else if (Pwdlength < 8) {
      result = "확인필요. 비밀번호는 9자리 이상으로 권장합니다.";
    } else if (Pwdlength < 9) {
      result = "안전. 현재 비밀번호는 적절한 길이입니다.";
    }

    //영소문자 갯수확인
    Pwd = Pwd.replaceAll("[a-z]", "");
    int az = Pwdlength - Pwd.length();
    Log.i("테스t", Integer.toString(az));

    //영대문자 갯수확인
    Pwd = Pwd.replaceAll("[A-Z]", "");
    int AZ = Pwdlength - Pwd.length() - az;
    Log.i("테스t", Integer.toString(AZ));

    //숫자 갯수확인
    Pwd = Pwd.replaceAll("[0-9]", "");
    int numlen = Pwdlength - Pwd.length() - az - AZ;
    Log.i("테스t", Integer.toString(numlen));

    //나머지 특수문자
    int charspeial = Pwd.length();
    Log.i("테스t", Integer.toString(charspeial));

    int a = 0;
    if (az != 0) {
      a++;
    }
    if (AZ != 0) {
      a++;
    }
    if (numlen != 0) {
      a++;
    }
    if (charspeial != 0) {
      a++;
    }
    switch (a) {
      case 1:
        result = result + "\r\n" + "경고. 비밀번호를 특수문자, 숫자, 영문자의 조합으로 변경해주세요";
        break;
      case 2:
        result = result + "\r\n" + "확인필요. 비밀번호는 특수문자, 숫자, 영문자의 조합을 권장합니다";
        break;
      case 3:
        result = result + "\r\n" + "안전. 현재 비밀번호는 강력한 조합입니다.";
        break;
      case 4:
        result = result + "\r\n" + "안전. 현재 비밀번호는 아주 강력한 조합입니다.";
        break;
      default:
        result = result + "\r\n" + "확인필요. 기기 비밀번호 체크 필요합니다."; //빈값이 넘어왔을 경우
        break;
    }

    return result;
  }
}
