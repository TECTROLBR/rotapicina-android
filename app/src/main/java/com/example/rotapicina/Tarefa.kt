package com.example.rotapicina

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tarefas")
data class Tarefa(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @ColumnInfo(name = "group_id")
    var groupId: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "texto")
    var texto: String,

    @ColumnInfo(name = "concluida")
    var concluida: Boolean = false,

    @ColumnInfo(name = "localizacao")
    var localizacao: String? = null,

    @ColumnInfo(name = "horario")
    var horario: Int? = null,

    @ColumnInfo(name = "dia_semana")
    var diaSemana: String
)