// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.psi;

import com.intellij.embedding.EmbeddedLazyParseableElementType;
import com.intellij.embedding.TemplateMasqueradingLexer;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.javascript.types.JSEmbeddedBlockElementType;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.TokenType;
import com.intellij.psi.css.impl.CssAdvancedElementTypes;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.xml.XmlElementType;
import com.intellij.psi.xml.XmlTokenType;
import com.jetbrains.plugins.jade.JadeLanguage;
import com.jetbrains.plugins.jade.js.JSEachStatementImpl;
import com.jetbrains.plugins.jade.js.JavaScriptInJadeLanguageDialect;
import com.jetbrains.plugins.jade.js.JavaScriptInJadeParser;
import com.jetbrains.plugins.jade.lexer.JSMetaCodeLexer;
import com.jetbrains.plugins.jade.lexer.JadeEmbeddedTokenTypesWrapper;
import org.jetbrains.annotations.NotNull;

public interface JadeTokenTypes {

  IElementType BAD_CHARACTER = TokenType.BAD_CHARACTER;

  IElementType INDENT = new IElementType("INDENT", JadeLanguage.INSTANCE);
  IElementType TAG_NAME = XmlTokenType.XML_TAG_NAME;
  IElementType ATTRIBUTE_NAME = XmlTokenType.XML_NAME;
  IElementType FILTER_NAME = new IElementType("FILTER_NAME", JadeLanguage.INSTANCE);
  IElementType NUMBER = new IElementType("NUMBER", JadeLanguage.INSTANCE);
  IElementType LPAREN = new IElementType("LPAREN", JadeLanguage.INSTANCE); // (
  IElementType RPAREN = new IElementType("RPAREN", JadeLanguage.INSTANCE); // )
  IElementType EQ = new IElementType("EQ", JadeLanguage.INSTANCE); // =
  IElementType NEQ = new IElementType("NEQ", JadeLanguage.INSTANCE); // !=
  IElementType MINUS = new IElementType("MINUS", JadeLanguage.INSTANCE); // -
  IElementType DOUBLE_QUOTE = new IElementType("DOUBLE_QUOTE", JadeLanguage.INSTANCE); // "
  IElementType STRING_LITERAL = new IElementType("STRING_LITERAL", JadeLanguage.INSTANCE); // "XXX"
  IElementType CHAR_LITERAL = new IElementType("CHAR_LITERAL", JadeLanguage.INSTANCE); // 'XXX'
  IElementType COMMENT = new IElementType("COMMENT", JadeLanguage.INSTANCE); // // ...
  IElementType UNBUF_COMMENT = new IElementType("UNBUFFERED_COMMENT", JadeLanguage.INSTANCE); // //- ...
  IElementType TEXT = new IElementType("TEXT", JadeLanguage.INSTANCE);
  IElementType DOCTYPE_KEYWORD = new IElementType("DOCTYPE_KEYWORD", JadeLanguage.INSTANCE);
  IElementType EOL = new IElementType("EOL", JadeLanguage.INSTANCE); // \n
  IElementType DIV = new IElementType("DIV", JadeLanguage.INSTANCE); // /
  IElementType DOT = new IElementType("DOT", JadeLanguage.INSTANCE); // .
  IElementType PLUS = new IElementType("PLUS", JadeLanguage.INSTANCE); // .
  IElementType HASH = new IElementType("HASH", JadeLanguage.INSTANCE); // #
  IElementType TAG_ID = new IElementType("TAG_ID", JadeLanguage.INSTANCE); // tag#tag_id
  IElementType TAG_CLASS = new IElementType("TAG_CLASS", JadeLanguage.INSTANCE); // tag.tag_class
  IElementType PIPE = new IElementType("PIPE", JadeLanguage.INSTANCE); // |
  IElementType COMMA = new IElementType("COMMA", JadeLanguage.INSTANCE); // ,
  IElementType ATTRIBUTE_VALUE = new IElementType("ATTRIBUTE_VALUE", JadeLanguage.INSTANCE);
  IElementType IDENTIFIER = new IElementType("IDENTIFIER", JadeLanguage.INSTANCE);
  IElementType COLON = new IElementType("COLON", JadeLanguage.INSTANCE); // :
  IElementType COND_KEYWORD = new IElementType("CONDITIONAL_KEYWORD", JadeLanguage.INSTANCE);
  IElementType ELSE_KEYWORD = new IElementType("ELSE_KEYWORD", JadeLanguage.INSTANCE);
  IElementType DEFAULT_KEYWORD = new IElementType("DEFAULT_KEYWORD", JadeLanguage.INSTANCE);
  IElementType ITERATION_KEYWORD = new IElementType("ITER_KEYWORD", JadeLanguage.INSTANCE);
  IElementType CASE = new IElementType("CASE", JadeLanguage.INSTANCE);
  IElementType WHEN = new IElementType("WHEN", JadeLanguage.INSTANCE);
  IElementType EXTENDS_KEYWORD = new IElementType("EXTENDS", JadeLanguage.INSTANCE);
  IElementType FILE_PATH = new IElementType("FILE_PATH", JadeLanguage.INSTANCE);
  IElementType INCLUDE_KEYWORD = new IElementType("INCLUDE", JadeLanguage.INSTANCE);
  IElementType MIXIN_KEYWORD = new IElementType("MIXIN_KEYWORD", JadeLanguage.INSTANCE);
  IElementType YIELD_KEYWORD = new IElementType("YIELD", JadeLanguage.INSTANCE);
  IElementType ATTRIBUTES_KEYWORD = new IElementType("ATTRIBUTES_KEYWORD", JadeLanguage.INSTANCE);

