package tanvd.grazi.ide.quickfix

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import tanvd.grazi.GraziConfig
import tanvd.grazi.grammar.*
import tanvd.grazi.spellcheck.SpellChecker


class GraziAddWord(private val typo: Typo) : LocalQuickFix, PriorityAction {

    override fun getName(): String {
        return "Add '${typo.word}' to global dictionary"
    }

    override fun getPriority() = PriorityAction.Priority.NORMAL

    override fun getFamilyName(): String = "Add word"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        GraziConfig.state.userWords.add(typo.word.toLowerCase())

        GrammarEngine.reset()
        GrammarChecker.reset()

        SpellChecker.reset()
    }
}
