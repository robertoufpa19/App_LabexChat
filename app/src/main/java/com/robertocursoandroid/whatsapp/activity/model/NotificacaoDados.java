package com.robertocursoandroid.whatsapp.activity.model;

import java.util.ArrayList;
import java.util.List;

public class NotificacaoDados {

    //Estrutura de dados para enviar ao firebase
    /*
    {
        "to": "topicos ou token",
        "notification" : {
            "title": "Título da notificação",
            "body" : "corpo da notificação"
        }
    }
    */

    private String to;
    private Notificacao notification;
    private List<Usuario> toGrupo = new ArrayList<>();

    public NotificacaoDados(String to, Notificacao notification) {
        this.to = to;
        this.notification = notification;
    }


    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Notificacao getNotification() {
        return notification;
    }

    public void setNotification(Notificacao notification) {
        this.notification = notification;
    }

    public List<Usuario> getToGrupo() {
        return toGrupo;
    }

    public void setToGrupo(List<Usuario> toGrupo) {
        this.toGrupo = toGrupo;
    }
}
