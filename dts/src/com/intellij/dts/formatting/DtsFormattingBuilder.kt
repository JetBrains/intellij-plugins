package com.intellij.dts.formatting

import com.intellij.formatting.FormattingContext
import com.intellij.formatting.FormattingModel
import com.intellij.formatting.FormattingModelBuilder
import com.intellij.formatting.FormattingModelProvider

class DtsFormattingBuilder : FormattingModelBuilder {
  override fun createModel(formattingContext: FormattingContext): FormattingModel {
    val settings = formattingContext.codeStyleSettings

    return FormattingModelProvider.createFormattingModelForPsiFile(
      formattingContext.containingFile,
      DtsBlock(
        formattingContext.node,
        DtsWrappingBuilder.childBuilder(settings),
        DtsSpacingBuilder(settings),
        null
      ),
      settings,
    )
  }
}
