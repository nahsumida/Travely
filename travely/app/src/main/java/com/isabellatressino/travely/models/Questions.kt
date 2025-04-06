package com.isabellatressino.travely.models

data class Option(
    val label: String,
    val iconResId: Int,
    var isSelected: Boolean = false
)

data class Question(
    val text: String,
    val options: List<Option>
)
