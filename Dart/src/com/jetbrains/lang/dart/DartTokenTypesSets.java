// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart;

import com.intellij.lang.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.text.BlockSupport;
import com.intellij.psi.tree.*;
import com.intellij.util.diff.FlyweightCapableTreeStructure;
import com.jetbrains.lang.dart.lexer.DartDocLexer;
import com.jetbrains.lang.dart.lexer.DartLexer;
import com.jetbrains.lang.dart.psi.impl.DartLazyParseableBlockImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import static com.jetbrains.lang.dart.DartTokenTypes.*;

public interface DartTokenTypesSets {
  IElementType WHITE_SPACE = TokenType.WHITE_SPACE;
  IElementType BAD_CHARACTER = TokenType.BAD_CHARACTER;

  // DartLexer returns multiline comments as a single MULTI_LINE_COMMENT or MULTI_LINE_DOC_COMMENT
  // DartDocLexer splits MULTI_LINE_DOC_COMMENT in tokens

  // can't appear in PSI because merged into MULTI_LINE_COMMENT
  IElementType MULTI_LINE_COMMENT_START = new DartElementType("MULTI_LINE_COMMENT_START");

  IElementType MULTI_LINE_DOC_COMMENT_START = new DartElementType("MULTI_LINE_DOC_COMMENT_START");
  IElementType MULTI_LINE_COMMENT_BODY = new DartElementType("MULTI_LINE_COMMENT_BODY");
  IElementType DOC_COMMENT_LEADING_ASTERISK = new DartElementType("DOC_COMMENT_LEADING_ASTERISK");
  IElementType MULTI_LINE_COMMENT_END = new DartElementType("MULTI_LINE_COMMENT_END");

  IElementType SINGLE_LINE_COMMENT = new DartElementType("SINGLE_LINE_COMMENT");
  IElementType SINGLE_LINE_DOC_COMMENT = new DartElementType("SINGLE_LINE_DOC_COMMENT");
  IElementType MULTI_LINE_COMMENT = new DartElementType("MULTI_LINE_COMMENT");
  IElementType MULTI_LINE_DOC_COMMENT = new DartDocCommentElementType();

  IElementType LAZY_PARSEABLE_BLOCK = new DartLazyParseableBlockElementType();

  TokenSet STRINGS = TokenSet.create(RAW_SINGLE_QUOTED_STRING, RAW_TRIPLE_QUOTED_STRING, OPEN_QUOTE, CLOSING_QUOTE, REGULAR_STRING_PART);

  TokenSet RESERVED_WORDS = TokenSet.create(ASSERT,
                                            BREAK,
                                            CASE,
                                            CATCH,
                                            CLASS,
                                            CONST,
                                            CONTINUE,
                                            DEFAULT,
                                            DO,
                                            ELSE,
                                            ENUM,
                                            EXTENDS,
                                            FALSE,
                                            FINAL,
                                            FINALLY,
                                            FOR,
                                            IF,
                                            IN,
                                            IS,
                                            NEW,
                                            NULL,
                                            RETHROW,
                                            RETURN,
                                            SUPER,
                                            SWITCH,
                                            THIS,
                                            THROW,
                                            TRUE,
                                            TRY,
                                            VAR,
                                            WHILE,
                                            WITH,
                                            // 'void' is not listed as reserved word in spec but it may only be used as the return type of a function, so may be treated as reserved word
                                            VOID);

  TokenSet BUILT_IN_IDENTIFIERS = TokenSet.create(ABSTRACT,
                                                  AS,
                                                  BASE,
                                                  COVARIANT,
                                                  DEFERRED,
                                                  EXPORT,
                                                  EXTENSION,
                                                  EXTERNAL,
                                                  FACTORY,
                                                  GET,
                                                  IMPLEMENTS,
                                                  IMPORT,
                                                  INTERFACE,
                                                  LIBRARY,
                                                  MIXIN,
                                                  OPERATOR,
                                                  PART,
                                                  SEALED,
                                                  SET,
                                                  STATIC,
                                                  TYPEDEF,
                                                  WHEN,
                                                  // next are not listed in spec, but they seem to have the same sense as BUILT_IN_IDENTIFIERS: somewhere treated as keywords, but can be used as normal identifiers
                                                  ON,
                                                  OF,
                                                  NATIVE,
                                                  SHOW,
                                                  HIDE,
                                                  SYNC,
                                                  ASYNC,
                                                  AWAIT,
                                                  YIELD,
                                                  LATE,
                                                  REQUIRED);

