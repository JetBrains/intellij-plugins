// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.refactoring;

import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.editor.FlexProjectDescriptor;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSChangeSignatureTestBase;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.dialects.ECMAL4LanguageDialect;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.refactoring.changeSignature.JSParameterInfo;
import com.intellij.openapi.util.Disposer;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;

import static com.intellij.refactoring.changeSignature.ParameterInfo.NEW_PARAMETER;

public class FlexChangeSignatureTest extends JSChangeSignatureTestBase {
  @Override
  public void setUp() throws Exception {
    super.setUp();
    FlexTestUtils.allowFlexVfsRootsFor(myFixture.getTestRootDisposable(), "");
    FlexTestUtils.setupFlexSdk(getModule(), getTestName(false), getClass(), myFixture.getTestRootDisposable());

  }

  @Override
  protected String[] getActiveFileNames() {
    return new String[]{"From.as", "From.mxml"};
  }

  @NotNull
  @Override
  protected String getTestRoot() {
    return "/flexChangeSignature/";
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return FlexProjectDescriptor.DESCRIPTOR;
  }

  public void testAddParam1() {
    doTest("bar", JSAttributeList.AccessType.PUBLIC, "int",
           new JSParameterInfo("stringParam", "String", "", "\"def\"", NEW_PARAMETER));
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testAddParam2() {
    doTest("blablabala__12", JSAttributeList.AccessType.PACKAGE_LOCAL, "",
           new JSParameterInfo("p", "mx.messaging.AbstractConsumer", "", "CONS", NEW_PARAMETER),
           new JSParameterInfo("p2", "flash.events.EventDispatcher", "null", "DISP", NEW_PARAMETER));
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testAddParam3() {
    doTest("bar", JSAttributeList.AccessType.PACKAGE_LOCAL, "",
           new JSParameterInfo("p1", "int", "", "100", NEW_PARAMETER),
           new JSParameterInfo("p2", "String", "abc", "\"def\"", NEW_PARAMETER),
           new JSParameterInfo("p3", "Boolean", "false", "", NEW_PARAMETER),
           new JSParameterInfo("p4", "...", "", "", NEW_PARAMETER, false, ECMAL4LanguageDialect.DIALECT_OPTION_HOLDER));
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testRemoveParam1() {
    doTest("foo", JSAttributeList.AccessType.PRIVATE, "flash.events.EventDispatcher");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testChangeParam1() {
    withRefactorSuperMethod(() -> doTest("renamed", JSAttributeList.AccessType.PACKAGE_LOCAL, "Boolean",
                                         new JSParameterInfo("i2", "Number", "", "", 1),
                                         new JSParameterInfo("sss", "String", "\"abc\"", "", 0),
                                         new JSParameterInfo("o", "flash.events.EventDispatcher", "FOO", "", 2),
                                         new JSParameterInfo("rest2", "...", "", "", 3, false, ECMAL4LanguageDialect.DIALECT_OPTION_HOLDER)),
                            false);
  }


  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testProp1() {
    doTest("v2", JSAttributeList.AccessType.PROTECTED, "Number");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testProp2() {
    doTest("v2", JSAttributeList.AccessType.PROTECTED, "void",
           new JSParameterInfo("value", "Number", "", "", 0));
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testPropagate1() {
    doDefaultTest((rootDir, rootAfter) -> {
      assertPropagationCandidates(new String[]{"bar", "zzz", "abc", "nopropagate"});
      performRefactoring("foo", JSAttributeList.AccessType.PACKAGE_LOCAL, "",
                         new JSParameterInfo("s", "String", "", "", 0),
                         new JSParameterInfo("added1", "Number", "", "added1def", NEW_PARAMETER),
                         new JSParameterInfo("added2", "Object", "", "added2def", NEW_PARAMETER));
    });
  }

  public void testConflicts1() {
    String[] conflicts = new String[]{
      "There is already a parameter p1 in method From.foo(String, String). It will conflict with the new parameter.",
      "There is already a parameter p1 in the method From.foo(String, String). It will conflict with the renamed parameter.",
      "There is already a local variable p2 in method From.foo(String, String). It will conflict with the new parameter.",
      "There is already a local variable p3 in method FromEx.foo(String, String). It will conflict with the new parameter.",
      "There is already a local variable p1 in method From.pp(). It will conflict with the new parameter.",
      "There is already a local variable p2 in method From.pp(). It will conflict with the new parameter.",
      "There is already a local variable p3 in method From.pp(). It will conflict with the new parameter.",
      "There is already a local variable p1 in function pp2(). It will conflict with the new parameter.",
      "Class From already contains a method bar()",
      "Method FromEx.foo(String, String) with internal visibility won't be accessible from function zz()",
      "Method From.foo(String, String) with internal visibility won't be able to participate in hierarchy"
    };
    doTestConflicts("bar", JSAttributeList.AccessType.PACKAGE_LOCAL, "String", conflicts,
                    new JSParameterInfo("p1", "String", "", "", 0), // p1
                    new JSParameterInfo("p1", "String", "", "", 1), // p2->p1
                    new JSParameterInfo("p1", "String", "", "a", NEW_PARAMETER),
                    new JSParameterInfo("p2", "String", "", "b", NEW_PARAMETER),
                    new JSParameterInfo("p3", "String", "", "c", NEW_PARAMETER));
  }

  public void testConflicts2() {
    String[] conflicts = new String[]{
      "There is already a local variable value2 in the property From.prop. It will conflict with the renamed parameter.",
      "Property From.prop with private visibility won't be accessible from function ttt()",
      "Class From already contains a field _prop2"
    };
    doTestConflicts("prop2", JSAttributeList.AccessType.PRIVATE, "*", conflicts,
                    new JSParameterInfo("value2", "", "", "foo", 0));
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testMxml() {
    doDefaultTest((rootDir, rootAfter) -> {
      assertPropagationCandidates(new String[]{"ref", "aaa"});
      performRefactoring("abc2", JSAttributeList.AccessType.PACKAGE_LOCAL, "",
             new JSParameterInfo("s", "String", "", "\"def\"", NEW_PARAMETER),
             new JSParameterInfo("p", "int", "", "0", NEW_PARAMETER),
             new JSParameterInfo("z", "Object", "", "this", NEW_PARAMETER));
    });
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testExistingSignature() {
    doTest("foo", JSAttributeList.AccessType.PACKAGE_LOCAL, "",
           new JSParameterInfo("p", "int", "15", "2000", NEW_PARAMETER),
           new JSParameterInfo("s", "String", "null", "\"abcde\"", NEW_PARAMETER));
  }

  public void testAddParam4() {
    doTest("foo", JSAttributeList.AccessType.PACKAGE_LOCAL, "void",
           new JSParameterInfo("i", "int", "", "777", NEW_PARAMETER),
           new JSParameterInfo("p", "String", "\"default\"", "", 0),
           new JSParameterInfo("z", "Test", "null", "", NEW_PARAMETER));
  }

  public void testAddParam5() {
    doTest("foo", JSAttributeList.AccessType.PACKAGE_LOCAL, "void",
           new JSParameterInfo("p", "String", "", "\"abc\"", NEW_PARAMETER),
           new JSParameterInfo("args", "...", "", "", 0, false, ECMAL4LanguageDialect.DIALECT_OPTION_HOLDER));
  }

  public void testNested() {
    doTest("nested2", JSAttributeList.AccessType.PACKAGE_LOCAL, "int",
           new JSParameterInfo("p", "String", "", "\"abc\"", NEW_PARAMETER));
  }

  public void testSuperConstructorCall() {
    doTest("From", JSAttributeList.AccessType.PACKAGE_LOCAL, "",
           new JSParameterInfo("p", "String", "", "", 0),
           new JSParameterInfo("b", "Boolean", "", "true", NEW_PARAMETER));
  }

  public void testSuperConstructorCall2() {
    doDefaultTest((rootDir, rootAfter) -> {
      assertPropagationCandidates(new String[]{"B"});
      performRefactoring("A", JSAttributeList.AccessType.PUBLIC, "",
             new JSParameterInfo("p", "String", "", "", 0),
             new JSParameterInfo("b", "Boolean", "", "true", NEW_PARAMETER));
    });
  }

  public void testNamespace() {
    doTest("foo2", JSAttributeList.AccessType.PUBLIC, "", new JSParameterInfo("p", "", "", "", 0));
  }

  public void testNamespace2() {
    doTest("foo2", JSAttributeList.AccessType.PRIVATE, "", new JSParameterInfo("p", "", "", "", 0));
  }

  public void testAddParam6() {
    doDefaultTest((rootDir, rootAfter) -> {
      assertPropagationCandidates(new String[]{"A", "B"});
      performRefactoring("doSmth", JSAttributeList.AccessType.PACKAGE_LOCAL, "",
             new JSParameterInfo("s", "String", "", "\"abc\"", NEW_PARAMETER),
             new JSParameterInfo("args", "...", "", "", 0, false, ECMAL4LanguageDialect.DIALECT_OPTION_HOLDER));
    });
  }

  public void testAddParam7() {
    doTest("doSmth", JSAttributeList.AccessType.PACKAGE_LOCAL, "",
           new JSParameterInfo("s", "String", "", "", 1),
           new JSParameterInfo("i", "int", "", "", 0),
           new JSParameterInfo("b", "Type", "", "def", NEW_PARAMETER),
           new JSParameterInfo("i2", "int", "0", "", 2),
           new JSParameterInfo("p", "From", "null", "", 3));
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testNoPropagateToSdkInheritor() {
    FlexTestUtils.addLibrary(getModule(), "Lib", getTestDataPath() + getTestRoot() + getTestName(false), "Flex_small.swc", null, null);
    Disposer.register(myFixture.getTestRootDisposable(), () -> FlexTestUtils.removeLibrary(getModule(), "Lib"));
    doDefaultTest((rootDir, rootAfter) -> {
      assertPropagationCandidates(new String[]{"bar", "listener"});
      performRefactoring("abc", JSAttributeList.AccessType.PACKAGE_LOCAL, "");
    });
  }

  private void doTestInaccessible() {
    try {
      doTest("", JSAttributeList.AccessType.PACKAGE_LOCAL, "", JSParameterInfo.EMPTY_ARRAY);
      fail("Refactoring should be inaccessible");
    }
    catch (CommonRefactoringUtil.RefactoringErrorHintException e) {
      // expected
    }
  }

  public void testAnonymousFunction1() {
    doTestInaccessible();
  }

  public void testAnonymousFunction2() {
    doTestInaccessible();
  }

  public void testAnonymousFunction3() {
    doTestInaccessible();
  }

  public void testAnonymousFunction4() {
    doTest("v2", JSAttributeList.AccessType.PACKAGE_LOCAL, "String", new JSParameterInfo("b2", "String", "", "", 1),
           new JSParameterInfo("a2", "int", "", "", 0), new JSParameterInfo("c", "Boolean", "", "false", NEW_PARAMETER));
  }

  public void testAnonymousFunction5() {
    doDefaultTest((rootDir, rootAfter) -> {
      assertPropagationCandidates(new String[]{"usage1", "usage2"});
      performRefactoring("sayLoud", JSAttributeList.AccessType.PUBLIC, "void",
             new JSParameterInfo("message", "String", "", "", 0),
             new JSParameterInfo("loud", "Boolean", "true", "false", NEW_PARAMETER));
    });
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testEventHandlerCall() {
    doTest("", JSAttributeList.AccessType.PACKAGE_LOCAL, "void",
           new JSParameterInfo("i", "int", "", "1000", NEW_PARAMETER));
  }

  public void testPropagateToFunctionExpression() {
    doDefaultTest((rootDir, rootAfter) -> {
      assertPropagationCandidates(new String[]{"f1"});
      performRefactoring("", JSAttributeList.AccessType.PACKAGE_LOCAL, "",
                         new JSParameterInfo("i", "int", "", "100", NEW_PARAMETER));
    });
  }

  public void testImportsForAgrumentsAndInitializers() {
    doTest("foo", JSAttributeList.AccessType.PACKAGE_LOCAL, "",
           new JSParameterInfo("p1", "com.Foo", "aaa.A.SIZE", "new com.Foo()", NEW_PARAMETER),
           new JSParameterInfo("p2", "com.Bar", "bbb.B.ourLength", "com.Bar.SIZE", NEW_PARAMETER),
           new JSParameterInfo("p3", "String", "", "com.Foo.MESSAGE", NEW_PARAMETER),
           new JSParameterInfo("p4", "bar.Zzz", "", "bar.Zzz.func(new bar.Yyy(), uuu.glob(com.Const))", NEW_PARAMETER),
           new JSParameterInfo("p5", "bar.Yyy", "", "unresolved", NEW_PARAMETER));
  }

  public void testIncompatibleOverrideConflict() {
    String[] conflicts = new String[]{
      JavaScriptBundle.message("change.signature.conflict.incompatible.override", "method B.foo()", "method A.foo(int)", "Method B.foo()")
    };
    doTestConflicts("foo2", JSAttributeList.AccessType.PUBLIC, "", conflicts, new JSParameterInfo("j", "int", "", "", 0));
  }

  public void testIncompatibleImplementationConflict() {
    String[] conflicts = new String[]{
      JavaScriptBundle.message("change.signature.conflict.incompatible.implementation", "method B.foo()", "method A.foo(int)", "Method B.foo()")
    };
    doTestConflicts("foo", JSAttributeList.AccessType.PUBLIC, "", conflicts, new JSParameterInfo("j", "int", "", "", 0));
  }

  public void testIncompatibleImplementation() {
    myIgnoreConflicts = true;
    doTest("foo2", JSAttributeList.AccessType.PUBLIC, "", new JSParameterInfo("j", "int", "", "", 0));
  }
}