package com.example.rotapicina

data class Tarefa(
    val texto: String,
    var concluida: Boolean = false,
    val localizacao: String? = null,
    val horario: Int? = null
)