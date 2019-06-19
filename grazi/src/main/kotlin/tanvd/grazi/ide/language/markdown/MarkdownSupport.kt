package tanvd.grazi.ide.language.markdown


import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.TokenSet
import org.intellij.plugins.markdown.lang.MarkdownElementTypes
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownFile
import tanvd.grazi.grammar.*
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.*
import tanvd.kex.buildSet

class MarkdownSupport : LanguageSupport() {
    companion object {
        val bulletsIgnoredCategories = listOf(Typo.Category.CASING)
    }

    override fun isSupported(file: PsiFile): Boolean {
        return file is MarkdownFile
    }

    override fun check(file: PsiFile) = buildSet<Typo> {
        //headers
        for (header in file.filterFor<PsiElement> { MarkdownPsiUtils.isHeader(it) }) {
            val ignoreFilter = IgnoreTokensFilter()
            val elements = header.filterForTextTokensExcluding(*MarkdownPsiUtils.inlineTypes.toTypedArray())

            ignoreFilter.populateMd(elements)

            addAll(ignoreFilter.filter(SanitizingGrammarChecker.default.check(elements)))

            ProgressManager.checkCanceled()
        }

        //simple paragraphs
        for (paragraph in file.filterFor<PsiElement> { it.node.hasType(MarkdownElementTypes.PARAGRAPH) }) {
            val ignoreFilter = IgnoreTokensFilter()
            val elements = paragraph.filterForTextTokensExcluding(*MarkdownPsiUtils.inlineTypes.toTypedArray(), MarkdownElementTypes.LIST_ITEM)

            ignoreFilter.populateMd(elements)

            addAll(ignoreFilter.filter(SanitizingGrammarChecker.default.check(elements)))

            ProgressManager.checkCanceled()
        }

        //code elements
        for (code in file.filterFor<PsiElement> { MarkdownPsiUtils.isCode(it) }) {
            val elements = code.filterForTokens<PsiElement>(MarkdownTokenTypes.TEXT)

            addAll(SanitizingGrammarChecker.default.check(elements).spellcheckOnly())

            ProgressManager.checkCanceled()
        }

        for (item in file.filterFor<PsiElement> { it.node.hasType(MarkdownElementTypes.LIST_ITEM) && it.node.noParentOfTypes(TokenSet.create(MarkdownElementTypes.LIST_ITEM)) }) {
            val ignoreFilter = IgnoreTokensFilter()
            val elements = item.filterForTextTokensExcluding(*MarkdownPsiUtils.inlineTypes.toTypedArray())

            ignoreFilter.populateMd(elements)

            addAll(ignoreFilter.filter(SanitizingGrammarChecker.default.check(elements).filter {
                it.info.category !in bulletsIgnoredCategories
            }))

            ProgressManager.checkCanceled()
        }
    }

    private fun IgnoreTokensFilter.populateMd(elements: Collection<PsiElement>) {
        populate(elements, addAsLeftIf = {
            val nextElement = it.traverse(take = { it.nextSibling ?: it.parent.firstChild }, cond = { MarkdownPsiUtils.isWhitespace(it) || MarkdownPsiUtils.isEol(it) })
            nextElement != null && MarkdownPsiUtils.isInline(nextElement)
        }, addAsRightIf = {
            val nextElement = it.traverse(take = { it.prevSibling ?: it.parent.lastChild }, cond = { MarkdownPsiUtils.isWhitespace(it) || MarkdownPsiUtils.isEol(it) })
            nextElement != null && MarkdownPsiUtils.isInline(nextElement)
        })
    }

}
