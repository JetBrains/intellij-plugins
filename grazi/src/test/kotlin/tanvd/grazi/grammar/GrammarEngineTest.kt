package tanvd.grazi.grammar

import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tanvd.grazi.GraziConfig
import tanvd.grazi.GraziPlugin
import tanvd.grazi.spellcheck.IdeaSpellchecker
import java.io.File
import kotlin.system.measureTimeMillis

class GrammarEngineTest {
    @BeforeEach
    fun prepare() {
        GraziPlugin.isTest = true
        GraziPlugin.invalidateCaches()
        GraziConfig.state.enabledSpellcheck = true

        IdeaSpellchecker.init { true }

        GraziPlugin.init()
    }

    @Test
    fun getFixes_emptyText_noTypos() {
        val fixes = GrammarEngine.getFixes("")
        assertIsEmpty(fixes)
    }

    @Test
    fun getFixes_correctText_noTypos() {
        val fixes = GrammarEngine.getFixes("Hello world")
        assertIsEmpty(fixes)
    }

    @Test
    fun getFixes_correctTextFewLines_noTypos() {
        val text = """
            |Hello world!
            |This is the start of a message.
            |The end is also here.
        """.trimMargin()
        val fixes = GrammarEngine.getFixes(text)
        assertIsEmpty(fixes)
    }


    @Test
    fun getFixes_oneLine_oneTypo() {
        val fixes = GrammarEngine.getFixes("hello world, my dear friend").toList()
        fixes.single().assertTypoIs(Typo.Category.CASING, IntRange(0, 4), listOf("Hello"))
    }

    @Test
    fun getFixes_typoOnAFirstLine_oneTypo() {
        val text = """
            |hello world!
            |This is the start of a message.
            |The end is also here world
        """.trimMargin()
        val fixes = GrammarEngine.getFixes(text)
        fixes.single().assertTypoIs(Typo.Category.CASING, IntRange(0, 4), listOf("Hello"))
    }

    @Test
    fun getFixes_typoOnALastLine_oneTypo() {
        val text = """
            |Hello world!
            |This is the start of a message.
            |The end is also here wrld
        """.trimMargin()
        val fixes = GrammarEngine.getFixes(text)
        fixes.single().assertTypoIs(Typo.Category.TYPOS, IntRange(66, 69), listOf("world"))
    }

    @Test
    fun getFixes_oneLine_fewTypos() {
        val fixes = GrammarEngine.getFixes("Hello. world,, tot he").toList()
        assertEquals(3, fixes.size)
        fixes[0].assertTypoIs(Typo.Category.CASING, IntRange(7, 11), listOf("World"))
        fixes[1].assertTypoIs(Typo.Category.PUNCTUATION, IntRange(12, 13), listOf(","))
        fixes[2].assertTypoIs(Typo.Category.TYPOS, IntRange(15, 20), listOf("to the"))
    }

    @Test
    fun getFixes_fewTypos_oneLine() {
        val text = """
            |Hello. world,, tot he.
            |This are my friend""".trimMargin()
        val fixes = GrammarEngine.getFixes(text).toList()
        assertEquals(4, fixes.size)
        fixes[0].assertTypoIs(Typo.Category.CASING, IntRange(7, 11), listOf("World"))
        fixes[1].assertTypoIs(Typo.Category.PUNCTUATION, IntRange(12, 13), listOf(","))
        fixes[2].assertTypoIs(Typo.Category.TYPOS, IntRange(15, 20), listOf("to the"))
        fixes[3].assertTypoIs(Typo.Category.GRAMMAR, IntRange(23, 30), listOf("This is"))
    }

    @Test
    fun getFixes_performance_middleSize() {
        val text = File("src/test/resources/sonnet_10.txt").readText()
        val grammar = GrammarEngine
        var fixes: List<Typo> = emptyList()
        val totalTime = measureTimeMillis {
            fixes = grammar.getFixes(text).toList()
        }
        assert(fixes.size > 100)
        assert(totalTime < 4000)
    }

    @Test
    fun getFixes_performance_cachedBigSize() {
        val text = File("src/test/resources/sonnet_50.txt").readText()
        var fixes: List<Typo> = emptyList()
        val totalTime = measureTimeMillis {
            fixes = GrammarEngine.getFixes(text).toList()
        }
        assert(fixes.size > 500)
        assert(totalTime < 10000)
    }
}
