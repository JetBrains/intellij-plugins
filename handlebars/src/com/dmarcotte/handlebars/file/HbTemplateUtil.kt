// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.dmarcotte.handlebars.file

import com.dmarcotte.handlebars.HbLanguage
import com.intellij.injected.editor.VirtualFileWindow
import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.fileTypes.UnknownFileType
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.templateLanguages.TemplateLanguage

internal fun guessTemplateFileType(virtualFile: VirtualFile?): FileType? {
  val fileTypeManager = FileTypeManager.getInstance()
  if (virtualFile == null || virtualFile is VirtualFileWindow) {
    return null
  }
  val extension = StringUtil.split(virtualFile.name, ".").lastOrNull() ?: return null
  val fileType = fileTypeManager.getFileTypeByExtension(extension)
  return if (fileType === UnknownFileType.INSTANCE) null else fileType
}

internal fun guessTemplateLanguage(virtualFile: VirtualFile): Language {
  val fileType = guessTemplateFileType(virtualFile) as? LanguageFileType ?: return HbLanguage.getDefaultTemplateLang().language
  val language = fileType.language
  return if (language is TemplateLanguage || language.isKindOf(HbLanguage.INSTANCE)) {
    HbLanguage.getDefaultTemplateLang().language
  } else {
    language
  }
}