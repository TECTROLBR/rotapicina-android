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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class TarefaAdapter(
    private val onTarefaClick: (Tarefa) -> Unit,
    private val onTarefaLongClick: (Tarefa) -> Unit,
    private val onTarefaEditClick: (Tarefa) -> Unit,
    private val onProvaClick: (Tarefa) -> Unit
) : ListAdapter<Tarefa, TarefaAdapter.TarefaViewHolder>(TarefaDiffCallback()) {

    inner class TarefaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val checkBox: CheckBox = view.findViewById(R.id.checkTarefa)
        private val textoTarefa: TextView = view.findViewById(R.id.textoTarefa)
        private val textoHorario: TextView = view.findViewById(R.id.textoHorario)
        private val btnProva: ImageButton = view.findViewById(R.id.btnProva)
        private val btnEditar: ImageButton = view.findViewById(R.id.btnEditar)
        private val btnIr: ImageButton = view.findViewById(R.id.btnIr)

        fun bind(tarefa: Tarefa) {
            checkBox.isChecked = tarefa.concluida
            textoTarefa.text = tarefa.texto

            textoTarefa.paintFlags = if (tarefa.concluida) {
                textoTarefa.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                textoTarefa.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            textoTarefa.alpha = if (tarefa.concluida) 0.5f else 1.0f

            if (tarefa.horario != null) {
                textoHorario.visibility = View.VISIBLE
                textoHorario.text = "${tarefa.horario}h"
            } else {
                textoHorario.visibility = View.GONE
            }

            if (!tarefa.localizacao.isNullOrBlank()) {
                btnIr.visibility = View.VISIBLE
                btnIr.setOnClickListener {
                    val context = itemView.context
                    val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(tarefa.localizacao)}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    try {
                        context.startActivity(mapIntent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, "Nenhum aplicativo de mapa encontrado.", Toast.LENGTH_SHORT).show()
                        Log.e("TarefaAdapter", "Erro ao abrir mapa: ", e)
                    }
                }
            } else {
                btnIr.visibility = View.GONE
            }

            btnProva.setOnClickListener { onProvaClick(tarefa) }
            btnEditar.setOnClickListener { onTarefaEditClick(tarefa) }
            itemView.setOnLongClickListener { onTarefaLongClick(tarefa); true }
            checkBox.setOnClickListener { onTarefaClick(tarefa) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TarefaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tarefa, parent, false)
        return TarefaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TarefaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class TarefaDiffCallback : DiffUtil.ItemCallback<Tarefa>() {
    override fun areItemsTheSame(oldItem: Tarefa, newItem: Tarefa): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Tarefa, newItem: Tarefa): Boolean {
        return oldItem == newItem
    }
}