// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.codeinsight

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PsiElementPattern
import com.intellij.patterns.PsiFilePattern
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import org.intellij.terraform.hcl.HCLTokenTypes
import org.intellij.terraform.hcl.patterns.HCLPatterns.Block
import org.intellij.terraform.hcl.patterns.HCLPatterns.IdentifierOrStringLiteral
import org.intellij.terraform.hcl.patterns.HCLPatterns.Object
import org.intellij.terraform.hcl.patterns.HCLPatterns.Property
import org.intellij.terraform.hcl.patterns.HCLPatterns.PropertyOrBlock
import org.intellij.terraform.hcl.psi.HCLFile

internal abstract class HclObjectKeyCompletionProvider : CompletionProvider<CompletionParameters>() {
  fun registerTo(contributor: CompletionContributor, filePattern: PsiFilePattern.Capture<HCLFile>) {
    contributor.extend(CompletionType.BASIC, objectKeyInEmptyObjectPattern(filePattern), this)
    contributor.extend(CompletionType.BASIC, objectKeyWhileTypingPattern(filePattern), this)
    contributor.extend(CompletionType.BASIC, objectKeyAfterPropertyPattern(filePattern), this)
  }

  // property = { <caret> }
  // property = { "<caret>" }
  // property { <caret> }
  // property { "<caret>" }
  private fun objectKeyInEmptyObjectPattern(filePattern: PsiFilePattern.Capture<HCLFile>): PsiElementPattern.Capture<PsiElement> =
    psiElement().withElementType(HCLTokenTypes.IDENTIFYING_LITERALS)
      .inFile(filePattern)
      .withParent(Object)
      .withSuperParent(2, PropertyOrBlock)
      .withSuperParent(3, Object)

  // property = { <caret>a = "" }
  // property = { "<caret>a" = "" }
  // property { <caret> = "" }
  // property { "<caret>" = "" }
  private fun objectKeyWhileTypingPattern(filePattern: PsiFilePattern.Capture<HCLFile>): PsiElementPattern.Capture<PsiElement> =
    psiElement().withElementType(HCLTokenTypes.IDENTIFYING_LITERALS)
      .inFile(filePattern)
      .withParent(IdentifierOrStringLiteral)
      .withSuperParent(2, Property)
      .withSuperParent(3, Object)
      .withSuperParent(4, PropertyOrBlock)
      .withSuperParent(5, Object)
      .withSuperParent(6, Block)

  // property = { a="" <caret> }
  private fun objectKeyAfterPropertyPattern(filePattern: PsiFilePattern.Capture<HCLFile>): PsiElementPattern.Capture<PsiElement> =
    psiElement().withElementType(HCLTokenTypes.IDENTIFYING_LITERALS)
      .inFile(filePattern)
      .withParent(psiElement(PsiErrorElement::class.java))
      .withSuperParent(2, Object)
      .withSuperParent(3, PropertyOrBlock)
      .withSuperParent(4, Object)
      .withSuperParent(5, Block)
}
