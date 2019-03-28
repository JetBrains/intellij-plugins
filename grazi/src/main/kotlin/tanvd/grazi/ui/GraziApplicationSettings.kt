package tanvd.grazi.ui

import com.intellij.openapi.components.*
import tanvd.grazi.grammar.GrammarEngineService
import java.util.*

@State(name = "GraziApplicationSettings", storages = [Storage("grazi.xml")])
class GraziApplicationSettings : PersistentStateComponent<GraziApplicationSettings.State> {
    data class State(val languages: HashSet<String> = hashSetOf("en"))

    private var _state = State()

    val myState: State
        get() = _state

    override fun getState(): State {
        return _state
    }

    override fun loadState(state: State) {
        _state = state
    }

    fun loadLanguages() {
        GrammarEngineService.getInstance().enabledLangs = ArrayList(_state.languages)
    }

    companion object {
        val instance: GraziApplicationSettings
            get() = ServiceManager.getService(GraziApplicationSettings::class.java)
    }
}
