package tanvd.grazi.grammar

import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tanvd.grazi.GraziConfig
import tanvd.grazi.GraziPlugin
import tanvd.grazi.spellcheck.IdeaSpellchecker
import java.io.File
import kotlin.system.measureTimeMillis

class GrammarEngineCacheTests {
    @BeforeEach
    fun prepare() {
        GraziPlugin.isTest = true
        GraziPlugin.invalidateCaches()
        GraziConfig.state.enabledSpellcheck = true

        IdeaSpellchecker.init { true }

        GraziPlugin.init()
    }

    @Test
    fun getFixes_performance_cachedMiddleSize() {
        val text = File("src/test/resources/sonnet_10.txt").readText()
        val fixes1 = GrammarEngine.getFixes(text).toList()

        var fixes2: List<Typo> = emptyList()
        val totalTime = measureTimeMillis {
            fixes2 = GrammarEngine.getFixes(text).toList()
        }
        Assert.assertEquals(fixes1, fixes2)
        assert(totalTime < 100)
    }

    @Test
    fun getFixes_performance_cachedBigSize() {
        val text = File("src/test/resources/sonnet_50.txt").readText()

        val fixes1 = GrammarEngine.getFixes(text).toList()
        var fixes2: List<Typo> = emptyList()
        val totalTime = measureTimeMillis {
            fixes2 = GrammarEngine.getFixes(text).toList()
        }
        Assert.assertEquals(fixes1, fixes2)
        assert(totalTime < 200)
    }
}
