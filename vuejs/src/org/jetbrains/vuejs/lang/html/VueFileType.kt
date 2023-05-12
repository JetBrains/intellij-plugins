// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html

import com.intellij.javascript.web.html.WebFrameworkHtmlFileType
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import org.jetbrains.vuejs.index.VUE_FILE_EXTENSION

class VueFileType private constructor() : WebFrameworkHtmlFileType(VueLanguage.INSTANCE, "Vue.js", "vue") {
  companion object {
    @JvmField
    val INSTANCE: VueFileType = VueFileType()

    val PsiFile.isDotVueFile
      get() = originalFile.virtualFile?.isDotVueFile
              ?: (this is VueFile && !this.name.endsWith(".html", true))

    val VirtualFile.isDotVueFile
      get() = nameSequence.endsWith(VUE_FILE_EXTENSION)

    fun isVueFileName(name: String) =
      name.endsWith(VUE_FILE_EXTENSION)

  }
}
