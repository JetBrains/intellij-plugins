package org.intellij.prisma.lang.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.intellij.prisma.lang.psi.PrismaElementTypes
import org.intellij.prisma.lang.psi.PrismaValueArgument

abstract class PrismaValueArgumentMixin(node: ASTNode) : PrismaArgumentImpl(node), PrismaValueArgument {

  override fun isDefault(): Boolean =
    PsiTreeUtil.skipWhitespacesAndCommentsBackward(this).elementType == PrismaElementTypes.LPAREN

}