package tanvd.grazi.ide.language.markdown


import com.intellij.psi.PsiElement
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes
import org.jetbrains.kotlin.idea.editor.fixers.range
import org.rust.lang.core.psi.ext.elementType
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.parents

class MarkdownSupport : LanguageSupport() {
    companion object {
        private val bulletsIgnoredCategories = listOf(Typo.Category.CASING)
    }

    override fun isRelevant(element: PsiElement): Boolean {
        return MarkdownPsiUtils.isHeader(element) || MarkdownPsiUtils.isParagraph(element) || MarkdownPsiUtils.isCode(element)
    }

    override fun check(element: PsiElement): Set<Typo> {
        return GrammarChecker.default.check(listOf(element), indexBasedIgnore = { token, index ->
            token.children.find { element -> index in element.range }?.elementType == MarkdownTokenTypes.TEXT
        }).filter { typo -> !(typo.isTypoInOuterListItem() && typo.info.category in bulletsIgnoredCategories) }.toSet()
    }

    private fun Typo.isTypoInOuterListItem() = this.location.element?.parents()?.any { element ->
        element.node?.let { MarkdownPsiUtils.isOuterListItem(element) } ?: false
    } ?: false
}
