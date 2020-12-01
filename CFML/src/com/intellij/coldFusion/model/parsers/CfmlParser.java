// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model.parsers;

import com.intellij.coldFusion.CfmlBundle;
import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.Stack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.coldFusion.model.lexer.CfmlTokenTypes.*;

/**
 * Created by Lera Nikolaenko
 */
public class CfmlParser implements PsiParser {

  private static class Tag {
    public String myTagName;
    public PsiBuilder.Marker myMarkerOfBegin;
    public PsiBuilder.Marker myMarkerOfContent;

    Tag(String string, PsiBuilder.Marker marker, PsiBuilder.Marker content) {
      myTagName = string;
      myMarkerOfBegin = marker;
      myMarkerOfContent = content;
    }
  }

  public static IElementType getElementTypeForTag(@NotNull String tagName) {
    if ("cfcomponent".equals(StringUtil.toLowerCase(tagName)) || "cfinterface".equals(StringUtil.toLowerCase(tagName))) {
      return CfmlElementTypes.COMPONENT_TAG;
    }
    else if ("cffunction".equals(StringUtil.toLowerCase(tagName))) {
      return CfmlElementTypes.FUNCTION_TAG;
    }
    else if ("cfinvoke".equals(StringUtil.toLowerCase(tagName))) {
      return CfmlElementTypes.INVOKE_TAG;
    }
    else if ("cfargument".equals(StringUtil.toLowerCase(tagName))) {
      return CfmlElementTypes.ARGUMENT_TAG;
    }
    else if ("cfscript".equals(StringUtil.toLowerCase(tagName))) {
      return CfmlElementTypes.SCRIPT_TAG;
    }
    else if ("cfproperty".equals(StringUtil.toLowerCase(tagName))) {
      return CfmlElementTypes.PROPERTY_TAG;
    }
    else if ("cfimport".equals(StringUtil.toLowerCase(tagName))) {
      return CfmlElementTypes.TAG_IMPORT;
    }
    else if ("cfloop".endsWith(StringUtil.toLowerCase(tagName))) {
      return CfmlElementTypes.FORTAGEXPRESSION;
    }
    return CfmlElementTypes.TAG;
  }

  @Override
  @NotNull
  public ASTNode parse(final IElementType root, final PsiBuilder builder) {
    Stack<Tag> tagNamesStack = new Stack<>();
    // builder.setDebugMode(true);
    final PsiBuilder.Marker marker = builder.mark();
    // parse component
    if (builder.getTokenType() == CfscriptTokenTypes.COMMENT ||
        builder.getTokenType() == CfscriptTokenTypes.COMPONENT_KEYWORD ||
        builder.getTokenType() == CfscriptTokenTypes.INTERFACE_KEYWORD ||
        builder.getTokenType() == CfscriptTokenTypes.IMPORT_KEYWORD) {
      (new CfscriptParser()).parseScript(builder, true);
      if (!builder.eof()) {
        builder.error(CfmlBundle.message("cfml.parsing.unexpected.token"));
      }
      while (!builder.eof()) {
        builder.advanceLexer();
      }
    }
    else {
      while (!builder.eof()) {
        if (builder.getTokenType() == OPENER) {
          parseOpenTag(builder, tagNamesStack);
        }
        else if (builder.getTokenType() == LSLASH_ANGLEBRACKET) {
          parseCloseTag(builder, tagNamesStack);
        }
        else {
          builder.advanceLexer();
        }
      }
      while (!tagNamesStack.isEmpty()) {
        Tag tag = tagNamesStack.pop();
        // tag.myMarkerOfBegin.drop();
        if (CfmlUtil.isUserDefined(tag.myTagName) || !CfmlUtil.isEndTagRequired(tag.myTagName, builder.getProject())) {
          tag.myMarkerOfBegin.doneBefore(getElementTypeForTag(tag.myTagName), tag.myMarkerOfContent);
        }
        else {
          tag.myMarkerOfBegin.doneBefore(getElementTypeForTag(tag.myTagName), tag.myMarkerOfContent,
                                         CfmlBundle.message("cfml.parsing.element.is.not.closed", tag.myTagName));
        }
        tag.myMarkerOfContent.drop();
      }
    }
    marker.done(root);
    return builder.getTreeBuilt();
  }

