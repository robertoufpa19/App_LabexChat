package com.robertocursoandroid.whatsapp.activity.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.robertocursoandroid.whatsapp.R;
import com.robertocursoandroid.whatsapp.activity.activity.ChatActivity;
import com.robertocursoandroid.whatsapp.activity.activity.GrupoActivity;
import com.robertocursoandroid.whatsapp.activity.adapter.ContatosAdapter;
import com.robertocursoandroid.whatsapp.activity.adapter.ConversasAdapter;
import com.robertocursoandroid.whatsapp.activity.config.ConfiguracaoFirebase;
import com.robertocursoandroid.whatsapp.activity.helper.RecyclerItemClickListener;
import com.robertocursoandroid.whatsapp.activity.helper.UsuarioFirebase;
import com.robertocursoandroid.whatsapp.activity.model.Conversa;
import com.robertocursoandroid.whatsapp.activity.model.Usuario;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContatosFragment extends Fragment {

    private RecyclerView recyclerViewListaContatos;
    private ContatosAdapter adapter;
    private ArrayList<Usuario> listaContatos = new ArrayList<>();
    private DatabaseReference usuariosRef;
    private ValueEventListener valueEventListenerContatos;
    private FirebaseUser usuarioAtual;




    public ContatosFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contatos, container, false);
        // configurações inicias
        recyclerViewListaContatos = view.findViewById(R.id.recyclerViewListaContatos);
        usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios");
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();




        //configurar adapter
        adapter = new ContatosAdapter(listaContatos, getActivity());

        // configurar recyclerview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewListaContatos.setLayoutManager(layoutManager);
        recyclerViewListaContatos.setHasFixedSize(true);
        recyclerViewListaContatos.setAdapter(adapter);

        // configurar evento de clique no recyclerview de contatos
        recyclerViewListaContatos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerViewListaContatos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            // evento de clique para ir na tela de conversas(Chat)
                            @Override
                            public void onItemClick(View view, int position) {

                                List<Usuario> listaUsuariosAtualizada = adapter.getContatos();


                                Usuario usuarioSelecionado = listaUsuariosAtualizada.get(position);
                                boolean cabeçalho = usuarioSelecionado.getEmail().isEmpty();
                                // condicão para selecionar criação de grupo ou selecionar contato para conversar
                                if(cabeçalho){
                                    // grupo
                                    Intent i = new Intent(getActivity(), GrupoActivity.class);
                                    startActivity(i);
                                }else{
                                    //contato
                                    Intent i = new Intent(getActivity(), ChatActivity.class);
                                    i.putExtra("chatContato",usuarioSelecionado);
                                    startActivity(i);
                                }


                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )

        );
        adicionarMenuGrupo();

        return  view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarContatos();
    }

    @Override
    public void onStop() {
        super.onStop();
        usuariosRef.removeEventListener(valueEventListenerContatos);
    }

    public  void recuperarContatos() {

        valueEventListenerContatos = usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                limparListaContatos();

                for (DataSnapshot dados: dataSnapshot.getChildren()){
                    Usuario usuario = dados.getValue(Usuario.class);

                    // verifica se o email não é igual ao do usuario logado!
                    // se for igual, então não aparece na lista de contatos
                    String emailUsuarioAtual = usuarioAtual.getEmail();
                    if(!emailUsuarioAtual.equals(usuario.getEmail())){
                        listaContatos.add(usuario);
                    }

                }
                adapter.notifyDataSetChanged();


            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public  void limparListaContatos(){
        listaContatos.clear();
        adicionarMenuGrupo();

    }

    public  void adicionarMenuGrupo(){
        // adiciona icone de grupo na lista de contatos
        // usuario com email vazio que sera usado como cabeçalho
        Usuario itemGrupo = new Usuario();
        itemGrupo.setNome("Novo Grupo");
        itemGrupo.setEmail("");

        listaContatos.add(itemGrupo);
    }

    public void pesquisarContatos(String texto){
        //Log.d("pesquisa",  texto );

        List<Usuario> listaContatosBusca = new ArrayList<>();

        for ( Usuario usuario : listaContatos ){

            String nome = usuario.getNome().toLowerCase();
            if(nome.contains(texto)){
                listaContatosBusca.add(usuario);
            }


        }

        adapter = new ContatosAdapter(listaContatosBusca, getActivity());
        recyclerViewListaContatos.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    public void recarregarContatos(){
        adapter = new ContatosAdapter(listaContatos, getActivity());
        recyclerViewListaContatos.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }


}
