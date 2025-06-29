package org.intellij.prisma.lang.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import org.intellij.prisma.lang.psi.PrismaStringLiteralExpression

abstract class PrismaStringLiteralExpressionMixin(node: ASTNode) : PrismaLiteralExpressionImpl(node), PrismaStringLiteralExpression {
  override fun getValue(): Any? = stringLiteral.text?.let { StringUtil.unquoteString(it) }

  override fun getReferences(): Array<out PsiReference> = CachedValuesManager.getCachedValue(this) {
    CachedValueProvider.Result.create(ReferenceProvidersRegistry.getReferencesFromProviders(this), this)
  }
}