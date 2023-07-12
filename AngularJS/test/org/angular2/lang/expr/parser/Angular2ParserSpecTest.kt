// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.parser

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.FileASTNode
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptNotNullExpression
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.util.ArrayUtil
import com.mscharhag.oleaster.matcher.Matchers
import com.mscharhag.oleaster.runner.OleasterRunner
import com.mscharhag.oleaster.runner.StaticRunnerSupport.describe
import com.mscharhag.oleaster.runner.StaticRunnerSupport.it
import junit.framework.AssertionFailedError
import org.angular2.lang.OleasterTestUtil.bootstrapLightPlatform
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.expr.psi.*
import org.junit.runner.RunWith

@RunWith(OleasterRunner::class)
class Angular2ParserSpecTest {
  init {
    describe("parser") {
      bootstrapLightPlatform()
      describe("parseAction") {
        it("should parse numbers") { checkAction("1") }
        it("should parse strings") {
          checkAction("'1'", "\"1\"")
          checkAction("\"1\"")
        }
        it("should parse null") { checkAction("null") }
        it("should parse undefined") { checkAction("undefined") }
        it("should parse unary - expressions") {
          checkAction("-1" /*, "0 - 1"*/)
          checkAction("+1" /*, "1 - 0"*/)
          checkAction("-'1'", "-\"1\"" /*, "0 - \"1\""*/)
          checkAction("+'1'", "+\"1\"" /*, "\"1\" - 0"*/)
        }
        it("should parse unary ! expressions") {
          checkAction("!true")
          checkAction("!!true")
          checkAction("!!!true")
        }
        it("should parse postfix ! expression") {
          checkAction("true!")
          checkAction("a!.b")
          checkAction("a!!!!.b")
        }
        it("should parse multiplicative expressions"
        ) { checkAction("3*4/2%5", "3 * 4 / 2 % 5") }
        it("should parse additive expressions") { checkAction("3 + 6 - 2") }
        it("should parse relational expressions") {
          checkAction("2 < 3")
          checkAction("2 > 3")
          checkAction("2 <= 2")
          checkAction("2 >= 2")
        }
        it("should parse equality expressions") {
          checkAction("2 == 3")
          checkAction("2 != 3")
        }
        it("should parse strict equality expressions") {
          checkAction("2 === 3")
          checkAction("2 !== 3")
        }
        it("should parse expressions") {
          checkAction("true && true")
          checkAction("true || false")
        }
        it("should parse grouped expressions") { checkAction("(1 + 2) * 3") }
        it("should ignore comments in expressions") { checkAction("a //comment", "a") }
        it("should retain // in string literals"
        ) { checkAction("\"http://www.google.com\"", "\"http://www.google.com\"") }
        it("should parse an empty string") { checkAction("") }
        describe("literals") {
          it("should parse array") {
            checkAction("[1][0]")
            checkAction("[[1]][0][0]")
            checkAction("[]")
            checkAction("[].length")
            checkAction("[1, 2].length")
          }
          it("should parse map") {
            checkAction("{}")
            checkAction("{a: 1, \"b\": 2, let: \"12\"}[2]")
            checkAction("{}[\"a\"]")
          }
          it("should only allow identifier, string, or keyword as map key") {
            expectActionError("{(:0}", "Expected identifier, keyword, or string")
            expectActionError("{1234:0}", "Expected identifier, keyword, or string")
          }
          it("should parse property shorthand declarations") {
            checkAction("{a, b, c}", "{a: a, b: b, c: c}")
            checkAction("{a: 1, b}", "{a: 1, b: b}")
            checkAction("{a, b: 1}", "{a: a, b: 1}")
            checkAction("{a: 1, b, c: 2}", "{a: 1, b: b, c: 2}")
          }
          it("should not allow property shorthand declaration on quoted properties") {
            expectActionError("{\"a-b\"}", ": expected")
          }
          it("should not infer invalid identifiers as shorthand property declarations") {
            expectActionError("{a.b}", ": expected")
            expectActionError("{a[\"b\"]}", ": expected")
            expectActionError("{1234}", "Expected identifier, keyword, or string")
          }
        }
        describe("member access") {
          it("should parse field access") {
            checkAction("a")
            checkAction("this.a", "this.a")
            checkAction("a.a")
            checkAction("a.var")
          }
          it("should only allow identifier or keyword as member names") {
            expectActionError("x.(", "Name expected")
            expectActionError("x. 1234", "Name expected")
            expectActionError("x.\"foo\"", "Name expected")
          }
          it("should parse safe field access") {
            checkAction("a?.a")
            checkAction("a.a?.a")
          }
        }
        describe("method calls") {
          it("should parse method calls") {
            checkAction("fn()")
            checkAction("add(1, 2)")
            checkAction("a.add(1, 2)")
            checkAction("fn().add(1, 2)")
          }
        }
        describe("functional calls") {
          it("should parse function calls") {
            checkAction("fn()(1, 2)")
          }
        }
        describe("keyed read") {
          it("should parse keyed reads") {
            checkBinding("a[\"a\"]")
            checkBinding("this.a[\"a\"]")
            checkBinding("a.a[\"a\"]")
          }
          it("should parse safe keyed reads") { checkBinding("a?.[\"a\"]") }
          describe("malformed keyed reads") {
            it("should recover on missing keys") {
              checkActionWithError("a[]", "a[]", "Expression expected") // different error than Angular
            }
            it("should recover on incomplete expression keys") {
              checkActionWithError("a[1 + ]", "a[1 + /*error*/]", "Expression expected") // different error than Angular
            }
            it("should recover on unterminated keys") {
              checkActionWithError("a[1 + 2", "a[1 + 2]", "] expected") // different error than Angular
            }
            it("should recover on incomplete and unterminated keys") {
              checkActionWithError("a[1 + ", "a[1 + /*error*/]", "Expression expected") // different error than Angular
            }
          }
        }
        describe("conditional") {
          it("should parse ternary/conditional expressions") {
            checkAction("7 == 3 + 4 ? 10 : 20")
            checkAction("false ? 10 : 20")
          }
          it("should report incorrect ternary operator syntax") { expectActionError("true?1", ": expected") }
        }
        describe("assignment") {
          it("should support field assignments") {
            checkAction("a = 12")
            checkAction("a.a.a = 123")
            checkAction("a = 123; b = 234;")
          }

          // TODO - implement as inspection
          //it("should report on safe field assignments",
          //   () -> {
          //     expectActionError("a?.a = 123", "cannot be used in the assignment");
          //   });
          it("should support array updates") { checkAction("a[0] = 200") }
        }
        it("should error when using pipes"
        ) { expectActionError("x|blah", "Action expression cannot contain pipes") }

        //it("should store the source in the result",
        //   () -> { expect(parseAction("someExpr", "someExpr")); });
        //
        //it("should store the passed-in location",
        //   () -> { expect(parseAction("someExpr", "location").location).toBe("location"); });
        it("should report when encountering interpolation") {
          expectActionError("{{a()}}",
                            "Expected identifier, keyword, or string" /* TODO - add proper parsing and tokenization of interpolations */ /*"Got interpolation ({{}}) where expression was expected"*/
          )
        }
      }
      describe("general error handling") {
        it("should report an unexpected token"
        ) { expectActionError("[1,2] trac", "Unexpected token 'trac'") }
        it("should report reasonable error for unconsumed tokens"
        ) { expectActionError(")", "Expression expected") }
        it("should report a missing expected token") { expectActionError("a(b", ", or ) expected") }
      }
      describe("parseBinding") {
        describe("pipes") {
          it("should parse pipes") {
            checkBinding("a(b | c)", "a((b | c))")
            checkBinding("a.b(c.d(e) | f)", "a.b((c.d(e) | f))")
            checkBinding("[1, 2, 3] | a", "([1, 2, 3] | a)")
            checkBinding("{a: 1, \"b\": 2} | c", "({a: 1, \"b\": 2} | c)")
            checkBinding("a[b] | c", "(a[b] | c)")
            checkBinding("a?.b | c", "(a?.b | c)")
            checkBinding("true | a", "(true | a)")
            checkBinding("a | b:c | d", "((a | b:c) | d)")
            checkBinding("a | b:(c | d)", "(a | b:((c | d)))")
          }
          it("should only allow identifier or keyword as formatter names") {
            expectBindingError("\"Foo\"|(", "identifier or keyword")
            expectBindingError("\"Foo\"|1234", "identifier or keyword")
            expectBindingError("\"Foo\"|\"uppercase\"", "identifier or keyword")
          }
          it("should parse quoted expressions") { checkBinding("a:b", "a:b") }
          it("should not crash when prefix part is not tokenizable"
          ) { checkBinding("\"a:b\"", "\"a:b\"") }
          it("should ignore whitespace around quote prefix") { checkBinding(" a :b", "a:b") }
          it("should refuse prefixes that are not single identifiers") {
            expectBindingError("a + b:c", "")
            expectBindingError("1:c", "")
          }
        }
        //
        //it("should store the source in the result",
        //   () -> {
        //     expect(parseBinding("someExpr").source).toBe("someExpr");
        //   });
        //
        //it("should store the passed-in location",
        //   () -> {
        //     expect(parseBinding("someExpr", "location").location).toBe("location");
        //   });
        it("should report chain expressions"
        ) { expectError(parseBinding("1;2"), "contain chained expressions") }
        it("should report assignment"
        ) { expectError(parseBinding("a=2"), "contain assignments") }
        it("should report when encountering interpolation") {
          expectBindingError("{{a.b}}", "Expected identifier, keyword, or string")
        }
        it("should parse conditional expression") { checkBinding("a < b ? a : b") }
        it("should parse nullish coalescing expression") { checkBinding("a ?? b") }
        it("should ignore comments in bindings") { checkBinding("a //comment", "a") }
        it("should retain // in string literals"
        ) { checkBinding("\"http://www.google.com\"", "\"http://www.google.com\"") }
        it("should retain // in : microsyntax") { checkBinding("one:a//b", "one:a//b") }
      }
      describe("parseTemplateBindings") {
        it("should parse a key without a value"
        ) { Matchers.expect(keys(parseTemplateBindings("a", ""))).toEqual(listOf("a")) }
        it("should allow string including dashes as keys") {
          var bindings = parseTemplateBindings("a", "b")
          Matchers.expect(keys(bindings)).toEqual(listOf("a"))
          bindings = parseTemplateBindings("a-b", "c")
          Matchers.expect(keys(bindings)).toEqual(listOf("a-b"))
        }
        it("should detect expressions as value") {
          var bindings = parseTemplateBindings("a", "b")
          Matchers.expect(exprSources(bindings)).toEqual(listOf("b"))
          bindings = parseTemplateBindings("a", "1+1")
          Matchers.expect(exprSources(bindings)).toEqual(listOf("1+1"))
        }
        it("should detect names as value") {
          val bindings = parseTemplateBindings("a", "let b")
          Matchers.expect(keyValues(bindings)).toEqual(listOf("a", "let b=\$implicit"))
        }
        it("should allow space and colon as separators") {
          val bindings = parseTemplateBindings("a", "b")
          Matchers.expect(keys(bindings)).toEqual(listOf("a"))
          Matchers.expect(exprSources(bindings)).toEqual(listOf("b"))
        }
        it("should allow multiple pairs") {
          val bindings = parseTemplateBindings("a", "1 b 2")
          Matchers.expect(keys(bindings)).toEqual(listOf("a", "aB"))
          Matchers.expect(exprSources(bindings)).toEqual(listOf("1", "2"))
        }
        it("should store the sources in the result") {
          val bindings = parseTemplateBindings("a", "1,b 2")
          ReadAction.run<RuntimeException> {
            Matchers.expect(bindings[0].expression!!.getText()).toEqual("1")
            Matchers.expect(bindings[1].expression!!.getText()).toEqual("2")
          }
        }

        //This feature is not required by WebStorm
        //it("should store the passed-in location", () -> {
        //  final Angular2TemplateBinding[] bindings = parseTemplateBindings("a", "1,b 2", "location");
        //  expect(bindings[0].getExpression().getLocation()).toEqual("location");
        //});
        it("should support let notation") {
          var bindings = parseTemplateBindings("key", "let i")
          Matchers.expect(keyValues(bindings)).toEqual(listOf("key", "let i=\$implicit"))
          bindings = parseTemplateBindings("key", "let a; let b")
          Matchers.expect(keyValues(bindings)).toEqual(listOf("key",
                                                              "let a=\$implicit",
                                                              "let b=\$implicit"))
          bindings = parseTemplateBindings("key", "let a; let b;")
          Matchers.expect(keyValues(bindings)).toEqual(listOf("key",
                                                              "let a=\$implicit",
                                                              "let b=\$implicit"))
          bindings = parseTemplateBindings("key", "let i-a = k-a")
          Matchers.expect(keyValues(bindings)).toEqual(listOf("key",
                                                              "let i-a=k-a"))
          bindings = parseTemplateBindings("key", "let item; let i = k")
          Matchers.expect(keyValues(bindings)).toEqual(listOf("key",
                                                              "let item=\$implicit",
                                                              "let i=k"))
          bindings = parseTemplateBindings("directive", "let item in expr; let a = b", "location")
          Matchers.expect(keyValues(bindings)).toEqual(listOf("directive",
                                                              "let item=\$implicit",
                                                              "directiveIn=expr" /* in location"*/,
                                                              "let a=b"))
        }
        it("should support as notation") {
          var bindings = parseTemplateBindings("ngIf", "exp as local", "location")
          Matchers.expect(keyValues(bindings)).toEqual(listOf("ngIf=exp" /*  in location"*/, "let local=ngIf"))
          bindings = parseTemplateBindings("ngFor", "let item of items as iter; index as i", "L")
          Matchers.expect(keyValues(bindings)).toEqual(
            listOf("ngFor", "let item=\$implicit", "ngForOf=items" /*  in L"*/, "let iter=ngForOf", "let i=index"))
        }
        it("should parse pipes") {
          val bindings = parseTemplateBindings("key", "value|pipe")
          val ast: PsiElement? = bindings[0].expression
          Matchers.expect(ast).toBeInstanceOf(Angular2PipeExpression::class.java)
        }
        describe("spans") {
          it("should should support let") {
            val source = "let i"
            Matchers.expect(keySpans(source, parseTemplateBindings("key", "let i"))).toEqual(listOf("", "let i"))
          }
          it("should support multiple lets") {
            val source = "let item; let i=index; let e=even;"
            Matchers.expect(keySpans(source, parseTemplateBindings("key", source)))
              .toEqual(listOf("", "let item", "let i=index", "let e=even"))
          }
          it("should support a prefix") {
            val source = "let person of people"
            val prefix = "ngFor"
            val bindings = parseTemplateBindings(prefix, source)
            Matchers.expect(keyValues(bindings)).toEqual(listOf("ngFor", "let person=\$implicit", "ngForOf=people"))
            Matchers.expect(keySpans(source, bindings)).toEqual(listOf("", "let person", "of people"))
          }
        }
      }
      describe("parseSimpleBinding") {
        it("should parse a field access") {
          val p = parseSimpleBinding("name")
          Matchers.expect(unparse(p)).toEqual("name")
        }
        it("should report when encountering pipes") {
          expectError(
            parseSimpleBinding("a | somePipe"),
            "Host binding expression cannot contain pipes")
        }
        it("should report when encountering interpolation") {
          expectError(
            parseSimpleBinding("{{exp}}"),
            "Expected identifier, keyword, or string" /*"Got interpolation ({{}}) where expression was expected"*/
          )
        }
        it("should report when encountering field write") {
          expectError(parseSimpleBinding("a = b"), "Binding expression cannot contain assignments")
        }
      }
      describe("error recovery") {
        it("should be able to recover from an extra paren") { recover("((a)))", "((a));") }
        it("should be able to recover from an extra bracket") { recover("[[a]]]", "[[a]];") }
        it("should be able to recover from a missing )") { recover("(a;b", "(a); b;") }
        it("should be able to recover from a missing ]") { recover("[a,b", "[a, b]") }
        it("should be able to recover from a missing selector") { recover("a.") }
        it("should be able to recover from a missing selector in a array literal"
        ) { recover("[[a.], b, c]") }
      }
    }
  }

