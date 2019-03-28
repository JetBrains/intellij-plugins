package tanvd.grazi.ide.language


import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.JavaDocTokenType
import com.intellij.psi.PsiFile
import com.intellij.psi.javadoc.*
import com.intellij.psi.util.PsiTreeUtil
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.utils.CustomTokensChecker

class JavaDocSupport : LanguageSupport {
    companion object {
        val tagsIgnoredCategories = listOf(Typo.Category.CASING)
    }

    private fun isTag(token: PsiDocToken) = token.parent is PsiDocTag

    override fun extract(file: PsiFile): List<LanguageSupport.Result> {
        val docs = PsiTreeUtil.collectElementsOfType(file, PsiDocComment::class.java)

        val result = ArrayList<LanguageSupport.Result>()
        for (doc in docs) {
            result += CustomTokensChecker.default.check(
                    PsiTreeUtil.collectElementsOfType(doc, PsiDocToken::class.java)
                            .filter { (it.tokenType == JavaDocTokenType.DOC_COMMENT_DATA) }
                            .filterNot { isTag(it) })

            result += CustomTokensChecker.default.check(
                    PsiTreeUtil.collectElementsOfType(doc, PsiDocToken::class.java)
                            .filter { (it.tokenType == JavaDocTokenType.DOC_COMMENT_DATA) }
                            .filter { isTag(it) })
                    .filter { it.typo.category !in tagsIgnoredCategories }

            ProgressManager.checkCanceled()
        }

        return result
    }
}
