package tanvd.grazi.ide.language.kotlin

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens
import org.jetbrains.kotlin.kdoc.parser.KDocElementTypes
import org.jetbrains.kotlin.kdoc.psi.impl.KDocSection
import org.jetbrains.kotlin.kdoc.psi.impl.KDocTag
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtFile
import tanvd.grazi.grammar.SanitizingGrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.*

class KDocSupport : LanguageSupport() {
    companion object {
        val tagsIgnoredCategories = listOf(Typo.Category.CASING)

        private fun isInTag(token: PsiElement) = token.node.hasParentOfTypes(KDocElementTypes.KDOC_TAG)
        private fun isTag(token: PsiElement) = token.node.elementType == KDocElementTypes.KDOC_TAG
        private fun isBody(token: PsiElement) = !isTag(token) && !isInTag(token) && token.parent.node.elementType == KDocElementTypes.KDOC_SECTION
        private fun isIdentifier(token: PsiElement) = token.parent.node.elementType == KDocElementTypes.KDOC_NAME
    }

    override fun isSupported(file: PsiFile): Boolean {
        return file is KtFile
    }

    /**
     * Checks:
     * * Body lines -- lines, which has KDocSection as parent and are not tags (or in tags).
     *   Includes identifiers (elements with a parent KDOC_NAME)
     * * Tag lines -- lines, which has a parent of type KDOC_TAG. Includes identifiers.
     *   Drops first identifier, cause it is just link to construct according to JavaDoc style guide.
     *
     * Note: Tag lines ignores casing.
     */
    override fun check(file: PsiFile) = buildSet<Typo> {
        for (doc in file.filterFor<KDocSection>()) {
            for (tagLine in doc.filterFor<KDocTag> { isTag(it) }) {
                addAll(SanitizingGrammarChecker.default.check(tagLine.filterForTokens<PsiElement>(KDocTokens.TEXT, KtTokens.IDENTIFIER)
                        .dropFirstIf { isIdentifier(it) })
                        .filterNot { it.info.category in tagsIgnoredCategories })
            }
            for (textBlock in doc.filterFor<KDocTag> { !isTag(it) }) {
                addAll(SanitizingGrammarChecker.default.check(textBlock.filterForTokens<PsiElement>(KDocTokens.TEXT, KtTokens.IDENTIFIER)
                        .filter { !isInTag(it) && (isBody(it) || isIdentifier(it)) }))
            }
            ProgressManager.checkCanceled()
        }
    }
}