  private class MyAstUnparser(private val myReportErrors: Boolean) : Angular2RecursiveVisitor() {
    private val result = StringBuilder()
    override fun visitElement(element: PsiElement) {
      if (element is TypeScriptNotNullExpression) {
        printElement(element.getExpression())
        result.append("!")
      }
      else if (element.javaClass == LeafPsiElement::class.java) {
        val leaf = element as LeafPsiElement
        result.append(leaf.getText())
      }
      else if (element is ASTWrapperPsiElement
               || element is PsiErrorElement
               || element is JSEmptyExpression
               || element is JSFile) {
        super.visitElement(element)
      }
      else {
        throw RuntimeException(element.javaClass.getName() + " not handled!!")
      }
    }

    override fun visitErrorElement(element: PsiErrorElement) {
      if (myReportErrors) {
        throw AssertionFailedError("Found error: " + element.getErrorDescription())
      }
    }

    override fun visitJSExpressionStatement(node: JSExpressionStatement) {
      printElement(node.getExpression())
    }

    override fun visitJSReferenceExpression(node: JSReferenceExpression) {
      val qualifier = node.getQualifier()
      if (qualifier != null) {
        printElement(qualifier)
        val qualifierType = qualifier.getNextSibling().getNode().getElementType()
        if (qualifierType === JSTokenTypes.ELVIS) {
          result.append("?.")
        }
        else if (qualifierType === JSTokenTypes.DOT) {
          result.append(".")
        }
        else {
          result.append("<q:")
            .append(qualifierType.toString())
            .append(">")
        }
      }
      if (node.getReferenceName() != null) {
        result.append(node.getReferenceName())
      }
    }

