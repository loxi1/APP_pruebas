package com.ubx.rfid_demo.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
public class GlobalClass {
    Context mContext;
    /* NETWORK MESSAGE */
    public static final String notNetworkMessage = "Debe conectarse a internet";
    public static final String networkMessage = "Conectado a internet";
    public static  final String version_sistema = "1.2";
    /* CONFIRM MESSAGE */
    public static final String confirmMessage = "Agregado correctamente";
    public static final String MyPREFERENCES = "VGlobal";
    // constructor
    public GlobalClass(Context context) {
        this.mContext = context;
    }

    public String getApiAuth(){
        return "Basic Q29mYXF1aW5vNjU0MTIzIQ==";
    }
    public String getUrlApi(){
        return "https://cofaco.com/despacho/";
    }
    public String getContentType(){
        return "application/json";
    }
    public String getTokenAuthApi(){
        return "Q29mYXF1aW5vNjU0MTIzIQ==";
    }

    public String getVersion_sistema() {
        return  version_sistema;
    }
    //Conexion a internet
    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            return false;
        } else
            return true;
    }
}
