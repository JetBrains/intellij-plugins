package tanvd.grazi.ide.ui

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.annotations.Property
import tanvd.grazi.grammar.GrammarEngine
import tanvd.grazi.language.Lang
import tanvd.grazi.language.LangChecker
import tanvd.grazi.spellcheck.SpellDictionary
import java.io.File


@State(name = "GraziApplicationSettings", storages = [Storage("grazi.xml")])
class GraziApplicationSettings : PersistentStateComponent<GraziApplicationSettings.State> {
    data class State(@Property val enabledLanguages: MutableSet<Lang> = hashSetOf(Lang.ENGLISH),
                     @Property var graziFolder: File = File(System.getProperty("user.home"), ".grazi"),
                     @Property var motherTongue: Lang = enabledLanguages.first(),
                     @Property var enabledSpellcheck: Boolean = false)

    private var myState = State()

    override fun getState(): State {
        return myState
    }

    override fun loadState(state: State) {
        myState = state

        init()
    }

    fun init() {
        LangChecker.clear()
        SpellDictionary.graziFolder = myState.graziFolder
        GrammarEngine.getInstance().enabledLangs = myState.enabledLanguages.toList()
    }


    companion object {
        val instance: GraziApplicationSettings
            get() = ServiceManager.getService(GraziApplicationSettings::class.java)
    }
}
