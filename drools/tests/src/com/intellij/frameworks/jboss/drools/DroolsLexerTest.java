// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.frameworks.jboss.drools;

import com.intellij.platform.testFramework.junit5.codeInsight.fixture.LexerTestFixture;
import com.intellij.plugins.drools.lang.lexer.DroolsLexer;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.testFramework.junit5.fixture.TestFixtures;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.LexerTestFixtureKt.lexerFixture;

@TestFixtures
public class DroolsLexerTest {
  private final TestFixture<LexerTestFixture> lexer = lexerFixture("contrib/drools/tests/testData/lexer/", () -> new DroolsLexer());

  @Test
  public void testSingleComments() {
    lexer.get().doTest("// single comment \n // single comment2");
  }

  @Test
  public void testDeclare() {
    lexer.get().doTest("declare KeyEvent @role(event) @expires(0s) end");
  }

  @Test
  public void testDeclare2() {
    lexer.get().doTest("declare KeyEvent @role(event) end");
  }

  @Test
  public void testDeclare3() {
    lexer.get().doTest("declare KeyEvent @role( event = 1) end");
  }

  @Test
  public void testMultilineComments() {
    lexer.get().doTest("/* first \n second */");
  }

  @Test
  public void testStringLiteral() {
    lexer.get().doTest("\"abc \"");
  }

  @Test
  public void testPackageStatement() {
    lexer.get().doTest("package aaa.bbb.ccc;");
  }

  @Test
  public void testPackageStatement2() {
    lexer.get().doTest("package or.and.foo;");
  }

  @Test
  public void testImportStatement() {
    lexer.get().doTest("import org.drools.examples.fibonacci.FibonacciExample.Fibonacci;");
  }

  @Test
  public void testSimpleRule() {
    lexer.get().doTest("rule Recurse\n when \n  true \n  then\n end");
  }

  @Test
  public void testSimpleRule2() {
    lexer.get().doTest("rule \"Recurse\"\n when \n  true \n  then\n end");
  }

  @Test
  public void testSimpleRule3() {
    lexer.get().doTest("""
             rule Calculate
             then
                 int value = 1;
             end""");
  }

  @Test
  public void testSimpleRule4() {
    lexer.get().doTest("""
             global org.drools.games.adventures.Counter counter

             dialect "mvel\"""");
  }

  @Test
  public void testInsertLogical() {
    lexer.get().doTest("rule A then insertLogical(new Foo()) end");
  }

  @Test
  public void testChunkBlock1() {
    lexer.get().doTest("duration (aaa+11) rule aa");
  }

  @Test
  public void testChunkBlock2() {
    lexer.get().doTest("""
             rule "Gold Priority"
                 duration 1000
                 when
             """);
  }

  @Test
  public void testChunkBlock3() {
    lexer.get().doTest("duration ()");
  }

  @Test
  public void testChunkBlock4() {
    lexer.get().doTest("duration (((aaa+11)))");
  }

  @Test
  public void testChunkBlock5() {
    lexer.get().doTest("duration ( rule aaa");
  }

  @Test
  public void testFunctionBlock() {
    lexer.get().doTest("""
             function void sendEscalationEmail( Customer customer, Ticket ticket ) {
                 System.out.println( "Email : " + ticket );
             }""");
  }

  @Test
  public void testFunctionBlock2() {
    lexer.get().doTest("function void sendEscalationEmail() { {}{ { {} } } }");
  }

  @Test
  public void testFunctionBlock3() {
    lexer.get().doTest("function void sendEscalationEmail() { rule aaa then end");
  }

  @Test
  public void testStatements1() {
    lexer.get().doTest("""
             then
                     aaa
                     modify( m ) { m+1 }
                     bbb
             end""");
  }

  @Test
  public void testStatements2() {
    lexer.get().doTest("""
             then
                     aaa
                     modify( m ) { m+1 }
                     modify( m ) { m+1 }
                     bbb
                     modify( m ) { m+1 }
             end""");
  }

  @Test
  public void testStatements3() {
    lexer.get().doTest("""
             then
                     update( m==2 ) ;        java_statement 1;
                     retract( aaa != bbb )
                     java_statement 2;
                     modify( m ) { m+1 }
                     java_statement 3;
             end""");
  }

  @Test
  public void testStatements4() {
    lexer.get().doTest("""
             then
                     java_statement 0;
                     update( (m==2) ) ;        java_statement 1;
                     retract( ()(aaa != bbb) )
                     java_statement 2;
                     modify( ()(m) ) { {}{{}}{m+1} }
                     java_statement 3;
             end""");
  }

  @Test
  public void testStatements5() {
    lexer.get().doTest("""
             then
                     aaa
                     modify( m ) { m+1         bbb
             end""");
  }

  @Test
  public void testIncorrectModify() {
    lexer.get().doTest("rule a then modify end");
  }

  @Test
  public void testStatements6() {
    lexer.get().doTest("""
             then
                     aaa
                     modify( m ) { m+1         bbb
             en""");
  }

  @Test
  public void testStatements7() {
    lexer.get().doTest("then  modify( $edgIntellijIdeaRulezzz ){}");
  }

  @Test
  public void testStatements8() {
    lexer.get().doTest("""
             then
                     java_statement 0;
                     update( (m==2) ) ;        java_statement 1;
                     retract( ()(aaa != bbb) )
             then[foo1]        retract( ()(aaa != bbb) )
                    java_statement 2;
             then[foo2]         modify( ()(m) ) { {}{{}}{m+1} }
                     java_statement 3;
             end""");
  }

  @Test
  public void testDeprecatedComments() {
    lexer.get().doTest("""
             then
                     java_statement 0;
                     #update( (m==2) ) ;        update( (m==2) ) ;        java_statement 1;
                     retract( ()(aaa != bbb) )
                     java_statement 2;
                     modify( ()(m) ) { {}{{}}{m+1} }
                     #java_statement 3;
                     java_statement 3;
                     #java_statement 3;
             end""");
  }

  @Test
  public void testDeprecatedComments2() {
    lexer.get().doTest("""
             then
                     #update( (m==2) ) ;        #java_statement 3;
             end""");
  }

  @Test
  public void testDeprecatedComments3() {
    lexer.get().doTest("then\n #update( (m==2)");
  }

  @Test
  public void testIncomleteFunction() {
    lexer.get().doTest("function void foo(){");
  }

  @Test
  public void testSimpleFunction() {
    lexer.get().doTest("function void foo(){}");
  }
}
