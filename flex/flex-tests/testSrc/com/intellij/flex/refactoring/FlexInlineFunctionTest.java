package com.intellij.flex.refactoring;

import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.editor.FlexProjectDescriptor;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.refactoring.JSInlineVarOrFunctionTestBase;
import com.intellij.openapi.util.Disposer;
import com.intellij.testFramework.LightProjectDescriptor;

public class FlexInlineFunctionTest extends JSInlineVarOrFunctionTestBase {

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("as_refactoring/inlineFunction/");
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return FlexProjectDescriptor.DESCRIPTOR;
  }

  @Override
  protected void setUp() throws Exception {

    super.setUp();
    FlexTestUtils.allowFlexVfsRootsFor(myFixture.getTestRootDisposable(), "as_refactoring/inlineFunction/");
    FlexTestUtils.setupFlexSdk(getModule(), getTestName(false), getClass(), myFixture.getTestRootDisposable());
  }

  private void defaultTest() {
    doTest(getTestName(false), "js2");
  }

  public void testDefaultParams() {
    defaultTest();
  }

  public void testJustStatements2() {
    defaultTest();
  }

  public void testJustStatements2_2() {
    defaultTest();
  }

  public void testJustStatements2_3() {
    defaultTest();
  }

  public void testJustStatements2_4() {
    doTestFailure(getTestName(false), "js2", JavaScriptBundle.message("javascript.refactoring.cannot.inline.function.with.multiple.returns"));
  }

  public void testJustStatements2_5() {
    defaultTest();
  }

  public void testJustStatements2_6() {
    defaultTest();
  }

  public void testJustOneCall() {
    doTest(new String[]{getTestName(false) + ".js2"}, true);
  }

  public void testJustStatementsInMxml() {
    doTest(getTestName(false), "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testInsideAttribute() {
    doTest(getTestName(false), "mxml");
    doTest(getTestName(false) + "_2", "mxml");
    doTest(getTestName(false) + "_3", "mxml");
  }

  public void testReplacingThis() {
    defaultTest();
  }

  public void testNoReplacingThis() {
    defaultTest();
  }

  public void testQualifyStatics() {
    defaultTest();
  }

  public void testStaticCall() {
    defaultTest();
  }


  public void testHasRestParams() {
    doTestFailure(getTestName(false), "js2",
                  JavaScriptBundle.message("javascript.refactoring.cannot.inline.function.referencing.rest.parameter"));
  }

  public void testConstructor() {
    shouldFail(JavaScriptBundle.message("javascript.refactoring.cannot.inline.constructor"));
  }

  public void testConstructor2() {
    shouldFail(JavaScriptBundle.message("javascript.refactoring.cannot.inline.constructor"));
  }

  private void shouldFail(String reason) {
    doTestFailure(getTestName(false), "js2", reason);
  }

  public void testMethodInHierarchy() {
    String reason = JavaScriptBundle.message("javascript.refactoring.cannot.inline.overrided.or.overridden.method");
    doTestFailure(getTestName(false) + 1, "js2", reason);
    doTestFailure(getTestName(false) + 2, "js2", reason);
    doTestFailure(getTestName(false) + 3, "js2", reason);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testMethodInHierarchyMxml() {
    doTestFailure(new String[]{getTestName(false) + ".mxml", getTestName(false) + ".js2"},
                  JavaScriptBundle.message("javascript.refactoring.cannot.inline.overrided.or.overridden.method"));
  }

  public void testInterfaceMethod() {
    shouldFail(JavaScriptBundle.message("javascript.refactoring.cannot.inline.interface.method"));
  }

  public void testMethodFromExternalLibrary() {
    FlexTestUtils.addLibrary(getModule(), "library", getTestDataPath(), "ExternalLib.swc", "ExternalLib.zip", null);
    Disposer.register(myFixture.getTestRootDisposable(), () -> FlexTestUtils.removeLibrary(getModule(), "library"));

    shouldFail(JavaScriptBundle.message("javascript.refactoring.cannot.inline.function.defined.in.library"));
  }

  public void testNonCallUsage() {
    defaultTest();
  }

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
    doTestConflicts(getTestName(false), "js2", conflicts);
  }

  public void testConflicts2() {
    String[] conflicts = new String[]{
      "Field Foo.u with protected visibility won't be accessible from inner class Bar",
      "Field Foo.u with protected visibility won't be accessible from method Bar.ff()",
      "Field Foo.v with private visibility won't be accessible from inner class Bar",
      "Field Foo.v with private visibility won't be accessible from method Bar.ff()"
    };
    doTestConflicts(getTestName(false), "js2", conflicts);
  }

  public void testConflicts3() {
    String[] conflicts = new String[]{
      "Field Conflicts3_2.t with private visibility won't be accessible from constructor Foo.Foo(int)",
      "Method Conflicts3_2.fff() with private visibility won't be accessible from constructor Foo.Foo(int)"
    };
    doTestConflicts(new String[]{getTestName(false) + ".js2", getTestName(false) + "_2.mxml"}, conflicts);
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testConflicts4() {
    String[] conflicts = new String[]{
      "Field Foo.abc with private visibility won't be accessible from function &lt;anonymous&gt;(*) in class Conflicts4",
      "Method Foo.zz() with protected visibility won't be accessible from function &lt;anonymous&gt;(*) in class Conflicts4"
    };
    doTestConflicts(new String[]{getTestName(false) + ".mxml", getTestName(false) + "_2.js2"}, conflicts);
  }

  public void testConflicts5() {
    String[] conflicts = new String[]{
      "Field Conflicts5.p with private visibility won't be accessible from file Conflicts5.js2",
      "Field Conflicts5.p with private visibility won't be accessible from file Conflicts5_2.js2"};
    doTestConflicts(new String[]{getTestName(false) + ".js2", getTestName(false) + "_2.js2"}, conflicts);
  }
}
