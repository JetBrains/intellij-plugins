package tanvd.grazi.ide.language.latex

import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.psi.*
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.filterNotToSet
import tanvd.grazi.utils.parents
import tanvd.kex.orTrue

class LatexSupport : LanguageSupport() {
    private fun PsiElement.isNotInMathEnvironment(): Boolean {
        return parents().none { it is LatexMathEnvironment }
    }

    private fun PsiElement.isNotInSquareBrackets(): Boolean {
        return parents().find { it is LatexGroup || it is LatexOpenGroup }?.let { it is LatexGroup }.orTrue()
    }

    override fun isRelevant(element: PsiElement): Boolean {
        return element is LatexNormalText && element.isNotInMathEnvironment() && element.isNotInSquareBrackets()
    }

    override fun check(element: PsiElement): Set<Typo> {
        require(element is LatexNormalText) { "Got non LatexNormalText in LatexSupport" }

        return GrammarChecker.default.check(element).filterNotToSet { typo -> typo.location.isAtStart() || typo.location.isAtEnd() }
    }
}
