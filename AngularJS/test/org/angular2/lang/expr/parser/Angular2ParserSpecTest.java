// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.angular2.lang.expr.parser;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.LightPlatformTestCase;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.containers.ContainerUtil;
import com.mscharhag.oleaster.runner.OleasterRunner;
import junit.framework.AssertionFailedError;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.expr.psi.Angular2BindingPipe;
import org.angular2.lang.expr.psi.Angular2RecursiveVisitor;
import org.angular2.lang.expr.psi.Angular2TemplateBinding;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Map;

import static com.intellij.openapi.util.Pair.pair;
import static com.intellij.testFramework.LightPlatformTestCase.getProject;
import static com.mscharhag.oleaster.matcher.Matchers.expect;
import static com.mscharhag.oleaster.runner.StaticRunnerSupport.describe;
import static com.mscharhag.oleaster.runner.StaticRunnerSupport.it;
import static org.angular2.lang.expr.lexer.Angular2TokenTypes.*;


@SuppressWarnings({"CodeBlock2Expr", "JUnitTestCaseWithNoTests", "SameParameterValue", "JUnitTestCaseWithNonTrivialConstructors",
  "ClassInitializerMayBeStatic"})
@RunWith(OleasterRunner.class)
public class Angular2ParserSpecTest {
  {
    describe("parser", () -> {
      describe("parseAction", () -> {
        it("should parse numbers", () -> {
          checkAction("1");
        });

        it("should parse strings", () -> {
          checkAction("'1'", "\"1\"");
          checkAction("\"1\"");
        });

        it("should parse null", () -> {
          checkAction("null");
        });

        it("should parse undefined", () -> {
          checkAction("undefined");
        });

        it("should parse unary - expressions", () -> {
          checkAction("-1", "0 - 1");
          checkAction("+1", "1 - 0");
          checkAction("-'1'", "0 - \"1\"");
          checkAction("+'1'", "\"1\" - 0");
        });

        it("should parse unary ! expressions", () -> {
          checkAction("!true");
          checkAction("!!true");
          checkAction("!!!true");
        });

        it("should parse postfix ! expression", () -> {
          checkAction("true!");
          checkAction("a!.b");
          checkAction("a!!!!.b");
        });

        it("should parse multiplicative expressions",
           () -> {
             checkAction("3*4/2%5", "3 * 4 / 2 % 5");
           });

        it("should parse additive expressions", () -> {
          checkAction("3 + 6 - 2");
        });

        it("should parse relational expressions", () -> {
          checkAction("2 < 3");
          checkAction("2 > 3");
          checkAction("2 <= 2");
          checkAction("2 >= 2");
        });

        it("should parse equality expressions", () -> {
          checkAction("2 == 3");
          checkAction("2 != 3");
        });

        it("should parse strict equality expressions", () -> {
          checkAction("2 === 3");
          checkAction("2 !== 3");
        });

        it("should parse expressions", () -> {
          checkAction("true && true");
          checkAction("true || false");
        });

        it("should parse grouped expressions", () -> {
          checkAction("(1 + 2) * 3");
        });

        it("should ignore comments in expressions", () -> {
          checkAction("a //comment", "a");
        });

        it("should retain // in string literals",
           () -> {
             checkAction("\"http://www.google.com\"", "\"http://www.google.com\"");
           });

        it("should parse an empty string", () -> {
          checkAction("");
        });

        describe("literals", () -> {
          it("should parse array", () -> {
            checkAction("[1][0]");
            checkAction("[[1]][0][0]");
            checkAction("[]");
            checkAction("[].length");
            checkAction("[1, 2].length");
          });

          it("should parse map", () -> {
            checkAction("{}");
            checkAction("{a: 1, \"b\": 2}[2]");
            checkAction("{}[\"a\"]");
          });

          it("should only allow identifier, string, or keyword as map key", () -> {
            expectActionError("{(:0}", "expected identifier, keyword, or string");
            expectActionError("{1234:0}", "expected identifier, keyword, or string");
          });
        });

        describe("member access", () -> {
          it("should parse field access", () -> {
            checkAction("a");
            checkAction("this.a", "a");
            checkAction("a.a");
          });

          it("should only allow identifier or keyword as member names", () -> {
            expectActionError("x.(", "identifier or keyword");
            expectActionError("x. 1234", "identifier or keyword");
            expectActionError("x.\"foo\"", "identifier or keyword");
          });

          it("should parse safe field access", () -> {
            checkAction("a?.a");
            checkAction("a.a?.a");
          });
        });

        describe("method calls", () -> {
          it("should parse method calls", () -> {
            checkAction("fn()");
            checkAction("add(1, 2)");
            checkAction("a.add(1, 2)");
            checkAction("fn().add(1, 2)");
          });
        });

        describe("functional calls", () -> {
          it("should parse function calls", () -> {
            checkAction("fn()(1, 2)");
          });
        });

        describe("conditional", () -> {
          it("should parse ternary/conditional expressions", () -> {
            checkAction("7 == 3 + 4 ? 10 : 20");
            checkAction("false ? 10 : 20");
          });

          it("should report incorrect ternary operator syntax", () -> {
            expectActionError("true?1", "Conditional expression true?1 requires all 3 expressions");
          });
        });

        describe("assignment", () -> {
          it("should support field assignments", () -> {
            checkAction("a = 12");
            checkAction("a.a.a = 123");
            checkAction("a = 123; b = 234;");
          });

          it("should report on safe field assignments",
             () -> {
               expectActionError("a?.a = 123", "cannot be used in the assignment");
             });

          it("should support array updates", () -> {
            checkAction("a[0] = 200");
          });
        });

        it("should error when using pipes",
           () -> {
             expectActionError("x|blah", "Cannot have a pipe");
           });

        //it("should store the source in the result",
        //   () -> { expect(parseAction("someExpr", "someExpr")); });
        //
        //it("should store the passed-in location",
        //   () -> { expect(parseAction("someExpr", "location").location).toBe("location"); });

        it("should report when encountering interpolation", () -> {
          expectActionError("{{a()}}", "Got interpolation ({{}}) where expression was expected");
        });
      });

      describe("general error handling", () -> {
        it("should report an unexpected token",
           () -> {
             expectActionError("[1,2] trac", "Unexpected token 'trac'");
           });

        it("should report reasonable error for unconsumed tokens",
           () -> {
             expectActionError(")", "Unexpected token ) at column 1 in [)]");
           });

        it("should report a missing expected token", () -> {
          expectActionError("a(b", "Missing expected ) at the end of the expression [a(b]");
        });
      });

      describe("parseBinding", () -> {
        describe("pipes", () -> {
          it("should parse pipes", () -> {
            checkBinding("a(b | c)", "a((b | c))");
            checkBinding("a.b(c.d(e) | f)", "a.b((c.d(e) | f))");
            checkBinding("[1, 2, 3] | a", "([1, 2, 3] | a)");
            checkBinding("{a: 1, \"b\": 2} | c", "({a: 1, \"b\": 2} | c)");
            checkBinding("a[b] | c", "(a[b] | c)");
            checkBinding("a?.b | c", "(a?.b | c)");
            checkBinding("true | a", "(true | a)");
            checkBinding("a | b:c | d", "((a | b:c) | d)");
            checkBinding("a | b:(c | d)", "(a | b:(c | d))");
          });

          it("should only allow identifier or keyword as formatter names", () -> {
            expectBindingError("\"Foo\"|(", "identifier or keyword");
            expectBindingError("\"Foo\"|1234", "identifier or keyword");
            expectBindingError("\"Foo\"|\"uppercase\"", "identifier or keyword");
          });

          it("should parse quoted expressions", () -> {
            checkBinding("a:b", "a:b");
          });

          it("should not crash when prefix part is not tokenizable",
             () -> {
               checkBinding("\"a:b\"", "\"a:b\"");
             });

          it("should ignore whitespace around quote prefix", () -> {
            checkBinding(" a :b", "a:b");
          });

          it("should refuse prefixes that are not single identifiers", () -> {
            expectBindingError("a + b:c", "");
            expectBindingError("1:c", "");
          });
        });
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

        it("should report chain expressions",
           () -> {
             expectError(parseBinding("1;2"), "contain chained expression");
           });

        it("should report assignment",
           () -> {
             expectError(parseBinding("a=2"), "contain assignments");
           });

        it("should report when encountering interpolation", () -> {
          expectBindingError("{{a.b}}", "Got interpolation ({{}}) where expression was expected");
        });

        it("should parse conditional expression", () -> {
          checkBinding("a < b ? a : b");
        });

        it("should ignore comments in bindings", () -> {
          checkBinding("a //comment", "a");
        });

        it("should retain // in string literals",
           () -> {
             checkBinding("\"http://www.google.com\"", "\"http://www.google.com\"");
           });

        it("should retain // in : microsyntax", () -> {
          checkBinding("one:a//b", "one:a//b");
        });
      });

      describe("parseTemplateBindings", () -> {

        it("should parse a key without a value",
           () -> {
             expect(keys(parseTemplateBindings("a", ""))).toEqual(new String[]{"a"});
           });

        it("should allow string including dashes as keys", () -> {
          Angular2TemplateBinding[] bindings = parseTemplateBindings("a", "b");
          expect(keys(bindings)).toEqual(new String[]{"a"});

          bindings = parseTemplateBindings("a-b", "c");
          expect(keys(bindings)).toEqual(new String[]{"a-b"});
        });

        it("should detect expressions as value", () -> {
          Angular2TemplateBinding[] bindings = parseTemplateBindings("a", "b");
          expect(exprSources(bindings)).toEqual(new String[]{"b"});

          bindings = parseTemplateBindings("a", "1+1");
          expect(exprSources(bindings)).toEqual(new String[]{"1+1"});
        });

        it("should detect names as value", () -> {
          final Angular2TemplateBinding[] bindings = parseTemplateBindings("a", "let b");
          expect(keyValues(bindings)).toEqual(new String[]{"a", "let b=$implicit"});
        });

        it("should allow space and colon as separators", () -> {
          Angular2TemplateBinding[] bindings = parseTemplateBindings("a", "b");
          expect(keys(bindings)).toEqual(new String[]{"a"});
          expect(exprSources(bindings)).toEqual(new String[]{"b"});
        });

        it("should allow multiple pairs", () -> {
          final Angular2TemplateBinding[] bindings = parseTemplateBindings("a", "1 b 2");
          expect(keys(bindings)).toEqual(new String[]{"a", "aB"});
          expect(exprSources(bindings)).toEqual(new String[]{"1 ", "2"});
        });

        it("should store the sources in the result", () -> {
          final Angular2TemplateBinding[] bindings = parseTemplateBindings("a", "1,b 2");
          expect(bindings[0].getExpression().getText()).toEqual("1");
          expect(bindings[1].getExpression().getText()).toEqual("2");
        });

        //it("should store the passed-in location", () -> {
        //  final Angular2TemplateBinding[] bindings = parseTemplateBindings("a", "1,b 2", "location");
        //  expect(bindings[0].getExpression().getLocation()).toEqual("location");
        //});

        it("should support let notation", () -> {
          Angular2TemplateBinding[] bindings = parseTemplateBindings("key", "let i");
          expect(keyValues(bindings)).toEqual(new String[]{"key", "let i=$implicit"});

          bindings = parseTemplateBindings("key", "let a; let b");
          expect(keyValues(bindings)).toEqual(new String[]{
            "key",
            "let a=$implicit",
            "let b=$implicit",
          });

          bindings = parseTemplateBindings("key", "let a; let b;");
          expect(keyValues(bindings)).toEqual(new String[]{
            "key",
            "let a=$implicit",
            "let b=$implicit",
          });

          bindings = parseTemplateBindings("key", "let i-a = k-a");
          expect(keyValues(bindings)).toEqual(new String[]{
            "key",
            "let i-a=k-a",
          });

          bindings = parseTemplateBindings("key", "let item; let i = k");
          expect(keyValues(bindings)).toEqual(new String[]{
            "key",
            "let item=$implicit",
            "let i=k",
          });

          bindings = parseTemplateBindings("directive", "let item in expr; let a = b", "location");
          expect(keyValues(bindings)).toEqual(new String[]{
            "directive",
            "let item=$implicit",
            "directiveIn=expr in location",
            "let a=b",
          });
        });

        it("should support as notation", () -> {
          Angular2TemplateBinding[] bindings = parseTemplateBindings("ngIf", "exp as local", "location");
          expect(keyValues(bindings)).toEqual(new String[]{"ngIf=exp  in location", "let local=ngIf"});

          bindings = parseTemplateBindings("ngFor", "let item of items as iter; index as i", "L");
          expect(keyValues(bindings)).toEqual(new String[]{
            "ngFor", "let item=$implicit", "ngForOf=items  in L", "let iter=ngForOf", "let i=index"
          });
        });

        it("should parse pipes", () -> {
          final Angular2TemplateBinding[] bindings = parseTemplateBindings("key", "value|pipe");
          final PsiElement ast = bindings[0].getExpression();
          expect(ast).toBeInstanceOf(Angular2BindingPipe.class);
        });

        describe("spans", () -> {
          it("should should support let", () -> {
            final String source = "let i";
            expect(keySpans(source, parseTemplateBindings("key", "let i"))).toEqual(new String[]{"", "let i"});
          });

          it("should support multiple lets", () -> {
            final String source = "let item; let i=index; let e=even;";
            expect(keySpans(source, parseTemplateBindings("key", source))).toEqual(new String[]{"", "let item", "let i=index", "let e=even"
            });
          });

          it("should support a prefix", () -> {
            final String source = "let person of people";
            final String prefix = "ngFor";
            final Angular2TemplateBinding[] bindings = parseTemplateBindings(prefix, source);
            expect(keyValues(bindings)).toEqual(new String[]{
              "ngFor", "let person=$implicit", "ngForOf=people in null"
            });
            expect(keySpans(source, bindings)).toEqual(new String[]{"", "let person ", "of people"});
          });
        });
      });

      describe("parseSimpleBinding", () -> {
        it("should parse a field access", () -> {
          ASTNode p = parseSimpleBinding("name");
          expect(unparse(p)).toEqual("name");
        });

        it("should report when encountering pipes", () -> {
          expectError(
            parseSimpleBinding("a | somePipe"),
            "Host binding expression cannot contain pipes");
        });

        it("should report when encountering interpolation", () -> {
          expectError(
            parseSimpleBinding("{{exp}}"),
            "Got interpolation ({{}}) where expression was expected");
        });

        it("should report when encountering field write", () -> {
          expectError(parseSimpleBinding("a = b"), "Bindings cannot contain assignments");
        });
      });
      describe("error recovery", () -> {
        it("should be able to recover from an extra paren", () -> recover("((a)))", "a"));
        it("should be able to recover from an extra bracket", () -> recover("[[a]]]", "[[a]]"));
        it("should be able to recover from a missing )", () -> recover("(a;b", "a; b;"));
        it("should be able to recover from a missing ]", () -> recover("[a,b", "[a, b]"));
        it("should be able to recover from a missing selector", () -> recover("a."));
        it("should be able to recover from a missing selector in a array literal",
           () -> recover("[[a.], b, c]"));
      });
    });


