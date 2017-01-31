package training.lang

import com.intellij.codeInsight.template.postfix.templates.LanguagePostfixTemplate
import com.intellij.lang.LanguageExtensionPoint
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.extensions.ExtensionPointName

/**
 * @author Sergey Karashevich
 */

@State(name = "TrainingLangManager", storages = arrayOf(Storage("trainingPlugin.xml")))
class LangManager: PersistentStateComponent<LangManager.State> {

    var supportedLanguagesExtensions: List<LanguageExtensionPoint<LangSupport>> = ExtensionPointName<LanguageExtensionPoint<LangSupport>>(LangSupport.EP_NAME).extensions.toList()
    val myState = State()

    var mySupportedLanguage: LangSupport? = null

    init {
        if (supportedLanguagesExtensions.size == 1) {
            val first = supportedLanguagesExtensions.first()
            mySupportedLanguage = first.instance
            myState.languageName = first.language
        }

    }

    companion object { fun getInstance(): LangManager = ServiceManager.getService(LangManager::class.java) }

    fun isLangUndefined() = (mySupportedLanguage == null)

    override fun loadState(state: State?) {
        if (state == null) return
        mySupportedLanguage = supportedLanguagesExtensions.find { langExt -> langExt.language == state.languageName }!!.instance
    }

    override fun getState() = myState

    class State {
        var languageName: String? = null
    }

}