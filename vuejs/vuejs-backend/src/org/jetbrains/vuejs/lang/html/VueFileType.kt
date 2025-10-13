// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html

import com.intellij.javascript.web.html.WebFrameworkHtmlFileType
import com.intellij.openapi.fileTypes.FileTypeEvent
import com.intellij.openapi.fileTypes.FileTypeListener
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.util.ClearableLazyValue
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import org.jetbrains.vuejs.index.VUE_FILE_EXTENSION

val PsiFile.isVueFile
  get() = originalFile.virtualFile?.isVueFile
          ?: (this is VueFile && isVueFileName(this.name))

val VirtualFile.isVueFile
  get() = isVueFileName(nameSequence)

fun isVueFileName(name: CharSequence): Boolean =
  name.endsWith(VUE_FILE_EXTENSION)
  || vueFileTypeAssociations.value.any { it.acceptsCharSequence(name) }

object VueFileType : WebFrameworkHtmlFileType(VueLanguage, "Vue.js", "vue") {
  class FileTypeChangeListener : FileTypeListener {
    override fun fileTypesChanged(event: FileTypeEvent) {
      vueFileTypeAssociations.drop()
    }
  }
}

private val vueFileTypeAssociations = ClearableLazyValue.create {
  FileTypeManager.getInstance().getAssociations(VueFileType)
}