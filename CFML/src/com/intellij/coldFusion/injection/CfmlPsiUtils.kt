// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.intellij.coldFusion.injection

import com.intellij.coldFusion.model.psi.CfmlLeafPsiElement
import com.intellij.coldFusion.model.psi.impl.CfmlTagImpl
import com.intellij.psi.PsiElement

internal fun PsiElement.traverse(): Iterable<PsiElement> {
  return Iterable {
    var pivotPsiElement: PsiElement = this@traverse
    object : Iterator<PsiElement> {
      override fun hasNext(): Boolean = pivotPsiElement.nextSibling != null
      override fun next(): PsiElement {
        pivotPsiElement = pivotPsiElement.nextSibling
        return pivotPsiElement
      }
    }
  }
}

internal fun <T> MutableList<T>.addNotNull(t: T?) {
  t ?: return
  this.add(t)
}

internal fun PsiElement.isCfifTag(): Boolean {
  return (this is CfmlTagImpl && this.tagName == "cfif")
}

internal fun getFirstCfifValue(cfifTag: CfmlTagImpl): PsiElement? {
  return cfifTag.firstChild.traverse().firstOrNull { it is CfmlLeafPsiElement }
}

internal fun PsiElement.isCfQueryTag(): Boolean {
  return this.isCfmlTag("cfquery")
}

internal fun PsiElement.isCfIfElseTagInsideCfQuery(): Boolean {
  return this.isCfmlTag("cfifelse") && this.isTagInsideCfQuery()
}

internal fun PsiElement.isCfElseTagInsideCfQuery(): Boolean {
  return this.isCfmlTag("cfelse") && this.isTagInsideCfQuery()
}

private fun PsiElement.isTagInsideCfQuery(): Boolean {
  return this.parent?.parent?.isCfQueryTag() ?: false
}

private fun PsiElement.isCfmlTag(tagName: String): Boolean {
  return (this is CfmlTagImpl && this.name != null && this.name?.toLowerCase() == tagName)
}