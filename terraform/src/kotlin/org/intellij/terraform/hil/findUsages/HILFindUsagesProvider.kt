/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
package org.intellij.terraform.hil.findUsages

import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import org.intellij.terraform.hil.psi.HILLexer
import org.intellij.terraform.hil.psi.ILProperty
import org.intellij.terraform.hil.psi.ILVariable

class HILFindUsagesProvider : FindUsagesProvider {
  override fun getWordsScanner(): WordsScanner {
    return HILWordsScanner(HILLexer())
  }

  override fun canFindUsagesFor(psiElement: PsiElement): Boolean {
    return psiElement is ILVariable || psiElement is ILProperty
  }

  override fun getHelpId(psiElement: PsiElement): String? {
    return null
  }

  override fun getType(element: PsiElement): String {
    if (element is ILVariable)
      return "hil.variable"
    if (element is ILProperty)
      return "hil.property"
    return ""
  }

  override fun getDescriptiveName(element: PsiElement): String {
    val name = when (element) {
      is PsiNamedElement -> element.name
      is NavigatablePsiElement -> element.name
      else -> null
    }
    return name ?: "<Not An PsiNamedElement/NavigatablePsiElement ${element.node.elementType}>"
  }

  override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
    return getDescriptiveName(element)
  }
}
