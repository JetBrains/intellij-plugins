// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model

import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValuesManager
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLExpression

class Variable(val declaration: HCLBlock) : Block(TypeModel.Variable) {
  val name: String get() = declaration.name
  val nameIdentifier: PsiElement get() = declaration.nameIdentifier!!

  fun getDefault(): HCLExpression? {
    return declaration.`object`?.findProperty(TypeModel.Variable_Default.name)?.value
  }

  fun getTypeExpression(): HCLExpression? {
    return declaration.`object`?.findProperty(TypeModel.Variable_Type.name)?.value
  }

  fun getType(): Type? {
    val expression = getTypeExpression() ?: return null
    return CachedValuesManager.getManager(declaration.project).getCachedValue(expression, VariableTypeCachedValueProvider(expression))
  }

  fun getCombinedType(): Type? {
    val typeType = getType()
    val defType = getDefault().getType() ?: return typeType
    if (typeType == null) return defType
    return getCommonSupertype(listOf(defType, typeType))
  }

  fun getDescription(): HCLExpression? {
    return declaration.`object`?.findProperty(TypeModel.Variable_Description.name)?.value
  }

  override fun equals(other: Any?): Boolean {
    if (other !is Variable) return false
    return declaration == other.declaration
  }

  override fun hashCode(): Int {
    return declaration.hashCode()
  }
}