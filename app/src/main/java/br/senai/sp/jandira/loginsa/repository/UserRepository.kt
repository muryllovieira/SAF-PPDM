package br.senai.sp.jandira.loginsa.repository

import br.senai.sp.jandira.loginsa.service.RetrofitHelper
import br.senai.sp.jandira.loginsa.service.UserService
import com.google.gson.JsonObject
import retrofit2.Response

class UserRepository {

    private val apiService = RetrofitHelper.getInstance().create(UserService::class.java)

    suspend fun registerUser(login: String, senha: String, imagem: String): Response<JsonObject> {
        val requestBody = JsonObject().apply {
            addProperty("login", login)
            addProperty("senha", senha)
            addProperty("imagem", imagem)
        }

        return apiService.createUser(requestBody)
    }

}