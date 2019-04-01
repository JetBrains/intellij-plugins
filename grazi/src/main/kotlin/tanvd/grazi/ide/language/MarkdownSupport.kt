package tanvd.grazi.ide.language


import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import org.intellij.plugins.markdown.lang.psi.impl.*
import tanvd.grazi.grammar.SanitizingGrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.utils.buildSet
import tanvd.grazi.utils.filterFor

class MarkdownSupport : LanguageSupport {
    companion object {
        val bulletsIgnoredCategories = listOf(Typo.Category.CASING)
    }

    override fun isSupported(file: PsiFile): Boolean {
        return file is MarkdownFile
    }

    override fun check(file: PsiFile) = buildSet<Typo> {
        for (header in file.filterFor<MarkdownHeaderImpl>()) {
            addAll(SanitizingGrammarChecker.default.check(header))

            ProgressManager.checkCanceled()
        }

        for (paragraph in file.filterFor<MarkdownParagraphImpl>().filter { it.parent !is MarkdownListItemImpl }) {
            addAll(SanitizingGrammarChecker.default.check(paragraph))

            ProgressManager.checkCanceled()
        }

        for (item in file.filterFor<MarkdownListItemImpl>()) {
            addAll(SanitizingGrammarChecker.default.check(item).filter {
                it.info.category !in bulletsIgnoredCategories
            })

            ProgressManager.checkCanceled()
        }
    }
}
