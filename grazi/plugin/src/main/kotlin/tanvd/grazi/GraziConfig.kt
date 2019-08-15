package tanvd.grazi

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.ProjectManager
import com.intellij.util.xmlb.annotations.Property
import tanvd.grazi.ide.msg.GraziStateLifecycle
import tanvd.grazi.language.Lang
import tanvd.kex.ifTrue


@State(name = "GraziConfig", storages = [Storage("grazi_global.xml")])
class GraziConfig : PersistentStateComponent<GraziConfig.State> {
    class State(@Property val enabledLanguages: Set<Lang> = hashSetOf(Lang.AMERICAN_ENGLISH),
                @Property val nativeLanguage: Lang = enabledLanguages.first(),
                @Property val enabledSpellcheck: Boolean = false,
                @Property val userWords: Set<String> = HashSet(),
                @Property val userDisabledRules: Set<String> = HashSet(),
                @Property val userEnabledRules: Set<String> = HashSet(),
                @Property val lastSeenVersion: String? = null,
                val availableLanguages: Set<Lang> = enabledLanguages.filter { it.jLanguage != null }.toSet()) {

        val missedLanguages: Set<Lang>
            get() = enabledLanguages.filter { it.jLanguage == null }.toSet() +
                    ((nativeLanguage.jLanguage == null).ifTrue { setOf(nativeLanguage) } ?: emptySet())

        fun clone() = State(
                enabledLanguages = HashSet(enabledLanguages), nativeLanguage = nativeLanguage, enabledSpellcheck = enabledSpellcheck,
                userWords = HashSet(userWords), userDisabledRules = HashSet(userDisabledRules), userEnabledRules = HashSet(userEnabledRules),
                lastSeenVersion = lastSeenVersion, availableLanguages = availableLanguages)

        fun hasMissedLanguages(withNative: Boolean = true) = (withNative && nativeLanguage.jLanguage == null) || enabledLanguages.any { it.jLanguage == null }

        fun update(enabledLanguages: Set<Lang> = this.enabledLanguages,
                   nativeLanguage: Lang = this.nativeLanguage,
                   enabledSpellcheck: Boolean = this.enabledSpellcheck,
                   userWords: Set<String> = this.userWords,
                   userDisabledRules: Set<String> = this.userDisabledRules,
                   userEnabledRules: Set<String> = this.userEnabledRules,
                   lastSeenVersion: String? = this.lastSeenVersion) = State(enabledLanguages, nativeLanguage, enabledSpellcheck,
                userWords, userDisabledRules, userEnabledRules, lastSeenVersion, enabledLanguages.filter { it.jLanguage != null }.toSet())

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as State

            if (enabledLanguages != other.enabledLanguages) return false
            if (nativeLanguage != other.nativeLanguage) return false
            if (enabledSpellcheck != other.enabledSpellcheck) return false
            if (userWords != other.userWords) return false
            if (userDisabledRules != other.userDisabledRules) return false
            if (userEnabledRules != other.userEnabledRules) return false
            if (lastSeenVersion != other.lastSeenVersion) return false
            if (availableLanguages != other.availableLanguages) return false

            return true
        }

        override fun hashCode(): Int {
            var result = enabledLanguages.hashCode()
            result = 31 * result + nativeLanguage.hashCode()
            result = 31 * result + enabledSpellcheck.hashCode()
            result = 31 * result + userWords.hashCode()
            result = 31 * result + userDisabledRules.hashCode()
            result = 31 * result + userEnabledRules.hashCode()
            result = 31 * result + (lastSeenVersion?.hashCode() ?: 0)
            result = 31 * result + availableLanguages.hashCode()
            return result
        }
    }

    companion object {
        private val instance: GraziConfig by lazy { ServiceManager.getService(GraziConfig::class.java) }

        /** Get copy of Grazi config state */
        fun get() = instance.state

        /** Update Grazi config state */
        @Synchronized
        fun update(change: (State) -> State) = instance.loadState(change(get()))
    }

    private var myState = State()

    override fun getState() = myState.clone()

    override fun loadState(state: State) {
        val prevState = myState
        myState = state

        if (prevState != myState) {
            ProjectManager.getInstance().openProjects.forEach { GraziStateLifecycle.publisher.update(prevState, myState, it) }
        }
    }
}
