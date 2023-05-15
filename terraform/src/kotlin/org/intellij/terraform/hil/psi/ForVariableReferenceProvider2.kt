// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.psi

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import org.intellij.terraform.config.codeinsight.TerraformConfigCompletionContributor
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.hcl.psi.HCLForExpression
import org.intellij.terraform.hcl.psi.HCLForIntro
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.common.Identifier
import org.intellij.terraform.hcl.psi.common.SelectExpression
import org.intellij.terraform.hil.codeinsight.HILCompletionContributor
import org.intellij.terraform.hil.psi.impl.getHCLHost

class ForVariableReferenceProvider2 : PsiReferenceProvider() {
  override fun getReferencesByElement(id: PsiElement, context: ProcessingContext): Array<out PsiReference> {
    if (id !is Identifier) return PsiReference.EMPTY_ARRAY
    id.getHCLHost() ?: return PsiReference.EMPTY_ARRAY
    if (!HILCompletionContributor.INSIDE_FOR_EXPRESSION_BODY.accepts(id)) return PsiReference.EMPTY_ARRAY
    val forExpr = forDeclarationForIdentifier(id) ?: return PsiReference.EMPTY_ARRAY
    if (forExpr.intro.identifiers.any { it.name == id.name }) {
      return arrayOf(ForVariableDirectReference(id))
    }
    return PsiReference.EMPTY_ARRAY
  }
}

class ForVariableCompletion : TerraformConfigCompletionContributor.OurCompletionProvider() {
  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    val id = parameters.position.parent as? Identifier ?: return
    val parent = id.parent
    if (parent is SelectExpression<*>) {
      if (parent.from !== id) return
    }
    id.parentsWithInjection().filterIsInstance<HCLForExpression>()
      .flatMap { it.intro.identifiers }
      .mapNotNull { it.name }
      .forEach {
        result.addElement(HILCompletionContributor.create(it)) 
      }
  }
}

fun forDeclarationForIdentifier(id: Identifier): HCLForExpression? =
  id.parentsWithInjection().filterIsInstance<HCLForExpression>().filter { forExpr ->
    forExpr.intro.identifiers.any { it.name == id.name }
  }.firstOrNull()

class ForVariableDirectReference(id: Identifier) : HCLElementLazyReferenceBase<Identifier>(id, false) {
  override fun resolve(incompleteCode: Boolean, includeFake: Boolean): List<HCLElement> {
    val forExpr = forDeclarationForIdentifier(element) ?: return emptyList()
    return forExpr.intro.identifiers.filter { it.name == element.name }.toList()
  }
}

val HCLForIntro.identifiers: Sequence<HCLIdentifier> get() = sequenceOf(var1, var2).filterNotNull()

fun PsiElement.parentsWithInjection(): Sequence<PsiElement> {
  val elt = this
  return sequence {
    var element: PsiElement? = elt.parent
    while (element != null) {
      yield(element)
      val next = element.parent
      element = if (next == null || next is PsiFile) {
        InjectedLanguageManager.getInstance(project).getInjectionHost(next ?: element)
      }
      else
        next
    }
  }
}