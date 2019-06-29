package tanvd.grazi

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.annotations.Property
import tanvd.grazi.language.Lang


@State(name = "GraziConfig", storages = [Storage("grazi_global.xml")])
class GraziConfig : PersistentStateComponent<GraziConfig.State> {
    data class State(@Property val enabledLanguages: MutableSet<Lang> = hashSetOf(Lang.AMERICAN_ENGLISH),
                     @Property var nativeLanguage: Lang = enabledLanguages.first(),
                     @Property var enabledSpellcheck: Boolean = true,
                     @Property val userWords: MutableSet<String> = HashSet(),
                     @Property val userDisabledRules: MutableSet<String> = HashSet(),
                     @Property var lastSeenVersion: String? = null)

    companion object {
        val instance: GraziConfig by lazy { ServiceManager.getService(GraziConfig::class.java) }

        val state: State
            get() = instance.state
    }


    private var myState = State()

    override fun getState(): State {
        return myState
    }

    override fun loadState(state: State) {
        val prevState = myState
        myState = state

        if (prevState != myState) {
            GraziPlugin.invalidateCaches()
            GraziPlugin.init()
        }
    }
}
