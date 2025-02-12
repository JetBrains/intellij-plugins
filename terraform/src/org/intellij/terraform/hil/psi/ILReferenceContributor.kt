// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.psi

import com.intellij.patterns.PlatformPatterns.or
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.psi.common.Identifier
import org.intellij.terraform.hcl.psi.withHCLHost
import org.intellij.terraform.hil.patterns.HILPatterns

class ILReferenceContributor : PsiReferenceContributor() {
  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(
      psiElement(Identifier::class.java).withParent(HILPatterns.IlseFromKnownScope), ILSelectFromScopeReferenceProvider)

    registrar.registerReferenceProvider(
      psiElement(Identifier::class.java).withParent(HILPatterns.IlseNotFromKnownScope), ILSelectFromSomethingReferenceProvider)

    registrar.registerReferenceProvider(
      psiElement(Identifier::class.java).withParent(HILPatterns.IlseFromKnownScope), ILScopeReferenceProvider)

    // ForExpression variable
    registrar.registerReferenceProvider(
      psiElement(Identifier::class.java).and(HILPatterns.InsideForExpressionBody), ForVariableReferenceProvider2())

    // 'dynamic' 'content' block reference
    registrar.registerReferenceProvider(
      psiElement(Identifier::class.java).with(HILPatterns.IsSeFromCondition)
        .withHCLHost(psiElement().inside(TfPsiPatterns.DynamicBlock)),
      DynamicBlockVariableReferenceProvider)

    // 'each' in resource or data source
    registrar.registerReferenceProvider(
      psiElement(Identifier::class.java)
        .withText("each")
        .with(HILPatterns.IsSeFromCondition)
        .withHCLHost(psiElement().inside(true, or(
          TfPsiPatterns.ResourceRootBlock, TfPsiPatterns.DataSourceRootBlock, TfPsiPatterns.ModuleRootBlock))
        ), ResourceEachVariableReferenceProvider)
  }
}