  private static void parseExpression(PsiBuilder builder) {
    if (builder.getTokenType() == START_EXPRESSION) {
      builder.advanceLexer();
    }
    else {
      return;
    }
    if (!(new CfmlExpressionParser(builder)).parseStructureDefinition()) {
      if (!(new CfmlExpressionParser(builder)).parseArrayDefinition()) {
        (new CfmlExpressionParser(builder)).parseExpression();
      }
    }

    if (builder.getTokenType() != END_EXPRESSION) {
      builder.error(CfmlBundle.message("cfml.parsing.expression.unclosed"));
    }
    else if (!builder.eof()) {
      builder.advanceLexer();
    }
  }

  private static void readValue(PsiBuilder builder, IElementType typeOfValue) {
    // reading string
    if (typeOfValue != null) {
      if (builder.getTokenType() == SINGLE_QUOTE || builder.getTokenType() == DOUBLE_QUOTE) {
        builder.advanceLexer();
        PsiBuilder.Marker marker = builder.mark();
        int valueStartOffset = builder.getCurrentOffset();
        while (!builder.eof() &&
               builder.getTokenType() != SINGLE_QUOTE_CLOSER &&
               builder.getTokenType() != DOUBLE_QUOTE_CLOSER &&
               !CfmlUtil.isControlToken(builder.getTokenType())) {
          if (builder.getTokenType() == START_EXPRESSION) {
            parseExpression(builder);
          }
          else {
            builder.advanceLexer();
          }
        }
        if (builder.getCurrentOffset() != valueStartOffset) {
          marker.done(typeOfValue);
        }
        else {
          marker.drop();
        }
        if (builder.getTokenType() != SINGLE_QUOTE_CLOSER &&
            builder.getTokenType() != DOUBLE_QUOTE_CLOSER) {
          builder.error(CfmlBundle.message("cfml.parsing.unexpected.token"));
        }
        else {
          builder.advanceLexer();
        }
        return;
      }
      else if (!CfmlUtil.isControlToken(builder.getTokenType()) && builder.getTokenType() != ATTRIBUTE) {
        (new CfmlExpressionParser(builder)).parseExpression();
        return;
      }
    }
    // reading what is comming up to the next control token
    while (!builder.eof() && !CfmlUtil.isControlToken(builder.getTokenType()) && builder.getTokenType() != ATTRIBUTE) {
      if (builder.getTokenType() == START_EXPRESSION) {
        parseExpression(builder);
      }
      else {
        builder.advanceLexer();
      }
    }
  }

  private static boolean doNeedNamedAttribute(String tagName) {
    if (tagName == null) {
      return false;
    }
    return !(StringUtil.toLowerCase(tagName).equals("cffunction") ||
             StringUtil.toLowerCase(tagName).equals("cfargument"));
  }

