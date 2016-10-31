package com.intellij.lang.javascript;

import com.intellij.flex.FlexTestUtils;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.javascript.flex.mxml.schema.FlexSchemaHandler;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.refactoring.changeSignature.JSParameterInfo;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

public class FlexChangeSignatureTest extends JSChangeSignatureTestBase {
  @Override
  public void setUp() throws Exception {
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexSchemaHandler.class.getResource("z.xsd"))),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));
    super.setUp();
  }

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
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass());
  }


  public void testAddParam1() throws Exception {
    doTest("bar", JSAttributeList.AccessType.PUBLIC, "int", new String[]{"zzz"},
           new JSParameterInfo("stringParam", "String", "", "\"def\"", -1));
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testAddParam2() throws Exception {
    doTest("blablabala__12", JSAttributeList.AccessType.PACKAGE_LOCAL, "", new String[]{"zzz"},
           new JSParameterInfo("p", "mx.messaging.AbstractConsumer", "", "CONS", -1),
           new JSParameterInfo("p2", "flash.events.EventDispatcher", "null", "DISP", -1));
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testAddParam3() throws Exception {
    doTest("bar", JSAttributeList.AccessType.PACKAGE_LOCAL, "", new String[]{"zzz"},
           new JSParameterInfo("p1", "int", "", "100", -1),
           new JSParameterInfo("p2", "String", "abc", "\"def\"", -1),
           new JSParameterInfo("p3", "Boolean", "false", "", -1),
           new JSParameterInfo("p4", "...", "", "", -1));
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testRemoveParam1() throws Exception {
    doTest("foo", JSAttributeList.AccessType.PRIVATE, "flash.events.EventDispatcher", new String[]{"zzz"});
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testChangeParam1() throws Exception {
    doTest("renamed", JSAttributeList.AccessType.PACKAGE_LOCAL, "Boolean", new String[]{"bar"},
           new JSParameterInfo("i2", "Number", "", "", 1),
           new JSParameterInfo("sss", "String", "\"abc\"", "", 0),
           new JSParameterInfo("o", "flash.events.EventDispatcher", "FOO", "", 2),
           new JSParameterInfo("rest2", "...", "", "", 3));
  }


  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testProp1() throws Exception {
    doTest("v2", JSAttributeList.AccessType.PROTECTED, "Number", ArrayUtil.EMPTY_STRING_ARRAY);
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testProp2() throws Exception {
    doTest("v2", JSAttributeList.AccessType.PROTECTED, "void", ArrayUtil.EMPTY_STRING_ARRAY,
           new JSParameterInfo("value", "Number", "", "", 0));
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testPropagate1() throws Exception {
    doTest("foo", JSAttributeList.AccessType.PACKAGE_LOCAL, "",
           new String[]{"bar", "zzz", "abc", "nopropagate"},
           new JSParameterInfo("s", "String", "", "", 0),
           new JSParameterInfo("added1", "Number", "", "added1def", -1),
           new JSParameterInfo("added2", "Object", "", "added2def", -1));
  }

  public void testConflicts1() throws Exception {
    String[] conflicts = new String[]{
      "There is already a parameter p1 in method From.foo(String, String). It will conflict with the new parameter.",
      "There is already a parameter p1 in the method From.foo(String, String). It will conflict with the renamed parameter.",
      "There is already a variable p2 in method From.foo(String, String). It will conflict with the new parameter.",
      "There is already a variable p3 in method FromEx.foo(String, String). It will conflict with the new parameter.",
      "There is already a variable p1 in method From.pp(). It will conflict with the new parameter.",
      "There is already a variable p2 in method From.pp(). It will conflict with the new parameter.",
      "There is already a variable p3 in method From.pp(). It will conflict with the new parameter.",
      "There is already a variable p1 in function pp2(). It will conflict with the new parameter.",
      "Class From already contains a method bar()",
      "Method FromEx.foo(String, String) with internal visibility won't be accessible from function zz()",
      "Method From.foo(String, String) with internal visibility won't be able to participate in hierarchy"
    };
    doTestConflicts("bar", JSAttributeList.AccessType.PACKAGE_LOCAL, "String", conflicts, new String[]{"pp", "zz"},
                    new JSParameterInfo("p1", "String", "", "", 0), // p1
                    new JSParameterInfo("p1", "String", "", "", 1), // p2->p1
                    new JSParameterInfo("p1", "String", "", "a", -1),
                    new JSParameterInfo("p2", "String", "", "b", -1),
                    new JSParameterInfo("p3", "String", "", "c", -1));
  }

  public void testConflicts2() throws Exception {
    String[] conflicts = new String[]{
      "There is already a variable value2 in the property From.prop. It will conflict with the renamed parameter.",
      "Property From.prop with private visibility won't be accessible from function ttt()",
      "Class From already contains a field _prop2"
    };
    doTestConflicts("prop2", JSAttributeList.AccessType.PRIVATE, "*", conflicts, ArrayUtil.EMPTY_STRING_ARRAY,
                    new JSParameterInfo("value2", "", "", "foo", 0));
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testMxml() throws Exception {
    doTest("abc2", JSAttributeList.AccessType.PACKAGE_LOCAL, "",
           new String[]{"ref", "aaa"},
           new JSParameterInfo("s", "String", "", "\"def\"", -1),
           new JSParameterInfo("p", "int", "", "0", -1),
           new JSParameterInfo("z", "Object", "", "this", -1));
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testExistingSignature() throws Exception {
    doTest("foo", JSAttributeList.AccessType.PACKAGE_LOCAL, "",
           new String[]{"bar"},
           new JSParameterInfo("p", "int", "15", "2000", -1),
           new JSParameterInfo("s", "String", "null", "\"abcde\"", -1));
  }

  public void testAddParam4() throws Exception {
    doTest("foo", JSAttributeList.AccessType.PACKAGE_LOCAL, "void",
           new String[]{"zzz"},
           new JSParameterInfo("i", "int", "", "777", -1),
           new JSParameterInfo("p", "String", "\"default\"", "", 0),
           new JSParameterInfo("z", "Test", "null", "", -1));
  }

  public void testAddParam5() throws Exception {
    doTest("foo", JSAttributeList.AccessType.PACKAGE_LOCAL, "void",
           new String[]{"zzz"},
           new JSParameterInfo("p", "String", "", "\"abc\"", -1),
           new JSParameterInfo("args", "...", "", "", 0));
  }

  public void testNested() throws Exception {
    doTest("nested2", JSAttributeList.AccessType.PACKAGE_LOCAL, "int",
           new String[]{"test"},
           new JSParameterInfo("p", "String", "", "\"abc\"", -1));
  }

  public void testSuperConstructorCall() throws Exception {
    doTest("From", JSAttributeList.AccessType.PACKAGE_LOCAL, "",
           new String[]{"FromEx"},
           new JSParameterInfo("p", "String", "", "", 0),
           new JSParameterInfo("b", "Boolean", "", "true", -1));
  }

  public void testSuperConstructorCall2() throws Exception {
    doTest("A", JSAttributeList.AccessType.PUBLIC, "",
           new String[]{"B"},
           new JSParameterInfo("p", "String", "", "", 0),
           new JSParameterInfo("b", "Boolean", "", "true", -1));
  }

  public void testNamespace() throws Exception {
    doTest("foo2", JSAttributeList.AccessType.PUBLIC, "", new String[]{"bar", "barZ"}, new JSParameterInfo("p", "", "", "", 0));
  }

  public void testNamespace2() throws Exception {
    doTest("foo2", JSAttributeList.AccessType.PRIVATE, "", new String[]{"bar"}, new JSParameterInfo("p", "", "", "", 0));
  }

  public void testAddParam6() throws Exception {
    doTest("doSmth", JSAttributeList.AccessType.PACKAGE_LOCAL, "",
           new String[]{"A", "B"},
           new JSParameterInfo("s", "String", "", "\"abc\"", -1),
           new JSParameterInfo("args", "...", "", "", 0));
  }

  public void testAddParam7() throws Exception {
    doTest("doSmth", JSAttributeList.AccessType.PACKAGE_LOCAL, "",
           new String[]{"zz"},
           new JSParameterInfo("s", "String", "", "", 1),
           new JSParameterInfo("i", "int", "", "", 0),
           new JSParameterInfo("b", "Type", "", "def", -1),
           new JSParameterInfo("i2", "int", "0", "", 2),
           new JSParameterInfo("p", "From", "null", "", 3));
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testNoPropagateToSdkInheritor() throws Exception {
    myAfterCommitRunnable =
      () -> FlexTestUtils.addLibrary(myModule, "Lib", getTestDataPath() + getTestRoot() + getTestName(false), "Flex_small.swc", null, null);
    doTest("abc", JSAttributeList.AccessType.PACKAGE_LOCAL, "", new String[]{"bar", "listener"});
  }

  private void doTestInaccessible() throws Exception {
    try {
      doTest("", JSAttributeList.AccessType.PACKAGE_LOCAL, "", null);
      fail("Refactoring should be inaccessible");
    }
    catch (CommonRefactoringUtil.RefactoringErrorHintException e) {
      // expected
    }
  }

  public void testAnonymousFunction1() throws Exception {
    doTestInaccessible();
  }

  public void testAnonymousFunction2() throws Exception {
    doTestInaccessible();
  }

  public void testAnonymousFunction3() throws Exception {
    doTestInaccessible();
  }

  public void testAnonymousFunction4() throws Exception {
    doTest("v2", JSAttributeList.AccessType.PACKAGE_LOCAL, "String", new String[]{"foo"}, new JSParameterInfo("b2", "String", "", "", 1),
           new JSParameterInfo("a2", "int", "", "", 0), new JSParameterInfo("c", "Boolean", "", "false", -1));
  }

  public void testAnonymousFunction5() throws Exception {
    doTest("sayLoud", JSAttributeList.AccessType.PUBLIC, "void", new String[]{"usage1", "usage2"},
           new JSParameterInfo("message", "String", "", "", 0),
           new JSParameterInfo("loud", "Boolean", "true", "false", -1));
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testEventHandlerCall() throws Exception {
    doTest("", JSAttributeList.AccessType.PACKAGE_LOCAL, "void", ArrayUtil.EMPTY_STRING_ARRAY,
           new JSParameterInfo("i", "int", "", "1000", -1));
  }

  public void testAnonymousFunction6() throws Exception {
    myAfterCommitRunnable = () -> WriteAction.run(() -> {
      String root = getTestRoot() + getTestName(false) + "/module2";
      Module module2 = ModuleManager.getInstance(myProject).newModule(getTestDataPath() + root, getModuleType().getId());
      myModulesToDispose.add(module2);
      PsiTestUtil.addSourceRoot(module2, getVirtualFile(root));
      FlexTestUtils.addFlexModuleDependency(module2, myModule);
    });
    doTestInaccessible();
  }

  public void testPropagateToFunctionExpression() throws Exception {
    doTest("", JSAttributeList.AccessType.PACKAGE_LOCAL, "", new String[]{"f1"},
           new JSParameterInfo("i", "int", "", "100", -1));
  }

  public void testImportsForAgrumentsAndInitializers() throws Exception {
    doTest("foo", JSAttributeList.AccessType.PACKAGE_LOCAL, "", null,
           new JSParameterInfo("p1", "com.Foo", "aaa.A.SIZE", "new com.Foo()", -1),
           new JSParameterInfo("p2", "com.Bar", "bbb.B.ourLength", "com.Bar.SIZE", -1),
           new JSParameterInfo("p3", "String", "", "com.Foo.MESSAGE", -1),
           new JSParameterInfo("p4", "bar.Zzz", "", "bar.Zzz.func(new bar.Yyy(), uuu.glob(com.Const))", -1),
           new JSParameterInfo("p5", "bar.Yyy", "", "unresolved", -1));
  }

  public void testIncompatibleOverrideConflict() throws Exception {
    String[] conflicts = new String[]{
      "Overriding method B.foo() has different number of parameters than refactored method A.foo(int). Method B.foo() will be ignored during refactoring."
    };
    doTestConflicts("foo2", JSAttributeList.AccessType.PUBLIC, "", conflicts, null, new JSParameterInfo("j", "int", "", "", 0));
  }

  public void testIncompatibleImplementationConflict() throws Exception {
    String[] conflicts = new String[]{
      "Implementing method B.foo() has different number of parameters than refactored method A.foo(int). Method B.foo() will be ignored during refactoring."
    };
    doTestConflicts("foo", JSAttributeList.AccessType.PUBLIC, "", conflicts, null, new JSParameterInfo("j", "int", "", "", 0));
  }

  public void testIncompatibleImplementation() throws Exception {
    myIgnoreConflicts = true;
    doTest("foo2", JSAttributeList.AccessType.PUBLIC, "", null, new JSParameterInfo("j", "int", "", "", 0));
  }
}