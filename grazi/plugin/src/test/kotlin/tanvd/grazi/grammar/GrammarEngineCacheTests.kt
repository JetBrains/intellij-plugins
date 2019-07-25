package tanvd.grazi.grammar

import org.junit.Test
import tanvd.grazi.GraziTestBase
import tanvd.kex.Resources
import kotlin.system.measureTimeMillis

class GrammarEngineCacheTests : GraziTestBase(true) {
    @Test
    fun `test cache performance for 10 sonnets`() {
        val text = Resources.getText("grammar/sonnet_10.txt")
        val fixes1 = GrammarEngine.getTypos(text).toList()

        var fixes2: List<Typo> = emptyList()
        val totalTime = measureTimeMillis {
            fixes2 = GrammarEngine.getTypos(text).toList()
        }
        assertEquals(fixes1, fixes2)
        assert(totalTime < 500)
    }

    @Test
    fun `test cache performance for 50 sonnets`() {
        val text = Resources.getText("grammar/sonnet_50.txt")

        val fixes1 = GrammarEngine.getTypos(text).toList()
        var fixes2: List<Typo> = emptyList()
        val totalTime = measureTimeMillis {
            fixes2 = GrammarEngine.getTypos(text).toList()
        }
        assertEquals(fixes1, fixes2)
        assert(totalTime < 1000)
    }
}
