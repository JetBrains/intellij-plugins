package tanvd.grazi.ide.language.markdown


import com.intellij.psi.PsiElement
import org.intellij.plugins.markdown.lang.MarkdownElementTypes
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes
import tanvd.grazi.grammar.*
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.spellcheckOnly
import tanvd.grazi.utils.traverse

class MarkdownSupport : LanguageSupport() {
    companion object {
        private val bulletsIgnoredCategories = listOf(Typo.Category.CASING)
    }

    override fun isRelevant(element: PsiElement): Boolean {
        return MarkdownPsiUtils.isHeader(element) || MarkdownPsiUtils.isParagraph(element)
                || MarkdownPsiUtils.isCode(element) || MarkdownPsiUtils.isOuterListItem(element)
    }

    override fun check(element: PsiElement): Set<Typo> {
        return when {
            MarkdownPsiUtils.isHeader(element) -> {
                val ignoreFilter = TokensFilter()
                val elements = element.filterForTextTokensExcluding(*MarkdownPsiUtils.inlineTypes.toTypedArray())

                ignoreFilter.populateMd(elements)

                ignoreFilter.filter(GrammarChecker.default.check(elements))

            }
            MarkdownPsiUtils.isCode(element) -> {
                val elements = element.filterForTokens<PsiElement>(MarkdownTokenTypes.TEXT)

                GrammarChecker.default.check(elements).spellcheckOnly()
            }
            MarkdownPsiUtils.isOuterListItem(element) -> {
                val ignoreFilter = TokensFilter()
                val elements = element.filterForTextTokensExcluding(*MarkdownPsiUtils.inlineTypes.toTypedArray())

                ignoreFilter.populateMd(elements)

                ignoreFilter.filter(GrammarChecker.default.check(elements).filter {
                    it.info.category !in bulletsIgnoredCategories
                })
            }
            MarkdownPsiUtils.isParagraph(element) -> {
                val ignoreFilter = TokensFilter()
                val elements = element.filterForTextTokensExcluding(*MarkdownPsiUtils.inlineTypes.toTypedArray(), MarkdownElementTypes.LIST_ITEM)

                ignoreFilter.populateMd(elements)

                ignoreFilter.filter(GrammarChecker.default.check(elements))
            }
            else -> {
                emptySet()
            }
        }
    }

    private fun TokensFilter.populateMd(elements: Collection<PsiElement>) {
        populate(elements, addAsLeftIf = {
            val nextElement = it.traverse(take = { it.nextSibling ?: it.parent.firstChild },
                    cond = { MarkdownPsiUtils.isWhitespace(it) || MarkdownPsiUtils.isEOL(it) })
            nextElement != null && MarkdownPsiUtils.isInline(nextElement)
        }, addAsRightIf = {
            val nextElement = it.traverse(take = { it.prevSibling ?: it.parent.lastChild },
                    cond = { MarkdownPsiUtils.isWhitespace(it) || MarkdownPsiUtils.isEOL(it) })
            nextElement != null && MarkdownPsiUtils.isInline(nextElement)
        })
    }

}
