package com.intellij.aws.cloudformation

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar

class CloudFormationReferenceContributor : PsiReferenceContributor() {
  private val ReferenceProviderInstance = CloudFormationReferenceProvider()

  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(PlatformPatterns.psiElement(JsonStringLiteral::class.java), ReferenceProviderInstance)
  }
}
