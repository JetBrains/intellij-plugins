package tanvd.grazi.ide.language.markdown

import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import org.intellij.plugins.markdown.lang.MarkdownElementTypes
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes
import tanvd.grazi.utils.hasType
import tanvd.grazi.utils.noParentOfTypes

object MarkdownPsiUtils {
    private val headerTypes = setOf(MarkdownElementTypes.ATX_1, MarkdownElementTypes.ATX_2, MarkdownElementTypes.ATX_3,
            MarkdownElementTypes.ATX_4, MarkdownElementTypes.ATX_5, MarkdownElementTypes.ATX_6)
    private val linkTypes = setOf(
            MarkdownElementTypes.LINK_DEFINITION, MarkdownElementTypes.LINK_LABEL, MarkdownElementTypes.LINK_DESTINATION,
            MarkdownElementTypes.LINK_TITLE, MarkdownElementTypes.LINK_TEXT, MarkdownElementTypes.LINK_COMMENT,
            MarkdownElementTypes.FULL_REFERENCE_LINK, MarkdownElementTypes.SHORT_REFERENCE_LINK,
            MarkdownElementTypes.AUTOLINK, MarkdownElementTypes.INLINE_LINK)
    private val textTypes = setOf(
            MarkdownTokenTypes.TEXT, MarkdownTokenTypes.WHITE_SPACE, MarkdownTokenTypes.SINGLE_QUOTE,
            MarkdownTokenTypes.DOUBLE_QUOTE, MarkdownTokenTypes.EXCLAMATION_MARK, MarkdownTokenTypes.COLON,
            MarkdownTokenTypes.LPAREN, MarkdownTokenTypes.RPAREN
    )
    private val codeTypes = setOf(MarkdownElementTypes.CODE_FENCE, MarkdownElementTypes.CODE_BLOCK, MarkdownElementTypes.CODE_SPAN)
    private val inlineTypes = linkTypes + codeTypes

    fun isParagraph(element: PsiElement) = element.node?.hasType(MarkdownElementTypes.PARAGRAPH) ?: false
    fun isHeader(element: PsiElement) = element.node?.hasType(headerTypes) ?: false
    fun isInline(element: PsiElement) = element.node?.hasType(inlineTypes) ?: false
    fun isCode(element: PsiElement) = element.node?.hasType(codeTypes) ?: false
    fun isText(element: PsiElement) = element.node?.hasType(textTypes) ?: false
    fun isOuterListItem(element: PsiElement) = element.node?.hasType(MarkdownElementTypes.LIST_ITEM) ?: false
            && element.node?.noParentOfTypes(TokenSet.create(MarkdownElementTypes.LIST_ITEM)) ?: false


    fun isWhitespace(element: PsiElement) = element.node.hasType(MarkdownTokenTypes.WHITE_SPACE)
    fun isEOL(element: PsiElement) = element.node.hasType(MarkdownTokenTypes.EOL)
}
