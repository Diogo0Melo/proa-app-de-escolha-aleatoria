package com.ogos.apprandomizador.ui.view.component

import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Modelo puro do movimento do giro — separação entre a ROTEIRIZAÇÃO do teatro
 * (quão longe, quão rápido, até onde passa do vencedor, como assenta) e o motor
 * de animação, que apenas avalia essas funções uma vez por frame.
 *
 * Nada aqui toca estado do Compose. São funções escalares determinísticas sobre
 * `t ∈ [0, 1]`, o que as torna testáveis em JVM. Os parâmetros chegam do mesmo
 * fluxo criptográfico do vencedor, então cada rodada tem um roteiro diferente e
 * nenhum padrão se repete.
 *
 * Formato da curva:
 *
 *     p(t) = start + viagem * avanço(t) + vacilo(t) + excesso(t)
 *
 * - `avanço` é um Hermite cúbico por partes: cruzeiro rápido e constante,
 *   depois uma desaceleração em degraus até parar morto em cima do vencedor.
 * - `vacilo` é uma senoidal amortecida que vive só na janela da frenagem — a
 *   sensação de algo sendo convocado, não calculado. Zera antes de `t = 1`.
 * - `excesso` leva a roleta 1–2 cartas além do vencedor num pico suave e a traz
 *   de volta num arraste mais lento. Probabilístico: alguns giros passam do
 *   ponto, outros param exatamente nele.
 *
 * O pouso é exato por construção: `vacilo(1) = excesso(1) = 0` e
 * `avanço(1) = 1`, então `p(1) = winner` com velocidade zero. O resultado já foi
 * decidido antes — a roda é encenação.
 */

/** Faixa do percurso, em cartas. Escalada pelo tamanho da lista em produção. */
internal const val TRAVEL_MIN_SLOTS = 10
internal const val TRAVEL_MAX_SLOTS = 28

/** Orçamento de tempo do giro. */
internal const val SPIN_DURATION_MIN_MS = 2500
internal const val SPIN_DURATION_MAX_MS = 3500

/** Vacilo: amplitude em cartas, "frequência" em ciclos completos da janela. */
internal const val HESITATION_AMPLITUDE_MIN = 0.22f
internal const val HESITATION_AMPLITUDE_MAX = 0.40f
internal const val HESITATION_CYCLES_MIN = 3.5f
internal const val HESITATION_CYCLES_MAX = 6f

/** Micro-assentamento: respiração amortecida da carta central. */
internal const val SETTLE_SCALE_AMPLITUDE = 0.04f
internal const val SETTLE_BOB_SLOTS = 0.05f
internal const val SETTLE_FREQUENCY_HZ = 4.6f
internal const val SETTLE_DAMPING_PER_SECOND = 6.5f

/** Faixa do SoundPool: o tick afina na dúvida e engrossa na pressa. */
internal const val TICK_PITCH_MIN = 0.8f
internal const val TICK_PITCH_MAX = 1.2f

// NÓS DO HERMITE DE AVANÇO: (tempo, fração da viagem, inclinação).
// Cada segmento respeita o critério de Fritsch–Carlson (inclinação das pontas
// <= 3x a corda), então o avanço é monotônico — a roleta nunca anda para trás
// no grosso do movimento. O primeiro segmento tem corda igual às inclinações
// das pontas, o que torna o cruzeiro perfeitamente uniforme.
private const val ADVANCE_CRUISE_SLOPE = 1.8f

private const val HESITATION_WINDOW_START = 0.42f
private const val OVERSHOOT_WINDOW_START = 0.78f
private const val OVERSHOOT_PEAK_TIME = 0.915f

private fun advanceNodeTime(i: Int): Float = when (i) {
    0 -> 0f
    1 -> 0.40f
    2 -> 0.58f
    3 -> 0.78f
    else -> 1f
}

private fun advanceNodeFraction(i: Int): Float = when (i) {
    0 -> 0f
    1 -> 0.72f
    2 -> 0.86f
    3 -> 0.955f
    else -> 1f
}

private fun advanceNodeSlope(i: Int): Float = when (i) {
    0 -> ADVANCE_CRUISE_SLOPE
    1 -> ADVANCE_CRUISE_SLOPE
    2 -> 0.9f
    3 -> 0.5f
    else -> 0f
}

