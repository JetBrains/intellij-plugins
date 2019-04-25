package tanvd.grazi.utils

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.TreeUtil
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil

inline fun <reified T : PsiElement> PsiElement.filterFor(filter: (T) -> Boolean = { true }): List<T> = PsiTreeUtil.collectElementsOfType(this, T::class.java).filter(filter).distinct()


fun ASTNode.noParentOfTypes(vararg tokens: IElementType) = noParentOfTypes(TokenSet.create(*tokens))
fun ASTNode.noParentOfTypes(tokenSet: TokenSet) = TreeUtil.findParent(this, tokenSet) == null

fun ASTNode.hasParentOfTypes(vararg tokens: IElementType) = hasParentOfTypes(TokenSet.create(*tokens))
fun ASTNode.hasParentOfTypes(tokenSet: TokenSet) = TreeUtil.findParent(this, tokenSet) != null
fun ASTNode.hasType(vararg tokens: IElementType) = this.elementType in tokens

inline fun <reified T : PsiElement> PsiElement.filterForTokens(vararg tokens: IElementType): List<T> = filterFor { token ->
    tokens.contains(token.node.elementType)
}
