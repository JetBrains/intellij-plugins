package tanvd.grazi.ide.quickfix

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import tanvd.grazi.GraziConfig
import tanvd.grazi.grammar.*
import tanvd.grazi.spellcheck.SpellChecker

class GraziDisableRule(private val typo: Typo) : LocalQuickFix, PriorityAction {
    override fun getName(): String {
        return "Disable rule '${typo.info.rule.description}'"
    }

    override fun getFamilyName(): String = "Disable rule"

    override fun getPriority() = PriorityAction.Priority.LOW

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        GraziConfig.state.userDisabledRules.add(typo.info.rule.id)

        GrammarEngine.reset()
        GrammarChecker.reset()

        SpellChecker.reset()
    }
}
