// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.ui.welcomeScreen.recentProjects.actionGroups.commonActions

import com.intellij.openapi.actionSystem.AnAction
import training.actions.ModuleActionGroup
import training.lang.LangManager
import training.learn.CourseManager
import training.learn.LearnBundle
import training.ui.welcomeScreen.recentProjects.actionGroups.CommonActionGroup

class TutorialsActionGroup : CommonActionGroup(LearnBundle.message("welcome.screen.tutorials.title"), emptyList()) {

  init {
    isExpanded = true
  }

  private val moduleActionGroups by lazy {
    CourseManager.instance.modules.map { ModuleActionGroup(it) }.toTypedArray()
  }

  override fun getActions(): Array<AnAction> {
    val result: ArrayList<AnAction> = arrayListOf()
    result.add(this)
    if (!isExpanded) return result.toTypedArray()
    if (LangManager.getInstance().isLangUndefined()) {
      val langSupport = LangManager.getInstance().supportedLanguagesExtensions.first().instance
      LangManager.getInstance().updateLangSupport(langSupport)
    }
    moduleActionGroups.forEach { result.add(it) }
    return result.toTypedArray()
  }

}