    override fun visitAngular2Chain(expressionChain: Angular2Chain) {
      for (el in expressionChain.statements) {
        printElement(el)
        result.append("; ")
      }
    }

    override fun visitAngular2Quote(quote: Angular2Quote) {
      result.append(quote.getName())
        .append(":")
        .append(quote.contents)
    }

    override fun visitAngular2PipeExpression(pipe: Angular2PipeExpression) {
      result.append("(")
      val args = pipe.getArguments()
      printElement(args[0])
      result.append(" | ")
        .append(pipe.getName())
      for (expr in ArrayUtil.remove(args, 0)) {
        result.append(":")
        printElement(expr)
      }
      result.append(")")
    }

    override fun visitAngular2Action(action: Angular2Action) {
      super.visitElement(action)
    }

    override fun visitAngular2Binding(binding: Angular2Binding) {
      super.visitElement(binding)
    }

    override fun visitAngular2Interpolation(interpolation: Angular2Interpolation) {
      super.visitElement(interpolation)
    }

    override fun visitAngular2SimpleBinding(simpleBinding: Angular2SimpleBinding) {
      super.visitElement(simpleBinding)
    }

    override fun visitJSIndexedPropertyAccessExpression(node: JSIndexedPropertyAccessExpression) {
      val qualifier = node.getQualifier()
      qualifier!!.accept(this)
      if (node.isElvis) {
        result.append("?.")
      }
      result.append("[")
      val indexExpression = node.getIndexExpression()
      indexExpression?.accept(this)
      result.append("]")
    }

