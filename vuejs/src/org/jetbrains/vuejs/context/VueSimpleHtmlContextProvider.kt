// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.context

import com.intellij.lang.javascript.library.JSCDNLibManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiFile
import com.intellij.psi.XmlRecursiveElementVisitor
import com.intellij.psi.impl.source.html.HtmlLikeFile
import com.intellij.psi.impl.source.xml.XmlTagImpl
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil
import org.jetbrains.vuejs.index.VUE_MODULE

class VueSimpleHtmlContextProvider : VueContextProvider {
  override fun isVueContextEnabled(file: PsiFile): Boolean {
    if (file is HtmlLikeFile) {
      return CachedValuesManager.getCachedValue(file) {
        CachedValueProvider.Result.create(hasVueLibraryImport(file), file)
      }
    }
    return false
  }

  companion object {
    @JvmStatic
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
  }
}