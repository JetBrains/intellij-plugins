/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.lang

import com.intellij.lang.Language
import com.intellij.lang.LanguageExtensionPoint
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.extensions.ExtensionPointName
import training.learn.CourseManager
import training.ui.LearnToolWindowFactory
import training.util.findLanguageByID
import training.util.trainerPluginConfigName

/**
 * @author Sergey Karashevich
 */
@State(name = "LangManager", storages = [Storage(value = trainerPluginConfigName)])
class LangManager : PersistentStateComponent<LangManager.State> {

  var supportedLanguagesExtensions: List<LanguageExtensionPoint<LangSupport>> = ExtensionPointName<LanguageExtensionPoint<LangSupport>>(LangSupport.EP_NAME).extensions.toList()
  var myState = State(null)

  private var myLangSupport: LangSupport? = null

  init {
    val onlyLang = supportedLanguagesExtensions.singleOrNull { Language.findLanguageByID(it.language) != null }
    if (onlyLang != null) {
      myLangSupport = onlyLang.instance
      myState.languageName = onlyLang.language
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

  fun getLangSupport(): LangSupport? {
    return myLangSupport
  }

  override fun loadState(state: State) {
    myLangSupport = supportedLanguagesExtensions.find { langExt -> langExt.language == state.languageName }?.instance ?: return
    myState.languageToLessonsNumberMap = state.languageToLessonsNumberMap
    myState.languageName = state.languageName
  }

  override fun getState() = myState

  fun getLanguageDisplayName(): String {
    if (myState.languageName == null) return "default"
    return (findLanguageByID(myState.languageName) ?: return "default").displayName
  }

  /** Primary Language Id -> Number of Lessons */
  fun getLanguageToLessonsNumberMap(): Map<String, Int> {
    val map = HashMap<String, Int>()
    val sorted = supportedLanguagesExtensions.sortedBy { it.language }
    for (langSupportExt in sorted) {
      val lessonsCount = CourseManager.instance.calcLessonsForLanguage(langSupportExt.instance)
      map[langSupportExt.language] = lessonsCount
    }
    return map
  }

  // Note: languageName - it is language Id actually
  // Default map is a map for the last published version (191.6183.6) before this field added
  data class State(var languageName: String? = null, var languageToLessonsNumberMap: Map<String, Int> = mapOf(
      "swift" to 26,
      "java" to 25,
      "ruby" to 14))
}