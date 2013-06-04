package com.jetbrains.lang.dart.analyzer;

import com.intellij.openapi.util.io.FileUtil;
import com.jetbrains.lang.dart.util.DartTestUtils;

public class DartTypeErrorAnalyzingTest extends DartAnalyzerTestBase {
  @Override
  protected String getBasePath() {
    return "/analyzer/type";
  }

  public void testCannotBeResolved1$CreateDartGetterSetterAction() throws Throwable {
    doTest("Undefined name 'hole'");
  }

  public void testCannotBeResolved1$CreateFieldAction() throws Throwable {
    doTest("Undefined name 'hole'");
  }

  public void testCannotBeResolved1$CreateGlobalDartGetterSetterAction() throws Throwable {
    doTest("Undefined name 'hole'");
  }

  public void testCannotBeResolved1$CreateGlobalVariableAction() throws Throwable {
    doTest("Undefined name 'hole'");
  }

  public void testCannotBeResolved1$CreateLocalVariableAction() throws Throwable {
    doTest("Undefined name 'hole'");
  }

  public void testCannotBeResolved2() throws Throwable {
    doTest("There is no such setter 'noField' in 'A'");
  }

  public void testCannotBeResolved3() throws Throwable {
    doTest("There is no such getter 'foo' in 'A'");
  }

  public void testCannotBeResolved4() throws Throwable {
    doTest("There is no such setter 'foo' in 'A'");
  }

  public void testCannotBeResolved5() throws Throwable {
    doTest("Undefined name 'Unknown'");
  }

  public void testCannotBeResolved6$CreateDartGetterSetterAction() throws Throwable {
    doTest("Undefined name 'unknown'");
  }

  public void testCannotBeResolved6$CreateGlobalDartGetterSetterAction() throws Throwable {
    doTest("Undefined name 'unknown'");
  }

  public void testCannotBeResolved6$CreateGlobalVariableAction() throws Throwable {
    doTest("Undefined name 'unknown'");
  }

  public void testCannotBeResolved7$CreateDartGetterSetterAction() throws Throwable {
    doTest("Undefined name 'unknown'");
  }

  public void testCannotBeResolved7$CreateGlobalDartGetterSetterAction() throws Throwable {
    doTest("Undefined name 'unknown'");
  }

  public void testCannotBeResolved8() throws Throwable {
    doTest("Undefined name 'Unknown'");
  }

  public void ConcreteClassHasUnimplementedMembers1() throws Throwable {
    // https://code.google.com/p/dart/issues/detail?id=10755
    doTest("Concrete class A has unimplemented member(s)");
  }

  public void ConcreteClassHasUnimplementedMembers2() throws Throwable {
    // https://code.google.com/p/dart/issues/detail?id=10755
    doTest("Concrete class A has unimplemented member(s)");
  }

  public void testFieldHasNoGetter1() throws Throwable {
    doTest("There is no such getter 'bar' in 'Foo'");
  }

  public void FieldHasNoSetter1() throws Throwable {
    // https://code.google.com/p/dart/issues/detail?id=10756
    doTest("Field 'bar' has no setter");
  }

  public void testInterfaceHasNoMethodNamed1() throws Throwable {
    doTest("The method 'foo' is not defined for the class 'A'");
  }

  public void testInterfaceHasNoMethodNamed2() throws Throwable {
    doTest("The method 'foo' is not defined for the class 'A'");
  }

  public void testInterfaceHasNoMethodNamed3() throws Throwable {
    doTest("There is no such operator '+' in 'A'");
  }

  public void testNoSuchType1() throws Throwable {
    doTest("Undefined class 'Foo'");
  }

  public void testNoSuchType2() throws Throwable {
    doTest("Undefined class 'Foo'");
  }

  public void testNotAMember1() throws Throwable {
    doTest("There is no such getter 'result' in 'Adder'");
  }
}
