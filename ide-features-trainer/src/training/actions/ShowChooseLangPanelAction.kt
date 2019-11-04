/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import training.ui.LearnToolWindowFactory
import training.ui.views.LanguageChoosePanel

/**
 * @author Sergey Karashevich
 */
class ShowChooseLangPanelAction: AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val myLanguageChoosePanel = LanguageChoosePanel()
        val project = e.project ?: return
        val myLearnToolWindow = LearnToolWindowFactory.learnWindowPerProject[project] ?: return
        val scrollPane = myLearnToolWindow.scrollPane
        scrollPane.setViewportView(myLanguageChoosePanel)
        scrollPane.revalidate()
        scrollPane.repaint()
    }

}