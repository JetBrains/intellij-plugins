// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.codeinsight

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.intellij.terraform.hcl.patterns.HCLPatterns
import org.intellij.terraform.hcl.psi.HCLArray
import org.intellij.terraform.hcl.psi.HCLProperty

internal object HclKeywordsCompletionProvider : CompletionProvider<CompletionParameters>() {
  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    result.addAllElements(Keywords.map { LookupElementBuilder.create(it) })
  }
}

internal val AfterCommaOrBracketPattern: PsiElementPattern.Capture<PsiElement> = psiElement().afterLeaf(",", "[")
  .withSuperParent(2, HCLArray::class.java)
  .withSuperParent(3, HCLPatterns.Property.with(
    object : PatternCondition<HCLProperty?>("Array except 'depends_on'") {
      override fun accepts(t: HCLProperty, context: ProcessingContext?): Boolean {
        return t.name != "depends_on"
      }
    })
  )

private val Keywords: List<String> = listOf("null", "true", "false")