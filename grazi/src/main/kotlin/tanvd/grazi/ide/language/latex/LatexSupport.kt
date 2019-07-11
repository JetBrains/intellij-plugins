package tanvd.grazi.ide.language.latex

import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.psi.LatexTypes.*
import org.jetbrains.kotlin.psi.psiUtil.parents
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.exclusiveFilterFor
import tanvd.grazi.utils.filterForTokens

class LatexSupport : LanguageSupport() {
    override fun isRelevant(element: PsiElement): Boolean {
        return (element is LatexEnvironmentContent || element is LatexGroup) && element.parents.none { it is LatexMathEnvironment }
    }

    override fun check(element: PsiElement): Set<Typo> {
        require(element is LatexEnvironmentContent || element is LatexGroup) { "Got non LatexEnvironmentContent or LatexGroup in LatexSupport" }
        return GrammarChecker.default.check(
                element.exclusiveFilterFor({ it is LatexEnvironment || it is LatexMathEnvironment || it is LatexOpenGroup || it is LatexGroup || it is LatexEnvironmentContent }) { it is LatexNormalText }
                        .flatMap { it.filterForTokens<PsiElement>(NORMAL_TEXT_WORD) }
        ).toSet()
    }
}
