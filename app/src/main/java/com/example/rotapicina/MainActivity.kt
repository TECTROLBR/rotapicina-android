package com.example.rotapicina

import android.content.Intent
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
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private val dias = listOf("Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado")
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = AppDatabase.getDatabase(this)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

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

        destacarDiaAtual()
    }

    override fun onResume() {
        super.onResume()
        atualizarSaudacao()
        verificarELimparTarefasNoDomingo()
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
            R.id.action_sobre -> {
                mostrarDialogoSobre()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun mostrarDialogoSobre() {
        val versao = packageManager.getPackageInfo(packageName, 0).versionName
        AlertDialog.Builder(this)
            .setTitle("Sobre o App")
            .setMessage("Piscina Fácil\nVersão: $versao\n\n© 2024 Denys Mota\nhakermota@gmail.com")
            .setPositiveButton("OK", null)
            .show()
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
            .setPositiveButton("Limpar") { _, _ ->
                val diasSelecionados = checkBoxes.mapIndexedNotNull { index, checkBox ->
                    if (checkBox.isChecked) dias[index] else null
                }

                if (diasSelecionados.isNotEmpty()) {
                    AlertDialog.Builder(this)
                        .setTitle("Atenção")
                        .setMessage("Esta ação apagará permanentemente as tarefas dos dias selecionados. Deseja continuar?")
                        .setPositiveButton("Apagar") { _, _ ->
                            limparTarefasParaDias(diasSelecionados)
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun limparTarefasParaDias(diasParaLimpar: List<String>) {
        lifecycleScope.launch {
            db.tarefaDao().deleteTarefasPorDias(diasParaLimpar)
            Toast.makeText(this@MainActivity, "Tarefas dos dias selecionados foram limpas!", Toast.LENGTH_SHORT).show()
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
            .setPositiveButton("Adicionar") { _, _ ->
                val textoTarefa = inputTarefa.text.toString()
                if (textoTarefa.isNotEmpty()) {
                    val textoLocalizacao = inputLocalizacao.text.toString()
                    val textoHorario = inputHorario.text.toString()

                    val diasSelecionados = checkBoxes.mapIndexedNotNull { index, checkBox ->
                        if (checkBox.isChecked) dias[index] else null
                    }

                    if (diasSelecionados.isNotEmpty()) {
                        lifecycleScope.launch {
                            val groupId = UUID.randomUUID().toString()
                            diasSelecionados.forEach { dia ->
                                val novaTarefa = Tarefa(
                                    groupId = groupId,
                                    texto = textoTarefa,
                                    localizacao = textoLocalizacao.ifEmpty { null },
                                    horario = textoHorario.toIntOrNull(),
                                    diaSemana = dia
                                )
                                db.tarefaDao().insert(novaTarefa)
                            }
                            Toast.makeText(this@MainActivity, "Tarefa adicionada aos dias selecionados!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun verificarELimparTarefasNoDomingo() {
        val calendario = Calendar.getInstance()
        val diaSemana = calendario.get(Calendar.DAY_OF_WEEK)
        val diff = diaSemana - Calendar.SUNDAY
        calendario.add(Calendar.DAY_OF_YEAR, -diff)

        val sharedPrefs = getSharedPreferences("ULTIMA_LIMPEZA_PREFS", MODE_PRIVATE)
        val domingoDaSemana = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendario.time)
        val ultimaLimpeza = sharedPrefs.getString("ultima_limpeza_semana", null)

        if (domingoDaSemana != ultimaLimpeza) {
            lifecycleScope.launch {
                db.tarefaDao().desmarcarTodasTarefas()
                sharedPrefs.edit { putString("ultima_limpeza_semana", domingoDaSemana) }
                Toast.makeText(this@MainActivity, "Tarefas reiniciadas para a nova semana!", Toast.LENGTH_LONG).show()
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

        val dayToButtonIdMap = mapOf(
            Calendar.MONDAY to R.id.btnSegunda,
            Calendar.TUESDAY to R.id.btnTerca,
            Calendar.WEDNESDAY to R.id.btnQuarta,
            Calendar.THURSDAY to R.id.btnQuinta,
            Calendar.FRIDAY to R.id.btnSexta,
            Calendar.SATURDAY to R.id.btnSabado
        )

        val today = calendario.get(Calendar.DAY_OF_WEEK)
        dayToButtonIdMap[today]?.let { buttonId ->
            findViewById<Button>(buttonId).backgroundTintList = tintList
        }
    }
}