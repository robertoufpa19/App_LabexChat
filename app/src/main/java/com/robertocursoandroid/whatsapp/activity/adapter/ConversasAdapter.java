package com.robertocursoandroid.whatsapp.activity.adapter;


import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.robertocursoandroid.whatsapp.R;
import com.robertocursoandroid.whatsapp.activity.model.Conversa;
import com.robertocursoandroid.whatsapp.activity.model.Grupo;
import com.robertocursoandroid.whatsapp.activity.model.Usuario;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversasAdapter extends RecyclerView.Adapter<ConversasAdapter.MyViewHolder> {


    private List<Conversa> conversas;
    private Context context;

    public ConversasAdapter(List<Conversa> listaConversa, Context c) {
        this.conversas = listaConversa;
        this.context = c;
    }

    public List<Conversa> getConversas(){
        return this.conversas;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_contatos,parent,false);

        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {



        Conversa conversa = conversas.get( position );
        holder.ultimaMensagem.setText( conversa.getUltimaMensagem() );
        // condição para verificar se é uma conversa de grupo ou não
        if(conversa.getIsGrupo().equals("true")){
            Grupo grupo = conversa.getGrupo();
            holder.nome.setText(grupo.getNome());
            //configura foto de grupo
            if (grupo.getFoto() != null ){
                Uri uri = Uri.parse( grupo.getFoto() );
                Glide.with( context ).load( uri ).into( holder.foto );
            }else {
                holder.foto.setImageResource(R.drawable.padrao);
            }


        }else if(conversa.getIsGrupo().equals("false")){
            Usuario usuario = conversa.getUsuarioExibicao();

            if(usuario != null){
                holder.nome.setText( usuario.getNome() );
                //configura foto de usuarios na conversa
                if ( usuario.getFoto() != null ){
                    Uri uri = Uri.parse( usuario.getFoto() );
                    Glide.with( context ).load( uri ).into( holder.foto );
                }else {
                    holder.foto.setImageResource(R.drawable.padrao);
                }
            }


        }


    }

    @Override
    public int getItemCount() {
        return conversas.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        CircleImageView foto;
        TextView nome, ultimaMensagem;

        public MyViewHolder(View itemView) {
            super(itemView);

            foto = itemView.findViewById(R.id.imageViewFotoContatos);
            nome = itemView.findViewById(R.id.textNomeContato);
            ultimaMensagem = itemView.findViewById(R.id.textEmailContato);

        }
    }


}
