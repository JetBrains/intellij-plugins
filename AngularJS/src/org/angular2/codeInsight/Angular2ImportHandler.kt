// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight

import com.intellij.lang.javascript.ecmascript6.TypeScriptImportHandler
import com.intellij.lang.javascript.ecmascript6.TypeScriptQualifiedNameResolver
import com.intellij.lang.javascript.psi.resolve.JSImportHandler
import com.intellij.lang.javascript.psi.resolve.JSTypeResolveResult
import com.intellij.psi.PsiElement
import org.angular2.entities.Angular2ComponentLocator

class Angular2ImportHandler : TypeScriptImportHandler() {

  override fun resolveNameImpl(type: String,
                               sourceRaw: PsiElement,
                               typeContext: TypeScriptQualifiedNameResolver.StrictKind,
                               includeAugmentations: Boolean): JSTypeResolveResult {
    val cls = Angular2ComponentLocator.findComponentClass(sourceRaw)
    return if (cls != null) {
      super.resolveNameImpl(type, cls, typeContext, includeAugmentations)
    }
    else {
      JS_IMPORT_HANDLER.resolveName(type, sourceRaw)
    }
  }

  companion object {

    private val JS_IMPORT_HANDLER = JSImportHandler.getInstance()
  }
}