  IElementType JS_CODE_BLOCK = new JSInJadeEmbeddedElementType();//JSElementTypes.EMBEDDED_CONTENT;
  IElementType JS_EXPR = new JSInJadeExpressionElementType();
  IElementType JS_CODE_BLOCK_PATCHED = new JSInJadeEmbeddedElementType();
  IElementType JS_MIXIN_PARAMS = new JSInJadeMixinParams();
  IElementType JS_MIXIN_PARAMS_VALUES = new JSInJadeMixinParamsValues();
  IElementType JS_EACH_EXPR = new JSInJadeForeachLineType();
  IElementType JS_META_CODE = new JSInJadeMetaCode();

  IElementType STYLE_BLOCK = CssAdvancedElementTypes.CSS_LAZY_STYLESHEET;

  IElementType FILTER_CODE = new IElementType("FILTER_CODE", JadeLanguage.INSTANCE);

  TokenSet JS_TOKENS = TokenSet.create(JS_CODE_BLOCK, JS_EACH_EXPR, JS_EXPR, JS_CODE_BLOCK_PATCHED, JS_EACH_EXPR, JS_META_CODE);

  TokenSet TEXT_SET = TokenSet.create(XmlElementType.XML_TEXT, XmlElementType.HTML_RAW_TEXT, XmlTokenType.XML_DATA_CHARACTERS);
  TokenSet COMMENTS = TokenSet.create(COMMENT, UNBUF_COMMENT);
  TokenSet STRING_LITERALS = TokenSet.create(STRING_LITERAL, CHAR_LITERAL);

  // Only for internal use since it pre-lexes interpolations and cuts out indents
  IElementType JADE_EMBEDDED_CONTENT = new JadeEmbeddedTokenTypesWrapper(
    new EmbeddedLazyParseableElementType("JADE_EMBEDDED_CONTENT", JadeLanguage.INSTANCE));

  IElementType FULL_JADE_EMBEDDED_CONTENT = new JadeEmbeddedTokenTypesWrapper(
    new EmbeddedLazyParseableElementType("FULL_JADE_EMBEDDED_CONTENT", JadeLanguage.INSTANCE)) {
    @Override
    public Lexer createLexer(@NotNull ASTNode chameleon, @NotNull Project project) {
      return LanguageParserDefinitions.INSTANCE.forLanguage(JadeLanguage.INSTANCE).createLexer(project);
    }
  };

