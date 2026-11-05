package org.jetbrains.qodana.jvm.java.psi

import com.intellij.openapi.application.readAction
import com.intellij.psi.PsiAnonymousClass
import com.intellij.psi.PsiArrayAccessExpression
import com.intellij.psi.PsiArrayInitializerExpression
import com.intellij.psi.PsiAssertStatement
import com.intellij.psi.PsiAssignmentExpression
import com.intellij.psi.PsiBinaryExpression
import com.intellij.psi.PsiBreakStatement
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassInitializer
import com.intellij.psi.PsiClassObjectAccessExpression
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiConditionalExpression
import com.intellij.psi.PsiContinueStatement
import com.intellij.psi.PsiDoWhileStatement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiExpressionStatement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiForStatement
import com.intellij.psi.PsiForeachStatement
import com.intellij.psi.PsiIfStatement
import com.intellij.psi.PsiInstanceOfExpression
import com.intellij.psi.PsiJavaCodeReferenceElement
import com.intellij.psi.PsiLabeledStatement
import com.intellij.psi.PsiLambdaExpression
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiLocalVariable
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiMethodReferenceExpression
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiNewExpression
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiPolyadicExpression
import com.intellij.psi.PsiPostfixExpression
import com.intellij.psi.PsiPrefixExpression
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.PsiReturnStatement
import com.intellij.psi.PsiSwitchLabelStatement
import com.intellij.psi.PsiSwitchStatement
import com.intellij.psi.PsiSynchronizedStatement
import com.intellij.psi.PsiThrowStatement
import com.intellij.psi.PsiTryStatement
import com.intellij.psi.PsiTypeCastExpression
import com.intellij.psi.PsiWhileStatement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtilCore
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.fixtures.lightJavaCodeInsightFixture
import com.intellij.testFramework.fixtures.lightJavaProjectFixture
import com.intellij.testFramework.junit5.TestApplication
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi.PsiSignalExtractor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes

/** Verifies [PsiSignalExtractor]: enclosing-scope detection and AST-shape serialization over real Java PSI. */
@TestApplication
class JavaPsiSignalExtractorTest {
  companion object {
    private val projectFixture = lightJavaProjectFixture()
  }

  private val myFixture by lightJavaCodeInsightFixture(projectFixture)

  @Test
  fun testExtractsEnclosingMethodScopeAndAstShape(): Unit = timeoutRunBlocking(timeout = 2.minutes) {
    val file = myFixture.configureByText(
      "Foo.java",
      """
      class Foo {
        void bar() {
          if (args()) { sink(); }
        }
        boolean args() { return true; }
        void sink() {}
      }
      """.trimIndent()
    )
    readAction {
      val ifStatement = PsiTreeUtil.findChildOfType(file, PsiIfStatement::class.java)!!

      val signals = PsiSignalExtractor.extractSignals(ifStatement)

      assertEquals("bar", signals.enclosingScopeName)
      assertEquals("METHOD", signals.enclosingScopeType)
      assertNotNull(signals.astShape)
      assertTrue(signals.astShape!!.startsWith("IF_STATEMENT"), "astShape was '${signals.astShape}'")
    }
  }

  /** A field has no enclosing method, so the class supplies the scope. */
  @Test
  fun testFieldReportsEnclosingClass(): Unit = timeoutRunBlocking(timeout = 2.minutes) {
    val file = myFixture.configureByText("Foo.java", "class Foo { int field = 1; }")
    readAction {
      val field = PsiTreeUtil.findChildOfType(file, PsiField::class.java)!!

      val signals = PsiSignalExtractor.extractSignals(field)

      assertEquals("Foo", signals.enclosingScopeName)
      assertEquals("CLASS", signals.enclosingScopeType)
      assertNotNull(signals.astShape)
    }
  }

  // type level
  @Test
  fun testClass(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiClass>("class Foo {}", "CLASS(CLASS_KEYWORD,IDENTIFIER)  [scope=Foo:CLASS]") }
  @Test
  fun testInterface(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiClass>("interface Foo {}", "CLASS(INTERFACE_KEYWORD,IDENTIFIER)  [scope=Foo:CLASS]") }
  @Test
  fun testEnum(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiClass>("enum E { A, B }", "CLASS(ENUM_KEYWORD,ENUM_CONSTANT,ENUM_CONSTANT)  [scope=E:CLASS]") }
  @Test
  fun testNestedClass(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiClass>("class Foo { class Inner {} }", "CLASS(CLASS_KEYWORD,IDENTIFIER)  [scope=Inner:CLASS]", text = "class Inner {}") }

  // class members
  @Test
  fun testField(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiField>("class Foo { int f = 1; }", "FIELD(TYPE(INT_KEYWORD),LITERAL_EXPRESSION)  [scope=Foo:CLASS]") }
  @Test
  fun testMethod(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiMethod>("class Foo { void bar() {} }", "METHOD(TYPE,IDENTIFIER,PARAMETER_LIST,CODE_BLOCK)  [scope=bar:METHOD]") }
  @Test
  fun testConstructor(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiMethod>("class Foo { Foo() {} }", "METHOD(IDENTIFIER,PARAMETER_LIST,CODE_BLOCK)  [scope=Foo:METHOD]") }
  /** An instance initializer takes the class name too, with its own element type. Same rule as `static { }`. */
  @Test
  fun testInstanceInit(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiClassInitializer>("final class Foo { { sink(); } void sink(){} }", "CLASS_INITIALIZER(CODE_BLOCK)  [scope=Foo:CLASS_INITIALIZER]") }
  @Test
  fun testStaticInit(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiClassInitializer>("final class Foo { static { sink(); } static void sink(){} }", "CLASS_INITIALIZER(MODIFIER_LIST,CODE_BLOCK)  [scope=Foo:CLASS_INITIALIZER]") }
  @Test
  fun testParameter(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiParameter>("class Foo { void bar(int p) {} }", "PARAMETER_LIST(PARAMETER(TYPE))  [scope=bar:METHOD]") }

  // initializers / assignment sides
  @Test
  fun testFieldInitRHS(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiMethodCallExpression>("class Foo { int f = compute(); int compute(){return 1;} }", "FIELD(TYPE(INT_KEYWORD),METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=Foo:CLASS]", text = "compute()") }
  @Test
  fun testLocalInitRHS(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiMethodCallExpression>("class Foo { void bar() { int x = compute(); } int compute(){return 1;} }", "DECLARATION_STATEMENT(LOCAL_VARIABLE)  [scope=bar:METHOD]", text = "compute()") }
  @Test
  fun testAssignLHS(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiReferenceExpression>("class Foo { int f; void bar() { f = compute(); } int compute(){return 1;} }", "EXPRESSION_STATEMENT(ASSIGNMENT_EXPRESSION)  [scope=bar:METHOD]", text = "f") }
  @Test
  fun testAssignRHS(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiMethodCallExpression>("class Foo { int f; void bar() { f = compute(); } int compute(){return 1;} }", "EXPRESSION_STATEMENT(ASSIGNMENT_EXPRESSION)  [scope=bar:METHOD]", text = "compute()") }
  @Test
  fun testAssignExpr(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiAssignmentExpression>("class Foo { int f; void bar() { f = compute(); } int compute(){return 1;} }", "EXPRESSION_STATEMENT(ASSIGNMENT_EXPRESSION(REFERENCE_EXPRESSION,METHOD_CALL_EXPRESSION))  [scope=bar:METHOD]", text = "f = compute()") }

  // statements in a function
  @Test
  fun testExprStatement(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiExpressionStatement>("class Foo { void bar() { sink(); } void sink(){} }", "EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=bar:METHOD]", text = "sink();") }
  @Test
  fun testLocalVariable(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiLocalVariable>("class Foo { void bar() { int x = 1; } }", "DECLARATION_STATEMENT(LOCAL_VARIABLE(TYPE,LITERAL_EXPRESSION))  [scope=bar:METHOD]") }
  @Test
  fun testReturnStatement(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiReturnStatement>("class Foo { int bar() { return 1; } }", "RETURN_STATEMENT(RETURN_KEYWORD,LITERAL_EXPRESSION)  [scope=bar:METHOD]") }

