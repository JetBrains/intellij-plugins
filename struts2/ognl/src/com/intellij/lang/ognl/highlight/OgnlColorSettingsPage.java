/*
 * Copyright 2015 The authors
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

package com.intellij.lang.ognl.highlight;

import com.intellij.lang.ognl.OgnlFileType;
import com.intellij.lang.ognl.OgnlLanguage;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Yann C&eacute;bron
 */
public class OgnlColorSettingsPage implements ColorSettingsPage {

  private static final AttributesDescriptor[] ATTRIBUTES_DESCRIPTORS = new AttributesDescriptor[]{
    new AttributesDescriptor("Background", OgnlHighlighter.BACKGROUND),
    new AttributesDescriptor("Expression bounds", OgnlHighlighter.EXPRESSION_BOUNDS),
    new AttributesDescriptor("Keyword", OgnlHighlighter.KEYWORDS),
    new AttributesDescriptor("Operations", OgnlHighlighter.OPERATIONS),
    new AttributesDescriptor("Identifier", OgnlHighlighter.IDENTIFIER),
    new AttributesDescriptor("String", OgnlHighlighter.STRING),
    new AttributesDescriptor("Number", OgnlHighlighter.NUMBER),
    new AttributesDescriptor("Comma", OgnlHighlighter.COMMA),
    new AttributesDescriptor("Parentheses", OgnlHighlighter.PARENTHESES),
    new AttributesDescriptor("Brackets", OgnlHighlighter.BRACKETS),
    new AttributesDescriptor("Braces", OgnlHighlighter.BRACES),
    new AttributesDescriptor("Class reference", OgnlHighlighter.FQN_TYPE)
  };

  @NotNull
  @Override
  public String getDisplayName() {
    return OgnlLanguage.INSTANCE.getDisplayName();
  }

  @Override
  public Icon getIcon() {
    return OgnlFileType.INSTANCE.getIcon();
  }

  @Override
  public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
    return ATTRIBUTES_DESCRIPTORS;
  }

  @Override
  public ColorDescriptor @NotNull [] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @NotNull
  @Override
  public SyntaxHighlighter getHighlighter() {
    return new OgnlHighlighter();
  }

  @NotNull
  @Override
  public String getDemoText() {
    return """
      %{booleanArray[3] == true ? this : 'nothing'}
      %{"valid escapes: My App\\nVersion 1.0 \\u00a9 2015 My Company"}
      %{"invalid escape: \\uXXX "}
      %{ 1 shl 3 && 3 gt 5}
      %{myAction.method()}
      %{#myBigInteger * 452H}
      %{id not in {1, 2}}
      %{listeners.{? #this instanceof <fqnType>ActionListener</fqnType>}}""";
  }

  @Override
  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    Map<String, TextAttributesKey> additionalMap = new HashMap<>();
    additionalMap.put("fqnType", OgnlHighlighter.FQN_TYPE);
    return additionalMap;
  }
}