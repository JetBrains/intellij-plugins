// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.patterns

import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import org.intellij.terraform.hcl.HCLParserDefinition
import org.intellij.terraform.hcl.HCLTokenTypes
import org.intellij.terraform.hcl.psi.*

object HCLPatterns {
  val Nothing: ElementPattern<PsiElement> = StandardPatterns.alwaysFalse<PsiElement>()

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

  val FileOrBlock: ElementPattern<PsiElement> = PlatformPatterns.or(File, Block)
  val PropertyOrBlock: ElementPattern<PsiElement> = PlatformPatterns.or(Property, Block)
}