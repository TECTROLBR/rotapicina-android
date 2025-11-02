package com.example.rotapicina

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class TarefaAdapter(
    private val tarefas: List<Tarefa>,
    private val onTarefaClick: (Tarefa) -> Unit,
    private val onTarefaLongClick: (Tarefa) -> Unit,
    private val onTarefaEditClick: (Tarefa) -> Unit // Novo callback para edição
) : RecyclerView.Adapter<TarefaAdapter.TarefaViewHolder>() {

    inner class TarefaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.checkTarefa)
        val textoTarefa: TextView = view.findViewById(R.id.textoTarefa)
        val textoHorario: TextView = view.findViewById(R.id.textoHorario)
        val btnEditar: ImageButton = view.findViewById(R.id.btnEditar) // Botão de editar
        val btnIr: ImageButton = view.findViewById(R.id.btnIr)

        fun bind(tarefa: Tarefa) {
            checkBox.isChecked = tarefa.concluida
            textoTarefa.text = tarefa.texto

            if (tarefa.concluida) {
                textoTarefa.paintFlags = textoTarefa.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                textoTarefa.alpha = 0.5f
            } else {
                textoTarefa.paintFlags = textoTarefa.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                textoTarefa.alpha = 1.0f
            }

            if (tarefa.horario != null) {
                textoHorario.visibility = View.VISIBLE
                textoHorario.text = "${tarefa.horario}h"
            } else {
                textoHorario.visibility = View.GONE
            }

            if (!tarefa.localizacao.isNullOrBlank()) {
                btnIr.visibility = View.VISIBLE
                btnIr.setOnClickListener { /* ...código do botão Ir... */ }
            } else {
                btnIr.visibility = View.GONE
            }

            // Configura o clique para o botão de editar
            btnEditar.setOnClickListener {
                onTarefaEditClick(tarefa)
            }

            itemView.setOnClickListener {
                onTarefaClick(tarefa)
            }

            itemView.setOnLongClickListener {
                onTarefaLongClick(tarefa)
                true
            }

            checkBox.setOnClickListener {
                onTarefaClick(tarefa)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TarefaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tarefa, parent, false)
        return TarefaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TarefaViewHolder, position: Int) {
        holder.bind(tarefas[position])
    }

    override fun getItemCount() = tarefas.size
}