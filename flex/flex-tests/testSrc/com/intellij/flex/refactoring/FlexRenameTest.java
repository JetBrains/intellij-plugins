// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.refactoring;

import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.execution.RunManager;
import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.editor.FlexProjectDescriptor;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.json.JsonLanguage;
import com.intellij.lang.LanguageNamesValidation;
import com.intellij.lang.javascript.JSAbstractRenameTest;
import com.intellij.lang.javascript.JSDaemonAnalyzerTestCase;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.CodeInsightTestUtil;
import com.intellij.util.CommonProcessors;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import static com.intellij.codeInsight.TargetElementUtil.findTargetElement;

public class FlexRenameTest extends JSAbstractRenameTest {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    FlexTestUtils.allowFlexVfsRootsFor(myFixture.getTestRootDisposable(), "");
    FlexTestUtils.setupFlexSdk(getModule(), getTestName(false), getClass(), myFixture.getTestRootDisposable());
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("") + "/flex_rename/";
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return FlexProjectDescriptor.DESCRIPTOR;
  }

  public void testRenameBundleRef() {
    String testName = getTestName(false);
    doTest("MyBundleName2.properties", testName + "_after.js2", testName + ".js2", "MyBundleName.properties");
    assertEquals(1, findRenamedRefsToReferencedElementAtCaret().length);
  }

  public void testJSNamespace() {
    String testName = getTestName(false);
    doTest("BBB", testName + "_after.js2", testName + ".js2", testName + "_2.js2");
    assertEquals(4, findRenamedRefsToReferencedElementAtCaret().length);
  }

  public void testJSNamespace2() {
    String testName = getTestName(false);
    doTest("BBB", true, testName + ".js2", testName + "_2.js2");
    assertEquals(3, findRenamedRefsToReferencedElementAtCaret().length);
  }

  public void testJSFunction() {
    String testName = getTestName(false);
    doTest("JSFunctionNew", testName + "_after.js2", testName + ".js2");
    assertEquals("JSFunctionNew.js2", myFixture.getFile().getName());
  }

  public void testJSFunction2() {
    String testName = getTestName(false);
    doTest("JSFunctionNew2", true, testName + ".js2", testName + "_2.js2");
    assertEquals(2, findRenamedRefsToReferencedElementAtCaret().length);
  }

  public void testJSVariable() {
    String testName = getTestName(false);
    doTest("JSVariableNew", testName + "_after.js2", testName + ".js2");
    assertEquals("JSVariableNew.js2", myFixture.getFile().getName());
  }

  public void testJSVariable2() {
    String testName = getTestName(false);
    doTest("JSVariableNew2", true, testName + ".js2", testName + "_2.js2");
    assertEquals(1, findRenamedRefsToReferencedElementAtCaret().length);
  }

  public void testJSClass() {
    doTest("BBB", "js2");
    assertEquals(1, findRenamedRefsToReferencedElementAtCaret().length);
  }

  public void testJSClass4() {
    doTest("Bar2", "js2");
  }

  public void testRenameDefaultProperty() {
    doTest("test2", "js2");
  }

  public void testRenameMethodOfInterface() {
    doTest("test2", "js2");
  }

  public void testRenameMethodOfInterface2() {
    final String name = getTestName(false);
    doTest("test2", false, name + "." + "js2");
    assertEquals(1, findRenamedRefsToReferencedElementAtCaret().length);
  }

  public void testRenameMethodOfInterface3() {
    final String testName = getTestName(false);
    doTest("test2", "RenameMethodOfInterface3_after.js2", testName + ".js2", testName + "_2.mxml", testName + "_3.mxml");
  }

  public void testRenameMethodOfInterface4() {
    doTest("foo2", "as");
  }

  public void testRenameMethodOfSuperClass() {
    doTest("test2", "js2");
  }

  public void testRenameMethodOfSuperClass2() {
    doTest("test2", "js2");
  }

  public void testJSClass2() {
    final String name = getTestName(false);
    doTest("BBB", true, name + ".js2");
  }

  public void testJSClass3() {
    doTest("AAA", "js2");
  }

  public void testRenameClassUpdatesJSDoc() {
    doTest("DDD", "js2");
  }

  public void testRenameMethodUpdatesJSDoc() {
    doTest("bar", "js2");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testMxmlComponentRename() {
    String name = getTestName(false);
    doTest("RenamedComponent2", name + "_after.mxml", name + ".mxml", "RenamedComponent.mxml");
    assertEquals(4, findRenamedRefsToReferencedElementAtCaret().length);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testMxmlComponentRename_2() {
    String name = getTestName(false);

    doTest("RenamedComponent2", name + "_after.mxml", name + ".mxml", "RenamedComponent.mxml");
    assertEquals(4, findRenamedRefsToReferencedElementAtCaret().length);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testRenameStateInMxml() {
    String name = getTestName(false);

    doTest("foo2", name + "_after.mxml", name + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testInlineFunRename() {
    String name = getTestName(false);
    myFixture.configureByFile(getBasePath() + name + ".mxml");

    CodeInsightTestUtil.doInlineRename(
      new JSInplaceRenameHandler(),
      "creationCompleteHandler2",
      InjectedLanguageUtil.getEditorForInjectedLanguageNoCommit(myFixture.getEditor(), myFixture.getFile()),
      myFixture.getElementAtCaret()
    );
    myFixture.checkResultByFile((getBasePath() + name + "_after.mxml"));
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testRenameStateGroupInMxml() {
    String name = getTestName(false);

    doTest("bar2", name + "_after.mxml", name + ".mxml");
  }

  public void testClassRename() {
    final String name = getTestName(false);
    doTest("ClassRename2", name + "_after.as", name + ".as", getTestName(false) + "_2.as");
    assertEquals(3, findRenamedRefsToReferencedElementAtCaret().length);
    assertEquals(0, JSDaemonAnalyzerTestCase.filterUnwantedInfos(myFixture.doHighlighting(), true, false, false).size());
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testMxmlComponentRename2() {
    final String name = getTestName(false);
    doTest("RenamedClazz", name + "_after.mxml", name + ".mxml", "Clazz.as");
    assertEquals(3, findRenamedRefsToReferencedElementAtCaret().length);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testMxmlComponentRename3() {
    String name = getTestName(false);
    doTest("RenamedComponent3", name + "_after.mxml", name + ".mxml", "RenamedComponent2.mxml");
    assertEquals(3, findRenamedRefsToReferencedElementAtCaret().length);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testMxmlComponentRename4() {
    String name = getTestName(false);
    doTest("RenamedComponent4", true, name + ".as", "MxmlComponentRename4_2.mxml", "MxmlComponentRename4_3.xml");
    assertEquals(6, findRenamedRefsToReferencedElementAtCaret().length);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testRenamePackageRefs() {
    String testName = getTestName(false);
    doTest("yyy", testName + "_after.mxml", testName + ".mxml", testName + "_2.as");
    assertEquals(6, findRenamedRefsToReferencedElementAtCaret().length);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testRenamePackageRefs2() {
    String testName = getTestName(false);
    doTest(
      "yyy2",
      testName + "_after.mxml",
      false,
      testName + "/foo/" + testName + ".mxml",
      testName + "/foo/" + testName + "_2.as"
    );
    assertEquals(6, findRenamedRefsToReferencedElementAtCaret().length);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testRenamePackageRefs3() {
    String testName = getTestName(false);
    doTest(
      "yyy2",
      testName + "_after.as",
      false,
      testName + "/foo/" + testName + ".as",
      testName + "/foo/" + testName + "_2.as",
      testName + "/foo/" + testName + "_3.mxml"
    );
    assertEquals(8, findRenamedRefsToReferencedElementAtCaret().length);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testRenamePackageRefs4() {
    String testName = getTestName(false);
    doTest("yyy", testName + "_after.as", testName + ".as", testName + "_2.as");
    assertEquals(8, findRenamedRefsToReferencedElementAtCaret().length);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testRenamePackageRefs5() {
    String testName = getTestName(false);
    doTest("yyy", testName + "_after.as", testName + ".as", testName + "_2.as");
    FileBasedIndex.getInstance().ensureUpToDate(JSPackageIndex.INDEX_ID, myFixture.getProject(), null);
    assertEquals(7, findRenamedRefsToReferencedElementAtCaret().length);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testClassRename2() {
    String name = getTestName(false);
    doTest("classRename2_2", name + "_after.as", name + ".as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testMxmlComponentIdRename() {
    final String name = getTestName(false);
    doTest("p2", name + "_after.mxml", name + ".mxml");
    assertEquals(2, findRenamedRefsToReferencedElementAtCaret().length);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testRenameRestParameter() {
    final String name = getTestName(false);
    doTest("rest2", name + "_after.js2", name + ".js2");
  }

  public void testRenameVarWhenRenamingGetSetFunctionFromInterface() {
    final String name = getTestName(false);
    doTest("yyy", name + "_after.js2", name + ".js2");
  }

  public void testRenameVarWhenRenamingGetSetFunctionFromInterface2() {
    final String name = getTestName(false);
    doTest("yyy", name + "_after.js2", name + ".js2");
  }

  public void testRenamePropertyAsWhole() {
    String name = getTestName(false);
    doTest("yyy", name + "_after.js2", name + ".js2");
    name += "_2";
    doTest("_yyy", name + "_after.js2", name + ".js2");
  }

  public void testRenameClassTwoTimes() {
    final String name = getTestName(false);
    myFixture.configureByFiles(name + "/mytest/boo/" + name + ".js2", name + "/mytest/foo/" + name + "_2.js2");
    int referencesCount = findRenamedRefsToReferencedElementAtCaret().length;
    performDialogRename(defaultParameters().withName(name + "_3"));
    myFixture.checkResultByFile(getBasePath() + name + "_after.js2");

    assertEquals(referencesCount, findRenamedRefsToReferencedElementAtCaret().length);

    performDialogRename(defaultParameters().withName(name + "_4"));
    myFixture.checkResultByFile(name + "_after2.js2");
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

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testRenamePropertyAsWhole2() {
    final String name = getTestName(false);
    doTest("yyy", name + "_after.js2", name + ".js2", name + ".mxml");
  }

  public void testRenameFileReferences() {
    final String name = getTestName(false);
    doTest("xxx.as", name + "_after.as", name + ".as", name + "_2.as");
  }

  public void testRenameFileReferences2() {
    final String name = getTestName(false);
    doTest("xxx.as", name + "_after.as", name + ".as", name.substring(0, name.length() - 1) + "_2.as");
    assertEquals(2, findRenamedRefsToReferencedElementAtCaret().length);
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testSeeTopLevelFunction() {
    String name = getTestName(false);
    doTest("SeeTopLevelFunction_3", true, name + ".as", name + "_2.as");

    assertEquals(2, findRenamedRefsToReferencedElementAtCaret().length);
  }

  public void testNamesValidator() {
    final NamesValidator namesValidator = LanguageNamesValidation.INSTANCE.forLanguage(JavaScriptFileType.INSTANCE.getLanguage());
    assertTrue(namesValidator.isIdentifier("zzz", myFixture.getProject()));

    assertTrue(!namesValidator.isIdentifier("\"zzz\"", myFixture.getProject()));
    assertTrue(namesValidator.isKeyword("function", myFixture.getProject()));

    assertTrue(!namesValidator.isIdentifier("zzz<=", myFixture.getProject()));

    final NamesValidator ecmaL4NamesValidator = LanguageNamesValidation.INSTANCE.forLanguage(JavaScriptSupportLoader.ECMA_SCRIPT_L4);
    assertTrue(ecmaL4NamesValidator.isKeyword("private", myFixture.getProject()));
    assertTrue(ecmaL4NamesValidator.isKeyword("namespace", myFixture.getProject()));
    assertTrue(ecmaL4NamesValidator.isIdentifier("namespace", myFixture.getProject()));

    final NamesValidator jsonNamesValidator = LanguageNamesValidation.INSTANCE.forLanguage(JsonLanguage.INSTANCE);
    assertTrue(jsonNamesValidator.isIdentifier("aaa.bbb", myFixture.getProject()));
  }

  public void testAmbiguity1() {
    String name = getTestName(false);
    doTest("Ambiguous", name + "_after.as", name + ".as", name + "_2.as", name + "_3.as");

    assertEquals(2, findRenamedRefsToReferencedElementAtCaret().length);
  }

  public void testAmbiguity2() {
    String name = getTestName(false);
    doTest("Ambiguous", name + "_after.as", name + ".as", name + "_2.as", name + "_3.as");

    assertEquals(2, findRenamedRefsToReferencedElementAtCaret().length);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testSuperClasses() {
    String name = getTestName(false);
    doTest("foo", true, name + ".as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testSuperClasses2() {
    String name = getTestName(false);
    doTest("foo", true, name + ".as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testRenameFileReferenceInInjectedCode() {
    final String name = getTestName(false);
    doTest("foo.txt", name + "_after.mxml", false, true, true, name + ".mxml", name + ".txt");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testRunConfigUpdatedOnMethodRename() {
    final RunManager runManager = RunManager.getInstance(myFixture.getProject());
    FlexTestUtils
      .createFlexUnitRunConfig(runManager, "SomeTest.testSomething()", getModule(), FlexUnitRunnerParameters.Scope.Method, "", "SomeTest",
                               "testSomething",
                               true);
    doTest("testAnotherThing", "as");
    FlexTestUtils.checkFlexUnitRunConfig(runManager, getModule(), "SomeTest.testAnotherThing()", "", "SomeTest", "testAnotherThing");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testConfigUpdatedOnClassRename() {
    FlexTestUtils.modifyBuildConfiguration(getModule(), bc -> bc.setMainClass("foo.bar.SomeClass"));

    final RunManager runManager = RunManager.getInstance(myFixture.getProject());
    FlexTestUtils.createFlexUnitRunConfig(runManager, "Own name", getModule(), FlexUnitRunnerParameters.Scope.Method, "", "foo.bar.SomeClass",
                                          "testSomething",
                                          false);
    FlexTestUtils
      .createFlexUnitRunConfig(runManager, "SomeClass", getModule(), FlexUnitRunnerParameters.Scope.Class, "", "foo.bar.SomeClass", "",
                               true);
    FlexTestUtils
      .createFlexUnitRunConfig(runManager, "Same class short name", getModule(), FlexUnitRunnerParameters.Scope.Class, "", "foo.SomeClass", "",
                               false);
    FlexTestUtils.createFlashRunConfig(runManager, getModule(), "SomeClass", "foo.bar.SomeClass", true);

    doTest("Renamed", "as");

    assertEquals("foo.bar.Renamed", FlexBuildConfigurationManager.getInstance(getModule()).getActiveConfiguration().getMainClass());
    FlexTestUtils.checkFlexUnitRunConfig(runManager, getModule(), "Own name", "", "foo.bar.Renamed", "testSomething");
    FlexTestUtils.checkFlexUnitRunConfig(runManager, getModule(), "Renamed", "", "foo.bar.Renamed", "");
    FlexTestUtils.checkFlexUnitRunConfig(runManager, getModule(), "Same class short name", "", "foo.SomeClass", "");
    FlexTestUtils.checkFlashRunConfig(runManager, getModule(), "Renamed", "foo.bar.Renamed");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testConfigUpdatedOnPackageRename() {
    FlexTestUtils.modifyBuildConfiguration(getModule(), bc -> bc.setMainClass("foo.bar.SomeClass"));

    final RunManager runManager = RunManager.getInstance(myFixture.getProject());
    FlexTestUtils.createFlexUnitRunConfig(runManager, "SomeClass.testSomething()", getModule(), FlexUnitRunnerParameters.Scope.Method, "",
                                          "foo.bar.SomeClass", "testSomething",
                                          true);
    FlexTestUtils
      .createFlexUnitRunConfig(runManager, "SomeClass", getModule(), FlexUnitRunnerParameters.Scope.Class, "", "foo.bar.SomeClass", "",
                               true);
    FlexTestUtils.createFlexUnitRunConfig(runManager, "foo.bar", getModule(), FlexUnitRunnerParameters.Scope.Package, "foo.bar", "", "", true);
    FlexTestUtils.createFlashRunConfig(runManager, getModule(), "SomeClass", "foo.bar.SomeClass", true);

    doTest("renamed", "as");

    assertEquals("foo.renamed.SomeClass", FlexBuildConfigurationManager.getInstance(getModule()).getActiveConfiguration().getMainClass());
    FlexTestUtils.checkFlexUnitRunConfig(runManager, getModule(), "SomeClass.testSomething()", "", "foo.renamed.SomeClass", "testSomething");
    FlexTestUtils.checkFlexUnitRunConfig(runManager, getModule(), "SomeClass", "", "foo.renamed.SomeClass", "");
    FlexTestUtils.checkFlexUnitRunConfig(runManager, getModule(), "foo.renamed", "foo.renamed", "", "");
    FlexTestUtils.checkFlashRunConfig(runManager, getModule(), "SomeClass", "foo.renamed.SomeClass");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testRunConfigUpdatedOnBcRename() {
    final RunManager runManager = RunManager.getInstance(myFixture.getProject());
    FlexTestUtils.createFlexUnitRunConfig(runManager, "SomeTest.testSomething()", getModule(), FlexUnitRunnerParameters.Scope.Method, "",
                                          "SomeTest", "testSomething",
                                          true);
    FlexTestUtils.createFlashRunConfig(runManager, getModule(), "SomeClass", "foo.bar.SomeClass", true);
    final String newBcName = "Abcde";
    FlexTestUtils.modifyBuildConfiguration(getModule(), configuration -> configuration.setName(newBcName));
    assertEquals(newBcName, FlexBuildConfigurationManager.getInstance(getModule()).getActiveConfiguration().getName());
    FlexTestUtils.checkFlexUnitRunConfig(runManager, getModule(), "SomeTest.testSomething()", "", "SomeTest", "testSomething");
    FlexTestUtils.checkFlashRunConfig(runManager, getModule(), "SomeClass", "foo.bar.SomeClass");
  }

  public void testLiteralReference1() {
    final String name = getTestName(false);
    doTest("bar", name + "_after.js2", false, false, false, name + ".js2");
  }

  public void testLiteralReference2() {
    final String name = getTestName(false);
    doTest("bar", name + "_after.js2", false, true, false, name + ".js2");
  }

  public void testLiteralReference5() {
    final String name = getTestName(false);
    doTest("Bar", name + "_after.as", false, false, false, name + ".as", name + "_2.as");
    assertEquals(1, findRenamedRefsToReferencedElementAtCaret().length);
  }

  public void testLiteralReference6() {
    final String name = getTestName(false);
    doTest("Bar", name + "_after.as", true, false, false, name + ".as");
    assertEquals(0, findRenamedRefsToReferencedElementAtCaret().length);
    assertEquals("Bar.as", myFixture.getFile().getName());
  }

  public void testLiteralReference7() {
    final String name = getTestName(false);
    doTest("Bar", name + "_after.as", true, true, false, name + ".as");
    assertEquals(1, findRenamedRefsToReferencedElementAtCaret().length);
    assertEquals("Bar.as", myFixture.getFile().getName());
  }

  public void testLiteralReference8() {
    final String name = getTestName(false);
    doTest("Bar", name + "_after.as", false, false, false, name + ".as");
    assertEquals(0, findRenamedRefsToReferencedElementAtCaret().length);
    assertEquals("Bar.as", myFixture.getFile().getName());
  }

  public void testLiteralReference9() {
    final String name = getTestName(false);
    doTest("Bar", name + "_after.as", false, true, false, name + ".as");
    assertEquals(1, findRenamedRefsToReferencedElementAtCaret().length);
    assertEquals("Bar.as", myFixture.getFile().getName());
  }

  public void testLiteralReference10() {
    final String name = getTestName(false);
    doTest("bar", name + "_after.as", false, false, false, name + ".as");
    assertEquals(0, findRenamedRefsToReferencedElementAtCaret().length);
  }

  public void testLiteralReference11() {
    final String name = getTestName(false);
    doTest("bar", name + "_after.as", false, true, false, name + ".as");
    assertEquals(1, findRenamedRefsToReferencedElementAtCaret().length);
  }

  public void testRenameFlexClassReferencedInCss() {
    final String name = getTestName(false);
    doTest(name + "Renamed", name + "_after.css", name + ".css", name + ".as");

    final PsiReference newReference = myFixture.getFile().findReferenceAt(myFixture.getEditor().getCaretModel().getOffset());
    final PsiElement newResolve = newReference == null ? null : newReference.resolve();
    assertTrue(newResolve instanceof JSClass &&
               "bar.RenameFlexClassReferencedInCssRenamed".equals(((JSClass)newResolve).getQualifiedName()));
  }

  @Override
  protected PsiElement findTarget() {
    PsiElement target = findTargetElement(myFixture.getEditor(), TargetElementUtil.ELEMENT_NAME_ACCEPTED | TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED);
    return ObjectUtils.coalesce(target, myFixture.getFile());
  }

  @Override
  protected void doTest(final String newName, String ext) {
    final String name = getTestName(false);
    doTest(newName, name + "_after." + ext, true, true, false,
           name + "." + ext); // current test data expects that string refs are renamed
  }

  protected void doTest(final String newName, String fileNameAfter, String... fileNames) {
    doTest(newName, fileNameAfter, false, true, false, fileNames);
  }

  protected void doTest(final String newName, boolean substituteElement, String... fileNames) {
    doTest(defaultParameters().withName(newName).substitute(substituteElement).withFiles(fileNames));
  }

  protected void doTest(final String newName, String fileNameAfter, boolean substituteElement, String... fileNames) {
    doTest(newName, fileNameAfter, substituteElement, false, false, fileNames);
  }

  protected void doTest(final String newName,
                        final String fileNameAfter,
                        final boolean substituteElement,
                        final boolean searchInCommentsAndStrings, //true by default
                        final boolean searchForTextOccurrences, //false by default
                        final String... fileNames) {
    myFixture.configureByFiles(fileNames);
    performDialogRename(defaultParameters()
                          .withName(newName)
                          .substitute(substituteElement)
                          .searchForTextOccurrences(searchForTextOccurrences)
                          .searchInCommentsAndStrings(searchInCommentsAndStrings));
    myFixture.checkResultByFile(fileNameAfter);
  }

  @Override
  protected PsiReference @NotNull [] findRenamedRefsToReferencedElementAtCaret() {
    PsiElement object = findTarget();
    assertNotNull(object);

    PsiReference[] references =
      ReferencesSearch.search(object, GlobalSearchScope.allScope(myFixture.getProject()), true).toArray(PsiReference.EMPTY_ARRAY);

    if (object instanceof JSFileImpl) {
      JSNamedElement element = ActionScriptResolveUtil.findMainDeclaredElement((JSFileImpl)object);
      if (element != null) object = element;
    }

    PsiElement additionalTarget = null;

    if (object instanceof JSClass) {
      additionalTarget = ((JSClass)object).getConstructor();
    }
    else if (object instanceof JSFunction && ((JSFunction)object).isConstructor()) {
      additionalTarget = object.getParent();
    }

    Set<PsiReference> uniquesSet = ContainerUtil.set(references);
    if (additionalTarget != null) {
      uniquesSet.addAll(ReferencesSearch.search(additionalTarget, GlobalSearchScope.allScope(myFixture.getProject()), true).findAll());
    }

    if (object instanceof JSFunction) {
      CommonProcessors.CollectProcessor<JSFunction> allFunctions =
        new CommonProcessors.CollectProcessor<>(Collections.synchronizedList(new ArrayList<>()));
      JSInheritanceUtil.iterateMethodsDown((JSFunction)object, allFunctions);
      for (JSFunction function : allFunctions.getResults()) {
        uniquesSet.addAll(ReferencesSearch.search(function, GlobalSearchScope.allScope(myFixture.getProject()), true).findAll());
      }
    }
    references = uniquesSet.toArray(PsiReference.EMPTY_ARRAY);
    return references;
  }

}
