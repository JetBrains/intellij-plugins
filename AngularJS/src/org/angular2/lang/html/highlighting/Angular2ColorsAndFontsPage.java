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
import org.angular2.lang.html.Angular2HtmlFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

import static org.angular2.lang.html.highlighting.Angular2HtmlFileHighlighter.*;

public class Angular2ColorsAndFontsPage implements ColorSettingsPage, DisplayPrioritySortable {
  private static final AttributesDescriptor[] ATTRS;

  static {
    ATTRS = new AttributesDescriptor[]{
      new AttributesDescriptor("Two way binding `[()]`", NG_BANANA_BINDING_ATTR_NAME),
      new AttributesDescriptor("Event binding `()`", NG_EVENT_BINDING_ATTR_NAME),
      new AttributesDescriptor("Property binding `[]`", NG_PROPERTY_BINDING_ATTR_NAME),
      new AttributesDescriptor("Structural directive binding `*`", NG_TEMPLATE_BINDINGS_ATTR_NAME),
      new AttributesDescriptor("Interpolation delimiter", NG_INTERPOLATION_DELIMITER),
      new AttributesDescriptor("Plural expression", NG_EXPANSION_FORM),
      new AttributesDescriptor("Plural expression comma", NG_EXPANSION_FORM_COMMA),
      new AttributesDescriptor("Plural expression delimiter", NG_EXPANSION_FORM_DELIMITER),
      new AttributesDescriptor("Template expression", NG_EXPRESSION),
      new AttributesDescriptor("Template reference variable `#`", NG_REFERENCE_ATTR_NAME),
    };
  }

  @Override
  @NotNull
  public String getDisplayName() {
    //noinspection HardCodedStringLiteral
    return "Angular Template";
  }

  @Override
  public Icon getIcon() {
    return AngularJSIcons.Angular2;
  }

  @Override
  @NotNull
  public AttributesDescriptor[] getAttributeDescriptors() {
    return ATTRS;
  }

  @Override
  @NotNull
  public ColorDescriptor[] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @Override
  @NotNull
  public SyntaxHighlighter getHighlighter() {
    final SyntaxHighlighter highlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(Angular2HtmlFileType.INSTANCE, null, null);
    assert highlighter != null;
    return highlighter;
  }

  @Override
  @NotNull
  public String getDemoText() {
    return "<li attr=\"value\"\n" +
           "    *ngFor=\"let hero of heroes as test\"\n" +
           "    [class.selected]=\"hero === selectedHero\"\n" +
           "    (click)=\"onSelect(hero)\">\n" +
           "  {{hero.name}}\n" +
           "</li>\n" +
           "\n" +
           "{heroes.length, plural, =0 {no heroes} =1 {one hero} other {{{heroes.length}} heroes}} found\n" +
           "\n" +
           "<input [(ngModel)]=\"hero.name\" #model=\"ngModel\"/>\n";
  }

  @Nullable
  @Override
  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return null;
  }

  @Override
  public DisplayPriority getPriority() {
    return DisplayPriority.LANGUAGE_SETTINGS;
  }
}
