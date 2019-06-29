package tanvd.grazi.ide.language.comment


import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.PsiCommentImpl
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport

class CommentsSupport : LanguageSupport() {
    override fun isRelevant(element: PsiElement): Boolean {
        return element is PsiCommentImpl
    }

    override fun check(element: PsiElement): Set<Typo> {
        return GrammarChecker.default.check(element)
    }
}
