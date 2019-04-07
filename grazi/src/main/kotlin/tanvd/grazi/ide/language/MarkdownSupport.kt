package tanvd.grazi.ide.language


import com.intellij.lang.ASTNode
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.TreeUtil
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import org.intellij.plugins.markdown.lang.MarkdownElementTypes
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownFile
import tanvd.grazi.grammar.SanitizingGrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.utils.*

class MarkdownSupport : LanguageSupport() {
    companion object {
        val bulletsIgnoredCategories = listOf(Typo.Category.CASING)
        val noSpellcheckingType = arrayOf(MarkdownElementTypes.CODE_BLOCK, MarkdownElementTypes.CODE_FENCE,
                MarkdownElementTypes.CODE_SPAN, MarkdownElementTypes.LINK_DESTINATION)

        private fun ASTNode.noParentOfTypes(tokenSet: TokenSet) = TreeUtil.findParent(this, tokenSet) == null

        private fun ASTNode.hasParentOfTypes(tokenSet: TokenSet) = TreeUtil.findParent(this, tokenSet) != null
        private fun ASTNode.hasType(vararg tokens: IElementType) = this.elementType in tokens


        private inline fun <reified T : PsiElement> PsiElement.filterForTokens(vararg tokens: IElementType, set: TokenSet? = null): Collection<T> = filterFor { token ->
            tokens.contains(token.node.elementType) && set?.let { token.node.noParentOfTypes(it) }.orTrue()
        }
    }

    override fun isSupported(file: PsiFile): Boolean {
        return file is MarkdownFile
    }

    override fun check(file: PsiFile) = buildSet<Typo> {
        //headers
        for (header in file.filterFor<PsiElement> {
            it.node.hasType(MarkdownElementTypes.ATX_1, MarkdownElementTypes.ATX_2, MarkdownElementTypes.ATX_3,
                    MarkdownElementTypes.ATX_4, MarkdownElementTypes.ATX_5, MarkdownElementTypes.ATX_6)
        }) {
            val elements = header.filterForTokens<PsiElement>(MarkdownTokenTypes.TEXT, set = TokenSet.create(*noSpellcheckingType))
            addAll(SanitizingGrammarChecker.default.check(elements))

            ProgressManager.checkCanceled()
        }

        //simple paragraphs
        for (paragraph in file.filterFor<PsiElement> { it.node.hasType(MarkdownElementTypes.PARAGRAPH) }) {
            val elements = paragraph.filterForTokens<PsiElement>(MarkdownTokenTypes.TEXT, set = TokenSet.create(*noSpellcheckingType, MarkdownElementTypes.LIST_ITEM))
            addAll(SanitizingGrammarChecker.default.check(elements))

            ProgressManager.checkCanceled()
        }

        //code elements
        for (code in file.filterFor<PsiElement> { it.node.hasType(MarkdownElementTypes.CODE_BLOCK, MarkdownElementTypes.CODE_FENCE, MarkdownElementTypes.CODE_SPAN) }) {
            val elements = code.filterForTokens<PsiElement>(MarkdownTokenTypes.TEXT)
            addAll(SanitizingGrammarChecker.default.check(elements).spellcheckOnly())

            ProgressManager.checkCanceled()
        }

        for (item in file.filterFor<PsiElement> { it.node.hasType(MarkdownElementTypes.LIST_ITEM) && it.node.noParentOfTypes(TokenSet.create(MarkdownElementTypes.LIST_ITEM)) }) {
            val elements = item.filterForTokens<PsiElement>(MarkdownTokenTypes.TEXT, set = TokenSet.create(*noSpellcheckingType))

            addAll(SanitizingGrammarChecker.default.check(elements).filter {
                it.info.category !in bulletsIgnoredCategories
            })

            ProgressManager.checkCanceled()
        }
    }
}
