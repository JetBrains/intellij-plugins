// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config

import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.intellij.terraform.hcl.HCLSyntaxHighlighter
import org.intellij.terraform.hcl.HCLSyntaxHighlighterFactory

class TfSyntaxHighlighterFactory : HCLSyntaxHighlighterFactory() {
  override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter {
    return HCLSyntaxHighlighter(TfParserDefinition.createLexer())
  }
}
