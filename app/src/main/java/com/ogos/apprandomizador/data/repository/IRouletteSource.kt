package com.ogos.apprandomizador.data.repository

import com.ogos.apprandomizador.model.ItemList
import kotlinx.coroutines.flow.Flow

/** Leitura das roletas do usuário, usada pelo oráculo para listar e vincular fontes. */
interface IRouletteSource {
    val allItems: Flow<List<ItemList>>
    fun getItemFlow(id: Long): Flow<ItemList?>
}