  TokenSet OPERATORS = TokenSet.create(
    MINUS, MINUS_EQ, MINUS_MINUS, PLUS, PLUS_PLUS, PLUS_EQ, DIV, DIV_EQ, MUL, MUL_EQ, INT_DIV, INT_DIV_EQ, REM_EQ, REM, BIN_NOT, NOT,
    EQ, EQ_EQ, NEQ, GT, GT_EQ, GT_GT_EQ, GT_GT, GT_GT_GT_EQ, GT_GT_GT, LT, LT_EQ, LT_LT, LT_LT_EQ, OR, OR_EQ, OR_OR, OR_OR_EQ, XOR, XOR_EQ,
    AND, AND_EQ, AND_AND, AND_AND_EQ, LBRACKET, RBRACKET, AS, QUEST_QUEST, QUEST_QUEST_EQ
  );

  TokenSet ASSIGNMENT_OPERATORS = TokenSet.create(
    // '=' | '*=' | '/=' | '~/=' | '%=' | '+=' | '-=' | '<<=' | '>>=' | '>>>=' | '&=' | '&&=' | '^=' | '|=' | '||=' | '??='
    EQ, MUL_EQ, DIV_EQ, INT_DIV_EQ, REM_EQ, PLUS_EQ, MINUS_EQ, LT_LT_EQ, GT_GT_EQ, GT_GT_GT_EQ, AND_EQ, AND_AND_EQ, XOR_EQ, OR_EQ, OR_OR_EQ,
    QUEST_QUEST_EQ
  );

  TokenSet BINARY_EXPRESSIONS = TokenSet.create(
    IF_NULL_EXPRESSION,
    LOGIC_OR_EXPRESSION,
    LOGIC_AND_EXPRESSION,
    COMPARE_EXPRESSION,
    SHIFT_EXPRESSION,
    ADDITIVE_EXPRESSION,
    MULTIPLICATIVE_EXPRESSION
  );

  TokenSet BINARY_OPERATORS = TokenSet.create(
    // '??
    QUEST_QUEST,
    // '&&' '||'
    AND_AND, OR_OR,
    // '==' '!='
    EQ_EQ, NEQ,
    // '<' '<=' '>' '>='
    LT, LT_EQ, GT, GT_EQ,
    // '&' '|' '^'
    AND, OR, XOR,
    // '<<' '>>' '>>>'
    LT_LT, GT_GT, GT_GT_GT,
    // '+' '-'
    PLUS, MINUS,
    // '*' '/' '%' '~/'
    MUL, DIV, REM, INT_DIV
  );

  TokenSet LOGIC_OPERATORS = TokenSet.create(
    OR_OR, AND_AND,
    // Strictly speaking, this isn't a logical operator, but should be formatted the same.
    QUEST_QUEST
  );

  TokenSet UNARY_OPERATORS = TokenSet.create(
    // '-' '!' '~' '++' '--'
    MINUS, NOT, BIN_NOT, PLUS_PLUS, MINUS_MINUS
  );

  TokenSet BITWISE_OPERATORS = TokenSet.create(BITWISE_OPERATOR);
  TokenSet FUNCTION_DEFINITION = TokenSet.create(
    FUNCTION_FORMAL_PARAMETER,
    FUNCTION_DECLARATION_WITH_BODY,
    FUNCTION_DECLARATION_WITH_BODY_OR_NATIVE,
    METHOD_DECLARATION,
    GETTER_DECLARATION,
    SETTER_DECLARATION
  );

  TokenSet COMMENTS = TokenSet.create(SINGLE_LINE_COMMENT, SINGLE_LINE_DOC_COMMENT, MULTI_LINE_COMMENT, MULTI_LINE_DOC_COMMENT);
  TokenSet DOC_COMMENT_CONTENTS =
    TokenSet.create(MULTI_LINE_DOC_COMMENT_START, MULTI_LINE_COMMENT_BODY, DOC_COMMENT_LEADING_ASTERISK, MULTI_LINE_COMMENT_END);

  IElementType EMBEDDED_CONTENT = new DartEmbeddedContentElementType();

  TokenSet BLOCKS = TokenSet.create(
    BLOCK,
    LAZY_PARSEABLE_BLOCK
  );

