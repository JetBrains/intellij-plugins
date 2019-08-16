package tanvd.grazi.ide.quickfix

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import tanvd.grazi.GraziConfig
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.ui.components.dsl.msg
import javax.swing.Icon

class GraziDisableRule(private val typo: Typo) : LocalQuickFix, Iconable, PriorityAction {
    override fun getFamilyName(): String = msg("grazi.quickfix.disablerule.family")

    override fun getName() = msg("grazi.quickfix.disablerule.text", typo.info.rule.description)

    override fun getIcon(flags: Int): Icon = AllIcons.Actions.Cancel

    override fun getPriority() = PriorityAction.Priority.LOW

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        GraziConfig.update {
            it.update(userEnabledRules = it.userEnabledRules - typo.info.rule.id, userDisabledRules = it.userDisabledRules + typo.info.rule.id)
        }
    }
}

