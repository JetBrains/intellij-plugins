// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.editor

import com.intellij.lang.Commenter
import com.intellij.lang.Language
import com.intellij.lang.javascript.frameworks.jsx.JSXCommentProvider
import com.intellij.openapi.editor.Editor
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile
import org.jetbrains.astro.lang.AstroLanguage
import org.jetbrains.astro.lang.psi.AstroContentRoot

class AstroCommentProvider : JSXCommentProvider() {
  override fun getLineCommenter(file: PsiFile, editor: Editor, lineStartLanguage: Language, lineEndLanguage: Language): Commenter? {
    val at = findFirstElementOnLine(file, editor)
    val isRootElement = at?.parent?.parent is AstroContentRoot
    // The default implementation falls back to an incompatible comment style for root elements
    return if (isRootElement) JSXCommenter(file) else super.getLineCommenter(file, editor, lineStartLanguage, lineEndLanguage)
  }

  override fun canProcess(file: PsiFile, viewProvider: FileViewProvider): Boolean {
    return file.language === AstroLanguage.INSTANCE
  }
}