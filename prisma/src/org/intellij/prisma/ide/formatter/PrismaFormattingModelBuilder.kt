package org.intellij.prisma.ide.formatter

import com.intellij.formatting.FormattingContext
import com.intellij.formatting.FormattingModel
import com.intellij.formatting.FormattingModelBuilder
import com.intellij.formatting.FormattingModelProvider

class PrismaFormattingModelBuilder : FormattingModelBuilder {
  override fun createModel(formattingContext: FormattingContext): FormattingModel {
    val file = formattingContext.containingFile
    val settings = formattingContext.codeStyleSettings
    val context = PrismaFormatBlockContext(settings)
    val block = PrismaFormatBlock(formattingContext.node, null, null, context)
    return FormattingModelProvider.createFormattingModelForPsiFile(file, block, settings)
  }
}