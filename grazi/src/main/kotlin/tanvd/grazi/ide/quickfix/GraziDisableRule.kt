package tanvd.grazi.ide.quickfix

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import tanvd.grazi.GraziConfig
import tanvd.grazi.GraziPlugin
import tanvd.grazi.grammar.Typo

class GraziDisableRule(private val typo: Typo) : LocalQuickFix, PriorityAction {
    override fun getName() = "Disable rule '${typo.info.rule.description}'"

    override fun getFamilyName(): String = "Disable rule"

    override fun getPriority() = PriorityAction.Priority.LOW

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        GraziConfig.state.userDisabledRules.add(typo.info.rule.id)

        GraziPlugin.invalidateCaches()
    }
}