    override fun visitJSParenthesizedExpression(node: JSParenthesizedExpression) {
      result.append("(")
      printElement(node.getInnerExpression())
      result.append(")")
    }

    override fun visitJSBinaryExpression(node: JSBinaryExpression) {
      printElement(node.getLOperand())
      result.append(" ")
      printOperator(node.getOperationSign())
      result.append(" ")
      printElement(node.getROperand())
    }

    override fun visitJSPrefixExpression(node: JSPrefixExpression) {
      printOperator(node.getOperationSign())
      printElement(node.getExpression())
    }

    override fun visitJSPostfixExpression(node: JSPostfixExpression) {
      printElement(node.getExpression())
      printOperator(node.getOperationSign())
    }

    override fun visitJSLiteralExpression(node: JSLiteralExpression) {
      if (node.isStringLiteral) {
        result.append("\"")
          .append(node.getStringValue())
          .append("\"")
      }
      else {
        result.append(node.getText())
      }
    }

    override fun visitJSArrayLiteralExpression(node: JSArrayLiteralExpression) {
      result.append("[")
      var first = true
      for (expr in node.getExpressions()) {
        if (!first) {
          result.append(", ")
        }
        else {
          first = false
        }
        printElement(expr)
      }
      result.append("]")
    }

    override fun visitJSObjectLiteralExpression(node: JSObjectLiteralExpression) {
      result.append("{")
      var first = true
      for (property in node.getProperties()) {
        if (!first) {
          result.append(", ")
        }
        else {
          first = false
        }
        printElement(property.getNameIdentifier())
        result.append(": ")
        printElement(property.getValue())
      }
      result.append("}")
    }

