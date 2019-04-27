package tanvd.grazi.ide.language.markdown

import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import org.intellij.plugins.markdown.lang.MarkdownElementTypes
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes
import tanvd.grazi.utils.*

object MarkdownPsiUtils {
    val headerTypes = setOf(MarkdownElementTypes.ATX_1, MarkdownElementTypes.ATX_2, MarkdownElementTypes.ATX_3,
            MarkdownElementTypes.ATX_4, MarkdownElementTypes.ATX_5, MarkdownElementTypes.ATX_6)
    val linkTypes = setOf(
            MarkdownElementTypes.LINK_DEFINITION, MarkdownElementTypes.LINK_LABEL, MarkdownElementTypes.LINK_DESTINATION,
            MarkdownElementTypes.LINK_TITLE, MarkdownElementTypes.LINK_TEXT, MarkdownElementTypes.LINK_COMMENT,
            MarkdownElementTypes.FULL_REFERENCE_LINK, MarkdownElementTypes.SHORT_REFERENCE_LINK,
            MarkdownElementTypes.AUTOLINK, MarkdownElementTypes.INLINE_LINK)
    val codeTypes = setOf(MarkdownElementTypes.CODE_FENCE, MarkdownElementTypes.CODE_BLOCK, MarkdownElementTypes.CODE_SPAN)
    val inlineTypes = linkTypes + codeTypes

    fun isHeader(element: PsiElement) = element.node.hasType(headerTypes)
    fun isInline(element: PsiElement) = element.node.hasType(inlineTypes)
    fun isCode(element: PsiElement) = element.node.hasType(codeTypes)
    fun isWhitespace(element: PsiElement) = element.node.hasType(MarkdownTokenTypes.WHITE_SPACE)
    fun isEol(element: PsiElement) = element.node.hasType(MarkdownTokenTypes.EOL)


}

inline fun <reified T : PsiElement> PsiElement.filterForTokens(vararg tokens: IElementType, excludeParents: TokenSet? = null): Collection<T> = filterFor { token ->
    tokens.contains(token.node.elementType) && excludeParents?.let { token.node.noParentOfTypes(it) }.orTrue()
}

fun PsiElement.filterForTextTokensExcluding(vararg excludeParents: IElementType) = filterForTokens<PsiElement>(MarkdownTokenTypes.TEXT,
        excludeParents = TokenSet.create(*excludeParents))
