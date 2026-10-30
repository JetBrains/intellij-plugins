// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.refactoring;

import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSChangeSignatureFixture;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.dialects.ECMAL4LanguageDialect;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.refactoring.changeSignature.JSParameterInfo;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.JSChangeSignatureFixture.jsChangeSignatureFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;
import static com.intellij.refactoring.changeSignature.ParameterInfo.NEW_PARAMETER;
import static org.junit.jupiter.api.Assertions.fail;

@TestApplication
public class FlexChangeSignatureTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSChangeSignatureFixture> js =
    jsChangeSignatureFixture(projectFixture, new JSFixtureOptions(FlexTestUtils.getTestDataPath(""))
        .withBasePath("/flexChangeSignature/")
        .withModuleType(FlexModuleType.MODULE_TYPE_ID), "From.as", "From.mxml");

  @BeforeEach
  void setUp() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    FlexTestUtils.allowFlexVfsRootsFor(myFixture.getTestRootDisposable(), "");
    FlexTestUtils.setupFlexSdk(js.get().getFixture().getModule(), js.get().getTestName(false), getClass(), myFixture.getTestRootDisposable());
  }

  @Test
  public void testAddParam1() {
    js.get().doTest("bar", JSAttributeList.AccessType.PUBLIC, "int",
                    new JSParameterInfo("stringParam", "String", "", "\"def\"", NEW_PARAMETER));
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testAddParam2() {
    js.get().doTest("blablabala__12", JSAttributeList.AccessType.PACKAGE_LOCAL, "",
                    new JSParameterInfo("p", "mx.messaging.AbstractConsumer", "", "CONS", NEW_PARAMETER),
                    new JSParameterInfo("p2", "flash.events.EventDispatcher", "null", "DISP", NEW_PARAMETER));
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testAddParam3() {
    js.get().doTest("bar", JSAttributeList.AccessType.PACKAGE_LOCAL, "",
                    new JSParameterInfo("p1", "int", "", "100", NEW_PARAMETER),
                    new JSParameterInfo("p2", "String", "abc", "\"def\"", NEW_PARAMETER),
                    new JSParameterInfo("p3", "Boolean", "false", "", NEW_PARAMETER),
                    new JSParameterInfo("p4", "...", "", "", NEW_PARAMETER, false, ECMAL4LanguageDialect.DIALECT_OPTION_HOLDER));
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testRemoveParam1() {
    js.get().doTest("foo", JSAttributeList.AccessType.PRIVATE, "flash.events.EventDispatcher");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testChangeParam1() {
    js.get().withRefactorSuperMethod(() -> js.get().doTest("renamed", JSAttributeList.AccessType.PACKAGE_LOCAL, "Boolean",
                                                  new JSParameterInfo("i2", "Number", "", "", 1),
                                                  new JSParameterInfo("sss", "String", "\"abc\"", "", 0),
                                                  new JSParameterInfo("o", "flash.events.EventDispatcher", "FOO", "", 2),
                                                  new JSParameterInfo("rest2", "...", "", "", 3, false, ECMAL4LanguageDialect.DIALECT_OPTION_HOLDER)),
                                     false);
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testProp1() {
    js.get().doTest("v2", JSAttributeList.AccessType.PROTECTED, "Number");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testProp2() {
    js.get().doTest("v2", JSAttributeList.AccessType.PROTECTED, "void",
                    new JSParameterInfo("value", "Number", "", "", 0));
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testPropagate1() {
    js.get().doDefaultTest((rootDir, rootAfter) -> {
      js.get().assertPropagationCandidates(new String[]{"bar", "zzz", "abc", "nopropagate"});
      js.get().performRefactoring("foo", JSAttributeList.AccessType.PACKAGE_LOCAL, "",
                                  new JSParameterInfo("s", "String", "", "", 0),
                                  new JSParameterInfo("added1", "Number", "", "added1def", NEW_PARAMETER),
                                  new JSParameterInfo("added2", "Object", "", "added2def", NEW_PARAMETER));
    });
  }

  @Test
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
    js.get().doTestConflicts("bar", JSAttributeList.AccessType.PACKAGE_LOCAL, "String", conflicts,
                             new JSParameterInfo("p1", "String", "", "", 0), // p1
                             new JSParameterInfo("p1", "String", "", "", 1), // p2->p1
                             new JSParameterInfo("p1", "String", "", "a", NEW_PARAMETER),
                             new JSParameterInfo("p2", "String", "", "b", NEW_PARAMETER),
                             new JSParameterInfo("p3", "String", "", "c", NEW_PARAMETER));
  }

  @Test
  public void testConflicts2() {
    String[] conflicts = new String[]{
      "There is already a local variable value2 in the property From.prop. It will conflict with the renamed parameter.",
      "Property From.prop with private visibility won't be accessible from function ttt()",
      "Class From already contains a field _prop2"
    };
    js.get().doTestConflicts("prop2", JSAttributeList.AccessType.PRIVATE, "*", conflicts,
                             new JSParameterInfo("value2", "", "", "foo", 0));
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testMxml() {
    js.get().doDefaultTest((rootDir, rootAfter) -> {
      js.get().assertPropagationCandidates(new String[]{"ref", "aaa"});
      js.get().performRefactoring("abc2", JSAttributeList.AccessType.PACKAGE_LOCAL, "",
             new JSParameterInfo("s", "String", "", "\"def\"", NEW_PARAMETER),
             new JSParameterInfo("p", "int", "", "0", NEW_PARAMETER),
             new JSParameterInfo("z", "Object", "", "this", NEW_PARAMETER));
    });
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testExistingSignature() {
    js.get().doTest("foo", JSAttributeList.AccessType.PACKAGE_LOCAL, "",
                    new JSParameterInfo("p", "int", "15", "2000", NEW_PARAMETER),
                    new JSParameterInfo("s", "String", "null", "\"abcde\"", NEW_PARAMETER));
  }

  @Test
  public void testAddParam4() {
    js.get().doTest("foo", JSAttributeList.AccessType.PACKAGE_LOCAL, "void",
                    new JSParameterInfo("i", "int", "", "777", NEW_PARAMETER),
                    new JSParameterInfo("p", "String", "\"default\"", "", 0),
                    new JSParameterInfo("z", "Test", "null", "", NEW_PARAMETER));
  }

  @Test
  public void testAddParam5() {
    js.get().doTest("foo", JSAttributeList.AccessType.PACKAGE_LOCAL, "void",
                    new JSParameterInfo("p", "String", "", "\"abc\"", NEW_PARAMETER),
                    new JSParameterInfo("args", "...", "", "", 0, false, ECMAL4LanguageDialect.DIALECT_OPTION_HOLDER));
  }

  @Test
  public void testNested() {
    js.get().doTest("nested2", JSAttributeList.AccessType.PACKAGE_LOCAL, "int",
                    new JSParameterInfo("p", "String", "", "\"abc\"", NEW_PARAMETER));
  }

  @Test
  public void testSuperConstructorCall() {
    js.get().doTest("From", JSAttributeList.AccessType.PACKAGE_LOCAL, "",
                    new JSParameterInfo("p", "String", "", "", 0),
                    new JSParameterInfo("b", "Boolean", "", "true", NEW_PARAMETER));
  }

  @Test
  public void testSuperConstructorCall2() {
    js.get().doDefaultTest((rootDir, rootAfter) -> {
      js.get().assertPropagationCandidates(new String[]{"B"});
      js.get().performRefactoring("A", JSAttributeList.AccessType.PUBLIC, "",
             new JSParameterInfo("p", "String", "", "", 0),
             new JSParameterInfo("b", "Boolean", "", "true", NEW_PARAMETER));
    });
  }

  @Test
  public void testNamespace() {
    js.get().doTest("foo2", JSAttributeList.AccessType.PUBLIC, "", new JSParameterInfo("p", "", "", "", 0));
  }

  @Test
  public void testNamespace2() {
    js.get().doTest("foo2", JSAttributeList.AccessType.PRIVATE, "", new JSParameterInfo("p", "", "", "", 0));
  }

  @Test
  public void testAddParam6() {
    js.get().doDefaultTest((rootDir, rootAfter) -> {
      js.get().assertPropagationCandidates(new String[]{"A", "B"});
      js.get().performRefactoring("doSmth", JSAttributeList.AccessType.PACKAGE_LOCAL, "",
             new JSParameterInfo("s", "String", "", "\"abc\"", NEW_PARAMETER),
             new JSParameterInfo("args", "...", "", "", 0, false, ECMAL4LanguageDialect.DIALECT_OPTION_HOLDER));
    });
  }

  @Test
  public void testAddParam7() {
    js.get().doTest("doSmth", JSAttributeList.AccessType.PACKAGE_LOCAL, "",
                    new JSParameterInfo("s", "String", "", "", 1),
                    new JSParameterInfo("i", "int", "", "", 0),
                    new JSParameterInfo("b", "Type", "", "def", NEW_PARAMETER),
                    new JSParameterInfo("i2", "int", "0", "", 2),
                    new JSParameterInfo("p", "From", "null", "", 3));
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testNoPropagateToSdkInheritor() {
    FlexTestUtils.addLibrary(js.get().getFixture().getModule(), "Lib", js.get().getTestDataPath() + js.get().getTestRoot() + js.get().getTestName(false), "Flex_small.swc", null, null);
    Module module = js.get().getFixture().getModule();
    Disposer.register(js.get().getFixture().getTestRootDisposable(), () -> FlexTestUtils.removeLibrary(module, "Lib"));
    js.get().doDefaultTest((rootDir, rootAfter) -> {
      js.get().assertPropagationCandidates(new String[]{"bar", "listener"});
      js.get().performRefactoring("abc", JSAttributeList.AccessType.PACKAGE_LOCAL, "");
    });
  }

  private void doTestInaccessible() {
    try {
      js.get().doTest("", JSAttributeList.AccessType.PACKAGE_LOCAL, "", JSParameterInfo.EMPTY_ARRAY);
      fail("Refactoring should be inaccessible");
    }
    catch (CommonRefactoringUtil.RefactoringErrorHintException e) {
      // expected
    }
  }

  @Test
  public void testAnonymousFunction1() {
    doTestInaccessible();
  }

  @Test
  public void testAnonymousFunction2() {
    doTestInaccessible();
  }

  @Test
  public void testAnonymousFunction3() {
    doTestInaccessible();
  }

  @Test
  public void testAnonymousFunction4() {
    js.get().doTest("v2", JSAttributeList.AccessType.PACKAGE_LOCAL, "String", new JSParameterInfo("b2", "String", "", "", 1),
                    new JSParameterInfo("a2", "int", "", "", 0), new JSParameterInfo("c", "Boolean", "", "false", NEW_PARAMETER));
  }

  @Test
  public void testAnonymousFunction5() {
    js.get().doDefaultTest((rootDir, rootAfter) -> {
      js.get().assertPropagationCandidates(new String[]{"usage1", "usage2"});
      js.get().performRefactoring("sayLoud", JSAttributeList.AccessType.PUBLIC, "void",
             new JSParameterInfo("message", "String", "", "", 0),
             new JSParameterInfo("loud", "Boolean", "true", "false", NEW_PARAMETER));
    });
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testEventHandlerCall() {
    js.get().doTest("", JSAttributeList.AccessType.PACKAGE_LOCAL, "void",
                    new JSParameterInfo("i", "int", "", "1000", NEW_PARAMETER));
  }

  @Test
  public void testPropagateToFunctionExpression() {
    js.get().doDefaultTest((rootDir, rootAfter) -> {
      js.get().assertPropagationCandidates(new String[]{"f1"});
      js.get().performRefactoring("", JSAttributeList.AccessType.PACKAGE_LOCAL, "",
                                  new JSParameterInfo("i", "int", "", "100", NEW_PARAMETER));
    });
  }

  @Test
  public void testImportsForAgrumentsAndInitializers() {
    js.get().doTest("foo", JSAttributeList.AccessType.PACKAGE_LOCAL, "",
                    new JSParameterInfo("p1", "com.Foo", "aaa.A.SIZE", "new com.Foo()", NEW_PARAMETER),
                    new JSParameterInfo("p2", "com.Bar", "bbb.B.ourLength", "com.Bar.SIZE", NEW_PARAMETER),
                    new JSParameterInfo("p3", "String", "", "com.Foo.MESSAGE", NEW_PARAMETER),
                    new JSParameterInfo("p4", "bar.Zzz", "", "bar.Zzz.func(new bar.Yyy(), uuu.glob(com.Const))", NEW_PARAMETER),
                    new JSParameterInfo("p5", "bar.Yyy", "", "unresolved", NEW_PARAMETER));
  }

  @Test
  public void testIncompatibleOverrideConflict() {
    String[] conflicts = new String[]{
      JavaScriptBundle.message("change.signature.conflict.incompatible.override", "method B.foo()", "method A.foo(int)", "Method B.foo()")
    };
    js.get().doTestConflicts("foo2", JSAttributeList.AccessType.PUBLIC, "", conflicts, new JSParameterInfo("j", "int", "", "", 0));
  }

  @Test
  public void testIncompatibleImplementationConflict() {
    String[] conflicts = new String[]{
      JavaScriptBundle.message("change.signature.conflict.incompatible.implementation", "method B.foo()", "method A.foo(int)", "Method B.foo()")
    };
    js.get().doTestConflicts("foo", JSAttributeList.AccessType.PUBLIC, "", conflicts, new JSParameterInfo("j", "int", "", "", 0));
  }

  @Test
  public void testIncompatibleImplementation() {
    js.get().myIgnoreConflicts = true;
    js.get().doTest("foo2", JSAttributeList.AccessType.PUBLIC, "", new JSParameterInfo("j", "int", "", "", 0));
  }
}