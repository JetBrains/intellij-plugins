// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro

import com.intellij.lang.ecmascript6.index.JSFrameworkFileIncludeProvider
import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration
import com.intellij.psi.impl.include.FileIncludeInfo
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.indexing.FileContent
import org.jetbrains.astro.lang.AstroFileType

class AstroFileIncludeProvider : JSFrameworkFileIncludeProvider(AstroFileType.INSTANCE) {
  override fun getIncludeInfos(content: FileContent): Array<FileIncludeInfo> =
    createFileIncludeInfos(
      PsiTreeUtil.collectElementsOfType(
        content.psiFile,
        ES6ImportDeclaration::class.java
      ).toList()
    )
}
