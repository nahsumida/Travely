package com.isabellatressino.travely

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding

class MainProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tvNome = findViewById<TextView>(R.id.tv_nome)
        val tvNomeCompleto = findViewById<TextView>(R.id.tv_nome_completo)
        val tvEmail = findViewById<TextView>(R.id.tv_email)
        val tvTipoPerfil = findViewById<TextView>(R.id.tv_tipo_perfil)


        carregarDadosUsuario(tvNome, tvNomeCompleto, tvEmail, tvTipoPerfil)
    }

    private fun carregarDadosUsuario(
        tvNome: TextView,
        tvNomeCompleto: TextView,
        tvEmail: TextView,
        tvTipoPerfil: TextView
    ) {

        

    }

}