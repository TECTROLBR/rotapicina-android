package com.example.rotapicina

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

class TarefasActivity : AppCompatActivity() {

    private lateinit var diaSemana: String
    private val tarefasViewModel: TarefasViewModel by viewModels()
    private lateinit var adapter: TarefaAdapter
    private lateinit var contadorTarefas: TextView

    private var tarefaAtual: Tarefa? = null

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val videoUris = result.data?.getStringArrayListExtra("video_uris")
            if (!videoUris.isNullOrEmpty()) {
                val uris = videoUris.map { Uri.parse(it) } as ArrayList<Uri>
                compartilharVideos(uris)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tarefas)

        diaSemana = intent.getStringExtra("DIA_SEMANA") ?: ""

        val titulo = findViewById<TextView>(R.id.tituloDia)
        contadorTarefas = findViewById(R.id.contadorTarefas)
        titulo.text = getString(R.string.titulo_tarefas_dia, diaSemana)

        setupRecyclerView()
        observeViewModel()

        tarefasViewModel.carregarTarefas(diaSemana)
    }

    private fun setupRecyclerView() {
        val recycler = findViewById<RecyclerView>(R.id.recyclerTarefas)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = TarefaAdapter(
            onTarefaClick = { tarefa -> tarefasViewModel.updateTarefa(tarefa.apply { concluida = !concluida }) },
            onTarefaLongClick = { tarefa -> onTarefaDelete(tarefa) },
            onTarefaEditClick = { tarefa -> mostrarDialogoEditarTarefa(tarefa) },
            onProvaClick = { tarefa -> iniciarFluxoDeProva(tarefa) }
        )
        recycler.adapter = adapter
    }

    private fun observeViewModel() {
        tarefasViewModel.tarefas.observe(this, Observer { tarefas ->
            adapter.submitList(tarefas)
            atualizarContador(tarefas)
        })
    }

    private fun onTarefaDelete(tarefa: Tarefa) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Tarefa")
            .setMessage("Tem certeza que deseja excluir esta tarefa?")
            .setPositiveButton("Excluir") { _, _ ->
                tarefasViewModel.deleteTarefa(tarefa)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEditarTarefa(tarefa: Tarefa) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_tarefa, null)
        val inputTarefa = dialogView.findViewById<EditText>(R.id.inputTarefa)
        val inputLocalizacao = dialogView.findViewById<EditText>(R.id.inputLocalizacao)
        val inputHorario = dialogView.findViewById<EditText>(R.id.inputHorario)

        inputTarefa.setText(tarefa.texto)
        inputLocalizacao.setText(tarefa.localizacao ?: "")
        inputHorario.setText(tarefa.horario?.toString() ?: "")

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Editar Tarefa")
            .setPositiveButton("Salvar") { _, _ ->
                val novoTexto = inputTarefa.text.toString()
                val novaLocalizacao = inputLocalizacao.text.toString().ifEmpty { null }
                val novoHorario = inputHorario.text.toString().toIntOrNull()

                if (novoTexto.isNotEmpty()) {
                    val tarefaAtualizada = tarefa.copy(texto = novoTexto, localizacao = novaLocalizacao, horario = novoHorario)
                    tarefasViewModel.updateByGroupId(tarefaAtualizada)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun iniciarFluxoDeProva(tarefa: Tarefa) {
        tarefaAtual = tarefa
        val intent = Intent(this, CameraActivity::class.java)
        cameraLauncher.launch(intent)
    }

    private fun compartilharVideos(uris: ArrayList<Uri>) {
        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "video/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            putExtra(Intent.EXTRA_TEXT, tarefaAtual?.texto)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            shareIntent.`package` = "com.whatsapp.w4b"
            startActivity(shareIntent)
        } catch (e: Exception) {
            try {
                shareIntent.`package` = "com.whatsapp"
                startActivity(shareIntent)
            } catch (e2: Exception) {
                Toast.makeText(this, "WhatsApp ou WhatsApp Business não instalado.", Toast.LENGTH_SHORT).show()
            }
        }
        tarefaAtual?.let { tarefasViewModel.updateTarefa(it.apply { concluida = true }) }
    }

    private fun atualizarContador(tarefas: List<Tarefa>) {
        val concluidas = tarefas.count { it.concluida }
        val total = tarefas.size
        contadorTarefas.text = "$concluidas/$total"
    }
}