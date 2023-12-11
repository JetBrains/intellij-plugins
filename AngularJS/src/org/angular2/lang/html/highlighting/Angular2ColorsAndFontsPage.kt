// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.highlighting

import com.intellij.lang.Language
import com.intellij.lang.javascript.highlighting.JSHighlighter
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.RainbowColorSettingsPage
import com.intellij.psi.codeStyle.DisplayPriority
import com.intellij.psi.codeStyle.DisplayPrioritySortable
import icons.AngularJSIcons
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.expr.highlighting.Angular2HighlighterColors
import org.angular2.lang.html.Angular17HtmlFileType
import org.angular2.lang.html.Angular17HtmlLanguage
import javax.swing.Icon

class Angular2ColorsAndFontsPage : RainbowColorSettingsPage, DisplayPrioritySortable {
  override fun getDisplayName(): String {
    return Angular2Bundle.message("angular.configurable.name.angular.template")
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
    return SyntaxHighlighterFactory.getSyntaxHighlighter(Angular17HtmlFileType.INSTANCE, null, null)!!
  }

  override fun getDemoText(): String {
    return """
      <li attr="value"
          *ngFor="let <ng-variable>hero</ng-variable> of <instance_variable>heroes</instance_variable> as <ng-variable>test</ng-variable>"
          [class.selected]="<ng-variable>hero</ng-variable> === <instance_variable>selectedHero</instance_variable>"
          (click)="<instance_method>onSelect</instance_method>(<ng-variable>hero</ng-variable>)">
        {{<ng-variable>hero</ng-variable>.<instance_variable>name</instance_variable>}}
      </li>
      
      @for (<ng-variable>hero</ng-variable> of <instance_variable>heroes</instance_variable>; track <instance_variable>heroes</instance_variable>.<instance_variable>name</instance_variable>) {
        {{<ng-variable>hero</ng-variable>.<instance_variable>name</instance_variable>}}
      } @empty {
        <p>No heroes!</p>
      }
      
      <div *ngIf="<ng-signal>heroSig</ng-signal>() as <ng-variable>hero</ng-variable>">{{<ng-variable>hero</ng-variable>.<instance_variable>name</instance_variable>}}</div>

      {<instance_variable>heroes</instance_variable>.<instance_variable>length</instance_variable>, plural, =0 {no heroes} =1 {one hero} other {{{<instance_variable>heroes</instance_variable>.<instance_variable>length</instance_variable>}} heroes}} found

      <input [(ngModel)]="<instance_variable>selectedHero</instance_variable>.<instance_variable>name</instance_variable>" #<ng-variable>model</ng-variable>="ngModel"/>
      
      """.trimIndent()
  }

  override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> {
    return tags
  }

  override fun getPriority(): DisplayPriority {
    return DisplayPriority.LANGUAGE_SETTINGS
  }

  override fun isRainbowType(type: TextAttributesKey): Boolean {
    return JSHighlighter.JS_PARAMETER == type
           || JSHighlighter.JS_LOCAL_VARIABLE == type
           || JSHighlighter.JS_INSTANCE_MEMBER_VARIABLE == type
           || Angular2HighlighterColors.NG_SIGNAL == type
           || Angular2HighlighterColors.NG_VARIABLE == type
  }

  override fun getLanguage(): Language =
    Angular17HtmlLanguage.INSTANCE

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
      AttributesDescriptor(Angular2Bundle.message("angular.colors.signal"),
                           Angular2HighlighterColors.NG_SIGNAL),
      AttributesDescriptor(Angular2Bundle.message("angular.colors.template-variable"),
                           Angular2HighlighterColors.NG_VARIABLE),
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
      AttributesDescriptor(Angular2Bundle.message("angular.colors.block-name"),
                           Angular2HtmlHighlighterColors.NG_BLOCK_NAME),
      AttributesDescriptor(Angular2Bundle.message("angular.colors.block-braces"),
                           Angular2HtmlHighlighterColors.NG_BLOCK_BRACES),
    )

    private val tags = mapOf(
      "ng-signal" to Angular2HighlighterColors.NG_SIGNAL,
      "ng-variable" to Angular2HighlighterColors.NG_VARIABLE,
      "instance_variable" to JSHighlighter.JS_INSTANCE_MEMBER_VARIABLE,
      "instance_method" to JSHighlighter.JS_INSTANCE_MEMBER_FUNCTION,
    )
  }
}