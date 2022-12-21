// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.context

import com.intellij.lang.html.HtmlCompatibleFile
import com.intellij.lang.javascript.library.JSCDNLibManager
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.XmlRecursiveElementVisitor
import com.intellij.psi.impl.source.xml.XmlTagImpl
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.xml.XmlTag
import com.intellij.webSymbols.context.WebSymbolsContextProvider
import com.intellij.xml.util.HtmlUtil
import org.jetbrains.vuejs.index.VUE_FILE_EXTENSION
import org.jetbrains.vuejs.index.VUE_MODULE
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.lang.html.VueLanguage

class VueFileContext : WebSymbolsContextProvider {

  override fun isEnabled(file: VirtualFile, project: Project): Boolean {
    return file.nameSequence.endsWith(VUE_FILE_EXTENSION)
  }

  override fun isEnabled(file: PsiFile): Boolean {
    val vf = file.originalFile.virtualFile
    if (vf != null && isEnabled(vf, file.project)
        || (vf == null && file.language == VueLanguage.INSTANCE)) {
      return true
    }
    if (file is HtmlCompatibleFile) {
      return CachedValuesManager.getCachedValue(file) {
        CachedValueProvider.Result.create(hasVueLibraryImport(file), file)
      }
    }
    return false
  }
}

fun hasVueLibraryImport(file: PsiFile): Boolean {
  var level = 0
  var result = false
  file.acceptChildren(object : XmlRecursiveElementVisitor() {
    override fun visitXmlTag(tag: XmlTag) {
      if (HtmlUtil.isScriptTag(tag) && hasVueScriptLink(tag)) {
        result = true
      }
      if (++level <= 3) {
        // Do not process XIncludes to avoid recursion
        (tag as? XmlTagImpl)?.getSubTags(false)?.forEach { it.accept(this) }
        level--
      }
    }

    private fun hasVueScriptLink(tag: XmlTag): Boolean {
      val link = tag.getAttribute(HtmlUtil.SRC_ATTRIBUTE_NAME)?.value
      if (link == null || !link.contains("vue")) {
        return false
      }
      if (JSCDNLibManager.getLibraryForUrl(link)?.libraryName == VUE_MODULE) {
        return true
      }
      val fileName = VfsUtil.extractFileName(link)
      return fileName != null && fileName.startsWith("vue.") && fileName.endsWith(".js")
    }
  })
  return result
}