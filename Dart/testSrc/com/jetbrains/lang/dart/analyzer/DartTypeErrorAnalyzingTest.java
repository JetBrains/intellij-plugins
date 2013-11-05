package com.jetbrains.lang.dart.analyzer;

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

  public void testCannotBeResolved8$DartImportFix() throws Throwable {
    doTest("Undefined name 'Unknown'", "foo.dart");
  }

  public void testCannotBeResolved9() throws Throwable {
    doTestWithoutCheck("There is no such getter 'foo' in 'Foo'", "cannotBeResolved9_Foo.dart");
    myFixture.checkResultByFile("cannotBeResolved9.dart");
    myFixture.checkResultByFile("cannotBeResolved9_Foo.dart", "cannotBeResolved9_Foo.after.dart", true);
  }

  public void testConcreteClassHasUnimplementedMembers1() throws Throwable {
    doTest("Missing inherited members: "); // can't use full error message here because the order of 3 parameters varies
  }

  public void testConcreteClassHasUnimplementedMembers2() throws Throwable {
    doTest("Missing inherited member 'I.foo'");
  }

  public void testFieldHasNoGetter1() throws Throwable {
    doTest("There is no such getter 'bar' in 'Foo'"); // dartanalyzer bug: reported as Hint instead of Error
  }

  public void testFieldHasNoSetter1() throws Throwable { // dartanalyzer bug: no error reported
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
    doTest("The name 'Foo' is not a type and cannot be used in an 'as' expression");
  }

  public void testNoSuchType1$DartImportFix() throws Throwable {
    doTest("The name 'Foo' is not a type and cannot be used in an 'as' expression", "foo.dart");
  }

  public void testNoSuchType2() throws Throwable {
    doTest("Undefined class 'Foo'");
  }

  public void testNotAMember1() throws Throwable {
    doTest("There is no such getter 'result' in 'Adder'");
  }
}