    override fun visitJSCallExpression(node: JSCallExpression) {
      printElement(node.getMethodExpression())
      result.append("(")
      var first = true
      for (expr in node.getArguments()) {
        if (!first) {
          result.append(", ")
        }
        else {
          first = false
        }
        printElement(expr)
      }
      result.append(")")
    }

    override fun visitJSConditionalExpression(node: JSConditionalExpression) {
      printElement(node.getCondition())
      result.append(" ? ")
      printElement(node.getThenBranch())
      result.append(" : ")
      printElement(node.getElseBranch())
    }

    override fun visitJSAssignmentExpression(node: JSAssignmentExpression) {
      printElement(node.getLOperand())
      result.append(" = ")
      printElement(node.getROperand())
    }

    override fun visitJSDefinitionExpression(node: JSDefinitionExpression) {
      printElement(node.getExpression())
    }

    override fun visitJSThisExpression(node: JSThisExpression) {
      result.append("this")
    }

    override fun visitJSEmptyStatement(node: JSEmptyStatement) {}
    override fun visitComment(comment: PsiComment) {
      //do nothing
    }

    override fun visitWhiteSpace(space: PsiWhiteSpace) {
      result.append(space.getText())
    }

    fun getResult(): String {
      return result.toString().trim { it <= ' ' }
    }

