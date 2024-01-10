// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight

import com.intellij.lang.javascript.psi.resolve.accessibility.TypeScriptConfigAccessibilityChecker
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import org.angular2.entities.Angular2ComponentLocator

class Angular2ConfigAccessibilityChecker : TypeScriptConfigAccessibilityChecker() {

  override fun getScope(place: PsiElement?): VirtualFile? {
    return place?.let { super.getScope(Angular2ComponentLocator.findComponentClass(it)) }
  }
}
