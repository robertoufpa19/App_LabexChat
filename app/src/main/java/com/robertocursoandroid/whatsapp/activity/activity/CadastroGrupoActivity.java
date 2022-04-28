package com.robertocursoandroid.whatsapp.activity.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.robertocursoandroid.whatsapp.R;
import com.robertocursoandroid.whatsapp.activity.adapter.GrupoSelecionadoAdapter;
import com.robertocursoandroid.whatsapp.activity.config.ConfiguracaoFirebase;
import com.robertocursoandroid.whatsapp.activity.helper.UsuarioFirebase;
import com.robertocursoandroid.whatsapp.activity.model.Grupo;
import com.robertocursoandroid.whatsapp.activity.model.Usuario;
import com.robertocursoandroid.whatsapp.activity.service.OuvinteMudancaRede;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CadastroGrupoActivity extends AppCompatActivity {

    private List<Usuario> listaMembrosSelecionados = new ArrayList<>();
    private TextView textToltalParticipante;
    private GrupoSelecionadoAdapter grupoSelecionadoAdapter;
    private RecyclerView recyclerMembrosSelecionados;
    private CircleImageView  imageGrupo;
    private static final int SELECAO_GALERIA = 200;
    private StorageReference storageReference;
    private Grupo grupo;
    private FloatingActionButton fabSalvarGrupo;
    private EditText editNomeGrupo;

    private Usuario usuario; //teste

    private OuvinteMudancaRede mudancaRede = new OuvinteMudancaRede();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_grupo);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Novo Grupo");
        toolbar.setSubtitle("Defina o Nome");
        setSupportActionBar(toolbar);

        //configuracoes iniciais
       textToltalParticipante = findViewById(R.id.textTotalParticipantes);
       recyclerMembrosSelecionados = findViewById(R.id.recyclerMembrosGrupo);
       imageGrupo = findViewById(R.id.imageGrupo);
       fabSalvarGrupo = findViewById(R.id.fabSalvarGrupo);
       editNomeGrupo = findViewById(R.id.editNomeGrupo);
       grupo = new Grupo(); // instancia o id do grupo

       storageReference = ConfiguracaoFirebase.getFirebaseStorage(); // configurar imagem no firebase

       // configurar evento de click  (imagem de grupo)
        imageGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                if(intent.resolveActivity(getPackageManager())!= null){
                    startActivityForResult(intent, SELECAO_GALERIA);
                }
            }
        });




        // recuperar lista de membros passada
         if(getIntent().getExtras() != null){
             List<Usuario> membros = (List<Usuario>) getIntent().getExtras().getSerializable("membros");
             listaMembrosSelecionados.addAll(membros);
             textToltalParticipante.setText("Participantes: "+ listaMembrosSelecionados.size());


         }

         // configurar recyclerview
        //Configurar recyclerview para os membros selecionados
        grupoSelecionadoAdapter = new GrupoSelecionadoAdapter(listaMembrosSelecionados, getApplicationContext());

        RecyclerView.LayoutManager layoutManagerHorizontal = new LinearLayoutManager(
                getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        );
        recyclerMembrosSelecionados.setLayoutManager(layoutManagerHorizontal);
        recyclerMembrosSelecionados.setHasFixedSize(true);
        recyclerMembrosSelecionados.setAdapter( grupoSelecionadoAdapter );

        // configurar floating action button
        fabSalvarGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
          String nomeGrupo = editNomeGrupo.getText().toString();

          // adiciona a lista de membros do grupo o usuario logado
          listaMembrosSelecionados.add(UsuarioFirebase.getDadosUsuarioLogado());

          grupo.setMembros(listaMembrosSelecionados);
          grupo.setNome(nomeGrupo);
          grupo.setToken(""); // lista de tokens
          grupo.salvar();


                Intent i = new Intent(CadastroGrupoActivity.this, ChatActivity.class);
                i.putExtra("chatGrupo",grupo);
                startActivity(i);


            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            Bitmap imagem = null;


            try {
                Uri localImagemSelecionada = data.getData();
                imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada);

                if(imagem != null){
                   imageGrupo.setImageBitmap(imagem);
                    // recuperar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    // salvar imagem no Firebase storage
                    final StorageReference imagemRef = storageReference
                            .child("imagem")
                            .child("grupos")
                            .child(grupo.getId() + ".jpeg");

                    // recuperar dados da imagem
                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);

                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(CadastroGrupoActivity.this,
                                    "Erro ao fazer upload da imagem!",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Toast.makeText(CadastroGrupoActivity.this,
                                    "Sucesso ao fazer upload da imagem!",
                                    Toast.LENGTH_SHORT).show();
                            //teste para antiga versão
                         /*  String url =  taskSnapshot.getDownloadUrl().toString();
                            grupo.setFoto(url);

                          */

                            //teste para nova versão
                            imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                String url =  task.getResult().toString();
                                   grupo.setFoto(url);

                                }
                            });


                        }
                    });




                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
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
