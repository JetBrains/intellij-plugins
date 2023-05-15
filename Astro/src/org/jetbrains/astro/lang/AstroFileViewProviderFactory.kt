// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang

import com.intellij.lang.Language
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*

class AstroFileViewProviderFactory : FileViewProviderFactory {
  override fun createFileViewProvider(file: VirtualFile,
                                      language: Language?,
                                      manager: PsiManager,
                                      eventSystemEnabled: Boolean): FileViewProvider =
    AstroFileViewProvider(manager, file, eventSystemEnabled, language)

  class AstroFileViewProvider internal constructor(manager: PsiManager,
                                                   file: VirtualFile,
                                                   eventSystemEnabled: Boolean,
                                                   language: Language?)
    : SingleRootFileViewProvider(manager, file, eventSystemEnabled, language ?: AstroLanguage.INSTANCE) {
    override fun findElementAt(offset: Int, language: Language): PsiElement? =
      findElementAt(offset, language.javaClass)

    override fun findElementAt(offset: Int, language: Class<out Language>): PsiElement? =
      when (language) {
        // Ignore language if it is XML to reuse the implementation of XmlGtTypedHandler.
        XMLLanguage::class.java -> super.findElementAt(offset)
        else -> super.findElementAt(offset, language)
      }
  }
}