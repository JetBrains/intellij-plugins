// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.patterns

import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern.Capture
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentsOfType
import com.intellij.util.ProcessingContext
import org.intellij.terraform.config.Constants.HCL_DATASOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_EPHEMERAL_IDENTIFIER
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.Scopes
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.config.patterns.TfPsiPatterns.DependsOnPattern
import org.intellij.terraform.hcl.HCLLanguage
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.hcl.psi.HCLForExpression
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.common.BaseExpression
import org.intellij.terraform.hcl.psi.common.Identifier
import org.intellij.terraform.hcl.psi.common.SelectExpression
import org.intellij.terraform.hcl.psi.withHCLHost
import org.intellij.terraform.hil.HILLanguage
import org.intellij.terraform.hil.codeinsight.withLanguages

internal object HILPatterns {
  private val InVariableBlock: PatternCondition<HCLElement?> = object : PatternCondition<HCLElement?>("In Variable block") {
    override fun accepts(t: HCLElement, context: ProcessingContext?): Boolean {
      val topmost = t.parentsOfType(HCLBlock::class.java).lastOrNull() ?: return false
      return TfPsiPatterns.VariableRootBlock.accepts(topmost)
    }
  }

  private val NotBlockIdentifier = object : PatternCondition<Identifier?>("Not a Block Identifier") {
    override fun accepts(t: Identifier, context: ProcessingContext?): Boolean {
      return t.parent !is HCLBlock
    }
  }

  val TfMethodPosition: Capture<PsiElement> = getMethodIdentifierPattern(
    PlatformPatterns.psiElement(HCLElement::class.java)
      .inFile(TfPsiPatterns.TerraformConfigFile)
      .without(InVariableBlock)
      .andNot(PlatformPatterns.psiElement().inside(DependsOnPattern))
  )

  val VariableTypePosition: Capture<PsiElement> = PlatformPatterns.psiElement().withLanguages(HCLLanguage)
    .withParent(PlatformPatterns.psiElement(HCLIdentifier::class.java).with(NotBlockIdentifier).with(InVariableBlock)).andNot(
      PlatformPatterns.psiElement().withSuperParent(2, SelectExpression::class.java)
    )

  val ForEachIteratorPosition: Capture<PsiElement> = PlatformPatterns.psiElement().withLanguages(HILLanguage, HCLLanguage)
    .withParent(PlatformPatterns.psiElement(Identifier::class.java).withHCLHost(
      PlatformPatterns.psiElement(HCLElement::class.java).inside(TfPsiPatterns.DynamicBlock))
    )

  val IlseFromKnownScope: Capture<SelectExpression<*>> = PlatformPatterns.psiElement(SelectExpression::class.java)
    .with(getScopeSelectPatternCondition(Scopes))
  val IlseNotFromKnownScope: Capture<SelectExpression<*>> = PlatformPatterns.psiElement(SelectExpression::class.java)
    .without(getScopeSelectPatternCondition(Scopes))
  val IlseFromDataScope: Capture<SelectExpression<*>> = PlatformPatterns.psiElement(SelectExpression::class.java)
    .with(getScopeSelectPatternCondition(setOf(HCL_DATASOURCE_IDENTIFIER)))
  val IlseDataSource: Capture<SelectExpression<*>> = PlatformPatterns.psiElement(SelectExpression::class.java)
    .with(object : PatternCondition<SelectExpression<*>?>(" SE_Data_Source()") {
      override fun accepts(t: SelectExpression<*>, context: ProcessingContext): Boolean {
        val from = t.from as? SelectExpression<*> ?: return false
        return IlseFromDataScope.accepts(from)
      }
    })

  val IlseFromEphemeralResource: Capture<SelectExpression<*>> = PlatformPatterns.psiElement(SelectExpression::class.java)
    .with(getScopeSelectPatternCondition(setOf(HCL_EPHEMERAL_IDENTIFIER)))
  val IlseEphemeralResource: Capture<SelectExpression<*>> = PlatformPatterns.psiElement(SelectExpression::class.java)
    .with(object : PatternCondition<SelectExpression<*>?>("SE_Ephemeral_Resource()") {
      override fun accepts(t: SelectExpression<*>, context: ProcessingContext): Boolean {
        val from = t.from as? SelectExpression<*> ?: return false
        return IlseFromEphemeralResource.accepts(from)
      }
    })

  val InsideForExpressionBody: Capture<PsiElement> = PlatformPatterns.psiElement()
    .withParent(PlatformPatterns.psiElement(BaseExpression::class.java)
                  .withHCLHost(PlatformPatterns.psiElement()
                                 .inside(false, PlatformPatterns.psiElement(HCLForExpression::class.java))))
  val IsSeFromCondition: PatternCondition<PsiElement> = object : PatternCondition<PsiElement>("IsSelectFrom") {
    override fun accepts(t: PsiElement, context: ProcessingContext?): Boolean {
      val parent = t.parent as? SelectExpression<*> ?: return false
      return parent.from === t
    }
  }

  fun getScopeSelectPatternCondition(scopes: Set<String>): PatternCondition<SelectExpression<*>?> {
    return object : PatternCondition<SelectExpression<*>?>("ScopeSelect($scopes)") {
      override fun accepts(t: SelectExpression<*>, context: ProcessingContext): Boolean {
        val from = t.from
        return from is Identifier && from.name in scopes
      }
    }
  }

  fun getMethodIdentifierPattern(hclHostPattern: ElementPattern<HCLElement>): Capture<PsiElement> =
    PlatformPatterns.psiElement().withLanguages(HILLanguage, HCLLanguage)
      .withParent(PlatformPatterns.psiElement(Identifier::class.java)
                    .with(NotBlockIdentifier)
                    .withHCLHost(hclHostPattern)
      ).andNot(PlatformPatterns.psiElement().withSuperParent(2, SelectExpression::class.java))
}