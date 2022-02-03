package com.robertocursoandroid.whatsapp.activity.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.robertocursoandroid.whatsapp.R;
import com.robertocursoandroid.whatsapp.activity.config.ConfiguracaoFirebase;
import com.robertocursoandroid.whatsapp.activity.helper.Base64Custom;
import com.robertocursoandroid.whatsapp.activity.helper.UsuarioFirebase;
import com.robertocursoandroid.whatsapp.activity.model.Usuario;

public class CadastroActivity extends AppCompatActivity {

    private TextInputEditText campoNome, campoEmail, campoSenha;

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

       campoNome  = findViewById(R.id.editCadastroNome);
        campoEmail = findViewById(R.id.editCadastroEmail);
        campoSenha = findViewById(R.id.editCadastroSenha);


    }
        // cadastro do usuario em tempo real
      public void cadastrarUsuario(final Usuario usuario){
         autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
           autenticacao.createUserWithEmailAndPassword(


                   usuario.getEmail(), usuario.getSenha()


           ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
               @Override
               public void onComplete(@NonNull Task<AuthResult> task) {

                   if(task.isSuccessful()){
                       Toast.makeText(CadastroActivity.this,
                               "Sucesso ao cadastrar usuario!",
                               Toast.LENGTH_SHORT).show();
                       UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());

                       finish();

                        try {

                            String indentificadorUsuario = Base64Custom.codificarBase64(usuario.getEmail());
                             usuario.setId(indentificadorUsuario);
                             usuario.salvar();


                        }catch (Exception e){
                            e.printStackTrace();
                        }

                   }else{


                       String excecao = "";

                       try{
                           throw task.getException();
                       }catch (FirebaseAuthWeakPasswordException e){
                           excecao = "Digite uma senha mais forte!";
                       }catch (FirebaseAuthInvalidCredentialsException e){
                           excecao = "Digite uma email valido!";
                       }catch (FirebaseAuthUserCollisionException e){
                           excecao = "Esta conta ja foi cadastrada!";
                       }catch (Exception e){
                           excecao = "Erro ao cadastrar o usuario!"+ e.getMessage();
                           e.printStackTrace();
                       }

                       Toast.makeText(CadastroActivity.this,
                               excecao,
                               Toast.LENGTH_SHORT).show();


                   }


               }


           });

      }


      public  void validarCadastroUsuario(View view){
         // recuperar os textos dos campos
          String textoNome = campoNome.getText().toString();
          String textoEmail = campoEmail.getText().toString();
          String textoSenha = campoSenha.getText().toString();



            if(!textoNome.isEmpty()){ // verifica se o nome nao esta vazio
                if(!textoEmail.isEmpty()){ // verifica se o email nao esta vazio
                   if(!textoSenha.isEmpty()){ // verifica se a senha nao esta vazia

                         final Usuario usuario = new Usuario();
                                usuario.setNome(textoNome);
                                usuario.setEmail(textoEmail);
                                usuario.setSenha(textoSenha);


                            // indentificador Usuario Token para enviar notificação para um usuario
                       // inicio cadastro do token usuario
                       FirebaseMessaging.getInstance().getToken()
                               .addOnCompleteListener(new OnCompleteListener<String>() {
                                   @Override
                                   public void onComplete(@NonNull Task<String> task) {
                                       if (!task.isSuccessful()) {
                                           Log.w("Cadastro token", "Fetching FCM registration token failed", task.getException());
                                           return;
                                       }

                                       // Get new FCM registration token
                                       String token = task.getResult();
                                       usuario.setToken(token);

                                   }
                               });
                       // fim cadastro do token
                                cadastrarUsuario(usuario);

                   }else{
                       Toast.makeText(CadastroActivity.this,
                               "Preencha a Senha",
                               Toast.LENGTH_SHORT).show();
                   }
                }else{
                    Toast.makeText(CadastroActivity.this,
                            "Preencha o Email",
                            Toast.LENGTH_SHORT).show();
                }

            }else{
                Toast.makeText(CadastroActivity.this,
                                "Preencha o Nome",
                                Toast.LENGTH_SHORT).show();
            }


      }



}
