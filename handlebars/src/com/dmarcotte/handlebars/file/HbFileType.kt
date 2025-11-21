// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.dmarcotte.handlebars.file

import com.dmarcotte.handlebars.HbBundle
import com.dmarcotte.handlebars.HbLanguage
import com.intellij.ide.highlighter.XmlLikeFileType
import com.intellij.lang.Language
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.readAction
import com.intellij.openapi.fileTypes.CharsetUtil
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.fileTypes.TemplateLanguageFileType
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings
import icons.HandlebarsIcons
import org.jetbrains.annotations.NonNls
import java.nio.charset.Charset
import javax.swing.Icon

open class HbFileType protected constructor(lang: Language) : XmlLikeFileType(lang), TemplateLanguageFileType {
  private constructor() : this(HbLanguage.INSTANCE)

  override fun getName(): String = "Handlebars/Mustache"

  override fun getDescription(): String = HbBundle.message("filetype.hb.description")

  override fun getDefaultExtension(): String = DEFAULT_EXTENSION

  override fun getIcon(): Icon = HandlebarsIcons.Handlebars_icon

  override fun extractCharsetFromFileContent(
    project: Project?,
    file: VirtualFile?,
    content: CharSequence,
  ): Charset? =
    if (ApplicationManager.getApplication().isReadAccessAllowed || project == null || file == null) {
      getAssociatedFileType(file, project)
    } else {
      runBlockingCancellable {
        readAction {
          getAssociatedFileType(file, project)
        }
      }
    }?.let { CharsetUtil.extractCharsetFromFileContent(project, file, it, content) }

  companion object {
    @JvmField val INSTANCE: LanguageFileType = HbFileType()

    @NonNls
    const val DEFAULT_EXTENSION: @NonNls String = "handlebars;hbs;mustache"
  }
}

private fun getAssociatedFileType(file: VirtualFile?, project: Project?): LanguageFileType? {
  val associatedFileType = if (project != null && file != null)
    TemplateDataLanguageMappings.getInstance(project).getMapping(file)?.associatedFileType
  else null
  return associatedFileType ?: HbLanguage.getDefaultTemplateLang()
}