/**
 * Roteiro de um único giro. Imutável, criado uma vez por `requestId` — nunca
 * por frame.
 */
internal class SpinMotionPlan(
    /** Posição inicial (contínua: retoma de onde a roda estava). */
    val start: Float,
    /** Posição final: exatamente o slot absoluto do vencedor. */
    val winner: Float,
    /** Vacilo: amplitude em cartas e número de ciclos na janela. */
    val hesitationAmplitude: Float,
    val hesitationCycles: Float,
    /** Excesso além do vencedor, em cartas (0 quando o giro não passa do ponto). */
    val overshootSlots: Float
) {
    /** Distância real percorrida — o que a curva avança. */
    val distance: Float = max(0f, winner - start)

    internal val hesitationStart: Float = HESITATION_WINDOW_START
    internal val overshootStart: Float = OVERSHOOT_WINDOW_START
    internal val overshootPeak: Float = OVERSHOOT_PEAK_TIME
}

/**
 * Avanço monotônico em `[0, 1]` para `t ∈ [0, 1]`: cruzeiro uniforme, depois
 * frenagem em degraus suaves até inclinação zero.
 */
internal fun advanceFraction(tRaw: Float): Float {
    val t = tRaw.coerceIn(0f, 1f)
    var i = 0
    while (i < 3 && t > advanceNodeTime(i + 1)) i++
    val t0 = advanceNodeTime(i)
    val t1 = advanceNodeTime(i + 1)
    val p0 = advanceNodeFraction(i)
    val p1 = advanceNodeFraction(i + 1)
    val m0 = advanceNodeSlope(i)
    val m1 = advanceNodeSlope(i + 1)
    val span = t1 - t0
    if (span <= 0f) return p1
    val u = (t - t0) / span
    val u2 = u * u
    val u3 = u2 * u
    val h00 = 2f * u3 - 3f * u2 + 1f
    val h10 = u3 - 2f * u2 + u
    val h01 = -2f * u3 + 3f * u2
    val h11 = u3 - u2
    return h00 * p0 + h10 * span * m0 + h01 * p1 + h11 * span * m1
}

/**
 * Posição da roleta no instante `t` do giro. Função pura, sem alocação — é
 * avaliada uma vez por frame pelo motor de animação e exaustivamente pelos
 * testes.
 */
internal fun evaluatePose(plan: SpinMotionPlan, tRaw: Float): Float {
    val t = tRaw.coerceIn(0f, 1f)
    val base = plan.start + plan.distance * advanceFraction(t)
    return base + hesitationOffset(plan, t) + overshootOffset(plan, t)
}

/**
 * Vacilo: senoidal amortecida dentro da janela de frenagem. O envelope é
 * `sin²(π·u)`, que zera — junto com a derivada — nas duas pontas, então a
 * trepidação nasce e morre sem quina. Em `t = 1` contribui exatamente 0.
 */
internal fun hesitationOffset(plan: SpinMotionPlan, tRaw: Float): Float {
    val amp = plan.hesitationAmplitude
    if (amp <= 0f) return 0f
    val t = tRaw.coerceIn(0f, 1f)
    val start = plan.hesitationStart
    if (t <= start) return 0f
    val u = (t - start) / max(1f - start, 1e-6f)
    val envelope = sin(PI_F * u)
    // Decaimento extra: a última parte da frenagem é quase pura suavidade — o
    // oráculo já sabe a resposta e só está esperando a carta assentar.
    val decay = exp(-2.2f * u)
    return amp * envelope * envelope * decay * sin(2f * PI_F * plan.hesitationCycles * u)
}

/**
 * Excesso: passa do vencedor num pico suave e recua num arraste mais lento. As
 * duas metades usam suavizações de Hermite com derivada zero nas pontas, então
 * não há descontinuidade de velocidade na subida, no pico nem no pouso. Em
 * `t = 1` contribui exatamente 0.
 */
