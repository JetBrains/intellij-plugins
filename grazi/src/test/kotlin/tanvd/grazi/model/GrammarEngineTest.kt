package tanvd.grazi.model

import org.junit.Assert.assertEquals
import org.junit.Test

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
    fun testCasing() {
        val fixes = GrammarEngine.getFixes("Hello. world,, the")
        assertEquals(2, fixes.size)
        assertEquals(TyposCategories.CASING, fixes[0].category)
        assertEquals(TyposCategories.PUNCTUATION, fixes[1].category)
    }

    @Test
    fun testInCorrectText() {
        val fixes = GrammarEngine.getFixes("A sentence with a error in the Hitchhiker's Guide tot he Galaxy")
        assertEquals(2, fixes.size)
    }
}