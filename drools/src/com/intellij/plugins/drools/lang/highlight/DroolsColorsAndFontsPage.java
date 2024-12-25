// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.highlight;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.plugins.drools.DroolsBundle;
import com.intellij.plugins.drools.JbossDroolsIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

import static com.intellij.plugins.drools.lang.highlight.DroolsSyntaxHighlighterColors.*;

public final class DroolsColorsAndFontsPage implements ColorSettingsPage {
  private static final AttributesDescriptor[] ATTRS;

  private static final @NonNls Map<String, TextAttributesKey> ourTags = new HashMap<>();

  static {
    ATTRS = new AttributesDescriptor[]{
      new AttributesDescriptor(DroolsBundle.message("drools.color.settings.description.line.comment"), LINE_COMMENT),
      new AttributesDescriptor(DroolsBundle.message("drools.color.settings.description.block.comment"), BLOCK_COMMENT),
      new AttributesDescriptor(DroolsBundle.message("drools.color.settings.description.doc.comment"), DOC_COMMENT),
      new AttributesDescriptor(DroolsBundle.message("drools.color.settings.description.keyword"), KEYWORD),
      new AttributesDescriptor(DroolsBundle.message("drools.color.settings.description.number"), NUMBER),
      new AttributesDescriptor(DroolsBundle.message("drools.color.settings.description.string"),STRING),
      new AttributesDescriptor(DroolsBundle.message("drools.color.settings.description.operator"), OPERATION_SIGN),
      new AttributesDescriptor(DroolsBundle.message("drools.color.settings.description.operations"), OPERATIONS),
      new AttributesDescriptor(DroolsBundle.message("drools.color.settings.description.parenths"), PARENTHS),
      new AttributesDescriptor(DroolsBundle.message("drools.color.settings.description.brackets"), BRACKETS),
      new AttributesDescriptor(DroolsBundle.message("drools.color.settings.description.braces"), BRACES),
      new AttributesDescriptor(DroolsBundle.message("drools.color.settings.description.comma"), COMMA),
      new AttributesDescriptor(DroolsBundle.message("drools.color.settings.description.dot"), DOT),
      new AttributesDescriptor(DroolsBundle.message("drools.color.settings.description.semicolon"), SEMICOLON),
      new AttributesDescriptor(DroolsBundle.message("drools.color.settings.description.bad.character"), BAD_CHARACTER),
      new AttributesDescriptor(DroolsBundle.message("drools.color.settings.description.public.static.field"), PUBLIC_STATIC_FIELD),
      new AttributesDescriptor(DroolsBundle.message("drools.color.settings.description.local.function"), FUNCTION),
      new AttributesDescriptor(DroolsBundle.message("drools.color.settings.description.local.variable"), LOCAL_VARIABLE),
      new AttributesDescriptor(DroolsBundle.message("drools.color.settings.description.rule"), RULE),
      new AttributesDescriptor(DroolsBundle.message("drools.color.settings.description.attribute"), ATTRIBUTES),
      new AttributesDescriptor(DroolsBundle.message("drools.color.settings.description.keyword.operation"), KEYWORD_OPERATIONS),
    };

    ourTags.put("rule", RULE);
    ourTags.put("function", FUNCTION);
    ourTags.put("local.variable", LOCAL_VARIABLE);
    ourTags.put("public.static.field", PUBLIC_STATIC_FIELD);
  }

  @Override
  public @NotNull String getDisplayName() {
    return DroolsBundle.DROOLS;
  }

  @Override
  public Icon getIcon() {
    return JbossDroolsIcons.Drools_16;
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
    return new DroolsSyntaxHighlighter();
  }

  @Override
  public @NotNull String getDemoText() {
    return """
      /*
       * Copyright 2010 JBoss Inc
       */
      package org.drools.examples.fibonacci ;

      import org.drools.examples.fibonacci.FibonacciExample.Fibonacci;

       // line comment
       dialect  "mvel"
      salience 10
      rule <rule>Recurse</rule>

          salience 10
          when
              not  (Fibonacci (sequence == 1 ) )
              <local.variable>f</local.variable> : Fibonacci  (value == -1 )
          then
              insert( new org.drools.examples.fibonacci.FibonacciExample.Fibonacci ( f.sequence - 1 ) );
              System.out.println( "recurse for "  +  f.sequence );
      end\s
      function String <function>createString</function> (String name) {
          return "Hi, " + name;
      }""";
  }

  @Override
  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return ourTags;
  }
}