  // control flow / nested statements
  @Test
  fun testIfStatement(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiIfStatement>("class Foo { void bar() { if (a()) { sink(); } } boolean a(){return true;} void sink(){} }", "IF_STATEMENT(IF_KEYWORD,METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST),BLOCK_STATEMENT(CODE_BLOCK))  [scope=bar:METHOD]") }
  @Test
  fun testIfInIf(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiIfStatement>("class Foo { void bar() { if (a()) { if (b()) { c(); } } } boolean a(){return true;} boolean b(){return true;} void c(){} }", "IF_STATEMENT(IF_KEYWORD,METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST),BLOCK_STATEMENT(CODE_BLOCK))  [scope=bar:METHOD]", text = "if (b()) { c(); }") }
  @Test
  fun testCallInNestedIf(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiMethodCallExpression>("class Foo { void bar() { if (a()) { if (b()) { c(); } } } boolean a(){return true;} boolean b(){return true;} void c(){} }", "EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=bar:METHOD]", text = "c()") }
  @Test
  fun testForLoop(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiForStatement>("class Foo { void bar() { for (int i = 0; i < 10; i++) { sink(); } } void sink(){} }", "FOR_STATEMENT(FOR_KEYWORD,DECLARATION_STATEMENT(LOCAL_VARIABLE),BINARY_EXPRESSION(REFERENCE_EXPRESSION,LITERAL_EXPRESSION),EXPRESSION_STATEMENT(POSTFIX_EXPRESSION),BLOCK_STATEMENT(CODE_BLOCK))  [scope=bar:METHOD]") }
  @Test
  fun testWhileLoop(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiWhileStatement>("class Foo { void bar() { while (a()) { sink(); } } boolean a(){return true;} void sink(){} }", "WHILE_STATEMENT(WHILE_KEYWORD,METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST),BLOCK_STATEMENT(CODE_BLOCK))  [scope=bar:METHOD]") }
  @Test
  fun testForeach(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiForeachStatement>("class Foo { void bar(int[] xs) { for (int value : xs) { sink(value); } } void sink(int value){} }", "FOREACH_STATEMENT(FOR_KEYWORD,PARAMETER(TYPE,IDENTIFIER),REFERENCE_EXPRESSION(IDENTIFIER),BLOCK_STATEMENT(CODE_BLOCK))  [scope=bar:METHOD]") }
  @Test
  fun testTryCatch(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiTryStatement>("class Foo { void bar() { try { risky(); } catch (Exception e) { handle(); } } void risky(){} void handle(){} }", "TRY_STATEMENT(TRY_KEYWORD,CODE_BLOCK(EXPRESSION_STATEMENT),CATCH_SECTION(CATCH_KEYWORD,PARAMETER,CODE_BLOCK))  [scope=bar:METHOD]") }

  // switch
  @Test
  fun testSwitchStatement(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiSwitchStatement>("class Foo { void bar(int x) { switch (x) { case 1: sink(); break; case 2: other(); break; } } void sink(){} void other(){} }", "SWITCH_STATEMENT(SWITCH_KEYWORD,REFERENCE_EXPRESSION,CODE_BLOCK(SWITCH_LABEL_STATEMENT,EXPRESSION_STATEMENT,BREAK_STATEMENT,SWITCH_LABEL_STATEMENT,EXPRESSION_STATEMENT,BREAK_STATEMENT))  [scope=bar:METHOD]") }
  @Test
  fun testCaseLabel(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiSwitchLabelStatement>("class Foo { void bar(int x) { switch (x) { case 1: sink(); break; case 2: other(); break; } } void sink(){} void other(){} }", "SWITCH_LABEL_STATEMENT(CASE_KEYWORD,CASE_LABEL_ELEMENT_LIST(LITERAL_EXPRESSION))  [scope=bar:METHOD]") }
  @Test
  fun testStmtInCase(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiExpressionStatement>("class Foo { void bar(int x) { switch (x) { case 1: sink(); break; case 2: other(); break; } } void sink(){} void other(){} }", "EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=bar:METHOD]", text = "sink();") }

  // lambdas / anonymous
  @Test
  fun testLambdaExpr(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiLambdaExpression>("class Foo { Runnable r = () -> sink(); void sink(){} }", "LAMBDA_EXPRESSION(PARAMETER_LIST,ARROW,METHOD_CALL_EXPRESSION)  [scope=r:LAMBDA_EXPRESSION]") }
  @Test
  fun testLambdaBlockStmt(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiExpressionStatement>("class Foo { Runnable r = () -> { sink(); }; void sink(){} }", "EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=r:LAMBDA_EXPRESSION]", text = "sink();") }
  @Test
  fun testStmtInLambdaInsideMethod(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiExpressionStatement>("class Foo { void bar() { Runnable r = () -> { sink(); }; } void sink(){} }", "EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=r:LAMBDA_EXPRESSION]", text = "sink();") }
  @Test
  fun testExpressionLambdaWithParams(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiLambdaExpression>("class Foo { java.util.function.BinaryOperator<Integer> f = (a, b) -> a + b; }", "LAMBDA_EXPRESSION(PARAMETER_LIST,ARROW,BINARY_EXPRESSION)  [scope=f:LAMBDA_EXPRESSION]") }
  @Test
  fun testBodyOfExpressionLambdaWithParams(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiBinaryExpression>("class Foo { java.util.function.BinaryOperator<Integer> f = (a, b) -> a + b; }", "BINARY_EXPRESSION(REFERENCE_EXPRESSION,REFERENCE_EXPRESSION)  [scope=f:LAMBDA_EXPRESSION]", text = "a + b") }
  @Test
  fun testExpressionLambdaInsideMethod(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiLambdaExpression>("class Foo { void bar() { Runnable r = () -> sink(); } void sink(){} }", "LAMBDA_EXPRESSION(PARAMETER_LIST,ARROW,METHOD_CALL_EXPRESSION)  [scope=r:LAMBDA_EXPRESSION]") }
  /** A type parameter is no scope, so the bound reports the class. Otherwise it would report `T`. */
  @Test
  fun testClassTypeParameterReportsClass(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiJavaCodeReferenceElement>("class Foo<T extends Number> { }", "TYPE_PARAMETER(EXTENDS_BOUND_LIST)  [scope=Foo:CLASS]", text = "Number") }
  /** The same for a method type parameter. The method is the nearest scope. */
  @Test
  fun testMethodTypeParameterReportsMethod(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiJavaCodeReferenceElement>("class Foo { <T extends Number> void bar(T t) {} }", "TYPE_PARAMETER(EXTENDS_BOUND_LIST)  [scope=bar:METHOD]", text = "Number") }
  @Test
  fun testAnonymousClass(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiAnonymousClass>("class Foo { Object r = new Object() { @Override public String toString() { sink(); return \"\"; } }; void sink(){} }", "ANONYMOUS_CLASS(JAVA_CODE_REFERENCE,EXPRESSION_LIST,METHOD)  [scope=r:ANONYMOUS_CLASS]") }
  // a lambda is a scope, so the astShape walk now stops under it
  @Test
  fun testExprInExpressionLambda(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiMethodCallExpression>("class Foo { Runnable r = () -> sink(); void sink(){} }", "METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION(IDENTIFIER),EXPRESSION_LIST)  [scope=r:LAMBDA_EXPRESSION]", text = "sink()") }
  @Test
  fun testLambdaArgumentReportsEnclosingMethod(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiMethodCallExpression>("class Foo { void bar(java.util.List<String> xs) { xs.forEach(x -> sink()); } void sink(){} }", "METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION(IDENTIFIER),EXPRESSION_LIST)  [scope=bar:METHOD]", text = "sink()") }
  @Test
  fun testLambdaArgumentExprReportsEnclosingMethod(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiLambdaExpression>("class Foo { void bar(java.util.List<String> xs) { xs.forEach(x -> sink()); } void sink(){} }", "LAMBDA_EXPRESSION(PARAMETER_LIST,ARROW,METHOD_CALL_EXPRESSION)  [scope=bar:METHOD]") }
  @Test
  fun testStmtInAnonymous(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiExpressionStatement>("class Foo { Object r = new Object() { @Override public String toString() { sink(); return \"\"; } }; void sink(){} }", "EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=toString:METHOD]", text = "sink();") }

  // array / new / ternary
  @Test
  fun testArrayInitializer(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiArrayInitializerExpression>("class Foo { int[] a = {1, 2, 3}; }", "FIELD(TYPE(TYPE),ARRAY_INITIALIZER_EXPRESSION(LITERAL_EXPRESSION,LITERAL_EXPRESSION,LITERAL_EXPRESSION))  [scope=Foo:CLASS]") }
  @Test
  fun testArrayElement(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiLiteralExpression>("class Foo { int[] a = {1, 2, 3}; }", "LITERAL_EXPRESSION  [scope=Foo:CLASS]", text = "2") }
  @Test
  fun testNewExpression(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiNewExpression>("class Foo { Object o = new Object(); }", "FIELD(TYPE(JAVA_CODE_REFERENCE),NEW_EXPRESSION(NEW_KEYWORD,JAVA_CODE_REFERENCE,EXPRESSION_LIST))  [scope=Foo:CLASS]") }
  @Test
  fun testTernary(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiConditionalExpression>("class Foo { int f = c() ? 1 : 2; boolean c(){return true;} }", "FIELD(TYPE(INT_KEYWORD),CONDITIONAL_EXPRESSION(METHOD_CALL_EXPRESSION,LITERAL_EXPRESSION,LITERAL_EXPRESSION))  [scope=Foo:CLASS]") }