  public static void parseAttributes(PsiBuilder builder, String tagName, IElementType attributeType, boolean strict) {
    if (tagName.equals("cfset")) {
      // parsing statement int cfset tag
      (new CfmlExpressionParser(builder)).parseStatement();
      return;
    }
    else if (tagName.equals("cfif") || tagName.equals("cfelseif") || tagName.equals("cfreturn")) {
      // parsin expression in condition tags
      (new CfmlExpressionParser(builder)).parseExpression();
      return;
    }

    while (!builder.eof() && !CfmlUtil.isControlToken(builder.getTokenType())) {
      if (builder.getTokenType() == attributeType ||
          builder.getTokenType() == CfscriptTokenTypes.DEFAULT_KEYWORD ||
          (tagName.equalsIgnoreCase("cfproperty") && builder.getTokenType() ==
                                                     CfscriptTokenTypes.ABORT_KEYWORD)) {
        @SuppressWarnings({"ConstantConditions"}) String attributeName = StringUtil.toLowerCase(builder.getTokenText());
        PsiBuilder.Marker attrMarker = builder.mark();
        // PsiBuilder.Marker attrNameMarker = myBuilder.mark();
        builder.advanceLexer();
        if (builder.getTokenType() != ASSIGN) {
          attrMarker.done(CfmlElementTypes.ATTRIBUTE);
          builder.error(CfmlBundle.message("cfml.parsing.no.value"));
          continue;
        }
        builder.advanceLexer();
        if ("name".equals(attributeName)) {
          readValue(builder, CfmlElementTypes.ATTRIBUTE_VALUE);
          if (doNeedNamedAttribute(tagName)) {
            attrMarker.done(CfmlElementTypes.NAMED_ATTRIBUTE);
          }
          else {
            attrMarker.done(CfmlElementTypes.ATTRIBUTE_NAME);
          }
        }
        else if ("method".equals(attributeName) && "cfinvoke".equals(tagName)) {
          readValue(builder, CfmlElementTypes.REFERENCE_EXPRESSION);
          attrMarker.done(CfmlElementTypes.TAG_FUNCTION_CALL);
        }
        else if ("index".equals(attributeName) && "cfloop".equals(tagName)) {
          readValue(builder, CfmlElementTypes.ATTRIBUTE_VALUE);
          attrMarker.done(CfmlElementTypes.FORTAGINDEXATTRIBUTE);
        }
        else {
          readValue(builder, CfmlElementTypes.ATTRIBUTE_VALUE);
          attrMarker.done(CfmlElementTypes.ATTRIBUTE);
        }
      }
      else {
        if (strict) {
          return;
        }
        builder.advanceLexer();
      }
    }
  }


  private static boolean parseCloser(PsiBuilder builder) {
    if (!builder.eof() && !CfmlUtil.isControlToken(builder.getTokenType())) {
      builder.error(CfmlBundle.message("cfml.parsing.unexpected.token"));
      builder.advanceLexer();
      while (!builder.eof() && !CfmlUtil.isControlToken(builder.getTokenType())) {
        builder.advanceLexer();
      }
    }
    if (builder.getTokenType() == CLOSER) {
      builder.advanceLexer();
      return true;
    }
    builder.error(CfmlBundle.message("cfml.parsing.tag.is.not.done"));
    return false;
  }

  private static boolean parseCloseTag(PsiBuilder builder, Stack<Tag> tagNamesStack) {
    builder.advanceLexer();
    if (builder.getTokenType() == CF_TAG_NAME) {
      @SuppressWarnings({"ConstantConditions"}) String closeTagName = StringUtil.toLowerCase(builder.getTokenText());
      // eating tag name
      builder.advanceLexer();
      // canParse = if in the stack somewhere (not necessary on the top) there is the same tag name
      boolean canParse = false;
      for (Tag t : tagNamesStack) {
        if (t.myTagName.equals(closeTagName)) {
          canParse = true;
          break;
        }
      }
      // drop all markers with unclosed tags
      if (canParse) {
        Tag tag = null;
        while (!tagNamesStack.empty() && !((tag = tagNamesStack.pop()).myTagName.equals(closeTagName))) {
          if (CfmlUtil.isUserDefined(tag.myTagName) || !CfmlUtil.isEndTagRequired(tag.myTagName, builder.getProject())) {
            tag.myMarkerOfBegin.doneBefore(getElementTypeForTag(tag.myTagName), tag.myMarkerOfContent);
          }
          else {
            tag.myMarkerOfBegin.doneBefore(getElementTypeForTag(tag.myTagName), tag.myMarkerOfContent,
                                           CfmlBundle.message("cfml.parsing.element.is.not.closed", tag.myTagName));
          }
          tag.myMarkerOfContent.drop();
        }
        parseCloser(builder);
        if (tag != null) {
          tag.myMarkerOfContent.drop();
          tag.myMarkerOfBegin.done(getElementTypeForTag(tag.myTagName));
        }
        return true;
      }
      else {
        builder.error(CfmlBundle.message("cfml.parsing.closing.tag.matches.nothing"));
        parseCloser(builder);
        return false;
      }
    }
    return false;
  }

