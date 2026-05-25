package de.hartz.software.parannoying.core.model.domain

data class SideFact(
        val id: String,
        val category: String,
        val content: String,
        val isFact: Boolean)