internal fun overshootOffset(plan: SpinMotionPlan, tRaw: Float): Float {
    val magnitude = plan.overshootSlots
    if (magnitude <= 0f) return 0f
    val t = tRaw.coerceIn(0f, 1f)
    val start = plan.overshootStart
    if (t <= start) return 0f
    if (t <= plan.overshootPeak) {
        val u = (t - start) / max(plan.overshootPeak - start, 1e-6f)
        return magnitude * smoothStep(u)
    }
    val u = (t - plan.overshootPeak) / max(1f - plan.overshootPeak, 1e-6f)
    return magnitude * (1f - smootherStep(u))
}

/**
 * Velocidade normalizada do avanço em `t` (0 = parado, 1 = cruzeiro pleno).
 * Diferença central numérica: barata e à prova de erro algébrico. Chamada só
 * nos cruzamentos de slot (~uma dezena de vezes por giro), nunca por frame.
 */
internal fun normalizedAdvanceSpeed(plan: SpinMotionPlan, tRaw: Float): Float {
    if (plan.distance <= 0f) return 0f
    val t = tRaw.coerceIn(0f, 1f)
    val h = 0.004f
    val lo = max(0f, t - h)
    val hi = min(1f, t + h)
    val span = hi - lo
    if (span <= 0f) return 0f
    val slope = (advanceFraction(hi) - advanceFraction(lo)) / span
    return (slope / ADVANCE_CRUISE_SLOPE).coerceIn(0f, 1f)
}

/**
 * Pitch do tick sonoro. No cruzeiro pleno o som fica agudo e apressado
 * ([TICK_PITCH_MAX]); na aproximação vacilante ele afina até o passo natural
 * ([TICK_PITCH_MIN]). É o som que conta a história: a pressa que vira dúvida.
 */
internal fun tickPitchForSpeed(normalizedSpeed: Float): Float {
    val s = normalizedSpeed.coerceIn(0f, 1f)
    return TICK_PITCH_MIN + (TICK_PITCH_MAX - TICK_PITCH_MIN) * s
}

/**
 * Micro-assentamento do vencedor: respiração amortecida em escala. Retorna 1.0
 * quando o envelope já morreu, então o multiplicador é neutro no repouso.
 *
 * Formato: `1 + amplitude · e^(-damping·t) · cos(2π·frequência·t)`. Com os
 * padrões daqui, três batidas visíveis em ~0,45 s.
 */
internal fun settleScale(elapsedSeconds: Float, amplitude: Float): Float {
    if (amplitude <= 0f) return 1f
    val t = elapsedSeconds
    if (t < 0f) return 1f
    val envelope = exp(-SETTLE_DAMPING_PER_SECOND * t)
    if (envelope * amplitude < SETTLE_EPSILON) return 1f
    return 1f + amplitude * envelope * cos(2f * PI_F * SETTLE_FREQUENCY_HZ * t)
}

/**
 * Mesmo amortecimento, aplicado ao deslocamento vertical da carta central — um
 * balanço de poucos pixels que arremata o pouso. Em frações do pitch do slot.
 */
internal fun settleBob(elapsedSeconds: Float, amplitude: Float): Float {
    if (amplitude <= 0f) return 0f
    val t = elapsedSeconds
    if (t < 0f) return 0f
    val envelope = exp(-SETTLE_DAMPING_PER_SECOND * t)
    if (envelope * amplitude < SETTLE_EPSILON) return 0f
    return amplitude * envelope * sin(2f * PI_F * SETTLE_FREQUENCY_HZ * t)
}

/** Duração prática do assentamento: até o envelope cair abaixo do limiar. */
internal fun settleDurationSeconds(amplitude: Float): Float {
    if (amplitude <= 0f) return 0f
    return (ln(amplitude / SETTLE_EPSILON) / SETTLE_DAMPING_PER_SECOND).toFloat()
        .coerceIn(0f, 1.5f)
}

private const val SETTLE_EPSILON = 0.003f
private const val PI_F = 3.14159265f

/** Suavização de Hermite 3u²-2u³ (derivada zero nas pontas). */
internal fun smoothStep(u: Float): Float {
    val x = u.coerceIn(0f, 1f)
    return x * x * (3f - 2f * x)
}

/** Suavização de Perlin 6u⁵-15u⁴+10u³ (derivada e curvatura zero nas pontas). */
internal fun smootherStep(u: Float): Float {
    val x = u.coerceIn(0f, 1f)
    return x * x * x * (x * (x * 6f - 15f) + 10f)
}