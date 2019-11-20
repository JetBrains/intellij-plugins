package training.ui.welcomeScreen.recentProjects.actionGroups.commonActions

import com.intellij.openapi.actionSystem.AnAction
import training.actions.ModuleActionGroup
import training.lang.LangManager
import training.learn.CourseManager
import training.ui.welcomeScreen.recentProjects.actionGroups.CommonActionGroup

class TutorialsActionGroup : CommonActionGroup("Tutorials", emptyList()) {

  private val moduleActionGroups by lazy {
    CourseManager.instance.modules.map { ModuleActionGroup(it) }.toTypedArray()
  }

  override fun getActions(): Array<AnAction?> {
    val result: ArrayList<AnAction?> = arrayListOf()
    result.add(this)
    if (!isExpanded) return result.toTypedArray()
    if (LangManager.getInstance().isLangUndefined()) LangManager.getInstance().updateLangSupport(
      LangManager.getInstance().supportedLanguagesExtensions.first().instance)
    moduleActionGroups.forEach {
      result.add(it)
    }
    return result.toTypedArray()
  }

}