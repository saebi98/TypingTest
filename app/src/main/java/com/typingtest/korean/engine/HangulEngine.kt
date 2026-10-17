package com.typingtest.korean.engine

import kotlin.math.max
import kotlin.math.roundToInt

enum class CharMatchStatus {
    MATCHED,    // 정확히 일치
    COMPOSING,  // 한글 조합 중 (정상 입력 경로)
    MISMATCH,   // 오타
    PENDING     // 아직 입력 전
}

data class TypingEvaluation(
    val charStatuses: List<CharMatchStatus>,
    val matchedStrokes: Int,
    val totalTypedStrokes: Int,
    val totalTargetStrokes: Int,
    val errorCharCount: Int,
    val accuracyPercent: Float,
    val cpm: Int,
    val nextRequiredJamo: Char?,
    val requiresShift: Boolean,
    val isCompleted: Boolean
)

object HangulEngine {

    private val CHOSEONG = charArrayOf(
        'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ',
        'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    )

    private val JUNGSEONG = charArrayOf(
        'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ',
        'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'
    )

    private val JONGSEONG = charArrayOf(
        '\u0000', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ',
        'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ',
        'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    )

    private val COMPOUND_VOWELS = mapOf(
        'ㅘ' to charArrayOf('ㅗ', 'ㅏ'),
        'ㅙ' to charArrayOf('ㅗ', 'ㅐ'),
        'ㅚ' to charArrayOf('ㅗ', 'ㅣ'),
        'ㅝ' to charArrayOf('ㅜ', 'ㅓ'),
        'ㅞ' to charArrayOf('ㅜ', 'ㅔ'),
        'ㅟ' to charArrayOf('ㅜ', 'ㅣ'),
        'ㅢ' to charArrayOf('ㅡ', 'ㅣ')
    )

    private val REVERSE_COMPOUND_VOWELS = mapOf(
        Pair('ㅗ', 'ㅏ') to 'ㅘ',
        Pair('ㅗ', 'ㅐ') to 'ㅙ',
        Pair('ㅗ', 'ㅣ') to 'ㅚ',
        Pair('ㅜ', 'ㅓ') to 'ㅝ',
        Pair('ㅜ', 'ㅔ') to 'ㅞ',
        Pair('ㅜ', 'ㅣ') to 'ㅟ',
        Pair('ㅡ', 'ㅣ') to 'ㅢ'
    )

    private val COMPOUND_JONGSEONG = mapOf(
        'ㄳ' to charArrayOf('ㄱ', 'ㅅ'),
        'ㄵ' to charArrayOf('ㄴ', 'ㅈ'),
        'ㄶ' to charArrayOf('ㄴ', 'ㅎ'),
        'ㄺ' to charArrayOf('ㄹ', 'ㄱ'),
        'ㄻ' to charArrayOf('ㄹ', 'ㅁ'),
        'ㄼ' to charArrayOf('ㄹ', 'ㅂ'),
        'ㄽ' to charArrayOf('ㄹ', 'ㅅ'),
        'ㄾ' to charArrayOf('ㄹ', 'ㅌ'),
        'ㄿ' to charArrayOf('ㄹ', 'ㅍ'),
        'ㅀ' to charArrayOf('ㄹ', 'ㅎ'),
        'ㅄ' to charArrayOf('ㅂ', 'ㅅ')
    )

    private val REVERSE_COMPOUND_JONGSEONG = mapOf(
        Pair('ㄱ', 'ㅅ') to 'ㄳ',
        Pair('ㄴ', 'ㅈ') to 'ㄵ',
        Pair('ㄴ', 'ㅎ') to 'ㄶ',
        Pair('ㄹ', 'ㄱ') to 'ㄺ',
        Pair('ㄹ', 'ㅁ') to 'ㄻ',
        Pair('ㄹ', 'ㅂ') to 'ㄼ',
        Pair('ㄹ', 'ㅅ') to 'ㄽ',
        Pair('ㄹ', 'ㅌ') to 'ㄾ',
        Pair('ㄹ', 'ㅍ') to 'ㄿ',
        Pair('ㄹ', 'ㅎ') to 'ㅀ',
        Pair('ㅂ', 'ㅅ') to 'ㅄ'
    )

    private val SHIFT_JAMO_MAP = mapOf(
        'ㄲ' to 'ㄱ',
        'ㄸ' to 'ㄷ',
        'ㅃ' to 'ㅂ',
        'ㅆ' to 'ㅅ',
        'ㅉ' to 'ㅈ',
        'ㅒ' to 'ㅐ',
        'ㅖ' to 'ㅔ'
    )

    /**
     * 단일 문자를 타수(키 입력) 단위의 자모 리스트로 분해합니다.
     * 예: '한' -> ['ㅎ', 'ㅏ', 'ㄴ'], '과' -> ['ㄱ', 'ㅗ', 'ㅏ'], '닭' -> ['ㄷ', 'ㅏ', 'ㄹ', 'ㄱ']
     */
    fun decomposeCharToStrokes(ch: Char): List<Char> {
        if (ch.code in 0xAC00..0xD7A3) {
            val syllableIndex = ch.code - 0xAC00
            val choIdx = syllableIndex / (21 * 28)
            val jungIdx = (syllableIndex % (21 * 28)) / 28
            val jongIdx = syllableIndex % 28

            val result = mutableListOf<Char>()
            result.add(CHOSEONG[choIdx])

            val jung = JUNGSEONG[jungIdx]
            COMPOUND_VOWELS[jung]?.let { result.addAll(it.toList()) } ?: result.add(jung)

            if (jongIdx != 0) {
                val jong = JONGSEONG[jongIdx]
                COMPOUND_JONGSEONG[jong]?.let { result.addAll(it.toList()) } ?: result.add(jong)
            }
            return result
        }
        COMPOUND_VOWELS[ch]?.let { return it.toList() }
        COMPOUND_JONGSEONG[ch]?.let { return it.toList() }
        return listOf(ch)
    }

    /**
     * 문자열 전체를 키 입력 타수(Stroke) 리스트로 분해합니다.
     */
    fun decomposeToStrokes(text: String): List<Char> {
        val list = mutableListOf<Char>()
        for (ch in text) {
            list.addAll(decomposeCharToStrokes(ch))
        }
        return list
    }

    /**
     * 문자열의 총 타수를 계산합니다.
     */
    fun countStrokes(text: String): Int = decomposeToStrokes(text).size

    /**
     * 목표 텍스트(target)와 현재 사용자 입력(input), 경과 시간(elapsedMillis)을 바탕으로
     * 글자별 상태(일치/조합중/오타/대기), 분당 타수(CPM), 정확도, 다음 눌러야 할 자모를 평가합니다.
     */
    fun evaluate(
        target: String,
        input: String,
        elapsedMillis: Long,
        previouslyCompletedStrokes: Int = 0,
        previousTotalTypedStrokes: Int = 0
    ): TypingEvaluation {
        val statuses = MutableList(target.length) { CharMatchStatus.PENDING }
        var matchedStrokesInCurrent = 0
        var typedStrokesInCurrent = 0
        var errorChars = 0
        var nextJamo: Char? = null

        for (i in target.indices) {
            val targetChar = target[i]
            val targetStrokes = decomposeCharToStrokes(targetChar)

            if (i < input.length) {
                val inputChar = input[i]
                val inputStrokes = decomposeCharToStrokes(inputChar)
                typedStrokesInCurrent += inputStrokes.size

                if (inputChar == targetChar) {
                    statuses[i] = CharMatchStatus.MATCHED
                    matchedStrokesInCurrent += targetStrokes.size
                } else {
                    val isLastInputChar = (i == input.length - 1)
                    if (isLastInputChar && isComposingPrefix(target, i, inputStrokes)) {
                        statuses[i] = CharMatchStatus.COMPOSING
                        val commonPrefixCount = countCommonPrefix(
                            getExtendedTargetStrokes(target, i),
                            inputStrokes
                        )
                        matchedStrokesInCurrent += commonPrefixCount
                        val extended = getExtendedTargetStrokes(target, i)
                        if (commonPrefixCount < extended.size) {
                            nextJamo = extended[commonPrefixCount]
                        }
                    } else {
                        statuses[i] = CharMatchStatus.MISMATCH
                        errorChars++
                        val commonPrefixCount = countCommonPrefix(targetStrokes, inputStrokes)
                        matchedStrokesInCurrent += commonPrefixCount
                        if (nextJamo == null) {
                            nextJamo = targetStrokes.firstOrNull()
                        }
                    }
                }
            } else {
                statuses[i] = CharMatchStatus.PENDING
                if (nextJamo == null) {
                    nextJamo = targetStrokes.firstOrNull()
                }
            }
        }

        val totalMatchedStrokes = previouslyCompletedStrokes + matchedStrokesInCurrent
        val totalTyped = previousTotalTypedStrokes + max(typedStrokesInCurrent, matchedStrokesInCurrent)
        val totalTargetStrokes = countStrokes(target)

        val accuracy = if (totalTyped == 0) {
            100f
        } else {
            ((totalMatchedStrokes.toFloat() / totalTyped.toFloat()) * 100f).coerceIn(0f, 100f)
        }

        val cpm = if (elapsedMillis < 300L || totalMatchedStrokes == 0) {
            0
        } else {
            ((totalMatchedStrokes * 60_000.0) / elapsedMillis.toDouble()).roundToInt().coerceAtMost(2000)
        }

        val requiresShift = nextJamo != null && (SHIFT_JAMO_MAP.containsKey(nextJamo) || nextJamo in 'A'..'Z')
        val isCompleted = input == target && target.isNotEmpty()

        return TypingEvaluation(
            charStatuses = statuses,
            matchedStrokes = matchedStrokesInCurrent,
            totalTypedStrokes = typedStrokesInCurrent,
            totalTargetStrokes = totalTargetStrokes,
            errorCharCount = errorChars,
            accuracyPercent = accuracy,
            cpm = cpm,
            nextRequiredJamo = nextJamo,
            requiresShift = requiresShift,
            isCompleted = isCompleted
        )
    }

    /**
     * Shift가 필요한 쌍자음/이중모음 또는 영문 대문자인 경우 기본 키 문자를 반환합니다.
     * 예: 'ㄲ' -> 'ㄱ', 'A' -> 'a'
     */
    fun getBaseKeyForJamo(jamo: Char): Char {
        if (jamo in 'A'..'Z') return jamo.lowercaseChar()
        return SHIFT_JAMO_MAP[jamo] ?: jamo
    }

    /**
     * 한글 IME 특성상 '바다'를 칠 때 '받'을 거쳐 '바다'가 되는 도깨비불(연음 조합) 현상까지
     * 정상 조합 중(Composing)으로 인정합니다.
     */
    private fun isComposingPrefix(target: String, index: Int, inputStrokes: List<Char>): Boolean {
        val extendedStrokes = getExtendedTargetStrokes(target, index)
        if (inputStrokes.isEmpty() || inputStrokes.size > extendedStrokes.size) return false
        for (k in inputStrokes.indices) {
            if (inputStrokes[k] != extendedStrokes[k]) return false
        }
        return true
    }

    private fun getExtendedTargetStrokes(target: String, index: Int): List<Char> {
        val currentStrokes = decomposeCharToStrokes(target[index]).toMutableList()
        if (index + 1 < target.length) {
            val nextStrokes = decomposeCharToStrokes(target[index + 1])
            currentStrokes.addAll(nextStrokes)
        }
        return currentStrokes
    }

    private fun countCommonPrefix(a: List<Char>, b: List<Char>): Int {
        val limit = minOf(a.size, b.size)
        var count = 0
        for (i in 0 until limit) {
            if (a[i] == b[i]) count++ else break
        }
        return count
    }

    // =========================================================================
    // 내장 두벌식 가상 키보드용 한글 오토마타 (자모 추가 & 백스페이스 조합 해제)
    // =========================================================================

    fun isConsonant(ch: Char): Boolean = CHOSEONG.contains(ch)
    fun isVowel(ch: Char): Boolean = JUNGSEONG.contains(ch)

    /**
     * 현재 문자열 끝에 두벌식 자모 1개를 입력하여 한글 오토마타 규칙대로 합성합니다.
     */
    fun appendJamo(currentText: String, jamo: Char): String {
        if (currentText.isEmpty()) return jamo.toString()

        val lastChar = currentText.last()
        val prefix = currentText.dropLast(1)

        // 1. 마지막 글자가 단독 자음(초성 후보)이고 새로 들어온 글자가 모음인 경우 -> 음절 생성
        if (isConsonant(lastChar) && isVowel(jamo)) {
            val choIdx = CHOSEONG.indexOf(lastChar)
            val jungIdx = JUNGSEONG.indexOf(jamo)
            if (choIdx >= 0 && jungIdx >= 0) {
                return prefix + composeSyllable(choIdx, jungIdx, 0)
            }
        }

        // 2. 마지막 글자가 단독 모음이고 새로 들어온 모음과 복모음 조합이 가능한 경우 (예: ㅗ + ㅏ = ㅘ)
        if (isVowel(lastChar) && isVowel(jamo)) {
            val combinedVowel = REVERSE_COMPOUND_VOWELS[Pair(lastChar, jamo)]
            if (combinedVowel != null) {
                return prefix + combinedVowel
            }
        }

        // 3. 마지막 글자가 완성형 음절(가~힣)인 경우
        if (lastChar.code in 0xAC00..0xD7A3) {
            val syllableIndex = lastChar.code - 0xAC00
            val choIdx = syllableIndex / (21 * 28)
            val jungIdx = (syllableIndex % (21 * 28)) / 28
            val jongIdx = syllableIndex % 28

            if (jongIdx == 0) {
                // 받침이 없는 상태에서 모음이 들어오면 복모음 결합 시도
                if (isVowel(jamo)) {
                    val currentJung = JUNGSEONG[jungIdx]
                    val combinedVowel = REVERSE_COMPOUND_VOWELS[Pair(currentJung, jamo)]
                    if (combinedVowel != null) {
                        val newJungIdx = JUNGSEONG.indexOf(combinedVowel)
                        return prefix + composeSyllable(choIdx, newJungIdx, 0)
                    }
                }
                // 받침이 없는 상태에서 자음이 들어오면 종성(받침)으로 결합 시도
                if (isConsonant(jamo)) {
                    val newJongIdx = JONGSEONG.indexOf(jamo)
                    if (newJongIdx > 0) {
                        return prefix + composeSyllable(choIdx, jungIdx, newJongIdx)
                    }
                }
            } else {
                // 이미 받침이 있는 상태에서 자음이 들어오면 겹받침 결합 시도 (예: 닭 = 달 + ㄱ)
                val currentJong = JONGSEONG[jongIdx]
                if (isConsonant(jamo)) {
                    val combinedJong = REVERSE_COMPOUND_JONGSEONG[Pair(currentJong, jamo)]
                    if (combinedJong != null) {
                        val newJongIdx = JONGSEONG.indexOf(combinedJong)
                        if (newJongIdx > 0) {
                            return prefix + composeSyllable(choIdx, jungIdx, newJongIdx)
                        }
                    }
                }
                // 받침이 있는 상태에서 모음이 들어오면 받침(또는 겹받침의 뒷자음)을 다음 음절 초성으로 분리 (도깨비불 연음)
                if (isVowel(jamo)) {
                    val newJungIdx = JUNGSEONG.indexOf(jamo)
                    val compoundSplit = COMPOUND_JONGSEONG[currentJong]
                    if (compoundSplit != null) {
                        val remainJongIdx = JONGSEONG.indexOf(compoundSplit[0])
                        val nextChoIdx = CHOSEONG.indexOf(compoundSplit[1])
                        if (remainJongIdx > 0 && nextChoIdx >= 0 && newJungIdx >= 0) {
                            val prevSyllable = composeSyllable(choIdx, jungIdx, remainJongIdx)
                            val nextSyllable = composeSyllable(nextChoIdx, newJungIdx, 0)
                            return prefix + prevSyllable + nextSyllable
                        }
                    } else {
                        val nextChoIdx = CHOSEONG.indexOf(currentJong)
                        if (nextChoIdx >= 0 && newJungIdx >= 0) {
                            val prevSyllable = composeSyllable(choIdx, jungIdx, 0)
                            val nextSyllable = composeSyllable(nextChoIdx, newJungIdx, 0)
                            return prefix + prevSyllable + nextSyllable
                        }
                    }
                }
            }
        }

        return currentText + jamo
    }

    /**
     * 백스페이스 입력 시 마지막 자모 1타 단위로 역분해합니다.
     */
    fun backspaceJamo(currentText: String): String {
        if (currentText.isEmpty()) return ""
        val lastChar = currentText.last()
        val prefix = currentText.dropLast(1)

        if (lastChar.code in 0xAC00..0xD7A3) {
            val syllableIndex = lastChar.code - 0xAC00
            val choIdx = syllableIndex / (21 * 28)
            val jungIdx = (syllableIndex % (21 * 28)) / 28
            val jongIdx = syllableIndex % 28

            if (jongIdx != 0) {
                val currentJong = JONGSEONG[jongIdx]
                val compoundSplit = COMPOUND_JONGSEONG[currentJong]
                return if (compoundSplit != null) {
                    val remainJongIdx = JONGSEONG.indexOf(compoundSplit[0])
                    prefix + composeSyllable(choIdx, jungIdx, remainJongIdx)
                } else {
                    prefix + composeSyllable(choIdx, jungIdx, 0)
                }
            }

            val currentJung = JUNGSEONG[jungIdx]
            val vowelSplit = COMPOUND_VOWELS[currentJung]
            if (vowelSplit != null) {
                val remainJungIdx = JUNGSEONG.indexOf(vowelSplit[0])
                return prefix + composeSyllable(choIdx, remainJungIdx, 0)
            }

            return prefix + CHOSEONG[choIdx]
        }

        val vowelSplit = COMPOUND_VOWELS[lastChar]
        if (vowelSplit != null) {
            return prefix + vowelSplit[0]
        }

        return prefix
    }

    private fun composeSyllable(choIdx: Int, jungIdx: Int, jongIdx: Int): Char {
        val code = 0xAC00 + (choIdx * 21 * 28) + (jungIdx * 28) + jongIdx
        return code.toChar()
    }
}
