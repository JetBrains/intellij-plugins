// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro

import com.intellij.ide.fileTemplates.DefaultCreateFromTemplateHandler
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.openapi.fileTypes.ex.FileTypeManagerEx
import org.jetbrains.astro.CreateAstroComponentAction.Companion.ASTRO_TEMPLATE_NAME
import org.jetbrains.astro.lang.sfc.AstroSfcFileType

class AstroCreateFromTemplateHandler : DefaultCreateFromTemplateHandler() {
  override fun handlesTemplate(template: FileTemplate): Boolean {
    val fileType = FileTypeManagerEx.getInstanceEx().getFileTypeByExtension(template.extension)
    return AstroSfcFileType.INSTANCE == fileType && ASTRO_TEMPLATE_NAME == template.name
  }

  override fun isNameRequired(): Boolean = true

  override fun prepareProperties(props: MutableMap<String, Any>) {
  }
}
