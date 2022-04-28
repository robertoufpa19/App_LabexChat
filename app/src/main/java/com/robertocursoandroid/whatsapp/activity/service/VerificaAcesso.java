package com.robertocursoandroid.whatsapp.activity.service;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

// verifica acesso os serviços de conexão
public class VerificaAcesso {


    public static boolean conectadoNaInternet(Context context) {


        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo[] infos = connectivityManager.getAllNetworkInfo();
            if (infos != null) {
                for (int i = 0; i < infos.length; i++) {
                    if (infos[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
