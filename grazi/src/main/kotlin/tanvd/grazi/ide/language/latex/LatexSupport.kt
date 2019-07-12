package tanvd.grazi.ide.language.latex

import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.psi.LatexGroup
import nl.hannahsten.texifyidea.psi.LatexMathEnvironment
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.psi.LatexOpenGroup
import org.jetbrains.kotlin.psi.psiUtil.parents
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport

class LatexSupport : LanguageSupport() {
    private val ignoredCategories = listOf(Typo.Category.CASING, Typo.Category.PUNCTUATION, Typo.Category.GRAMMAR)

    private fun PsiElement.isNotInMathEnvironment(): Boolean {
        return parents.none { it is LatexMathEnvironment }
    }

    private fun PsiElement.isNotInSquareBrackets(): Boolean {
        return parents.find { it is LatexGroup || it is LatexOpenGroup }?.let { it is LatexGroup } ?: true
    }

    override fun isRelevant(element: PsiElement): Boolean {
        return element is LatexNormalText && element.isNotInMathEnvironment() && element.isNotInSquareBrackets()
    }

    override fun check(element: PsiElement): Set<Typo> {
        require(element is LatexNormalText) { "Got non LatexNormalText in LatexSupport" }
        return GrammarChecker.default.check(element).filterNot { typo ->
            typo.info.category in ignoredCategories && (typo.location.isAtStart() || typo.location.isAtEnd())
        }.toSet()
    }
}
