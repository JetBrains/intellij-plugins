/*
 * Copyright 2012 The authors
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

package com.intellij.lang.ognl.parsing;

import com.intellij.lang.ognl.psi.OgnlTokenTypes;
import com.intellij.lang.pratt.*;
import com.intellij.patterns.IElementTypePattern;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.lang.ognl.psi.OgnlTokenTypes.*;
import static com.intellij.lang.pratt.PathPattern.path;
import static com.intellij.lang.pratt.PrattRegistry.registerParser;
import static com.intellij.patterns.PlatformPatterns.elementType;

/**
 * @author Yann C&eacute;bron
 */
public class OgnlParser extends PrattParser {

  private static final int INITIAL_LEVEL = 0;
  private static final int EXPR_LEVEL = INITIAL_LEVEL + 10;
  private static final int EXPR_BIT_LEVEL = EXPR_LEVEL + 10;
  private static final int COMP_LEVEL = EXPR_BIT_LEVEL + 10;
  private static final int EQ_LEVEL = COMP_LEVEL + 10;
  private static final int EQ_COMP_LEVEL = EQ_LEVEL + 10;
  private static final int NUMBERS_LEVEL = EQ_COMP_LEVEL + 10;
  private static final int UNARY_LEVEL = NUMBERS_LEVEL + 20;
  private static final int ATOM_LEVEL = UNARY_LEVEL + 20;

  private static final IElementTypePattern IDENTIFIER_PATTERN = elementType().or(OgnlElementTypes.REFERENCE_EXPRESSION,
                                                                                 OgnlElementTypes.VARIABLE_EXPRESSION);

