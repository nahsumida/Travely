package com.isabellatressino.travely.activity

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.isabellatressino.travely.R
import com.isabellatressino.travely.databinding.ActivityCreditCardBinding
import java.time.LocalDate

class CreditCardActivity : AppCompatActivity() {

    private val binding by lazy { ActivityCreditCardBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)



        with(binding) {
            etCardNumber.addTextChangedListener(createTextWatcher(tvCardNumber))
            etCardName.addTextChangedListener(createTextWatcher(tvCardName))
            etExpirationDate.addTextChangedListener(createTextWatcher(tvCardExpirationDate))
            etCvv.addTextChangedListener(createTextWatcher(tvCvv))

            btnConfirm.setOnClickListener {
                isCardValid()
            }
        }

    }

    private fun createTextWatcher(
        textView: TextView,
    ): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                textView.text = s.toString()
            }
        }
    }

    private fun isCardValid() {
        with(binding) {


            // Verifica se os campos são nulos
            if (etCardNumber.text.isNullOrEmpty() ||
                etCardName.text.isNullOrEmpty() ||
                etExpirationDate.text.isNullOrEmpty() ||
                etCvv.text.isNullOrEmpty()
            ) {
                showAlertMessage("Erro", "Preencha todos os dados para continuar.")
                return
            }

            // Verifica se os campos são válidos
            val isCardNumberValid = isCardNumberValid(etCardNumber.unMasked)
            val isCardExpirationDateValid = isExpirationDateValid(etExpirationDate.masked)
            val isCardCvvValid = isCardCvvValid(etCvv.text.toString())

            if (!isCardNumberValid) etCardNumber.error = "Número de cartão inválido"

            if (!isCardExpirationDateValid) etExpirationDate.error = "Data inválida"

            if (!isCardCvvValid) etCvv.error = "Cvv inválido"

            if (isCardCvvValid && isCardExpirationDateValid && isCardNumberValid) {
                //startActivity(compra realizada com sucesso)
                Toast.makeText(this@CreditCardActivity, "Comprado", Toast.LENGTH_LONG).show()
            }

        }

    }

    private fun isCardNumberValid(cardNumber: String): Boolean {
        return cardNumber.length >= 16
    }

    private fun isExpirationDateValid(date: String): Boolean {

        // Obtém a data atual
        val currentDate = LocalDate.now()
        val currentYear = currentDate.year % 100
        val currentMonth = currentDate.monthValue

        val (expirationMonth, expirationYear) = date.split("/")

        // Verifica se o ano de expiração é menor que o ano atual ou se é o mesmo ano, mas o mês de expiração é menor
        if (expirationYear.toInt() < currentYear || (expirationYear.toInt() == currentYear && expirationMonth.toInt() < currentMonth)) {
            return false
        }

        // Verifica se o mês de expiração está dentro do intervalo de 1 a 12
        if (expirationMonth.toInt() !in 1..12) return false

        return true
    }

    private fun isCardCvvValid(cvv: String): Boolean {
        return cvv.length == 3
    }

    private fun showAlertMessage(type: String, message: String) {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.custom_dialog, null)

        val alertDialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val title = view.findViewById<TextView>(R.id.tv_title)
        val text = view.findViewById<TextView>(R.id.tv_text)
        val button = view.findViewById<Button>(R.id.btn_dialog)

        title.text = type
        text.text = message

        button.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()

        alertDialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

}