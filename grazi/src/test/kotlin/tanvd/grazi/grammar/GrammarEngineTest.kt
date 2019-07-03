package tanvd.grazi.grammar

import org.junit.Test
import tanvd.grazi.GraziTestBase
import tanvd.kex.Resources
import kotlin.system.measureTimeMillis

class GrammarEngineTest : GraziTestBase(true) {

    @Test
    fun `test empty text`() {
        val fixes = GrammarEngine.getTypos("")
        assertIsEmpty(fixes)
    }

    @Test
    fun `test correct text`() {
        val fixes = GrammarEngine.getTypos("Hello world")
        assertIsEmpty(fixes)
    }

    @Test
    fun `test correct few lines text`() {
        val text = """
            |Hello world!
            |This is the start of a message.
            |The end is also here.
        """.trimMargin()
        val fixes = GrammarEngine.getTypos(text)
        assertIsEmpty(fixes)
    }


    @Test
    fun `test one line text with typo`() {
        val text = "Tot he world, my dear friend"
        val fixes = GrammarEngine.getTypos(text).toList()
        fixes.single().assertTypoIs(Typo.Category.TYPOS, IntRange(0, 5), listOf("To the"), text)
    }

    @Test
    fun `test few lines text with typo on first line`() {
        val text = """
            |Tot he world!
            |This is the start of a message.
            |The end is also here world.
        """.trimMargin()
        val fixes = GrammarEngine.getTypos(text)
        fixes.single().assertTypoIs(Typo.Category.TYPOS, IntRange(0, 5), listOf("To the"), text)
    }

    @Test
    fun `test few lines text with typo on last line`() {
        val text = """
            |Hello world!
            |This is the start of a message.
            |It is a the friend.
        """.trimMargin()
        val fixes = GrammarEngine.getTypos(text)
        fixes.single().assertTypoIs(Typo.Category.GRAMMAR, IntRange(51, 55), listOf("a", "the"), text)
    }

    @Test
    fun `test one line text with few typos`() {
        val text = "Hello. world,, tot he"
        val fixes = GrammarEngine.getTypos(text).toList()
        assertEquals(2, fixes.size)
        fixes[0].assertTypoIs(Typo.Category.PUNCTUATION, IntRange(12, 13), listOf(","), text)
        fixes[1].assertTypoIs(Typo.Category.TYPOS, IntRange(15, 20), listOf("to the"), text)
    }

    @Test
    fun `test few lines text with few typos`() {
        val text = """
            |Hello. world,, tot he.
            |This are my friend.""".trimMargin()
        val fixes = GrammarEngine.getTypos(text).toList()
        assertEquals(3, fixes.size)
        fixes[0].assertTypoIs(Typo.Category.PUNCTUATION, IntRange(12, 13), listOf(","), text)
        fixes[1].assertTypoIs(Typo.Category.TYPOS, IntRange(15, 20), listOf("to the"), text)
        fixes[2].assertTypoIs(Typo.Category.GRAMMAR, IntRange(23, 30), listOf("This is"), text)
    }

    @Test
    fun `test performance with 10 sonnets`() {
        val text = Resources.getText("grammar/sonnet_10.txt")
        var fixes: List<Typo> = emptyList()
        val totalTime = measureTimeMillis {
            fixes = GrammarEngine.getTypos(text).toList()
        }
        fixes.forEach { it.verify(text) }
        assert(fixes.size > 30)
        assert(totalTime < 10_000)
    }

    @Test
    fun `test performance with 50 sonnets`() {
        val text = Resources.getText("grammar/sonnet_50.txt")
        var fixes: List<Typo> = emptyList()
        val totalTime = measureTimeMillis {
            fixes = GrammarEngine.getTypos(text).toList()
        }
        fixes.forEach { it.verify(text) }
        assert(fixes.size > 200)
        assert(totalTime < 20_000)
    }
}