  TokenSet BLOCKS_EXT = TokenSet.create(
    BLOCK,
    LAZY_PARSEABLE_BLOCK,
    CLASS_MEMBERS,
    DartParserDefinition.DART_FILE,
    EMBEDDED_CONTENT
  );

  TokenSet DECLARATIONS = TokenSet.create(
    CLASS_DEFINITION,
    FUNCTION_DECLARATION_WITH_BODY,
    FUNCTION_DECLARATION_WITH_BODY_OR_NATIVE,
    GETTER_DECLARATION,
    SETTER_DECLARATION,
    VAR_DECLARATION_LIST,
    FUNCTION_TYPE_ALIAS
  );

  class DartDocCommentElementType extends ILazyParseableElementType {
    public DartDocCommentElementType() {
      super("MULTI_LINE_DOC_COMMENT", DartLanguage.INSTANCE);
    }

    @Override
    public ASTNode parseContents(@NotNull final ASTNode chameleon) {
      final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(chameleon.getTreeParent().getPsi().getProject(),
                                                                               chameleon,
                                                                               new DartDocLexer(),
                                                                               getLanguage(),
                                                                               chameleon.getChars());
      doParse(builder);
      return builder.getTreeBuilt().getFirstChildNode();
    }

    private void doParse(final PsiBuilder builder) {
      final PsiBuilder.Marker root = builder.mark();

      while (!builder.eof()) {
        builder.advanceLexer();
      }

      root.done(this);
    }
  }

  class DartLazyParseableBlockElementType extends IReparseableElementType {
    public DartLazyParseableBlockElementType() {
      super("LAZY_PARSEABLE_BLOCK", DartLanguage.INSTANCE);
    }

    @Override
    public boolean isParsable(@NotNull final CharSequence buffer, @NotNull final Language fileLanguage, @NotNull final Project project) {
      return PsiBuilderUtil.hasProperBraceBalance(buffer, new DartLexer(), LBRACE, RBRACE);
    }

    @Nullable
    @Override
    public ASTNode createNode(@NotNull final CharSequence text) {
      return new DartLazyParseableBlockImpl(this, text);
    }

    @Override
    protected ASTNode doParseContents(@NotNull ASTNode lazyParseableBlock, @NotNull PsiElement psi) {
      PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(psi.getProject(), lazyParseableBlock);

      if (isSyncOrAsync(lazyParseableBlock)) {
        builder.putUserData(DartGeneratedParserUtilBase.INSIDE_SYNC_OR_ASYNC_FUNCTION, true);
      }
      new DartParser().parseLight(BLOCK, builder);
      return builder.getTreeBuilt().getFirstChildNode();
    }

    private static boolean isSyncOrAsync(@NotNull final ASTNode newBlock) {
      final ASTNode oldBlock = Pair.getFirst(newBlock.getUserData(BlockSupport.TREE_TO_BE_REPARSED));
      final IElementType type = (oldBlock != null ? oldBlock : newBlock).getTreeParent().getFirstChildNode().getElementType();
      return type == SYNC || type == ASYNC;
    }
  }

  class DartEmbeddedContentElementType extends ILazyParseableElementType implements ILightLazyParseableElementType {
    public DartEmbeddedContentElementType() {
      super("DART_EMBEDDED_CONTENT", DartInHtmlLanguage.INSTANCE);
    }

    @Override
    protected ASTNode doParseContents(@NotNull ASTNode chameleon, @NotNull PsiElement psi) {
      PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(psi.getProject(), chameleon);
      new DartParser().parseLight(DartParserDefinition.DART_FILE, builder);
      return builder.getTreeBuilt().getFirstChildNode();
    }

    @Override
    public @NotNull FlyweightCapableTreeStructure<LighterASTNode> parseContents(@NotNull LighterLazyParseableNode chameleon) {
      PsiFile file = chameleon.getContainingFile();
      assert file != null : chameleon;

      final PsiBuilder psiBuilder = PsiBuilderFactory.getInstance().createBuilder(file.getProject(), chameleon);

      final PsiBuilder builder = adapt_builder_(EMBEDDED_CONTENT, psiBuilder, new DartParser(), DartParser.EXTENDS_SETS_);

      PsiBuilder.Marker marker = enter_section_(builder, 0, _COLLAPSE_, "<dart embedded content>");
      boolean result = DartParser.dartUnit(builder, 0);
      exit_section_(builder, 0, marker, EMBEDDED_CONTENT, result, true, TRUE_CONDITION);
      return builder.getLightTree();
    }
  }
}
