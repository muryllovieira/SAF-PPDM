package br.senai.sp.jandira.loginsa.service

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UserService {
    @POST("/usuario/cadastrarUsuario")
    suspend fun createUser(@Body body: JsonObject): Response<JsonObject>
}