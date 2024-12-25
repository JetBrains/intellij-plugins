// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.UI.highlighting;

import com.intellij.coldFusion.CfmlBundle;
import com.intellij.coldFusion.model.files.CfmlFileType;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Map;

/**
 * Created by Lera Nikolaenko
 */
public final class CfmlColorSettingPage implements ColorSettingsPage {
  private static final AttributesDescriptor[] ATTRS;

  static {
    ATTRS = new AttributesDescriptor[]{
      new AttributesDescriptor(CfmlBundle.message("cfml.attribute"),
                               CfmlHighlighter.CfmlFileHighlighter.CFML_ATTRIBUTE),
      new AttributesDescriptor(CfmlBundle.message("cfml.comment"),
                               CfmlHighlighter.CfmlFileHighlighter.CFML_COMMENT),
      new AttributesDescriptor(CfmlBundle.message("cfml.tag.name"),
                               CfmlHighlighter.CfmlFileHighlighter.CFML_TAG_NAME),
      new AttributesDescriptor(CfmlBundle.message("cfml.bracket"),
                               CfmlHighlighter.CfmlFileHighlighter.CFML_BRACKETS),
      new AttributesDescriptor(CfmlBundle.message("cfml.operator"),
                               CfmlHighlighter.CfmlFileHighlighter.CFML_OPERATOR),
      new AttributesDescriptor(CfmlBundle.message("cfml.string"),
                               CfmlHighlighter.CfmlFileHighlighter.CFML_STRING),
      new AttributesDescriptor(CfmlBundle.message("cfml.number"),
                               CfmlHighlighter.CfmlFileHighlighter.CFML_NUMBER),
      new AttributesDescriptor(CfmlBundle.message("cfml.identifier"),
                               CfmlHighlighter.CfmlFileHighlighter.CFML_IDENTIFIER),
      new AttributesDescriptor(CfmlBundle.message("cfml.badcharacter"),
                               CfmlHighlighter.CfmlFileHighlighter.CFML_BAD_CHARACTER),
      new AttributesDescriptor(CfmlBundle.message("cfml.sharp"),
                               CfmlHighlighter.CfmlFileHighlighter.CFML_SHARP),
      new AttributesDescriptor(CfmlBundle.message("cfml.keyword"),
                               CfmlHighlighter.CfmlFileHighlighter.CFML_KEYWORD)
    };
  }

  @Override
  public @NotNull String getDisplayName() {
    //noinspection HardCodedStringLiteral
    return "CFML";
  }

  @Override
  public Icon getIcon() {
    return CfmlFileType.INSTANCE.getIcon();
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
    return new CfmlHighlighter.CfmlFileHighlighter(null);
  }

  @Override
  public @NotNull String getDemoText() {
    return """
      <cffunction name="test">
      \t<cfargument name="fred" test="test"/>
      \t<cfscript>
      \t\tWriteOutput("FREDFREDFRED");
      \t</cfscript>
      \t<cfif thisisatest is 1>
      \t\t<cfoutput>asdfasdf</cfoutput>
      \t</cfif>
      </cffunction>
      <cfset somethinghere = 2/>
      <cfset test(fred)/>
      <cffunction name="test" >
      \t<cfargument name="test" default="#WriteOutput(""?"")#"/> <!--- I think this is valid! --->
      </cffunction>
      <cfoutput>
      \tThis is a test
      </cfoutput>
      <cfscript>
      \tif(find("some text", agent ) and not find("some other", agent ))
      \t{
      \t\t// comment string
      \t\tmyResult = reFind("some text ([5-9]\\.[0-9])", sAgent, 1, true );
      \t}
      </cfscript>""";
  }

  @Override
  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return null;
  }
}
