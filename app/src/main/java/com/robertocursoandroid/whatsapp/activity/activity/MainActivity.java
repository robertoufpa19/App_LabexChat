package com.robertocursoandroid.whatsapp.activity.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.robertocursoandroid.whatsapp.R;
import com.robertocursoandroid.whatsapp.activity.config.ConfiguracaoFirebase;
import com.robertocursoandroid.whatsapp.activity.fragment.ContatosFragment;
import com.robertocursoandroid.whatsapp.activity.fragment.ConversasFragment;
import com.robertocursoandroid.whatsapp.activity.helper.Permissao;
import com.robertocursoandroid.whatsapp.activity.service.OuvinteMudancaRede;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private MaterialSearchView searchView;

    private OuvinteMudancaRede mudancaRede = new OuvinteMudancaRede();

    private String[] permissoesNecessarias = new String[]{

            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        //   searchView = findViewById(R.id.materialSearchPrincipal);


        // configura titutlo para a toolbar
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("LabexChat");
        setSupportActionBar( toolbar );

        // validar permissões
        Permissao.validarPermissoes(permissoesNecessarias, this, 1);

        //Configurar abas
        final FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(),
                FragmentPagerItems.with(this)
                        .add("Conversas", ConversasFragment.class)
                        .add("Contatos", ContatosFragment.class)
                        .create()
        );

        final ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);

        SmartTabLayout viewPagerTab = findViewById(R.id.viewPagerTab);
        viewPagerTab.setViewPager(viewPager);


        //Configuração do search view
        searchView = findViewById(R.id.materialSearchPrincipal);

        //Listener para o search view
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {

                ConversasFragment fragment = (ConversasFragment) adapter.getPage(0);
                fragment.recarregarConversas();

            }
        });


        //Listener para caixa de texto
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Log.d("evento", "onQueryTextSubmit");
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Log.d("evento", "onQueryTextChange");

                // verifica se esta pesquisando conversas ou contatos
                switch (viewPager.getCurrentItem()){
                    case 0:
                        ConversasFragment conversasFragment = (ConversasFragment) adapter.getPage(0);
                        if( newText != null && !newText.isEmpty() ){
                            conversasFragment.pesquisarConversas( newText.toLowerCase() );
                        }else{
                            conversasFragment.recarregarConversas();
                        }
                        break;

                    case 1:
                        ContatosFragment contatosFragment = (ContatosFragment) adapter.getPage(1);
                        if( newText != null && !newText.isEmpty() ){
                            contatosFragment.pesquisarContatos( newText.toLowerCase() );
                        }else{
                            contatosFragment.recarregarContatos();
                        }

                        break;
                }




                return true;
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);

        // configura menu de pesquisa
        MenuItem item = menu.findItem(R.id.menuPesquisa);
        searchView.setMenuItem(item);


        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch ( item.getItemId() ){
            case R.id.menuSair :
                deslogarUsuario();
                finish();
                break;
            case R.id.menuConfiguracoes :
                abrirConfiguracoes();
                break;

            case R.id.menuSobre:
                abrirSobre();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void deslogarUsuario(){

        try {
            autenticacao.signOut();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void abrirConfiguracoes(){
        Intent intent = new Intent(MainActivity.this, ConfiguracoesActivity.class);
        startActivity( intent );
    }
    public void abrirSobre(){
        Intent intent = new Intent(MainActivity.this, SobreActivity.class);
        startActivity( intent );
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