  // more statements
  @Test
  fun testDoWhile(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiDoWhileStatement>("class Foo { void bar() { do { sink(); } while (a()); } boolean a(){return true;} void sink(){} }", "DO_WHILE_STATEMENT(DO_KEYWORD,BLOCK_STATEMENT(CODE_BLOCK),WHILE_KEYWORD,METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=bar:METHOD]") }
  @Test
  fun testThrowStatement(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiThrowStatement>("class Foo { void bar() { throw new RuntimeException(); } }", "THROW_STATEMENT(THROW_KEYWORD,NEW_EXPRESSION(NEW_KEYWORD,JAVA_CODE_REFERENCE,EXPRESSION_LIST))  [scope=bar:METHOD]") }
  @Test
  fun testAssertStatement(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiAssertStatement>("class Foo { void bar() { assert a(); } boolean a(){return true;} }", "ASSERT_STATEMENT(ASSERT_KEYWORD,METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=bar:METHOD]") }
  @Test
  fun testSynchronized(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiSynchronizedStatement>("class Foo { void bar() { synchronized (this) { sink(); } } void sink(){} }", "SYNCHRONIZED_STATEMENT(SYNCHRONIZED_KEYWORD,THIS_EXPRESSION(THIS_KEYWORD),CODE_BLOCK(EXPRESSION_STATEMENT))  [scope=bar:METHOD]") }
  @Test
  fun testLabeled(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiLabeledStatement>("class Foo { void bar() { loop: while (cond()) { if (done()) break loop; } } boolean cond(){ return true; } boolean done(){ return true; } }", "LABELED_STATEMENT(IDENTIFIER,WHILE_STATEMENT(WHILE_KEYWORD,METHOD_CALL_EXPRESSION,BLOCK_STATEMENT))  [scope=bar:METHOD]") }
  @Test
  fun testBreakStatement(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiBreakStatement>("class Foo { void bar() {\n//noinspection ALL\nfor (;;) { break; } } }", "BREAK_STATEMENT(BREAK_KEYWORD)  [scope=bar:METHOD]") }
  @Test
  fun testContinueStatement(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiContinueStatement>("class Foo { void bar() { for (;;) {\n//noinspection ALL\ncontinue; } } }", "CONTINUE_STATEMENT(CONTINUE_KEYWORD)  [scope=bar:METHOD]") }
  @Test
  fun testTryWithResources(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiTryStatement>("class Foo { void bar() throws Exception { try (java.io.OutputStream s = open()) { s.flush(); } } java.io.OutputStream open(){ return null; } }", "TRY_STATEMENT(TRY_KEYWORD,RESOURCE_LIST(RESOURCE_VARIABLE),CODE_BLOCK(EXPRESSION_STATEMENT))  [scope=bar:METHOD]") }
  @Test
  fun testMultiCatch(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiTryStatement>("class Foo { void bar() { try { risky(); } catch (java.io.IOException | RuntimeException e) { handle(); } } void risky() throws java.io.IOException { throw new java.io.IOException(); } void handle(){} }", "TRY_STATEMENT(TRY_KEYWORD,CODE_BLOCK(EXPRESSION_STATEMENT),CATCH_SECTION(CATCH_KEYWORD,PARAMETER,CODE_BLOCK))  [scope=bar:METHOD]") }

  // more expressions
  @Test
  fun testBinaryExpr(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiBinaryExpression>("class Foo { boolean f() { return a() && b(); } boolean a(){return true;} boolean b(){return true;} }", "RETURN_STATEMENT(RETURN_KEYWORD,BINARY_EXPRESSION(METHOD_CALL_EXPRESSION,ANDAND,METHOD_CALL_EXPRESSION))  [scope=f:METHOD]", text = "a() && b()") }
  @Test
  fun testPolyadicExpr(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiPolyadicExpression>("class Foo { int f() { return 1 + 2 + 3; } }", "RETURN_STATEMENT(RETURN_KEYWORD,POLYADIC_EXPRESSION(LITERAL_EXPRESSION,LITERAL_EXPRESSION,LITERAL_EXPRESSION))  [scope=f:METHOD]") }
  @Test
  fun testPrefixExpr(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiPrefixExpression>("class Foo { boolean f(boolean x) { return !x; } }", "RETURN_STATEMENT(RETURN_KEYWORD,PREFIX_EXPRESSION(REFERENCE_EXPRESSION))  [scope=f:METHOD]") }
  @Test
  fun testPostfixExpr(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiPostfixExpression>("class Foo { int bar(int i) { i++; return i; } }", "EXPRESSION_STATEMENT(POSTFIX_EXPRESSION(REFERENCE_EXPRESSION,PLUSPLUS))  [scope=bar:METHOD]") }
  @Test
  fun testCastExpr(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiTypeCastExpression>("class Foo { int f(Object o) { return (int) o; } }", "RETURN_STATEMENT(RETURN_KEYWORD,TYPE_CAST_EXPRESSION(TYPE,REFERENCE_EXPRESSION))  [scope=f:METHOD]") }
  @Test
  fun testInstanceofExpr(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiInstanceOfExpression>("class Foo { boolean f(Object o) { return o instanceof String; } }", "RETURN_STATEMENT(RETURN_KEYWORD,INSTANCE_OF_EXPRESSION(REFERENCE_EXPRESSION,INSTANCEOF_KEYWORD,TYPE))  [scope=f:METHOD]") }
  @Test
  fun testMethodRef(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiMethodReferenceExpression>("class Foo { Runnable r = Foo::sink; static void sink(){} }", "FIELD(TYPE(JAVA_CODE_REFERENCE),METHOD_REF_EXPRESSION(REFERENCE_EXPRESSION,DOUBLE_COLON,IDENTIFIER))  [scope=Foo:CLASS]") }
  @Test
  fun testArrayAccess(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiArrayAccessExpression>("class Foo { int f(int[] a) { return a[0]; } }", "RETURN_STATEMENT(RETURN_KEYWORD,ARRAY_ACCESS_EXPRESSION(REFERENCE_EXPRESSION,LITERAL_EXPRESSION))  [scope=f:METHOD]") }
  @Test
  fun testQualifiedRef(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiReferenceExpression>("class Foo { int x; int f() { return this.x; } }", "RETURN_STATEMENT(RETURN_KEYWORD,REFERENCE_EXPRESSION(THIS_EXPRESSION))  [scope=f:METHOD]", text = "this.x") }
  @Test
  fun testNestedCall(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiMethodCallExpression>("class Foo { void bar() { a().b(); } Foo a(){ return this; } void b(){} }", "EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=bar:METHOD]", text = "a().b()") }
  @Test
  fun testClassObjAccess(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiClassObjectAccessExpression>("class Foo { Class<?> c = Foo.class; }", "FIELD(TYPE(JAVA_CODE_REFERENCE),CLASS_OBJECT_ACCESS_EXPRESSION(TYPE,CLASS_KEYWORD))  [scope=Foo:CLASS]") }

  // more nesting
  @Test
  fun testLocalClass(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiClass>("class Foo { void bar() { class Local { void m() {} } } }", "CLASS(CLASS_KEYWORD,IDENTIFIER,METHOD)  [scope=Local:CLASS]", text = "class Local { void m() {} }") }
  @Test
  fun testNestedLambda(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiLambdaExpression>("class Foo { java.util.function.Supplier<Runnable> s = () -> () -> sink(); void sink(){} }", "LAMBDA_EXPRESSION(PARAMETER_LIST,ARROW,METHOD_CALL_EXPRESSION)  [scope=s:LAMBDA_EXPRESSION]", text = "() -> sink()") }
  @Test
  fun testCallArgIsCall(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertSig<PsiMethodCallExpression>("class Foo { void bar() { outer(inner()); } void outer(int x){} int inner(){ return 1; } }", "METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST)  [scope=bar:METHOD]", text = "inner()") }

  // Whole-tree characterization
  // Each test walks ONE top-level member's subtree of [SAMPLE] and asserts the astShape + [scope] that
  // PsiSignalExtractor would produce if a problem were reported at each node. Together they cover every node of
  // the sample (partitioned by member), but a failure names one member and runs in isolation. On an intentional
  // logic change, regenerate the affected golden from that test's assertion diff.

