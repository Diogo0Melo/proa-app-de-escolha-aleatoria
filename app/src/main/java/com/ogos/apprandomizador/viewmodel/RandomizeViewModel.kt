package com.ogos.apprandomizador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ogos.apprandomizador.data.repository.IRepository
import com.ogos.apprandomizador.model.ItemList
import com.ogos.apprandomizador.model.RaffleItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.SecureRandom

/**
 * One pending draw. The ViewModel picks the winning [targetItem] FIRST; the 3D wheel
 * in the View then locates that item in the displayed (shuffled) list, animates the
 * ring until its slot sits at the center and calls back via finishSpin(). [listId]
 * ties the target to the list it was drawn from, guarding against list changes
 * during a spin. [requestId] makes every roll a distinct key even when the same
 * item is drawn twice in a row.
 */
data class SpinTarget(
    val targetItem: RaffleItem,
    val listId: Long,
    val requestId: Long
)

class RandomizeViewModel(private val repository: IRepository) : ViewModel() {

    private val secureRandom = SecureRandom()
    private var spinRequestId = 0L

    private var currentItemJob: Job? = null

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
        val winner = list.items[secureRandom.nextInt(list.items.size)]
        spinRequestId += 1
        _spinTarget.value =
            SpinTarget(targetItem = winner, listId = list.id, requestId = spinRequestId)
        _isSpinning.value = true
    }

    /**
     * Wheel-completion callback. Records the draw atomically via the repository's
     * recordDraw (draw_history entry + ItemList counters in one transaction).
     * The item/listId validation guards against list changes during the spin, and
     * the null-target check guards against double completion
     * (e.g. animation finished + cancellation race).
     */
    fun finishSpin() {
        val target = _spinTarget.value ?: return
        val list = currentItemList.value
        // Validate the item is still in the current list (guards against list change during spin)
        if (target.targetItem in list.items && list.id == target.listId) {
            _currentRandomItem.value = target.targetItem
            viewModelScope.launch {
                repository.recordDraw(listId = target.listId, itemName = target.targetItem.name)
            }
        }
        _spinTarget.value = null
        _isSpinning.value = false
    }

    fun defaultText(defaultString: String, color: Long) {
        _currentRandomItem.value = RaffleItem(defaultString, color)
    }

    fun setCurrentItem(id: Long) {
        // Cancel any in-progress spin when switching lists
        _spinTarget.value = null
        _isSpinning.value = false
        // Cancel previous collection and start reactive observation
        currentItemJob?.cancel()
        currentItemJob = viewModelScope.launch {
            repository.getItemFlow(id).collect { item ->
                if (item != null) {
                    _currentItemList.value = item
                }
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
