// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.patterns

import com.intellij.patterns.*
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.parentsOfType
import com.intellij.util.ProcessingContext
import org.intellij.terraform.config.Constants.HCL_DATASOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_PROVIDER_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_RESOURCE_IDENTIFIER
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.HCLTokenTypes
import org.intellij.terraform.hcl.psi.*

object HCLPatterns {
  val Nothing: ElementPattern<PsiElement> = StandardPatterns.alwaysFalse()

  val WhiteSpace: PsiElementPattern.Capture<PsiWhiteSpace> = PlatformPatterns.psiElement(PsiWhiteSpace::class.java)
  val AtLeastOneEOL: PsiElementPattern.Capture<PsiWhiteSpace> = PlatformPatterns.psiElement(PsiWhiteSpace::class.java).withText(StandardPatterns.string().contains("\n"))

  val Identifier: PsiElementPattern.Capture<HCLIdentifier> = PlatformPatterns.psiElement(HCLIdentifier::class.java)
  val Literal: PsiElementPattern.Capture<HCLStringLiteral> = PlatformPatterns.psiElement(HCLStringLiteral::class.java)

  val File: PsiElementPattern.Capture<HCLFile> = PlatformPatterns.psiElement(HCLFile::class.java)

  val Block: PsiElementPattern.Capture<HCLBlock> = PlatformPatterns.psiElement(HCLBlock::class.java)
  val Property: PsiElementPattern.Capture<HCLProperty> = PlatformPatterns.psiElement(HCLProperty::class.java)
  val Object: PsiElementPattern.Capture<HCLObject> = PlatformPatterns.psiElement(HCLObject::class.java)

  val Array: PsiElementPattern.Capture<HCLArray> = PlatformPatterns.psiElement(HCLArray::class.java)

  val SelectExpression: PsiElementPattern.Capture<HCLSelectExpression> = PlatformPatterns.psiElement(HCLSelectExpression::class.java)

  val IdentifierOrStringLiteral: ElementPattern<HCLValue> = PlatformPatterns.or(Identifier, Literal)
  val IdentifierOrStringLiteralOrSimple: ElementPattern<PsiElement> = PlatformPatterns.or(IdentifierOrStringLiteral, PlatformPatterns.psiElement().withElementType(
    HCLTokenTypes.IDENTIFYING_LITERALS))

  val BlockTypeIdentifierLiteral: PsiElementPattern.Capture<HCLStringLiteral> =
    PlatformPatterns.psiElement(HCLStringLiteral::class.java)
      .withParent(Block)
      .with(object : PatternCondition<HCLStringLiteral>("resource/data block type identifier") {
        override fun accepts(literal: HCLStringLiteral, context: ProcessingContext?): Boolean {
          val validIdentifiers = setOf(HCL_PROVIDER_IDENTIFIER, HCL_RESOURCE_IDENTIFIER, HCL_DATASOURCE_IDENTIFIER)
          return literal.parentsOfType<HCLBlock>(true)
            .filter { it.getNameElementUnquoted(0) in validIdentifiers }
            .firstOrNull { block -> block.getNameElementUnquoted(1) == HCLPsiUtil.stripQuotes(literal.text) } != null
        }
      })

  val FileOrBlock: ElementPattern<PsiElement> = PlatformPatterns.or(File, Block)
  val PropertyOrBlock: ElementPattern<PsiElement> = PlatformPatterns.or(Property, Block)

  val HashesStringLiterals: PsiElementPattern.Capture<HCLStringLiteral> =
    PlatformPatterns.psiElement(HCLStringLiteral::class.java)
      .withSuperParent(1, HCLArray::class.java)
      .withSuperParent(2, TfPsiPatterns.propertyWithName("hashes"))
      .withSuperParent(3, HCLObject::class.java)
      .withSuperParent(4, PlatformPatterns.psiElement(HCLBlock::class.java)
        .with(TfPsiPatterns.createBlockPattern("provider"))
      )
}