    //setup environment
    new LightPlatformTestCase() {
      {
        try {
          setUp();
        }
        catch (Exception e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      protected boolean shouldContainTempFiles() {
        return false;
      }
    };
  }

  private static void checkAction(String exp) {
    checkAction(exp, exp);
  }

  private static void checkAction(String text, String expected) {
    expectAst(parseAction(text), expected);
  }

  private static void expectActionError(String text, String error) {
    expectError(parseAction(text), error);
  }

  private static ASTNode parseAction(String text) {
    return parse(text, Angular2PsiParser.ACTION);
  }

  private static void recover(String text) {
    recover(text, text);
  }

  private static void recover(String text, String expected) {
    ASTNode expr = parseAction(text);
    expect(unparse(ReadAction.compute(() -> expr.getPsi()), false)).toEqual(expected);
  }

  private static void checkBinding(String text) {
    checkBinding(text, text);
  }

  private static void checkBinding(String text, String expected) {
    expectAst(parseBinding(text), expected);
  }

  private static void expectBindingError(String text, String error) {
    expectError(parseBinding(text), error);
  }

  private static ASTNode parseBinding(String text) {
    return parse(text, Angular2PsiParser.BINDING);
  }

  private static ASTNode parseSimpleBinding(String text) {
    return parse(text, Angular2PsiParser.SIMPLE_BINDING);
  }

  private static Angular2TemplateBinding[] parseTemplateBindings(String key, String value) {
    return parseTemplateBindings(key, value, null);
  }

  private static Angular2TemplateBinding[] parseTemplateBindings(String key, String value, String location) {
    return null;
  }

  private static String[] keys(Angular2TemplateBinding[] templateBindings) {
    return Arrays.stream(templateBindings)
                 .map(binding -> binding.getKey())
                 .toArray(String[]::new);
  }

  private static String[] keyValues(Angular2TemplateBinding[] templateBindings) {
    return Arrays.stream(templateBindings).map(binding -> {
      if (binding.keyIsVar()) {
        return "let " + binding.getKey() + (binding.getName() == null ? "=null" : '=' + binding.getName());
      }
      else {
        return binding.getKey() + (binding.getExpression() == null ? "" : "=" + unparse(binding.getExpression()));
      }
    }).toArray(String[]::new);
  }

  private static String[] keySpans(String source, Angular2TemplateBinding[] templateBindings) {
    return Arrays.stream(templateBindings)
                 .map(binding -> source.substring(binding.getTextRange().getStartOffset(), binding.getTextRange().getEndOffset()))
                 .toArray(String[]::new);
  }

  private static String[] exprSources(Angular2TemplateBinding[] templateBindings) {
    return Arrays.stream(templateBindings)
                 .map(binding -> binding.getExpression() != null ? binding.getExpression().getText() : null)
                 .toArray(String[]::new);
  }


  private static void expectAst(ASTNode root, String expected) {
    expect(unparse(root)).toEqual(expected);
  }

  private static void expectError(ASTNode root, String error) {
    StringBuilder errors = new StringBuilder();
    root.getPsi().accept(new Angular2RecursiveVisitor() {
      @Override
      public void visitErrorElement(PsiErrorElement element) {
        if (errors.length() > 0) {
          errors.append("\n");
        }
        errors.append(element.getErrorDescription());
      }
    });
    expect(errors.toString()).toEqual(error);
  }

  private static ASTNode parse(String text, String extension) {
    return ReadAction.compute(() -> {
      VirtualFile virtualFile = new LightVirtualFile("test." + extension, text);
      SingleRootFileViewProvider viewProvider = new MySingleRootFileViewProvider(virtualFile);
      ParserDefinition parserDefinition = new Angular2ParserDefinition();
      PsiFile psiFile = parserDefinition.createFile(viewProvider);

      final Angular2ParserDefinition definition = new Angular2ParserDefinition();
      final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(getProject(), psiFile.getNode());
      return definition.createParser(getProject()).parse(Angular2ElementTypes.FILE, builder);
    });
  }

  private static String unparse(ASTNode root) {
    return ReadAction.compute(() -> {
      return unparse(root.getPsi());
    });
  }

  private static String unparse(PsiElement root) {
    return unparse(root, true);
  }

  private static String unparse(PsiElement root, boolean reportErrors) {

    return ReadAction.compute(() -> {
      MyAstUnparser unparser = new MyAstUnparser(reportErrors);
      root.accept(unparser);
      return unparser.getResult();
    });
  }

  @SuppressWarnings("NewClassNamingConvention")
  private static class MySingleRootFileViewProvider extends SingleRootFileViewProvider {

    public MySingleRootFileViewProvider(VirtualFile file) {
      super(PsiManager.getInstance(getProject()), file, false, Angular2Language.INSTANCE);
    }
  }

  @SuppressWarnings("NewClassNamingConvention")
  private static class MyAstUnparser extends Angular2RecursiveVisitor {

    private static final Map<IElementType, String> operators = ContainerUtil.newHashMap(
      pair(PLUS, "+"),
      pair(MINUS, "-"),
      pair(MULT, "*"),
      pair(DIV, "/"),
      pair(PERC, "%"),
      pair(XOR, "^"),
      pair(EQ, "="),
      pair(EQEQEQ, "==="),
      pair(NEQEQ, "!=="),
      pair(EQEQ, "=="),
      pair(NE, "!="),
      pair(LT, "<"),
      pair(GT, ">"),
      pair(LE, "<="),
      pair(GE, ">="),
      pair(ANDAND, "&&"),
      pair(OROR, "||"),
      pair(AND, "&"),
      pair(OR, "|"),
      pair(EXCL, "!")
    );

    private final StringBuilder result = new StringBuilder();

    private final boolean myReportErrors;

    public MyAstUnparser(boolean reportErrors) {
      super();
      myReportErrors = reportErrors;
    }

    @Override
    public void visitElement(PsiElement element) {
      if (element instanceof ASTWrapperPsiElement
          || element instanceof PsiErrorElement
          || element.getClass() == LeafPsiElement.class) {
        super.visitElement(element);
      }
      else {
        throw new RuntimeException(element.getClass().getName() + " not handled!!");
      }
    }

    @Override
    public void visitErrorElement(PsiErrorElement element) {
      if (myReportErrors) {
        throw new AssertionFailedError("Found error: " + element.getErrorDescription());
      }
    }

    @Override
    public void visitJSExpressionStatement(JSExpressionStatement node) {
      printElement(node.getExpression());
    }

    @Override
    public void visitJSReferenceExpression(JSReferenceExpression node) {
      if (node.getQualifier() != null) {
        printElement(node.getQualifier());
        result.append(".");
      }
      if (node.getReferenceName() != null) {
        result.append(node.getReferenceName());
      }
    }

    @Override
    public void visitJSIndexedPropertyAccessExpression(JSIndexedPropertyAccessExpression node) {
      node.getQualifier().accept(this);
      result.append("[");
      node.getIndexExpression().accept(this);
      result.append("]");
    }

    @Override
    public void visitJSParenthesizedExpression(JSParenthesizedExpression node) {
      result.append("(");
      printElement(node.getInnerExpression());
      result.append(")");
    }

    @Override
    public void visitJSBinaryExpression(JSBinaryExpression node) {
      printElement(node.getLOperand());
      result.append(" ");
      printOperator(node.getOperationSign());
      result.append(" ");
      printElement(node.getROperand());
    }

    @Override
    public void visitJSPrefixExpression(JSPrefixExpression node) {
      printOperator(node.getOperationSign());
      printElement(node.getExpression());
    }

    @Override
    public void visitJSPostfixExpression(JSPostfixExpression node) {
      printElement(node.getExpression());
      printOperator(node.getOperationSign());
    }

    @Override
    public void visitJSLiteralExpression(JSLiteralExpression node) {
      if (node.isStringLiteral()) {
        result.append("\"");
        result.append(node.getStringValue());
        result.append("\"");
      }
      else {
        result.append(node.getText());
      }
    }

    @Override
    public void visitJSArrayLiteralExpression(JSArrayLiteralExpression node) {
      result.append("[");
      boolean first = true;
      for (JSExpression expr: node.getExpressions()) {
        if (!first) {
          result.append(", ");
        }
        else {
          first = false;
        }
        printElement(expr);
      }
      result.append("]");
    }

    @Override
    public void visitJSObjectLiteralExpression(JSObjectLiteralExpression node) {
      result.append("{");
      boolean first = true;
      for (JSProperty property: node.getProperties()) {
        if (!first) {
          result.append(", ");
        }
        else {
          first = false;
        }
        printElement(property.getLiteralExpressionInitializer());
        result.append(": ");
        printElement(property.getValue());
      }
      result.append("}");
    }

    @Override
    public void visitJSCallExpression(JSCallExpression node) {
      printElement(node.getMethodExpression());
      result.append("(");
      boolean first = true;
      for (JSExpression expr: node.getArguments()) {
        if (!first) {
          result.append(", ");
        }
        else {
          first = false;
        }
        printElement(expr);
      }
      result.append(")");
    }

    @Override
    public void visitJSConditionalExpression(JSConditionalExpression node) {
      printElement(node.getCondition());
      result.append(" ? ");
      printElement(node.getThen());
      result.append(" : ");
      printElement(node.getElse());
    }

    @Override
    public void visitJSDefinitionExpression(JSDefinitionExpression node) {
      result.append(node.getInitializerReference());
      result.append(" = ");
      printElement(node.getExpression());
    }

    @Override
    public void visitJSThisExpression(JSThisExpression node) {
      result.append("this");
    }

    @Override
    public void visitJSLabeledStatement(JSLabeledStatement node) {
      result.append(node.getLabel());
      result.append(": ");
      printElement(node.getStatement());
    }

    @Override
    public void visitWhiteSpace(PsiWhiteSpace space) {
      result.append(space.getText());
    }

    public String getResult() {
      return result.toString().trim();
    }

    private void printElement(PsiElement expr) {
      if (expr == null) {
        result.append("<err>");
      }
      else {
        expr.accept(this);
      }
    }

    private void printOperator(IElementType sign) {
      result.append(operators.getOrDefault(sign, "<" + sign.toString() + ">"));
    }
  }
}
