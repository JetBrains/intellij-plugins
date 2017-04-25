package com.intellij.flex.refactoring;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.javascript.flex.mxml.schema.FlexSchemaHandler;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.refactoring.inlineFunction.JSInlineFunctionTestBase;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

public class FlexInlineFunctionTest extends JSInlineFunctionTestBase {

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("as_refactoring/inlineFunction/");
  }

  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  protected void setUp() throws Exception {
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexSchemaHandler.class.getResource("z.xsd"))),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));
    super.setUp();
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass());
  }

  private void defaultTest() throws Exception {
    doTest(getTestName(false), "js2", SUCCESS);
  }

  public void testDefaultParams() throws Exception {
    defaultTest();
  }

  public void testJustStatements2() throws Exception {
    defaultTest();
  }

  public void testJustStatements2_2() throws Exception {
    defaultTest();
  }

  public void testJustStatements2_3() throws Exception {
    defaultTest();
  }

  public void testJustStatements2_4() throws Exception {
    shouldFail();
  }

  public void testJustStatements2_5() throws Exception {
    defaultTest();
  }

  public void testJustStatements2_6() throws Exception {
    defaultTest();
  }

  public void testJustOneCall() throws Exception {
    doTest(new String[]{getTestName(false) + ".js2"}, SUCCESS, NO_CONFLICTS, true);
  }

  public void testJustStatementsInMxml() throws Exception {
    doTest(getTestName(false), "mxml", SUCCESS);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testInsideAttribute() throws Exception {
    doTest(getTestName(false), "mxml", SUCCESS);
    doTest(getTestName(false) + "_2", "mxml", SUCCESS);
    doTest(getTestName(false) + "_3", "mxml", SUCCESS);
  }

  public void testReplacingThis() throws Exception {
    defaultTest();
  }

  public void testNoReplacingThis() throws Exception {
    defaultTest();
  }

  public void testQualifyStatics() throws Exception {
    defaultTest();
  }

  public void testStaticCall() throws Exception {
    defaultTest();
  }


  public void testHasRestParams() throws Exception {
    shouldFail();
  }

  public void testConstructor() throws Exception {
    shouldFail("Can not inline constructor");
  }

  public void testConstructor2() throws Exception {
    shouldFail("Can not inline constructor");
  }

  private void shouldFail() throws Exception {
    doTest(getTestName(false), "js2", FAIL_ANY_REASON);
  }

  private void shouldFail(String reason) throws Exception {
    doTest(getTestName(false), "js2", reason);
  }

  public void testMethodInHierarchy() throws Exception {
    String reason = "Can not inline method that participates in hierarchy";
    doTest(getTestName(false) + 1, "js2", reason);
    doTest(getTestName(false) + 2, "js2", reason);
    doTest(getTestName(false) + 3, "js2", reason);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testMethodInHierarchyMxml() throws Exception {
    doTest(new String[]{getTestName(false) + ".mxml", getTestName(false) + ".js2"}, "Can not inline method that participates in hierarchy",
           NO_CONFLICTS);
  }

  public void testInterfaceMethod() throws Exception {
    shouldFail("Can not inline interface method");
  }

  public void testMethodFromExternalLibrary() throws Exception {
    myAfterCommitRunnable =
      () -> FlexTestUtils.addLibrary(myModule, "library", getTestDataPath(), "ExternalLib.swc", "ExternalLib.zip", null);

    shouldFail("Can not inline function defined in external library");
  }

  public void testNonCallUsage() throws Exception {
    shouldFail("Can not inline non call usage");
  }

  public void testConflicts1() throws Exception {
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

  public void testConflicts2() throws Exception {
    String[] conflicts = new String[]{
      "Field Foo.u with protected visibility won't be accessible from inner class Bar",
      "Field Foo.u with protected visibility won't be accessible from method Bar.ff()",
      "Field Foo.v with private visibility won't be accessible from inner class Bar",
      "Field Foo.v with private visibility won't be accessible from method Bar.ff()"
    };
    doTestConflicts(getTestName(false), "js2", conflicts);
  }

  public void testConflicts3() throws Exception {
    String[] conflicts = new String[]{
      "Field Conflicts3_2.t with private visibility won't be accessible from constructor Foo.Foo(int)",
      "Method Conflicts3_2.fff() with private visibility won't be accessible from constructor Foo.Foo(int)"
    };
    doTestConflicts(new String[]{getTestName(false) + ".js2", getTestName(false) + "_2.mxml"}, conflicts);
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testConflicts4() throws Exception {
    String[] conflicts = new String[]{
      "Field Foo.abc with private visibility won't be accessible from function &lt;anonymous&gt;(*) in class Conflicts4",
      "Method Foo.zz() with protected visibility won't be accessible from function &lt;anonymous&gt;(*) in class Conflicts4"
    };
    doTestConflicts(new String[]{getTestName(false) + ".mxml", getTestName(false) + "_2.js2"}, conflicts);
  }

  public void testConflicts5() throws Exception {
    String[] conflicts = new String[]{
      "Field Conflicts5.p with private visibility won't be accessible from file Conflicts5.js2",
      "Field Conflicts5.p with private visibility won't be accessible from file Conflicts5_2.js2"};
    doTestConflicts(new String[]{getTestName(false) + ".js2", getTestName(false) + "_2.js2"}, conflicts);
  }
}