  // A little for JavaScript in Jade
  IElementType EACH_EXPR = new JadeElementTypes.JSInJadeElementType("EACH_EXPR") {
    @Override
    public @NotNull ASTNode createCompositeNode() {
      return new JSEachStatementImpl(this);
    }
  };
  IElementType INTERPOLATED_EXPRESSION = new IElementType("INTERPOLATED_EXPRESSION", JavaScriptInJadeLanguageDialect.INSTANCE);
  IElementType INTERPOLATED_STRING_START = new IElementType("INTERPOLATED_STRING_START", JavaScriptInJadeLanguageDialect.INSTANCE);
  IElementType INTERPOLATED_STRING_END = new IElementType("INTERPOLATED_STRING_END", JavaScriptInJadeLanguageDialect.INSTANCE);
  IElementType INTERPOLATED_STRING_PART = new IElementType("INTERPOLATED_STRING_PART", JavaScriptInJadeLanguageDialect.INSTANCE);

  class JSInJadeEmbeddedElementType extends JadeLazyParseableElementType {
    public JSInJadeEmbeddedElementType() {
      super("EMBEDDED_CONTENT", JavaScriptInJadeLanguageDialect.INSTANCE);
    }

    @Override
    protected void parseIntoBuilder(@NotNull PsiBuilder builder) {
      new JavaScriptInJadeParser(builder).parseJS(this);
    }

    @Override
    public ASTNode parseAndGetTree(@NotNull PsiBuilder builder) {
      parseIntoBuilder(builder);
      return builder.getTreeBuilt();
    }
  }

  class JSInJadeExpressionElementType extends JadeLazyParseableElementType {
    public JSInJadeExpressionElementType() {
      super("EMBEDDED_EXPRESSION", JavaScriptInJadeLanguageDialect.INSTANCE);
    }

    @Override
    protected void parseIntoBuilder(@NotNull PsiBuilder builder) {
      new JavaScriptInJadeParser(builder).parseExpression();
    }
  }

  class JSInJadeForeachLineType extends JadeLazyParseableElementType {
    public JSInJadeForeachLineType() {
      super("EMBEDDED_FOREACH", JavaScriptInJadeLanguageDialect.INSTANCE);
    }

    @Override
    public void parseIntoBuilder(@NotNull PsiBuilder builder) {
      new JavaScriptInJadeParser(builder).parseForeach();
    }
  }

  class JSInJadeMixinParams extends JadeLazyParseableElementType {
    public JSInJadeMixinParams() {
      super("EMBEDDED_MIXIN_PARAMS", JavaScriptInJadeLanguageDialect.INSTANCE);
    }

    @Override
    public void parseIntoBuilder(@NotNull PsiBuilder builder) {
      new JavaScriptInJadeParser(builder).parseMixinParams();
    }
  }

  class JSInJadeMixinParamsValues extends JadeLazyParseableElementType {
    public JSInJadeMixinParamsValues() {
      super("EMBEDDED_MIXIN_PARAMS_VALUES", JavaScriptInJadeLanguageDialect.INSTANCE);
    }

    @Override
    public void parseIntoBuilder(@NotNull PsiBuilder builder) {
      new JavaScriptInJadeParser(builder).parseMixinParamsValues();
    }
  }

  class JSInJadeMetaCode extends EmbeddedLazyParseableElementType implements JSEmbeddedBlockElementType {

    public JSInJadeMetaCode() {
      super("JS_META_CODE", JavaScriptInJadeLanguageDialect.INSTANCE);
    }

    @Override
    public Lexer createLexer(@NotNull ASTNode chameleon, @NotNull Project project) {
      ASTNode embeddedContentParent = chameleon;
      while (embeddedContentParent != null && !(embeddedContentParent.getElementType() instanceof EmbeddedLazyParseableElementType)) {
        embeddedContentParent = embeddedContentParent.getTreeParent();
      }

      if (embeddedContentParent != null && embeddedContentParent != chameleon) {
        ASTNode first = embeddedContentParent.getFirstChildNode();
        if (first != null && first.getElementType() == TemplateMasqueradingLexer.MINUS_TYPE) {
          ASTNode next = first.getTreeNext();
          if (next.getElementType() == INDENT && next.textContains('\n')) {
            return null;
          }
        }
      }

      return new JSMetaCodeLexer(chameleon);
    }
  }


}
