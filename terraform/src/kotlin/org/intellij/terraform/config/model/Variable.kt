/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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