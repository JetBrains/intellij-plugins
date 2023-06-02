package com.intellij.webassembly.lang.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.tree.TokenSet
import com.intellij.webassembly.lang.WebAssemblyReference
import com.intellij.webassembly.lang.psi.WebAssemblyElementFactory
import com.intellij.webassembly.lang.psi.WebAssemblyNamedReferencedElement
import com.intellij.webassembly.lang.psi.WebAssemblyTypes

open class WebAssemblyNamedReferencedElementImpl(node: ASTNode)
  : ASTWrapperPsiElement(node), WebAssemblyNamedReferencedElement {

  override fun getReferences(): Array<PsiReference> {
    val result: MutableList<PsiReference> = mutableListOf()

    when (node.elementType) {
      WebAssemblyTypes.EXPORT -> {
        node.findChildByType(
          WebAssemblyTypes.EXPORTDESC)
      }
      else -> node
    }?.getChildren(TokenSet.create(WebAssemblyTypes.IDX))
      ?.forEach {
        result.add(WebAssemblyReference(node, it))
      }

    return result.toTypedArray()
  }

  override fun getReference(): PsiReference? = if (references.isNotEmpty()) references[0] else null

  override fun getNameIdentifier(): PsiElement? {
    return node.findChildByType(WebAssemblyTypes.IDENTIFIER)?.psi
  }

  override fun setName(name: String): PsiElement? {
    val newIdent: ASTNode = WebAssemblyElementFactory
      .createElement(node.psi.project, name)
      .findChildByType(WebAssemblyTypes.IDENTIFIER) as ASTNode

    node
      .findChildByType(WebAssemblyTypes.IDENTIFIER)
      ?.let {
        node.replaceChild(it, newIdent)
        return node.psi
      }

    node.addChild(newIdent, node.firstChildNode.treeNext.treeNext)
    return node.psi
  }
}