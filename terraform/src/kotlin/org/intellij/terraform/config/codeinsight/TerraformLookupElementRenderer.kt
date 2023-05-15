// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.codeInsight.lookup.LookupElementRenderer
import com.intellij.openapi.util.text.StringUtil
import org.intellij.terraform.hcl.Icons
import org.intellij.terraform.config.model.*
import javax.swing.Icon

class TerraformLookupElementRenderer : LookupElementRenderer<LookupElement>() {
  override fun renderElement(element: LookupElement, presentation: LookupElementPresentation) {
    presentation.itemText = element.lookupString
    val obj = element.`object`
    if (obj is PropertyOrBlockType) {
      if (obj is PropertyType) {
        presentation.icon = Icons.Property
        presentation.isItemTextBold = obj.required
        presentation.isStrikeout = obj.deprecated != null
        presentation.setTailText(trimDescription(obj.description), true)
        presentation.setTypeText(trimType(obj.type), getTypeIcon(obj.type))
      } else if (obj is BlockType) {
        presentation.icon = Icons.Object
        presentation.isItemTextBold = obj.required
        presentation.isStrikeout = obj.deprecated != null
        presentation.setTailText(trimDescription(obj.description), true)
      }
    }
  }

  private fun trimType(type: Type): String {
    return StringUtil.shortenTextWithEllipsis(type.toString(), 30, 0)
  }

  private fun trimDescription(description: String?): String? {
    if (description == null) return null
    return " " + StringUtil.shortenTextWithEllipsis(description, 55, 0, true)
  }

  private fun getTypeIcon(type: Type): Icon? {
    // TODO: Implement
    when {
//      Types.Identifier ->
//      Types.String ->
//      Types.Number ->
      isObjectType(type) -> Icons.Object
      isListType(type)-> Icons.Array
//      Types.StringWithInjection ->
//      Types.Boolean ->
//      Types.Null ->
//      Types.Invalid ->
//      Types.Any ->
    }
    return null
  }
}
