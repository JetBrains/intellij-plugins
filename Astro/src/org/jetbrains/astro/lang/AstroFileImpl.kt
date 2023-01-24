// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.lang.html.HtmlCompatibleFile
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.search.PsiElementProcessor
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import org.jetbrains.astro.codeInsight.astroContentRoot

class AstroFileImpl(provider: FileViewProvider)
  : PsiFileBase(provider, AstroLanguage.INSTANCE), HtmlCompatibleFile, XmlFile {
  override fun getFileType(): FileType =
    AstroFileType.INSTANCE

  override fun processElements(processor: PsiElementProcessor<*>, place: PsiElement): Boolean =
    document?.processElements(processor, place) == true

  override fun getFileResolveScope(): GlobalSearchScope =
    ProjectScope.getAllScope(project)

  override fun ignoreReferencedElementAccessibility(): Boolean =
    false

  override fun getDocument(): XmlDocument? =
    astroContentRoot()

  override fun getRootTag(): XmlTag? =
    document?.rootTag

  override fun toString(): String {
    return "AstroFile:$name"
  }
}