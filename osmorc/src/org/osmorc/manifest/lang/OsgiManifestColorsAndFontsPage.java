/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osmorc.manifest.lang;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import icons.OsmorcIdeaIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.lang.manifest.highlighting.ManifestColorsAndFonts;
import org.jetbrains.lang.manifest.highlighting.ManifestSyntaxHighlighterFactory;
import org.osmorc.i18n.OsmorcBundle;

import javax.swing.Icon;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public final class OsgiManifestColorsAndFontsPage implements ColorSettingsPage {
  private static final AttributesDescriptor[] ATTRIBUTE_DESCRIPTORS = {
    new AttributesDescriptor(OsmorcBundle.message("settings.colors.header.name"), ManifestColorsAndFonts.HEADER_NAME_KEY),
    new AttributesDescriptor(OsmorcBundle.message("settings.colors.header.assignment"), ManifestColorsAndFonts.HEADER_ASSIGNMENT_KEY),
    new AttributesDescriptor(OsmorcBundle.message("settings.colors.header.value"), ManifestColorsAndFonts.HEADER_VALUE_KEY),
    new AttributesDescriptor(OsmorcBundle.message("settings.colors.attribute.name"), OsgiManifestColorsAndFonts.ATTRIBUTE_NAME_KEY),
    new AttributesDescriptor(OsmorcBundle.message("settings.colors.attribute.assignment"), OsgiManifestColorsAndFonts.ATTRIBUTE_ASSIGNMENT_KEY),
    new AttributesDescriptor(OsmorcBundle.message("settings.colors.attribute.value"), OsgiManifestColorsAndFonts.ATTRIBUTE_VALUE_KEY),
    new AttributesDescriptor(OsmorcBundle.message("settings.colors.directive.name"), OsgiManifestColorsAndFonts.DIRECTIVE_NAME_KEY),
    new AttributesDescriptor(OsmorcBundle.message("settings.colors.directive.assignment"), OsgiManifestColorsAndFonts.DIRECTIVE_ASSIGNMENT_KEY),
    new AttributesDescriptor(OsmorcBundle.message("settings.colors.directive.value"), OsgiManifestColorsAndFonts.DIRECTIVE_VALUE_KEY),
    new AttributesDescriptor(OsmorcBundle.message("settings.colors.clause.separator"), OsgiManifestColorsAndFonts.CLAUSE_SEPARATOR_KEY),
    new AttributesDescriptor(OsmorcBundle.message("settings.colors.parameter.separator"), OsgiManifestColorsAndFonts.PARAMETER_SEPARATOR_KEY)
  };

  private static final Map<String, TextAttributesKey> ADDITIONAL_HIGHLIGHTING;
  static {
    ADDITIONAL_HIGHLIGHTING = new HashMap<>();
    ADDITIONAL_HIGHLIGHTING.put("attributeName", OsgiManifestColorsAndFonts.ATTRIBUTE_NAME_KEY);
    ADDITIONAL_HIGHLIGHTING.put("attributeAssignment", OsgiManifestColorsAndFonts.ATTRIBUTE_ASSIGNMENT_KEY);
    ADDITIONAL_HIGHLIGHTING.put("attributeValue", OsgiManifestColorsAndFonts.ATTRIBUTE_VALUE_KEY);
    ADDITIONAL_HIGHLIGHTING.put("directiveName", OsgiManifestColorsAndFonts.DIRECTIVE_NAME_KEY);
    ADDITIONAL_HIGHLIGHTING.put("directiveAssignment", OsgiManifestColorsAndFonts.DIRECTIVE_ASSIGNMENT_KEY);
    ADDITIONAL_HIGHLIGHTING.put("directiveValue", OsgiManifestColorsAndFonts.DIRECTIVE_VALUE_KEY);
    ADDITIONAL_HIGHLIGHTING.put("clauseSeparator", OsgiManifestColorsAndFonts.CLAUSE_SEPARATOR_KEY);
    ADDITIONAL_HIGHLIGHTING.put("parameterSeparator", OsgiManifestColorsAndFonts.PARAMETER_SEPARATOR_KEY);
  }

  @Override
  public @NotNull String getDisplayName() {
    return OsmorcBundle.message("settings.colors.title");
  }

  @Override
  public @Nullable Icon getIcon() {
    return OsmorcIdeaIcons.Osgi;
  }

  @Override
  public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
    return ATTRIBUTE_DESCRIPTORS;
  }

  @Override
  public @Nullable Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return ADDITIONAL_HIGHLIGHTING;
  }

  @Override
  public ColorDescriptor @NotNull [] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @Override
  public @NotNull SyntaxHighlighter getHighlighter() {
    return ManifestSyntaxHighlighterFactory.HIGHLIGHTER;
  }

  @Override
  public @NotNull String getDemoText() {
    return """
      Manifest-Version: 1.0
      Bundle-Name: Osmorc Test
      Bundle-SymbolicName: org.osmorc.test<parameterSeparator>;</parameterSeparator><directiveName>singleton</directiveName><directiveAssignment>:=</directiveAssignment><directiveValue>true</directiveValue>
      Bundle-Version: 0.1.0
      Require-Bundle: some.bundle<parameterSeparator>;</parameterSeparator><attributeName>bundle-version</attributeName><attributeAssignment>=</attributeAssignment><attributeValue>"2.0.0"</attributeValue><clauseSeparator>,</clauseSeparator>
       other.bundle""";
  }
}
