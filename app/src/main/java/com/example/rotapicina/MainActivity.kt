package com.example.rotapicina

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private val dias = listOf("Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        sharedPreferences = getSharedPreferences("TAREFAS_APP", MODE_PRIVATE)

        val botoes = listOf(R.id.btnSegunda, R.id.btnTerca, R.id.btnQuarta, R.id.btnQuinta, R.id.btnSexta, R.id.btnSabado)

        botoes.forEachIndexed { index, btnId ->
            findViewById<Button>(btnId).setOnClickListener {
                val intent = Intent(this, TarefasActivity::class.java)
                intent.putExtra("DIA_SEMANA", dias[index])
                startActivity(intent)
            }
        }

        val fab = findViewById<FloatingActionButton>(R.id.fabAdicionarTarefaPrincipal)
        fab.setOnClickListener {
            mostrarDialogoAdicionarTarefaRecorrente()
        }

        atualizarSaudacao()
        destacarDiaAtual()
        verificarELimparTarefasNoDomingo(dias)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_limpar_tarefas -> {
                mostrarDialogoLimparDias()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun mostrarDialogoLimparDias() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_limpar_dias, null)
        val checkBoxes = listOf(
            dialogView.findViewById<CheckBox>(R.id.checkSegunda),
            dialogView.findViewById<CheckBox>(R.id.checkTerca),
            dialogView.findViewById<CheckBox>(R.id.checkQuarta),
            dialogView.findViewById<CheckBox>(R.id.checkQuinta),
            dialogView.findViewById<CheckBox>(R.id.checkSexta),
            dialogView.findViewById<CheckBox>(R.id.checkSabado)
        )

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Limpar Tarefas")
            .setPositiveButton("Limpar") { dialog, _ ->
                val diasSelecionados = mutableListOf<String>()
                checkBoxes.forEachIndexed { index, checkBox ->
                    if (checkBox.isChecked) {
                        diasSelecionados.add(dias[index])
                    }
                }

                if (diasSelecionados.isNotEmpty()) {
                    AlertDialog.Builder(this)
                        .setTitle("Atenção")
                        .setMessage("Esta ação apagará permanentemente as tarefas dos dias selecionados. Deseja continuar?")
                        .setPositiveButton("Apagar") { _, _ ->
                            limparTarefasParaDias(diasSelecionados)
                            Toast.makeText(this, "Tarefas dos dias selecionados foram limpas!", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun limparTarefasParaDias(diasParaLimpar: List<String>) {
        sharedPreferences.edit {
            diasParaLimpar.forEach { dia ->
                remove("lista_tarefas_$dia")
            }
        }
    }

    private fun mostrarDialogoAdicionarTarefaRecorrente() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_tarefa_recorrente, null)
        val inputTarefa = dialogView.findViewById<EditText>(R.id.inputTarefa)
        val inputLocalizacao = dialogView.findViewById<EditText>(R.id.inputLocalizacao)
        val inputHorario = dialogView.findViewById<EditText>(R.id.inputHorario)

        val checkBoxes = listOf(
            dialogView.findViewById<CheckBox>(R.id.checkSegunda),
            dialogView.findViewById<CheckBox>(R.id.checkTerca),
            dialogView.findViewById<CheckBox>(R.id.checkQuarta),
            dialogView.findViewById<CheckBox>(R.id.checkQuinta),
            dialogView.findViewById<CheckBox>(R.id.checkSexta),
            dialogView.findViewById<CheckBox>(R.id.checkSabado)
        )

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Adicionar Tarefa Recorrente")
            .setPositiveButton("Adicionar") { dialog, _ ->
                val textoTarefa = inputTarefa.text.toString()
                if (textoTarefa.isNotEmpty()) {
                    val textoLocalizacao = inputLocalizacao.text.toString()
                    val textoHorario = inputHorario.text.toString()
                    val horario = textoHorario.toIntOrNull()
                    val localizacao = textoLocalizacao.ifEmpty { null }

                    val diasSelecionados = mutableListOf<String>()
                    checkBoxes.forEachIndexed { index, checkBox ->
                        if (checkBox.isChecked) {
                            diasSelecionados.add(dias[index])
                        }
                    }

                    if (diasSelecionados.isNotEmpty()) {
                        val novaTarefa = Tarefa(textoTarefa, false, localizacao, horario)
                        salvarTarefaParaDias(novaTarefa, diasSelecionados)
                        Toast.makeText(this, "Tarefa adicionada aos dias selecionados!", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun salvarTarefaParaDias(tarefa: Tarefa, dias: List<String>) {
        sharedPreferences.edit {
            dias.forEach { dia ->
                val chave = "lista_tarefas_$dia"
                val tarefasJson = sharedPreferences.getString(chave, null)
                val tarefasSalvas: ArrayList<Tarefa> = if (tarefasJson != null) {
                    val tipo = object : TypeToken<ArrayList<Tarefa>>() {}.type
                    gson.fromJson(tarefasJson, tipo)
                } else {
                    ArrayList()
                }
                tarefasSalvas.add(Tarefa(tarefa.texto, tarefa.concluida, tarefa.localizacao, tarefa.horario)) // Cria uma cópia
                tarefasSalvas.sortWith(compareBy({ it.horario == null }, { it.horario }))
                putString(chave, gson.toJson(tarefasSalvas))
            }
        }
    }

    private fun atualizarSaudacao() {
        val calendario = Calendar.getInstance()
        val hora = calendario.get(Calendar.HOUR_OF_DAY)
        val tituloSaudacao = findViewById<TextView>(R.id.tituloSaudacao)

        val saudacao = when (hora) {
            in 6..11 -> "Bom Dia, Técnico!"
            in 12..18 -> "Boa Tarde, Técnico!"
            in 19..23 -> "Boa Noite, Técnico!"
            else -> "Que bela Madrugada, Técnico!"
        }

        tituloSaudacao.text = saudacao
    }

    private fun destacarDiaAtual() {
        val calendario = Calendar.getInstance()
        val corDestaque = "#4CAF50".toColorInt()
        val tintList = ColorStateList.valueOf(corDestaque)

        when (calendario.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> findViewById<Button>(R.id.btnSegunda).backgroundTintList = tintList
            Calendar.TUESDAY -> findViewById<Button>(R.id.btnTerca).backgroundTintList = tintList
            Calendar.WEDNESDAY -> findViewById<Button>(R.id.btnQuarta).backgroundTintList = tintList
            Calendar.THURSDAY -> findViewById<Button>(R.id.btnQuinta).backgroundTintList = tintList
            Calendar.FRIDAY -> findViewById<Button>(R.id.btnSexta).backgroundTintList = tintList
            Calendar.SATURDAY -> findViewById<Button>(R.id.btnSabado).backgroundTintList = tintList
        }
    }

    private fun verificarELimparTarefasNoDomingo(dias: List<String>) {
        val calendario = Calendar.getInstance()
        val hoje = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val ultimaLimpeza = sharedPreferences.getString("ultima_limpeza", null)

        if (calendario.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY && hoje != ultimaLimpeza) {
            sharedPreferences.edit {
                dias.forEach { dia ->
                    val chave = "lista_tarefas_$dia"
                    val tarefasJson = sharedPreferences.getString(chave, null)
                    if (tarefasJson != null) {
                        val tipo = object : TypeToken<ArrayList<Tarefa>>() {}.type
                        val tarefas: ArrayList<Tarefa> = gson.fromJson(tarefasJson, tipo)

                        tarefas.forEach { it.concluida = false }

                        val novoJson = gson.toJson(tarefas)
                        putString(chave, novoJson)
                    }
                }
                putString("ultima_limpeza", hoje)
            }

            Toast.makeText(this, "Tarefas da semana reiniciadas!", Toast.LENGTH_LONG).show()
        }
    }
}