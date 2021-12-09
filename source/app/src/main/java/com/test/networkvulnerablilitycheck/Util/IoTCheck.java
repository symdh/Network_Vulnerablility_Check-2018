package com.test.networkvulnerablilitycheck.Util;


import android.util.Log;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class IoTCheck {
    public IoTCheck() {

    }

    static public boolean checkEncrypt(String sIp, int port) {
        try {
            SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket sslSocket = (SSLSocket) sslsocketfactory.createSocket();
            sslSocket.connect(new InetSocketAddress(sIp, port), 2500);
            sslSocket.setSoTimeout(2000); //InputStream 무한루프 레어땜에

            InputStream in = sslSocket.getInputStream();
            OutputStream out = sslSocket.getOutputStream();

            out.write(1);
            int isloop = 0;

            while (in.available() > 0) {
                isloop++;
                if(isloop == 2500) {
                    break;
                }
            }

            Log.i("테스트", Integer.toString(port) + "번 포트 ssl 됨");
            return true;
        } catch (Exception exception) {
            Log.i("테스트", Integer.toString(port) + "번 포트 ssl 안됨");
            return false;
        }
    }
}
