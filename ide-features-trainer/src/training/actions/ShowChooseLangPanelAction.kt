package training.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import training.ui.LearnToolWindowFactory
import training.ui.views.LanguageChoosePanel

/**
 * @author Sergey Karashevich
 */
class ShowChooseLangPanelAction: AnAction() {

    override fun actionPerformed(e: AnActionEvent?) {
        val myLanguageChoosePanel = LanguageChoosePanel()
        val myLearnToolWindow = LearnToolWindowFactory.getMyLearnToolWindow()
        val scrollPane = myLearnToolWindow.scrollPane
        scrollPane!!.setViewportView(myLanguageChoosePanel)
        scrollPane.revalidate()
        scrollPane.repaint()
    }

}