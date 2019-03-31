package tanvd.grazi.ide.ui

import com.intellij.openapi.components.*
import tanvd.grazi.grammar.GrammarEngine
import tanvd.grazi.language.LangChecker
import tanvd.grazi.spellcheck.SpellDictionary
import java.io.File
import java.util.ArrayList
import java.util.HashSet


@State(name = "GraziApplicationSettings", storages = [Storage("grazi.xml")])
class GraziApplicationSettings : PersistentStateComponent<GraziApplicationSettings.State> {
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
        GrammarEngine.getInstance().enabledLangs = ArrayList(myState.languages)
    }

    class State {
        val languages: MutableSet<String> = HashSet()
        var graziFolder = File(System.getProperty("user.home"), ".grazi")

        init {
            languages.add("en")
        }
    }

    companion object {

        val instance: GraziApplicationSettings
            get() = ServiceManager.getService(GraziApplicationSettings::class.java)
    }
}
