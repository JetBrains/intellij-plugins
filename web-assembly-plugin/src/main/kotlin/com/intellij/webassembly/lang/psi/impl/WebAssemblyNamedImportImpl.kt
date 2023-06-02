package com.intellij.webassembly.lang.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.webassembly.lang.psi.WebAssemblyElementFactory
import com.intellij.webassembly.lang.psi.WebAssemblyNamedElement
import com.intellij.webassembly.lang.psi.WebAssemblyTypes


open class WebAssemblyNamedImportImpl(node: ASTNode) : ASTWrapperPsiElement(node), WebAssemblyNamedElement {
  private val importdescNode: ASTNode? = node.findChildByType(WebAssemblyTypes.IMPORTDESC)

  override fun getNameIdentifier(): PsiElement? = importdescNode?.findChildByType(
    WebAssemblyTypes.IDENTIFIER)?.psi

  override fun setName(name: String): PsiElement? {
    val newIdent: ASTNode = WebAssemblyElementFactory
      .createImport(node.psi.project, name)
      .findChildByType(WebAssemblyTypes.IMPORTDESC)
      ?.findChildByType(WebAssemblyTypes.IDENTIFIER) as ASTNode

    importdescNode
      ?.findChildByType(WebAssemblyTypes.IDENTIFIER)
      ?.let {
        node.replaceChild(it, newIdent)
        return node.psi
      }

    (importdescNode?.findChildByType(WebAssemblyTypes.FUNCKEY)
     ?: importdescNode?.findChildByType(WebAssemblyTypes.MEMORYKEY)
     ?: importdescNode?.findChildByType(WebAssemblyTypes.TABLEKEY)
     ?: importdescNode?.findChildByType(WebAssemblyTypes.GLOBALKEY))
      ?.treeNext
      ?.let { node.addChild(newIdent, it) }

    return node.psi
  }
}