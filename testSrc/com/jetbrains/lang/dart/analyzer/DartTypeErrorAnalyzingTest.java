package com.jetbrains.lang.dart.analyzer;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;

public class DartTypeErrorAnalyzingTest extends DartAnalyzerTestBase {
  @Override
  protected String getTestDataPath() {
    return PathManager.getHomePath() + FileUtil.toSystemDependentName("/plugins/Dart/testData/analyzer/type");
  }

  public void testCannotBeResolved1$CreateDartGetterSetterAction() throws Throwable {
    doTest("cannot resolve hole");
  }

  public void testCannotBeResolved1$CreateFieldAction() throws Throwable {
    doTest("cannot resolve hole");
  }

  public void testCannotBeResolved1$CreateGlobalDartGetterSetterAction() throws Throwable {
    doTest("cannot resolve hole");
  }

  public void testCannotBeResolved1$CreateGlobalVariableAction() throws Throwable {
    doTest("cannot resolve hole");
  }

  public void testCannotBeResolved1$CreateLocalVariableAction() throws Throwable {
    doTest("cannot resolve hole");
  }

  public void testCannotBeResolved2() throws Throwable {
    doTest("cannot resolve noField");
  }

  public void testCannotBeResolved3() throws Throwable {
    doTest("cannot resolve f");
  }

  public void testCannotBeResolved4() throws Throwable {
    doTest("cannot resolve f");
  }

  public void testCannotBeResolved5() throws Throwable {
    doTest("cannot resolve Unknown");
  }

  public void testCannotBeResolved6$CreateDartGetterSetterAction() throws Throwable {
    doTest("cannot resolve unknown");
  }

  public void testCannotBeResolved6$CreateGlobalDartGetterSetterAction() throws Throwable {
    doTest("cannot resolve unknown");
  }

  public void testCannotBeResolved6$CreateGlobalVariableAction() throws Throwable {
    doTest("cannot resolve unknown");
  }

  public void testCannotBeResolved7$CreateDartGetterSetterAction() throws Throwable {
    doTest("cannot resolve unknown");
  }

  public void testCannotBeResolved7$CreateGlobalDartGetterSetterAction() throws Throwable {
    doTest("cannot resolve unknown");
  }

  public void testCannotBeResolved8() throws Throwable {
    doTest("cannot resolve Unknown");
  }

  public void testConcreteClassHasUnimplementedMembers1() throws Throwable {
    doTest("Concrete class A has unimplemented member(s) \n" +
           "    # From Foo:\n" +
           "        int fooA\n" +
           "        void fooB()\n" +
           "    # From Bar:\n" +
           "        void barA()");
  }

  public void testConcreteClassHasUnimplementedMembers2() throws Throwable {
    doTest("Concrete class A has unimplemented member(s) \n" +
           "    # From I:\n" +
           "        dynamic foo");
  }

  public void testFieldHasNoGetter1() throws Throwable {
    doTest("Field 'bar' has no getter");
  }

  public void testFieldHasNoSetter1() throws Throwable {
    doTest("Field 'bar' has no setter");
  }

  public void testInterfaceHasNoMethodNamed1() throws Throwable {
    doTest("\"A\" has no method named \"foo\"");
  }

  public void testInterfaceHasNoMethodNamed2() throws Throwable {
    doTest("\"A\" has no method named \"foo\"");
  }

  public void testInterfaceHasNoMethodNamed3() throws Throwable {
    doTest("\"A\" has no method named \"operator +\"");
  }

  public void testNoSuchType1() throws Throwable {
    doTest("no such type \"Foo\"");
  }

  public void testNoSuchType2() throws Throwable {
    doTest("no such type \"Foo\"");
  }

  public void testNotAMember1() throws Throwable {
    doTest("\"result\" is not a member of Adder");
  }
}
