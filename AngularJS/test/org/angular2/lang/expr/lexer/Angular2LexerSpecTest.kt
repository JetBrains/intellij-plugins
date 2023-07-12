// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.lexer

import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.tree.IElementType
import com.mscharhag.oleaster.matcher.Matchers
import com.mscharhag.oleaster.runner.OleasterRunner
import com.mscharhag.oleaster.runner.StaticRunnerSupport.describe
import com.mscharhag.oleaster.runner.StaticRunnerSupport.it
import org.junit.runner.RunWith

@RunWith(OleasterRunner::class)
class Angular2LexerSpecTest {
  init {
    describe("lexer") {
      describe("token") {
        it("should tokenize a simple identifier") {
          val tokens = lex("j")
          Matchers.expect(tokens.size).toEqual(1)
          expectIdentifierToken(tokens[0], 0, "j")
        }
        it("should tokenize \"this\"") {
          val tokens = lex("this")
          Matchers.expect(tokens.size).toEqual(1)
          expectKeywordToken(tokens[0], 0, JSTokenTypes.THIS_KEYWORD)
        }
        it("should tokenize a dotted identifier") {
          val tokens = lex("j.k")
          Matchers.expect(tokens.size).toEqual(3)
          expectIdentifierToken(tokens[0], 0, "j")
          expectCharacterToken(tokens[1], 1, JSTokenTypes.DOT)
          expectIdentifierToken(tokens[2], 2, "k")
        }
        it("should tokenize an operator") {
          val tokens = lex("j-k")
          Matchers.expect(tokens.size).toEqual(3)
          expectOperatorToken(tokens[1], 1, JSTokenTypes.MINUS)
        }
        it("should tokenize an indexed operator") {
          val tokens = lex("j[k]")
          Matchers.expect(tokens.size).toEqual(4)
          expectCharacterToken(tokens[1], 1, JSTokenTypes.LBRACKET)
          expectCharacterToken(tokens[3], 3, JSTokenTypes.RBRACKET)
        }
        it("should tokenize numbers") {
          val tokens = lex("88")
          Matchers.expect(tokens.size).toEqual(1)
          expectNumberToken(tokens[0], 0, 88)
        }
        it("should tokenize numbers within index ops"
        ) { expectNumberToken(lex("a[22]")[2], 2, 22) }
        it("should tokenize simple quoted strings"
        ) { expectStringToken(lex("\"a\"")[0], 0, "a") }
        it("should tokenize quoted strings with escaped quotes"
        ) { expectStringToken(lex("\"a\\\"\"")[0], 0, "a\"") }
        it("should tokenize a string") {
          val tokens = lex("j-a.bc[22]+1.3|f:'a\\'c':\"d\\\"e\"")
          expectIdentifierToken(tokens[0], 0, "j")
          expectOperatorToken(tokens[1], 1, JSTokenTypes.MINUS)
          expectIdentifierToken(tokens[2], 2, "a")
          expectCharacterToken(tokens[3], 3, JSTokenTypes.DOT)
          expectIdentifierToken(tokens[4], 4, "bc")
          expectCharacterToken(tokens[5], 6, JSTokenTypes.LBRACKET)
          expectNumberToken(tokens[6], 7, 22)
          expectCharacterToken(tokens[7], 9, JSTokenTypes.RBRACKET)
          expectOperatorToken(tokens[8], 10, JSTokenTypes.PLUS)
          expectNumberToken(tokens[9], 11, 1.3)
          expectOperatorToken(tokens[10], 14, JSTokenTypes.OR)
          expectIdentifierToken(tokens[11], 15, "f")
          expectCharacterToken(tokens[12], 16, JSTokenTypes.COLON)
          expectStringToken(tokens[13], 17, "a'c")
          expectCharacterToken(tokens[14], 23, JSTokenTypes.COLON)
          expectStringToken(tokens[15], 24, "d\"e")
        }
        it("should tokenize undefined") {
          val tokens = lex("undefined")
          expectKeywordToken(tokens[0], 0, JSTokenTypes.UNDEFINED_KEYWORD)
          Matchers.expect(tokens[0].isKeywordUndefined).toBeTrue()
        }
        it("should ignore whitespace") {
          val tokens = lex("a \t \n \r b")
          expectIdentifierToken(tokens[0], 0, "a")
          expectIdentifierToken(tokens[1], 8, "b")
        }
        it("should tokenize quoted string") {
          val str = "['\\'', \"\\\"\"]"
          val tokens = lex(str)
          expectStringToken(tokens[1], 1, "'")
          expectStringToken(tokens[3], 7, "\"")
        }
        it("should tokenize escaped quoted string") {
          val str = "\"\\\"\\n\\f\\r\\t\\v\\u00A0\""
          val tokens = lex(str)
          Matchers.expect(tokens.size).toEqual(1)
          Matchers.expect(tokens[0].toString()).toEqual("\"\n\u000c\r\t\u000b\u00A0")
        }
        it("should tokenize unicode") {
          val tokens = lex("\"\\u00A0\"")
          Matchers.expect(tokens.size).toEqual(1)
          Matchers.expect(tokens[0].toString()).toEqual("\u00a0")
        }
        it("should tokenize relation") {
          val tokens = lex("! == != < > <= >= === !==")
          expectOperatorToken(tokens[0], 0, JSTokenTypes.EXCL)
          expectOperatorToken(tokens[1], 2, JSTokenTypes.EQEQ)
          expectOperatorToken(tokens[2], 5, JSTokenTypes.NE)
          expectOperatorToken(tokens[3], 8, JSTokenTypes.LT)
          expectOperatorToken(tokens[4], 10, JSTokenTypes.GT)
          expectOperatorToken(tokens[5], 12, JSTokenTypes.LE)
          expectOperatorToken(tokens[6], 15, JSTokenTypes.GE)
          expectOperatorToken(tokens[7], 18, JSTokenTypes.EQEQEQ)
          expectOperatorToken(tokens[8], 22, JSTokenTypes.NEQEQ)
        }
        it("should tokenize statements") {
          val tokens = lex("a;b;")
          expectIdentifierToken(tokens[0], 0, "a")
          expectCharacterToken(tokens[1], 1, JSTokenTypes.SEMICOLON)
          expectIdentifierToken(tokens[2], 2, "b")
          expectCharacterToken(tokens[3], 3, JSTokenTypes.SEMICOLON)
        }
        it("should tokenize function invocation") {
          val tokens = lex("a()")
          expectIdentifierToken(tokens[0], 0, "a")
          expectCharacterToken(tokens[1], 1, JSTokenTypes.LPAR)
          expectCharacterToken(tokens[2], 2, JSTokenTypes.RPAR)
        }
        it("should tokenize simple method invocations") {
          val tokens = lex("a.method()")
          expectIdentifierToken(tokens[2], 2, "method")
        }
        it("should tokenize method invocation") {
          val tokens = lex("a.b.c (d) - e.f()")
          expectIdentifierToken(tokens[0], 0, "a")
          expectCharacterToken(tokens[1], 1, JSTokenTypes.DOT)
          expectIdentifierToken(tokens[2], 2, "b")
          expectCharacterToken(tokens[3], 3, JSTokenTypes.DOT)
          expectIdentifierToken(tokens[4], 4, "c")
          expectCharacterToken(tokens[5], 6, JSTokenTypes.LPAR)
          expectIdentifierToken(tokens[6], 7, "d")
          expectCharacterToken(tokens[7], 8, JSTokenTypes.RPAR)
          expectOperatorToken(tokens[8], 10, JSTokenTypes.MINUS)
          expectIdentifierToken(tokens[9], 12, "e")
          expectCharacterToken(tokens[10], 13, JSTokenTypes.DOT)
          expectIdentifierToken(tokens[11], 14, "f")
          expectCharacterToken(tokens[12], 15, JSTokenTypes.LPAR)
          expectCharacterToken(tokens[13], 16, JSTokenTypes.RPAR)
        }
        it("should tokenize number") {
          expectNumberToken(
            lex("0.5")[0], 0, 0.5)
        }
        it("should tokenize number with exponent") {
          var tokens = lex("0.5E-10")
          Matchers.expect(tokens.size).toEqual(1)
          expectNumberToken(tokens[0], 0, 0.5E-10)
          tokens = lex("0.5E+10")
          expectNumberToken(tokens[0], 0, 0.5E+10)
        }

        //it("should return exception for invalid exponent", () -> {
        //  expectErrorToken(
        //    lex("0.5E-")[0], 4, "Lexer Error: Invalid exponent at column 4 in expression [0.5E-]");
        //
        //  expectErrorToken(
        //    lex("0.5E-A")[0], 4,
        //    "Lexer Error: Invalid exponent at column 4 in expression [0.5E-A]");
        //});
        it("should tokenize number starting with a dot"
        ) { expectNumberToken(lex(".5")[0], 0, 0.5) }
        it("should throw error on invalid unicode") {
          expectErrorToken(
            lex("'\\u1''bla'")[1], 2,
            "Lexer Error: Invalid unicode escape [\\u1''b] at column 2 in expression ['\\u1''bla']")
        }
        it("should tokenize hash as operator") {
          expectOperatorToken(
            lex("#")[0], 0, JSTokenTypes.SHARP)
        }
        it("should tokenize ?. as operator") {
          expectOperatorToken(
            lex("?.")[0], 0, JSTokenTypes.ELVIS)
        }
        it("should tokenize single line comment") {
          val tokens = lex("a//foo bar")
          expectIdentifierToken(tokens[0], 0, "a")
          Matchers.expect(tokens[1].tokenType).toEqual(JSTokenTypes.C_STYLE_COMMENT)
        }
        it("should tokenize multi line comment") {
          val tokens = lex("a//foo bar\nfoo")
          Matchers.expect(tokens.size).toEqual(2)
          expectIdentifierToken(tokens[0], 0, "a")
          Matchers.expect(tokens[1].tokenType).toEqual(JSTokenTypes.C_STYLE_COMMENT)
        }
      }
    }
  }

