package tanvd.grazi.ide.language.markdown


import com.intellij.lang.Language
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import org.intellij.plugins.markdown.lang.*
import tanvd.grazi.grammar.*
import tanvd.grazi.ide.language.LanguageElementSupport
import tanvd.grazi.utils.spellcheckOnly
import tanvd.grazi.utils.traverse
import tanvd.kex.buildSet

class MarkdownSupport : LanguageElementSupport() {
    companion object {
        val bulletsIgnoredCategories = listOf(Typo.Category.CASING)
    }

    override fun isSupported(language: Language): Boolean {
        return language is MarkdownLanguage
    }

    override fun isRelevant(element: PsiElement): Boolean {
        return MarkdownPsiUtils.isHeader(element) || MarkdownPsiUtils.isParagraph(element)
                || MarkdownPsiUtils.isCode(element) || MarkdownPsiUtils.isOuterListItem(element)
    }

    override fun check(element: PsiElement) = buildSet<Typo> {
        when {
            MarkdownPsiUtils.isHeader(element) -> {
                val ignoreFilter = IgnoreTokensFilter()
                val elements = element.filterForTextTokensExcluding(*MarkdownPsiUtils.inlineTypes.toTypedArray())

                ignoreFilter.populateMd(elements)

                addAll(ignoreFilter.filter(SanitizingGrammarChecker.default.check(elements)))

            }
            MarkdownPsiUtils.isParagraph(element) -> {
                val ignoreFilter = IgnoreTokensFilter()
                val elements = element.filterForTextTokensExcluding(*MarkdownPsiUtils.inlineTypes.toTypedArray(), MarkdownElementTypes.LIST_ITEM)

                ignoreFilter.populateMd(elements)

                addAll(ignoreFilter.filter(SanitizingGrammarChecker.default.check(elements)))

                ProgressManager.checkCanceled()
            }
            MarkdownPsiUtils.isCode(element) -> {
                val elements = element.filterForTokens<PsiElement>(MarkdownTokenTypes.TEXT)

                addAll(SanitizingGrammarChecker.default.check(elements).spellcheckOnly())

                ProgressManager.checkCanceled()
            }
            MarkdownPsiUtils.isOuterListItem(element) -> {
                val ignoreFilter = IgnoreTokensFilter()
                val elements = element.filterForTextTokensExcluding(*MarkdownPsiUtils.inlineTypes.toTypedArray())

                ignoreFilter.populateMd(elements)

                addAll(ignoreFilter.filter(SanitizingGrammarChecker.default.check(elements).filter {
                    it.info.category !in bulletsIgnoredCategories
                }))
            }
        }
    }

    private fun IgnoreTokensFilter.populateMd(elements: Collection<PsiElement>) {
        populate(elements, addAsLeftIf = {
            val nextElement = it.traverse(take = { it.nextSibling ?: it.parent.firstChild },
                    cond = { MarkdownPsiUtils.isWhitespace(it) || MarkdownPsiUtils.isEol(it) })
            nextElement != null && MarkdownPsiUtils.isInline(nextElement)
        }, addAsRightIf = {
            val nextElement = it.traverse(take = { it.prevSibling ?: it.parent.lastChild },
                    cond = { MarkdownPsiUtils.isWhitespace(it) || MarkdownPsiUtils.isEol(it) })
            nextElement != null && MarkdownPsiUtils.isInline(nextElement)
        })
    }

}
