package tanvd.grazi.ide.language.latex

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.psi.LatexTypes.NORMAL_TEXT_WORD
import org.jetbrains.kotlin.idea.conversion.copy.end
import org.jetbrains.kotlin.idea.conversion.copy.start
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.rust.lang.core.psi.ext.isAncestorOf
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.exclusiveFilterFor
import tanvd.grazi.utils.filterForTokens

class LatexSupport : LanguageSupport() {
    private val ignoredCategories = listOf(Typo.Category.CASING, Typo.Category.PUNCTUATION, Typo.Category.GRAMMAR)

    override fun isRelevant(element: PsiElement): Boolean {
        return element is LatexNormalText && element.parents.none { it is LatexMathEnvironment }
                && PsiTreeUtil.findFirstParent(element) { it is LatexOpenGroup }?.let { latexOpenGroup ->
            PsiTreeUtil.findFirstParent(element) { it is LatexGroup }?.let { latexOpenGroup.isAncestorOf(it) } ?: false
        } ?: true
    }

    override fun check(element: PsiElement): Set<Typo> {
        require(element is LatexNormalText) { "Got non LatexNormalText in LatexSupport" }
        return GrammarChecker.default.check(
                element.filterForTokens<PsiElement>(NORMAL_TEXT_WORD)
        ).filterNot { typo ->
            typo.info.category in ignoredCategories &&
            typo.location.element?.let {
                it.textRange.start == element.textRange.start || it.textRange.end == element.textRange.end
            } ?: false
        }.toSet()
    }
}
