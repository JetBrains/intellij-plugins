// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.codeinsight

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.util.ProcessingContext
import org.intellij.terraform.hcl.psi.HCLArray
import org.intellij.terraform.hcl.psi.HCLProperty

/**
 * Based on com.intellij.json.codeinsight.JsonCompletionContributor
 */
open class HCLCompletionContributor : CompletionContributor() {

  private val AFTER_EQUALS_IN_PROPERTY = psiElement().afterLeaf("=").withSuperParent(2, HCLProperty::class.java)
  private val AFTER_COMMA_OR_BRACKET_IN_ARRAY = psiElement().afterLeaf(",", "[").withSuperParent(2, HCLArray::class.java)

  init {
    extend(CompletionType.BASIC, AFTER_EQUALS_IN_PROPERTY, MyKeywordsCompletionProvider)
    // FIXME: Make it work
    extend(CompletionType.BASIC, AFTER_COMMA_OR_BRACKET_IN_ARRAY, MyKeywordsCompletionProvider)
  }

  override fun beforeCompletion(context: CompletionInitializationContext) {
    if (context.dummyIdentifier != CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED) {
      context.dummyIdentifier = CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED
    }
  }

  private object MyKeywordsCompletionProvider : CompletionProvider<CompletionParameters>() {
    private val KEYWORDS = arrayOf("null", "true", "false")

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      if (!parameters.isExtendedCompletion) return
      for (keyword in KEYWORDS) {
        result.addElement(LookupElementBuilder.create(keyword))
      }
    }
  }
}
