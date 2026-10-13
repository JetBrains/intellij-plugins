// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.plugin.mdx.lang

import com.intellij.lang.Commenter
import com.intellij.lang.Language
import com.intellij.lang.LanguageCommenters
import com.intellij.openapi.editor.Editor
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.templateLanguages.MultipleLangCommentProvider
import com.intellij.psi.templateLanguages.TemplateLanguageFileViewProvider
import org.intellij.plugin.mdx.lang.template.MdxFileViewProvider

internal class MdxCommentProvider : MultipleLangCommentProvider {
  private val mdxCommenter = MdxCommenter()
  private val javascriptTagCommenter = MdxCommenter(MdxCommentContext.JAVASCRIPT)

  override fun canProcess(file: PsiFile, viewProvider: FileViewProvider): Boolean = viewProvider is MdxFileViewProvider

  override fun getLineCommenter(file: PsiFile, editor: Editor, lineStartLanguage: Language, lineEndLanguage: Language): Commenter {
    val fallback = fallbackCommenter(file, editor, lineStartLanguage, lineEndLanguage)
    return if (fallback?.lineCommentPrefix != null) javascriptTagCommenter else mdxCommenter
  }

  private fun fallbackCommenter(file: PsiFile, editor: Editor, startLanguage: Language, endLanguage: Language): Commenter? {
    val viewProvider = file.viewProvider
    val provider = MultipleLangCommentProvider.EP_NAME.extensionList.firstOrNull {
      it !is MdxCommentProvider && it.canProcess(file, viewProvider)
    }
    if (provider != null) return provider.getLineCommenter(file, editor, startLanguage, endLanguage)

    var language = if (LanguageCommenters.INSTANCE.forLanguage(startLanguage) == null || file.language.baseLanguage == startLanguage) {
      file.language
    }
    else startLanguage
    if (viewProvider is TemplateLanguageFileViewProvider && language == viewProvider.templateDataLanguage) {
      language = viewProvider.baseLanguage
    }
    return LanguageCommenters.INSTANCE.forLanguage(language)
  }
}
