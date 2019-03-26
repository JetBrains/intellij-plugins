package tanvd.grazi.model

import org.junit.Assert.assertEquals
import org.junit.Test
import tanvd.grazi.grammar.GrammarEngine

class GrammarEngineTest {
    @Test
    fun testCorrectText() {
        val fixes = GrammarEngine.getFixes("Hello world")
        assertEquals(0, fixes.size)
    }

    @Test
    fun testFirstLetterText() {
        val fixes = GrammarEngine.getFixes("hello world")
        assertEquals(1, fixes.size)
        assertEquals("Hello", fixes[0].fix!![0])
    }

    @Test
    fun testDifferentTypos() {
        val fixes = GrammarEngine.getFixes("Hello. world,, tot he")
        assertEquals(3, fixes.size)
        assertEquals(Typo.Category.CASING, fixes[0].category)
        assertEquals(Typo.Category.PUNCTUATION, fixes[1].category)
        assertEquals(Typo.Category.TYPOS, fixes[2].category)
    }

    @Test
    fun testNotCorrectText() {
        val fixes = GrammarEngine.getFixes("A sentence with a error in the Hitchhiker's Guide tot he Galaxy")
        assertEquals(2, fixes.size)
    }
}
