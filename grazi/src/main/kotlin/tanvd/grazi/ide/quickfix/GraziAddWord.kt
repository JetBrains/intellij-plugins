package tanvd.grazi.ide.quickfix

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import icons.SpellcheckerIcons
import tanvd.grazi.GraziConfig
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.msg.GraziAppLifecycle
import javax.swing.Icon


class GraziAddWord(private val typo: Typo) : LocalQuickFix, Iconable, PriorityAction {
    override fun getIcon(flags: Int): Icon = SpellcheckerIcons.Dictionary

    override fun getName() = "Save '${typo.word}' to dictionary"

    override fun getPriority() = PriorityAction.Priority.NORMAL

    override fun getFamilyName() = "Save word"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        GraziConfig.update {
            it.copy(userWords = it.userWords + typo.word.toLowerCase())
        }

        GraziAppLifecycle.publisher.reset()
    }
}
