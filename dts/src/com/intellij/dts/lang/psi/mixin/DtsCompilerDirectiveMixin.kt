package com.intellij.dts.lang.psi.mixin

import com.intellij.dts.lang.DtsAffiliation
import com.intellij.dts.lang.DtsTokenSets
import com.intellij.dts.lang.psi.DtsCompilerDirective
import com.intellij.dts.lang.psi.DtsStatementKind
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.dts.util.DtsUtil
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType

abstract class DtsCompilerDirectiveMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsCompilerDirective {
  override val dtsDirective: PsiElement
    get() = DtsUtil.children(this).first { DtsTokenSets.compilerDirectives.contains(it.elementType) }

  override val dtsDirectiveType: IElementType
    get() = dtsDirective.node.elementType

  override val dtsIsComplete: Boolean
    get() = !PsiTreeUtil.hasErrorElements(this)

  override val dtsStatementKind: DtsStatementKind
    get() {
      return when (dtsDirectiveType) {
        DtsTypes.DELETE_PROP -> DtsStatementKind.PROPERTY
        DtsTypes.DELETE_NODE -> DtsStatementKind.NODE
        else -> DtsStatementKind.UNKNOWN
      }
    }

  override val dtsDirectiveArgs: List<PsiElement>
    get() {
      return DtsUtil.children(this)
        .dropWhile { !DtsTokenSets.compilerDirectives.contains(it.elementType) }
        .drop(1)
        .toList()
    }

  override val dtsAffiliation: DtsAffiliation
    get() {
      return when (dtsDirectiveType) {
        DtsTypes.MEMRESERVE, DtsTypes.V1, DtsTypes.PLUGIN, DtsTypes.OMIT_NODE -> DtsAffiliation.ROOT
        DtsTypes.DELETE_PROP -> DtsAffiliation.NODE
        DtsTypes.DELETE_NODE -> {
          when (dtsDirectiveArgs.firstOrNull()?.elementType) {
            DtsTypes.NAME -> DtsAffiliation.NODE
            DtsTypes.P_HANDLE -> DtsAffiliation.ROOT
            else -> DtsAffiliation.UNKNOWN
          }
        }
        else -> DtsAffiliation.UNKNOWN
      }
    }
}
