package tanvd.grazi.ide.language.kotlin

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens
import org.jetbrains.kotlin.kdoc.parser.KDocElementTypes
import org.jetbrains.kotlin.kdoc.psi.impl.KDocTag
import org.jetbrains.kotlin.lexer.KtTokens
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.*

class KDocSupport : LanguageSupport() {
    companion object {
        private val tagsIgnoredCategories = listOf(Typo.Category.CASING)

        private fun isInTag(token: PsiElement) = token.node.hasParentOfTypes(KDocElementTypes.KDOC_TAG)
        private fun isTag(token: PsiElement) = token.node.elementType == KDocElementTypes.KDOC_TAG
        private fun isBody(token: PsiElement) = !isTag(token) && !isInTag(token) && token.parent.node.elementType == KDocElementTypes.KDOC_SECTION
        private fun isIdentifier(token: PsiElement) = token.parent.node.elementType == KDocElementTypes.KDOC_NAME
    }

    override fun isRelevant(element: PsiElement) = element is KDocTag

    /**
     * Checks:
     * * Body lines -- lines, which has KDocSection as parent and are not tags (or in tags).
     *   Includes identifiers (elements with a parent KDOC_NAME)
     * * Tag lines -- lines, which has a parent of type KDOC_TAG. Includes identifiers.
     *   Drops first identifier, cause it is just link to construct according to JavaDoc style guide.
     *
     * Note: Tag lines ignores casing.
     */
    override fun check(element: PsiElement) = when {
        isTag(element) -> {
            GrammarChecker.default.check(element.filterForTokens<PsiElement>(KDocTokens.TEXT, KtTokens.IDENTIFIER)
                    .dropFirstIf { isIdentifier(it) })
                    .filterNotToSet { it.info.category in tagsIgnoredCategories }
        }
        else -> {
            GrammarChecker.default.check(element.filterForTokens<PsiElement>(KDocTokens.TEXT, KtTokens.IDENTIFIER)
                    .filterToSet { !isInTag(it) && (isBody(it) || isIdentifier(it)) })
        }
    }
}
