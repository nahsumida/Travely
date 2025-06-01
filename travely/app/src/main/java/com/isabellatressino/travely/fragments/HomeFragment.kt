package com.isabellatressino.travely.fragments

import RecommendationItem
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.isabellatressino.travely.MapActivity
import com.isabellatressino.travely.adapters.CarouselAdapter
import com.isabellatressino.travely.databinding.FragmentHomeBinding
import com.isabellatressino.travely.viewmodel.UserViewModel
import org.json.JSONObject

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        setupUserInfo()
        setupListeners()
        return binding.root
    }

    private fun setupUserInfo() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            userViewModel.fetchUser(uid, requireContext())
        }

        // Observa os dados e carrega o carrossel quando disponíveis
        userViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvName.text = it.name
                binding.tvProfile.text = it.getProfileDescription()
                setupCarousel(uid = FirebaseAuth.getInstance().currentUser?.uid, profile = it.profile)
            }
        }
    }

    private fun setupListeners() {
        binding.cardViewMap.setOnClickListener {
            startActivity(Intent(requireContext(), MapActivity::class.java))
        }
    }

    private fun setupCarousel(uid: String?, profile: String?) {
        if (uid == null || profile == null) {
            Log.e("setupCarousel", "UID ou perfil ausente")
            return
        }

        val recyclerView = binding.carouselRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val jsonBody = JSONObject().apply {
            put("auth_id", uid)
            put("profile", profile)
        }

        val request = object : JsonObjectRequest(
            Method.POST,
            "http://10.0.2.2:8000/recomendar/",
            jsonBody,
            { response ->
                try {
                    val recommendations = mutableListOf<RecommendationItem>()

                    // Pega o array "recomendacoes" dentro do objeto response
                    val recArray = response.getJSONArray("recomendacoes")

                    for (i in 0 until recArray.length()) {
                        val obj = recArray.getJSONObject(i)
                        val subtypes = List(obj.getJSONArray("subtypes").length()) { j ->
                            obj.getJSONArray("subtypes").getString(j)
                        }
                        recommendations.add(
                            RecommendationItem(
                                id = obj.getString("id"),
                                name = obj.getString("name"),
                                predictedInterest = obj.getDouble("predicted_interest"),
                                subtypes = subtypes
                            )
                        )
                    }

                    Log.d("setupCarousel", "Itens recomendados: ${recommendations.size}")
                    recyclerView.adapter = CarouselAdapter(recommendations)
                } catch (e: Exception) {
                    Log.e("setupCarousel", "Erro ao processar resposta: ${e.message}", e)
                }
            },
            { error ->
                Log.e("setupCarousel", "Erro na requisição: ${error.message}", error)
                if (error.networkResponse != null) {
                    Log.e("setupCarousel", "Código HTTP: ${error.networkResponse.statusCode}")
                    Log.e("setupCarousel", "Corpo do erro: ${String(error.networkResponse.data)}")
                }
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return hashMapOf("Content-Type" to "application/json")
            }
        }

        Volley.newRequestQueue(requireContext()).add(request)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
