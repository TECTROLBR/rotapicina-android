package com.example.rotapicina

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TarefasViewModel(application: Application) : AndroidViewModel(application) {

    private val tarefaDao: TarefaDao
    private val _tarefas = MutableLiveData<List<Tarefa>>()
    val tarefas: LiveData<List<Tarefa>> = _tarefas

    init {
        val db = AppDatabase.getDatabase(application)
        tarefaDao = db.tarefaDao()
    }

    fun carregarTarefas(dia: String) {
        viewModelScope.launch {
            _tarefas.value = tarefaDao.getTarefasPorDia(dia)
        }
    }

    fun updateTarefa(tarefa: Tarefa) {
        viewModelScope.launch {
            tarefaDao.update(tarefa)
            carregarTarefas(tarefa.diaSemana) // Recarrega a lista para refletir a mudança
        }
    }

    fun deleteTarefa(tarefa: Tarefa) {
        viewModelScope.launch {
            tarefaDao.delete(tarefa)
            carregarTarefas(tarefa.diaSemana)
        }
    }

    fun updateByGroupId(tarefa: Tarefa) {
        viewModelScope.launch {
            tarefaDao.updateByGroupId(tarefa.groupId, tarefa.texto, tarefa.localizacao, tarefa.horario)
            carregarTarefas(tarefa.diaSemana)
        }
    }
}
