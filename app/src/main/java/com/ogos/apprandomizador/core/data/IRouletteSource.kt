package com.ogos.apprandomizador.core.data

import com.ogos.apprandomizador.core.model.ItemList
import kotlinx.coroutines.flow.Flow

/** Leitura das roletas do usuário, usada pelo oráculo para listar e vincular fontes. */
interface IRouletteSource {
    val allItems: Flow<List<ItemList>>
    fun getItemFlow(id: Long): Flow<ItemList?>
}
