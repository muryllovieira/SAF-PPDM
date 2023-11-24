package br.senai.sp.jandira.loginsa

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import br.senai.sp.jandira.loginsa.repository.UserRepository
import br.senai.sp.jandira.loginsa.service.RetrofitHelper
import br.senai.sp.jandira.loginsa.service.UserService
import br.senai.sp.jandira.loginsa.ui.theme.LoginSATheme
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginSATheme {
                Login(lifecycleScope = lifecycleScope)

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Login(lifecycleScope: LifecycleCoroutineScope) {

    //REFERENCIA PARA ACESSO E MANiPULACAO DO CLOUD STORAGE


    //REFERENCIA PARA ACESSO E MANIPULACAO DO CLOUD FIRESTORE
    var firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    var context = LocalContext.current

    //obter fot oda galeria de imagens

    var fotoUri by remember {
        mutableStateOf<Uri?>(null)
    }

    //criar o objeto que abrira a galeria e retornara a uri da imagem selecionada
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) {
        fotoUri = it
    }

    var painter = rememberAsyncImagePainter(
        ImageRequest.Builder(context).data(fotoUri).build()
    )

    var emailState by remember {
        mutableStateOf("")
    }

    var passwordState by remember {
        mutableStateOf("")
    }

    var url by remember {
        mutableStateOf("")
    }
    var storageRef: StorageReference =
        FirebaseStorage.getInstance().reference.child("images")

    suspend fun register(
        login: String,
        senha: String,
        imagem: Uri
    ) {
        val userRepository = UserRepository()

        storageRef = storageRef.child(System.currentTimeMillis().toString())
        imagem?.let {

//            var uploadtask = storageRef.putFile(it)
//            uploadtask.addOnCompleteListener { task ->
            storageRef.putFile(it).addOnCompleteListener{task ->

                if (task.isSuccessful) {

                    storageRef.downloadUrl.addOnSuccessListener { uri ->

                        val map = HashMap<String, Any>()
                        map["pic"] = uri.toString()
                        Log.e("url", "register: ${uri}", )
                        firebaseFirestore.collection("images").add(map)
                            .addOnCompleteListener { firestoreTask ->

                                if (firestoreTask.isSuccessful) {
                                    Toast.makeText(
                                        context,
                                        "UPLOAD REALIZADO COM SUCESSO",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    url = uri.toString()

                                } else {
                                    Toast.makeText(
                                        context,
                                        "ERRO AO TENTAR REALIZAR O UPLOADdentro",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }


                            }

                    }

                } else {

                    Toast.makeText(
                        context,
                        "ERRO AO TENTAR REALIZAR O UPLOADfora",
                        Toast.LENGTH_SHORT
                    ).show()

                }


            }


            if (url != "") {

//                val response = userRepository.registerUser(login, senha, url)
                 val apiService = RetrofitHelper.getInstance().create(UserService::class.java)
                lifecycleScope.launch {
                    val body = JsonObject().apply {
                        addProperty("login", login)
                        addProperty("senha", senha)
                        addProperty("imagem" , url)
                    }
                    Log.e("body", "register: ${body}", )
                    val response = apiService.createUser(body)
                    if (response.isSuccessful) {
                        Log.d(MainActivity::class.java.simpleName, "$response")
                        Log.d(MainActivity::class.java.simpleName, "Registro bem-sucedido")
                        Toast.makeText(context, "Usuário criado", Toast.LENGTH_SHORT).show()

                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(
                            MainActivity::class.java.simpleName,
                            "Erro durante o registro: $errorBody"
                        )
                        Toast.makeText(context, "Erro durante o registro", Toast.LENGTH_SHORT).show()
                    }
                }


            }


        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "LOGIN",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Green
                    )

                }
                Spacer(modifier = Modifier.height(54.dp))
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier
                        .size(150.dp)
                        .clickable {
                            launcher.launch("image/*")
                        }
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center),
                        shape = CircleShape,
                        color = Color.Transparent
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img),
                            contentDescription = "",
                            modifier = Modifier
                                .fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                        Image(
                            painter = painter,
                            contentDescription = "",
                            modifier = Modifier
                                .fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
//

                    }
                    Image(
                        imageVector = ImageVector.vectorResource(id = R.drawable.baseline_camera_alt_24),
                        contentDescription = "",
                        modifier = Modifier
                            .height(28.dp)
                            .width(48.dp)
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp),
                        colorFilter = ColorFilter.tint(color = Color.Green)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = emailState,
                    onValueChange = { emailState = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    label = { Text(text = "E-mail") }

                )
                Spacer(modifier = Modifier.height(15.dp))
                OutlinedTextField(
                    value = passwordState,
                    onValueChange = { passwordState = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    label = { Text(text = "Password") }

                )
                Spacer(modifier = Modifier.height(54.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End,

                    ) {
                    Button(
                        onClick = {

                            lifecycleScope.launch {
                                register(emailState, passwordState, fotoUri!!)
                            }


                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(45.dp),
                        colors = ButtonDefaults.buttonColors(Color.Green)
                    ) {
                        Text(text = "CREATE ACCOUNT", color = Color.Cyan)
                    }
                }
            }
        }
    }
}
