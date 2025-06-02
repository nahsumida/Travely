package com.isabellatressino.travely

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.isabellatressino.travely.fragments.QuestionsFragment
import com.isabellatressino.travely.models.User

class QuestionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questions)

        val user: User? =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra("user", User::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getSerializableExtra("user") as? User
            }

        if (savedInstanceState == null && user != null) {
            val fragment = QuestionsFragment.newInstance(user)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
        }
    }
}