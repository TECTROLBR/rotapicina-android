package com.example.rotapicina

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("TAREFAS_APP", Context.MODE_PRIVATE)

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

        destacarDiaAtual()
        verificarELimparTarefasNoDomingo(dias)
    }

    private fun destacarDiaAtual() {
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

    private fun verificarELimparTarefasNoDomingo(dias: List<String>) {
        val calendario = Calendar.getInstance()
        val hoje = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val ultimaLimpeza = sharedPreferences.getString("ultima_limpeza", null)

        // Se for domingo e a limpeza ainda não foi feita hoje
        if (calendario.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY && hoje != ultimaLimpeza) {
            val editor = sharedPreferences.edit()

            dias.forEach { dia ->
                val chave = "lista_tarefas_$dia"
                val tarefasJson = sharedPreferences.getString(chave, null)
                if (tarefasJson != null) {
                    val tipo = object : TypeToken<ArrayList<Tarefa>>() {}.type
                    val tarefas: ArrayList<Tarefa> = gson.fromJson(tarefasJson, tipo)

                    tarefas.forEach { it.concluida = false } // Desmarca todas as tarefas

                    val novoJson = gson.toJson(tarefas)
                    editor.putString(chave, novoJson)
                }
            }

            editor.putString("ultima_limpeza", hoje) // Salva a data da limpeza
            editor.apply()

            // Exibe a notificação para o usuário
            Toast.makeText(this, "Tarefas da semana reiniciadas!", Toast.LENGTH_LONG).show()
        }
    }
}