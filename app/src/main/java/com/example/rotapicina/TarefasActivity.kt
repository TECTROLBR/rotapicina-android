package com.example.rotapicina

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.ArrayList

class TarefasActivity : AppCompatActivity() {

    private lateinit var diaSemana: String
    private val tarefas = ArrayList<Tarefa>()
    private lateinit var adapter: TarefaAdapter
    private val gson = Gson()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var contadorTarefas: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tarefas)

        diaSemana = intent.getStringExtra("DIA_SEMANA") ?: ""
        sharedPreferences = getSharedPreferences("TAREFAS_APP", MODE_PRIVATE)

        val titulo = findViewById<TextView>(R.id.tituloDia)
        contadorTarefas = findViewById(R.id.contadorTarefas)
        titulo.text = getString(R.string.titulo_tarefas_dia, diaSemana)

        val recycler = findViewById<RecyclerView>(R.id.recyclerTarefas)
        recycler.layoutManager = LinearLayoutManager(this)

        adapter = TarefaAdapter(
            tarefas,
            onTarefaClick = { tarefa ->
                tarefa.concluida = !tarefa.concluida
                ordenarESalvar()
            },
            onTarefaLongClick = { tarefa ->
                tarefas.remove(tarefa)
                ordenarESalvar()
            },
            onTarefaEditClick = { tarefa ->
                mostrarDialogoEditarTarefa(tarefa)
            }
        )
        recycler.adapter = adapter

        carregarTarefas()
    }

    private fun atualizarContador() {
        val concluidas = tarefas.count { it.concluida }
        val total = tarefas.size
        contadorTarefas.text = "$concluidas/$total"
    }

    private fun mostrarDialogoEditarTarefa(tarefa: Tarefa) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_tarefa, null)
        val inputTarefa = dialogView.findViewById<EditText>(R.id.inputTarefa)
        val inputLocalizacao = dialogView.findViewById<EditText>(R.id.inputLocalizacao)
        val inputHorario = dialogView.findViewById<EditText>(R.id.inputHorario)

        inputTarefa.setText(tarefa.texto)
        inputLocalizacao.setText(tarefa.localizacao ?: "")
        inputHorario.setText(tarefa.horario?.toString() ?: "")

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        builder.setTitle("Editar Tarefa")

        builder.setPositiveButton("Salvar") { dialog, _ ->
            val textoTarefa = inputTarefa.text.toString()
            val textoLocalizacao = inputLocalizacao.text.toString()
            val textoHorario = inputHorario.text.toString()

            if (textoTarefa.isNotEmpty()) {
                tarefa.texto = textoTarefa
                tarefa.localizacao = textoLocalizacao.ifEmpty { null }
                tarefa.horario = textoHorario.toIntOrNull()
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
        tarefas.sortWith(compareBy({ it.horario == null }, { it.horario }))
        adapter.notifyDataSetChanged()
        salvarTarefas()
        atualizarContador()
    }

    private fun salvarTarefas() {
        sharedPreferences.edit {
            val tarefasJson = gson.toJson(tarefas)
            putString("lista_tarefas_$diaSemana", tarefasJson)
        }
    }

    private fun carregarTarefas() {
        val tarefasJson = sharedPreferences.getString("lista_tarefas_$diaSemana", null)
        if (tarefasJson != null) {
            val tipo = object : TypeToken<ArrayList<Tarefa>>() {}.type
            val tarefasSalvas: ArrayList<Tarefa> = gson.fromJson(tarefasJson, tipo)
            tarefas.clear()
            tarefas.addAll(tarefasSalvas)
            ordenarESalvar()
        } else {
            atualizarContador()
        }
    }
}