  @Test
  fun testTreeClassHeader(): Unit = timeoutRunBlocking(timeout = 2.minutes) {
    val sample = sampleClass()
    assertEquals(
      "CLASS[Sample] => CLASS(CLASS_KEYWORD,IDENTIFIER,FIELD,FIELD,FIELD,CLASS_INITIALIZER,METHOD,METHOD,METHOD,METHOD,METHOD,METHOD,METHOD,METHOD)  [scope=Sample:CLASS]",
      readAction { format(sample, 0) },
    )
  }

  @Test
  fun testTreeFieldWithInitializerCall(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertEquals(
    """
      FIELD[field] => FIELD(TYPE(INT_KEYWORD),IDENTIFIER,METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=Sample:CLASS]
        TYPE => FIELD(TYPE(INT_KEYWORD),IDENTIFIER,METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=Sample:CLASS]
        METHOD_CALL_EXPRESSION => FIELD(TYPE(INT_KEYWORD),IDENTIFIER,METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=Sample:CLASS]
          REFERENCE_EXPRESSION => FIELD(TYPE,IDENTIFIER,METHOD_CALL_EXPRESSION)  [scope=Sample:CLASS]
          EXPRESSION_LIST => FIELD(TYPE,IDENTIFIER,METHOD_CALL_EXPRESSION)  [scope=Sample:CLASS]
            LITERAL_EXPRESSION => METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST)  [scope=Sample:CLASS]
            LITERAL_EXPRESSION => METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST)  [scope=Sample:CLASS]
    """.trimIndent(),
    characterize(member("field")),
  ) }

  @Test
  fun testTreeUninitializedField(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertEquals(
    """
      FIELD[other] => FIELD(TYPE(INT_KEYWORD),IDENTIFIER)  [scope=Sample:CLASS]
        TYPE => FIELD(TYPE(INT_KEYWORD),IDENTIFIER)  [scope=Sample:CLASS]
    """.trimIndent(),
    characterize(member("other")),
  ) }

  @Test
  fun testTreeArrayField(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertEquals(
    """
      FIELD[data] => FIELD(TYPE(TYPE),IDENTIFIER,ARRAY_INITIALIZER_EXPRESSION(LITERAL_EXPRESSION,LITERAL_EXPRESSION,LITERAL_EXPRESSION))  [scope=Sample:CLASS]
        TYPE => FIELD(TYPE(TYPE),IDENTIFIER,ARRAY_INITIALIZER_EXPRESSION(LITERAL_EXPRESSION,LITERAL_EXPRESSION,LITERAL_EXPRESSION))  [scope=Sample:CLASS]
          TYPE => FIELD(TYPE,IDENTIFIER,ARRAY_INITIALIZER_EXPRESSION)  [scope=Sample:CLASS]
        ARRAY_INITIALIZER_EXPRESSION => FIELD(TYPE(TYPE),IDENTIFIER,ARRAY_INITIALIZER_EXPRESSION(LITERAL_EXPRESSION,LITERAL_EXPRESSION,LITERAL_EXPRESSION))  [scope=Sample:CLASS]
          LITERAL_EXPRESSION => LITERAL_EXPRESSION  [scope=Sample:CLASS]
          LITERAL_EXPRESSION => LITERAL_EXPRESSION  [scope=Sample:CLASS]
          LITERAL_EXPRESSION => LITERAL_EXPRESSION  [scope=Sample:CLASS]
    """.trimIndent(),
    characterize(member("data")),
  ) }

  @Test
  fun testTreeStaticInitializer(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertEquals(
    """
      CLASS_INITIALIZER => CLASS_INITIALIZER(MODIFIER_LIST,CODE_BLOCK)  [scope=Sample:CLASS_INITIALIZER]
        MODIFIER_LIST => MODIFIER_LIST(STATIC_KEYWORD)  [scope=Sample:CLASS_INITIALIZER]
        CODE_BLOCK => CODE_BLOCK(EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION))  [scope=Sample:CLASS_INITIALIZER]
          EXPRESSION_STATEMENT => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=Sample:CLASS_INITIALIZER]
            METHOD_CALL_EXPRESSION => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=Sample:CLASS_INITIALIZER]
              REFERENCE_EXPRESSION => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION)  [scope=Sample:CLASS_INITIALIZER]
              EXPRESSION_LIST => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION)  [scope=Sample:CLASS_INITIALIZER]
    """.trimIndent(),
    characterize(staticInitializer()),
  ) }

  @Test
  fun testTreeConstructor(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertEquals(
    """
      METHOD[Sample] => METHOD(IDENTIFIER,PARAMETER_LIST,CODE_BLOCK)  [scope=Sample:METHOD]
        PARAMETER_LIST => PARAMETER_LIST  [scope=Sample:METHOD]
        CODE_BLOCK => CODE_BLOCK(EXPRESSION_STATEMENT(ASSIGNMENT_EXPRESSION))  [scope=Sample:METHOD]
          EXPRESSION_STATEMENT => EXPRESSION_STATEMENT(ASSIGNMENT_EXPRESSION(REFERENCE_EXPRESSION,REFERENCE_EXPRESSION))  [scope=Sample:METHOD]
            ASSIGNMENT_EXPRESSION => EXPRESSION_STATEMENT(ASSIGNMENT_EXPRESSION(REFERENCE_EXPRESSION,REFERENCE_EXPRESSION))  [scope=Sample:METHOD]
              REFERENCE_EXPRESSION => EXPRESSION_STATEMENT(ASSIGNMENT_EXPRESSION)  [scope=Sample:METHOD]
                THIS_EXPRESSION => ASSIGNMENT_EXPRESSION(REFERENCE_EXPRESSION,REFERENCE_EXPRESSION)  [scope=Sample:METHOD]
              REFERENCE_EXPRESSION => EXPRESSION_STATEMENT(ASSIGNMENT_EXPRESSION)  [scope=Sample:METHOD]
    """.trimIndent(),
    characterize(member("Sample")),
  ) }

  @Test
  fun testTreeEmptyStaticMethod(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertEquals(
    """
      METHOD[init] => METHOD(MODIFIER_LIST,TYPE,IDENTIFIER,PARAMETER_LIST,CODE_BLOCK)  [scope=init:METHOD]
        MODIFIER_LIST => MODIFIER_LIST(STATIC_KEYWORD)  [scope=init:METHOD]
        TYPE => TYPE(VOID_KEYWORD)  [scope=init:METHOD]
        PARAMETER_LIST => PARAMETER_LIST  [scope=init:METHOD]
        CODE_BLOCK => CODE_BLOCK  [scope=init:METHOD]
    """.trimIndent(),
    characterize(member("init")),
  ) }