  private static void parseOpenTag(PsiBuilder builder, Stack<Tag> tagNamesStack) {
    PsiBuilder.Marker marker;
    String currentTagName;

    while (!builder.eof()) {
      marker = builder.mark();
      builder.advanceLexer();

      // parsing tag name
      if (builder.getTokenType() == CF_TAG_NAME) {
        currentTagName = StringUtil.toLowerCase(builder.getTokenText());
        builder.advanceLexer();
      }
      else {
        builder.error(CfmlBundle.message("cfml.parsing.unexpected.token"));
        marker.drop();
        continue;
      }

      parseAttributes(builder, currentTagName, ATTRIBUTE, false);

      // if CLOSER found than the check, if tag can be single performed,
      // if closing tag found, check if it matches the opening one, otherwise continue cicle
      if (builder.eof()) {
        builder.error(CfmlBundle.message("cfml.parsing.tag.is.not.done"));
        marker.done(getElementTypeForTag(currentTagName));
        return;
      }
      if (builder.getTokenType() == CLOSER) {
        builder.advanceLexer();
        marker.done(getElementTypeForTag(currentTagName));
      }
      else if (builder.getTokenType() == R_ANGLEBRACKET) {
        builder.advanceLexer();
        // ignore cfscript tag for psi tree depth decreasing
        //if (!"cfscript".equals(currentTagName.toLowerCase())) {
        tagNamesStack.push(new Tag(currentTagName, marker, builder.mark()));
        //}
      }
      else {
        /*
        PsiBuilder.Marker contentMarker = myBuilder.mark();
        marker.doneBefore(CF_TAG_NAME, contentMarker,
                CfmlBundle.message("cfml.parsing.element.is.not.closed", currentTagName));
        contentMarker.drop();
        */
        builder.error(CfmlBundle.message("cfml.parsing.tag.is.not.done"));
        tagNamesStack.push(new Tag(currentTagName, marker, builder.mark()));
        // marker.error(CfmlBundle.message("cfml.parsing.tag.is.not.done"));
      }
      if (StringUtil.toLowerCase(currentTagName).equals("cfscript")) {
        (new CfscriptParser()).parseScript(builder, true);
        /*
        final String closing = swallowClosing(builder);
        if (closing == null || !"cfscript".equals(closing.toLowerCase())) {
          builder.error(CfmlBundle.message("cfml.parsing.element.is.not.closed", "cfscript"));
        }
        marker.drop();
        */
      }
      while (!builder.eof() && builder.getTokenType() != OPENER) {
        if (builder.getTokenType() == LSLASH_ANGLEBRACKET) {
          parseCloseTag(builder, tagNamesStack);
        }
        else if (builder.getTokenType() == START_EXPRESSION) {
          parseExpression(builder);
        }
        else {
          builder.advanceLexer();
        }
      }
    }
  }

  @Nullable
  private static String swallowClosing(PsiBuilder builder) {
    if (compareAndEat(builder, LSLASH_ANGLEBRACKET)) {
      String tagName = builder.getTokenText();
      if (compareAndEat(builder, CF_TAG_NAME) && compareAndEat(builder, CLOSER)) {
        return tagName;
      }
    }
    return null;
  }

  private static boolean compareAndEat(PsiBuilder builder, IElementType type) {
    if (builder.getTokenType() != type) {
      return false;
    }
    builder.advanceLexer();
    return true;
  }

  /*
  protected final void advance() {
    builder.advanceLexer();
  }

  private void error(final String message) {
    builder.error(message);
  }

  private PsiBuilder.Marker mark() {
    return builder.mark();
  }

  private IElementType token() {
    return builder.getTokenType();
  }
  */

  public String toString() {
    return "CfmlParser";
  }
}
