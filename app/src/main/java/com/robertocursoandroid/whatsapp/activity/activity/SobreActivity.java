package com.robertocursoandroid.whatsapp.activity.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;

import com.robertocursoandroid.whatsapp.R;
import com.robertocursoandroid.whatsapp.activity.service.OuvinteMudancaRede;

public class SobreActivity extends AppCompatActivity {

    private OuvinteMudancaRede mudancaRede = new OuvinteMudancaRede();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sobre);

    }



    @Override
    protected void onStart() {
        super.onStart();
        // verificar acesso a internet
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mudancaRede, filter);

    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(mudancaRede);
    }
}