  private class Token private constructor(val tokenType: IElementType, val contents: String, var index: Int) {
    val isKeywordUndefined: Boolean
      get() = tokenType === JSTokenTypes.UNDEFINED_KEYWORD
    val isString: Boolean
      get() = tokenType === JSTokenTypes.STRING_LITERAL
    val isNumber: Boolean
      get() = tokenType === JSTokenTypes.NUMERIC_LITERAL
    val isIdentifier: Boolean
      get() = tokenType === JSTokenTypes.IDENTIFIER
    val isError: Boolean
      get() = tokenType === JSTokenTypes.BAD_CHARACTER || tokenType === Angular2TokenTypes.INVALID_ESCAPE_SEQUENCE

    fun toNumber(): Double {
      return contents.toDouble()
    }

    override fun toString(): String {
      return if (tokenType === JSTokenTypes.STRING_LITERAL) {
        StringUtil.unquoteString(contents)
      }
      else contents
    }

    companion object {
      fun create(lexer: Angular2Lexer, text: String): Array<Token> {
        val result: MutableList<Token> = ArrayList()
        lexer.start(text, 0, text.length)
        var tokenType: IElementType = JSTokenTypes.WHITE_SPACE
        var prevToken: Token? = null
        while (lexer.getTokenType()?.also { tokenType = it } != null) {
          if (tokenType !== JSTokenTypes.WHITE_SPACE) {
            if (tokenType === JSTokenTypes.STRING_LITERAL_PART
                || tokenType === Angular2TokenTypes.ESCAPE_SEQUENCE) {
              val toAdd = if (tokenType === Angular2TokenTypes.ESCAPE_SEQUENCE) {
                StringUtil.unescapeStringCharacters(lexer.tokenText.replace("\\v", "\u000b"))
              }
              else {
                lexer.tokenText
              }
              if (prevToken != null
                  && prevToken.tokenType === JSTokenTypes.STRING_LITERAL) {
                result[result.size - 1] = Token(JSTokenTypes.STRING_LITERAL,
                                                prevToken.contents + toAdd, prevToken.index)
              }
              else {
                result.add(Token(JSTokenTypes.STRING_LITERAL, toAdd, lexer.getTokenStart()))
              }
            }
            else {
              result.add(Token(tokenType, lexer.tokenText, lexer.getTokenStart()))
            }
            prevToken = result[result.size - 1]
          }
          lexer.advance()
        }
        return result.toTypedArray<Token>()
      }
    }
  }

