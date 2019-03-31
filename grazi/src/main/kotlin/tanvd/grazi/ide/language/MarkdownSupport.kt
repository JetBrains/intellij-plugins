package tanvd.grazi.ide.language


import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.plugins.markdown.lang.psi.impl.*
import tanvd.grazi.grammar.CustomTokensChecker
import tanvd.grazi.grammar.Typo

class MarkdownSupport : LanguageSupport {
    companion object {
        val bulletsIgnoredCategories = listOf(Typo.Category.CASING)
    }

    override fun isSupport(file: PsiFile): Boolean {
        return file is MarkdownFile
    }

    override fun extract(file: PsiFile): List<LanguageSupport.Result> {
        val result = ArrayList<LanguageSupport.Result>()
        for (paragraph in PsiTreeUtil.collectElementsOfType(file, MarkdownParagraphImpl::class.java).filter { it.parent !is MarkdownListItemImpl }) {
            result += CustomTokensChecker.default.check(paragraph)

            ProgressManager.checkCanceled()
        }
        for (header in PsiTreeUtil.collectElementsOfType(file, MarkdownHeaderImpl::class.java)) {
            result += CustomTokensChecker.default.check(header)

            ProgressManager.checkCanceled()
        }
        for (item in PsiTreeUtil.collectElementsOfType(file, MarkdownListItemImpl::class.java).filter { it.parent !is MarkdownListItemImpl }) {
            result += CustomTokensChecker.default.check(item).filter {
                it.typo.category !in bulletsIgnoredCategories
            }

            ProgressManager.checkCanceled()
        }

        return result
    }
}
