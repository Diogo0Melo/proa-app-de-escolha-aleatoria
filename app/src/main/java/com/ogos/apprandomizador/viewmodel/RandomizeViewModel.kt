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
 * Encenação de um sorteio: cinco números tirados do fluxo criptográfico, ainda
 * sem forma. Cada campo é uma unidade em `[0, 1)` que a View converte em
 * parâmetro concreto (percurso, excesso, vacilo, duração) usando o tamanho real
 * da sequência exibida — informação que só existe lá embaixo.
 */
data class SpinCosmetics(
    val travelUnit: Float,
    val overshootUnit: Float,
    val hesitationAmplitudeUnit: Float,
    val hesitationCyclesUnit: Float,
    val durationUnit: Float
)

/**
 * One pending draw. The ViewModel picks the winning [targetItem] FIRST; the 3D wheel
 * in the View then locates that item in the displayed (shuffled) list, animates the
 * ring until its slot sits at the center and calls back via finishSpin(). [listId]
 * ties the target to the list it was drawn from, guarding against list changes
 * during a spin. [requestId] makes every roll a distinct key even when the same
 * item is drawn twice in a row.
 *
 * [cosmetics] and [shuffleSeed] come from the same entropy cocktail as the
 * winner, so the whole performance — ordem dos cartões, distância percorrida,
 * se a roda passa do ponto, quão rápido o som bate — é um só acaso, não vários
 * ruídos independentes.
 */
data class SpinTarget(
    val targetItem: RaffleItem,
    val listId: Long,
    val requestId: Long,
    val cosmetics: SpinCosmetics,
    val shuffleSeed: Long
)

class RandomizeViewModel(private val repository: IRepository) : ViewModel() {

    private val secureRandom = SecureRandom()
    private var spinRequestId = 0L

    /** Coleta reativa da lista atual - cancelada ao trocar de lista. */
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
     * Inverted roll flow: choose the winner up-front, then hand the target to
     * the View. No animation and no recording happens here; finishSpin() does
     * that once the wheel lands.
     *
     * Entropy cocktail: SecureRandom(32B) + nanoTime + relógio de parede + id
     * da thread -> SHA-256. O digest vira um fluxo de blocos que paga, nesta
     * ordem: o índice do vencedor (amostragem por rejeição, uniforme), as
     * cinco unidades da encenação e a semente do embaralhamento da sequência de
     * exibição. Tudo do mesmo acaso — nada de kotlin.random fraco no caminho do
     * sorteio.
     */
    fun performRoll() {
        val list = currentItemList.value
        if (list.items.isEmpty()) return
        if (_isSpinning.value) return

        val seed = spinSeedMaterial(
            secureBytes = ByteArray(SECURITY_SEED_BYTES).also(secureRandom::nextBytes),
            nanoTime = System.nanoTime(),
            wallClockMillis = System.currentTimeMillis(),
            threadId = Thread.currentThread().id
        )
        val stream = deriveSpinStream(seed, spinRequestId + 1)

        val size = list.items.size
        val winnerIndex = pickWinnerIndex(stream, size) { secureRandom.nextInt(it) }
        val winner = list.items[winnerIndex]

        val cosmetics = SpinCosmetics(
            travelUnit = stream.nextDouble().toFloat(),
            overshootUnit = stream.nextDouble().toFloat(),
            hesitationAmplitudeUnit = stream.nextDouble().toFloat(),
            hesitationCyclesUnit = stream.nextDouble().toFloat(),
            durationUnit = stream.nextDouble().toFloat()
        )
        val shuffleSeed = stream.nextIntNonNegative().toLong() and 0xFFFFFFFFL

        spinRequestId += 1
        _spinTarget.value =
            SpinTarget(
                targetItem = winner,
                listId = list.id,
                requestId = spinRequestId,
                cosmetics = cosmetics,
                shuffleSeed = shuffleSeed
            )
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
        // Clear the target synchronously: guards against double completion and
        // lets the View lock the landed sequence immediately.
        _spinTarget.value = null
        val list = currentItemList.value
        // Validate the item is still in the current list (guards against list change during spin)
        if (target.targetItem in list.items && list.id == target.listId) {
            // Persist-before-publish: the draw is recorded FIRST; the result is
            // only surfaced (and the button re-enabled) after the Room write
            // commits, so a process death between landing and commit can never
            // show an unpersisted result.
            viewModelScope.launch {
                repository.recordDraw(listId = target.listId, itemName = target.targetItem.name)
                // Re-check after the await: if the list switched mid-write, discard
                // the publish (silent-discard semantics), but always re-enable.
                if (currentItemList.value.id == target.listId) {
                    _currentRandomItem.value = target.targetItem
                }
                _isSpinning.value = false
            }
        } else {
            _isSpinning.value = false
        }
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

    private companion object {
        /** Bytes de entropia crua do coquetel (o SHA-256 os comprime em 32). */
        const val SECURITY_SEED_BYTES = 32
    }
}
