// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.template.editor

import com.intellij.lang.Language
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.ex.util.LayerDescriptor
import com.intellij.openapi.editor.ex.util.LayeredLexerEditorHighlighter
import com.intellij.openapi.editor.highlighter.EditorHighlighter
import com.intellij.openapi.fileTypes.EditorHighlighterProvider
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.intellij.terraform.hil.psi.TerraformTemplateLanguage
import org.intellij.terraform.hil.psi.TerraformTemplateTokenTypes
import org.intellij.terraform.template.doComputeTemplateDataLanguage

private class TftplEditorHighlighterProvider : EditorHighlighterProvider {
  override fun getEditorHighlighter(project: Project?,
                                    fileType: FileType,
                                    virtualFile: VirtualFile?,
                                    colors: EditorColorsScheme): EditorHighlighter {
    val inferredDataLanguage = guessDataLanguageFileType(project, virtualFile)
    return TftplEditorHighlighter(project, virtualFile, colors, inferredDataLanguage)
  }

  private fun guessDataLanguageFileType(project: Project?, virtualFile: VirtualFile?): Language {
    return if (project == null || virtualFile == null) {
      PlainTextLanguage.INSTANCE
    }
    else {
      doComputeTemplateDataLanguage(virtualFile, project)
    }
  }
}

internal class TftplEditorHighlighter(project: Project?,
                                      file: VirtualFile?,
                                      colors: EditorColorsScheme,
                                      inferredDataLanguage: Language) : LayeredLexerEditorHighlighter(
  SyntaxHighlighterFactory.getSyntaxHighlighter(TerraformTemplateLanguage, project, file), colors) {

  init {
    val outerHighlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(inferredDataLanguage, project, file)
    if (outerHighlighter != null) {
      registerLayer(
        TerraformTemplateTokenTypes.DATA_LANGUAGE_TOKEN_UNPARSED,
        LayerDescriptor(outerHighlighter, "")
      )
    }
  }
}