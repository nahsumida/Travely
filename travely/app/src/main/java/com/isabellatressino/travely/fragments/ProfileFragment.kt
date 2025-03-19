package com.isabellatressino.travely.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.isabellatressino.travely.R
import com.isabellatressino.travely.databinding.FragmentHomeBinding
import com.isabellatressino.travely.databinding.FragmentProfileBinding
import com.isabellatressino.travely.viewmodel.UserViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        observeUserData()

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "9agkbQSGtkTnmPXHQ39oVj9nQu42"
        userViewModel.fetchUser(uid, requireContext())

        return binding.root
    }

    private fun observeUserData() {
        userViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvName.text = user.name
                binding.tvEmail.text = user.email
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}