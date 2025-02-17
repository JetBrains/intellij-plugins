// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.javascript.JavaScriptParserBundle;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.xml.parsing.XmlParserBundle;
import com.jetbrains.plugins.jade.JadeBundle;
import com.jetbrains.plugins.jade.psi.JadeElementTypes;
import com.jetbrains.plugins.jade.psi.JadeTokenTypes;

public final class TagParsing {
  static void parseAttributeList(final PsiBuilder builder) {
    builder.advanceLexer();

    boolean seenEq = false;
    boolean reportedAttributeValueExpectedError = false;
    boolean hadValue = false;
    boolean first = true;
    PsiBuilder.Marker attribute = null;
    while (true) {
      IElementType tokenType = builder.getTokenType();
      if (tokenType == null || builder.getTokenType() == JadeTokenTypes.RPAREN) {
        if (attribute != null) {
          attribute.done(JadeElementTypes.ATTRIBUTE);
        }
        if (attribute != null && seenEq && !reportedAttributeValueExpectedError) {
          builder.error(XmlParserBundle.message("xml.parsing.attribute.value.expected"));
        }
        else if (tokenType == null) {
          builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.rparen"));
        }
        if (tokenType == JadeTokenTypes.RPAREN) {
          builder.advanceLexer();
        }
        break;
      }

      if (tokenType == JadeTokenTypes.COMMA) {
        if (attribute != null) {
          attribute.done(JadeElementTypes.ATTRIBUTE);
          attribute = null;
        }
        seenEq = false;
        hadValue = false;
        reportedAttributeValueExpectedError = true;

        builder.advanceLexer();
      }
      else if (tokenType == JadeTokenTypes.ATTRIBUTE_NAME) {
        if (first) {
          builder.mark().done(JadeElementTypes.JADE_PSEUDO_WHITESPACE);
          first = false;
        }
        if (attribute == null) {
          attribute = builder.mark();
        }

        builder.advanceLexer();
        hadValue = false;
      }
      // NEQ stands for unescaped string
      else if (isAttributeValueStart(tokenType)) {
        if (seenEq) {
          if (attribute != null && !reportedAttributeValueExpectedError) {
            builder.error(XmlParserBundle.message("xml.parsing.attribute.value.expected"));
            reportedAttributeValueExpectedError = true;
          }
        }
        else {
          seenEq = true;
        }
        builder.advanceLexer();
      }
      else if (tokenType == XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER ||
               tokenType == JadeTokenTypes.JS_CODE_BLOCK ||
               tokenType == JadeTokenTypes.JS_EXPR) {
        if (hadValue) {
          builder.error(JadeBundle.message("pug.parser.error.expected-whitespace-attribute-name-or-rparen"));
          while (tokenType != JadeTokenTypes.ATTRIBUTE_NAME && tokenType != JadeTokenTypes.RPAREN && !builder.eof()) {
            builder.advanceLexer();
            tokenType = builder.lookAhead(1);
          }
          continue;
        }
        seenEq = false;
        hadValue = true;
        reportedAttributeValueExpectedError = true;

        if (attribute == null) {
          attribute = builder.mark();
          builder.mark().done(JadeElementTypes.FAKE_ATTR_NAME);
        }
        PsiBuilder.Marker attributeValue = builder.mark();
        if (builder.getTokenType() == XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER) {
          builder.advanceLexer();
        }

        builder.advanceLexer();

        if (builder.getTokenType() == XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER) {
          builder.advanceLexer();
        }
        attributeValue.done(JadeElementTypes.ATTRIBUTE_VALUE);
        int i = 0;
        while (builder.lookAhead(i) == JadeTokenTypes.EOL) {
          i++;
        }
        if (builder.lookAhead(i) == JadeTokenTypes.ATTRIBUTE_NAME) {
          attribute.done(JadeElementTypes.ATTRIBUTE);
          attribute = null;
        }
      }
      else {
        builder.advanceLexer();
      }
    }
  }

  private static boolean isAttributeValueStart(IElementType tokenType) {
    return tokenType == JadeTokenTypes.EQ || tokenType == JadeTokenTypes.NEQ;
  }
}
