package com.robertocursoandroid.whatsapp.activity.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.robertocursoandroid.whatsapp.R;
import com.robertocursoandroid.whatsapp.activity.adapter.MensagensAdapter;

import com.robertocursoandroid.whatsapp.activity.api.NotificacaoService;
import com.robertocursoandroid.whatsapp.activity.config.ConfiguracaoFirebase;
import com.robertocursoandroid.whatsapp.activity.helper.Base64Custom;
import com.robertocursoandroid.whatsapp.activity.helper.UsuarioFirebase;
import com.robertocursoandroid.whatsapp.activity.model.Conversa;
import com.robertocursoandroid.whatsapp.activity.model.Grupo;
import com.robertocursoandroid.whatsapp.activity.model.Mensagem;

import com.robertocursoandroid.whatsapp.activity.model.Notificacao;
import com.robertocursoandroid.whatsapp.activity.model.NotificacaoDados;
import com.robertocursoandroid.whatsapp.activity.model.Usuario;
import com.robertocursoandroid.whatsapp.activity.service.OuvinteMudancaRede;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ChatActivity extends AppCompatActivity {
    private TextView textViewNome;
    private CircleImageView circleImageViewFoto;
    private Usuario usuarioDestinatario;
    private Usuario usuarioRemetente;
    private EditText editMensagem;
    private List<Mensagem> mensagens = new ArrayList<>();
    // camera do chat e galeria
    private ImageView imageGaleria;
    private static final int SELECAO_GALERIA = 200;
    private ImageView imageCamera;
    private static final int SELECAO_CAMERA = 100;

    private Grupo grupo;

    // identificador usuarios remetentes e destinatarios
    private String idUsuarioRemetente; // usuario logado
    private String idUsuarioDestinatario; // usuario que iremos mandar a mensagem

    private RecyclerView recyclerMensagens;

    private MensagensAdapter adapter;

    private DatabaseReference database;
    private DatabaseReference mensagensRef;
    private StorageReference storage;


    private ChildEventListener childEventListenerMensagens;

    private Retrofit retrofit;
    private String baseUrl;
    private String token;
    private String listaTokensMembrosGrupo;

    private DatabaseReference usuarioRef;

    private OuvinteMudancaRede mudancaRede = new OuvinteMudancaRede();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        // configurar toolbar do chat
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // configurações inicias para recuperar usuario
        textViewNome = findViewById(R.id.textViewNomeChat);
        circleImageViewFoto = findViewById(R.id.circleImageFotoChat);
        editMensagem = findViewById(R.id.editMensagem);
//  setContentView(editMensagem); // teste
        recyclerMensagens = findViewById(R.id.recyclerMensagens);
        imageCamera       = findViewById(R.id.imageCamera); // recupera foto tirada na hora para enviar no chat
        imageGaleria      = findViewById(R.id.imageGaleria);// recupera foto da galeria para enviar no chat


        // recuperar dados do Idusuario remetente e usuario remetente
        idUsuarioRemetente = UsuarioFirebase.getIdentificadorUsuario();
        usuarioRemetente = UsuarioFirebase.getDadosUsuarioLogado();


        //Recuperar dados do usuário destinatario: "membros do grupo"
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            if(bundle.containsKey("chatGrupo")){
                grupo = (Grupo) bundle.getSerializable("chatGrupo");
                idUsuarioDestinatario = grupo.getId();
                // recuperar token do usuario destinatario
                recuperarTokenDestinatario();

                textViewNome.setText(grupo.getNome()); // exibe nome do grupo em cima do chat
                // exibe foto do grupo em cima do chat
                String foto = grupo.getFoto();

                if(foto != ""){
                    Picasso.get()
                            .load(foto)
                            .into(circleImageViewFoto);
                }else{
                    circleImageViewFoto.setImageResource(R.drawable.padrao);
                }

            }else if(bundle.containsKey("chatContato")){
                ///**** inicio conversa normal
                usuarioDestinatario = (Usuario) bundle.getSerializable("chatContato");
                textViewNome.setText(usuarioDestinatario.getNome()); // exibe nome do destinatario em cima do chat

                String foto = usuarioDestinatario.getFoto();
                if(foto != ""){
                    Picasso.get()
                            .load(foto)
                            .into(circleImageViewFoto);
                }else{
                    circleImageViewFoto.setImageResource(R.drawable.padrao);
                }
                //recuperar dados usuario destinatario
                idUsuarioDestinatario = Base64Custom.codificarBase64( usuarioDestinatario.getEmail() );
                recuperarTokenDestinatario();
                ///**** fim conversa normal
            }

        }

        // configuração adapter
        adapter = new MensagensAdapter(mensagens, getApplicationContext());

        // configuração RecyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerMensagens.setLayoutManager(layoutManager);
        recyclerMensagens.setHasFixedSize(true);
        recyclerMensagens.setAdapter(adapter);
        // configuração firebase
        database = ConfiguracaoFirebase.getFirebaseDatabase();
        storage = ConfiguracaoFirebase.getFirebaseStorage();

        mensagensRef = database.child("mensagens")
                .child(idUsuarioRemetente)
                .child(idUsuarioDestinatario);

        // evento de click na camera do chat
        imageCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if(intent.resolveActivity(getPackageManager())!= null){
                    startActivityForResult(intent, SELECAO_CAMERA);
                }
            }
        });

        // evento de click na galeria do chat

        imageGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if(intent.resolveActivity(getPackageManager())!= null){
                    startActivityForResult(intent, SELECAO_GALERIA);
                }
            }
        });





        //Configuração da retrofit para enviar requisição ao firebase e então para ele enviar a notificação
        baseUrl = "https://fcm.googleapis.com/fcm/";
        retrofit = new Retrofit.Builder()
                .baseUrl( baseUrl )
                .addConverterFactory(GsonConverterFactory.create())
                .build();




    }


    public void recuperarTokenDestinatario(){
        Bundle bundleToken = getIntent().getExtras();
        if(bundleToken  != null){
            if(bundleToken.containsKey("chatGrupo")){

                grupo = (Grupo) bundleToken.getSerializable("chatGrupo");
                for(Usuario membro: grupo.getMembros()){ // percorre a lista de membro para notificar
                    String tokenMembroGrupo = membro.getToken();
                    listaTokensMembrosGrupo = tokenMembroGrupo;

                }

            }else if(bundleToken.containsKey("chatContato")){
               // usuarioDestinatario = (Usuario) bundleToken.getSerializable("chatContato");
              //  token = usuarioDestinatario.getToken(); //teste recuperar token usuario padrao


                usuarioDestinatario = (Usuario) bundleToken.getSerializable("chatContato");
                // token = usuarioDestinatario.getTokenUsuario();
                // recuperar token do NO usuarios
                usuarioRef =  ConfiguracaoFirebase.getFirebaseDatabase()
                        .child("usuarios")
                        .child(idUsuarioDestinatario)
                        .child("token");
                usuarioRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String tokenUsuario =  snapshot.getValue().toString();
                        token = tokenUsuario;

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        }

    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        recuperarTokenDestinatario();

        if(resultCode == RESULT_OK){
            Bitmap imagem = null;
            try {
                switch (requestCode){
                    case SELECAO_CAMERA:
                        imagem =(Bitmap) data.getExtras().get("data");
                        break;

                    case SELECAO_GALERIA:
                        Uri localImagemSelecionada = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada);

                        break;
                }

                if(imagem != null){

                    //  imageGaleria.setImageBitmap(imagem);


                    // recuperar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    // cria nome que não se repete
                    String nomeImagem = UUID.randomUUID().toString();

                    // configurar referencia do firebase
                    final StorageReference imageRef = storage.child("imagem")
                            .child("fotos")
                            .child(idUsuarioRemetente)
                            .child(nomeImagem);

                    UploadTask uploadTask = imageRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Erro", "Erro ao fazer upload");

                            Toast.makeText(ChatActivity.this,
                                    "Erro ao fazer upload da imagem!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            // String dowloadUrl = taskSnapshot.getDownloadUrl().toString();

                            //teste para nova versão
                            imageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    String dowloadUrl =  task.getResult().toString();

                                    Bundle bundleFoto = getIntent().getExtras();
                                    if(bundleFoto  != null){
                                        if(bundleFoto.containsKey("chatGrupo")){ // mensagem no grupo
                                            grupo = (Grupo) bundleFoto.getSerializable("chatGrupo");

                                            for(Usuario membro: grupo.getMembros()){
                                                String idRemetenteGrupo = Base64Custom.codificarBase64(membro.getEmail());
                                                String idUsuarioLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();

                                                Mensagem mensagem = new Mensagem();
                                                mensagem.setIdUsuario(idUsuarioLogadoGrupo);
                                                mensagem.setMensagem("imagem.jpeg");
                                                mensagem.setNome(usuarioRemetente.getNome()); // nome do usuario que mandou mensagem no grupo
                                                mensagem.setImagem(dowloadUrl);
                                                // salvar mensagem para os membros do grupo
                                                // grupo
                                                salvarMensagem(idRemetenteGrupo, idUsuarioDestinatario, mensagem);

                                                // salvar conversas do grupo                             // membros do grpo
                                                salvarConversa( idRemetenteGrupo,idUsuarioDestinatario, usuarioDestinatario, mensagem, true, true);
                                            }
                                            enviarNotificacao();

                                        }else if(bundleFoto.containsKey("chatContato")){ // mensagem normal entre duas pessaos
                                            usuarioDestinatario = (Usuario) bundleFoto.getSerializable("chatContato");
                                            Mensagem mensagem = new Mensagem();
                                            mensagem.setIdUsuario(idUsuarioRemetente);
                                            mensagem.setMensagem("imagem.jpeg");
                                            mensagem.setImagem(dowloadUrl);
                                            // salvar mensagem para o rementente
                                            salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario,mensagem);
                                            // salvar mensagem para o destinatario
                                            salvarMensagem(idUsuarioDestinatario,idUsuarioRemetente,mensagem);
                                            enviarNotificacao();
                                        }
                                        Toast.makeText(ChatActivity.this,
                                                "Sucesso ao enviar imagem!",
                                                Toast.LENGTH_SHORT).show();
                                    }





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

    public  void enviarMensagem(View view){

        Bundle bundleEnviarMensagem = getIntent().getExtras();
        if(bundleEnviarMensagem != null){

            String  textoMensagem = editMensagem.getText().toString();
            if(!textoMensagem.isEmpty()){
                if(bundleEnviarMensagem.containsKey("chatGrupo")){
                    grupo = (Grupo) bundleEnviarMensagem.getSerializable("chatGrupo");
                    // trocando mensagens entre grupo
                    for(Usuario membro: grupo.getMembros()){
                        String idRemetenteGrupo = Base64Custom.codificarBase64(membro.getEmail());
                        String idUsuarioLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();

                        Mensagem mensagem = new Mensagem();
                        mensagem.setIdUsuario(idUsuarioLogadoGrupo);
                        mensagem.setMensagem(textoMensagem);
                        mensagem.setNome(usuarioRemetente.getNome()); // nome do usuario que mandou mensagem no grupo

                        // salvar mensagem para os membros do grupo
                        // grupo
                        salvarMensagem(idRemetenteGrupo, idUsuarioDestinatario, mensagem);

                        // salvar conversas do grupo                             // membros do grpo
                        salvarConversa( idRemetenteGrupo,idUsuarioDestinatario, usuarioDestinatario, mensagem, true, true);
                    }
                }else if(bundleEnviarMensagem.containsKey("chatContato")){
                    usuarioDestinatario = (Usuario) bundleEnviarMensagem.getSerializable("chatContato");
                    // trocando mensagens entre usuarios padrão
                    Mensagem mensagem = new Mensagem();
                    mensagem.setIdUsuario(idUsuarioRemetente);
                    mensagem.setMensagem(textoMensagem);






                    // salvar mensagem para o remetente
                    salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem); //
                    // salvar mensagem para o destinatario
                    salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);

                    // salvar conversas remetente                          // usuario exibição
                    salvarConversa(idUsuarioRemetente,idUsuarioDestinatario, usuarioDestinatario, mensagem, false, false);
                    // salvar conversas destinatario
                    // usuario exibição
                    salvarConversa(idUsuarioDestinatario, idUsuarioRemetente, usuarioRemetente, mensagem,false, true );



                }
            }else{
                Toast.makeText(ChatActivity.this,
                        "Digite uma mensagem para enviar!",
                        Toast.LENGTH_LONG).show();
            }


        }


        enviarNotificacao();

        Log.d("Teste", " Mensagem enviada");
    }


    public void enviarNotificacao(){
        //fazer uma condição pra ver se é grupo ou não

        Bundle bundleNotificacao = getIntent().getExtras();
        if(bundleNotificacao.containsKey("chatGrupo")){
            grupo = (Grupo) bundleNotificacao.getSerializable("chatGrupo");

            for(Usuario membro: grupo.getMembros()){  //teste
                String tokenMembroGrupo = membro.getToken();

                listaTokensMembrosGrupo = tokenMembroGrupo;

                String tokenDestinatarioGrupo = listaTokensMembrosGrupo;
                String to = "";// para quem vou enviar a menssagem
                to = tokenDestinatarioGrupo ;

                //Monta objeto notificação
                Notificacao notificacao = new Notificacao("Nova Mensagem", "" + usuarioRemetente.getNome());
                NotificacaoDados notificacaoDados = new NotificacaoDados(to, notificacao );

                NotificacaoService service = retrofit.create(NotificacaoService.class);
                Call<NotificacaoDados> call = service.salvarNotificacao( notificacaoDados );

                call.enqueue(new Callback<NotificacaoDados>() {
                    @Override
                    public void onResponse(Call<NotificacaoDados> call, Response<NotificacaoDados> response) {

                        if( response.isSuccessful() ){
                            //teste para verificar se enviou a notificação
                           /*  Toast.makeText(getApplicationContext(),
                                     "codigo: " + response.code(),
                                     Toast.LENGTH_LONG ).show();

                            */

                        }
                    }

                    @Override
                    public void onFailure(Call<NotificacaoDados> call, Throwable t) {

                    }
                });


                ///// fim  código para notificação grupo

            }




        }else if(bundleNotificacao.containsKey("chatContato")){
            usuarioDestinatario = (Usuario) bundleNotificacao.getSerializable("chatContato");

            //  recuperarTokenDestinatario();
            // String jurema = "er1eptpnRMKlsLJFYKv2cm:APA91bGq9g3feWSPIKRoiF8yDrcpyzDdK0Hn0bLI-uCDdXWDriRm8Jb-_UKcVpESVfv808o3H7qef-idxWD2dufgCokcTuONwQxM0zdWba-L00ojGJj3VTZRX-GsNkIVrU1fq7DIdYs7";
            //  String roberto = "cHpfmiiQS32YqN0DlNviFW:APA91bH95zMMLBuboc16H8sbJWzDf40rWuoLEO744qKGf9i7xv9RyNw8EX_-ZUr_jlOxWiTb3bmXM6DnMq_degLYr1gKpap7XIU_IC6XO3VJWtl1B8mG95MD6LhfBkyG-Xs3IMpfc1zU";

            token = usuarioDestinatario.getToken();//  recuperar Token Destinatario

            String tokenDestinatario = token;
            String to = "";// para quem vou enviar a menssagem
            to = tokenDestinatario ;



            //Monta objeto notificação
            Notificacao notificacao = new Notificacao("Nova Mensagem", ""+usuarioRemetente.getNome());
            NotificacaoDados notificacaoDados = new NotificacaoDados(to, notificacao );

            NotificacaoService service = retrofit.create(NotificacaoService.class);
            Call<NotificacaoDados> call = service.salvarNotificacao( notificacaoDados );



            call.enqueue(new Callback<NotificacaoDados>() {
                @Override
                public void onResponse(Call<NotificacaoDados> call, Response<NotificacaoDados> response) {

                    if( response.isSuccessful() ){

                        //teste para verificar se enviou a notificação
                           /*  Toast.makeText(getApplicationContext(),
                                     "codigo: " + response.code(),
                                     Toast.LENGTH_LONG ).show();

                            */

                    }
                }

                @Override
                public void onFailure(Call<NotificacaoDados> call, Throwable t) {

                }
            });


            ///// fim  código para notificação

        }


    }


    private void salvarConversa(String idRemetente, String idDestinatario, Usuario usuarioExibição, Mensagem msg, boolean isGroup, boolean novaMensagem){

        Conversa conversaRemetente = new Conversa();
        conversaRemetente.setIdRemetente(idRemetente);
        conversaRemetente.setIdDestinatario(idDestinatario);
        conversaRemetente.setUltimaMensagem(msg.getMensagem());

        // salvar  mensagens novas
        conversaRemetente.setNovaMensagem(String.valueOf(novaMensagem));

        if(isGroup){ // conversa de grupo
            conversaRemetente.setIsGrupo("true");
            conversaRemetente.setGrupo(grupo);

        }else{ // conversa normal
            conversaRemetente.setUsuarioExibicao(usuarioExibição);
            conversaRemetente.setIsGrupo("false");

        }

        conversaRemetente.salvar();


    }


    private void salvarMensagem(String idRemetente, String idDestinatario, Mensagem msg){
        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference mensagemRef = database.child("mensagens");

        mensagemRef.child(idRemetente)
                .child(idDestinatario)
                .push()
                .setValue(msg);

        // limpar mensagem
        editMensagem.setText("");

    }

    @Override
    protected void onStart() {
        super.onStart();

        // verificar acesso a internet
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mudancaRede, filter);

        recuperarMensagens();
        recuperarTokenDestinatario();

    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mudancaRede);
        mensagensRef.removeEventListener(childEventListenerMensagens);
    }

    // metodo para recuperar mensagens do firebase
    private void recuperarMensagens(){
        mensagens.clear();
        recuperarTokenDestinatario();

        childEventListenerMensagens = mensagensRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Mensagem mensagem = dataSnapshot.getValue(Mensagem.class);
                mensagens.add(mensagem);

                adapter.notifyDataSetChanged();

                // da o foco na ultima mensagem enviada
                recyclerMensagens.scrollToPosition(mensagens.size() -1 );

                Log.d("Teste"," Conversa salva");
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



}
