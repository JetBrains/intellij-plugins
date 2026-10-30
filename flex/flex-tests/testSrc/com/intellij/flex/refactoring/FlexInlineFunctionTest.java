package com.intellij.flex.refactoring;

import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.refactoring.JSInlineVarOrFunctionFixture;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;
import static com.intellij.lang.javascript.refactoring.JSInlineVarOrFunctionFixture.jsInlineVarOrFunctionFixture;

@TestApplication
public class FlexInlineFunctionTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSInlineVarOrFunctionFixture> js =
    jsInlineVarOrFunctionFixture(projectFixture, new JSFixtureOptions(FlexTestUtils.getTestDataPath("as_refactoring/inlineFunction/"))
        .withModuleType(FlexModuleType.MODULE_TYPE_ID));

  @BeforeEach
  void setUp() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    FlexTestUtils.allowFlexVfsRootsFor(myFixture.getTestRootDisposable(), "as_refactoring/inlineFunction/");
    FlexTestUtils.setupFlexSdk(js.get().getFixture().getModule(), js.get().getTestName(false), getClass(), myFixture.getTestRootDisposable());
  }

  private void defaultTest() {
    js.get().doTest(js.get().getTestName(false), "js2");
  }

  @Test
  public void testDefaultParams() {
    defaultTest();
  }

  @Test
  public void testJustStatements2() {
    defaultTest();
  }

  @Test
  public void testJustStatements2_2() {
    defaultTest();
  }

  @Test
  public void testJustStatements2_3() {
    defaultTest();
  }

  @Test
  public void testJustStatements2_4() {
    js.get().doTestFailure(js.get().getTestName(false), "js2", JavaScriptBundle.message("javascript.refactoring.cannot.inline.function.with.multiple.returns"));
  }

  @Test
  public void testJustStatements2_5() {
    defaultTest();
  }

  @Test
  public void testJustStatements2_6() {
    defaultTest();
  }

  @Test
  public void testJustOneCall() {
    js.get().doTest(new String[]{js.get().getTestName(false) + ".js2"}, true);
  }

  @Test
  public void testJustStatementsInMxml() {
    js.get().doTest(js.get().getTestName(false), "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testInsideAttribute() {
    js.get().doTest(js.get().getTestName(false), "mxml");
    js.get().doTest(js.get().getTestName(false) + "_2", "mxml");
    js.get().doTest(js.get().getTestName(false) + "_3", "mxml");
  }

  @Test
  public void testReplacingThis() {
    defaultTest();
  }

  @Test
  public void testNoReplacingThis() {
    defaultTest();
  }

  @Test
  public void testQualifyStatics() {
    defaultTest();
  }

  @Test
  public void testStaticCall() {
    defaultTest();
  }

  @Test
  public void testHasRestParams() {
    js.get().doTestFailure(js.get().getTestName(false), "js2",
                           JavaScriptBundle.message("javascript.refactoring.cannot.inline.function.referencing.rest.parameter"));
  }

  @Test
  public void testConstructor() {
    shouldFail(JavaScriptBundle.message("javascript.refactoring.cannot.inline.constructor"));
  }

  @Test
  public void testConstructor2() {
    shouldFail(JavaScriptBundle.message("javascript.refactoring.cannot.inline.constructor"));
  }

  private void shouldFail(String reason) {
    js.get().doTestFailure(js.get().getTestName(false), "js2", reason);
  }

  @Test
  public void testMethodInHierarchy() {
    String reason = JavaScriptBundle.message("javascript.refactoring.cannot.inline.overrided.or.overridden.method");
    js.get().doTestFailure(js.get().getTestName(false) + 1, "js2", reason);
    js.get().doTestFailure(js.get().getTestName(false) + 2, "js2", reason);
    js.get().doTestFailure(js.get().getTestName(false) + 3, "js2", reason);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testMethodInHierarchyMxml() {
    js.get().doTestFailure(new String[]{js.get().getTestName(false) + ".mxml", js.get().getTestName(false) + ".js2"},
                           JavaScriptBundle.message("javascript.refactoring.cannot.inline.overrided.or.overridden.method"));
  }

  @Test
  public void testInterfaceMethod() {
    shouldFail(JavaScriptBundle.message("javascript.refactoring.cannot.inline.interface.method"));
  }

  @Test
  public void testMethodFromExternalLibrary() {
    FlexTestUtils.addLibrary(js.get().getFixture().getModule(), "library", js.get().getTestDataPath(), "ExternalLib.swc", "ExternalLib.zip", null);
    Module module = js.get().getFixture().getModule();
    Disposer.register(js.get().getFixture().getTestRootDisposable(), () -> FlexTestUtils.removeLibrary(module, "library"));

    shouldFail(JavaScriptBundle.message("javascript.refactoring.cannot.inline.function.defined.in.library"));
  }

  @Test
  public void testNonCallUsage() {
    defaultTest();
  }

  @Test
  public void testConflicts1() {
    String[] conflicts = new String[]{
      "Field Foo.inter with internal visibility won't be accessible from method OtherPackage.otherPackageFunc()",
      "Field Foo.inter with internal visibility won't be accessible from method SubClass.subclassFunc()",
      "Field Foo.priv with private visibility won't be accessible from method Neighbour.neighbourFunc()",
      "Field Foo.priv with private visibility won't be accessible from method OtherPackage.otherPackageFunc()",
      "Field Foo.priv with private visibility won't be accessible from method SubClass.subclassFunc()",
      "Field Foo.prot with protected visibility won't be accessible from method Neighbour.neighbourFunc()",
      "Field Foo.prot with protected visibility won't be accessible from method OtherPackage.otherPackageFunc()",
      "Method Foo.fInter() with internal visibility won't be accessible from method OtherPackage.otherPackageFunc()",
      "Method Foo.fInter() with internal visibility won't be accessible from method SubClass.subclassFunc()",
      "Method Foo.fPriv() with private visibility won't be accessible from method Neighbour.neighbourFunc()",
      "Method Foo.fPriv() with private visibility won't be accessible from method OtherPackage.otherPackageFunc()",
      "Method Foo.fPriv() with private visibility won't be accessible from method SubClass.subclassFunc()",
      "Method Foo.fProt() with protected visibility won't be accessible from method Neighbour.neighbourFunc()",
      "Method Foo.fProt() with protected visibility won't be accessible from method OtherPackage.otherPackageFunc()"
    };
    js.get().doTestConflicts(js.get().getTestName(false), "js2", conflicts);
  }

  @Test
  public void testConflicts2() {
    String[] conflicts = new String[]{
      "Field Foo.u with protected visibility won't be accessible from inner class Bar",
      "Field Foo.u with protected visibility won't be accessible from method Bar.ff()",
      "Field Foo.v with private visibility won't be accessible from inner class Bar",
      "Field Foo.v with private visibility won't be accessible from method Bar.ff()"
    };
    js.get().doTestConflicts(js.get().getTestName(false), "js2", conflicts);
  }

  @Test
  public void testConflicts3() {
    String[] conflicts = new String[]{
      "Field Conflicts3_2.t with private visibility won't be accessible from constructor Foo.Foo(int)",
      "Method Conflicts3_2.fff() with private visibility won't be accessible from constructor Foo.Foo(int)"
    };
    js.get().doTestConflicts(new String[]{js.get().getTestName(false) + ".js2", js.get().getTestName(false) + "_2.mxml"}, conflicts);
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testConflicts4() {
    String[] conflicts = new String[]{
      "Field Foo.abc with private visibility won't be accessible from function &lt;anonymous&gt;(*) in class Conflicts4",
      "Method Foo.zz() with protected visibility won't be accessible from function &lt;anonymous&gt;(*) in class Conflicts4"
    };
    js.get().doTestConflicts(new String[]{js.get().getTestName(false) + ".mxml", js.get().getTestName(false) + "_2.js2"}, conflicts);
  }

  @Test
  public void testConflicts5() {
    String[] conflicts = new String[]{
      "Field Conflicts5.p with private visibility won't be accessible from file Conflicts5.js2",
      "Field Conflicts5.p with private visibility won't be accessible from file Conflicts5_2.js2"};
    js.get().doTestConflicts(new String[]{js.get().getTestName(false) + ".js2", js.get().getTestName(false) + "_2.js2"}, conflicts);
  }
}
