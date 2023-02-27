/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.terraform.hcl

import com.intellij.application.options.colors.InspectionColorSettingsPage
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import com.intellij.psi.codeStyle.DisplayPriority
import com.intellij.psi.codeStyle.DisplayPrioritySortable
import javax.swing.Icon

class HCLColorsPage : ColorSettingsPage, InspectionColorSettingsPage, DisplayPrioritySortable {

  companion object {
    private val descriptors: Array<out AttributesDescriptor> = arrayOf(
      AttributesDescriptor(HCLBundle.message("hcl.color.settings.braces.and.operators.brackets"), HCLSyntaxHighlighterFactory.HCL_BRACKETS),
      AttributesDescriptor(HCLBundle.message("hcl.color.settings.braces.and.operators.braces"), HCLSyntaxHighlighterFactory.HCL_BRACES),
      AttributesDescriptor(HCLBundle.message("hcl.color.settings.braces.and.operators.comma"), HCLSyntaxHighlighterFactory.HCL_COMMA),
      AttributesDescriptor(HCLBundle.message("hcl.color.settings.braces.and.operators.operation.sign"), HCLSyntaxHighlighterFactory.HCL_OPERATION_SIGN),

      AttributesDescriptor(HCLBundle.message("hcl.color.settings.number"), HCLSyntaxHighlighterFactory.HCL_NUMBER),
      AttributesDescriptor(HCLBundle.message("hcl.color.settings.keyword"), HCLSyntaxHighlighterFactory.HCL_KEYWORD),
      AttributesDescriptor(HCLBundle.message("hcl.color.settings.identifier"), HCLSyntaxHighlighterFactory.HCL_IDENTIFIER),

      AttributesDescriptor(HCLBundle.message("hcl.color.settings.comments.line.comment"), HCLSyntaxHighlighterFactory.HCL_LINE_COMMENT),
      AttributesDescriptor(HCLBundle.message("hcl.color.settings.comments.block.comment"), HCLSyntaxHighlighterFactory.HCL_BLOCK_COMMENT),

      AttributesDescriptor(HCLBundle.message("hcl.color.settings.property.name"), HCLSyntaxHighlighterFactory.HCL_PROPERTY_KEY),

      AttributesDescriptor(HCLBundle.message("hcl.color.settings.block.only.name.type"), HCLSyntaxHighlighterFactory.HCL_BLOCK_NAME_KEY),
      AttributesDescriptor(HCLBundle.message("hcl.color.settings.block.first.type"), HCLSyntaxHighlighterFactory.HCL_BLOCK_FIRST_TYPE_KEY),
      AttributesDescriptor(HCLBundle.message("hcl.color.settings.block.second.type"), HCLSyntaxHighlighterFactory.HCL_BLOCK_SECOND_TYPE_KEY),
      AttributesDescriptor(HCLBundle.message("hcl.color.settings.block.other.types"), HCLSyntaxHighlighterFactory.HCL_BLOCK_OTHER_TYPES_KEY),
      AttributesDescriptor(HCLBundle.message("hcl.color.settings.block.name"), HCLSyntaxHighlighterFactory.HCL_BLOCK_NAME_KEY),

      AttributesDescriptor(HCLBundle.message("hcl.color.settings.string.text"), HCLSyntaxHighlighterFactory.HCL_STRING),
      AttributesDescriptor(HCLBundle.message("hcl.color.settings.string.valid.escape.sequence"), HCLSyntaxHighlighterFactory.HCL_VALID_ESCAPE),
      AttributesDescriptor(HCLBundle.message("hcl.color.settings.string.invalid.escape.sequence"), HCLSyntaxHighlighterFactory.HCL_INVALID_ESCAPE)
    )
    private val additional: Map<String, TextAttributesKey> = mapOf(
        "pk" to HCLSyntaxHighlighterFactory.HCL_PROPERTY_KEY,
        "bt1" to HCLSyntaxHighlighterFactory.HCL_BLOCK_FIRST_TYPE_KEY,
        "bt2" to HCLSyntaxHighlighterFactory.HCL_BLOCK_SECOND_TYPE_KEY,
        "btO" to HCLSyntaxHighlighterFactory.HCL_BLOCK_OTHER_TYPES_KEY,
        "bn" to HCLSyntaxHighlighterFactory.HCL_BLOCK_NAME_KEY,
        "bon" to HCLSyntaxHighlighterFactory.HCL_BLOCK_ONLY_NAME_KEY
    )
  }

  override fun getIcon(): Icon {
    return Icons.FileTypes.HCL
  }

  override fun getHighlighter(): SyntaxHighlighter {
    return SyntaxHighlighterFactory.getSyntaxHighlighter(HCLLanguage, null, null)
  }

  override fun getDemoText(): String {
    return """/*
  Here's simple HCL code to show you syntax highlighter.
  Suggestions are welcome at https://github.com/VladRassokhin/intellij-hcl/issues
*/
<pk>name</pk> = value
// Simple single line comment
<bt1>block</bt1> <bt2>with</bt2> <btO>five</btO> <btO>types</btO> <btO>and</btO> <bn>'name'</bn> {
  <pk>array</pk> = [ 'a', 100, "b", 10.5e-42, true, false ]
  <pk>empty_array</pk> = []
  <pk>empty_object</pk> = {}
  # Yet another comment style
  <pk>strings</pk> = {
    <pk>"something"</pk> = "\"Quoted Yep!\""
    <pk>bad</pk> = "Invalid escaping:\c"
    <pk>'good'</pk> = "Valid escaping:\"\n\"\"
  }
}
<bon>block_with_only_one_name</bon> {
}
"""
  }

  override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> {
    return additional
  }

  override fun getAttributeDescriptors(): Array<out AttributesDescriptor> {
    return descriptors
  }

  override fun getColorDescriptors(): Array<out ColorDescriptor> {
    return ColorDescriptor.EMPTY_ARRAY
  }

  override fun getDisplayName(): String {
    return HCLLanguage.displayName
  }

  override fun getPriority(): DisplayPriority {
    return DisplayPriority.LANGUAGE_SETTINGS
  }
}