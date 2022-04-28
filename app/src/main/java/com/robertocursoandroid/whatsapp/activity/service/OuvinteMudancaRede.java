package com.robertocursoandroid.whatsapp.activity.service;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.widget.AppCompatButton;

import com.robertocursoandroid.whatsapp.R;

public class OuvinteMudancaRede extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {


        if (!VerificaAcesso.conectadoNaInternet(context)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View layout_dialogo = LayoutInflater.from(context).inflate(R.layout.dialogo_acesso_internet, null);
            builder.setView(layout_dialogo);


            AppCompatButton botaoOk = layout_dialogo.findViewById(R.id.botaoOk);

            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.setCancelable(false);

            dialog.getWindow().setGravity(Gravity.CENTER);


            botaoOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    onReceive(context, intent);

                }
            });
        }

    }
}
