package tanvd.grazi.ide.language.latex

import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.psi.LatexMathEnvironment
import nl.hannahsten.texifyidea.psi.LatexNormalText
import org.jetbrains.kotlin.psi.psiUtil.parents
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport

class LatexSupport : LanguageSupport() {
    companion object {
        private val tagsIgnoredCategories = listOf(Typo.Category.CASING)
    }

    override fun isRelevant(element: PsiElement): Boolean {
        return element is LatexNormalText && element.parents.none { it is LatexMathEnvironment }
    }

    override fun check(element: PsiElement): Set<Typo> {
        require(element is LatexNormalText) { "Got non LatexNormalText in LatexSupport" }
        return GrammarChecker.default.check(element).filterNot { it.info.category in tagsIgnoredCategories }.toSet()
    }
}
