package com.isabellatressino.travely.fragments

import com.isabellatressino.travely.viewmodel.UserViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.isabellatressino.travely.databinding.FragmentHomeBinding

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

        observeUserData()

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "9agkbQSGtkTnmPXHQ39oVj9nQu42"
        userViewModel.fetchUser(uid, requireContext())

        return binding.root
    }

    private fun observeUserData() {
        userViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvName.text = user.name
                binding.tvProfile.text = user.profile
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}