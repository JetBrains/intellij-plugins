// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.editor

import com.intellij.lang.Commenter
import com.intellij.lang.Language
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.editing.JavascriptCommenter
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.xml.XmlCommenter
import com.intellij.openapi.editor.Editor
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.templateLanguages.MultipleLangCommentProvider
import org.jetbrains.astro.lang.AstroLanguage
import org.jetbrains.astro.lang.frontmatter.AstroFrontmatterLanguage

class AstroCommentProvider : MultipleLangCommentProvider {
  override fun getLineCommenter(file: PsiFile, editor: Editor, lineStartLanguage: Language, lineEndLanguage: Language): Commenter {
    val element = file.findElementAt(editor.caretModel.offset)
    return if (
      lineStartLanguage == AstroFrontmatterLanguage.INSTANCE ||
      lineStartLanguage.baseLanguage == JavascriptLanguage.INSTANCE ||
      lineStartLanguage.baseLanguage == JavaScriptSupportLoader.TYPESCRIPT ||
      (lineStartLanguage.baseLanguage == JavaScriptSupportLoader.TYPESCRIPT_JSX && element?.language == JavascriptLanguage.INSTANCE) ||
      element?.parent is JSEmbeddedContent ||
      // Used for block comments because their language suddenly changes to Astro
      element?.parent?.language == AstroFrontmatterLanguage.INSTANCE) JavascriptCommenter()
    else XmlCommenter()
  }

  override fun canProcess(file: PsiFile, viewProvider: FileViewProvider): Boolean {
    return file.language === AstroLanguage.INSTANCE
  }
}