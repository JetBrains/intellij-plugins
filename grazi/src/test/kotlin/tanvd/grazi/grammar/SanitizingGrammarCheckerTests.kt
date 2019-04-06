package tanvd.grazi.grammar

import org.junit.Assert
import org.junit.jupiter.api.*
import tanvd.grazi.GraziConfig
import tanvd.grazi.GraziPlugin
import tanvd.grazi.spellcheck.IdeaSpellchecker
import java.io.File
import kotlin.system.measureTimeMillis


class SanitizingGrammarCheckerTests {
    @BeforeEach
    fun prepare() {
        GraziPlugin.isTest = true
        GraziPlugin.invalidateCaches()
        GraziConfig.state.enabledSpellcheck = true

        IdeaSpellchecker.init { true }

        GraziPlugin.init()
    }

    @Test
    fun check_emptyText_noTypos() {
        val token = plain("")
        val fixes = SanitizingGrammarChecker.default.check(token)
        assertIsEmpty(fixes)
    }

    @Test
    fun check_correctText_noTypos() {
        val token = plain("Hello world")
        val fixes = SanitizingGrammarChecker.default.check(token)
        assertIsEmpty(fixes)
    }

    @Test
    fun check_correctTextFewLines_noTypos() {
        val tokens = plain("Hello world!\n", "This is the start of a message.\n", "The end is also here.")
        val fixes = SanitizingGrammarChecker.default.check(tokens)
        assertIsEmpty(fixes)
    }


    @Test
    fun check_oneLine_oneTypo() {
        val text = "hello world, my dear friend"
        val tokens = plain(text).toList()
        val fixes = SanitizingGrammarChecker.default.check(tokens)
        fixes.single().assertTypoIs(Typo.Category.CASING, IntRange(0, 4), listOf("Hello"), text)
    }

    @Test
    fun check_typoOnAFirstLine_oneTypo() {
        val text = listOf("hello world!\n", "This is the start of a message.\n", "The end is also here world\n")
        val tokens = plain(text)
        val fixes = SanitizingGrammarChecker.default.check(tokens)
        fixes.single().assertTypoIs(Typo.Category.CASING, IntRange(0, 4), listOf("Hello"), text[0])
    }

    @Test
    fun check_typoOnALastLine_oneTypo() {
        val text = listOf("Hello world!\n", "This is the start of a message.\n", "The end is also here wrld\n")
        val tokens = plain(text)
        val fixes = SanitizingGrammarChecker.default.check(tokens)
        fixes.single().assertTypoIs(Typo.Category.TYPOS, IntRange(21, 24), listOf("world"), text[2])
    }

    @Test
    fun check_oneLine_fewTypos() {
        val text = "Hello. world,, tot he"
        val tokens = plain(text)
        val fixes = SanitizingGrammarChecker.default.check(tokens).toList()
        Assert.assertEquals(3, fixes.size)
        fixes[0].assertTypoIs(Typo.Category.CASING, IntRange(7, 11), listOf("World"), text)
        fixes[1].assertTypoIs(Typo.Category.PUNCTUATION, IntRange(12, 13), listOf(","), text)
        fixes[2].assertTypoIs(Typo.Category.TYPOS, IntRange(15, 20), listOf("to the"), text)
    }

    @Test
    fun check_fewLines_fewTypos() {
        val text = listOf("Hello. world,, tot he.\n", "This are my friend.")
        val tokens = plain(text)
        val fixes = SanitizingGrammarChecker.default.check(tokens).toList()
        Assert.assertEquals(4, fixes.size)
        fixes[0].assertTypoIs(Typo.Category.CASING, IntRange(7, 11), listOf("World"), text[0])
        fixes[1].assertTypoIs(Typo.Category.PUNCTUATION, IntRange(12, 13), listOf(","), text[0])
        fixes[2].assertTypoIs(Typo.Category.TYPOS, IntRange(15, 20), listOf("to the"), text[0])
        fixes[3].assertTypoIs(Typo.Category.GRAMMAR, IntRange(0, 7), listOf("This is"), text[1])
    }


    @Test
    fun check_javaDocFewLines_fewTypos() {
        val text = listOf("* Hello. world,, tot he.\n", "* * This is the next Javadoc string.\n", " * This are my friend.")
        val tokens = plain(text)
        val fixes = SanitizingGrammarChecker.default.check(tokens).toList()
        Assert.assertEquals(4, fixes.size)
        fixes[0].assertTypoIs(Typo.Category.CASING, IntRange(9, 13), listOf("World"), text[0])
        fixes[1].assertTypoIs(Typo.Category.PUNCTUATION, IntRange(14, 15), listOf(","), text[0])
        fixes[2].assertTypoIs(Typo.Category.TYPOS, IntRange(17, 22), listOf("to the"), text[0])
        fixes[3].assertTypoIs(Typo.Category.GRAMMAR, IntRange(3, 10), listOf("This is"), text[2])
    }

    @Test
    fun check_whiteSpacesFewLines_fewTypos() {
        val text = listOf("  Hello.    world,, tot    he.  \n  ", "     This   is the     next Javadoc string.   \n",
                "    This are my friend.    ")
        val tokens = plain(text)
        val fixes = SanitizingGrammarChecker.default.check(tokens).toList()
        Assert.assertEquals(4, fixes.size)
        fixes[0].assertTypoIs(Typo.Category.CASING, IntRange(12, 16), listOf("World"), text[0])
        fixes[1].assertTypoIs(Typo.Category.PUNCTUATION, IntRange(17, 18), listOf(","), text[0])
        fixes[2].assertTypoIs(Typo.Category.TYPOS, IntRange(20, 28), listOf("to the"), text[0])
        fixes[3].assertTypoIs(Typo.Category.GRAMMAR, IntRange(4, 11), listOf("This is"), text[2])
    }

    @Test
    fun check_performance_middleSize() {
        val text = File("src/test/resources/sonnet_10.txt").readText()
        val tokens = plain(text.split("\n").map { it + "\n" })
        var fixes: List<Typo> = emptyList()
        val totalTime = measureTimeMillis {
            fixes = SanitizingGrammarChecker.default.check(tokens).toList()
        }
        fixes.forEach { it.verify(text) }
        assert(fixes.size < 50)
        assert(totalTime < 10_000)
    }

    @Test
    fun check_performance_bigSize() {
        val text = File("src/test/resources/sonnet_50.txt").readText()
        val tokens = plain(text.split("\n").map { it + "\n" })
        var fixes: List<Typo> = emptyList()
        val totalTime = measureTimeMillis {
            fixes = SanitizingGrammarChecker.default.check(tokens).toList()
        }
        fixes.forEach { it.verify(text) }
        assert(fixes.size < 500)
        assert(totalTime < 20_000)
    }

    @Disabled
    @Test
    fun check_performance_veryBigSize() {
        val tokens = plain(File("src/test/resources/pride_and_prejudice.txt").readText().split("\n").map { it + "\n" })
        var fixes: List<Typo> = emptyList()
        val totalTime = measureTimeMillis {
            fixes = SanitizingGrammarChecker.default.check(tokens).toList()
        }
        fixes.forEach { it.verify() }
        assert(totalTime < 180_000)
    }
}
