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
package org.intellij.terraform.hcl.findUsages

import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.HCLParserDefinition
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.config.patterns.TerraformPatterns

open class HCLFindUsagesProvider : FindUsagesProvider {
  override fun getWordsScanner(): WordsScanner? {
    return HCLWordsScanner(HCLParserDefinition.createLexer())
  }

  override fun canFindUsagesFor(psiElement: PsiElement): Boolean {
    if (psiElement !is PsiNamedElement || psiElement !is HCLElement) {
      return false
    }
    if (psiElement is HCLIdentifier) {
      if (HCLPsiUtil.isPropertyValue(psiElement)) {
        if (TerraformPatterns.DynamicBlockIterator.accepts(psiElement.parent)) {
          return true
        }
      } else if (TerraformPatterns.ForVariable.accepts(psiElement)) {
        return true
      }
    }
    if (TerraformPatterns.RootBlock.accepts(psiElement)) {
      if (TerraformPatterns.LocalsRootBlock.accepts(psiElement)) {
        return false
      }
      if (TerraformPatterns.TerraformRootBlock.accepts(psiElement)) {
        return false
      }
    }
    return true
  }

  override fun getHelpId(psiElement: PsiElement): String? {
    return null
  }

  override fun getType(element: PsiElement): String {
    if (TerraformPatterns.TerraformFile.accepts(element.containingFile)) {
      val parent = element.parent

      if (element is HCLBlock) {
        @NlsSafe val type = element.getNameElementUnquoted(0)
        if (TerraformPatterns.RootBlock.accepts(element)) {
          when (type) {
            "module" -> return HCLBundle.message("HCLFindUsagesProvider.type.module")
            "variable" -> return HCLBundle.message("HCLFindUsagesProvider.type.variable")
            "output" -> return HCLBundle.message("HCLFindUsagesProvider.type.output.value")
            "provider" -> return HCLBundle.message("HCLFindUsagesProvider.type.provider")
            "resource" -> return HCLBundle.message("HCLFindUsagesProvider.type.resource")
            "data" -> return HCLBundle.message("HCLFindUsagesProvider.type.data.source")

            "terraform" -> return HCLBundle.message("HCLFindUsagesProvider.type.terraform.configuration")
            "locals" -> return HCLBundle.message("HCLFindUsagesProvider.type.local.values")
          }
        }
        if (TerraformPatterns.Backend.accepts(element)){
          return HCLBundle.message("HCLFindUsagesProvider.type.backend.configuration")
        }
        return "$type"
      }

      if (element is HCLProperty) {
        if (TerraformPatterns.LocalsRootBlock.accepts(parent?.parent)) {
          return HCLBundle.message("HCLFindUsagesProvider.type.local.value")
        }
        return HCLBundle.message("HCLFindUsagesProvider.type.property")
      }
      if (element is HCLIdentifier) {
        if (HCLPsiUtil.isPropertyValue(element)) {
          if (TerraformPatterns.DynamicBlockIterator.accepts(element.parent)) {
            return HCLBundle.message("HCLFindUsagesProvider.type.dynamic.iterator")
          }
        } else if (TerraformPatterns.ForVariable.accepts(element)) {
          return HCLBundle.message("HCLFindUsagesProvider.type.for.loop.variable")
        }
      }
    }

    if (element is HCLBlock) {
      return HCLBundle.message("HCLFindUsagesProvider.type.named.block", element.getNameElementUnquoted(0))
    }
    if (element is HCLProperty) {
      return HCLBundle.message("HCLFindUsagesProvider.type.property")
    }
    if (element is PsiNamedElement) {
      //      return element.name ?:
      return HCLBundle.message("HCLFindUsagesProvider.type.untyped.named.element", element.javaClass.name)
    }
    return HCLBundle.message("HCLFindUsagesProvider.type.untyped.non.psi.named.element", element.node.elementType)
  }

  override fun getDescriptiveName(element: PsiElement): String {
    val name = if (element is PsiNamedElement) element.name else null
    return name ?: "<Not An PsiNamedElement ${element.node.elementType}>"
  }

  override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
    if (useFullName) {
      if (element is HCLBlock) {
        return element.fullName
      }
    }
    return getDescriptiveName(element)
  }
}
