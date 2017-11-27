package com.intellij.flex.refactoring;

import com.intellij.execution.RunManager;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javascript.JSSupportLoader;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.javascript.flex.mxml.schema.FlexSchemaHandler;
import com.intellij.json.JsonLanguage;
import com.intellij.lang.LanguageNamesValidation;
import com.intellij.lang.javascript.*;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunnerParameters;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.index.JSPackageIndex;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSImportStatement;
import com.intellij.lang.javascript.refactoring.JSInplaceRenameHandler;
import com.intellij.lang.refactoring.NamesValidator;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestUtil;
import com.intellij.util.indexing.FileBasedIndex;

import java.io.File;

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

public class FlexRenameTest extends JSAbstractRenameTest {

  @Override
  protected void setUp() throws Exception {
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexSchemaHandler.class.getResource("z.xsd"))),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));
    super.setUp();
  }

  protected String getBasePath() {
    return "/flex_rename/";
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), getTestRootDisposable());
  }

  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  public void testRenameBundleRef() throws Exception {
    String testName = getTestName(false);
    doTest("MyBundleName2.properties", testName + "_after.js2", testName + ".js2", "MyBundleName.properties");
    assertEquals(1, findRenamedRefsToReferencedElementAtCaret().length);
  }

  public void testJSNamespace() throws Exception {
    String testName = getTestName(false);
    doTest("BBB", testName + "_after.js2", testName + ".js2", testName + "_2.js2");
    assertEquals(5, findRenamedRefsToReferencedElementAtCaret().length);
  }

  public void testJSNamespace2() throws Exception {
    String testName = getTestName(false);
    doTest("BBB", testName + "_after.js2", true, testName + ".js2", testName + "_2.js2");
    assertEquals(3, findRenamedRefsToReferencedElementAtCaret().length);
  }

  public void testJSFunction() throws Exception {
    String testName = getTestName(false);
    doTest("JSFunctionNew", testName + "_after.js2", testName + ".js2");
    assertEquals("JSFunctionNew.js2", myFile.getName());
  }

  public void testJSFunction2() throws Exception {
    String testName = getTestName(false);
    doTest("JSFunctionNew2", testName + "_after.js2", true, testName + ".js2", testName + "_2.js2");
    assertEquals(2, findRenamedRefsToReferencedElementAtCaret().length);
  }

  public void testJSVariable() throws Exception {
    String testName = getTestName(false);
    doTest("JSVariableNew", testName + "_after.js2", testName + ".js2");
    assertEquals("JSVariableNew.js2", myFile.getName());
  }

  public void testJSVariable2() throws Exception {
    String testName = getTestName(false);
    doTest("JSVariableNew2", testName + "_after.js2", true, testName + ".js2", testName + "_2.js2");
    assertEquals(1, findRenamedRefsToReferencedElementAtCaret().length);
  }

  public void testJSClass() throws Exception {
    doTest("BBB", "js2");
    assertEquals(2, findRenamedRefsToReferencedElementAtCaret().length);
  }

  public void testJSClass4() throws Exception {
    doTest("Bar2", "js2");
  }

  public void testRenameDefaultProperty() throws Exception {
    doTest("test2", "js2");
  }

  public void testRenameMethodOfInterface() throws Exception {
    doTest("test2", "js2");
  }

  public void testRenameMethodOfInterface2() throws Exception {
    final String name = getTestName(false);
    doTest("test2", name + "_after." + "js2", false, name + "." + "js2");
    assertEquals(1, findRenamedRefsToReferencedElementAtCaret().length);
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testRenameMethodOfInterface3() throws Exception {
    final String testName = getTestName(false);
    doTest("test2", "RenameMethodOfInterface3_after.js2", testName + ".js2", testName + "_2.mxml", testName + "_3.mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testRenameMethodOfInterface4() throws Exception {
    doTest("foo2", "as");
  }

  public void testRenameMethodOfSuperClass() throws Exception {
    doTest("test2", "js2");
  }

  public void testRenameMethodOfSuperClass2() throws Exception {
    doTest("test2", "js2");
  }

  public void testJSClass2() throws Exception {
    final String name = getTestName(false);
    doTest("BBB", name + "_after.js2", true, name + ".js2");
  }

  public void testJSClass3() throws Exception {
    doTest("AAA", "js2");
  }

  public void testRenameClassUpdatesJSDoc() throws Exception {
    doTest("DDD", "js2");
  }

  public void testRenameMethodUpdatesJSDoc() throws Exception {
    doTest("bar", "js2");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testMxmlComponentRename() throws Exception {
    String name = getTestName(false);
    doTest("RenamedComponent2", name + "_after.mxml", name + ".mxml", "RenamedComponent.mxml");
    assertEquals(4, findRenamedRefsToReferencedElementAtCaret().length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testMxmlComponentRename_2() throws Exception {
    String name = getTestName(false);

    doTest("RenamedComponent2", name + "_after.mxml", name + ".mxml", "RenamedComponent.mxml");
    assertEquals(4, findRenamedRefsToReferencedElementAtCaret().length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testRenameStateInMxml() throws Exception {
    String name = getTestName(false);

    doTest("foo2", name + "_after.mxml", name + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testInlineFunRename() throws Exception {
    String name = getTestName(false);
    configureByFile(getBasePath() + name + ".mxml");

    PsiElement target = findTarget();
    CodeInsightTestUtil.doInlineRename(
      new JSInplaceRenameHandler(),
      "creationCompleteHandler2",
      InjectedLanguageUtil.getEditorForInjectedLanguageNoCommit(myEditor, myFile),
      target
    );
    checkResultByFile(getBasePath() + name + "_after.mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testRenameStateGroupInMxml() throws Exception {
    String name = getTestName(false);

    doTest("bar2", name + "_after.mxml", name + ".mxml");
  }

  public void testClassRename() throws Exception {
    final String name = getTestName(false);
    doTest("ClassRename2", name + "_after.as", name + ".as", getTestName(false) + "_2.as");
    myFile = myPsiManager.findFile(myFile.getVirtualFile());
    assertEquals(4, findRenamedRefsToReferencedElementAtCaret().length);
    assertEquals(0, JSDaemonAnalyzerTestCase.filterUnwantedInfos(doHighlighting(), true, false, false).size());
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testMxmlComponentRename2() throws Exception {
    final String name = getTestName(false);
    doTest("RenamedClazz", name + "_after.mxml", name + ".mxml", "Clazz.as");
    assertEquals(4, findRenamedRefsToReferencedElementAtCaret().length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testMxmlComponentRename3() throws Exception {
    String name = getTestName(false);
    doTest("RenamedComponent3", name + "_after.mxml", name + ".mxml", "RenamedComponent2.mxml");
    assertEquals(3, findRenamedRefsToReferencedElementAtCaret().length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testMxmlComponentRename4() throws Exception {
    String name = getTestName(false);
    doTest("RenamedComponent4", name + "_after.as", true, name + ".as", "MxmlComponentRename4_2.mxml", "MxmlComponentRename4_3.xml");
    assertEquals(7, findRenamedRefsToReferencedElementAtCaret().length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testRenamePackageRefs() throws Exception {
    String testName = getTestName(false);
    doTest("yyy", testName + "_after.mxml", testName + ".mxml", testName + "_2.as");
    assertEquals(6, findRenamedRefsToReferencedElementAtCaret().length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testRenamePackageRefs2() throws Exception {
    String testName = getTestName(false);
    doTest(
      new File(getTestDataPath() + getBasePath() + testName),
      "yyy2",
      testName + "_after.mxml",
      false,
      testName + File.separatorChar + "foo" + File.separatorChar + testName + ".mxml",
      testName + File.separatorChar + "foo" + File.separatorChar + testName + "_2.as"
    );
    assertEquals(6, findRenamedRefsToReferencedElementAtCaret().length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testRenamePackageRefs3() throws Exception {
    String testName = getTestName(false);
    doTest(
      new File(getTestDataPath() + getBasePath() + testName),
      "yyy2",
      testName + "_after.as",
      false,
      testName + File.separatorChar + "foo" + File.separatorChar + testName + ".as",
      testName + File.separatorChar + "foo" + File.separatorChar + testName + "_2.as",
      testName + File.separatorChar + "foo" + File.separatorChar + testName + "_3.mxml"
    );
    assertEquals(9, findRenamedRefsToReferencedElementAtCaret().length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testRenamePackageRefs4() throws Exception {
    String testName = getTestName(false);
    doTest("yyy", testName + "_after.as", testName + ".as", testName + "_2.as");
    assertEquals(8, findRenamedRefsToReferencedElementAtCaret().length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testRenamePackageRefs5() throws Exception {
    String testName = getTestName(false);
    doTest("yyy", testName + "_after.as", testName + ".as", testName + "_2.as");
    FileBasedIndex.getInstance().ensureUpToDate(JSPackageIndex.INDEX_ID, myProject, null);
    assertEquals(8, findRenamedRefsToReferencedElementAtCaret().length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testClassRename2() throws Exception {
    String name = getTestName(false);
    doTest("classRename2_2", name + "_after.as", name + ".as");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithJsSupportLoader})
  public void testMxmlComponentIdRename() throws Exception {
    final String name = getTestName(false);
    doTest("p2", name + "_after.mxml", name + ".mxml");
    assertEquals(2, findRenamedRefsToReferencedElementAtCaret().length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testRenameRestParameter() throws Exception {
    final String name = getTestName(false);
    doTest("rest2", name + "_after.js2", name + ".js2");
  }

  public void testRenameVarWhenRenamingGetSetFunctionFromInterface() throws Exception {
    final String name = getTestName(false);
    doTest("yyy", name + "_after.js2", name + ".js2");
  }

  public void testRenameVarWhenRenamingGetSetFunctionFromInterface2() throws Exception {
    final String name = getTestName(false);
    doTest("yyy", name + "_after.js2", name + ".js2");
  }

  public void testRenamePropertyAsWhole() throws Exception {
    String name = getTestName(false);
    doTest("yyy", name + "_after.js2", name + ".js2");
    name += "_2";
    doTest("_yyy", name + "_after.js2", name + ".js2");
  }

  public void testRenameClassTwoTimes() throws Exception {
    final String name = getTestName(false);
    VirtualFile file = getVirtualFile(getBasePath() + name + "/mytest/foo/" + name + "_2.js2");
    VirtualFile[] files = {
      getVirtualFile(getBasePath() + name + "/mytest/boo/" + name + ".js2"), file
    };

    configureByFiles(new File(getTestDataPath() + getBasePath() + name), files);
    int referencesCount = findRenamedRefsToReferencedElementAtCaret().length;
    performAction(name + "_3", true);
    checkResultByFile(getBasePath() + name + "_after.js2");

    assertEquals(referencesCount, findRenamedRefsToReferencedElementAtCaret().length);

    performAction(name + "_4", true);
    checkResultByFile(getBasePath() + name + "_after2.js2");
    PsiReference[] refs = findRenamedRefsToReferencedElementAtCaret();
    assertEquals(referencesCount, refs.length);

    for (PsiReference ref : refs) {
      PsiElement refElement = ref.getElement();
      PsiElement refElementParent = refElement.getParent();
      assertFalse(refElementParent instanceof JSReferenceExpression);

      if (!(refElementParent instanceof JSImportStatement)) {
        assertEquals(name + "_4", refElement.getText());
      }
    }
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testRenamePropertyAsWhole2() throws Exception {
    final String name = getTestName(false);
    doTest("yyy", name + "_after.js2", name + ".js2", name + ".mxml");
  }

  public void testRenameFileReferences() throws Exception {
    final String name = getTestName(false);
    doTest("xxx.as", name + "_after.as", name + ".as", name + "_2.as");
  }

  public void testRenameFileReferences2() throws Exception {
    final String name = getTestName(false);
    doTest("xxx.as", name + "_after.as", name + ".as", name.substring(0, name.length() - 1) + "_2.as");
    assertEquals(2, findRenamedRefsToReferencedElementAtCaret().length);
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testSeeTopLevelFunction() throws Exception {
    String name = getTestName(false);
    doTest("SeeTopLevelFunction_3", name + "_after.as", true, name + ".as", name + "_2.as");

    assertEquals(2, findRenamedRefsToReferencedElementAtCaret().length);
  }

  public void testNamesValidator() {
    final NamesValidator namesValidator = LanguageNamesValidation.INSTANCE.forLanguage(JSSupportLoader.getJsFileType().getLanguage());
    assertTrue(namesValidator.isIdentifier("zzz", myProject));

    assertTrue(!namesValidator.isIdentifier("\"zzz\"", myProject));
    assertTrue(namesValidator.isKeyword("function", myProject));

    assertTrue(!namesValidator.isIdentifier("zzz<=", myProject));

    final NamesValidator ecmaL4NamesValidator = LanguageNamesValidation.INSTANCE.forLanguage(JavaScriptSupportLoader.ECMA_SCRIPT_L4);
    assertTrue(ecmaL4NamesValidator.isKeyword("private", myProject));
    assertTrue(ecmaL4NamesValidator.isKeyword("namespace", myProject));
    assertTrue(ecmaL4NamesValidator.isIdentifier("namespace", myProject));

    final NamesValidator jsonNamesValidator = LanguageNamesValidation.INSTANCE.forLanguage(JsonLanguage.INSTANCE);
    assertTrue(jsonNamesValidator.isIdentifier("aaa.bbb", myProject));
  }

  public void testAmbiguity1() throws Exception {
    String name = getTestName(false);
    doTest("Ambiguous", name + "_after.as", name + ".as", name + "_2.as", name + "_3.as");

    assertEquals(3, findRenamedRefsToReferencedElementAtCaret().length);
  }

  public void testAmbiguity2() throws Exception {
    String name = getTestName(false);
    doTest("Ambiguous", name + "_after.as", name + ".as", name + "_2.as", name + "_3.as");

    assertEquals(3, findRenamedRefsToReferencedElementAtCaret().length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testSuperClasses() throws Exception {
    String name = getTestName(false);
    doTest("foo", name + "_after.as", true, name + ".as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testSuperClasses2() throws Exception {
    String name = getTestName(false);
    doTest("foo", name + "_after.as", true, name + ".as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testRenameFileReferenceInInjectedCode() throws Exception {
    final String name = getTestName(false);
    doTest(null, "foo.txt", name + "_after.mxml", false, true, true, name + ".mxml", name + ".txt");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testRunConfigUpdatedOnMethodRename() throws Exception {
    final RunManager runManager = RunManager.getInstance(myProject);
    FlexTestUtils
      .createFlexUnitRunConfig(runManager, "SomeTest.testSomething()", myModule, FlexUnitRunnerParameters.Scope.Method, "", "SomeTest",
                               "testSomething",
                               true);
    doTest("testAnotherThing", "as");
    FlexTestUtils.checkFlexUnitRunConfig(runManager, myModule, "SomeTest.testAnotherThing()", "", "SomeTest", "testAnotherThing");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testConfigUpdatedOnClassRename() throws Exception {
    FlexTestUtils.modifyBuildConfiguration(myModule, bc -> bc.setMainClass("foo.bar.SomeClass"));

    final RunManager runManager = RunManager.getInstance(myProject);
    FlexTestUtils.createFlexUnitRunConfig(runManager, "Own name", myModule, FlexUnitRunnerParameters.Scope.Method, "", "foo.bar.SomeClass",
                                          "testSomething",
                                          false);
    FlexTestUtils
      .createFlexUnitRunConfig(runManager, "SomeClass", myModule, FlexUnitRunnerParameters.Scope.Class, "", "foo.bar.SomeClass", "",
                               true);
    FlexTestUtils
      .createFlexUnitRunConfig(runManager, "Same class short name", myModule, FlexUnitRunnerParameters.Scope.Class, "", "foo.SomeClass", "",
                               false);
    FlexTestUtils.createFlashRunConfig(runManager, myModule, "SomeClass", "foo.bar.SomeClass", true);

    doTest("Renamed", "as");

    assertEquals("foo.bar.Renamed", FlexBuildConfigurationManager.getInstance(myModule).getActiveConfiguration().getMainClass());
    FlexTestUtils.checkFlexUnitRunConfig(runManager, myModule, "Own name", "", "foo.bar.Renamed", "testSomething");
    FlexTestUtils.checkFlexUnitRunConfig(runManager, myModule, "Renamed", "", "foo.bar.Renamed", "");
    FlexTestUtils.checkFlexUnitRunConfig(runManager, myModule, "Same class short name", "", "foo.SomeClass", "");
    FlexTestUtils.checkFlashRunConfig(runManager, myModule, "Renamed", "foo.bar.Renamed");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testConfigUpdatedOnPackageRename() throws Exception {
    FlexTestUtils.modifyBuildConfiguration(myModule, bc -> bc.setMainClass("foo.bar.SomeClass"));

    final RunManager runManager = RunManager.getInstance(myProject);
    FlexTestUtils.createFlexUnitRunConfig(runManager, "SomeClass.testSomething()", myModule, FlexUnitRunnerParameters.Scope.Method, "",
                                          "foo.bar.SomeClass", "testSomething",
                                          true);
    FlexTestUtils
      .createFlexUnitRunConfig(runManager, "SomeClass", myModule, FlexUnitRunnerParameters.Scope.Class, "", "foo.bar.SomeClass", "",
                               true);
    FlexTestUtils.createFlexUnitRunConfig(runManager, "foo.bar", myModule, FlexUnitRunnerParameters.Scope.Package, "foo.bar", "", "", true);
    FlexTestUtils.createFlashRunConfig(runManager, myModule, "SomeClass", "foo.bar.SomeClass", true);

    doTest("renamed", "as");

    assertEquals("foo.renamed.SomeClass", FlexBuildConfigurationManager.getInstance(myModule).getActiveConfiguration().getMainClass());
    FlexTestUtils.checkFlexUnitRunConfig(runManager, myModule, "SomeClass.testSomething()", "", "foo.renamed.SomeClass", "testSomething");
    FlexTestUtils.checkFlexUnitRunConfig(runManager, myModule, "SomeClass", "", "foo.renamed.SomeClass", "");
    FlexTestUtils.checkFlexUnitRunConfig(runManager, myModule, "foo.renamed", "foo.renamed", "", "");
    FlexTestUtils.checkFlashRunConfig(runManager, myModule, "SomeClass", "foo.renamed.SomeClass");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testRunConfigUpdatedOnBcRename() {
    final RunManager runManager = RunManager.getInstance(myProject);
    FlexTestUtils.createFlexUnitRunConfig(runManager, "SomeTest.testSomething()", myModule, FlexUnitRunnerParameters.Scope.Method, "",
                                          "SomeTest", "testSomething",
                                          true);
    FlexTestUtils.createFlashRunConfig(runManager, myModule, "SomeClass", "foo.bar.SomeClass", true);
    final String newBcName = "Abcde";
    FlexTestUtils.modifyBuildConfiguration(myModule, configuration -> configuration.setName(newBcName));
    assertEquals(newBcName, FlexBuildConfigurationManager.getInstance(myModule).getActiveConfiguration().getName());
    FlexTestUtils.checkFlexUnitRunConfig(runManager, myModule, "SomeTest.testSomething()", "", "SomeTest", "testSomething");
    FlexTestUtils.checkFlashRunConfig(runManager, myModule, "SomeClass", "foo.bar.SomeClass");
  }

  public void testLiteralReference1() throws Exception {
    final String name = getTestName(false);
    doTest(null, "bar", name + "_after.js2", false, false, false, name + ".js2");
  }

  public void testLiteralReference2() throws Exception {
    final String name = getTestName(false);
    doTest(null, "bar", name + "_after.js2", false, true, false, name + ".js2");
  }

  public void testLiteralReference5() throws Exception {
    final String name = getTestName(false);
    doTest(null, "Bar", name + "_after.as", false, false, false, name + ".as", name + "_2.as");
    assertEquals(2, findRenamedRefsToReferencedElementAtCaret().length);
  }

  public void testLiteralReference6() throws Exception {
    final String name = getTestName(false);
    doTest(null, "Bar", name + "_after.as", true, false, false, name + ".as");
    assertEquals(1, findRenamedRefsToReferencedElementAtCaret().length);
    assertEquals("Bar.as", myFile.getName());
  }

  public void testLiteralReference7() throws Exception {
    final String name = getTestName(false);
    doTest(null, "Bar", name + "_after.as", true, true, false, name + ".as");
    assertEquals(2, findRenamedRefsToReferencedElementAtCaret().length);
    assertEquals("Bar.as", myFile.getName());
  }

  public void testLiteralReference8() throws Exception {
    final String name = getTestName(false);
    doTest(null, "Bar", name + "_after.as", false, false, false, name + ".as");
    assertEquals(1, findRenamedRefsToReferencedElementAtCaret().length);
    assertEquals("Bar.as", myFile.getName());
  }

  public void testLiteralReference9() throws Exception {
    final String name = getTestName(false);
    doTest(null, "Bar", name + "_after.as", false, true, false, name + ".as");
    assertEquals(2, findRenamedRefsToReferencedElementAtCaret().length);
    assertEquals("Bar.as", myFile.getName());
  }

  public void testLiteralReference10() throws Exception {
    final String name = getTestName(false);
    doTest(null, "bar", name + "_after.as", false, false, false, name + ".as");
    assertEquals(0, findRenamedRefsToReferencedElementAtCaret().length);
  }

  public void testLiteralReference11() throws Exception {
    final String name = getTestName(false);
    doTest(null, "bar", name + "_after.as", false, true, false, name + ".as");
    assertEquals(1, findRenamedRefsToReferencedElementAtCaret().length);
  }

  public void testRenameFlexClassReferencedInCss() throws Exception {
    final String name = getTestName(false);
    doTest(name + "Renamed", name + "_after.css", name + ".css", name + ".as");

    final PsiReference newReference = myFile.findReferenceAt(myEditor.getCaretModel().getOffset());
    final PsiElement newResolve = newReference == null ? null : newReference.resolve();
    assertTrue(newResolve instanceof JSClass &&
               "bar.RenameFlexClassReferencedInCssRenamed".equals(((JSClass)newResolve).getQualifiedName()));
  }
}
