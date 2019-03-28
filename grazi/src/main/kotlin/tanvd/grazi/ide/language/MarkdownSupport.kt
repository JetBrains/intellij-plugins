package tanvd.grazi.ide.language


import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.plugins.markdown.lang.psi.impl.*
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.utils.CustomTokensChecker

class MarkdownSupport : LanguageSupport {
    companion object {
        val bulletsIgnoredCategories = listOf(Typo.Category.CASING)
    }

    override fun extract(file: PsiFile): List<LanguageSupport.Result> {
        val result = ArrayList<LanguageSupport.Result>()
        for (paragraph in PsiTreeUtil.collectElementsOfType(file, MarkdownParagraphImpl::class.java)) {
            result += CustomTokensChecker.default.check(paragraph)
        }
        for (header in PsiTreeUtil.collectElementsOfType(file, MarkdownHeaderImpl::class.java)) {
            result += CustomTokensChecker.default.check(header)
        }
        for (item in PsiTreeUtil.collectElementsOfType(file, MarkdownListItemImpl::class.java)) {
            result += CustomTokensChecker.default.check(item).filter {
                it.typo.category !in bulletsIgnoredCategories
            }
        }

        return result
    }
}
