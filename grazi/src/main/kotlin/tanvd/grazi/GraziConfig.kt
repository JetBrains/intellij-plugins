package tanvd.grazi

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.annotations.Property
import tanvd.grazi.language.Lang
import java.io.File


@State(name = "GraziConfig", storages = [Storage("grazi.xml")])
class GraziConfig : PersistentStateComponent<GraziConfig.State> {
    data class State(@Property val enabledLanguages: MutableSet<Lang> = hashSetOf(Lang.ENGLISH),
                     @Property var graziFolder: File = File(System.getProperty("user.home"), ".grazi"),
                     @Property var motherTongue: Lang = enabledLanguages.first(),
                     @Property var enabledSpellcheck: Boolean = false)

    companion object {
        val instance: GraziConfig by lazy { if (GraziPlugin.isTest) GraziConfig() else ServiceManager.getService(GraziConfig::class.java) }

        val state: GraziConfig.State
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
