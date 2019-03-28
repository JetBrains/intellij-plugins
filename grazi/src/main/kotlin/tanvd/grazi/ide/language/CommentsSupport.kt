package tanvd.grazi.ide.language


import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.PsiCommentImpl
import com.intellij.psi.util.PsiTreeUtil
import tanvd.grazi.ide.language.utils.CustomTokensChecker

class CommentsSupport : LanguageSupport {
    override fun extract(file: PsiFile): List<LanguageSupport.Result> {
        val comments = PsiTreeUtil.collectElementsOfType(file, PsiCommentImpl::class.java)

        val result = ArrayList<LanguageSupport.Result>()
        for (comment in comments) {
            result += CustomTokensChecker.default.check(listOf(comment))

            ProgressManager.checkCanceled()
        }

        return result
    }
}
