// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.imports

import com.intellij.lang.ecmascript6.psi.ES6ImportExportDeclaration
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.javascript.modules.imports.JSImportDescriptor
import com.intellij.lang.javascript.modules.imports.JSSimpleImportCandidate
import com.intellij.psi.PsiElement

class Angular2GlobalImportCandidate(name: String, place: PsiElement)
  : JSSimpleImportCandidate(name, null, place) {

  override fun createDescriptor(): JSImportDescriptor {
    return Angular2GlobalImportCandidateDescriptor(
      "\$GLOBAL$", name, null,
      ES6ImportExportDeclaration.ImportExportPrefixKind.IMPORT,
      ES6ImportPsiUtil.ImportExportType.DEFAULT
    )
  }

}