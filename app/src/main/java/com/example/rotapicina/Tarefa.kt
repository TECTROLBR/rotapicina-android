package com.example.rotapicina

data class Tarefa(
    var texto: String,
    var concluida: Boolean = false,
    var localizacao: String? = null,
    var horario: Int? = null
)