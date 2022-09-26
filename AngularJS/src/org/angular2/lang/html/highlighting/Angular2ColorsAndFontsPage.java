// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.highlighting;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.psi.codeStyle.DisplayPriority;
import com.intellij.psi.codeStyle.DisplayPrioritySortable;
import icons.AngularJSIcons;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.html.Angular2HtmlFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

import static org.angular2.lang.html.highlighting.Angular2HtmlHighlighterColors.*;

public class Angular2ColorsAndFontsPage implements ColorSettingsPage, DisplayPrioritySortable {
  private static final AttributesDescriptor[] ATTRS;

  static {
    ATTRS = new AttributesDescriptor[]{
      new AttributesDescriptor(Angular2Bundle.message("angular.colors.banana-binding"), NG_BANANA_BINDING_ATTR_NAME),
      new AttributesDescriptor(Angular2Bundle.message("angular.colors.event-binding"), NG_EVENT_BINDING_ATTR_NAME),
      new AttributesDescriptor(Angular2Bundle.message("angular.colors.property-binding"), NG_PROPERTY_BINDING_ATTR_NAME),
      new AttributesDescriptor(Angular2Bundle.message("angular.colors.template-binding"), NG_TEMPLATE_BINDINGS_ATTR_NAME),
      new AttributesDescriptor(Angular2Bundle.message("angular.colors.interpolation-delimiter"), NG_INTERPOLATION_DELIMITER),
      new AttributesDescriptor(Angular2Bundle.message("angular.colors.expansion-form"), NG_EXPANSION_FORM),
      new AttributesDescriptor(Angular2Bundle.message("angular.colors.expansion-form-comma"), NG_EXPANSION_FORM_COMMA),
      new AttributesDescriptor(Angular2Bundle.message("angular.colors.expansion-form-delimiter"), NG_EXPANSION_FORM_DELIMITER),
      new AttributesDescriptor(Angular2Bundle.message("angular.colors.template-expression"), NG_EXPRESSION),
      new AttributesDescriptor(Angular2Bundle.message("angular.colors.ref-var"), NG_REFERENCE_ATTR_NAME),
    };
  }

  @Override
  public @NotNull String getDisplayName() {
    //noinspection HardCodedStringLiteral
    return "Angular Template";
  }

  @Override
  public Icon getIcon() {
    return AngularJSIcons.Angular2;
  }

  @Override
  public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
    return ATTRS;
  }

  @Override
  public ColorDescriptor @NotNull [] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @Override
  public @NotNull SyntaxHighlighter getHighlighter() {
    final SyntaxHighlighter highlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(Angular2HtmlFileType.INSTANCE, null, null);
    assert highlighter != null;
    return highlighter;
  }

  @Override
  public @NotNull String getDemoText() {
    return """
      <li attr="value"
          *ngFor="let hero of heroes as test"
          [class.selected]="hero === selectedHero"
          (click)="onSelect(hero)">
        {{hero.name}}
      </li>

      {heroes.length, plural, =0 {no heroes} =1 {one hero} other {{{heroes.length}} heroes}} found

      <input [(ngModel)]="hero.name" #model="ngModel"/>
      """;
  }

  @Override
  public @Nullable Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return null;
  }

  @Override
  public DisplayPriority getPriority() {
    return DisplayPriority.LANGUAGE_SETTINGS;
  }
}
