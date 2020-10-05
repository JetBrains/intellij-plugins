// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.editor

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.generation.IndentedCommenter
import com.intellij.lang.Commenter
import com.intellij.lang.Language
import com.intellij.lang.LanguageCommenters
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.xml.XmlCommenter
import com.intellij.openapi.editor.Editor
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.templateLanguages.MultipleLangCommentProvider
import org.jetbrains.vuejs.lang.html.VueLanguage

class VueCommenterProvider : MultipleLangCommentProvider {

  override fun getLineCommenter(file: PsiFile?, editor: Editor?, lineStartLanguage: Language?, lineEndLanguage: Language?): Commenter? =
    if (lineStartLanguage == lineEndLanguage && lineStartLanguage == VueLanguage.INSTANCE && file != null) {
      CodeStyle.getLanguageSettings(file, HTMLLanguage.INSTANCE).let { styleSettings ->
        object : XmlCommenter(), IndentedCommenter {
          override fun forceIndentedLineComment(): Boolean? = !styleSettings.LINE_COMMENT_AT_FIRST_COLUMN
          override fun forceIndentedBlockComment(): Boolean? = !styleSettings.BLOCK_COMMENT_AT_FIRST_COLUMN
        }
      }
    }
    else lineStartLanguage?.let { LanguageCommenters.INSTANCE.forLanguage(it) }

  override fun canProcess(file: PsiFile?, viewProvider: FileViewProvider?): Boolean = file?.language == VueLanguage.INSTANCE
}