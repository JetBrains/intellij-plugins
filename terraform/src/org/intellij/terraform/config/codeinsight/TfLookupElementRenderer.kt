// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.codeInsight.lookup.LookupElementRenderer
import com.intellij.openapi.util.text.StringUtil
import org.intellij.terraform.config.model.*
import org.intellij.terraform.hcl.Icons
import javax.swing.Icon

class TfLookupElementRenderer : LookupElementRenderer<LookupElement>() {
  override fun renderElement(element: LookupElement, presentation: LookupElementPresentation) {
    presentation.itemText = element.lookupString
    val obj = element.`object`
    if (obj is PropertyOrBlockType) {
      presentation.isItemTextBold = obj.required
      presentation.isStrikeout = obj.deprecated != null
      when (obj) {
        is PropertyType -> {
          presentation.icon = Icons.Property
          presentation.setTypeText(trimType(obj.type), getTypeIcon(obj.type))
        }
        is BlockType -> {
          presentation.icon = Icons.Object
        }
      }
    }
  }

  private fun trimType(type: Type): String {
    return StringUtil.shortenTextWithEllipsis(type.toString(), 30, 0)
  }

  private fun getTypeIcon(type: Type): Icon? {
    when {
      isObjectType(type) -> Icons.Object
      isListType(type)-> Icons.Array
    }
    return null
  }
}
