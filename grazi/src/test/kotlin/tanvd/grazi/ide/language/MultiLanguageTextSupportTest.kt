package tanvd.grazi.ide.language

import org.junit.Test
import tanvd.grazi.GraziConfig
import tanvd.grazi.GraziTestBase
import tanvd.grazi.language.Lang


class MultiLanguageTextSupportTest : GraziTestBase(true) {

    override fun setUp() {
        super.setUp()
        GraziConfig.update { it.copy(enabledLanguages = it.enabledLanguages + Lang.RUSSIAN) }
    }

    override fun tearDown() {
        super.tearDown()
        GraziConfig.update { it.copy(enabledLanguages = it.enabledLanguages - Lang.RUSSIAN) }
    }

    @Test
    fun `test grammar check in file`() {
        // NOTE: lost one mistake in russian (к друг)
        runHighlightTestForFile("ide/language/markdown/ExampleMultiLang.md")
    }
}
