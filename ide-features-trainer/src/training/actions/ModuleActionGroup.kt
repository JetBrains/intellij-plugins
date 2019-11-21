package training.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.util.ui.EmptyIcon
import training.learn.interfaces.Module
import training.ui.LearnIcons
import training.ui.welcomeScreen.recentProjects.RenderableAction
import javax.swing.Icon

class ModuleActionGroup(val module: Module): DefaultActionGroup(module.name, module.lessons.map { OpenLessonAction(it) }), RenderableAction {
    override var isValid: Boolean
        get() = true
        set(value) {}
    var isExpanded = false
    override val action: AnAction
        get() = this

    override val name: String
        get() = module.name
    override val description: String?
        get() = module.description
    override val icon: Icon?
        get() = if (!module.hasNotPassedLesson()) LearnIcons.checkMarkGray else null
    override val emptyIcon: Icon
        get() = EmptyIcon.ICON_0
    override fun isPopup(): Boolean {
        return isExpanded
    }

    override fun actionPerformed(e: AnActionEvent) {
        //todo: add fus collection here
        val lessonToOpen = module.giveNotPassedLesson() ?: module.lessons.first()
        val openLessonAction = OpenLessonAction(lessonToOpen)
        ActionUtil.performActionDumbAware(openLessonAction, e)
    }
}