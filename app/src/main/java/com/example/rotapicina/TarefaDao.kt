package com.example.rotapicina

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TarefaDao {

    @Insert
    suspend fun insert(tarefa: Tarefa)

    @Update
    suspend fun update(tarefa: Tarefa)

    @Delete
    suspend fun delete(tarefa: Tarefa)

    @Query("SELECT * FROM tarefas WHERE dia_semana = :dia ORDER BY horario IS NULL, horario ASC")
    suspend fun getTarefasPorDia(dia: String): List<Tarefa>

    @Query("DELETE FROM tarefas WHERE dia_semana IN (:dias)")
    suspend fun deleteTarefasPorDias(dias: List<String>)

    @Query("UPDATE tarefas SET concluida = 0")
    suspend fun desmarcarTodasTarefas()

    @Query("UPDATE tarefas SET texto = :texto, localizacao = :localizacao, horario = :horario WHERE group_id = :groupId")
    suspend fun updateByGroupId(groupId: String, texto: String, localizacao: String?, horario: Int?)

    @Query("UPDATE tarefas SET cloro = :cloro, ph = :ph, alcalinidade = :alcalinidade WHERE group_id = :groupId")
    suspend fun updateMedicoesByGroupId(groupId: String, cloro: Double?, ph: Double?, alcalinidade: Double?)
}
