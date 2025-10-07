// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.index

import com.intellij.lang.ecmascript6.index.ES6FileIncludeProvider
import com.intellij.lang.ecmascript6.index.JSFrameworkFileIncludeProvider
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.psi.impl.include.FileIncludeInfo
import com.intellij.util.indexing.FileContent
import org.jetbrains.vuejs.lang.html.VueFileType

/**
 * ES6FileIncludeProvider doesn't work for vue files but we need these files in index for building ts imports graph
 * @see ES6FileIncludeProvider
 */
class VueFileIncludeProvider : JSFrameworkFileIncludeProvider(VueFileType) {
  override fun getIncludeInfos(content: FileContent): Array<FileIncludeInfo> {
    if (!ES6FileIncludeProvider.checkTextHasImportKeyword(content)) return emptyArray()

    val psiFile = content.psiFile
    val importDeclarations = (findModule(psiFile, false)?.let { ES6ImportPsiUtil.getImportDeclarations(it) } ?: emptyList()) +
                             (findModule(psiFile, true)?.let { ES6ImportPsiUtil.getImportDeclarations(it) } ?: emptyList())

    return createFileIncludeInfos(importDeclarations)
  }
}
