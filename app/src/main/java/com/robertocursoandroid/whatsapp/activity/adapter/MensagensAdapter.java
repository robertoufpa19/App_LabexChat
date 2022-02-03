package com.robertocursoandroid.whatsapp.activity.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.robertocursoandroid.whatsapp.R;
import com.robertocursoandroid.whatsapp.activity.helper.UsuarioFirebase;
import com.robertocursoandroid.whatsapp.activity.model.Mensagem;

import java.util.List;

public class MensagensAdapter extends RecyclerView.Adapter<MensagensAdapter.MyViewHolder> {
    private List<Mensagem> mensagens;
    private Context context;

    private static final int TIPO_REMETENTE = 0;
    private static final int TIPO_DESTINATARIO = 1;

    public MensagensAdapter(List<Mensagem> lista, Context c ) {
        this.mensagens = lista;
        this.context = c;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View item = null;
        // condição para verificar qual item utilizar no envio da mensagem ou do recebimento dela
        if(viewType == TIPO_REMETENTE){
            item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_mensagem_remetente, parent,false);

        }else if(viewType == TIPO_DESTINATARIO){
            item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_mensagem_destinatario, parent,false);
        }
        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Mensagem mensagem = mensagens.get(position);
        String msg = mensagem.getMensagem();
        String imagem = mensagem.getImagem();
        // se o usuario tiver uma imagem vamos exibir a imagem.Senão vamos exibir o texto
        if(imagem != null){
            Uri url = Uri.parse(imagem);
            Glide.with(context).load(url).into(holder.imagem); // imagem recuperada

            String nome = mensagem.getNome();
            if(!nome.isEmpty()){
                holder.nome.setText(nome);
            }else {
                holder.nome.setVisibility(View.GONE);
            }
            // esconder texto
            holder.mensagem.setVisibility(View.GONE);



        }else{
            holder.mensagem.setText(msg); // texto recuperado

            String nome = mensagem.getNome();
            if(!nome.isEmpty()){
                holder.nome.setText(nome);
            }else {
                holder.nome.setVisibility(View.GONE);
            }
            // esconder imagem
            holder.imagem.setVisibility(View.GONE);


        }

    }

    @Override
    public int getItemCount() {
        return mensagens.size();
    }

    @Override
    public int getItemViewType(int position) {

        Mensagem mensagem = mensagens.get(position); // recupera a mensagem para o priemiro item que sera exibido na lista
        String idUsuario = UsuarioFirebase.getIdentificadorUsuario(); // recupera o id do usuario

        if(idUsuario.equals(mensagem.getIdUsuario())){
            return TIPO_REMETENTE;
        }
        return  TIPO_DESTINATARIO;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView mensagem;
        TextView nome;
        ImageView imagem;

        public MyViewHolder(View itemView){
            super(itemView);
            mensagem = itemView.findViewById(R.id.textMensagemTexto);
            imagem = itemView.findViewById(R.id.imageMensagemFoto);
            nome = itemView.findViewById(R.id.textNomeExibicao);

        }
    }
}
