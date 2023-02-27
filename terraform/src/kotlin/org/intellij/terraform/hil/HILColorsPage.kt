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
package org.intellij.terraform.hil

import com.intellij.application.options.colors.InspectionColorSettingsPage
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import com.intellij.psi.codeStyle.DisplayPriority
import com.intellij.psi.codeStyle.DisplayPrioritySortable
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.Icons
import javax.swing.Icon

class HILColorPage : ColorSettingsPage, InspectionColorSettingsPage, DisplayPrioritySortable {

  companion object {
    private val descriptors: Array<out AttributesDescriptor> = arrayOf(
      AttributesDescriptor(HCLBundle.message("hil.color.settings.braces.operators.parentheses"), HILSyntaxHighlighterFactory.TIL_PARENS),
      AttributesDescriptor(HCLBundle.message("hil.color.settings.braces.operators.braces"), HILSyntaxHighlighterFactory.TIL_BRACES),
      AttributesDescriptor(HCLBundle.message("hil.color.settings.braces.operators.brackets"), HILSyntaxHighlighterFactory.TIL_BRACKETS),
      AttributesDescriptor(HCLBundle.message("hil.color.settings.braces.operators.comma"), HILSyntaxHighlighterFactory.TIL_COMMA),
      AttributesDescriptor(HCLBundle.message("hil.color.settings.braces.operators.operation.sign"), HILSyntaxHighlighterFactory.TIL_OPERATOR),
      AttributesDescriptor(HCLBundle.message("hil.color.settings.braces.operators.dot"), HILSyntaxHighlighterFactory.TIL_DOT),
      AttributesDescriptor(HCLBundle.message("hil.color.settings.number"), HILSyntaxHighlighterFactory.TIL_NUMBER),
      AttributesDescriptor(HCLBundle.message("hil.color.settings.string"), HILSyntaxHighlighterFactory.TIL_STRING),
      AttributesDescriptor(HCLBundle.message("hil.color.settings.keyword"), HILSyntaxHighlighterFactory.TIL_KEYWORD),
      AttributesDescriptor(HCLBundle.message("hil.color.settings.identifier"), HILSyntaxHighlighterFactory.TIL_IDENTIFIER),
      AttributesDescriptor(HCLBundle.message("hil.color.settings.predefined.scope"), HILSyntaxHighlighterFactory.TIL_PREDEFINED_SCOPE),
      AttributesDescriptor(HCLBundle.message("hil.color.settings.resource.type.reference"), HILSyntaxHighlighterFactory.TIL_RESOURCE_TYPE_REFERENCE),
      AttributesDescriptor(HCLBundle.message("hil.color.settings.resource.instance.reference"), HILSyntaxHighlighterFactory.TIL_RESOURCE_INSTANCE_REFERENCE),
      AttributesDescriptor(HCLBundle.message("hil.color.settings.property.reference"), HILSyntaxHighlighterFactory.TIL_PROPERTY_REFERENCE),
      AttributesDescriptor(HCLBundle.message("hil.color.settings.valid.escape.sequence"), HILSyntaxHighlighterFactory.TIL_VALID_ESCAPE),
      AttributesDescriptor(HCLBundle.message("hil.color.settings.invalid.escape.sequence"), HILSyntaxHighlighterFactory.TIL_INVALID_ESCAPE)
    )
    private val additional: Map<String, TextAttributesKey> = mapOf(
        "rt" to HILSyntaxHighlighterFactory.TIL_RESOURCE_TYPE_REFERENCE,
        "ri" to HILSyntaxHighlighterFactory.TIL_RESOURCE_INSTANCE_REFERENCE,
        "pr" to HILSyntaxHighlighterFactory.TIL_PROPERTY_REFERENCE,
        "s" to HILSyntaxHighlighterFactory.TIL_PREDEFINED_SCOPE
    )
  }

  override fun getIcon(): Icon {
    return Icons.FileTypes.HIL
  }

  override fun getHighlighter(): SyntaxHighlighter {
    return SyntaxHighlighterFactory.getSyntaxHighlighter(HILLanguage, null, null)
  }

  override fun getDemoText(): String {
    return "\${\"interpolation\".example.call(10, \"a\\n\\o\", \n" +
        "<s>var</s>.foo, <s>path</s>.module, 1 - 0 + (11 * 4) / 2 % 1, \n" +
        "\ttrue || !false, false && !true, true ? 1 : 2, null,\n" +
        "<rt>aws_instance</rt>.<ri>inst</ri>.<pr>availability_zone</pr>[0])}"
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
    return HILLanguage.displayName
  }

  override fun getPriority(): DisplayPriority {
    return DisplayPriority.LANGUAGE_SETTINGS
  }
}
