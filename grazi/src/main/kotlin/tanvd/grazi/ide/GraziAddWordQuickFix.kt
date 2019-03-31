package tanvd.grazi.ide

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import tanvd.grazi.grammar.GrammarCache
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.spellcheck.SpellDictionary


class GraziAddWordQuickFix(private val word: String, private val hash: Int) : LocalQuickFix {

    override fun getName(): String {
        return "Save '$word' to global dictionary"
    }

    override fun getFamilyName(): String = "Save word"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        SpellDictionary.usersCustom().add(word)
        GrammarCache.invalidate(hash)
        GrammarChecker.clear()
    }
}
