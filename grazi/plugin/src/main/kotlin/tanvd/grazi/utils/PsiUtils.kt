package tanvd.grazi.utils

import com.intellij.lang.ASTNode
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.TreeUtil
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil

inline fun <reified T : PsiElement> PsiElement.filterFor(filter: (T) -> Boolean = { true }): List<T> = PsiTreeUtil.collectElementsOfType(this, T::class.java).filter(filter).distinct()

fun ASTNode.noParentOfTypes(tokenSet: TokenSet) = TreeUtil.findParent(this, tokenSet) == null

fun ASTNode.hasParentOfTypes(vararg tokens: IElementType) = hasParentOfTypes(TokenSet.create(*tokens))
fun ASTNode.hasParentOfTypes(tokenSet: TokenSet) = TreeUtil.findParent(this, tokenSet) != null

fun ASTNode.hasType(vararg tokens: IElementType) = hasType(tokens.toSet())
fun ASTNode.hasType(tokens: Set<IElementType>) = this.elementType in tokens

inline fun <reified T : PsiElement> PsiElement.filterForTokens(vararg tokens: IElementType): List<T> = filterFor { token ->
    tokens.contains(token.node.elementType)
}

inline fun <reified T : PsiElement> T.toPointer(): SmartPsiElementPointer<T> = SmartPointerManager.createPointer(this)

fun PsiElement.parents(): Sequence<PsiElement> = generateSequence(this) { it.parent }

fun PsiElement.isInjectedFragment(): Boolean {
    val host = this.parents().filter { it is PsiLanguageInjectionHost }.firstOrNull() as? PsiLanguageInjectionHost ?: return false
    var isInjected = false
    InjectedLanguageManager.getInstance(project).enumerate(host) { _, _ ->
        isInjected = true
    }
    return isInjected
}
