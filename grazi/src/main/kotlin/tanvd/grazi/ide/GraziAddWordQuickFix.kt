package tanvd.grazi.ide

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import tanvd.grazi.grammar.GrammarCache
import tanvd.grazi.language.LangChecker
import tanvd.grazi.spellcheck.SpellDictionary


class GraziAddWordQuickFix(private val word: String) : LocalQuickFix {

    override fun getName(): String {
        return "Add '$word' to global dictionary"
    }

    override fun getFamilyName(): String = "Add word"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        SpellDictionary.usersCustom().add(word)
        GrammarCache.reset()
        LangChecker.clear()
    }
}
