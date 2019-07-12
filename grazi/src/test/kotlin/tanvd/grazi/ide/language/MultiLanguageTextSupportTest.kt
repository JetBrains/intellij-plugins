package tanvd.grazi.ide.language

import org.junit.Test
import tanvd.grazi.GraziConfig
import tanvd.grazi.GraziTestBase
import tanvd.grazi.ide.msg.GraziAppLifecycle
import tanvd.grazi.language.Lang


class MultiLanguageTextSupportTest : GraziTestBase(true) {

    override fun setUp() {
        super.setUp()
        GraziConfig.state.enabledLanguages.add(Lang.RUSSIAN)
        GraziAppLifecycle.publisher.reInit()
    }

    override fun tearDown() {
        super.tearDown()
        GraziConfig.state.enabledLanguages.remove(Lang.RUSSIAN)
        GraziAppLifecycle.publisher.reInit()
    }

    @Test
    fun `test grammar check in file`() {
        // NOTE: lost one mistake in russian (к друг)
        runHighlightTestForFile("ide/language/markdown/ExampleMultiLang.md")
    }
}
