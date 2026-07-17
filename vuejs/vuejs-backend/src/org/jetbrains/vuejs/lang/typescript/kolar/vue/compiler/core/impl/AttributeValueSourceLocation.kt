// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.impl

import com.intellij.psi.xml.XmlAttributeValue
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.SourceLocation

class AttributeValueSourceLocation(
  private val element: XmlAttributeValue,
) : SourceLocation {
  override val startOffset: Int
    get() = element.valueTextRange.startOffset

  override val endOffset: Int
    get() = element.valueTextRange.endOffset

  override val source: String
    get() = element.value
}
