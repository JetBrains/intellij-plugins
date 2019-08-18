package tanvd.grazi.ide.language.java

import com.intellij.psi.JavaDocTokenType
import com.intellij.psi.PsiElement
import com.intellij.psi.javadoc.*
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.filterFor
import tanvd.kex.buildSet

class JDocSupport : LanguageSupport() {
    companion object {
        private val tagsIgnoredCategories = listOf(Typo.Category.CASING)

        private fun isTag(token: PsiDocToken) = token.parent is PsiDocTag
        private fun isCodeTag(token: PsiDocToken) = isTag(token) && ((token.parent as PsiDocTag).nameElement.text == "@code")
    }

    override fun isRelevant(element: PsiElement) = element is PsiDocComment

    /**
     * Checks:
     * * Body lines -- lines, which are a DOC_COMMENT_DATA, and their parent is not PsiDocTag
     * * Tag lines -- lines, which are a DOC_COMMENT_DATA, and their parent is PsiDocTag
     *
     * Note: Tag lines ignores casing.
     */
    override fun check(element: PsiElement) = buildSet<Typo> {
        require(element is PsiDocComment) { "Got non PsiDocComment in JDocSupport" }

        val allDocTokens = element.filterFor<PsiDocToken> { it.tokenType == JavaDocTokenType.DOC_COMMENT_DATA }

        addAll(GrammarChecker.default.check(allDocTokens.filterNot { isTag(it) }))
        addAll(GrammarChecker.default.check(allDocTokens.filter { isTag(it) && !isCodeTag(it) })
                .filterNot { it.info.category in tagsIgnoredCategories })
    }
}
