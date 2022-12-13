package org.intellij.prisma.ide.completion.schema

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext
import org.intellij.prisma.ide.schema.PrismaSchemaElement
import org.intellij.prisma.ide.schema.PrismaSchemaKind
import org.intellij.prisma.lang.psi.PrismaPsiPatterns

object PrismaKeywordProvider : PrismaSchemaCompletionProvider() {
  override val kind: PrismaSchemaKind = PrismaSchemaKind.KEYWORD

  override val pattern = PrismaPsiPatterns.topKeyword

  override fun createLookupElement(
    schemaElement: PrismaSchemaElement,
    parameters: CompletionParameters,
    context: ProcessingContext
  ): LookupElementBuilder {
    return super.createLookupElement(schemaElement, parameters, context).bold()
  }
}

