// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.lexer;

import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.IElementType;
import com.mscharhag.oleaster.runner.OleasterRunner;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static com.mscharhag.oleaster.matcher.Matchers.expect;
import static com.mscharhag.oleaster.runner.StaticRunnerSupport.describe;
import static com.mscharhag.oleaster.runner.StaticRunnerSupport.it;
import static org.angular2.lang.expr.lexer.Angular2TokenTypes.*;

@SuppressWarnings({"JUnitTestCaseWithNonTrivialConstructors", "CodeBlock2Expr", "SameParameterValue", "JUnitTestCaseWithNoTests",
  "ClassInitializerMayBeStatic"})
@RunWith(OleasterRunner.class)
public class Angular2LexerSpecTest {

  private static Token[] lex(String text) {
    return Token.create(new Angular2Lexer(), text);
  }

  private static void expectToken(Token token, int index) {
    expect(token.index).toEqual(index);
  }

  private static void expectCharacterToken(Token token, int index, IElementType character) {
    expectToken(token, index);
    expect(token.tokenType == character).toBeTrue();
  }

  private static void expectOperatorToken(Token token, int index, IElementType operator) {
    expectToken(token, index);
    expect(token.tokenType == operator).toBeTrue();
  }

  private static void expectNumberToken(Token token, int index, int n) {
    expectToken(token, index);
    expect(token.isNumber()).toBeTrue();
    expect(token.toNumber()).toEqual(n);
  }

  private static void expectNumberToken(Token token, int index, double n) {
    expectToken(token, index);
    expect(token.isNumber()).toBeTrue();
    expect(token.toNumber()).toEqual(n);
  }

  private static void expectStringToken(Token token, int index, String str) {
    expectToken(token, index);
    expect(token.isString()).toBeTrue();
    expect(token.toString()).toEqual(str);
  }

  private static void expectIdentifierToken(Token token, int index, String identifier) {
    expectToken(token, index);
    expect(token.isIdentifier()).toBeTrue();
    expect(token.toString()).toEqual(identifier);
  }

  private static void expectKeywordToken(Token token, int index, IElementType keyword) {
    expectToken(token, index);
    expect(token.tokenType == keyword).toBeTrue();
  }

  private static void expectErrorToken(Token token, int index, String message) {
    // expectToken(token, index);
    expect(token.isError()).toBeTrue();
    //expect(token.toString()).toEqual(message);
  }

