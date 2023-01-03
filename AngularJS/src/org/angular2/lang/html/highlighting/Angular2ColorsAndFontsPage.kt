// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.highlighting

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import com.intellij.psi.codeStyle.DisplayPriority
import com.intellij.psi.codeStyle.DisplayPrioritySortable
import icons.AngularJSIcons
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.html.Angular2HtmlFileType
import javax.swing.Icon

class Angular2ColorsAndFontsPage : ColorSettingsPage, DisplayPrioritySortable {
  override fun getDisplayName(): String {
    return "Angular Template"
  }

  override fun getIcon(): Icon {
    return AngularJSIcons.Angular2
  }

  override fun getAttributeDescriptors(): Array<AttributesDescriptor> {
    return ATTRS
  }

  override fun getColorDescriptors(): Array<ColorDescriptor> {
    return ColorDescriptor.EMPTY_ARRAY
  }

  override fun getHighlighter(): SyntaxHighlighter {
    return SyntaxHighlighterFactory.getSyntaxHighlighter(Angular2HtmlFileType.INSTANCE, null, null)!!
  }

  override fun getDemoText(): String {
    return """
      <li attr="value"
          *ngFor="let hero of heroes as test"
          [class.selected]="hero === selectedHero"
          (click)="onSelect(hero)">
        {{hero.name}}
      </li>

      {heroes.length, plural, =0 {no heroes} =1 {one hero} other {{{heroes.length}} heroes}} found

      <input [(ngModel)]="hero.name" #model="ngModel"/>
      
      """.trimIndent()
  }

  override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? {
    return null
  }

  override fun getPriority(): DisplayPriority {
    return DisplayPriority.LANGUAGE_SETTINGS
  }

  companion object {
    private val ATTRS: Array<AttributesDescriptor> = arrayOf(
      AttributesDescriptor(Angular2Bundle.message("angular.colors.banana-binding"),
                           Angular2HtmlHighlighterColors.NG_BANANA_BINDING_ATTR_NAME),
      AttributesDescriptor(Angular2Bundle.message("angular.colors.event-binding"),
                           Angular2HtmlHighlighterColors.NG_EVENT_BINDING_ATTR_NAME),
      AttributesDescriptor(Angular2Bundle.message("angular.colors.property-binding"),
                           Angular2HtmlHighlighterColors.NG_PROPERTY_BINDING_ATTR_NAME),
      AttributesDescriptor(Angular2Bundle.message("angular.colors.template-binding"),
                           Angular2HtmlHighlighterColors.NG_TEMPLATE_BINDINGS_ATTR_NAME),
      AttributesDescriptor(Angular2Bundle.message("angular.colors.interpolation-delimiter"),
                           Angular2HtmlHighlighterColors.NG_INTERPOLATION_DELIMITER),
      AttributesDescriptor(Angular2Bundle.message("angular.colors.expansion-form"),
                           Angular2HtmlHighlighterColors.NG_EXPANSION_FORM),
      AttributesDescriptor(Angular2Bundle.message("angular.colors.expansion-form-comma"),
                           Angular2HtmlHighlighterColors.NG_EXPANSION_FORM_COMMA),
      AttributesDescriptor(Angular2Bundle.message("angular.colors.expansion-form-delimiter"),
                           Angular2HtmlHighlighterColors.NG_EXPANSION_FORM_DELIMITER),
      AttributesDescriptor(Angular2Bundle.message("angular.colors.template-expression"),
                           Angular2HtmlHighlighterColors.NG_EXPRESSION),
      AttributesDescriptor(Angular2Bundle.message("angular.colors.ref-var"),
                           Angular2HtmlHighlighterColors.NG_REFERENCE_ATTR_NAME))

  }
}