  @Test
  fun testTreeIfElseNestedLoops(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertEquals(
    """
      METHOD[compute] => METHOD(TYPE,IDENTIFIER,PARAMETER_LIST,CODE_BLOCK)  [scope=compute:METHOD]
        TYPE => TYPE(INT_KEYWORD)  [scope=compute:METHOD]
        PARAMETER_LIST => PARAMETER_LIST(PARAMETER(TYPE),PARAMETER(TYPE))  [scope=compute:METHOD]
          PARAMETER[a] => PARAMETER_LIST(PARAMETER(TYPE),PARAMETER(TYPE))  [scope=compute:METHOD]
            TYPE => PARAMETER_LIST(PARAMETER,PARAMETER)  [scope=compute:METHOD]
          PARAMETER[b] => PARAMETER_LIST(PARAMETER(TYPE),PARAMETER(TYPE))  [scope=compute:METHOD]
            TYPE => PARAMETER_LIST(PARAMETER,PARAMETER)  [scope=compute:METHOD]
        CODE_BLOCK => CODE_BLOCK(DECLARATION_STATEMENT(LOCAL_VARIABLE),IF_STATEMENT(IF_KEYWORD,BINARY_EXPRESSION,BLOCK_STATEMENT,ELSE_KEYWORD,BLOCK_STATEMENT),RETURN_STATEMENT(RETURN_KEYWORD,REFERENCE_EXPRESSION))  [scope=compute:METHOD]
          DECLARATION_STATEMENT => DECLARATION_STATEMENT(LOCAL_VARIABLE(TYPE,IDENTIFIER,BINARY_EXPRESSION))  [scope=compute:METHOD]
            LOCAL_VARIABLE[sum] => DECLARATION_STATEMENT(LOCAL_VARIABLE(TYPE,IDENTIFIER,BINARY_EXPRESSION))  [scope=compute:METHOD]
              TYPE => DECLARATION_STATEMENT(LOCAL_VARIABLE)  [scope=compute:METHOD]
              BINARY_EXPRESSION => DECLARATION_STATEMENT(LOCAL_VARIABLE)  [scope=compute:METHOD]
                REFERENCE_EXPRESSION => LOCAL_VARIABLE(TYPE,IDENTIFIER,BINARY_EXPRESSION)  [scope=compute:METHOD]
                REFERENCE_EXPRESSION => LOCAL_VARIABLE(TYPE,IDENTIFIER,BINARY_EXPRESSION)  [scope=compute:METHOD]
          IF_STATEMENT => IF_STATEMENT(IF_KEYWORD,BINARY_EXPRESSION(REFERENCE_EXPRESSION,LITERAL_EXPRESSION),BLOCK_STATEMENT(CODE_BLOCK),ELSE_KEYWORD,BLOCK_STATEMENT(CODE_BLOCK))  [scope=compute:METHOD]
            BINARY_EXPRESSION => IF_STATEMENT(IF_KEYWORD,BINARY_EXPRESSION(REFERENCE_EXPRESSION,LITERAL_EXPRESSION),BLOCK_STATEMENT(CODE_BLOCK),ELSE_KEYWORD,BLOCK_STATEMENT(CODE_BLOCK))  [scope=compute:METHOD]
              REFERENCE_EXPRESSION => IF_STATEMENT(IF_KEYWORD,BINARY_EXPRESSION,BLOCK_STATEMENT,ELSE_KEYWORD,BLOCK_STATEMENT)  [scope=compute:METHOD]
              LITERAL_EXPRESSION => IF_STATEMENT(IF_KEYWORD,BINARY_EXPRESSION,BLOCK_STATEMENT,ELSE_KEYWORD,BLOCK_STATEMENT)  [scope=compute:METHOD]
            BLOCK_STATEMENT => IF_STATEMENT(IF_KEYWORD,BINARY_EXPRESSION(REFERENCE_EXPRESSION,LITERAL_EXPRESSION),BLOCK_STATEMENT(CODE_BLOCK),ELSE_KEYWORD,BLOCK_STATEMENT(CODE_BLOCK))  [scope=compute:METHOD]
              CODE_BLOCK => IF_STATEMENT(IF_KEYWORD,BINARY_EXPRESSION,BLOCK_STATEMENT,ELSE_KEYWORD,BLOCK_STATEMENT)  [scope=compute:METHOD]
                WHILE_STATEMENT => WHILE_STATEMENT(WHILE_KEYWORD,BINARY_EXPRESSION(REFERENCE_EXPRESSION,LITERAL_EXPRESSION),BLOCK_STATEMENT(CODE_BLOCK))  [scope=compute:METHOD]
                  BINARY_EXPRESSION => WHILE_STATEMENT(WHILE_KEYWORD,BINARY_EXPRESSION(REFERENCE_EXPRESSION,LITERAL_EXPRESSION),BLOCK_STATEMENT(CODE_BLOCK))  [scope=compute:METHOD]
                    REFERENCE_EXPRESSION => WHILE_STATEMENT(WHILE_KEYWORD,BINARY_EXPRESSION,BLOCK_STATEMENT)  [scope=compute:METHOD]
                    LITERAL_EXPRESSION => WHILE_STATEMENT(WHILE_KEYWORD,BINARY_EXPRESSION,BLOCK_STATEMENT)  [scope=compute:METHOD]
                  BLOCK_STATEMENT => WHILE_STATEMENT(WHILE_KEYWORD,BINARY_EXPRESSION(REFERENCE_EXPRESSION,LITERAL_EXPRESSION),BLOCK_STATEMENT(CODE_BLOCK))  [scope=compute:METHOD]
                    CODE_BLOCK => WHILE_STATEMENT(WHILE_KEYWORD,BINARY_EXPRESSION,BLOCK_STATEMENT)  [scope=compute:METHOD]
                      EXPRESSION_STATEMENT => EXPRESSION_STATEMENT(POSTFIX_EXPRESSION(REFERENCE_EXPRESSION,MINUSMINUS))  [scope=compute:METHOD]
                        POSTFIX_EXPRESSION => EXPRESSION_STATEMENT(POSTFIX_EXPRESSION(REFERENCE_EXPRESSION,MINUSMINUS))  [scope=compute:METHOD]
                          REFERENCE_EXPRESSION => EXPRESSION_STATEMENT(POSTFIX_EXPRESSION)  [scope=compute:METHOD]
            BLOCK_STATEMENT => IF_STATEMENT(IF_KEYWORD,BINARY_EXPRESSION(REFERENCE_EXPRESSION,LITERAL_EXPRESSION),BLOCK_STATEMENT(CODE_BLOCK),ELSE_KEYWORD,BLOCK_STATEMENT(CODE_BLOCK))  [scope=compute:METHOD]
              CODE_BLOCK => IF_STATEMENT(IF_KEYWORD,BINARY_EXPRESSION,BLOCK_STATEMENT,ELSE_KEYWORD,BLOCK_STATEMENT)  [scope=compute:METHOD]
                FOR_STATEMENT => FOR_STATEMENT(FOR_KEYWORD,DECLARATION_STATEMENT(LOCAL_VARIABLE),BINARY_EXPRESSION(REFERENCE_EXPRESSION,REFERENCE_EXPRESSION),EXPRESSION_STATEMENT(POSTFIX_EXPRESSION),BLOCK_STATEMENT(CODE_BLOCK))  [scope=compute:METHOD]
                  DECLARATION_STATEMENT => FOR_STATEMENT(FOR_KEYWORD,DECLARATION_STATEMENT(LOCAL_VARIABLE),BINARY_EXPRESSION(REFERENCE_EXPRESSION,REFERENCE_EXPRESSION),EXPRESSION_STATEMENT(POSTFIX_EXPRESSION),BLOCK_STATEMENT(CODE_BLOCK))  [scope=compute:METHOD]
                    LOCAL_VARIABLE[i] => FOR_STATEMENT(FOR_KEYWORD,DECLARATION_STATEMENT,BINARY_EXPRESSION,EXPRESSION_STATEMENT,BLOCK_STATEMENT)  [scope=compute:METHOD]
                      TYPE => DECLARATION_STATEMENT(LOCAL_VARIABLE)  [scope=compute:METHOD]
                      LITERAL_EXPRESSION => DECLARATION_STATEMENT(LOCAL_VARIABLE)  [scope=compute:METHOD]
                  BINARY_EXPRESSION => FOR_STATEMENT(FOR_KEYWORD,DECLARATION_STATEMENT(LOCAL_VARIABLE),BINARY_EXPRESSION(REFERENCE_EXPRESSION,REFERENCE_EXPRESSION),EXPRESSION_STATEMENT(POSTFIX_EXPRESSION),BLOCK_STATEMENT(CODE_BLOCK))  [scope=compute:METHOD]
                    REFERENCE_EXPRESSION => FOR_STATEMENT(FOR_KEYWORD,DECLARATION_STATEMENT,BINARY_EXPRESSION,EXPRESSION_STATEMENT,BLOCK_STATEMENT)  [scope=compute:METHOD]
                    REFERENCE_EXPRESSION => FOR_STATEMENT(FOR_KEYWORD,DECLARATION_STATEMENT,BINARY_EXPRESSION,EXPRESSION_STATEMENT,BLOCK_STATEMENT)  [scope=compute:METHOD]
                  EXPRESSION_STATEMENT => FOR_STATEMENT(FOR_KEYWORD,DECLARATION_STATEMENT(LOCAL_VARIABLE),BINARY_EXPRESSION(REFERENCE_EXPRESSION,REFERENCE_EXPRESSION),EXPRESSION_STATEMENT(POSTFIX_EXPRESSION),BLOCK_STATEMENT(CODE_BLOCK))  [scope=compute:METHOD]
                    POSTFIX_EXPRESSION => FOR_STATEMENT(FOR_KEYWORD,DECLARATION_STATEMENT,BINARY_EXPRESSION,EXPRESSION_STATEMENT,BLOCK_STATEMENT)  [scope=compute:METHOD]
                      REFERENCE_EXPRESSION => EXPRESSION_STATEMENT(POSTFIX_EXPRESSION)  [scope=compute:METHOD]
                  BLOCK_STATEMENT => FOR_STATEMENT(FOR_KEYWORD,DECLARATION_STATEMENT(LOCAL_VARIABLE),BINARY_EXPRESSION(REFERENCE_EXPRESSION,REFERENCE_EXPRESSION),EXPRESSION_STATEMENT(POSTFIX_EXPRESSION),BLOCK_STATEMENT(CODE_BLOCK))  [scope=compute:METHOD]
                    CODE_BLOCK => FOR_STATEMENT(FOR_KEYWORD,DECLARATION_STATEMENT,BINARY_EXPRESSION,EXPRESSION_STATEMENT,BLOCK_STATEMENT)  [scope=compute:METHOD]
                      EXPRESSION_STATEMENT => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=compute:METHOD]
                        METHOD_CALL_EXPRESSION => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=compute:METHOD]
                          REFERENCE_EXPRESSION => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION)  [scope=compute:METHOD]
                          EXPRESSION_LIST => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION)  [scope=compute:METHOD]
                            REFERENCE_EXPRESSION => METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST)  [scope=compute:METHOD]
          RETURN_STATEMENT => RETURN_STATEMENT(RETURN_KEYWORD,REFERENCE_EXPRESSION(IDENTIFIER))  [scope=compute:METHOD]
            REFERENCE_EXPRESSION => RETURN_STATEMENT(RETURN_KEYWORD,REFERENCE_EXPRESSION(IDENTIFIER))  [scope=compute:METHOD]
    """.trimIndent(),
    characterize(member("compute")),
  ) }

