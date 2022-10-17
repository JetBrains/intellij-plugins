/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.frameworks.jboss.drools;

import com.intellij.lexer.Lexer;
import com.intellij.plugins.drools.lang.lexer.DroolsLexer;
import com.intellij.testFramework.LexerTestCase;
import org.jetbrains.annotations.NonNls;

public class DroolsLexerTest extends LexerTestCase {
  @Override
  protected Lexer createLexer() {
    return new DroolsLexer();
  }

  @Override
  protected String getDirPath() {
    return "contrib/drools/tests/testData/lexer/";
  }

  public void testSingleComments() {
    doTest("// single comment \n // single comment2");
  }
  public void testDeclare() {
    doTest("declare KeyEvent @role(event) @expires(0s) end");
  }

  public void testDeclare2() {
    doTest("declare KeyEvent @role(event) end");
  }

  public void testDeclare3() {
    doTest("declare KeyEvent @role( event = 1) end");
  }

  public void testMultilineComments() {
    doTest("/* first \n second */");
  }

  public void testStringLiteral() {
    doTest("\"abc \"");
  }

  public void testPackageStatement() {
    doTest("package aaa.bbb.ccc;");
  }

  public void testPackageStatement2() {
    doTest("package or.and.foo;");
  }

  public void testImportStatement() {
    doTest("import org.drools.examples.fibonacci.FibonacciExample.Fibonacci;");
  }

  public void testSimpleRule() {
    doTest("rule Recurse\n when \n  true \n  then\n end");
  }

  public void testSimpleRule2() {
    doTest("rule \"Recurse\"\n when \n  true \n  then\n end");
  }

  public void testSimpleRule3() {
    doTest("""
             rule Calculate
             then
                 int value = 1;
             end""");
  }

  public void testSimpleRule4() {
    doTest("""
             global org.drools.games.adventures.Counter counter

             dialect "mvel\"""");
  }

  public void testInsertLogical() {
    doTest("rule A then insertLogical(new Foo()) end");
  }

  public void testChunkBlock1() {
    doTest("duration (aaa+11) rule aa");
  }

  public void testChunkBlock2() {
    doTest("""
             rule "Gold Priority"
                 duration 1000
                 when
             """);
  }

  public void testChunkBlock3() {
    doTest("duration ()");
  }

  public void testChunkBlock4() {
    doTest("duration (((aaa+11)))");
  }

  public void testChunkBlock5() {
    doTest("duration ( rule aaa");
  }

  public void testFunctionBlock() {
    doTest("""
             function void sendEscalationEmail( Customer customer, Ticket ticket ) {
                 System.out.println( "Email : " + ticket );
             }""");
  }

  public void testFunctionBlock2() {
    doTest("function void sendEscalationEmail() { {}{ { {} } } }");
  }

  public void testFunctionBlock3() {
    doTest("function void sendEscalationEmail() { rule aaa then end");
  }

  public void testStatements1() {
    doTest("""
             then
                     aaa
                     modify( m ) { m+1 }
                     bbb
             end""");
  }

  public void testStatements2() {
    doTest("""
             then
                     aaa
                     modify( m ) { m+1 }
                     modify( m ) { m+1 }
                     bbb
                     modify( m ) { m+1 }
             end""");
  }

  public void testStatements3() {
    doTest("""
             then
                     update( m==2 ) ;        java_statement 1;
                     retract( aaa != bbb )
                     java_statement 2;
                     modify( m ) { m+1 }
                     java_statement 3;
             end""");
  }

  public void testStatements4() {
    doTest("""
             then
                     java_statement 0;
                     update( (m==2) ) ;        java_statement 1;
                     retract( ()(aaa != bbb) )
                     java_statement 2;
                     modify( ()(m) ) { {}{{}}{m+1} }
                     java_statement 3;
             end""");
  }

  public void testStatements5() {
    doTest("""
             then
                     aaa
                     modify( m ) { m+1         bbb
             end""");
  }
  public void testIncorrectModify() {
    doTest("rule a then modify end");
  }

  public void testStatements6() {
    doTest("""
             then
                     aaa
                     modify( m ) { m+1         bbb
             en""");
  }

  public void testStatements7() {
    doTest("then  modify( $edgIntellijIdeaRulezzz ){}");
  }

  public void testStatements8() {
    doTest("""
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

  public void testDeprecatedComments() {
    doTest("""
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
  public void testDeprecatedComments2() {
    doTest("""
             then
                     #update( (m==2) ) ;        #java_statement 3;
             end""");
  }
  public void testDeprecatedComments3() {
    doTest("then\n #update( (m==2)");
  }
  public void testIncomleteFunction() {
    doTest("function void foo(){");
  }

  public void testSimpleFunction() {
    doTest("function void foo(){}");
  }

  @Override
  protected void doTest(@NonNls String text) {
    super.doTest(text);
    checkCorrectRestart(text);
  }
}
