package org.jetbrains.webstorm.lang.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.webstorm.lang.WebAssemblyReference
import org.jetbrains.webstorm.lang.WebAssemblyUtil
import org.jetbrains.webstorm.lang.psi.*

open class WebAssemblyReferencedTableInitInstrImpl(node: ASTNode) : ASTWrapperPsiElement(node), WebAssemblyReferencedElement {
    override fun getReferences(): Array<PsiReference> {
        val result: MutableList<PsiReference> = mutableListOf()

        val children: Array<ASTNode> = node.getChildren(TokenSet.create(WebAssemblyTypes.IDX))

        PsiTreeUtil.getParentOfType(node.psi, WebAssemblyModulefield::class.java)?.let { parent ->
            when (children.size) {
                1 -> {
                    result.add(WebAssemblyReference(node, children[0], WebAssemblyUtil.findModulefield(WebAssemblyTypes.ELEM, parent)))
                }
                2 -> {
                    result.add(WebAssemblyReference(node, children[0], WebAssemblyUtil.findImportedModulefield(WebAssemblyTypes.TABLE, parent)))
                    result.add(WebAssemblyReference(node, children[1], WebAssemblyUtil.findModulefield(WebAssemblyTypes.ELEM, parent)))
                }
                else -> {}
            }
        }

        return result.toTypedArray()
    }

    override fun getReference(): PsiReference? = if (references.isNotEmpty()) references[0] else null
}