  @Test
  fun testTreeControlFlow(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertEquals(
    """
      METHOD[controlFlow] => METHOD(TYPE,IDENTIFIER,PARAMETER_LIST,CODE_BLOCK)  [scope=controlFlow:METHOD]
        TYPE => TYPE(VOID_KEYWORD)  [scope=controlFlow:METHOD]
        PARAMETER_LIST => PARAMETER_LIST(PARAMETER(TYPE),PARAMETER(TYPE,IDENTIFIER))  [scope=controlFlow:METHOD]
          PARAMETER[x] => PARAMETER_LIST(PARAMETER(TYPE),PARAMETER(TYPE,IDENTIFIER))  [scope=controlFlow:METHOD]
            TYPE => PARAMETER_LIST(PARAMETER,PARAMETER)  [scope=controlFlow:METHOD]
          PARAMETER[xs] => PARAMETER_LIST(PARAMETER(TYPE),PARAMETER(TYPE,IDENTIFIER))  [scope=controlFlow:METHOD]
            TYPE => PARAMETER_LIST(PARAMETER,PARAMETER)  [scope=controlFlow:METHOD]
              TYPE => PARAMETER(TYPE,IDENTIFIER)  [scope=controlFlow:METHOD]
        CODE_BLOCK => CODE_BLOCK(SWITCH_STATEMENT(SWITCH_KEYWORD,REFERENCE_EXPRESSION,CODE_BLOCK),FOREACH_STATEMENT(FOR_KEYWORD,PARAMETER,REFERENCE_EXPRESSION,BLOCK_STATEMENT),TRY_STATEMENT(TRY_KEYWORD,CODE_BLOCK,CATCH_SECTION,FINALLY_KEYWORD,CODE_BLOCK))  [scope=controlFlow:METHOD]
          SWITCH_STATEMENT => SWITCH_STATEMENT(SWITCH_KEYWORD,REFERENCE_EXPRESSION,CODE_BLOCK(SWITCH_LABELED_RULE,SWITCH_LABELED_RULE,SWITCH_LABELED_RULE))  [scope=controlFlow:METHOD]
            REFERENCE_EXPRESSION => SWITCH_STATEMENT(SWITCH_KEYWORD,REFERENCE_EXPRESSION,CODE_BLOCK(SWITCH_LABELED_RULE,SWITCH_LABELED_RULE,SWITCH_LABELED_RULE))  [scope=controlFlow:METHOD]
            CODE_BLOCK => SWITCH_STATEMENT(SWITCH_KEYWORD,REFERENCE_EXPRESSION,CODE_BLOCK(SWITCH_LABELED_RULE,SWITCH_LABELED_RULE,SWITCH_LABELED_RULE))  [scope=controlFlow:METHOD]
              SWITCH_LABELED_RULE => SWITCH_LABELED_RULE(CASE_KEYWORD,CASE_LABEL_ELEMENT_LIST(LITERAL_EXPRESSION),ARROW,EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION))  [scope=controlFlow:METHOD]
                CASE_LABEL_ELEMENT_LIST => CASE_LABEL_ELEMENT_LIST(LITERAL_EXPRESSION)  [scope=controlFlow:METHOD]
                  LITERAL_EXPRESSION => CASE_LABEL_ELEMENT_LIST(LITERAL_EXPRESSION)  [scope=controlFlow:METHOD]
                EXPRESSION_STATEMENT => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=controlFlow:METHOD]
                  METHOD_CALL_EXPRESSION => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=controlFlow:METHOD]
                    REFERENCE_EXPRESSION => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION)  [scope=controlFlow:METHOD]
                    EXPRESSION_LIST => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION)  [scope=controlFlow:METHOD]
                      REFERENCE_EXPRESSION => METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST)  [scope=controlFlow:METHOD]
              SWITCH_LABELED_RULE => SWITCH_LABELED_RULE(CASE_KEYWORD,CASE_LABEL_ELEMENT_LIST(LITERAL_EXPRESSION),ARROW,EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION))  [scope=controlFlow:METHOD]
                CASE_LABEL_ELEMENT_LIST => CASE_LABEL_ELEMENT_LIST(LITERAL_EXPRESSION)  [scope=controlFlow:METHOD]
                  LITERAL_EXPRESSION => CASE_LABEL_ELEMENT_LIST(LITERAL_EXPRESSION)  [scope=controlFlow:METHOD]
                EXPRESSION_STATEMENT => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=controlFlow:METHOD]
                  METHOD_CALL_EXPRESSION => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=controlFlow:METHOD]
                    REFERENCE_EXPRESSION => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION)  [scope=controlFlow:METHOD]
                    EXPRESSION_LIST => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION)  [scope=controlFlow:METHOD]
                      LITERAL_EXPRESSION => METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST)  [scope=controlFlow:METHOD]
              SWITCH_LABELED_RULE => SWITCH_LABELED_RULE(DEFAULT_KEYWORD,ARROW,BLOCK_STATEMENT(CODE_BLOCK))  [scope=controlFlow:METHOD]
                BLOCK_STATEMENT => BLOCK_STATEMENT(CODE_BLOCK(EXPRESSION_STATEMENT))  [scope=controlFlow:METHOD]
                  CODE_BLOCK => BLOCK_STATEMENT(CODE_BLOCK(EXPRESSION_STATEMENT))  [scope=controlFlow:METHOD]
                    EXPRESSION_STATEMENT => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=controlFlow:METHOD]
                      METHOD_CALL_EXPRESSION => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=controlFlow:METHOD]
                        REFERENCE_EXPRESSION => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION)  [scope=controlFlow:METHOD]
                        EXPRESSION_LIST => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION)  [scope=controlFlow:METHOD]
                          LITERAL_EXPRESSION => METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST)  [scope=controlFlow:METHOD]
          FOREACH_STATEMENT => FOREACH_STATEMENT(FOR_KEYWORD,PARAMETER(TYPE),REFERENCE_EXPRESSION(IDENTIFIER),BLOCK_STATEMENT(CODE_BLOCK))  [scope=controlFlow:METHOD]
            PARAMETER[v] => FOREACH_STATEMENT(FOR_KEYWORD,PARAMETER(TYPE),REFERENCE_EXPRESSION(IDENTIFIER),BLOCK_STATEMENT(CODE_BLOCK))  [scope=controlFlow:METHOD]
              TYPE => FOREACH_STATEMENT(FOR_KEYWORD,PARAMETER,REFERENCE_EXPRESSION,BLOCK_STATEMENT)  [scope=controlFlow:METHOD]
            REFERENCE_EXPRESSION => FOREACH_STATEMENT(FOR_KEYWORD,PARAMETER(TYPE),REFERENCE_EXPRESSION(IDENTIFIER),BLOCK_STATEMENT(CODE_BLOCK))  [scope=controlFlow:METHOD]
            BLOCK_STATEMENT => FOREACH_STATEMENT(FOR_KEYWORD,PARAMETER(TYPE),REFERENCE_EXPRESSION(IDENTIFIER),BLOCK_STATEMENT(CODE_BLOCK))  [scope=controlFlow:METHOD]
              CODE_BLOCK => FOREACH_STATEMENT(FOR_KEYWORD,PARAMETER,REFERENCE_EXPRESSION,BLOCK_STATEMENT)  [scope=controlFlow:METHOD]
                EXPRESSION_STATEMENT => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=controlFlow:METHOD]
                  METHOD_CALL_EXPRESSION => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=controlFlow:METHOD]
                    REFERENCE_EXPRESSION => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION)  [scope=controlFlow:METHOD]
                    EXPRESSION_LIST => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION)  [scope=controlFlow:METHOD]
                      REFERENCE_EXPRESSION => METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST)  [scope=controlFlow:METHOD]
          TRY_STATEMENT => TRY_STATEMENT(TRY_KEYWORD,CODE_BLOCK(EXPRESSION_STATEMENT),CATCH_SECTION(CATCH_KEYWORD,PARAMETER,CODE_BLOCK),FINALLY_KEYWORD,CODE_BLOCK(EXPRESSION_STATEMENT))  [scope=controlFlow:METHOD]
            CODE_BLOCK => TRY_STATEMENT(TRY_KEYWORD,CODE_BLOCK(EXPRESSION_STATEMENT),CATCH_SECTION(CATCH_KEYWORD,PARAMETER,CODE_BLOCK),FINALLY_KEYWORD,CODE_BLOCK(EXPRESSION_STATEMENT))  [scope=controlFlow:METHOD]
              EXPRESSION_STATEMENT => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=controlFlow:METHOD]
                METHOD_CALL_EXPRESSION => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=controlFlow:METHOD]
                  REFERENCE_EXPRESSION => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION)  [scope=controlFlow:METHOD]
                  EXPRESSION_LIST => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION)  [scope=controlFlow:METHOD]
            CATCH_SECTION => TRY_STATEMENT(TRY_KEYWORD,CODE_BLOCK(EXPRESSION_STATEMENT),CATCH_SECTION(CATCH_KEYWORD,PARAMETER,CODE_BLOCK),FINALLY_KEYWORD,CODE_BLOCK(EXPRESSION_STATEMENT))  [scope=controlFlow:METHOD]
              PARAMETER[e] => TRY_STATEMENT(TRY_KEYWORD,CODE_BLOCK,CATCH_SECTION,FINALLY_KEYWORD,CODE_BLOCK)  [scope=controlFlow:METHOD]
                TYPE => CATCH_SECTION(CATCH_KEYWORD,PARAMETER,CODE_BLOCK)  [scope=controlFlow:METHOD]
                  JAVA_CODE_REFERENCE => PARAMETER(TYPE)  [scope=controlFlow:METHOD]
              CODE_BLOCK => TRY_STATEMENT(TRY_KEYWORD,CODE_BLOCK,CATCH_SECTION,FINALLY_KEYWORD,CODE_BLOCK)  [scope=controlFlow:METHOD]
                EXPRESSION_STATEMENT => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=controlFlow:METHOD]
                  METHOD_CALL_EXPRESSION => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=controlFlow:METHOD]
                    REFERENCE_EXPRESSION => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION)  [scope=controlFlow:METHOD]
                    EXPRESSION_LIST => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION)  [scope=controlFlow:METHOD]
                      PREFIX_EXPRESSION => METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST)  [scope=controlFlow:METHOD]
                        LITERAL_EXPRESSION => EXPRESSION_LIST(PREFIX_EXPRESSION)  [scope=controlFlow:METHOD]
            CODE_BLOCK => TRY_STATEMENT(TRY_KEYWORD,CODE_BLOCK(EXPRESSION_STATEMENT),CATCH_SECTION(CATCH_KEYWORD,PARAMETER,CODE_BLOCK),FINALLY_KEYWORD,CODE_BLOCK(EXPRESSION_STATEMENT))  [scope=controlFlow:METHOD]
              EXPRESSION_STATEMENT => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=controlFlow:METHOD]
                METHOD_CALL_EXPRESSION => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=controlFlow:METHOD]
                  REFERENCE_EXPRESSION => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION)  [scope=controlFlow:METHOD]
                  EXPRESSION_LIST => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION)  [scope=controlFlow:METHOD]
                    LITERAL_EXPRESSION => METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST)  [scope=controlFlow:METHOD]
    """.trimIndent(),
    characterize(member("controlFlow")),
  ) }

