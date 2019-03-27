package tanvd.grazi.model

import org.junit.Assert.assertEquals
import org.junit.Test
import tanvd.grazi.grammar.GrammarEngineService
import java.io.File
import kotlin.system.measureTimeMillis

class GrammarEngineServiceTest {
    @Test
    fun testCorrectText() {
        val fixes = GrammarEngineService().getFixes("Hello world")
        assertEquals(0, fixes.size)
    }

    @Test
    fun testFirstLetterText() {
        val fixes = GrammarEngineService().getFixes("hello world")
        assertEquals(1, fixes.size)
        assertEquals("Hello", fixes[0].fix!![0])
    }

    @Test
    fun testDifferentTypos() {
        val fixes = GrammarEngineService().getFixes("Hello. world,, tot he")
        assertEquals(3, fixes.size)
        assertEquals(Typo.Category.CASING, fixes[0].category)
        assertEquals(Typo.Category.PUNCTUATION, fixes[1].category)
        assertEquals(Typo.Category.TYPOS, fixes[2].category)
    }

    @Test
    fun testNotCorrectText() {
        val fixes = GrammarEngineService().getFixes("A sentence with a error in the Hitchhiker's Guide tot he Galaxy")
        assertEquals(2, fixes.size)
    }

    @Test
    fun testPerformanceMiddle() {
        val text = File("src/test/resources/english_big.txt").readText()
        val grammar = GrammarEngineService()
        var fixes = grammar.getFixes(text)
        val totalTime = measureTimeMillis {
            fixes = grammar.getFixes(text)
        }
        assert(0.01 > totalTime / 1000)
    }
}
