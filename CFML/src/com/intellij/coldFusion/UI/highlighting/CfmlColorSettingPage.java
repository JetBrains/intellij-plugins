/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
 * Date: 06.10.2008
 */
public class CfmlColorSettingPage implements ColorSettingsPage {
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

  private static final ColorDescriptor[] COLORS = new ColorDescriptor[0];

  @NotNull
  public String getDisplayName() {
    //noinspection HardCodedStringLiteral
    return "CFML";
  }

  public Icon getIcon() {
    return CfmlFileType.INSTANCE.getIcon();
  }

  @NotNull
  public AttributesDescriptor[] getAttributeDescriptors() {
    return ATTRS;
  }

  @NotNull
  public ColorDescriptor[] getColorDescriptors() {
    return COLORS;
  }

  @NotNull
  public SyntaxHighlighter getHighlighter() {
    return new CfmlHighlighter.CfmlFileHighlighter(null);
  }

  @NotNull
  public String getDemoText() {
    return "<cffunction name=\"test\">\n" +
           "\t<cfargument name=\"fred\" test=\"test\"/>\n" +
           "\t<cfscript>\n" +
           "\t\tWriteOutput(\"FREDFREDFRED\");\n" +
           "\t</cfscript>\n" +
           "\t<cfif thisisatest is 1>\n" +
           "\t\t<cfoutput>asdfasdf</cfoutput>\n" +
           "\t</cfif>\n" +
           "</cffunction>\n" +
           "<cfset somethinghere = 2/>\n" +
           "<cfset test(fred)/>\n" +
           "<cffunction name=\"test\" >\n" +
           "\t<cfargument name=\"test\" default=\"#WriteOutput(\"\"?\"\")#\"/> <!--- I think this is valid! --->\n" +
           "</cffunction>\n" +
           "<cfoutput>\n" +
           "\tThis is a test\n" +
           "</cfoutput>\n" +
           "<cfsciprt>\n" +
           "\tif(find(\"some text\", agent ) and not find(\"some other\", agent ))\n" +
           "\t{\n" +
           "\t\t// comment string\n" +
           "\t\tmyResult = reFind(\"some text ([5-9]\\.[0-9])\", sAgent, 1, true );\n" +
           "\t}\n" +
           "</cfscript>";
  }

  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return null;
  }
}
