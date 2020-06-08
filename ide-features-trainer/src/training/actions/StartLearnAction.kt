// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.actions

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.ui.DialogWrapper
import icons.FeaturesTrainerIcons
import training.lang.LangManager
import training.lang.LangSupport
import training.learn.CourseManager
import training.learn.LearnBundle
import training.learn.interfaces.Lesson
import training.ui.views.LanguageChoosePanel
import javax.swing.JComponent

class StartLearnAction : AnAction(
  LearnBundle.message("learn.WelcomeScreen.StartLearn.text", ApplicationNamesInfo.getInstance().fullProductName),
  LearnBundle.message("learn.WelcomeScreen.StartLearn.description"), FeaturesTrainerIcons.FeatureTrainer) {

  override fun actionPerformed(e: AnActionEvent) {
    if (LangManager.getInstance().isLangUndefined()) {
      val dialog = MyDialog().initialize()
      with(dialog) {
        if (showAndGet()) {
          LangManager.getInstance().updateLangSupport(this.myLangChoosePanel.getActiveLangSupport())
          doAction()
        }
      }
    }
    else
      doAction()
  }

  class MyDialog : DialogWrapper(null, true) {

    val myLangChoosePanel = LanguageChoosePanel(toolWindow = null, opaque = false, addButton = false)

    fun initialize(): MyDialog {
      isModal = true
      title = LearnBundle.message("learn.choose.language.dialog.title")
      setOKButtonText(LearnBundle.message("learn.choose.language.button"))
      horizontalStretch = 1.33f
      verticalStretch = 1.25f
      init()
      return this
    }

    override fun createCenterPanel(): JComponent? = myLangChoosePanel
  }

  private fun doAction() {
    val action = OpenLessonAction(findLessonToLearn())
    val context = DataContext.EMPTY_CONTEXT
    val event = AnActionEvent.createFromAnAction(action, null, "", context)

    ActionUtil.performActionDumbAware(action, event)
  }

  private fun findLessonToLearn(): Lesson {
    val langSupport: LangSupport = LangManager.getInstance().getLangSupport()
                                   ?: throw IllegalStateException("Language for studying hasn't been chosen yet.")
    val modules = CourseManager.instance.getModulesByLanguage(langSupport)
    //let's take first lesson, which was not passed or open the first one
    return modules.map { it.giveNotPassedLesson() }.firstOrNull() ?: modules.first().lessons.first()
  }

  companion object {
    /** Show tutorials == don't show link */
    fun updateState(dontShowLink: Boolean) {
      val actionManager = ActionManager.getInstance()
      val actionLinksGroup = actionManager.getAction(IdeActions.GROUP_WELCOME_SCREEN_QUICKSTART) as DefaultActionGroup
      val action = actionLinksGroup.getChildren(null, actionManager).find { it is StartLearnAction }
      if (dontShowLink) {
        if (action != null) actionLinksGroup.remove(action)
      }
      else {
        if (action == null) actionLinksGroup.addAction(StartLearnAction(), Constraints.LAST, actionManager)
      }
    }
  }
}