  @Test
  fun testTreeNesting(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertEquals(
    """
      METHOD[nesting] => METHOD(TYPE,IDENTIFIER,PARAMETER_LIST,CODE_BLOCK)  [scope=nesting:METHOD]
        TYPE => TYPE(VOID_KEYWORD)  [scope=nesting:METHOD]
        PARAMETER_LIST => PARAMETER_LIST  [scope=nesting:METHOD]
        CODE_BLOCK => CODE_BLOCK(DECLARATION_STATEMENT(LOCAL_VARIABLE),DECLARATION_STATEMENT(CLASS),DECLARATION_STATEMENT(LOCAL_VARIABLE))  [scope=nesting:METHOD]
          DECLARATION_STATEMENT => DECLARATION_STATEMENT(LOCAL_VARIABLE(TYPE,LAMBDA_EXPRESSION))  [scope=nesting:METHOD]
            LOCAL_VARIABLE[r] => DECLARATION_STATEMENT(LOCAL_VARIABLE(TYPE,LAMBDA_EXPRESSION))  [scope=nesting:METHOD]
              TYPE => DECLARATION_STATEMENT(LOCAL_VARIABLE)  [scope=nesting:METHOD]
                JAVA_CODE_REFERENCE => LOCAL_VARIABLE(TYPE,LAMBDA_EXPRESSION)  [scope=nesting:METHOD]
              LAMBDA_EXPRESSION => LAMBDA_EXPRESSION(PARAMETER_LIST,ARROW,CODE_BLOCK)  [scope=r:LAMBDA_EXPRESSION]
                PARAMETER_LIST => PARAMETER_LIST  [scope=r:LAMBDA_EXPRESSION]
                CODE_BLOCK => CODE_BLOCK(EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION))  [scope=r:LAMBDA_EXPRESSION]
                  EXPRESSION_STATEMENT => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=r:LAMBDA_EXPRESSION]
                    METHOD_CALL_EXPRESSION => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=r:LAMBDA_EXPRESSION]
                      REFERENCE_EXPRESSION => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION)  [scope=r:LAMBDA_EXPRESSION]
                      EXPRESSION_LIST => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION)  [scope=r:LAMBDA_EXPRESSION]
                        LITERAL_EXPRESSION => METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST)  [scope=r:LAMBDA_EXPRESSION]
          DECLARATION_STATEMENT => DECLARATION_STATEMENT(CLASS(CLASS_KEYWORD,IDENTIFIER,METHOD))  [scope=nesting:METHOD]
            CLASS[Local] => CLASS(CLASS_KEYWORD,IDENTIFIER,METHOD)  [scope=Local:CLASS]
              METHOD[m] => METHOD(TYPE,PARAMETER_LIST,CODE_BLOCK)  [scope=m:METHOD]
                TYPE => TYPE(VOID_KEYWORD)  [scope=m:METHOD]
                PARAMETER_LIST => PARAMETER_LIST  [scope=m:METHOD]
                CODE_BLOCK => CODE_BLOCK(EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION))  [scope=m:METHOD]
                  EXPRESSION_STATEMENT => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=m:METHOD]
                    METHOD_CALL_EXPRESSION => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=m:METHOD]
                      REFERENCE_EXPRESSION => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION)  [scope=m:METHOD]
                      EXPRESSION_LIST => EXPRESSION_STATEMENT(METHOD_CALL_EXPRESSION)  [scope=m:METHOD]
                        LITERAL_EXPRESSION => METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST)  [scope=m:METHOD]
          DECLARATION_STATEMENT => DECLARATION_STATEMENT(LOCAL_VARIABLE(TYPE,NEW_EXPRESSION))  [scope=nesting:METHOD]
            LOCAL_VARIABLE[o] => DECLARATION_STATEMENT(LOCAL_VARIABLE(TYPE,NEW_EXPRESSION))  [scope=nesting:METHOD]
              TYPE => DECLARATION_STATEMENT(LOCAL_VARIABLE)  [scope=nesting:METHOD]
                JAVA_CODE_REFERENCE => LOCAL_VARIABLE(TYPE,NEW_EXPRESSION)  [scope=nesting:METHOD]
              NEW_EXPRESSION => DECLARATION_STATEMENT(LOCAL_VARIABLE)  [scope=nesting:METHOD]
                ANONYMOUS_CLASS => ANONYMOUS_CLASS(JAVA_CODE_REFERENCE,EXPRESSION_LIST,METHOD)  [scope=o:ANONYMOUS_CLASS]
                  JAVA_CODE_REFERENCE => JAVA_CODE_REFERENCE(IDENTIFIER)  [scope=o:ANONYMOUS_CLASS]
                  EXPRESSION_LIST => EXPRESSION_LIST  [scope=o:ANONYMOUS_CLASS]
                  METHOD[toString] => METHOD(MODIFIER_LIST,TYPE,IDENTIFIER,PARAMETER_LIST,CODE_BLOCK)  [scope=toString:METHOD]
                    MODIFIER_LIST => MODIFIER_LIST(ANNOTATION(JAVA_CODE_REFERENCE),PUBLIC_KEYWORD)  [scope=toString:METHOD]
                      ANNOTATION => MODIFIER_LIST(ANNOTATION(JAVA_CODE_REFERENCE),PUBLIC_KEYWORD)  [scope=toString:METHOD]
                        JAVA_CODE_REFERENCE => MODIFIER_LIST(ANNOTATION,PUBLIC_KEYWORD)  [scope=toString:METHOD]
                    TYPE => TYPE(JAVA_CODE_REFERENCE(IDENTIFIER))  [scope=toString:METHOD]
                      JAVA_CODE_REFERENCE => TYPE(JAVA_CODE_REFERENCE(IDENTIFIER))  [scope=toString:METHOD]
                    PARAMETER_LIST => PARAMETER_LIST  [scope=toString:METHOD]
                    CODE_BLOCK => CODE_BLOCK(RETURN_STATEMENT(RETURN_KEYWORD,METHOD_CALL_EXPRESSION))  [scope=toString:METHOD]
                      RETURN_STATEMENT => RETURN_STATEMENT(RETURN_KEYWORD,METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=toString:METHOD]
                        METHOD_CALL_EXPRESSION => RETURN_STATEMENT(RETURN_KEYWORD,METHOD_CALL_EXPRESSION(REFERENCE_EXPRESSION,EXPRESSION_LIST))  [scope=toString:METHOD]
                          REFERENCE_EXPRESSION => RETURN_STATEMENT(RETURN_KEYWORD,METHOD_CALL_EXPRESSION)  [scope=toString:METHOD]
                          EXPRESSION_LIST => RETURN_STATEMENT(RETURN_KEYWORD,METHOD_CALL_EXPRESSION)  [scope=toString:METHOD]
    """.trimIndent(),
    characterize(member("nesting")),
  ) }

