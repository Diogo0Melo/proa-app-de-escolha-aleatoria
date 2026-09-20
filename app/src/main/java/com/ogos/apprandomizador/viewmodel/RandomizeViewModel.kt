package com.ogos.apprandomizador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ogos.apprandomizador.data.repository.IRepository
import com.ogos.apprandomizador.model.DrawHistoryEntry
import com.ogos.apprandomizador.model.ItemList
import com.ogos.apprandomizador.model.RaffleItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.SecureRandom

/**
 * One pending draw. The ViewModel picks [targetIndex] FIRST; the 3D wheel
 * in the View then animates the ring until that slot sits at the center and
 * calls back via finishSpin(). [requestId] makes every roll a distinct key
 * even when the same index is drawn twice in a row.
 */
data class SpinTarget(
    val targetIndex: Int,
    val requestId: Long
)

class RandomizeViewModel(private val repository: IRepository) : ViewModel() {

    private val secureRandom = SecureRandom()
    private var spinRequestId = 0L

    private val _currentItemList = MutableStateFlow(ItemList())
    val currentItemList: StateFlow<ItemList> = _currentItemList.asStateFlow()
    private val _currentRandomItem = MutableStateFlow<RaffleItem>(RaffleItem("", 0))
    val currentRandomItem: StateFlow<RaffleItem> = _currentRandomItem.asStateFlow()
    private val _isSpinning = MutableStateFlow(false)
    val isSpinning: StateFlow<Boolean> = _isSpinning.asStateFlow()
    private val _spinTarget = MutableStateFlow<SpinTarget?>(null)
    val spinTarget: StateFlow<SpinTarget?> = _spinTarget.asStateFlow()

    /**
     * Inverted roll flow: choose the winner up-front with SecureRandom, then
     * hand the target to the View. No animation and no recording happens here;
     * finishSpin() does that once the wheel lands.
     */
    fun performRoll() {
        val list = currentItemList.value
        if (list.items.isEmpty()) return
        if (_isSpinning.value) return
        val targetIndex = secureRandom.nextInt(list.items.size)
        spinRequestId += 1
        _spinTarget.value = SpinTarget(targetIndex, spinRequestId)
        _isSpinning.value = true
    }

    /**
     * Wheel-completion callback. Records the draw in the separate
     * draw_history table (infinite, queryable) and updates ItemList
     * counters. The null-target check guards against double completion
     * (e.g. animation finished + cancellation race).
     */
    fun finishSpin() {
        val target = _spinTarget.value ?: return
        val list = currentItemList.value
        if (target.targetIndex in list.items.indices) {
            val drawn = list.items[target.targetIndex]
            val updated = list.recordDraw(drawn)
            _currentItemList.value = updated
            _currentRandomItem.value = drawn
            updateItem(updated)
            viewModelScope.launch {
                repository.saveDrawHistory(
                    DrawHistoryEntry(
                        itemListId = list.id,
                        resultName = drawn.name
                    )
                )
            }
        }
        _spinTarget.value = null
        _isSpinning.value = false
    }

    fun defaultText(defaultString: String, color: Long) {
        _currentRandomItem.value = RaffleItem(defaultString, color)
    }

    fun setCurrentItem(id: Long) {
        viewModelScope.launch {
            val item = repository.getItem(id)
            if (item != null) {
                _currentItemList.value = item
            }
        }
    }

    fun updateItem(item: ItemList) {
        viewModelScope.launch {
            repository.update(item)
        }
    }

    fun insertItem(item: ItemList) {
        viewModelScope.launch {
            repository.save(item)
        }
    }

    fun getAllItems(): Flow<List<ItemList>> = repository.allItems
}
