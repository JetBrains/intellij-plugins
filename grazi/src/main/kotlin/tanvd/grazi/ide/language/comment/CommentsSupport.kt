package tanvd.grazi.ide.language.comment


import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.PsiCommentImpl
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.ide.language.LanguageSupport

class CommentsSupport : LanguageSupport() {
    override fun isRelevant(element: PsiElement) = element is PsiCommentImpl

    override fun check(element: PsiElement) = GrammarChecker.default.check(element)
}
