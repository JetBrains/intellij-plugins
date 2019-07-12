package tanvd.grazi.ide.language.latex

import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.psi.LatexGroup
import nl.hannahsten.texifyidea.psi.LatexMathEnvironment
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.psi.LatexOpenGroup
import nl.hannahsten.texifyidea.psi.LatexTypes.NORMAL_TEXT_WORD
import org.jetbrains.kotlin.idea.conversion.copy.end
import org.jetbrains.kotlin.idea.conversion.copy.start
import org.jetbrains.kotlin.psi.psiUtil.parents
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.filterForTokens

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

    private fun Typo.isAtStartOrAtEndOfElement(element: PsiElement): Boolean {
        return location.element?.let {
            it.textRange.start == element.textRange.start || it.textRange.end == element.textRange.end
        } ?: false
    }

    override fun check(element: PsiElement): Set<Typo> {
        require(element is LatexNormalText) { "Got non LatexNormalText in LatexSupport" }
        return GrammarChecker.default.check(element.filterForTokens<PsiElement>(NORMAL_TEXT_WORD)).filterNot { typo ->
            typo.info.category in ignoredCategories && typo.isAtStartOrAtEndOfElement(element)
        }.toSet()
    }
}
