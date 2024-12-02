package com.isabellatressino.travely.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.isabellatressino.travely.R
import com.isabellatressino.travely.databinding.ActivityPaymentBinding

class PaymentActivity : AppCompatActivity() {

    private val binding by lazy { ActivityPaymentBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Configuração do botão de voltar
        binding.btnBack.setOnClickListener {
            finish() // Finaliza a Activity atual e retorna à anterior
        }

        // Listener para o método de pagamento selecionado
        binding.cardMetodoPagamento.apply {
            binding.rbCartao.setOnClickListener { handlePaymentMethodSelection("Cartão de Crédito") }
            binding.rbPix.setOnClickListener { handlePaymentMethodSelection("Pix") }
        }

        // Configuração de exibição de informações do pagamento
        setupPaymentInfo()
    }

    /**
     * Configura as informações da reserva no CardView
     */
    private fun setupPaymentInfo() {
        try {
            // Substituir essas strings por dados reais do banco ou de um intent recebido
            binding.cardInformacoesReserva.apply {
                binding.tvPlaceName.text = "Parque Dom Pedro"
                binding.tvBookingDate.text = "10/10/2024    10:30"
                binding.tvTicketAmount.text = "3 Ingressos"
                binding.tvTotalPrice.text = "R$ 450,00"
            }
        } catch (e: Exception) {
            Log.e("setupPaymentInfo", "Erro ao carregar informações da reserva", e)
            Toast.makeText(this, "Erro ao carregar informações", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Lida com a seleção de um método de pagamento
     */
    private fun handlePaymentMethodSelection(method: String) {
        Toast.makeText(this, "Método selecionado: $method", Toast.LENGTH_SHORT).show()
        // Adicionar lógica de navegação ou manipulação com base na seleção
    }
}
