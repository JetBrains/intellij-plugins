package tanvd.grazi.ide.quickfix

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import icons.SpellcheckerIcons
import tanvd.grazi.GraziConfig
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.ui.components.dsl.msg
import javax.swing.Icon

class GraziAddWord(private val typo: Typo) : LocalQuickFix, Iconable, PriorityAction {
    override fun getFamilyName() = msg("grazi.quickfix.addword.family")

    override fun getName() = msg("grazi.quickfix.addword.text", typo.word)

    override fun getIcon(flags: Int): Icon = SpellcheckerIcons.Dictionary

    override fun getPriority() = PriorityAction.Priority.NORMAL

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        GraziConfig.update {
            it.update(userWords = it.userWords + typo.word.toLowerCase())
        }
    }
}
