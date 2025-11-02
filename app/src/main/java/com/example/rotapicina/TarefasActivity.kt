package com.example.rotapicina

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.ArrayList

class TarefasActivity : AppCompatActivity() {

    private lateinit var diaSemana: String
    private val tarefas = ArrayList<Tarefa>()
    private lateinit var adapter: TarefaAdapter
    private val gson = Gson()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tarefas)

        diaSemana = intent.getStringExtra("DIA_SEMANA") ?: ""
        sharedPreferences = getSharedPreferences("TAREFAS_APP", Context.MODE_PRIVATE)

        val titulo = findViewById<TextView>(R.id.tituloDia)
        titulo.text = "Tarefas de $diaSemana"

        val recycler = findViewById<RecyclerView>(R.id.recyclerTarefas)
        recycler.layoutManager = LinearLayoutManager(this)

        adapter = TarefaAdapter(
            tarefas,
            onTarefaClick = { tarefa ->
                tarefa.concluida = !tarefa.concluida
                adapter.notifyDataSetChanged()
                salvarTarefas()
            },
            onTarefaLongClick = { tarefa ->
                tarefas.remove(tarefa)
                ordenarESalvar()
            }
        )
        recycler.adapter = adapter

        val fab = findViewById<FloatingActionButton>(R.id.fabAdicionarTarefa)
        fab.setOnClickListener {
            mostrarDialogoAdicionarTarefa()
        }

        carregarTarefas()
    }

    private fun mostrarDialogoAdicionarTarefa() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_tarefa, null)
        val inputTarefa = dialogView.findViewById<EditText>(R.id.inputTarefa)
        val inputLocalizacao = dialogView.findViewById<EditText>(R.id.inputLocalizacao)
        val inputHorario = dialogView.findViewById<EditText>(R.id.inputHorario)

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        builder.setTitle("Adicionar Tarefa")

        builder.setPositiveButton("Adicionar") { dialog, _ ->
            val textoTarefa = inputTarefa.text.toString()
            val textoLocalizacao = inputLocalizacao.text.toString()
            val textoHorario = inputHorario.text.toString()

            if (textoTarefa.isNotEmpty()) {
                val horario = textoHorario.toIntOrNull()
                val localizacao = if (textoLocalizacao.isNotEmpty()) textoLocalizacao else null
                val novaTarefa = Tarefa(textoTarefa, false, localizacao, horario)
                tarefas.add(novaTarefa)
                ordenarESalvar()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun ordenarESalvar() {
        // Tarefas com horário vêm primeiro, ordenadas pelo horário.
        // Tarefas sem horário vêm depois.
        tarefas.sortWith(compareBy( { it.horario == null }, { it.horario } ))
        adapter.notifyDataSetChanged()
        salvarTarefas()
    }

    private fun salvarTarefas() {
        val editor = sharedPreferences.edit()
        val tarefasJson = gson.toJson(tarefas)
        editor.putString("lista_tarefas_$diaSemana", tarefasJson)
        editor.apply()
    }

    private fun carregarTarefas() {
        val tarefasJson = sharedPreferences.getString("lista_tarefas_$diaSemana", null)
        if (tarefasJson != null) {
            val tipo = object : TypeToken<ArrayList<Tarefa>>() {}.type
            val tarefasSalvas: ArrayList<Tarefa> = gson.fromJson(tarefasJson, tipo)
            tarefas.clear()
            tarefas.addAll(tarefasSalvas)
            ordenarESalvar() // Ordena ao carregar também
        }
    }
}