  {

    describe("lexer", () -> {
      describe("token", () -> {
        it("should tokenize a simple identifier", () -> {
          final Token[] tokens = lex("j");
          expect(tokens.length).toEqual(1);
          expectIdentifierToken(tokens[0], 0, "j");
        });

        it("should tokenize \"this\"", () -> {
          final Token[] tokens = lex("this");
          expect(tokens.length).toEqual(1);
          expectKeywordToken(tokens[0], 0, THIS_KEYWORD);
        });

        it("should tokenize a dotted identifier", () -> {
          final Token[] tokens = lex("j.k");
          expect(tokens.length).toEqual(3);
          expectIdentifierToken(tokens[0], 0, "j");
          expectCharacterToken(tokens[1], 1, DOT);
          expectIdentifierToken(tokens[2], 2, "k");
        });

        it("should tokenize an operator", () -> {
          final Token[] tokens = lex("j-k");
          expect(tokens.length).toEqual(3);
          expectOperatorToken(tokens[1], 1, MINUS);
        });

        it("should tokenize an indexed operator", () -> {
          final Token[] tokens = lex("j[k]");
          expect(tokens.length).toEqual(4);
          expectCharacterToken(tokens[1], 1, LBRACKET);
          expectCharacterToken(tokens[3], 3, RBRACKET);
        });

        it("should tokenize numbers", () -> {
          final Token[] tokens = lex("88");
          expect(tokens.length).toEqual(1);
          expectNumberToken(tokens[0], 0, 88);
        });

        it("should tokenize numbers within index ops",
           () -> {
             expectNumberToken(lex("a[22]")[2], 2, 22);
           });

        it("should tokenize simple quoted strings",
           () -> {
             expectStringToken(lex("\"a\"")[0], 0, "a");
           });

        it("should tokenize quoted strings with escaped quotes",
           () -> {
             expectStringToken(lex("\"a\\\"\"")[0], 0, "a\"");
           });

        it("should tokenize a string", () -> {
          final Token[] tokens = lex("j-a.bc[22]+1.3|f:'a\\'c':\"d\\\"e\"");
          expectIdentifierToken(tokens[0], 0, "j");
          expectOperatorToken(tokens[1], 1, MINUS);
          expectIdentifierToken(tokens[2], 2, "a");
          expectCharacterToken(tokens[3], 3, DOT);
          expectIdentifierToken(tokens[4], 4, "bc");
          expectCharacterToken(tokens[5], 6, LBRACKET);
          expectNumberToken(tokens[6], 7, 22);
          expectCharacterToken(tokens[7], 9, RBRACKET);
          expectOperatorToken(tokens[8], 10, PLUS);
          expectNumberToken(tokens[9], 11, 1.3);
          expectOperatorToken(tokens[10], 14, OR);
          expectIdentifierToken(tokens[11], 15, "f");
          expectCharacterToken(tokens[12], 16, COLON);
          expectStringToken(tokens[13], 17, "a'c");
          expectCharacterToken(tokens[14], 23, COLON);
          expectStringToken(tokens[15], 24, "d\"e");
        });

        it("should tokenize undefined", () -> {
          final Token[] tokens = lex("undefined");
          expectKeywordToken(tokens[0], 0, UNDEFINED_KEYWORD);
          expect(tokens[0].isKeywordUndefined()).toBeTrue();
        });

        it("should ignore whitespace", () -> {
          final Token[] tokens = lex("a \t \n \r b");
          expectIdentifierToken(tokens[0], 0, "a");
          expectIdentifierToken(tokens[1], 8, "b");
        });

        it("should tokenize quoted string", () -> {
          final String str = "['\\'', \"\\\"\"]";
          final Token[] tokens = lex(str);
          expectStringToken(tokens[1], 1, "'");
          expectStringToken(tokens[3], 7, "\"");
        });

        it("should tokenize escaped quoted string", () -> {
          final String str = "\"\\\"\\n\\f\\r\\t\\v\\u00A0\"";
          final Token[] tokens = lex(str);
          expect(tokens.length).toEqual(1);
          expect(tokens[0].toString()).toEqual("\"\n\f\r\t\u000b\u00A0");
        });

        it("should tokenize unicode", () -> {
          final Token[] tokens = lex("\"\\u00A0\"");
          expect(tokens.length).toEqual(1);
          expect(tokens[0].toString()).toEqual("\u00a0");
        });

        it("should tokenize relation", () -> {
          final Token[] tokens = lex("! == != < > <= >= === !==");
          expectOperatorToken(tokens[0], 0, EXCL);
          expectOperatorToken(tokens[1], 2, EQEQ);
          expectOperatorToken(tokens[2], 5, NE);
          expectOperatorToken(tokens[3], 8, LT);
          expectOperatorToken(tokens[4], 10, GT);
          expectOperatorToken(tokens[5], 12, LE);
          expectOperatorToken(tokens[6], 15, GE);
          expectOperatorToken(tokens[7], 18, EQEQEQ);
          expectOperatorToken(tokens[8], 22, NEQEQ);
        });

        it("should tokenize statements", () -> {
          final Token[] tokens = lex("a;b;");
          expectIdentifierToken(tokens[0], 0, "a");
          expectCharacterToken(tokens[1], 1, SEMICOLON);
          expectIdentifierToken(tokens[2], 2, "b");
          expectCharacterToken(tokens[3], 3, SEMICOLON);
        });

        it("should tokenize function invocation", () -> {
          final Token[] tokens = lex("a()");
          expectIdentifierToken(tokens[0], 0, "a");
          expectCharacterToken(tokens[1], 1, LPAR);
          expectCharacterToken(tokens[2], 2, RPAR);
        });

        it("should tokenize simple method invocations", () -> {
          final Token[] tokens = lex("a.method()");
          expectIdentifierToken(tokens[2], 2, "method");
        });

        it("should tokenize method invocation", () -> {
          final Token[] tokens = lex("a.b.c (d) - e.f()");
          expectIdentifierToken(tokens[0], 0, "a");
          expectCharacterToken(tokens[1], 1, DOT);
          expectIdentifierToken(tokens[2], 2, "b");
          expectCharacterToken(tokens[3], 3, DOT);
          expectIdentifierToken(tokens[4], 4, "c");
          expectCharacterToken(tokens[5], 6, LPAR);
          expectIdentifierToken(tokens[6], 7, "d");
          expectCharacterToken(tokens[7], 8, RPAR);
          expectOperatorToken(tokens[8], 10, MINUS);
          expectIdentifierToken(tokens[9], 12, "e");
          expectCharacterToken(tokens[10], 13, DOT);
          expectIdentifierToken(tokens[11], 14, "f");
          expectCharacterToken(tokens[12], 15, LPAR);
          expectCharacterToken(tokens[13], 16, RPAR);
        });

        it("should tokenize number", () -> {
          expectNumberToken(lex("0.5")[0], 0, 0.5);
        });

        it("should tokenize number with exponent", () -> {
          Token[] tokens = lex("0.5E-10");
          expect(tokens.length).toEqual(1);
          expectNumberToken(tokens[0], 0, 0.5E-10);
          tokens = lex("0.5E+10");
          expectNumberToken(tokens[0], 0, 0.5E+10);
        });

        //it("should return exception for invalid exponent", () -> {
        //  expectErrorToken(
        //    lex("0.5E-")[0], 4, "Lexer Error: Invalid exponent at column 4 in expression [0.5E-]");
        //
        //  expectErrorToken(
        //    lex("0.5E-A")[0], 4,
        //    "Lexer Error: Invalid exponent at column 4 in expression [0.5E-A]");
        //});

        it("should tokenize number starting with a dot",
           () -> {
             expectNumberToken(lex(".5")[0], 0, 0.5);
           });

        it("should throw error on invalid unicode", () -> {
          expectErrorToken(
            lex("'\\u1''bla'")[1], 2,
            "Lexer Error: Invalid unicode escape [\\u1''b] at column 2 in expression ['\\u1''bla']");
        });

        it("should tokenize hash as operator", () -> {
          expectOperatorToken(lex("#")[0], 0, SHARP);
        });

        it("should tokenize ?. as operator", () -> {
          expectOperatorToken(lex("?.")[0], 0, ELVIS);
        });
        it("should tokenize single line comment", () -> {
          final Token[] tokens = lex("a//foo bar");
          expectIdentifierToken(tokens[0], 0, "a");
          expect(tokens[1].tokenType).toEqual(C_STYLE_COMMENT);
        });
        it("should tokenize multi line comment", () -> {
          final Token[] tokens = lex("a//foo bar\nfoo");
          expect(tokens.length).toEqual(2);
          expectIdentifierToken(tokens[0], 0, "a");
          expect(tokens[1].tokenType).toEqual(C_STYLE_COMMENT);
        });
      });
    });
  }

