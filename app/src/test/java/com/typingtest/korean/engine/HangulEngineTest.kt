package com.typingtest.korean.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HangulEngineTest {

    @Test
    fun testDecomposeCharToStrokes() {
        assertEquals(listOf('ㅎ', 'ㅏ', 'ㄴ'), HangulEngine.decomposeCharToStrokes('한'))
        assertEquals(listOf('ㄱ', 'ㅗ', 'ㅏ'), HangulEngine.decomposeCharToStrokes('과'))
        assertEquals(listOf('ㄷ', 'ㅏ', 'ㄹ', 'ㄱ'), HangulEngine.decomposeCharToStrokes('닭'))
        assertEquals(6, HangulEngine.countStrokes("한글"))
    }

    @Test
    fun testImeComposingAndDokkaebibulPhenomenon() {
        // '한글'을 입력할 때 '하'까지 입력한 상태는 오타가 아니라 COMPOSING 이어야 함
        val eval1 = HangulEngine.evaluate(
            target = "한글",
            input = "하",
            elapsedMillis = 2000L
        )
        assertEquals(CharMatchStatus.COMPOSING, eval1.charStatuses[0])
        assertEquals(CharMatchStatus.PENDING, eval1.charStatuses[1])
        assertEquals('ㄴ', eval1.nextRequiredJamo)
        assertEquals(0, eval1.errorCharCount)

        // '바다'를 입력할 때 'ㅂ','ㅏ','ㄷ' 입력 시 한글 IME가 일시적으로 '받'을 만드는 현상(도깨비불)도 COMPOSING 인정
        val eval2 = HangulEngine.evaluate(
            target = "바다",
            input = "받",
            elapsedMillis = 2000L
        )
        assertEquals(CharMatchStatus.COMPOSING, eval2.charStatuses[0])
        assertEquals('ㅏ', eval2.nextRequiredJamo)
        assertEquals(0, eval2.errorCharCount)

        // 실제 오타('호')인 경우 MISMATCH 판정
        val eval3 = HangulEngine.evaluate(
            target = "한글",
            input = "호",
            elapsedMillis = 2000L
        )
        assertEquals(CharMatchStatus.MISMATCH, eval3.charStatuses[0])
        assertEquals(1, eval3.errorCharCount)
    }

    @Test
    fun testDubeolsikAutomatonAppendAndBackspace() {
        var text = ""
        val strokes = listOf('ㅎ', 'ㅏ', 'ㄴ', 'ㄱ', 'ㅡ', 'ㄹ')
        for (jamo in strokes) {
            text = HangulEngine.appendJamo(text, jamo)
        }
        assertEquals("한글", text)

        // 겹받침 및 도깨비불 연음 테스트: 'ㄷ','ㅏ','ㄹ','ㄱ' -> "닭", 이어서 'ㅏ' -> "달가"
        var word = ""
        for (jamo in listOf('ㄷ', 'ㅏ', 'ㄹ', 'ㄱ')) {
            word = HangulEngine.appendJamo(word, jamo)
        }
        assertEquals("닭", word)
        word = HangulEngine.appendJamo(word, 'ㅏ')
        assertEquals("달가", word)

        // 백스페이스 역분해 테스트: "닭" -> "달" -> "다" -> "ㄷ" -> ""
        var back = "닭"
        back = HangulEngine.backspaceJamo(back)
        assertEquals("달", back)
        back = HangulEngine.backspaceJamo(back)
        assertEquals("다", back)
        back = HangulEngine.backspaceJamo(back)
        assertEquals("ㄷ", back)
        back = HangulEngine.backspaceJamo(back)
        assertEquals("", back)
    }

    @Test
    fun testCompletedEvaluation() {
        val eval = HangulEngine.evaluate(
            target = "대한민국",
            input = "대한민국",
            elapsedMillis = 3000L
        )
        assertTrue(eval.isCompleted)
        assertFalse(eval.charStatuses.any { it != CharMatchStatus.MATCHED })
        assertEquals(100f, eval.accuracyPercent, 0.01f)
    }

    @Test
    fun testEnglishEvaluationAndShift() {
        val eval = HangulEngine.evaluate(
            target = "Korea",
            input = "",
            elapsedMillis = 1000L
        )
        assertEquals('K', eval.nextRequiredJamo)
        assertTrue(eval.requiresShift)
        assertEquals('k', HangulEngine.getBaseKeyForJamo('K'))

        val eval2 = HangulEngine.evaluate(
            target = "Korea",
            input = "Ko",
            elapsedMillis = 1000L
        )
        assertEquals(CharMatchStatus.MATCHED, eval2.charStatuses[0])
        assertEquals(CharMatchStatus.MATCHED, eval2.charStatuses[1])
        assertEquals('r', eval2.nextRequiredJamo)
        assertFalse(eval2.requiresShift)
    }
}
