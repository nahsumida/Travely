package com.isabellatressino.travely.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isabellatressino.travely.dao.UserDao
import com.isabellatressino.travely.databinding.FragmentHomeBinding
import com.isabellatressino.travely.interfaces.IUserDao

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val userDao: IUserDao = UserDao()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

//        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val uid = "9agkbQSGtkTnmPXHQ39oVj9nQu42"
        if (uid != null) {
            getUserInfo(uid)
        }

        return binding.root
    }

    private fun getUserInfo(uid: String) {
        userDao.getUserByAuthId(
            uid,
            onSuccess = { user ->
                if (user != null) {
                    binding.tvName.text = user.name
                    binding.tvProfile.text = user.profile
                } else {
                    Log.w("getUserInfo", "Usuário não encontrado")
                    Toast.makeText(requireContext(), "Usuário não encontrado", Toast.LENGTH_SHORT)
                        .show()
                }
            },
            onFailure = { exception ->
                Log.e("getUserInfo", "Erro ao buscar usuário", exception)
                Toast.makeText(
                    requireContext(),
                    "Erro ao carregar dados do usuário",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
