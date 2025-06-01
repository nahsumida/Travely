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

        Log.e("testeeeioio", "ajksncoasnciacsi")


        val recyclerView = binding.carouselRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val jsonBody = JSONObject().apply {
            put("auth_id", uid)
            put("profile", profile)
        }

        Log.e("testeeeioio", jsonBody.toString())

        val request = object : JsonObjectRequest(
            Method.POST,
            "http://10.0.2.2:8000/recomendar/",
            jsonBody,
            { response ->
                try {
                    Log.e("testeeeioio", "cheguei na response")
                    Log.e("testeeeioio", response.toString())

                    val recommendations = mutableListOf<RecommendationItem>()
                    for (i in 0 until response.length()) {
                        val obj = response.getJSONObject(i.toString())
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
                    binding.carouselRecyclerView.adapter = CarouselAdapter(recommendations)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        Volley.newRequestQueue(requireContext()).add(request)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
