// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.patterns

import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern.Capture
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentsOfType
import com.intellij.util.ProcessingContext
import org.intellij.terraform.config.Constants.HCL_DATASOURCE_IDENTIFIER
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.Scopes
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.config.patterns.TfPsiPatterns.DependsOnPattern
import org.intellij.terraform.hcl.HCLLanguage
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.hcl.psi.common.BaseExpression
import org.intellij.terraform.hcl.psi.common.Identifier
import org.intellij.terraform.hcl.psi.common.SelectExpression
import org.intellij.terraform.hil.HILLanguage
import org.intellij.terraform.hil.codeinsight.withLanguages

object HILPatterns {
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

  val MethodPosition: Capture<PsiElement> = PlatformPatterns.psiElement().withLanguages(HILLanguage, HCLLanguage).withParent(
    PlatformPatterns.psiElement(Identifier::class.java).with(NotBlockIdentifier).withHCLHost(
      PlatformPatterns.psiElement(HCLElement::class.java).without(InVariableBlock).andNot(
        PlatformPatterns.psiElement().inside(DependsOnPattern))
    )
  ).andNot(PlatformPatterns.psiElement().withSuperParent(2, SelectExpression::class.java))

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

  internal fun getScopeSelectPatternCondition(scopes: Set<String>): PatternCondition<SelectExpression<*>?> {
    return object : PatternCondition<SelectExpression<*>?>("ScopeSelect($scopes)") {
      override fun accepts(t: SelectExpression<*>, context: ProcessingContext): Boolean {
        val from = t.from
        return from is Identifier && from.name in scopes
      }
    }
  }
}