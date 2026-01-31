// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.terraform.template.editor

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.tree.IElementType
import com.intellij.terraform.template.lexer.TerraformTemplateLexer
import org.intellij.terraform.hil.HILSyntaxHighlighter
import org.intellij.terraform.hil.HILSyntaxHighlighterFactory
import org.intellij.terraform.hil.psi.template.TftplTokenTypes

internal class TftplSyntaxHighlightingFactory : HILSyntaxHighlighterFactory() {
  override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter {
    return TerraformTemplateSyntaxHighlighter()
  }
}

private class TerraformTemplateSyntaxHighlighter : HILSyntaxHighlighter() {
  override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> {
    return if (tokenType == TftplTokenTypes.DATA_LANGUAGE_TOKEN_UNPARSED) {
      emptyArray()
    }
    else {
      pack(super.getTokenHighlights(tokenType), TEMPLATE_BACKGROUND)
    }
  }

  override fun getHighlightingLexer(): Lexer {
    return TerraformTemplateLexer()
  }
}