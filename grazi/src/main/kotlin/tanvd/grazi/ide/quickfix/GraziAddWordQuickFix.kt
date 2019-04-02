package tanvd.grazi.ide.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import tanvd.grazi.grammar.*
import tanvd.grazi.spellcheck.*


class GraziAddWordQuickFix(private val typo: Typo) : LocalQuickFix {

    override fun getName(): String {
        return "Save '${typo.word}' to global dictionary"
    }

    override fun getFamilyName(): String = "Save word"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        SpellDictionary.usersCustom().add(typo.word.toLowerCase())

        GrammarEngine.reset()
        GrammarChecker.reset()

        SpellChecker.reset()
    }
}
