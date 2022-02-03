package com.robertocursoandroid.whatsapp.activity.api;

import com.robertocursoandroid.whatsapp.activity.model.NotificacaoDados;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface NotificacaoService {

    @Headers({
            "Authorization:key=AAAA5Q2ZCZ0:APA91bHfaCjOXOrlgjrcnj0dUrCgdZ3c5L5FHliB3B3o_snA8WnGLNGnYQzl1YhtsOsMcwRyPv1YMnb4VxkoMlDarU5T13uAxeus6kR68N7KNl1Kiw93dBlwHn87pw7jtW4oAZwYw4DX",
            "Content-Type:application/json"
    })
    @POST("send")
    Call<NotificacaoDados> salvarNotificacao(@Body NotificacaoDados notificacaoDados);

}
