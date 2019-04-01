package tanvd.grazi.ide.language


import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.PsiCommentImpl
import tanvd.grazi.grammar.SanitizingGrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.utils.buildSet
import tanvd.grazi.utils.filterFor

class CommentsSupport : LanguageSupport {
    override fun isSupported(file: PsiFile): Boolean {
        return true
    }

    override fun check(file: PsiFile) = buildSet<Typo> {
        for (comment in file.filterFor<PsiCommentImpl>()) {
            addAll(SanitizingGrammarChecker.default.check(comment))

            ProgressManager.checkCanceled()
        }
    }
}
