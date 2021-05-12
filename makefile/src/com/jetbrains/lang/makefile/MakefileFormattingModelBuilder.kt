package com.jetbrains.lang.makefile

import com.intellij.formatting.*
import com.intellij.psi.formatter.DocumentBasedFormattingModel

class MakefileFormattingModelBuilder : FormattingModelBuilder {
    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        val settings = formattingContext.codeStyleSettings
        val block = MakefileFormattingBlock(formattingContext.node, settings, SpacingBuilder(settings, MakefileLanguage))

        return DocumentBasedFormattingModel(block, settings, formattingContext.containingFile)
    }
}
