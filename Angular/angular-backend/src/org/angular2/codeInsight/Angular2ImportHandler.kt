// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight

import com.intellij.lang.javascript.ecmascript6.TypeScriptImportHandler
import com.intellij.lang.javascript.ecmascript6.TypeScriptQualifiedNameResolver
import com.intellij.lang.javascript.psi.resolve.JSImportHandler
import com.intellij.lang.javascript.psi.resolve.JSTypeResolveResult
import com.intellij.psi.PsiElement
import org.angular2.entities.Angular2EntitiesProvider

class Angular2ImportHandler : TypeScriptImportHandler() {

  override fun resolveNameImpl(type: String,
                               sourceRaw: PsiElement,
                               typeContext: TypeScriptQualifiedNameResolver.StrictKind,
                               includeAugmentations: Boolean): JSTypeResolveResult {
    val resolveScope = Angular2EntitiesProvider.findTemplateComponent(sourceRaw)?.jsResolveScope
    return if (resolveScope != null) {
      super.resolveNameImpl(type, resolveScope, typeContext, includeAugmentations)
    }
    else {
      JS_IMPORT_HANDLER.resolveName(type, sourceRaw)
    }
  }

  companion object {

    private val JS_IMPORT_HANDLER = JSImportHandler.getInstance()
  }
}