  @SuppressWarnings("NewClassNamingConvention")
  private static final class Token {

    public final IElementType tokenType;
    public final String contents;
    public int index;

    private Token(IElementType tokenType, String contents, int index) {
      this.tokenType = tokenType;
      this.contents = contents;
      this.index = index;
    }

    public boolean isKeywordUndefined() {
      return tokenType == JSTokenTypes.UNDEFINED_KEYWORD;
    }

    public boolean isString() {
      return tokenType == JSTokenTypes.STRING_LITERAL;
    }

    public boolean isNumber() {
      return tokenType == JSTokenTypes.NUMERIC_LITERAL;
    }

    public double toNumber() {
      return Double.parseDouble(contents);
    }

    @Override
    public String toString() {
      if (tokenType == JSTokenTypes.STRING_LITERAL) {
        return StringUtil.unquoteString(contents);
      }
      return contents;
    }

    public boolean isIdentifier() {
      return tokenType == JSTokenTypes.IDENTIFIER;
    }

    public boolean isError() {
      return tokenType == BAD_CHARACTER || tokenType == INVALID_ESCAPE_SEQUENCE;
    }

    public static Token[] create(Angular2Lexer lexer, String text) {
      List<Token> result = new ArrayList<>();
      lexer.start(text, 0, text.length());
      IElementType tokenType;
      Token prevToken = null;
      while ((tokenType = lexer.getTokenType()) != null) {
        if (tokenType != WHITE_SPACE) {
          if ((tokenType == STRING_LITERAL_PART
               || tokenType == ESCAPE_SEQUENCE)) {
            String toAdd;
            if (tokenType == ESCAPE_SEQUENCE) {
              toAdd = StringUtil.unescapeStringCharacters(lexer.getTokenText().replace("\\v", "\u000b"));
            }
            else {
              toAdd = lexer.getTokenText();
            }
            if (prevToken != null
                && prevToken.tokenType == STRING_LITERAL) {
              result.set(result.size() - 1, new Token(STRING_LITERAL, prevToken.contents + toAdd, prevToken.index));
            }
            else {
              result.add(new Token(STRING_LITERAL, toAdd, lexer.getTokenStart()));
            }
          }
          else {
            result.add(new Token(tokenType, lexer.getTokenText(), lexer.getTokenStart()));
          }
          prevToken = result.get(result.size() - 1);
        }
        lexer.advance();
      }
      return result.toArray(new Token[0]);
    }
  }
}
