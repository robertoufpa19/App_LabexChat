package com.robertocursoandroid.whatsapp.activity.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.robertocursoandroid.whatsapp.R;
import com.robertocursoandroid.whatsapp.activity.config.ConfiguracaoFirebase;
import com.robertocursoandroid.whatsapp.activity.helper.Permissao;
import com.robertocursoandroid.whatsapp.activity.helper.UsuarioFirebase;
import com.robertocursoandroid.whatsapp.activity.model.Usuario;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConfiguracoesActivity extends AppCompatActivity {

     private String[] permissoesNecessarias = new String[]{
             Manifest.permission.READ_EXTERNAL_STORAGE, // permite acesso a galeria do dispositivo
             Manifest.permission.CAMERA  // permite acesso a camera do dispositivo
     };

       private Button buttonCamera, buttonGaleria;
       private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;

    private CircleImageView circleImageViewFotoPerfil;

    private StorageReference storageReference;
    private String identificadorUsuario;
    private ImageView imageAtualizarNome;

    private EditText editNomePerfil;

    private Usuario usuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes);

        // configura titutlo para a toolbar
     /*   Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Configurações");
        setSupportActionBar(toolbar); // metodo para funcionar a toolbar em versoes anteriores do android

      */
        // configurações iniciais
         storageReference = ConfiguracaoFirebase.getFirebaseStorage(); // configurar imagem no firebase
         identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario(); // metodo criado para identificar o usuario
         usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

         // validar permissoes
        Permissao.validarPermissoes(permissoesNecessarias, this, 1);

         buttonCamera = findViewById(R.id.buttonCamera);
         buttonGaleria = findViewById(R.id.buttonGaleria);
        circleImageViewFotoPerfil = findViewById(R.id.circleImageViewFotoPerfil);
        editNomePerfil = findViewById(R.id.editNomePerfil);
        imageAtualizarNome = findViewById(R.id.imageAtualizarNome);


        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Configurações");
        setSupportActionBar( toolbar );

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

       // Recuperar dados do usuario
        FirebaseUser usuario = UsuarioFirebase.getUsuarioAtual();
           Uri url = usuario.getPhotoUrl();
            if(url != null){
                Glide.with(ConfiguracoesActivity.this)
                        .load(url)
                        .into(circleImageViewFotoPerfil);

            }else{
                circleImageViewFotoPerfil.setImageResource(R.drawable.padrao);
            }

            editNomePerfil.setText(usuario.getDisplayName());


         buttonCamera.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                   if(intent.resolveActivity(getPackageManager())!= null){
                       startActivityForResult(intent, SELECAO_CAMERA);
                   }

             }
         });
         buttonGaleria.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent( Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                 if(intent.resolveActivity(getPackageManager())!= null){
                     startActivityForResult(intent, SELECAO_GALERIA);
                 }
             }
         });

                        // atualiza nome do usuario logado
         imageAtualizarNome.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 String nome = editNomePerfil.getText().toString();
                   boolean retorno = UsuarioFirebase.atualizarNomeUsuario(nome);

                   if(retorno){
                         usuarioLogado.setNome(nome);
                         usuarioLogado.atualizar();

                       Toast.makeText(ConfiguracoesActivity.this,
                               "Sucesso ao alterar nome do usuario!",
                               Toast.LENGTH_SHORT).show();
                   }
             }
         });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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
                           circleImageViewFotoPerfil.setImageBitmap(imagem);

                            // recuperar dados da imagem para o firebase
                           ByteArrayOutputStream baos = new ByteArrayOutputStream();
                             imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                              byte[] dadosImagem = baos.toByteArray();

                             // salvar imagem no Firebase storage
                             final StorageReference imagemRef = storageReference
                                     .child("imagem")
                                     .child("perfil")
                                    // .child(identificadorUsuario)  // menos uma pasta no fire base storage
                                     .child(identificadorUsuario + ".jpeg");

                                  // recuperar dados da imagem
                           UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                             uploadTask.addOnFailureListener(new OnFailureListener() {
                                 @Override
                                 public void onFailure(@NonNull Exception e) {

                                     Toast.makeText(ConfiguracoesActivity.this,
                                             "Erro ao fazer upload da imagem!",
                                             Toast.LENGTH_SHORT).show();

                                 }
                             }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                 @Override
                                 public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                     Toast.makeText(ConfiguracoesActivity.this,
                                             "Sucesso ao fazer upload da imagem!",
                                             Toast.LENGTH_SHORT).show();

                                //   Uri url =  taskSnapshot.getDownloadUrl();
                                //      atualizarFotoUsuario(url);

                                     //teste para nova versão
                                     imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                         @Override
                                         public void onComplete(@NonNull Task<Uri> task) {
                                             Uri url =  task.getResult();
                                             atualizarFotoUsuario(url);

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

      public void atualizarFotoUsuario(Uri url){
             UsuarioFirebase.atualizarFotoUsuario(url);
          boolean retorno = UsuarioFirebase.atualizarFotoUsuario(url);
           if(retorno){
               usuarioLogado.setFoto(url.toString());
               usuarioLogado.atualizar();
               Toast.makeText(ConfiguracoesActivity.this,
                       "Sua foto foi alterada!",
                       Toast.LENGTH_SHORT).show();
           }



      }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

         for(int permissaoResultado: grantResults){
             if(permissaoResultado == PackageManager.PERMISSION_DENIED){
                     alertaValidacaoPermissao();
             }
         }
    }
         // metodo para validar as permissões do APP LabexWhats no dispositivo
    public void alertaValidacaoPermissao(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
           builder.setTitle("Permissão Negada!");
           builder.setMessage("Para utilizar o App é necessário aceitar as permissões!");
           builder.setCancelable(false);
           builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int which) {
                    finish();

               }
           });

           AlertDialog dialog = builder.create();
                       dialog.show();

    }
}
