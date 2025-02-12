// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.findUsages

import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.createHclLexer
import org.intellij.terraform.hcl.psi.*

open class HCLFindUsagesProvider : FindUsagesProvider {
  override fun getWordsScanner(): WordsScanner? {
    return HCLWordsScanner(createHclLexer())
  }

  override fun canFindUsagesFor(psiElement: PsiElement): Boolean {
    if (psiElement !is PsiNamedElement || psiElement !is HCLElement) {
      return false
    }
    if (psiElement is HCLIdentifier) {
      if (HCLPsiUtil.isPropertyValue(psiElement)) {
        if (TfPsiPatterns.DynamicBlockIterator.accepts(psiElement.parent)) {
          return true
        }
      } else if (TfPsiPatterns.ForVariable.accepts(psiElement)) {
        return true
      }
    }
    if (TfPsiPatterns.RootBlock.accepts(psiElement)) {
      if (TfPsiPatterns.LocalsRootBlock.accepts(psiElement)) {
        return false
      }
      if (TfPsiPatterns.TerraformRootBlock.accepts(psiElement)) {
        return false
      }
    }
    return true
  }

  override fun getHelpId(psiElement: PsiElement): String? {
    return null
  }

  override fun getType(element: PsiElement): String {
    if (TfPsiPatterns.TerraformFile.accepts(element.containingFile)) {
      val parent = element.parent

      if (element is HCLBlock) {
        @NlsSafe val type = element.getNameElementUnquoted(0)
        if (TfPsiPatterns.RootBlock.accepts(element)) {
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
        if (TfPsiPatterns.Backend.accepts(element)){
          return HCLBundle.message("HCLFindUsagesProvider.type.backend.configuration")
        }
        return "$type"
      }

      if (element is HCLProperty) {
        if (TfPsiPatterns.LocalsRootBlock.accepts(parent?.parent)) {
          return HCLBundle.message("HCLFindUsagesProvider.type.local.value")
        }
        return HCLBundle.message("HCLFindUsagesProvider.type.property")
      }
      if (element is HCLIdentifier) {
        if (HCLPsiUtil.isPropertyValue(element)) {
          if (TfPsiPatterns.DynamicBlockIterator.accepts(element.parent)) {
            return HCLBundle.message("HCLFindUsagesProvider.type.dynamic.iterator")
          }
        } else if (TfPsiPatterns.ForVariable.accepts(element)) {
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
