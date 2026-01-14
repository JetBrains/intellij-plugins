// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.stack.component

import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import org.intellij.terraform.hcl.psi.reference.SourcePropertyReferenceProvider

internal class TfComponentReferenceContributor : PsiReferenceContributor() {
  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    SourcePropertyReferenceProvider.register(registrar, TfComponentPsiPatterns.TfComponentOrStackBlock)
  }
}