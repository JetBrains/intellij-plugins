// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.impl

import com.intellij.psi.xml.XmlTag

const val TEMPLATE_TAG_NAME: String = "template"
const val SLOT_TAG_NAME: String = "slot"

fun isTemplate(
  tag: XmlTag,
): Boolean =
  tag.localName == TEMPLATE_TAG_NAME