  @Test
  fun testTreeExpressionBodyMethod(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertEquals(
    """
      METHOD[name] => METHOD(TYPE,IDENTIFIER,PARAMETER_LIST,CODE_BLOCK)  [scope=name:METHOD]
        TYPE => TYPE(JAVA_CODE_REFERENCE(IDENTIFIER))  [scope=name:METHOD]
          JAVA_CODE_REFERENCE => TYPE(JAVA_CODE_REFERENCE(IDENTIFIER))  [scope=name:METHOD]
        PARAMETER_LIST => PARAMETER_LIST  [scope=name:METHOD]
        CODE_BLOCK => CODE_BLOCK(RETURN_STATEMENT(RETURN_KEYWORD,LITERAL_EXPRESSION))  [scope=name:METHOD]
          RETURN_STATEMENT => RETURN_STATEMENT(RETURN_KEYWORD,LITERAL_EXPRESSION(STRING_LITERAL))  [scope=name:METHOD]
            LITERAL_EXPRESSION => RETURN_STATEMENT(RETURN_KEYWORD,LITERAL_EXPRESSION(STRING_LITERAL))  [scope=name:METHOD]
    """.trimIndent(),
    characterize(member("name")),
  ) }

  @Test
  fun testTreeParameterizedMethod(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertEquals(
    """
      METHOD[log] => METHOD(TYPE,IDENTIFIER,PARAMETER_LIST,CODE_BLOCK)  [scope=log:METHOD]
        TYPE => TYPE(VOID_KEYWORD)  [scope=log:METHOD]
        PARAMETER_LIST => PARAMETER_LIST(PARAMETER(TYPE))  [scope=log:METHOD]
          PARAMETER[v] => PARAMETER_LIST(PARAMETER(TYPE))  [scope=log:METHOD]
            TYPE => PARAMETER_LIST(PARAMETER)  [scope=log:METHOD]
        CODE_BLOCK => CODE_BLOCK  [scope=log:METHOD]
    """.trimIndent(),
    characterize(member("log")),
  ) }

  @Test
  fun testTreeEmptyVoidMethod(): Unit = timeoutRunBlocking(timeout = 2.minutes) { assertEquals(
    """
      METHOD[risky] => METHOD(TYPE,IDENTIFIER,PARAMETER_LIST,CODE_BLOCK)  [scope=risky:METHOD]
        TYPE => TYPE(VOID_KEYWORD)  [scope=risky:METHOD]
        PARAMETER_LIST => PARAMETER_LIST  [scope=risky:METHOD]
        CODE_BLOCK => CODE_BLOCK  [scope=risky:METHOD]
    """.trimIndent(),
    characterize(member("risky")),
  ) }

  private suspend fun sampleClass(): PsiClass {
    val file = myFixture.configureByText("Sample.java", RICH_SAMPLE)
    return readAction { PsiTreeUtil.findChildOfType(file, PsiClass::class.java)!! }
  }

  private suspend fun member(name: String): PsiElement {
    val sample = sampleClass()
    return readAction { sample.children.first { (it as? PsiNamedElement)?.name == name } }
  }

  private suspend fun staticInitializer(): PsiElement {
    val sample = sampleClass()
    return readAction { sample.children.first { it is PsiClassInitializer } }
  }

  /** Formats one node the way a problem reported there would be characterized: `TYPE[name] => astShape  [scope]`. */
  private fun format(element: PsiElement, depth: Int): String {
    val signals = PsiSignalExtractor.extractSignals(element)
    val type = PsiUtilCore.getElementType(element)?.toString() ?: "?"
    val name = (element as? PsiNamedElement)?.name?.let { "[$it]" } ?: ""
    return "${"  ".repeat(depth)}$type$name => ${signals.astShape ?: "-"}  [scope=${signals.enclosingScopeName ?: "-"}:${signals.enclosingScopeType ?: "-"}]"
  }

  /** Walks [root] and all descendants (leaf tokens / whitespace / comments skipped), one [format] line per node. */
  private suspend fun characterize(root: PsiElement): String = readAction {
    buildString {
      fun walk(element: PsiElement, depth: Int) {
        if (element is PsiWhiteSpace || element is PsiComment || element.firstChild == null) return
        appendLine(format(element, depth))
        var child = element.firstChild
        while (child != null) {
          walk(child, depth + 1)
          child = child.nextSibling
        }
      }
      walk(root, 0)
    }
  }.trimEnd()

  private suspend inline fun <reified T : PsiElement> assertSig(@Language("JAVA") code: String, expected: String, text: String? = null) {
    assertEquals(expected, sig<T>(code, text))
  }

  private suspend inline fun <reified T : PsiElement> sig(@Language("JAVA") code: String, text: String? = null): String {
    val file = myFixture.configureByText("Foo.java", code)
    return readAction {
      val candidates = PsiTreeUtil.findChildrenOfType(file, T::class.java)
      val element = (if (text == null) candidates.firstOrNull() else candidates.firstOrNull { it.text == text })
        ?: return@readAction "NOT FOUND ${T::class.simpleName}" + (text?.let { " '$it'" } ?: "")
      val s = PsiSignalExtractor.extractSignals(element)
      "${s.astShape ?: "-"}  [scope=${s.enclosingScopeName ?: "-"}:${s.enclosingScopeType ?: "-"}]"
    }
  }

  /**
   * A deliberately rich Java class exercising every structural boundary the algorithm cares about, split into
   * cohesive members so each characterization test stays small: class/method boundaries, a field with a call and
   * an array initializer (collection-literal boundary), a static initializer, a constructor, `compute` (if/else +
   * nested loops → depth-2 render vs. maxDepth exhaustion), `controlFlow` (arrow-`case` clause boundary,
   * try/catch/finally, foreach) and `nesting` (lambda block, local + anonymous classes).
   */
  private val RICH_SAMPLE = """
    class Sample {
      int field = compute(1, 2);
      int other;
      int[] data = {1, 2, 3};

      static {
        init();
      }

      Sample() {
        this.other = field;
      }

      static void init() {}

      int compute(int a, int b) {
        int sum = a + b;
        if (sum > 0) {
          while (sum > 0) {
            sum--;
          }
        } else {
          for (int i = 0; i < b; i++) {
            log(i);
          }
        }
        return sum;
      }

      void controlFlow(int x, int[] xs) {
        switch (x) {
          case 1 -> log(x);
          case 2 -> log(2);
          default -> { log(0); }
        }
        for (int v : xs) {
          log(v);
        }
        try {
          risky();
        } catch (Exception e) {
          log(-1);
        } finally {
          log(9);
        }
      }

      void nesting() {
        Runnable r = () -> { log(42); };
        class Local { void m() { log(7); } }
        Object o = new Object() {
          @Override public String toString() { return name(); }
        };
      }

      String name() { return "x"; }
      void log(int v) {}
      void risky() {}
    }
  """.trimIndent()
}