  companion object {
    private fun lex(text: String): Array<Token> {
      return Token.create(Angular2Lexer(), text)
    }

    private fun expectToken(token: Token, index: Int) {
      Matchers.expect(token.index).toEqual(index.toLong())
    }

    private fun expectCharacterToken(token: Token, index: Int, character: IElementType) {
      expectToken(token, index)
      Matchers.expect(token.tokenType === character).toBeTrue()
    }

    private fun expectOperatorToken(token: Token, index: Int, operator: IElementType) {
      expectToken(token, index)
      Matchers.expect(token.tokenType === operator).toBeTrue()
    }

    private fun expectNumberToken(token: Token, index: Int, n: Int) {
      expectToken(token, index)
      Matchers.expect(token.isNumber).toBeTrue()
      Matchers.expect(token.toNumber()).toEqual(n.toDouble())
    }

    private fun expectNumberToken(token: Token, index: Int, n: Double) {
      expectToken(token, index)
      Matchers.expect(token.isNumber).toBeTrue()
      Matchers.expect(token.toNumber()).toEqual(n)
    }

    private fun expectStringToken(token: Token, index: Int, str: String) {
      expectToken(token, index)
      Matchers.expect(token.isString).toBeTrue()
      Matchers.expect(token.toString()).toEqual(str)
    }

    private fun expectIdentifierToken(token: Token, index: Int, identifier: String) {
      expectToken(token, index)
      Matchers.expect(token.isIdentifier).toBeTrue()
      Matchers.expect(token.toString()).toEqual(identifier)
    }

    private fun expectKeywordToken(token: Token, index: Int, keyword: IElementType) {
      expectToken(token, index)
      Matchers.expect(token.tokenType === keyword).toBeTrue()
    }

    private fun expectErrorToken(token: Token, index: Int, message: String) {
      // expectToken(token, index);
      Matchers.expect(token.isError).toBeTrue()
      //expect(token.toString()).toEqual(message);
    }
  }
}
