package tanvd.grazi.ide.language

import org.junit.Test
import tanvd.grazi.GraziConfig
import tanvd.grazi.GraziTestBase
import tanvd.grazi.ide.msg.GraziAppLifecycle
import tanvd.grazi.language.Lang


class MultiLanguageTextSupportTest : GraziTestBase(true) {

    override fun setUp() {
        super.setUp()
        val state = GraziConfig.get()
        GraziConfig.update(state.copy(enabledLanguages = state.enabledLanguages + Lang.RUSSIAN))
        GraziAppLifecycle.publisher.reInit()
    }

    override fun tearDown() {
        super.tearDown()
        val state = GraziConfig.get()
        GraziConfig.update(state.copy(enabledLanguages = state.enabledLanguages - Lang.RUSSIAN))
        GraziAppLifecycle.publisher.reInit()
    }

    @Test
    fun `test grammar check in file`() {
        // NOTE: lost one mistake in russian (к друг)
        runHighlightTestForFile("ide/language/markdown/ExampleMultiLang.md")
    }
}
