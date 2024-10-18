package com.isabellatressino.travely

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isabellatressino.travely.databinding.ActivityLocationBinding
import java.util.*
import kotlin.math.*

/**
 * Activity responsável por gerenciar a localização do usuário e a seleção de opções de locação.
 * authors: Lais, Isabella, Alex e Marcos
 */
class LocationActivity : AppCompatActivity() {

    // Binding para acessar os elementos do layout
    private val binding by lazy { ActivityLocationBinding.inflate(layoutInflater) }

    // Variável que acessa os serviços de localização da API
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Variáveis para armazenar a latitude e longitude do usuário
    private var latitudeUser: Double = 0.0
    private var longitudeUser: Double = 0.0

    companion object {
        // Código de solicitação de permissão
        private const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }

    /**
     * BroadcastReceiver para finalizar a atividade quando um determinado filtro é recebido.
     * @author: Isabella
     */
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            finish()
        }
    }

    // Instância do banco de dados Firestore
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Registrar o BroadcastReceiver
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastReceiver, IntentFilter("meuFiltro"))

        // Inicializar o cliente de localização
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Verifique e solicite permissão de acesso à localização
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        } else {
            // Permissão já concedida, obter a localização atual
            //obterLocalizacaoAtual()
        }


    }



//    /**
//     * @author: Lais
//     */
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                obterLocalizacaoAtual()
//            } else {
//                // Permissão negada, mostrar mensagem ou tratar de outra forma
//                Toast.makeText(
//                    this,
//                    "Permissão de localização negada.",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }
//    }



    /**
     * Carrega os dados do banco de dados Firestore com base na localização atual do usuário.
     * @author: Lais
     */
//    private fun carregarDadosBanco() {
//        val sharedPreferences = getSharedPreferences("uid", MODE_PRIVATE)
//        val valorRecuperado = sharedPreferences.getString("uid", null)
//
//        Log.d(TAG, "Uid: $valorRecuperado")
//
//        val editLocal = findViewById<TextView>(R.id.local)
//
//        if (!valorRecuperado.isNullOrEmpty()) {
//            val documentReference = db.collection("Unidades de Locação").document(valorRecuperado)
//
//            Log.d(TAG, "DocumentReference: $documentReference")
//
//            documentReference.get()
//                .addOnSuccessListener { document ->
//                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
//
//                    if (document.exists()) {
//                        // O documento com o ID recuperado existe, agora você pode acessar seus dados
//                        val nome = document.getString("name")
//
//                        // Acessando a latitude e longitude do ponto de locação
//                        val latLng = document.getGeoPoint("latLng")
//
//                        if (latLng != null) {
//                            val latitude = latLng.latitude
//                            val longitude = latLng.longitude
//
//                            Log.d(TAG, "Latitude: $latitude")
//                            Log.d(TAG, "Longitude: $longitude")
//
//                            val distanciaLimite = 1 // distância limite em quilômetros
//
//                            val distancia = calcularDistancia(
//                                latitudeUser, longitudeUser, latitude, longitude
//                            )
//
//                            if (distancia <= distanciaLimite) {
//                                // A localização está próxima do ponto de locação
//
//                                editLocal.text = nome
//
//                                val pricesArray = document.get("prices") as? List<*>
//                                if (pricesArray != null) {
//                                    // Limpar as opções anteriores
//                                    clearRadioButtonOptions()
//                                    // Adicionar os preços aos RadioButtons
//                                    addPricesToRadioButtons(pricesArray)
//
//                                } else {
//                                    // O campo 'prices' não é um array ou está vazio
//                                    Toast.makeText(
//                                        this@LocationActivity,
//                                        "Não há preços associados com essa Unidade de Locação.",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                }
//
//                            } else {
//                                // A localização não está próxima do ponto de locação
//                                showAlertMessage("Aviso: Você deve estar a, no máximo, 1000 metros do último pin selecionado no mapa para alugar um armário.")
//                            }
//
//                        } else {
//                            Toast.makeText(
//                                this@LocationActivity,
//                                "Localização não permitida ou encontrada, tente novamente.",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//
//                    } else {
//                        // Faça algo para lidar com esse caso, se necessário
//                    }
//                }
//                .addOnFailureListener { exception ->
//                    Log.e(TAG, "Erro ao pegar o documento", exception)
//                }
//        } else {
//            showAlertMessage("Aviso: Selecione o pin da unidade desejada no mapa antes de tentar alugar um armário.")
//            finish()
//        }
//    }



}

