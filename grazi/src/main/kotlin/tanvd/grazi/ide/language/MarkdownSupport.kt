package tanvd.grazi.ide.language


import com.intellij.lang.ASTNode
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.TreeUtil
import com.intellij.psi.tree.TokenSet
import org.intellij.plugins.markdown.lang.MarkdownElementTypes
import org.intellij.plugins.markdown.lang.psi.impl.*
import tanvd.grazi.grammar.SanitizingGrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.utils.buildSet
import tanvd.grazi.utils.filterFor

class MarkdownSupport : LanguageSupport() {
    companion object {
        val bulletsIgnoredCategories = listOf(Typo.Category.CASING)
        val noSpellcheckingType = arrayOf(MarkdownElementTypes.CODE_BLOCK, MarkdownElementTypes.CODE_FENCE,
                MarkdownElementTypes.CODE_SPAN, MarkdownElementTypes.LINK_DESTINATION)

        private fun ASTNode.noParentOfTypes(tokenSet: TokenSet) = TreeUtil.findParent(this, tokenSet) == null

    }

    override fun isSupported(file: PsiFile): Boolean {
        return file is MarkdownFile
    }

    override fun check(file: PsiFile) = buildSet<Typo> {
        for (header in file.filterFor<MarkdownHeaderImpl> { it.node.noParentOfTypes(TokenSet.create(*noSpellcheckingType)) }) {
            addAll(SanitizingGrammarChecker.default.check(header))

            ProgressManager.checkCanceled()
        }

        for (paragraph in file.filterFor<MarkdownParagraphImpl> { it.node.noParentOfTypes(TokenSet.create(*noSpellcheckingType, MarkdownElementTypes.LIST_ITEM)) }) {
            addAll(SanitizingGrammarChecker.default.check(paragraph))

            ProgressManager.checkCanceled()
        }

        for (item in file.filterFor<MarkdownListItemImpl> { it.node.noParentOfTypes(TokenSet.create(*noSpellcheckingType, MarkdownElementTypes.LIST_ITEM)) }) {
            addAll(SanitizingGrammarChecker.default.check(item).filter {
                it.info.category !in bulletsIgnoredCategories
            })

            ProgressManager.checkCanceled()
        }
    }
}
