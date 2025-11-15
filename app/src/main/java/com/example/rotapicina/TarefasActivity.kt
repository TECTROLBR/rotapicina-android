package com.example.rotapicina

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.ArrayList

class TarefasActivity : AppCompatActivity() {

    private lateinit var diaSemana: String
    private val tarefas = ArrayList<Tarefa>()
    private lateinit var adapter: TarefaAdapter
    private val gson = Gson()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var contadorTarefas: TextView

    private var tarefaAtual: Tarefa? = null
    private var videoUri: Uri? = null

    private val permissaoCameraLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            lancarCamera()
        } else {
            Toast.makeText(this, "Permissão de câmera negada.", Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            videoUri?.let { compartilharVideo(it) }
        }
    }

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
            },
            onProvaClick = { tarefa ->
                iniciarFluxoDeProva(tarefa)
            }
        )
        recycler.adapter = adapter

        carregarTarefas()
    }

    private fun iniciarFluxoDeProva(tarefa: Tarefa) {
        tarefaAtual = tarefa
        permissaoCameraLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun lancarCamera() {
        val videoFile = File(filesDir, "video_prova.mp4")
        videoUri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", videoFile)

        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
        }
        cameraLauncher.launch(intent)
    }

    private fun compartilharVideo(uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "video/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, tarefaAtual?.texto) // Adiciona o texto da tarefa
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            shareIntent.`package` = "com.whatsapp.w4b" // Tenta o WhatsApp Business
            startActivity(shareIntent)
        } catch (e: Exception) {
            try {
                shareIntent.`package` = "com.whatsapp" // Tenta o WhatsApp normal
                startActivity(shareIntent)
            } catch (e2: Exception) {
                Toast.makeText(this, "WhatsApp ou WhatsApp Business não instalado.", Toast.LENGTH_SHORT).show()
            }
        }

        tarefaAtual?.let {
            it.concluida = true
            ordenarESalvar()
        }
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