/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.actions

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.ui.DialogWrapper
import training.lang.LangManager
import training.learn.LearnBundle
import training.ui.LearnIcons
import training.ui.views.LanguageChoosePanel
import javax.swing.JComponent

class StartLearnAction : AnAction(
  LearnBundle.message("learn.WelcomeScreen.StartLearn.text", ApplicationNamesInfo.getInstance().fullProductName),
  LearnBundle.message("learn.WelcomeScreen.StartLearn.description"), LearnIcons.chevronIcon) {

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

    val myLangChoosePanel = LanguageChoosePanel(opaque = false, addButton = false)

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

  fun doAction() {
    val action = ActionManager.getInstance().getAction("learn.open.lesson")

    val context = DataContext.EMPTY_CONTEXT
    val event = AnActionEvent.createFromAnAction(action, null, "", context)

    ActionUtil.performActionDumbAware(action, event)
  }

}