    private fun printElement(expr: PsiElement?) {
      expr?.accept(this) ?: result.append("/*error*/")
    }

    private fun printOperator(sign: IElementType?) {
      result.append(operators.getOrDefault(sign, "<" + sign.toString() + ">"))
    }

    companion object {
      private val operators = mapOf(
        JSTokenTypes.PLUS to "+",
        JSTokenTypes.MINUS to "-",
        JSTokenTypes.MULT to "*",
        JSTokenTypes.DIV to "/",
        JSTokenTypes.PERC to "%",
        JSTokenTypes.XOR to "^",
        JSTokenTypes.EQ to "=",
        JSTokenTypes.EQEQEQ to "===",
        JSTokenTypes.NEQEQ to "!==",
        JSTokenTypes.EQEQ to "==",
        JSTokenTypes.NE to "!=",
        JSTokenTypes.LT to "<",
        JSTokenTypes.GT to ">",
        JSTokenTypes.LE to "<=",
        JSTokenTypes.GE to ">=",
        JSTokenTypes.ANDAND to "&&",
        JSTokenTypes.OROR to "||",
        JSTokenTypes.QUEST_QUEST to "??",
        JSTokenTypes.AND to "&",
        JSTokenTypes.OR to "|",
        JSTokenTypes.EXCL to "!",
      )
    }
  }

