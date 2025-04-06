package com.isabellatressino.travely.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.isabellatressino.travely.R
import com.isabellatressino.travely.adapters.OptionAdapter
import com.isabellatressino.travely.databinding.FragmentQuestionsBinding
import com.isabellatressino.travely.models.Option
import com.isabellatressino.travely.models.Question


class QuestionsFragment : Fragment() {
    private var _binding: FragmentQuestionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var optionAdapter: OptionAdapter

    private val questions = listOf(
        Question(
            text = "Qual é o seu principal objetivo ao viajar?",
            options = listOf(
                Option("Negócios", R.drawable.profilebusiness),
                Option("Experiência Cultural", R.drawable.profileculture),
                Option("Gastronomia", R.drawable.profilefood),
                Option("Lazer e descanço", R.drawable.profilerelax),
                Option("Aventura", R.drawable.profileadventure),
                Option("Compras", R.drawable.profileshopp)
            )
        ),
        Question(
            text = "Com quem você costuma viajar?",
            options = listOf(
                Option("Sozinho(a)", R.drawable.icon_alone),
                Option("Família", R.drawable.icon_family),
                Option("Amigos", R.drawable.icon_friends),
                Option("Casal", R.drawable.icon_couple),
                Option("Excursão", R.drawable.icon_excursion)
            )
        ),
        Question(
            text = "Qual a sua faixa etária?",
            options = listOf(
                Option("Adolecente", R.drawable.icon_teenager),
                Option("Jovem", R.drawable.icon_young),
                Option("Adulto", R.drawable.icon_adult),
                Option("Meia-idade", R.drawable.icon_middle_age),
                Option("Idoso", R.drawable.icon_old),
            )
        ),
        Question(
            text = "Qual clima você prefere?",
            options = listOf(
                Option("Calor", R.drawable.icon_hot),
                Option("Frio", R.drawable.icon_cold)
            )
        )
    )

    private var currentQuestionIndex = 0
    private val answers = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        showCurrentQuestion()

        binding.buttonNext.setOnClickListener {
            val selectedOption = optionAdapter.getSelectedOption()
            if (selectedOption != null) {
                answers.add(selectedOption.label)
                goToNextQuestion()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Selecione uma opção para continuar",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showCurrentQuestion() {
        val question = questions[currentQuestionIndex]
        binding.questionTitle.text = question.text

        optionAdapter = OptionAdapter(question.options)
        binding.optionsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.optionsRecyclerView.adapter = optionAdapter

        binding.buttonNext.setOnClickListener {
            val selectedOption = optionAdapter.getSelectedOption()
            if (selectedOption != null) {
                answers.add(selectedOption.label)
                goToNextQuestion()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Selecione uma opção para continuar",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun goToNextQuestion() {
        currentQuestionIndex++
        if (currentQuestionIndex < questions.size) {
            showCurrentQuestion()
        } else {
            // Fim das perguntas, prossiga para próxima tela ou salve os dados
            Toast.makeText(requireContext(), "Mapeamento concluído!", Toast.LENGTH_SHORT).show()
            // Exemplo: startActivity(Intent(requireContext(), HomeActivity::class.java))
            Log.d("UserAnswer", "Respostas finais do usuário:")
            answers.forEach { q ->
                Log.d("UserAnswer", q)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}