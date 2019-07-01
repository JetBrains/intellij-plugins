// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes

import com.intellij.lang.javascript.psi.JSType
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.model.VueInputProperty
import org.jetbrains.vuejs.model.webtypes.json.Attribute

class VueWebTypesInputProperty(it: Attribute, typeProvider: (Any?) -> JSType?) : VueInputProperty {
  override val name: String = it.name!!
  override val source: PsiElement? = null
  override val jsType: JSType? = typeProvider(it.type)
}
