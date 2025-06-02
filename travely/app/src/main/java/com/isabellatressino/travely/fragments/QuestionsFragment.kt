package com.isabellatressino.travely.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.isabellatressino.travely.LoginActivity
import com.isabellatressino.travely.MainScreenActivity
import com.isabellatressino.travely.R
import com.isabellatressino.travely.adapters.OptionAdapter
import com.isabellatressino.travely.dao.UserDao
import com.isabellatressino.travely.databinding.FragmentQuestionsBinding
import com.isabellatressino.travely.models.Option
import com.isabellatressino.travely.models.Question
import com.isabellatressino.travely.models.User


class QuestionsFragment : Fragment() {
    private var _binding: FragmentQuestionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var optionAdapter: OptionAdapter
    private var user: User? = null
    private val userDao by lazy { UserDao() }

    private var currentQuestionIndex = 0
    private val answers = mutableListOf<String>()

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

    companion object {
        fun newInstance(user: User): QuestionsFragment {
            val fragment = QuestionsFragment()
            val bundle = Bundle().apply {
                putSerializable("user", user)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        user = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable("user", User::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable("user") as? User
        }
    }

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
                // TODO: transformar em dialog
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
    }

    private fun goToNextQuestion() {
        currentQuestionIndex++
        if (currentQuestionIndex < questions.size) {
            showCurrentQuestion()
        } else {
            //user?.answers = answers
            user?.profile = classifyUserProfile(answers)

            Log.d("UserTest", "$user")

            user?.let {
                binding.buttonNext.isEnabled = false

                userDao.registerUser(
                    it,
                    onSuccess = {
                        // TODO: transformar em dialog
                        Toast.makeText(
                            requireContext(),
                            "Cadastro realizado com sucesso! Verifique seu e-mail.",
                            Toast.LENGTH_LONG
                        ).show()

                        val intent = Intent(requireContext(), LoginActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    },
                    onFailure = { errorMessage ->
                        binding.buttonNext.isEnabled = true
                        Toast.makeText(
                            requireContext(),
                            "Erro ao salvar usuário: $errorMessage",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.e("UserSaveError", "Erro ao salvar usuário: $errorMessage")
                    }
                )
            }
        }
    }

    private fun classifyUserProfile(answers: List<String>): String {
        return when {
            answers.contains("Experiência Cultural") -> "cultural"
            answers.contains("Compras") -> "compras"
            answers.contains("Gastronomia") -> "gastronomico"
            answers.contains("Aventura") -> "aventureiro"
            answers.contains("Negócios") -> "negocios"
            answers.contains("Lazer e descanço") -> "descanso"
            else -> "desconhecido"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}