package tanvd.grazi.grammar

import org.junit.Assert
import org.junit.Test
import tanvd.grazi.GraziTestBase
import tanvd.kex.Resources
import kotlin.system.measureTimeMillis


class GrammarCheckerTests : GraziTestBase(true) {

    @Test
    fun `test empty text`() {
        val token = plain("")
        val fixes = GrammarChecker.default.check(token)
        assertIsEmpty(fixes)
    }

    @Test
    fun `test correct text`() {
        val token = plain("Hello world")
        val fixes = GrammarChecker.default.check(token)
        assertIsEmpty(fixes)
    }

    @Test
    fun `test few lines of correct text`() {
        val tokens = plain("Hello world!\n", "This is the start of a message.\n", "The end is also here.")
        val fixes = GrammarChecker.default.check(tokens)
        assertIsEmpty(fixes)
    }


    @Test
    fun `test one line of text with one typo`() {
        val text = "Tot he world, my dear friend"
        val tokens = plain(text).toList()
        val fixes = GrammarChecker.default.check(tokens)
        fixes.single().assertTypoIs(Typo.Category.TYPOS, IntRange(0, 5), listOf("To the"), text)
    }

    @Test
    fun `test few lines of text with typo on first line`() {
        val text = listOf("Tot he world!\n", "This is the start of a message.\n", "The end is also here world\n")
        val tokens = plain(text)
        val fixes = GrammarChecker.default.check(tokens)
        fixes.single().assertTypoIs(Typo.Category.TYPOS, IntRange(0, 5), listOf("To the"), text[0])
    }

    @Test
    fun `test few lines of text with typo on last line`() {
        val text = listOf("Hello world!\n", "This is the start of a message.\n", "It is a the friend\n")
        val tokens = plain(text)
        val fixes = GrammarChecker.default.check(tokens)
        fixes.single().assertTypoIs(Typo.Category.GRAMMAR, IntRange(6, 10), listOf("a", "the"), text[2])
    }

    @Test
    fun `test one line of text with multiple typos`() {
        val text = "Hello. world,, tot he. text for english."
        val tokens = plain(text)
        val fixes = GrammarChecker.default.check(tokens).toList()
        Assert.assertEquals(2, fixes.size)
        fixes[0].assertTypoIs(Typo.Category.PUNCTUATION, IntRange(12, 13), listOf(","), text)
        fixes[1].assertTypoIs(Typo.Category.TYPOS, IntRange(15, 20), listOf("to the"), text)
    }

    @Test
    fun `test few lines of text with few typos`() {
        val text = listOf("Hello. world,, tot he.\n", "This are my friend.")
        val tokens = plain(text)
        val fixes = GrammarChecker.default.check(tokens).toList()
        Assert.assertEquals(3, fixes.size)
        fixes[0].assertTypoIs(Typo.Category.PUNCTUATION, IntRange(12, 13), listOf(","), text[0])
        fixes[1].assertTypoIs(Typo.Category.TYPOS, IntRange(15, 20), listOf("to the"), text[0])
        fixes[2].assertTypoIs(Typo.Category.GRAMMAR, IntRange(0, 7), listOf("This is"), text[1])
    }


    @Test
    fun `test javadoc-like text with few typos`() {
        val text = listOf("* Hello. world,, tot he.\n", "* * This is the next Javadoc string.\n", " * This are my friend.")
        val tokens = plain(text)
        val fixes = GrammarChecker.default.check(tokens).toList()
        Assert.assertEquals(3, fixes.size)
        fixes[0].assertTypoIs(Typo.Category.PUNCTUATION, IntRange(14, 15), listOf(","), text[0])
        fixes[1].assertTypoIs(Typo.Category.TYPOS, IntRange(17, 22), listOf("to the"), text[0])
        fixes[2].assertTypoIs(Typo.Category.GRAMMAR, IntRange(3, 10), listOf("This is"), text[2])
    }

    @Test
    fun `test pretty formatted text with few typos`() {
        val text = listOf("  Hello.    world,, tot    he.  \n  ", "     This   is the     next Javadoc string.   \n",
                "    This are my friend.    ")
        val tokens = plain(text)
        val fixes = GrammarChecker.default.check(tokens).toList()
        Assert.assertEquals(3, fixes.size)
        fixes[0].assertTypoIs(Typo.Category.PUNCTUATION, IntRange(17, 18), listOf(","), text[0])
        fixes[1].assertTypoIs(Typo.Category.TYPOS, IntRange(20, 28), listOf("to the"), text[0])
        fixes[2].assertTypoIs(Typo.Category.GRAMMAR, IntRange(4, 11), listOf("This is"), text[2])
    }

    @Test
    fun `test performance for 10 sonnets`() {
        val text = Resources.getText("grammar/sonnet_10.txt")
        val tokens = plain(text.split("\n").map { it + "\n" })
        var fixes: List<Typo> = emptyList()
        val totalTime = measureTimeMillis {
            fixes = GrammarChecker.default.check(tokens).toList()
        }
        fixes.forEach { it.verify(text) }
        assert(fixes.size < 50)
        assert(totalTime < 10_000)
    }

    @Test
    fun `test performance for 50 sonnets`() {
        val text = Resources.getText("grammar/sonnet_50.txt")
        val tokens = plain(text.split("\n").map { it + "\n" })
        var fixes: List<Typo> = emptyList()
        val totalTime = measureTimeMillis {
            fixes = GrammarChecker.default.check(tokens).toList()
        }
        fixes.forEach { it.verify(text) }
        assert(fixes.size < 500)
        assert(totalTime < 100_000)
    }
}
