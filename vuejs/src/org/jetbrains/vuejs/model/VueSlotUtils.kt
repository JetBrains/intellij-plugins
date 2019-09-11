// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.psi.xml.XmlAttribute
import com.intellij.xml.util.HtmlUtil.TEMPLATE_TAG_NAME
import org.jetbrains.vuejs.codeInsight.tags.VueElementDescriptor

private const val DEFAULT_SLOT = "default"

fun getAvailableSlots(attr: XmlAttribute, newApi: Boolean): List<VueSlot> {
  val tag = attr.parent
  return if (!newApi || tag.name == TEMPLATE_TAG_NAME) {
    (tag.parentTag?.descriptor as? VueElementDescriptor)?.getSlots() ?: emptyList()
  }
  else {
    (tag.descriptor as? VueElementDescriptor)?.getSlots()?.filter { it.name == DEFAULT_SLOT } ?: emptyList()
  }
}

