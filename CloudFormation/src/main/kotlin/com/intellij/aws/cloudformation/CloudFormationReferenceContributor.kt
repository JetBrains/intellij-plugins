package com.intellij.aws.cloudformation

import com.intellij.json.psi.JsonReferenceExpression
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import org.jetbrains.yaml.psi.YAMLScalar

class CloudFormationReferenceContributor : PsiReferenceContributor() {
  private val ReferenceProviderInstance = CloudFormationReferenceProvider()

  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(PlatformPatterns.psiElement(JsonStringLiteral::class.java), ReferenceProviderInstance)
    registrar.registerReferenceProvider(PlatformPatterns.psiElement(JsonReferenceExpression::class.java), ReferenceProviderInstance)
    registrar.registerReferenceProvider(PlatformPatterns.psiElement(YAMLScalar::class.java), ReferenceProviderInstance)
  }
}
