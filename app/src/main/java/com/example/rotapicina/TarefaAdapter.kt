package com.example.rotapicina

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout

class TarefaAdapter(
    private val onTarefaClick: (Tarefa) -> Unit,
    private val onTarefaLongClick: (Tarefa) -> Unit,
    private val onTarefaEditClick: (Tarefa) -> Unit,
    private val onProvaClick: (Tarefa) -> Unit,
    private val onSaveMedicao: (Tarefa) -> Unit
) : ListAdapter<Tarefa, TarefaAdapter.TarefaViewHolder>(TarefaDiffCallback()) {

    inner class TarefaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val checkBox: CheckBox = view.findViewById(R.id.checkTarefa)
        private val textoTarefa: TextView = view.findViewById(R.id.textoTarefa)
        private val textoHorario: TextView = view.findViewById(R.id.textoHorario)
        private val btnProva: ImageButton = view.findViewById(R.id.btnProva)
        private val btnEditar: ImageButton = view.findViewById(R.id.btnEditar)
        private val btnIr: ImageButton = view.findViewById(R.id.btnIr)
        private val btnExpand: ImageButton = view.findViewById(R.id.btnExpand)
        private val layoutMedicoes: LinearLayout = view.findViewById(R.id.layoutMedicoes)
        private val inputCloro: EditText = view.findViewById(R.id.inputCloro)
        private val layoutInputCloro: TextInputLayout = view.findViewById(R.id.layoutInputCloro)
        private val inputPh: EditText = view.findViewById(R.id.inputPh)
        private val layoutInputPh: TextInputLayout = view.findViewById(R.id.layoutInputPh)
        private val inputAlcalinidade: EditText = view.findViewById(R.id.inputAlcalinidade)
        private val layoutInputAlcalinidade: TextInputLayout = view.findViewById(R.id.layoutInputAlcalinidade)
        private val btnSalvarMedicao: ImageButton = view.findViewById(R.id.btnSalvarMedicao)

        init {
            inputCloro.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val valor = s.toString().replace(',', '.').toDoubleOrNull()
                    atualizarCorCloro(valor)
                }
            })

            inputPh.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val valor = s.toString().replace(',', '.').toDoubleOrNull()
                    atualizarCorPh(valor)
                }
            })

            inputAlcalinidade.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val valor = s.toString().replace(',', '.').toDoubleOrNull()
                    atualizarCorAlcalinidade(valor)
                }
            })
        }

        fun atualizarCorCloro(valor: Double?) {
            val (corFundo, corTexto) = when {
                valor == null -> Pair(Color.WHITE, Color.BLACK)
                valor < 0.5 -> Pair(Color.parseColor("#FFFDE7"), Color.BLACK) // Amarelo muito pálido
                valor <= 1.0 -> Pair(Color.parseColor("#FFF59D"), Color.BLACK) // Amarelo claro
                valor <= 3.0 -> Pair(Color.parseColor("#FDD835"), Color.BLACK) // Amarelo Ouro (Ideal)
                valor <= 5.0 -> Pair(Color.parseColor("#FB8C00"), Color.BLACK) // Laranja
                else -> Pair(Color.parseColor("#D84315"), Color.WHITE) // Vermelho
            }
            layoutInputCloro.boxBackgroundColor = corFundo
            inputCloro.setTextColor(corTexto)
            
            val colorStateList = ColorStateList.valueOf(corTexto)
            layoutInputCloro.defaultHintTextColor = colorStateList
            layoutInputCloro.hintTextColor = colorStateList
            layoutInputCloro.boxStrokeColor = corTexto
        }

        fun atualizarCorPh(valor: Double?) {
            val (corFundo, corTexto) = when {
                valor == null -> Pair(Color.WHITE, Color.BLACK)
                valor < 7.0 -> Pair(Color.parseColor("#FFF59D"), Color.BLACK) // Amarelo (Ácido)
                valor <= 7.2 -> Pair(Color.parseColor("#FFB74D"), Color.BLACK) // Laranja (Baixo)
                valor <= 7.6 -> Pair(Color.parseColor("#EF5350"), Color.WHITE) // Vermelho/Rosa (Ideal teste Fenol)
                else -> Pair(Color.parseColor("#BA68C8"), Color.WHITE) // Roxo (Alto/Básico)
            }
            layoutInputPh.boxBackgroundColor = corFundo
            inputPh.setTextColor(corTexto)

            val colorStateList = ColorStateList.valueOf(corTexto)
            layoutInputPh.defaultHintTextColor = colorStateList
            layoutInputPh.hintTextColor = colorStateList
            layoutInputPh.boxStrokeColor = corTexto
        }

        fun atualizarCorAlcalinidade(valor: Double?) {
            val (corFundo, corTexto) = when {
                valor == null -> Pair(Color.WHITE, Color.BLACK)
                valor < 80 -> Pair(Color.parseColor("#EF5350"), Color.WHITE) // Vermelho (Baixo)
                valor <= 120 -> Pair(Color.parseColor("#66BB6A"), Color.WHITE) // Verde (Bom)
                else -> Pair(Color.parseColor("#EF5350"), Color.WHITE) // Vermelho (Alto)
            }
            layoutInputAlcalinidade.boxBackgroundColor = corFundo
            inputAlcalinidade.setTextColor(corTexto)

            val colorStateList = ColorStateList.valueOf(corTexto)
            layoutInputAlcalinidade.defaultHintTextColor = colorStateList
            layoutInputAlcalinidade.hintTextColor = colorStateList
            layoutInputAlcalinidade.boxStrokeColor = corTexto
        }

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

            // Bind values to EditTexts
            // setText triggers TextWatcher, so color updates automatically
            inputCloro.setText(tarefa.cloro?.toString() ?: "")
            inputPh.setText(tarefa.ph?.toString() ?: "")
            inputAlcalinidade.setText(tarefa.alcalinidade?.toString() ?: "")

            layoutMedicoes.visibility = View.GONE
            
            btnExpand.setOnClickListener {
                if (layoutMedicoes.visibility == View.VISIBLE) {
                    layoutMedicoes.visibility = View.GONE
                } else {
                    layoutMedicoes.visibility = View.VISIBLE
                }
            }

            btnSalvarMedicao.setOnClickListener {
                val novoCloro = inputCloro.text.toString().replace(',', '.').toDoubleOrNull()
                val novoPh = inputPh.text.toString().replace(',', '.').toDoubleOrNull()
                val novaAlcalinidade = inputAlcalinidade.text.toString().replace(',', '.').toDoubleOrNull()
                
                val tarefaAtualizada = tarefa.copy(
                    cloro = novoCloro, 
                    ph = novoPh,
                    alcalinidade = novaAlcalinidade
                )
                onSaveMedicao(tarefaAtualizada)
                
                Toast.makeText(itemView.context, "Medições Salvas", Toast.LENGTH_SHORT).show()
                layoutMedicoes.visibility = View.GONE
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