  static {

    // "%{" + matching "}"
    registerParser(EXPRESSION_START, INITIAL_LEVEL, new ReducingParser() {
      @Override
      public IElementType parseFurther(final PrattBuilder builder) {
        parseExpression(builder);
        builder.assertToken(EXPRESSION_END, "'}' expected");
        return OgnlElementTypes.EXPRESSION_HOLDER;
      }
    });

    // boolean ops
    registerParser(OR_KEYWORD,
                   EXPR_LEVEL + 2,
                   path().left(),
                   expression(EXPR_LEVEL + 2, OgnlElementTypes.BINARY_EXPRESSION));
    registerParser(OR_OR,
                   EXPR_LEVEL + 2,
                   path().left(),
                   expression(EXPR_LEVEL + 2, OgnlElementTypes.BINARY_EXPRESSION));
    registerParser(AND_KEYWORD,
                   EXPR_LEVEL + 3,
                   path().left(),
                   expression(EXPR_LEVEL + 3, OgnlElementTypes.BINARY_EXPRESSION));
    registerParser(AND_AND,
                   EXPR_LEVEL + 3,
                   path().left(),
                   expression(EXPR_LEVEL + 3, OgnlElementTypes.BINARY_EXPRESSION));

    // bitwise boolean ops
    registerParser(OR,
                   EXPR_BIT_LEVEL + 2,
                   path().left(),
                   expression(EXPR_BIT_LEVEL + 2, OgnlElementTypes.BINARY_EXPRESSION));
    registerParser(BOR_KEYWORD,
                   EXPR_BIT_LEVEL + 2,
                   path().left(),
                   expression(EXPR_BIT_LEVEL + 2, OgnlElementTypes.BINARY_EXPRESSION));
    registerParser(AND,
                   EXPR_BIT_LEVEL + 3,
                   path().left(),
                   expression(EXPR_BIT_LEVEL + 3, OgnlElementTypes.BINARY_EXPRESSION));
    registerParser(BAND_KEYWORD,
                   EXPR_BIT_LEVEL + 3,
                   path().left(),
                   expression(EXPR_BIT_LEVEL + 3, OgnlElementTypes.BINARY_EXPRESSION));
    registerParser(XOR, EXPR_BIT_LEVEL, path().left(), expression(EXPR_BIT_LEVEL, OgnlElementTypes.BINARY_EXPRESSION));
    registerParser(XOR_KEYWORD,
                   EXPR_BIT_LEVEL,
                   path().left(),
                   expression(EXPR_BIT_LEVEL, OgnlElementTypes.BINARY_EXPRESSION));

    // bitwise shift ops
    registerParser(SHIFT_LEFT,
                   EXPR_BIT_LEVEL,
                   path().left(),
                   expression(EXPR_BIT_LEVEL, OgnlElementTypes.BINARY_EXPRESSION));
    registerParser(SHIFT_LEFT_KEYWORD,
                   EXPR_BIT_LEVEL,
                   path().left(),
                   expression(EXPR_BIT_LEVEL, OgnlElementTypes.BINARY_EXPRESSION));
    registerParser(SHIFT_RIGHT,
                   EXPR_BIT_LEVEL,
                   path().left(),
                   expression(EXPR_BIT_LEVEL, OgnlElementTypes.BINARY_EXPRESSION));
    registerParser(SHIFT_RIGHT_KEYWORD,
                   EXPR_BIT_LEVEL,
                   path().left(),
                   expression(EXPR_BIT_LEVEL, OgnlElementTypes.BINARY_EXPRESSION));
    registerParser(SHIFT_RIGHT_LOGICAL,
                   EXPR_BIT_LEVEL,
                   path().left(),
                   expression(EXPR_BIT_LEVEL, OgnlElementTypes.BINARY_EXPRESSION));
    registerParser(SHIFT_RIGHT_LOGICAL_KEYWORD,
                   EXPR_BIT_LEVEL,
                   path().left(),
                   expression(EXPR_BIT_LEVEL, OgnlElementTypes.BINARY_EXPRESSION));


    // equals/comparison
    registerParser(EQUAL, EQ_LEVEL, path().left(), expression(EQ_LEVEL, OgnlElementTypes.BINARY_EXPRESSION));
    registerParser(EQ_KEYWORD, EQ_LEVEL, path().left(), expression(EQ_LEVEL, OgnlElementTypes.BINARY_EXPRESSION));
    registerParser(NOT_EQUAL, EQ_LEVEL, path().left(), expression(EQ_LEVEL, OgnlElementTypes.BINARY_EXPRESSION));
    registerParser(NEQ_KEYWORD, EQ_LEVEL, path().left(), expression(EQ_LEVEL, OgnlElementTypes.BINARY_EXPRESSION));

    registerParser(LESS, EQ_COMP_LEVEL, path().left(), expression(EQ_COMP_LEVEL, OgnlElementTypes.BINARY_EXPRESSION));
    registerParser(LT_KEYWORD,
                   EQ_COMP_LEVEL,
                   path().left(),
                   expression(EQ_COMP_LEVEL, OgnlElementTypes.BINARY_EXPRESSION));

    registerParser(LESS_EQUAL,
                   EQ_COMP_LEVEL,
                   path().left(),
                   expression(EQ_COMP_LEVEL, OgnlElementTypes.BINARY_EXPRESSION));
    registerParser(LT_EQ_KEYWORD,
                   EQ_COMP_LEVEL,
                   path().left(),
                   expression(EQ_COMP_LEVEL, OgnlElementTypes.BINARY_EXPRESSION));

    registerParser(GREATER,
                   EQ_COMP_LEVEL,
                   path().left(),
                   expression(EQ_COMP_LEVEL, OgnlElementTypes.BINARY_EXPRESSION));
    registerParser(GT_KEYWORD,
                   EQ_COMP_LEVEL,
                   path().left(),
                   expression(EQ_COMP_LEVEL, OgnlElementTypes.BINARY_EXPRESSION));

    registerParser(GREATER_EQUAL,
                   EQ_COMP_LEVEL,
                   path().left(),
                   expression(EQ_COMP_LEVEL, OgnlElementTypes.BINARY_EXPRESSION));
    registerParser(GT_EQ_KEYWORD,
                   EQ_COMP_LEVEL,
                   path().left(),
                   expression(EQ_COMP_LEVEL, OgnlElementTypes.BINARY_EXPRESSION));


    // operations
    registerParser(PLUS,
                   NUMBERS_LEVEL + 7,
                   path().left(),
                   expression(NUMBERS_LEVEL + 7, OgnlElementTypes.BINARY_EXPRESSION));
    registerParser(PLUS,
                   UNARY_LEVEL + 1,
                   path().up(),
                   expression(UNARY_LEVEL, OgnlElementTypes.UNARY_EXPRESSION)); // TODO needed?

    registerParser(MINUS,
                   NUMBERS_LEVEL + 7,
                   path().left(),
                   expression(NUMBERS_LEVEL + 7, OgnlElementTypes.BINARY_EXPRESSION));
    registerParser(MINUS, UNARY_LEVEL + 1, path().up(), expression(UNARY_LEVEL, OgnlElementTypes.UNARY_EXPRESSION));

    registerParser(MULTIPLY,
                   NUMBERS_LEVEL + 8,
                   path().left(),
                   expression(NUMBERS_LEVEL + 8, OgnlElementTypes.BINARY_EXPRESSION));
    registerParser(DIVISION,
                   NUMBERS_LEVEL + 8,
                   path().left(),
                   expression(NUMBERS_LEVEL + 8, OgnlElementTypes.BINARY_EXPRESSION));

    registerParser(MODULO,
                   NUMBERS_LEVEL + 9,
                   path().left(),
                   expression(NUMBERS_LEVEL + 9, OgnlElementTypes.BINARY_EXPRESSION));

    registerParser(NEGATE,
                   UNARY_LEVEL + 1,
                   path().up(),
                   expression(UNARY_LEVEL + 1, OgnlElementTypes.UNARY_EXPRESSION));
    registerParser(NOT, UNARY_LEVEL + 1, path().up(), expression(UNARY_LEVEL + 1, OgnlElementTypes.UNARY_EXPRESSION));
    registerParser(NOT_KEYWORD,
                   UNARY_LEVEL + 1,
                   path().up(),
                   expression(UNARY_LEVEL + 1, OgnlElementTypes.UNARY_EXPRESSION));


    // literals TODO detect missing closing quote/tick
    registerParser(OgnlTokenTypes.CHARACTER_LITERAL,
                   ATOM_LEVEL + 1,
                   path().up(),
                   TokenParser.postfix(OgnlElementTypes.STRING_LITERAL));
    registerParser(STRING_LITERAL,
                   ATOM_LEVEL + 1,
                   path().up(),
                   TokenParser.postfix(OgnlElementTypes.STRING_LITERAL));

    registerParser(INTEGER_LITERAL,
                   ATOM_LEVEL + 1,
                   path().up(),
                   TokenParser.postfix(OgnlElementTypes.INTEGER_LITERAL));
    registerParser(BIG_INTEGER_LITERAL,
                   ATOM_LEVEL + 1,
                   path().up(),
                   TokenParser.postfix(OgnlElementTypes.BIG_INTEGER_LITERAL));
    registerParser(DOUBLE_LITERAL,
                   ATOM_LEVEL + 1,
                   path().up(),
                   TokenParser.postfix(OgnlElementTypes.DOUBLE_LITERAL));
    registerParser(BIG_DECIMAL_LITERAL,
                   ATOM_LEVEL + 1,
                   path().up(),
                   TokenParser.postfix(OgnlElementTypes.BIG_DECIMAL_LITERAL));

    registerParser(FALSE_KEYWORD, ATOM_LEVEL + 1, path().up(), TokenParser.postfix(OgnlElementTypes.BOOLEAN_LITERAL));
    registerParser(TRUE_KEYWORD, ATOM_LEVEL + 1, path().up(), TokenParser.postfix(OgnlElementTypes.BOOLEAN_LITERAL));
    registerParser(NULL_KEYWORD, ATOM_LEVEL + 1, path().up(), TokenParser.postfix(OgnlElementTypes.NULL_LITERAL));

    // (...): parenthesized expression
    registerParser(LPARENTH, ATOM_LEVEL + 1, path().up(), new ReducingParser() {
      @NotNull
      public IElementType parseFurther(@NotNull final PrattBuilder builder) {
        parseExpression(builder);
        assertClosingParenthesis(builder);
        return OgnlElementTypes.PARENTHESIZED_EXPRESSION;
      }
    });

    // [...]: indexed expression, only after identifier/var
    registerParser(LBRACKET, EXPR_LEVEL + 1, path().left(IDENTIFIER_PATTERN).up(),
                   new ReducingParser() {
                     @Override
                     public IElementType parseFurther(final PrattBuilder builder) {
                       parseExpression(builder);
                       assertClosingBracket(builder);
                       return OgnlElementTypes.INDEXED_EXPRESSION;
                     }
                   });

    // { a,b,c } list expression
    registerParser(LBRACE, EXPR_LEVEL + 1, path().up(), new ReducingParser() {
      @Override
      public IElementType parseFurther(final PrattBuilder builder) {
        parseExpression(builder);

        if (builder.assertToken(COMMA, "Sequence expected")) {

          do {
            parseExpression(builder);
          } while (builder.checkToken(COMMA));

        }
        assertClosingCurlyBracket(builder);
        return OgnlElementTypes.SEQUENCE_EXPRESSION;
      }
    });

    // special stuff ============================

    // condition ? then : else
    registerParser(QUESTION, EXPR_LEVEL + 1, new ReducingParser() {
      @Override
      public IElementType parseFurther(final PrattBuilder builder) {
        builder.createChildBuilder(EXPR_LEVEL, "'then' expression expected").parse();
        builder.assertToken(COLON, "':' expected");
        builder.createChildBuilder(EXPR_LEVEL, "'else' expression expected").parse();
        return OgnlElementTypes.CONDITIONAL_EXPRESSION;
      }
    });

    // "X in ...", "X not in ..." TODO require sequence expr
    registerParser(IN_KEYWORD,
                   EXPR_LEVEL + 1,
                   path().left(),
                   expression(EXPR_LEVEL, OgnlElementTypes.BINARY_EXPRESSION));
    registerParser(NOT_IN_KEYWORD,
                   EXPR_LEVEL + 1,
                   path().left(),
                   expression(EXPR_LEVEL, OgnlElementTypes.BINARY_EXPRESSION));

    // "new TypeName(p1,p2)", "new TypeName[]" + opt. "{el1,el2}"
    registerParser(NEW_KEYWORD,
                   EXPR_LEVEL + 1,
                   path().up(),
                   new ReducingParser() {
                     @Override
                     public IElementType parseFurther(final PrattBuilder builder) {
                       final MutableMarker mark = builder.mark();
                       builder.assertToken(IDENTIFIER, "Type expected"); // TODO support ClassReference "a.b.Class"
                       mark.finish(OgnlElementTypes.REFERENCE_EXPRESSION);

                       if (builder.checkToken(LPARENTH)) {
                         if (builder.checkToken(RPARENTH)) {
                           return OgnlElementTypes.NEW_EXPRESSION;
                         }

                         parseExpression(builder);
                         while (builder.checkToken(COMMA)) {
                           parseExpression(builder);
                         }

                         assertClosingParenthesis(builder);
                         return OgnlElementTypes.NEW_EXPRESSION;
                       }

                       if (builder.checkToken(LBRACKET)) {
                         if (builder.checkToken(RBRACKET)) {

                           // Optional: sequence
                           if (builder.getTokenType() == LBRACE) {
                             final MutableMarker sequenceMarker = builder.mark();
                             parseExpression(builder);
                             sequenceMarker.finish(OgnlElementTypes.SEQUENCE_EXPRESSION);
                           }

                           return OgnlElementTypes.NEW_EXPRESSION;
                         }

                         parseExpression(builder);   // [<X>]
                         assertClosingBracket(builder);
                         return OgnlElementTypes.NEW_EXPRESSION;
                       }

                       builder.error("Constructor or array expected");
                       return null;
                     }
                   });

    // instanceof
    registerParser(INSTANCEOF_KEYWORD,
                   EXPR_LEVEL + 1,
                   path().left(),
                   expression(EXPR_LEVEL, OgnlElementTypes.BINARY_EXPRESSION));

    // method calls: reference([paramA, paramB, ..])
    registerParser(LPARENTH,
                   EXPR_LEVEL + 1,
                   path().left(OgnlElementTypes.REFERENCE_EXPRESSION).up(),
                   new ReducingParser() {
                     @Override
                     public IElementType parseFurther(final PrattBuilder builder) {
                       if (!builder.checkToken(RPARENTH)) {
                         parseExpression(builder);

                         while (builder.checkToken(COMMA)) {
                           parseExpression(builder);
                         }

                         assertClosingParenthesis(builder);
                       }
                       return OgnlElementTypes.METHOD_CALL_EXPRESSION;
                     }
                   });

    // TODO static method calls @class@method

    // TODO static field ref @class@field
    registerParser(AT, EXPR_LEVEL + 1, path().up(), new ReducingParser() {
      @Override
      public IElementType parseFurther(PrattBuilder builder) {
        builder.assertToken(IDENTIFIER, "Class identifier expected");

        while (builder.checkToken(DOT)) {
          parseExpression(builder);
        }

        builder.assertToken(AT, "@ expected");
        builder.assertToken(IDENTIFIER, "Field or method identifier expected");
        return OgnlElementTypes.REFERENCE_EXPRESSION;
      }
    });

    // TODO projection/selection: e1.{e2} / e1.{?e2}

    // TODO sub-expression: e1.(e2)
    // TODO chained sub-expression: headline.parent.(ensureLoaded(), name)

    // TODO list creation: { e, ... } --> already via sequence?
    // TODO array creation: new array-component-class[] { e, ... }
    // TODO map creation: #{ e1 : e2, ... }
    // TODO map creation w/ class: #@classname@{ e1 : e2, ... }

    // TODO lambda: :[ e ]

    // #var
    registerParser(HASH, ATOM_LEVEL + 1, path().up(), new ReducingParser() {
      @Override
      public IElementType parseFurther(final PrattBuilder builder) {
        builder.assertToken(IDENTIFIER, "Variable identifier expected");
        return OgnlElementTypes.VARIABLE_EXPRESSION;
      }
    });

    // TODO #var = value

    // reference ("plain")
    registerParser(IDENTIFIER, ATOM_LEVEL + 1, path().up(), TokenParser.postfix(OgnlElementTypes.REFERENCE_EXPRESSION));

    // nested references (a.b.c) TODO check PSI nesting
    registerParser(DOT, EXPR_LEVEL + 1, path().left(), new ReducingParser() {
      @NotNull
      public IElementType parseFurther(@NotNull final PrattBuilder builder) {
        builder.assertToken(IDENTIFIER, "Nested expression expected");
        return OgnlElementTypes.REFERENCE_EXPRESSION;
      }
    });

  }

  @Nullable
  private static IElementType parseExpression(final PrattBuilder builder) {
    return builder.createChildBuilder(EXPR_LEVEL, "Expression expected").parse();
  }

  private static void assertClosingParenthesis(final PrattBuilder builder) {
    builder.assertToken(RPARENTH, "')' expected");
  }

  private static void assertClosingBracket(final PrattBuilder builder) {
    builder.assertToken(RBRACKET, "']' expected");
  }

  private static boolean assertClosingCurlyBracket(final PrattBuilder builder) {
    return builder.assertToken(RBRACE, "'}' expected");
  }

  private static TokenParser expression(final int priority, final OgnlElementType type) {
    return TokenParser.infix(priority, type, "Expression expected");
  }

}