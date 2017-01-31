package training.lang

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

/**
 * @author Sergey Karashevich
 */

@State(name = "TrainingLangManager", storages = arrayOf(Storage("trainingPlugin.xml")))
class LangManager: PersistentStateComponent<LangManager.State> {

    var supportedLanguages: List<LangSupport> = LangSupport.LANG_SUPPORT_EP_NAME.extensions.toList()
    val myState = State()

    var mySupportedLanguage: LangSupport?

    init {
        //TODO: REMOVE BEFORE RELEASE
        addMockLangSupport()
        mySupportedLanguage =
                if (supportedLanguages.size == 1) supportedLanguages.first()
                else null
    }

    companion object { fun getInstance(): LangManager = ServiceManager.getService(LangManager::class.java) }

    fun selectSupportedLang(langSupport: LangSupport) {
        mySupportedLanguage = langSupport
        myState.languageName = langSupport.getLangName()
    }

    fun isLangUndefined() = (mySupportedLanguage == null)

    override fun loadState(state: State?) {
        if (state == null) return
        mySupportedLanguage = supportedLanguages.find { lang -> lang.getLangName() == state.languageName }
    }

    override fun getState() = myState

    class State {
        var languageName: String? = null
    }

    //TODO: remove before release
    fun addMockLangSupport() {
        supportedLanguages += listOf(MockLangSupport("Lang1"), MockLangSupport("Lang2"), MockLangSupport("Lang3"))
    }

}