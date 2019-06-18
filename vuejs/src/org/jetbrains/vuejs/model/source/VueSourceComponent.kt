// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.index.VueIndexData
import org.jetbrains.vuejs.model.VueRegularComponent

class VueSourceComponent(sourceElement: PsiElement,
                         declaration: JSObjectLiteralExpression?,
                         private val indexData: VueIndexData?)
  : VueSourceContainer(sourceElement, declaration), VueRegularComponent {

  override val defaultName: String?
    get() {
      return indexData?.originalName
             ?: (declaration?.findProperty("name")
               ?.value as? JSLiteralExpression)
               ?.let {
                 if (it.isQuotedLiteral) it.stringValue else null
               }
    }

}
