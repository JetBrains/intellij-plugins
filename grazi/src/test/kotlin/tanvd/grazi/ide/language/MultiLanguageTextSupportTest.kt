package tanvd.grazi.ide.language

import tanvd.grazi.GraziConfig
import tanvd.grazi.GraziTestBase
import tanvd.grazi.ide.GraziLifecycle
import tanvd.grazi.language.Lang


class MultiLanguageTextSupportTest : GraziTestBase(true) {
    fun `test grammar check in file`() {
        val isRussianEnabled = GraziConfig.state.enabledLanguages.contains(Lang.RUSSIAN)

        if(!isRussianEnabled) {
            GraziConfig.state.enabledLanguages.add(Lang.RUSSIAN)
            GraziLifecycle.publisher.reInit()
        }

        runHighlightTestForFile("ide/language/plain/ExampleRU.txt")

        if (!isRussianEnabled) {
            GraziConfig.state.enabledLanguages.remove(Lang.RUSSIAN)
            GraziLifecycle.publisher.reInit()
        }
    }
}
