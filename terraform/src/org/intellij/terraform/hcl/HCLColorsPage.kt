// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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
import org.intellij.terraform.TerraformIcons
import javax.swing.Icon

class HCLColorsPage : ColorSettingsPage, InspectionColorSettingsPage, DisplayPrioritySortable {

  private val descriptors: Array<out AttributesDescriptor> = arrayOf(
    AttributesDescriptor(HCLBundle.message("hcl.color.settings.braces.and.operators.brackets"), HCLSyntaxHighlighter.HCL_BRACKETS),
    AttributesDescriptor(HCLBundle.message("hcl.color.settings.braces.and.operators.braces"), HCLSyntaxHighlighter.HCL_BRACES),
    AttributesDescriptor(HCLBundle.message("hcl.color.settings.braces.and.operators.comma"), HCLSyntaxHighlighter.HCL_COMMA),
    AttributesDescriptor(HCLBundle.message("hcl.color.settings.braces.and.operators.operation.sign"), HCLSyntaxHighlighter.HCL_OPERATION_SIGN),

    AttributesDescriptor(HCLBundle.message("hcl.color.settings.number"), HCLSyntaxHighlighter.HCL_NUMBER),
    AttributesDescriptor(HCLBundle.message("hcl.color.settings.keyword"), HCLSyntaxHighlighter.HCL_KEYWORD),
    AttributesDescriptor(HCLBundle.message("hcl.color.settings.identifier"), HCLSyntaxHighlighter.HCL_IDENTIFIER),

    AttributesDescriptor(HCLBundle.message("hcl.color.settings.comments.line.comment"), HCLSyntaxHighlighter.HCL_LINE_COMMENT),
    AttributesDescriptor(HCLBundle.message("hcl.color.settings.comments.block.comment"), HCLSyntaxHighlighter.HCL_BLOCK_COMMENT),

    AttributesDescriptor(HCLBundle.message("hcl.color.settings.property.name"), HCLSyntaxHighlighter.HCL_PROPERTY_KEY),

    AttributesDescriptor(HCLBundle.message("hcl.color.settings.block.only.name.type"), HCLSyntaxHighlighter.HCL_BLOCK_NAME_KEY),
    AttributesDescriptor(HCLBundle.message("hcl.color.settings.block.first.type"), HCLSyntaxHighlighter.HCL_BLOCK_FIRST_TYPE_KEY),
    AttributesDescriptor(HCLBundle.message("hcl.color.settings.block.second.type"), HCLSyntaxHighlighter.HCL_BLOCK_SECOND_TYPE_KEY),
    AttributesDescriptor(HCLBundle.message("hcl.color.settings.block.other.types"), HCLSyntaxHighlighter.HCL_BLOCK_OTHER_TYPES_KEY),
    AttributesDescriptor(HCLBundle.message("hcl.color.settings.block.name"), HCLSyntaxHighlighter.HCL_BLOCK_NAME_KEY),

    AttributesDescriptor(HCLBundle.message("hcl.color.settings.string.text"), HCLSyntaxHighlighter.HCL_STRING),
    AttributesDescriptor(HCLBundle.message("hcl.color.settings.string.valid.escape.sequence"), HCLSyntaxHighlighter.HCL_VALID_ESCAPE),
    AttributesDescriptor(HCLBundle.message("hcl.color.settings.string.invalid.escape.sequence"), HCLSyntaxHighlighter.HCL_INVALID_ESCAPE)
  )
  private val additional: Map<String, TextAttributesKey> = mapOf(
    "pk" to HCLSyntaxHighlighter.HCL_PROPERTY_KEY,
    "bt1" to HCLSyntaxHighlighter.HCL_BLOCK_FIRST_TYPE_KEY,
    "bt2" to HCLSyntaxHighlighter.HCL_BLOCK_SECOND_TYPE_KEY,
    "btO" to HCLSyntaxHighlighter.HCL_BLOCK_OTHER_TYPES_KEY,
    "bn" to HCLSyntaxHighlighter.HCL_BLOCK_NAME_KEY,
    "bon" to HCLSyntaxHighlighter.HCL_BLOCK_ONLY_NAME_KEY
  )

  override fun getIcon(): Icon {
    return TerraformIcons.HashiCorp
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