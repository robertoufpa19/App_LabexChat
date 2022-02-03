package com.robertocursoandroid.whatsapp.activity.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.robertocursoandroid.whatsapp.R;
import com.robertocursoandroid.whatsapp.activity.config.ConfiguracaoFirebase;
import com.robertocursoandroid.whatsapp.activity.model.Usuario;

public class LoginActivity extends AppCompatActivity {

      private TextInputEditText campoEmail, campoSenha;

       private FirebaseAuth  autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        campoEmail = findViewById(R.id.editLoginEmail);
        campoSenha = findViewById(R.id.editLoginSenha);


    }

        public  void logarUsuario(Usuario  usuario){

        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {



                if (task.isSuccessful()){

                      abrirTelaPrincipal();

                }else{
                    String excecao = "";

                    try{
                        throw task.getException();
                    }catch (FirebaseAuthInvalidUserException e){
                        excecao = "Usuario não esta cadastrado!";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        excecao = "Email ou senha não correspondem ao usuario cadastrado!";
                    }catch (Exception e){
                        excecao = "Erro ao cadastrar o usuario!"+ e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(LoginActivity.this,
                            excecao,
                            Toast.LENGTH_SHORT).show();

                }
            }
        });
        }



      public void validarAutenticacaoUsuario(View view){
          String textoEmail = campoEmail.getText().toString();
          String textoSenha = campoSenha.getText().toString();


             // verifica se email e senha foram digitados
          if(!textoEmail.isEmpty()){ // verifica se o email nao esta vazio
              if(!textoSenha.isEmpty()){ // verifica se a senha nao esta vazio

                  Usuario  usuario = new Usuario();
                  usuario.setEmail(textoEmail);
                  usuario.setSenha(textoSenha);

                  logarUsuario(usuario);

              }else{
                  Toast.makeText(LoginActivity.this,
                          "Preencha a senha",
                          Toast.LENGTH_SHORT).show();
              }

          }else{
              Toast.makeText(LoginActivity.this,
                      "Preencha o email",
                      Toast.LENGTH_SHORT).show();
          }


      }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser usuarioAtual = autenticacao.getCurrentUser();

        if(usuarioAtual != null){
            abrirTelaPrincipal();
         }


    }

    public void abrirTelaCadastro(View view){
         Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
           startActivity(intent);
     }

      public void abrirTelaPrincipal(){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
          startActivity(intent);
      }


}
