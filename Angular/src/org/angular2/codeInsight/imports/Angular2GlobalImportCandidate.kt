// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.imports

import com.intellij.lang.javascript.modules.imports.JSImportDescriptor
import com.intellij.lang.javascript.modules.imports.JSSimpleImportCandidate
import com.intellij.psi.PsiElement

class Angular2GlobalImportCandidate(exportedName: String, val fieldName: String, place: PsiElement)
  : JSSimpleImportCandidate(exportedName, null, place) {

  override fun createDescriptor(): JSImportDescriptor =
    Angular2GlobalImportCandidateDescriptor(fieldName, name)

}