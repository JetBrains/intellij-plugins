// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.imports

import com.intellij.lang.ecmascript6.actions.JSImportDescriptorBuilder
import com.intellij.lang.javascript.modules.JSImportPlaceInfo.ImportContext
import com.intellij.lang.javascript.modules.imports.JSImportDescriptor
import com.intellij.lang.javascript.modules.imports.JSSimpleImportCandidate
import com.intellij.psi.PsiElement

class Angular2ModuleImportCandidate(
  name: String,
  candidate: PsiElement,
  place: PsiElement
) : JSSimpleImportCandidate(name, candidate, place) {

  override fun createDescriptor(): JSImportDescriptor? {
    val element = element ?: return null
    val effectivePlace = myPlace.getElement() ?: return null
    return JSImportDescriptorBuilder(effectivePlace, true)
      .createDescriptor(name, element, ImportContext.SIMPLE)
  }

}