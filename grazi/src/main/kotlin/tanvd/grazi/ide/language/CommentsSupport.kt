package tanvd.grazi.ide.language


import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.PsiCommentImpl
import com.intellij.psi.util.PsiTreeUtil
import tanvd.grazi.grammar.SanitizingGrammarChecker

class CommentsSupport : LanguageSupport {
    override fun isSupport(file: PsiFile): Boolean {
        return true
    }

    override fun extract(file: PsiFile): List<LanguageSupport.Result> {
        val comments = PsiTreeUtil.collectElementsOfType(file, PsiCommentImpl::class.java)

        val result = ArrayList<LanguageSupport.Result>()
        for (comment in comments) {
            result += SanitizingGrammarChecker.default.check(listOf(comment))

            ProgressManager.checkCanceled()
        }

        return result
    }
}
