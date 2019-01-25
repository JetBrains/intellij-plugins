package training.lang

import com.intellij.lang.LanguageExtensionPoint
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.extensions.ExtensionPointName
import training.ui.LearnToolWindowFactory
import training.util.findLanguageByID

/**
 * @author Sergey Karashevich
 */

@State(name = "LangManager", storages = arrayOf(Storage(value = "ide-features-trainer.xml")))
class LangManager : PersistentStateComponent<LangManager.State> {

  var supportedLanguagesExtensions: List<LanguageExtensionPoint<LangSupport>> = ExtensionPointName<LanguageExtensionPoint<LangSupport>>(LangSupport.EP_NAME).extensions.toList()
  var myState = State(null)

  private var myLangSupport: LangSupport? = null

  init {
    if (supportedLanguagesExtensions.size == 1) {
      val first = supportedLanguagesExtensions.first()
      myLangSupport = first.instance
      myState.languageName = first.language
    }
  }

  companion object {
    fun getInstance(): LangManager = ServiceManager.getService(LangManager::class.java)
  }

  fun isLangUndefined() = (myLangSupport == null)

  //do not call this if LearnToolWindow with modules or learn views due to reinitViews
  fun updateLangSupport(langSupport: LangSupport) {
    myLangSupport = langSupport
    myState.languageName  = supportedLanguagesExtensions.find { it.instance == langSupport }?.language ?: throw Exception("Unable to get language.")
    LearnToolWindowFactory.learnWindowPerProject.values.forEach{ it.reinitViews() }
  }

  fun getLangSupport(): LangSupport {
    return myLangSupport ?: throw Exception("Lang support is not defined.")
  }

  override fun loadState(state: State) {
    myLangSupport = supportedLanguagesExtensions.find { langExt -> langExt.language == state.languageName }?.instance ?: return
    myState.languageName = state.languageName
  }

  override fun getState() = myState

  fun getLanguageDisplayName(): String {
    if (myState.languageName == null) return "default"
    return (findLanguageByID(myState.languageName) ?: return "default").displayName
  }

  data class State(var languageName: String? = "java")
}