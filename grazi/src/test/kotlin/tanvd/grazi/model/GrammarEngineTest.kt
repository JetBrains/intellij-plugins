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
    fun testInCorrectText() {
        val fixes = GrammarEngine.getFixes("A sentence with a error in the Hitchhiker's Guide tot he Galaxy")
        assertEquals(3, fixes.size)
    }
}