  companion object {
    private fun checkAction(exp: String) {
      checkAction(exp, exp)
    }

    private fun checkAction(text: String, expected: String) {
      expectAst(parseAction(text), expected)
    }

    private fun expectActionError(text: String, error: String) {
      expectError(parseAction(text), error)
    }

    private fun checkActionWithError(text: String, expected: String, error: String) {
      checkAction(text, expected)
      expectActionError(text, error)
    }

    private fun parseAction(text: String): ASTNode {
      return parse(text, Angular2PsiParser.ACTION)
    }

    private fun recover(text: String, expected: String = text) {
      val expr = parseAction(text)
      Matchers.expect(unparse(ReadAction.compute<PsiElement, RuntimeException> { expr.getPsi() }, false)).toEqual(expected)
    }

    private fun checkBinding(text: String, expected: String = text) {
      expectAst(parseBinding(text), expected)
    }

    private fun expectBindingError(text: String, error: String) {
      expectError(parseBinding(text), error)
    }

    private fun parseBinding(text: String): ASTNode {
      return parse(text, Angular2PsiParser.BINDING)
    }

    private fun parseSimpleBinding(text: String): ASTNode {
      return parse(text, Angular2PsiParser.SIMPLE_BINDING)
    }

    private fun parseTemplateBindings(key: String,
                                      value: String,
                                      @Suppress("unused") location: String? = null): Array<Angular2TemplateBinding> {
      val root = parse(value, key + "." + Angular2PsiParser.TEMPLATE_BINDINGS)
      return ReadAction.compute<Array<Angular2TemplateBinding>, RuntimeException> {
        root.findChildByType(Angular2ElementTypes.TEMPLATE_BINDINGS_STATEMENT)!!
          .getPsi(Angular2TemplateBindings::class.java)
          .bindings
      }
    }

    private fun keys(templateBindings: Array<Angular2TemplateBinding>): List<String> {
      return templateBindings.map { it.key }
    }

    private fun keyValues(templateBindings: Array<Angular2TemplateBinding>): List<String> {
      return templateBindings.map { binding ->
        if (binding.keyIsVar()) {
          "let " + binding.key + if (binding.getName() == null) "=null" else '='.toString() + binding.getName()
        }
        else {
          binding.key + if (binding.expression == null) "" else "=" + unparse(binding.expression)
        }
      }
    }

    private fun keySpans(source: String, templateBindings: Array<Angular2TemplateBinding>): List<String> {
      return ReadAction.compute<List<String>, RuntimeException> {
        templateBindings.map { it.getTextRange().substring(source) }
      }
    }

    private fun exprSources(templateBindings: Array<Angular2TemplateBinding>): List<String?> {
      return ReadAction.compute<List<String?>, RuntimeException> {
        templateBindings.map { it.expression?.getText() }
      }
    }

    private fun expectAst(root: ASTNode, expected: String) {
      Matchers.expect(unparse(root)).toEqual(expected)
    }

    private fun expectError(root: ASTNode, expectedError: String) {
      val error = StringBuilder()
      root.getPsi().accept(object : Angular2RecursiveVisitor() {
        override fun visitErrorElement(element: PsiErrorElement) {
          if (error.isEmpty()) {
            error.append(element.getErrorDescription())
          }
        }
      })
      if (StringUtil.isNotEmpty(expectedError)) {
        Matchers.expect(error.toString()).toEndWith(expectedError)
      }
      else {
        Matchers.expect(!error.toString().isEmpty()).toBeTrue()
      }
    }

    private fun parse(text: String, extension: String): ASTNode {
      return ReadAction.compute<FileASTNode, RuntimeException> {
        PsiFileFactory.getInstance(ProjectManager.getInstance().getDefaultProject())
          .createFileFromText("test.$extension", Angular2Language.INSTANCE, text)
          .getNode()
      }
    }

    private fun unparse(root: ASTNode): String {
      return ReadAction.compute<String, RuntimeException> { unparse(root.getPsi()) }
    }

    private fun unparse(root: PsiElement?, reportErrors: Boolean = true): String {
      return ReadAction.compute<String, RuntimeException> {
        val unparser = MyAstUnparser(reportErrors)
        root!!.accept(unparser)
        unparser.getResult()
      }
    }
  }
}
