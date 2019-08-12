// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.index.VueIndexData
import org.jetbrains.vuejs.model.VueRegularComponent

class VueSourceComponent(sourceElement: JSImplicitElement,
                         clazz: JSClass?,
                         declaration: JSObjectLiteralExpression?,
                         private val indexData: VueIndexData?)
  : VueSourceContainer(sourceElement, clazz, declaration), VueRegularComponent {

  override val defaultName: String?
    get() = indexData?.originalName
            ?: getTextIfLiteral(initializer?.findProperty("name")?.value)

}
