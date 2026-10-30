// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.refactoring;

import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.execution.RunManager;
import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.flex.util.JSDaemonAnalyzerTestCase;
import com.intellij.json.JsonLanguage;
import com.intellij.lang.LanguageNamesValidation;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.FlexSupportLoader;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunnerParameters;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.index.JSPackageIndex;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSNamedElement;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSImportStatement;
import com.intellij.lang.javascript.psi.impl.JSFileImpl;
import com.intellij.lang.javascript.psi.resolve.ActionScriptResolveUtil;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.lang.javascript.refactoring.rename.JSInplaceRenameHandler;
import com.intellij.lang.refactoring.NamesValidator;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.CodeInsightTestUtil;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.util.CommonProcessors;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.FileBasedIndex;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.codeInsight.TargetElementUtil.findTargetElement;
import static com.intellij.flex.refactoring.FlexRenameFixture.flexRenameFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestApplication
public class FlexRenameTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<FlexRenameFixture> js =
    flexRenameFixture(projectFixture, new JSFixtureOptions(FlexTestUtils.getTestDataPath("") + "/flex_rename/", "js")
        .withModuleType(FlexModuleType.MODULE_TYPE_ID));

  @BeforeEach
  void setUp() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    FlexTestUtils.allowFlexVfsRootsFor(myFixture.getTestRootDisposable(), "");
    FlexTestUtils.setupFlexSdk(js.get().getFixture().getModule(), js.get().getTestName(false), getClass(), myFixture.getTestRootDisposable());
  }

  @Test
  public void testRenameBundleRef() {
    String testName = js.get().getTestName(false);
    js.get().doTest("MyBundleName2.properties", testName + "_after.js2", testName + ".js2", "MyBundleName.properties");
    assertEquals(1, js.get().findRenamedRefsToReferencedElementAtCaret().length);
  }

  @Test
  public void testJSNamespace() {
    String testName = js.get().getTestName(false);
    js.get().doTest("BBB", testName + "_after.js2", testName + ".js2", testName + "_2.js2");
    assertEquals(4, js.get().findRenamedRefsToReferencedElementAtCaret().length);
  }

  @Test
  public void testJSNamespace2() {
    String testName = js.get().getTestName(false);
    js.get().doTest("BBB", true, testName + ".js2", testName + "_2.js2");
    assertEquals(3, js.get().findRenamedRefsToReferencedElementAtCaret().length);
  }

  @Test
  public void testJSFunction() {
    String testName = js.get().getTestName(false);
    js.get().doTest("JSFunctionNew", testName + "_after.js2", testName + ".js2");
    assertEquals("JSFunctionNew.js2", js.get().getFixture().getFile().getName());
  }

  @Test
  public void testJSFunction2() {
    String testName = js.get().getTestName(false);
    js.get().doTest("JSFunctionNew2", true, testName + ".js2", testName + "_2.js2");
    assertEquals(2, js.get().findRenamedRefsToReferencedElementAtCaret().length);
  }

  @Test
  public void testJSVariable() {
    String testName = js.get().getTestName(false);
    js.get().doTest("JSVariableNew", testName + "_after.js2", testName + ".js2");
    assertEquals("JSVariableNew.js2", js.get().getFixture().getFile().getName());
  }

  @Test
  public void testJSVariable2() {
    String testName = js.get().getTestName(false);
    js.get().doTest("JSVariableNew2", true, testName + ".js2", testName + "_2.js2");
    assertEquals(1, js.get().findRenamedRefsToReferencedElementAtCaret().length);
  }

  @Test
  public void testJSClass() {
    js.get().doTest("BBB", "js2");
    assertEquals(1, js.get().findRenamedRefsToReferencedElementAtCaret().length);
  }

  @Test
  public void testJSClass4() {
    js.get().doTest("Bar2", "js2");
  }

  @Test
  public void testRenameDefaultProperty() {
    js.get().doTest("test2", "js2");
  }

  @Test
  public void testRenameMethodOfInterface() {
    js.get().doTest("test2", "js2");
  }

  @Test
  public void testRenameMethodOfInterface2() {
    final String name = js.get().getTestName(false);
    js.get().doTest("test2", false, name + "." + "js2");
    assertEquals(1, js.get().findRenamedRefsToReferencedElementAtCaret().length);
  }

  @Test
  public void testRenameMethodOfInterface3() {
    final String testName = js.get().getTestName(false);
    js.get().doTest("test2", "RenameMethodOfInterface3_after.js2", testName + ".js2", testName + "_2.mxml", testName + "_3.mxml");
  }

  @Test
  public void testRenameMethodOfInterface4() {
    js.get().doTest("foo2", "as");
  }

  @Test
  public void testRenameMethodOfSuperClass() {
    js.get().doTest("test2", "js2");
  }

  @Test
  public void testRenameMethodOfSuperClass2() {
    js.get().doTest("test2", "js2");
  }

  @Test
  public void testJSClass2() {
    final String name = js.get().getTestName(false);
    js.get().doTest("BBB", true, name + ".js2");
  }

  @Test
  public void testJSClass3() {
    js.get().doTest("AAA", "js2");
  }

  @Test
  public void testRenameClassUpdatesJSDoc() {
    js.get().doTest("DDD", "js2");
  }

  @Test
  public void testRenameMethodUpdatesJSDoc() {
    js.get().doTest("bar", "js2");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testMxmlComponentRename() {
    String name = js.get().getTestName(false);
    js.get().doTest("RenamedComponent2", name + "_after.mxml", name + ".mxml", "RenamedComponent.mxml");
    assertEquals(4, js.get().findRenamedRefsToReferencedElementAtCaret().length);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testMxmlComponentRename_2() {
    String name = js.get().getTestName(false);

    js.get().doTest("RenamedComponent2", name + "_after.mxml", name + ".mxml", "RenamedComponent.mxml");
    assertEquals(4, js.get().findRenamedRefsToReferencedElementAtCaret().length);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testRenameStateInMxml() {
    String name = js.get().getTestName(false);

    js.get().doTest("foo2", name + "_after.mxml", name + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testInlineFunRename() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    String name = js.get().getTestName(false);
    myFixture.configureByFile(name + ".mxml");

    EdtTestUtil.runInEdtAndWait(() -> CodeInsightTestUtil.doInlineRename(
      new JSInplaceRenameHandler(),
      "creationCompleteHandler2",
      InjectedLanguageUtil.getEditorForInjectedLanguageNoCommit(myFixture.getEditor(), myFixture.getFile()),
      myFixture.getElementAtCaret()
    ));
    myFixture.checkResultByFile(name + "_after.mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testRenameStateGroupInMxml() {
    String name = js.get().getTestName(false);

    js.get().doTest("bar2", name + "_after.mxml", name + ".mxml");
  }

  @Test
  public void testClassRename() {
    final String name = js.get().getTestName(false);
    js.get().doTest("ClassRename2", name + "_after.as", name + ".as", js.get().getTestName(false) + "_2.as");
    assertEquals(3, js.get().findRenamedRefsToReferencedElementAtCaret().length);
    assertEquals(0, JSDaemonAnalyzerTestCase.filterUnwantedInfos(js.get().getFixture().doHighlighting(), true, false, false).size());
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testMxmlComponentRename2() {
    final String name = js.get().getTestName(false);
    js.get().doTest("RenamedClazz", name + "_after.mxml", name + ".mxml", "Clazz.as");
    assertEquals(3, js.get().findRenamedRefsToReferencedElementAtCaret().length);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testMxmlComponentRename3() {
    String name = js.get().getTestName(false);
    js.get().doTest("RenamedComponent3", name + "_after.mxml", name + ".mxml", "RenamedComponent2.mxml");
    assertEquals(3, js.get().findRenamedRefsToReferencedElementAtCaret().length);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testMxmlComponentRename4() {
    String name = js.get().getTestName(false);
    js.get().doTest("RenamedComponent4", true, name + ".as", "MxmlComponentRename4_2.mxml", "MxmlComponentRename4_3.xml");
    assertEquals(6, js.get().findRenamedRefsToReferencedElementAtCaret().length);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testRenamePackageRefs() {
    String testName = js.get().getTestName(false);
    js.get().doTest("yyy", testName + "_after.mxml", testName + ".mxml", testName + "_2.as");
    assertEquals(6, js.get().findRenamedRefsToReferencedElementAtCaret().length);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testRenamePackageRefs2() {
    String testName = js.get().getTestName(false);
    js.get().doTest(
      "yyy2",
      testName + "_after.mxml",
      false,
      testName + "/foo/" + testName + ".mxml",
      testName + "/foo/" + testName + "_2.as"
    );
    assertEquals(6, js.get().findRenamedRefsToReferencedElementAtCaret().length);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testRenamePackageRefs3() {
    String testName = js.get().getTestName(false);
    js.get().doTest(
      "yyy2",
      testName + "_after.as",
      false,
      testName + "/foo/" + testName + ".as",
      testName + "/foo/" + testName + "_2.as",
      testName + "/foo/" + testName + "_3.mxml"
    );
    assertEquals(8, js.get().findRenamedRefsToReferencedElementAtCaret().length);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testRenamePackageRefs4() {
    String testName = js.get().getTestName(false);
    js.get().doTest("yyy", testName + "_after.as", testName + ".as", testName + "_2.as");
    assertEquals(8, js.get().findRenamedRefsToReferencedElementAtCaret().length);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testRenamePackageRefs5() {
    String testName = js.get().getTestName(false);
    js.get().doTest("yyy", testName + "_after.as", testName + ".as", testName + "_2.as");
    ReadAction.run(() -> FileBasedIndex.getInstance().ensureUpToDate(JSPackageIndex.INDEX_ID, js.get().getFixture().getProject(), null));
    assertEquals(7, js.get().findRenamedRefsToReferencedElementAtCaret().length);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testClassRename2() {
    String name = js.get().getTestName(false);
    js.get().doTest("classRename2_2", name + "_after.as", name + ".as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testMxmlComponentIdRename() {
    final String name = js.get().getTestName(false);
    js.get().doTest("p2", name + "_after.mxml", name + ".mxml");
    assertEquals(2, js.get().findRenamedRefsToReferencedElementAtCaret().length);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testRenameRestParameter() {
    final String name = js.get().getTestName(false);
    js.get().doTest("rest2", name + "_after.js2", name + ".js2");
  }

  @Test
  public void testRenameVarWhenRenamingGetSetFunctionFromInterface() {
    final String name = js.get().getTestName(false);
    js.get().doTest("yyy", name + "_after.js2", name + ".js2");
  }

  @Test
  public void testRenameVarWhenRenamingGetSetFunctionFromInterface2() {
    final String name = js.get().getTestName(false);
    js.get().doTest("yyy", name + "_after.js2", name + ".js2");
  }

  @Test
  public void testRenamePropertyAsWhole() {
    String name = js.get().getTestName(false);
    js.get().doTest("yyy", name + "_after.js2", name + ".js2");
    name += "_2";
    js.get().doTest("_yyy", name + "_after.js2", name + ".js2");
  }

  @Test
  public void testRenameClassTwoTimes() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    final String name = js.get().getTestName(false);
    myFixture.configureByFiles(name + "/mytest/boo/" + name + ".js2", name + "/mytest/foo/" + name + "_2.js2");
    int referencesCount = js.get().findRenamedRefsToReferencedElementAtCaret().length;
    js.get().performDialogRename(js.get().helper().defaultParameters().withName(name + "_3"));
    myFixture.checkResultByFile(name + "_after.js2");

    assertEquals(referencesCount, js.get().findRenamedRefsToReferencedElementAtCaret().length);

    js.get().performDialogRename(js.get().helper().defaultParameters().withName(name + "_4"));
    myFixture.checkResultByFile(name + "_after2.js2");
    PsiReference[] refs = js.get().findRenamedRefsToReferencedElementAtCaret();
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

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testRenamePropertyAsWhole2() {
    final String name = js.get().getTestName(false);
    js.get().doTest("yyy", name + "_after.js2", name + ".js2", name + ".mxml");
  }

  @Test
  public void testRenameFileReferences() {
    final String name = js.get().getTestName(false);
    js.get().doTest("xxx.as", name + "_after.as", name + ".as", name + "_2.as");
  }

  @Test
  public void testRenameFileReferences2() {
    final String name = js.get().getTestName(false);
    js.get().doTest("xxx.as", name + "_after.as", name + ".as", name.substring(0, name.length() - 1) + "_2.as");
    assertEquals(2, js.get().findRenamedRefsToReferencedElementAtCaret().length);
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testSeeTopLevelFunction() {
    String name = js.get().getTestName(false);
    js.get().doTest("SeeTopLevelFunction_3", true, name + ".as", name + "_2.as");

    assertEquals(2, js.get().findRenamedRefsToReferencedElementAtCaret().length);
  }

  @Test
  public void testNamesValidator() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    final NamesValidator namesValidator = LanguageNamesValidation.INSTANCE.forLanguage(JavaScriptFileType.INSTANCE.getLanguage());
    assertTrue(namesValidator.isIdentifier("zzz", myFixture.getProject()));

    assertTrue(!namesValidator.isIdentifier("\"zzz\"", myFixture.getProject()));
    assertTrue(namesValidator.isKeyword("function", myFixture.getProject()));

    assertTrue(!namesValidator.isIdentifier("zzz<=", myFixture.getProject()));

    final NamesValidator ecmaL4NamesValidator = LanguageNamesValidation.INSTANCE.forLanguage(FlexSupportLoader.ECMA_SCRIPT_L4);
    assertTrue(ecmaL4NamesValidator.isKeyword("private", myFixture.getProject()));
    assertTrue(ecmaL4NamesValidator.isKeyword("namespace", myFixture.getProject()));
    assertTrue(ecmaL4NamesValidator.isIdentifier("namespace", myFixture.getProject()));

    final NamesValidator jsonNamesValidator = LanguageNamesValidation.INSTANCE.forLanguage(JsonLanguage.INSTANCE);
    assertTrue(jsonNamesValidator.isIdentifier("aaa.bbb", myFixture.getProject()));
  }

  @Test
  public void testAmbiguity1() {
    String name = js.get().getTestName(false);
    js.get().doTest("Ambiguous", name + "_after.as", name + ".as", name + "_2.as", name + "_3.as");

    assertEquals(2, js.get().findRenamedRefsToReferencedElementAtCaret().length);
  }

  @Test
  public void testAmbiguity2() {
    String name = js.get().getTestName(false);
    js.get().doTest("Ambiguous", name + "_after.as", name + ".as", name + "_2.as", name + "_3.as");

    assertEquals(2, js.get().findRenamedRefsToReferencedElementAtCaret().length);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testSuperClasses() {
    String name = js.get().getTestName(false);
    js.get().doTest("foo", true, name + ".as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testSuperClasses2() {
    String name = js.get().getTestName(false);
    js.get().doTest("foo", true, name + ".as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testRenameFileReferenceInInjectedCode() {
    final String name = js.get().getTestName(false);
    js.get().doTest("foo.txt", name + "_after.mxml", false, true, true, name + ".mxml", name + ".txt");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testRunConfigUpdatedOnMethodRename() {
    final RunManager runManager = RunManager.getInstance(js.get().getFixture().getProject());
    FlexTestUtils
      .createFlexUnitRunConfig(runManager, "SomeTest.testSomething()", js.get().getFixture().getModule(), FlexUnitRunnerParameters.Scope.Method, "", "SomeTest",
                               "testSomething",
                               true);
    js.get().doTest("testAnotherThing", "as");
    FlexTestUtils.checkFlexUnitRunConfig(runManager, js.get().getFixture().getModule(), "SomeTest.testAnotherThing()", "", "SomeTest", "testAnotherThing");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testConfigUpdatedOnClassRename() {
    FlexTestUtils.modifyBuildConfiguration(js.get().getFixture().getModule(), bc -> bc.setMainClass("foo.bar.SomeClass"));

    final RunManager runManager = RunManager.getInstance(js.get().getFixture().getProject());
    FlexTestUtils.createFlexUnitRunConfig(runManager, "Own name", js.get().getFixture().getModule(), FlexUnitRunnerParameters.Scope.Method, "", "foo.bar.SomeClass",
                                          "testSomething",
                                          false);
    FlexTestUtils
      .createFlexUnitRunConfig(runManager, "SomeClass", js.get().getFixture().getModule(), FlexUnitRunnerParameters.Scope.Class, "", "foo.bar.SomeClass", "",
                               true);
    FlexTestUtils
      .createFlexUnitRunConfig(runManager, "Same class short name", js.get().getFixture().getModule(), FlexUnitRunnerParameters.Scope.Class, "", "foo.SomeClass", "",
                               false);
    FlexTestUtils.createFlashRunConfig(runManager, js.get().getFixture().getModule(), "SomeClass", "foo.bar.SomeClass", true);

    js.get().doTest("Renamed", "as");

    assertEquals("foo.bar.Renamed", FlexBuildConfigurationManager.getInstance(js.get().getFixture().getModule()).getActiveConfiguration().getMainClass());
    FlexTestUtils.checkFlexUnitRunConfig(runManager, js.get().getFixture().getModule(), "Own name", "", "foo.bar.Renamed", "testSomething");
    FlexTestUtils.checkFlexUnitRunConfig(runManager, js.get().getFixture().getModule(), "Renamed", "", "foo.bar.Renamed", "");
    FlexTestUtils.checkFlexUnitRunConfig(runManager, js.get().getFixture().getModule(), "Same class short name", "", "foo.SomeClass", "");
    FlexTestUtils.checkFlashRunConfig(runManager, js.get().getFixture().getModule(), "Renamed", "foo.bar.Renamed");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testConfigUpdatedOnPackageRename() {
    FlexTestUtils.modifyBuildConfiguration(js.get().getFixture().getModule(), bc -> bc.setMainClass("foo.bar.SomeClass"));

    final RunManager runManager = RunManager.getInstance(js.get().getFixture().getProject());
    FlexTestUtils.createFlexUnitRunConfig(runManager, "SomeClass.testSomething()", js.get().getFixture().getModule(), FlexUnitRunnerParameters.Scope.Method, "",
                                          "foo.bar.SomeClass", "testSomething",
                                          true);
    FlexTestUtils
      .createFlexUnitRunConfig(runManager, "SomeClass", js.get().getFixture().getModule(), FlexUnitRunnerParameters.Scope.Class, "", "foo.bar.SomeClass", "",
                               true);
    FlexTestUtils.createFlexUnitRunConfig(runManager, "foo.bar", js.get().getFixture().getModule(), FlexUnitRunnerParameters.Scope.Package, "foo.bar", "", "", true);
    FlexTestUtils.createFlashRunConfig(runManager, js.get().getFixture().getModule(), "SomeClass", "foo.bar.SomeClass", true);

    js.get().doTest("renamed", "as");

    assertEquals("foo.renamed.SomeClass", FlexBuildConfigurationManager.getInstance(js.get().getFixture().getModule()).getActiveConfiguration().getMainClass());
    FlexTestUtils.checkFlexUnitRunConfig(runManager, js.get().getFixture().getModule(), "SomeClass.testSomething()", "", "foo.renamed.SomeClass", "testSomething");
    FlexTestUtils.checkFlexUnitRunConfig(runManager, js.get().getFixture().getModule(), "SomeClass", "", "foo.renamed.SomeClass", "");
    FlexTestUtils.checkFlexUnitRunConfig(runManager, js.get().getFixture().getModule(), "foo.renamed", "foo.renamed", "", "");
    FlexTestUtils.checkFlashRunConfig(runManager, js.get().getFixture().getModule(), "SomeClass", "foo.renamed.SomeClass");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testRunConfigUpdatedOnBcRename() {
    final RunManager runManager = RunManager.getInstance(js.get().getFixture().getProject());
    FlexTestUtils.createFlexUnitRunConfig(runManager, "SomeTest.testSomething()", js.get().getFixture().getModule(), FlexUnitRunnerParameters.Scope.Method, "",
                                          "SomeTest", "testSomething",
                                          true);
    FlexTestUtils.createFlashRunConfig(runManager, js.get().getFixture().getModule(), "SomeClass", "foo.bar.SomeClass", true);
    final String newBcName = "Abcde";
    FlexTestUtils.modifyBuildConfiguration(js.get().getFixture().getModule(), configuration -> configuration.setName(newBcName));
    assertEquals(newBcName, FlexBuildConfigurationManager.getInstance(js.get().getFixture().getModule()).getActiveConfiguration().getName());
    FlexTestUtils.checkFlexUnitRunConfig(runManager, js.get().getFixture().getModule(), "SomeTest.testSomething()", "", "SomeTest", "testSomething");
    FlexTestUtils.checkFlashRunConfig(runManager, js.get().getFixture().getModule(), "SomeClass", "foo.bar.SomeClass");
  }

  @Test
  public void testLiteralReference1() {
    final String name = js.get().getTestName(false);
    js.get().doTest("bar", name + "_after.js2", false, false, false, name + ".js2");
  }

  @Test
  public void testLiteralReference2() {
    final String name = js.get().getTestName(false);
    js.get().doTest("bar", name + "_after.js2", false, true, false, name + ".js2");
  }

  @Test
  public void testLiteralReference5() {
    final String name = js.get().getTestName(false);
    js.get().doTest("Bar", name + "_after.as", false, false, false, name + ".as", name + "_2.as");
    assertEquals(1, js.get().findRenamedRefsToReferencedElementAtCaret().length);
  }

  @Test
  public void testLiteralReference6() {
    final String name = js.get().getTestName(false);
    js.get().doTest("Bar", name + "_after.as", true, false, false, name + ".as");
    assertEquals(0, js.get().findRenamedRefsToReferencedElementAtCaret().length);
    assertEquals("Bar.as", js.get().getFixture().getFile().getName());
  }

  @Test
  public void testLiteralReference7() {
    final String name = js.get().getTestName(false);
    js.get().doTest("Bar", name + "_after.as", true, true, false, name + ".as");
    assertEquals(1, js.get().findRenamedRefsToReferencedElementAtCaret().length);
    assertEquals("Bar.as", js.get().getFixture().getFile().getName());
  }

  @Test
  public void testLiteralReference8() {
    final String name = js.get().getTestName(false);
    js.get().doTest("Bar", name + "_after.as", false, false, false, name + ".as");
    assertEquals(0, js.get().findRenamedRefsToReferencedElementAtCaret().length);
    assertEquals("Bar.as", js.get().getFixture().getFile().getName());
  }

  @Test
  public void testLiteralReference9() {
    final String name = js.get().getTestName(false);
    js.get().doTest("Bar", name + "_after.as", false, true, false, name + ".as");
    assertEquals(1, js.get().findRenamedRefsToReferencedElementAtCaret().length);
    assertEquals("Bar.as", js.get().getFixture().getFile().getName());
  }

  @Test
  public void testLiteralReference10() {
    final String name = js.get().getTestName(false);
    js.get().doTest("bar", name + "_after.as", false, false, false, name + ".as");
    assertEquals(0, js.get().findRenamedRefsToReferencedElementAtCaret().length);
  }

  @Test
  public void testLiteralReference11() {
    final String name = js.get().getTestName(false);
    js.get().doTest("bar", name + "_after.as", false, true, false, name + ".as");
    assertEquals(1, js.get().findRenamedRefsToReferencedElementAtCaret().length);
  }

  @Test
  public void testRenameFlexClassReferencedInCss() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    final String name = js.get().getTestName(false);
    js.get().doTest(name + "Renamed", name + "_after.css", name + ".css", name + ".as");

    JSTestUtils.runInBackgroundReadActionAndWaitVoid(() -> {
      final PsiReference newReference = myFixture.getFile().findReferenceAt(myFixture.getEditor().getCaretModel().getOffset());
      final PsiElement newResolve = newReference == null ? null : newReference.resolve();
      assertTrue(newResolve instanceof JSClass &&
                 "bar.RenameFlexClassReferencedInCssRenamed".equals(((JSClass)newResolve).getQualifiedName()));
    });
  }
}
