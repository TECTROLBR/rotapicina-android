package com.example.rotapicina

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dias = listOf("Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado")
        val botoes = listOf(
            R.id.btnSegunda,
            R.id.btnTerca,
            R.id.btnQuarta,
            R.id.btnQuinta,
            R.id.btnSexta,
            R.id.btnSabado
        )

        // Configura o clique para todos os botões
        botoes.forEachIndexed { index, btnId ->
            findViewById<Button>(btnId).setOnClickListener {
                val intent = Intent(this, TarefasActivity::class.java)
                intent.putExtra("DIA_SEMANA", dias[index])
                startActivity(intent)
            }
        }

        // Destaca o botão do dia atual
        val calendario = Calendar.getInstance()
        when (calendario.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> findViewById<Button>(R.id.btnSegunda).setBackgroundColor(Color.parseColor("#4CAF50"))
            Calendar.TUESDAY -> findViewById<Button>(R.id.btnTerca).setBackgroundColor(Color.parseColor("#4CAF50"))
            Calendar.WEDNESDAY -> findViewById<Button>(R.id.btnQuarta).setBackgroundColor(Color.parseColor("#4CAF50"))
            Calendar.THURSDAY -> findViewById<Button>(R.id.btnQuinta).setBackgroundColor(Color.parseColor("#4CAF50"))
            Calendar.FRIDAY -> findViewById<Button>(R.id.btnSexta).setBackgroundColor(Color.parseColor("#4CAF50"))
            Calendar.SATURDAY -> findViewById<Button>(R.id.btnSabado).setBackgroundColor(Color.parseColor("#4CAF50"))
        }
    }
}