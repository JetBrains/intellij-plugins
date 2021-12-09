// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.highlighting;

import com.intellij.application.options.CodeStyle;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupEx;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.codeInsight.template.impl.TemplateManagerImpl;
import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection;
import com.intellij.codeInspection.htmlInspections.HtmlUnknownBooleanAttributeInspection;
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection;
import com.intellij.flex.model.bc.LinkageType;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.flex.parser.FlexImporterTest;
import com.intellij.flex.util.ActionScriptDaemonAnalyzerTestCase;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.javascript.*;
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexBuildConfiguration;
import com.intellij.lang.javascript.formatter.ECMA4CodeStyleSettings;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.lang.javascript.highlighting.JavaScriptLineMarkerProvider;
import com.intellij.lang.javascript.inspection.JSUnusedAssignmentInspection;
import com.intellij.lang.javascript.inspections.JSMethodCanBeStaticInspection;
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection;
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection;
import com.intellij.lang.javascript.inspections.actionscript.JSImplicitlyInternalDeclarationInspection;
import com.intellij.lang.javascript.inspections.actionscript.JSUntypedDeclarationInspection;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSNamedElement;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.impl.ActionScriptClassImpl;
import com.intellij.lang.javascript.psi.resolve.JSClassResolver;
import com.intellij.lang.javascript.validation.fixes.ActionScriptCreateClassOrInterfaceFix;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.spellchecker.inspections.SpellCheckingInspection;
import com.intellij.spellchecker.quickfixes.RenameTo;
import com.intellij.testFramework.ExpectedHighlightingData;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.util.IncorrectOperationException;
import com.sixrr.inspectjs.validity.BadExpressionStatementJSInspection;
import com.sixrr.inspectjs.validity.FunctionWithInconsistentReturnsJSInspection;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ActionScriptHighlightingTest extends ActionScriptDaemonAnalyzerTestCase {
  @NonNls private static final String BASE_PATH = "/js2_highlighting/";
  protected Runnable myAfterCommitRunnable = null;

  @Override
  protected String getBasePath() {
    return BASE_PATH.substring(0, BASE_PATH.length() - 1);
  }

  @Override
  @NonNls
  protected String getExtension() {
    return "js2";
  }

  @NotNull
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @Override
  protected void setUp() throws Exception {
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "");
    super.setUp();
    myAfterCommitRunnable = null;
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), getTestRootDisposable());
  }

  @NotNull
  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  protected void doCommitModel(@NotNull ModifiableRootModel rootModel) {
    super.doCommitModel(rootModel);
    if (myAfterCommitRunnable != null) {
      myAfterCommitRunnable.run();
    }
  }

  public void testScriptHighlightingInE4X() throws Exception {
    //                       10        20         30           40         50
    //             01234567890123456789012 34567890123456 7 89012 34567890123456789
    String text = "var a = <xml>{ c }</xml>";
    configureByFile(BASE_PATH + getTestName(false) + ".js2");
    EditorHighlighter highlighter = HighlighterFactory.createHighlighter(myProject, getFile().getVirtualFile());
    highlighter.setText(text);
    HighlighterIterator iterator = highlighter.createIterator(15);
    assertEquals(JSTokenTypes.IDENTIFIER, iterator.getTokenType());
  }

  public void testNsOrModifierNotUnderClass() {
    Collection<HighlightInfo> infoCollection = defaultTest();
    IntentionAction action =
      findIntentionAction(infoCollection, JavaScriptBundle.message("javascript.fix.remove.access.modifier"), myEditor, myFile);
    assertNotNull(action);

    action = findIntentionAction(infoCollection, JavaScriptBundle.message("javascript.fix.remove.namespace.reference"), myEditor, myFile);
    assertNotNull(action);
  }

  public void testCreateParameter() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult(JavaScriptBundle.message("javascript.fix.create.parameter", "yyy"));
  }

  public void testCreateParameter_2() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult(JavaScriptBundle.message("javascript.fix.create.parameter", "yyy"));
  }

  public void testCreateParameter_3() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult(JavaScriptBundle.message("javascript.fix.create.parameter", "yyy"));
  }

  public void testCreateParameter2() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult(JavaScriptBundle.message("javascript.fix.create.parameter", "yyy"));
  }

  public void testReferencingPrivatesAndIncludeMembers() {
    doTestFor(true, getTestName(false) + ".js2", getTestName(false) + "_2.js2");
  }

  public void testHighlightExtends() {
    doTestFor(true, null, () -> {
      JSTestUtils.HighlightUsagesInfo handler = JSTestUtils.getFindUsagesHandlerHighlights(myEditor, myFile);
      List<PsiElement> targets = handler.targets;
      assertEquals(1, targets.size());
      assertTrue(targets.get(0).getText().contains("class Foo"));
      List<String> readUsages = handler.readUsages;
      assertEquals(1, readUsages.size());
      assertEquals("foo", readUsages.get(0));
    }, getTestName(false) + ".js2");
  }

  public void testHighlightImplements() {
    doTestFor(true, null, () -> {
      JSTestUtils.HighlightUsagesInfo handler = JSTestUtils.getFindUsagesHandlerHighlights(myEditor, myFile);
      assertNotNull(handler);
      List<PsiElement> targets = handler.targets;
      assertEquals(2, targets.size());
      assertTrue(targets.get(0).getText().contains("interface IFoo"));
      assertTrue(targets.get(1).getText().contains("interface IBar"));
      List<String> readUsages = handler.readUsages;
      assertEquals(2, readUsages.size());
      assertEquals("foo", readUsages.get(0));
      assertEquals("baz", readUsages.get(1));
    }, getTestName(false) + ".js2");
  }

  public void testParsleyAndRobotlegsMetadata() {
    defaultTest();
  }

  public void testSwizMetadata() {
    defaultTest();
  }

  public void testSpringActionScriptMetadata() {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithUnusedImports})
  public void testReferencingNameInE4X() {
    defaultTest();
  }

  public void testMethodCanBeStatic() throws Exception {
    JSMethodCanBeStaticInspection inspection = new JSMethodCanBeStaticInspection();
    inspection.myOnlyPrivate = false;
    enableInspectionTool(inspection);
    doSimpleHighlightingWithInvokeFixAndCheckResult("Make 'static'");
  }

  public void testMethodCanBeStatic2() throws Exception {
    JSMethodCanBeStaticInspection inspection = new JSMethodCanBeStaticInspection();
    inspection.myOnlyPrivate = false;
    enableInspectionTool(inspection);
    doSimpleHighlightingWithInvokeFixAndCheckResult("Make 'static'");
  }

  public void testMethodCanBeStatic3() throws Exception {
    JSMethodCanBeStaticInspection inspection = new JSMethodCanBeStaticInspection();
    inspection.myOnlyPrivate = false;
    enableInspectionTool(inspection);
    doSimpleHighlightingWithInvokeFixAndCheckResult("Make 'static'");
  }

  public void testMethodCanBeStatic4() {
    enableInspectionTool(new JSMethodCanBeStaticInspection());
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithGumboSdk})
  public void testMethodCanBeStaticImplicitVars() {
    JSMethodCanBeStaticInspection inspection = new JSMethodCanBeStaticInspection();
    inspection.myOnlyPrivate = false;
    enableInspectionTool(inspection);
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithUnusedImports})
  public void testCDATAInE4X() {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithUnusedImports, JSTestOption.WithFlexSdk})
  public void testReferencingNameInE4X2() {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithUnusedImports})
  public void testQNameProblem() {
    defaultTest();
  }

  public void testReferencingInternalClass() {
    String testName = getTestName(false);
    doTestFor(true, testName + ".js2", testName + "_2.js2");
  }

  public void testSpellChecker() throws Exception {
    enableInspectionTool(new SpellCheckingInspection());
    doSimpleHighlightingWithInvokeFixAndCheckResult(RenameTo.getFixName());
  }

  public void testSpellChecker_2() throws Exception {
    runRenameSpellcheckerFixWithChooseVariant("typo");
  }

  private void runRenameSpellcheckerFixWithChooseVariant(String variantName) throws Exception {
    TemplateManagerImpl.setTemplateTesting(getTestRootDisposable());
    enableInspectionTool(new SpellCheckingInspection());

    doSimpleHighlightingWithInvokeFixAndCheckResult(RenameTo.getFixName());

    final LookupEx lookup = LookupManager.getActiveLookup(myEditor);
    assertNotNull(lookup);
    boolean selected = false;

    for (LookupElement l : lookup.getItems()) {
      if (variantName.equals(l.getLookupString())) {
        selected = true;
        ((LookupImpl)lookup).finishLookup(Lookup.AUTO_INSERT_SELECT_CHAR, l);
      }
    }

    assertTrue(selected);

    LookupManager.hideActiveLookup(myProject);

    checkResultByFile(BASE_PATH + getTestName(false) + "_after2.js2");
  }

  public void testSpellChecker2() throws Exception {
    runRenameSpellcheckerFixWithChooseVariant("typo");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testProxy() {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithUnusedImports})
  public void testAnyAttrInE4X() {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    defaultTest();
  }

  public void testCheckingCast() {
    defaultTest();
  }

  public void testE4XPredefines() {
    defaultTest();
  }

  public void testGetObjectAndOtherMethodsWithPropertyRefs() {
    defaultTest();
  }

  public void testPrivateMethodForInterfaceImplementation() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Make method 'bar' public");
  }

  public void testDefaultProperty() {
    defaultTest();
  }

  public void testAS3NsOpenedByDefault() {
    String testName = getTestName(false);
    doTestFor(true, testName + ".js2", testName + "_2.js2");
  }

  public void testVirtualKeyword() {
    defaultTest();
  }

  public void testDeprecated() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Replace deprecated code with ResourceManager.getInstance().getResourceBundle()");
  }

  public void testDeprecated2() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Replace deprecated code with Bar.yyy");
  }

  public void testObjectMembers() {
    defaultTest();
  }

  public void testCircularDependency() {
    defaultTest();
  }

  public void testNestedClass() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testTypeEvalFails() {
    defaultTest();
  }

  public void testClassRefsInArrayElementType() {
    defaultTest();
  }

  public void testCreateClassAccessingProperty() {
    defaultTest();
  }

  public void testTypeReferenceInAs() {
    defaultTest();
  }

  public void testTypedArray() {
    defaultTest();
  }

  public void testNamespaceReferencedWithoutImport() {
    defaultTest();
  }

  public void testReportAboutUsingInterfaceInNew() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testBindableClassImplicitlyImplementsIEventDispatcher() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testBindableClassImplicitlyImplementsIEventDispatcher2() {
    doTestFor(true, new File(getTestDataPath() + BASE_PATH + getTestName(false)), (Runnable)null, getTestName(false) + "/Main.js2");
    final JSClassResolver resolver =
      JSDialectSpecificHandlersFactory.forLanguage(JavaScriptSupportLoader.ECMA_SCRIPT_L4).getClassResolver();
    assertNotNull(((ActionScriptClassImpl)resolver.findClassByQName("OtherClass", myModule.getModuleScope())).getStub());
    assertNotNull(((ActionScriptClassImpl)resolver.findClassByQName("OtherClass2", myModule.getModuleScope())).getStub());
  }

  public void testImplicitGetSet() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testHighlightQualifiedNameWithoutImport() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("mx.messaging.messages.AbstractMessage?");
  }

  public void testUsingFunctionDeclarations() {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    defaultTest();
  }

  public void testReportAccessorProblems() {
    defaultTest();
  }

  public void testReportAccessorProblems2() throws Exception {
    final Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck(JavaScriptBundle.message("javascript.fix.set.element.visibility", "property 'foo'", "internal"), "as",
                                         infoCollection);
  }

  public void testReportAccessorProblems3() throws Exception {
    final Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck(JavaScriptBundle.message("javascript.fix.set.element.visibility", "property 'foo'", "public"), "as",
                                         infoCollection);
  }

  public void testReportAccessorProblems4() {
    doTestFor(true, getTestName(false) + ".as");
  }

  public void testTypeReferenceInNewWithFunExpr() {
    defaultTest();
  }

  public void testAssignmentToConst() {
    defaultTest();
  }

  public void testIncludedMembers() {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".js2", testName + "_2.js2");
  }

  @JSTestOptions(JSTestOption.WithGumboSdk)
  public void testIncompatibleOverride() {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".js2");
  }

  public void testIncompatibleOverrideFix() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change return type to expected");
  }

  public void testIncompatibleOverrideFix2() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change parameters to expected");
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testIncompatibleOverrideFix3() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change return type to expected");
  }

  public void testIncompatibleOverrideFix4() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change return type to expected");
  }

  public void testIncompatibleOverrideFix5() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change Base.foo() signature");
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testIncompatibleOverrideFix6() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change parameters to expected");
  }

  public void testNoSuperConstructorCall() throws Exception {
    final String testName = getTestName(false);
    final Collection<HighlightInfo> infoCollection = doTestFor(true, testName + ".js2");
    findAndInvokeIntentionAction(infoCollection, "Create constructor matching super", myEditor, myFile);
    checkResultByFile(BASE_PATH + testName + "_after.js2");
  }

  public void testNoSuperConstructorCall2() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult("Insert super class constructor invocation", "js2");
  }

  public void testNoSuperConstructorCall3() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult("Remove initializer", "js2");
  }

  public void testNoSuperConstructorCall4() throws Exception {
    final String testName = getTestName(false);
    final Collection<HighlightInfo> infoCollection = doTestFor(true, testName + ".js2", testName + "_2.js2");
    findAndInvokeIntentionAction(infoCollection, "Create constructor matching super", myEditor, myFile);
    checkResultByFile(BASE_PATH + testName + "_after.js2");
  }

  public void testRemoveMethodWithAsdoc() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    doHighlightingWithInvokeFixAndCheckResult("Remove unused method 'Unused'", "js2");
  }

  public void testSuperConstructorCall() {
    defaultTest();
  }

  public void testDefaultConstructorVisibility() {
    defaultTest();
  }

  public void testFinalModifiers() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Remove final modifier");
  }

  public void testFinalModifiers2() {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testImportBeforePackage() {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".js2");
  }

  @JSTestOptions({JSTestOption.WithUnusedImports})
  public void testIncompatibleImplementation() {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".js2", testName + "_2.js2");
  }

  public void testIncompatibleImplementationFix() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change return type to expected");
  }

  public void testIncompatibleImplementationFix2() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change parameters to expected");
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testIncompatibleImplementationFix3() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change return type to expected");
  }

  public void testIncompatibleImplementationFix4() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change return type to expected");
  }

  public void testIncompatibleImplementationFix5() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change Int1.foo() signature");
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testIncompatibleImplementationFix6() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change parameters to expected");
  }

  public void testJSDoc() {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    final String testName = getTestName(false);
    doTestFor(true, testName + ".js2", testName + "_2.js2");
  }

  public void testJSDoc2() {
    defaultTest();
  }

  public void testDuplicates() {
    defaultTest();
  }

  public void testDuplicatesSmall() {
    defaultTest();
  }

  public void testFunctionSignatureMismatch() {
    enableCheckGuessedTypes();
    defaultTest();
  }

  public void testFunctionSignatureMismatch2() throws Exception {
    enableCheckGuessedTypes();
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change FunctionSignatureMismatch2.foo() signature");
  }

  public void testFunctionSignatureMismatch3() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change Base.foo() signature");
  }

  public void testFunctionSignatureMismatch4() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change Base.foo() signature");
  }

  public void testFunctionSignatureMismatch5() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change FunctionSignatureMismatch5.foo() signature");
  }

  public void testFunctionSignatureMismatch6() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change FunctionSignatureMismatch6.foo() signature");
  }

  public void testFunctionSignatureMismatch7() throws Exception {
    enableCheckGuessedTypes();
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change FunctionSignatureMismatch7.foo() signature");
  }

  public void testFunctionSignatureMismatch8() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change FunctionSignatureMismatch8.foo() signature");
  }

  public void testFunctionSignatureMismatch9() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change FunctionSignatureMismatch9.foo() signature");
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testFunctionSignatureMismatch10() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change FunctionSignatureMismatch10.foo() signature");
  }

  public void testFunctionSignatureMismatch11() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change FunctionSignatureMismatch11.foo() signature");
  }

  public void testFunctionSignatureMismatch12() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change FunctionSignatureMismatch12.say() signature");
  }

  public void testFunctionSignatureMismatch13() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change FunctionSignatureMismatch13.say() signature");
  }

  public void testFunctionSignatureMismatch14() throws Exception {
    enableCheckGuessedTypes();
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change FunctionSignatureMismatch14.zz() signature");
  }

  public void testFunctionSignatureMismatch15() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change FunctionSignatureMismatch15.foo() signature");
  }

  public void testFunctionSignatureMismatch16() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change FunctionSignatureMismatch16.foo() signature");
  }

  public void testFunctionSignatureMismatchRemoveParam1() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change FunctionSignatureMismatchRemoveParam1.foo() signature");
  }

  public void testFunctionSignatureMismatchRemoveParam2() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change FunctionSignatureMismatchRemoveParam2.foo() signature");
  }

  public void testFunctionSignatureMismatchRemoveParam3() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change FunctionSignatureMismatchRemoveParam3.foo() signature");
  }

  public void testFunctionSignatureMismatchRemoveParam4() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change FunctionSignatureMismatchRemoveParam4.foo() signature");
  }

  public void testFunctionSignatureMismatchChangeParam1() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change FunctionSignatureMismatchChangeParam1.foo() signature");
  }

  public void testFunctionSignatureMismatchFixInaccessible1() {
    final Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".js2");
    assertInaccessible(infos, "Change FunctionSignatureMismatchFixInaccessible1.foo() signature");
  }

  public void testFunctionSignatureMismatchAddParam1() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change FunctionSignatureMismatchAddParam1.foo() signature");
  }

  public void testFunctionSignatureMismatchAddParam2() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change FunctionSignatureMismatchAddParam2.foo() signature");
  }

  public void testFieldPrefix() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change FieldPrefix.ee() signature");
  }

  public void testPropertyPrefix() throws Exception {
    JSCodeStyleSettings jsCodeStyleSettings = CodeStyle.getSettings(getProject()).getCustomSettings(ECMA4CodeStyleSettings.class);
    String prefix = jsCodeStyleSettings.PROPERTY_PREFIX;
    jsCodeStyleSettings.PROPERTY_PREFIX = "prop";
    try {
      doSimpleHighlightingWithInvokeFixAndCheckResult("Change PropertyPrefix.ee() signature");
    }
    finally {
      jsCodeStyleSettings.PROPERTY_PREFIX = prefix;
    }
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testQualifyReferencesInArguments() throws Exception {
    enableCheckGuessedTypes();
    String testName = getTestName(false);
    String root = getTestDataPath() + getBasePath() + "/" + testName;
    Collection<HighlightInfo> infoCollection =
      doTestFor(true, new File(root + "/before"), (Runnable)null, testName + "/before/" + "Ref1.as");
    findAndInvokeIntentionAction(infoCollection, "Change Foo.func() signature", myEditor, myFile);
    FileDocumentManager.getInstance().saveAllDocuments();
    VirtualFile dirAfter = LocalFileSystem.getInstance().findFileByIoFile(new File(root + "/after"));
    VirtualFile actualDir = ProjectRootManager.getInstance(myProject).getFileIndex().getContentRootForFile(myFile.getVirtualFile());
    PlatformTestUtil.assertDirectoriesEqual(dirAfter, actualDir);
  }

  public void testConstructorSignatureMismatch1() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Create constructor 'ConstructorSignatureMismatch1'");
  }

  private void setActiveEditor(String relativePath) {
    VirtualFile file = myFile.getVirtualFile().findFileByRelativePath(relativePath);
    FileEditor[] editors = FileEditorManager.getInstance(myProject).getEditors(file);
    assertEquals(1, editors.length);
    FileEditor fileEditor = editors[0];
    assertTrue(fileEditor instanceof TextEditor);
    setActiveEditor(((TextEditor)fileEditor).getEditor());
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testConstructorSignatureMismatch2() throws Exception {
    Collection<HighlightInfo> infoCollection = defaultTestForTwoFiles();
    findAndInvokeIntentionAction(infoCollection, "Create constructor 'Foo'", myEditor, myFile);
    setActiveEditor("../" + getTestName(false) + "_2." + getExtension());
    checkResultByFile(BASE_PATH + getTestName(false) + "_after.js2");
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testConstructorSignatureMismatch3() throws Exception {
    Collection<HighlightInfo> infoCollection = defaultTestForTwoFiles();
    findAndInvokeIntentionAction(infoCollection, "Create constructor 'Foo'", myEditor, myFile);
    setActiveEditor("../" + getTestName(false) + "_2." + getExtension());
    checkResultByFile(BASE_PATH + getTestName(false) + "_after.js2");
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testCreateConstructor1() throws Exception {
    doTestCreateConstructor("Subclass1.js2", "SuperClass");
  }

  private void doTestCreateConstructor(String filename, String superClassName) throws IOException {
    String testName = getTestName(false);
    String root = getTestDataPath() + getBasePath() + "/" + testName;
    Collection<HighlightInfo> infoCollection =
      doTestFor(true, new File(root + "/before"), (Runnable)null, testName + "/before/" + filename);
    findAndInvokeIntentionAction(infoCollection, "Create constructor '" + superClassName + "'", myEditor, myFile);
    FileDocumentManager.getInstance().saveAllDocuments();
    VirtualFile dirAfter = LocalFileSystem.getInstance().findFileByIoFile(new File(root + "/after"));
    VirtualFile actualDir = ProjectRootManager.getInstance(myProject).getFileIndex().getContentRootForFile(myFile.getVirtualFile());
    PlatformTestUtil.assertDirectoriesEqual(dirAfter, actualDir);
  }

  public void testUnresolvedMembers2() {
    doTestFor(true, getTestName(false) + ".js2", getTestName(false) + "_2.js2");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testRegress() {
    defaultTest();
  }

  public void testRegress2() {
    defaultTest();
    final PsiReference ref = myFile.findReferenceAt(myEditor.getCaretModel().getOffset());
    assertTrue(ref instanceof PsiPolyVariantReference);
    final ResolveResult[] resolveResults = ((PsiPolyVariantReference)ref).multiResolve(false);
    assertTrue(2 == resolveResults.length);
  }

  public void testDynamicAttribute() {
    defaultTest();
  }

  public void testSeveralVisibilityModifiers() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Remove visibility modifier");
  }

  public void testQualifiedThings() {
    String testName = getTestName(false);
    doTestFor(true, testName + ".js2", testName + "_2.js2");
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testThisTypeIsDynamic() {
    defaultTest();
  }

  public void testAssignmentTypeMismatch() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Insert cast");
  }

  public void testAssignmentTypeMismatch_2() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Insert cast");
  }

  public void testAssignmentTypeMismatch_3() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Insert cast");
  }

  public void testAssignmentTypeMismatch_4() {
    checkActionAvailable("Insert cast", false);
  }

  public void testAssignmentTypeMismatch_5() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Insert cast");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testAssignmentTypeMismatch2() {
    defaultTest();
  }

  public void testAssignmentTypeMismatch3() {
    defaultTest();
  }

  public void testBinaryArgTypeMismatch() {
    defaultTest();
  }

  public void testReportingVoidTypeIssues() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Remove return value");
  }

  public void testCheckAnnotationAttributes() {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testForInTypeMismatch() {
    defaultTest();
  }

  public void testReturnTypeMismatch() {
    defaultTest();
  }

  public void testInvokedVariableTypeMismatch() {
    defaultTest();
  }

  public void testMissingReturnArgument() {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithUnusedImports})
  public void testCorrectScopeOfImports() {
    doTestFor(true, getTestName(false) + ".js2", getTestName(false) + "_2.js2");
  }

  @JSTestOptions({JSTestOption.WithUnusedImports, JSTestOption.WithFlexSdk})
  public void testCorrectScopeOfImports2() {
    defaultTest();
  }

  public void testUsingPropertyAsFunctionCall() throws Exception {
    Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".js2");
    findAndInvokeIntentionAction(infoCollection, "Remove argument list", myEditor, myFile);
    checkResultByFile(BASE_PATH + getTestName(false) + "_after.js2");
  }

  public void testImplicitlyDefined() {
    defaultTest();
  }

  public void testNSDeclarationUnderPackage() {
    defaultTest();
  }

  public void testVariableUnderPackage() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Rename variable 'Foo' to 'VariableUnderPackage'");
  }

  // TODO now stubs are created for variable in package
  //@JSTestOptions({/*JSTestOption.WithoutSourceRoot*/})
  //public void testUnresolvedMembers() throws Exception {
  //  doTestFor(true, getTestName(false) + ".js2");
  //}

  @JSTestOptions({JSTestOption.WithoutSourceRoot})
  public void testNoSourceRoot() {
    defaultTest();
  }

  public void testCheckReadWrite() {
    defaultTest();
  }

  public void testNamespaceElementReferences() {
    defaultTest();
  }

  public void testNamespaceElementReferences2() {
    defaultTest();
    JSReferenceExpression expr =
      PsiTreeUtil.getParentOfType(myFile.findElementAt(myEditor.getCaretModel().getOffset()), JSReferenceExpression.class);
    assertNotNull(expr);
    PsiElement exprResolve = expr.resolve();
    assertTrue(exprResolve instanceof JSFunction);

    JSClass clazz = PsiTreeUtil.getParentOfType(exprResolve, JSClass.class);
    assertNotNull(clazz);
    assertEquals("Foo", clazz.getQualifiedName());
  }

  public void testRefsInIncludes() {
    Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".js2");
    findAndInvokeIntentionAction(infos, "Create File RefsInIncludesz.js2", myEditor, myFile);
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testUnusedSymbols() {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    defaultTest();
  }

  public void testUnusedSymbols2() {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    defaultTest();
  }

  public void testUnusedSymbols3() {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    defaultTest();
  }

  public void testUnusedSymbols4_2() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    doSimpleHighlightingWithInvokeFixAndCheckResult("Remove unused namespace 'baz'");
  }

  public void testUnusedGlobalSymbols() {
    globalUnusedTestWith2Files();
  }

  public void testUnusedGlobalSymbols2() {
    globalUnusedTestWith2Files();
  }

  public void testUnusedGlobalSymbols3() {
    globalUnusedTestWith2Files();
  }

  public void testUnusedGlobalSymbols4() {
    enableInspectionTool(new JSUnusedGlobalSymbolsInspection());
    defaultTest();
  }

  public void testUnusedGlobalSymbols5() {
    enableInspectionTool(new JSUnusedGlobalSymbolsInspection());
    doTestFor(true, getTestName(false) + ".as");
  }

  private void globalUnusedTestWith2Files() {
    enableInspectionTool(new JSUnusedGlobalSymbolsInspection());
    String testName = getTestName(false);
    doTestFor(true, testName + ".js2", testName + "_2.js2");
  }

  public void testUnusedParameterHasCreateFieldQuickFix() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    doSimpleHighlightingWithInvokeFixAndCheckResult(JavaScriptBundle.message("javascript.create.field.intention.name", "xxx"));
  }

  public void testUnusedParameterHasCreateFieldQuickFix_2() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    doSimpleHighlightingWithInvokeFixAndCheckResult(JavaScriptBundle.message("javascript.create.field.intention.name", "_bar"));
  }

  public void testUnusedParameterHasCreateFieldQuickFix_3() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    doSimpleHighlightingWithInvokeFixAndCheckResult(JavaScriptBundle.message("javascript.create.field.intention.name", "value"));
  }

  public void testUnusedParameterHasCreateFieldQuickFix_4() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    doSimpleHighlightingWithInvokeFixAndCheckResult(JavaScriptBundle.message("javascript.create.field.intention.name", "_value"));
  }

  public void testUnusedParameterHasCreateFieldQuickFix_5() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    doSimpleHighlightingWithInvokeFixAndCheckResult(JavaScriptBundle.message("javascript.create.field.intention.name", "_bar"));
  }

  public void testUnusedParameterHasAssignToFieldQuickFix() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    doSimpleHighlightingWithInvokeFixAndCheckResult("Assign parameter 'xxx' to field");
  }

  public void testUnusedParameterHasAssignToFieldQuickFix_2() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    doSimpleHighlightingWithInvokeFixAndCheckResult("Assign parameter 'xxx' to field");
  }

  public void testUnusedParameterHasAssignToFieldQuickFix_3() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    doSimpleHighlightingWithInvokeFixAndCheckResult("Assign parameter 'xxx' to field");
  }

  public void testDuplicatedSymbols() {
    defaultTest();
  }

  public void testThisInStaticFunction() {
    defaultTest();
  }

  public void testDynamicUsageNoTypeCheck() {
    String testName = getTestName(false);
    doTestFor(true, testName + ".js2", testName + "_2.js2");
  }

  public void testParametersInStatics() {
    defaultTest();
  }

  public void testRestParameterIsNotMarkedAsHavingNoType() throws Exception {
    enableInspectionTool(new JSUntypedDeclarationInspection());
    doHighlightingWithInvokeFixAndCheckResult("Remove type reference", "js2");
  }

  public void testImplicitlyInternalDeclaration() throws Exception {
    enableInspectionTool(new JSImplicitlyInternalDeclarationInspection());
    doHighlightingWithInvokeFixAndCheckResult("Add explicit internal modifier", "js2");
  }

  public void testImplicitlyInternalDeclaration2() throws Exception {
    enableInspectionTool(new JSImplicitlyInternalDeclarationInspection());
    doHighlightingWithInvokeFixAndCheckResult("Add explicit internal modifier", "js2");
  }

  public void testImplicitlyInternalDeclaration3() {
    enableInspectionTool(new JSImplicitlyInternalDeclarationInspection());
    defaultTest();
  }

  public void testUntypedDeclarationForFunExpr() throws Exception {
    String fileName = getTestName(false);
    runUntypedDeclarationInspectionTestWithFix(fileName, new String[]{fileName + ".js2"}, "js2");
  }

  public void testUntypedDeclarationForFunExpr_2() throws Exception {
    String fileName = getTestName(false);
    runUntypedDeclarationInspectionTestWithFix(fileName, new String[]{fileName + ".js2"}, "js2");
  }

  public void testUntypedDeclarationForFun() throws Exception {
    String fileName = getTestName(false);
    runUntypedDeclarationInspectionTestWithFix(fileName, new String[]{fileName + ".js2"}, "js2");
  }

  public void testUntypedDeclarationForFun2() throws Exception {
    String fileName = getTestName(false);
    runUntypedDeclarationInspectionTestWithFix(fileName, new String[]{fileName + ".js2"}, "js2");
  }

  public void testUntypedDeclarationForFun2_2() throws Exception {
    String fileName = getTestName(false);
    runUntypedDeclarationInspectionTestWithFix(fileName, new String[]{fileName + ".js2"}, "js2");
  }

  public void testUntypedDeclarationForFun2_3() throws Exception {
    String fileName = getTestName(false);
    runUntypedDeclarationInspectionTestWithFix(fileName, new String[]{fileName + ".js2"}, "js2");
  }

  public void testAllowReferenceAnyFieldFromObjectType() {
    defaultTest();
  }

  public void testValidateClassAndPackageName() throws Exception {
    final Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".js2");
    IntentionAction action = findIntentionAction(infoCollection, "Rename File", myEditor, myFile);
    assertNotNull(action);

    findAndInvokeActionWithExpectedCheck("Rename class 'XXX' to 'ValidateClassAndPackageName'", "js2", infoCollection);
  }

  public void testImportClass() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("yyy.XXX?");
  }

  public void testDoNotImportJSDefinition() {
    final Collection<HighlightInfo> infos = defaultTestForTwoFiles();
    assertNull(findIntentionAction(infos, "foo.A.Bar?", getEditor(), getFile()));
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testImportClass_2() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("mypackage.Alert?");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testImportClass_3() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("mypackage.Alert?");
  }

  public void testImportNsAddsUseNamespace() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("foo.Foo?");
  }

  public void testNoImportClass() throws Exception {
    configureByFile(getBasePath() + "/" + getTestName(false) + ".js");
    List<HighlightInfo> infoList = doHighlighting();
    IntentionAction intentionAction = findIntentionAction(infoList, "show?", myEditor, myFile);
    assertNull(intentionAction);
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testImportFunction() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("mypackage.getDefinitionByName?");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testNoImportFunction() {
    Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".js2");
    IntentionAction action = findIntentionAction(infoCollection, "Bar.foo?", myEditor, myFile);
    assertNull(action);
    infoCollection = doTestFor(true, getTestName(false) + "_2.js2");
    action = findIntentionAction(infoCollection, "Bar.foo?", myEditor, myFile);
    assertNull(action);
  }

  public void testCreateClassOrInterfaceAction() {
    doCreateClassOrInterfaceTest("YYY", true, true, true);
  }

  public void testCreateClassOrInterfaceAction2() {
    doCreateClassOrInterfaceTest("YYY", false, true, true);
  }

  public void testCreateClassOrInterfaceAction3() {
    doCreateClassOrInterfaceTest("Abc", true, false, true);
  }

  public void testCreateClassOrInterfaceAction4() {
    doCreateClassOrInterfaceTest("Abc", false, false, true);
  }

  public void testCreateClassOrInterfaceAction5() {
    doCreateClassOrInterfaceTest("Abc", false, false, true);
  }

  private void doCreateClassOrInterfaceTestWithCheck(String name, boolean assertNoErrors) throws Exception {
    JSTestUtils.disableFileHeadersInTemplates(getProject());
    doCreateClassOrInterfaceTest(name, true, false, assertNoErrors);
    setActiveEditor("../foo/" + name + ".as");
    checkResultByFile(BASE_PATH + getTestName(false) + "_created.js2");
  }

  public void testCreateClassOrInterfaceAction6() throws Exception {
    doCreateClassOrInterfaceTestWithCheck("Abc", true);
  }

  public void testCreateClassOrInterfaceAction7() throws Exception {
    String newText = "package ${PACKAGE_NAME}#if (${PACKAGE_NAME} != \"\") #end{\n" +
                     "${Access_modifier} class ${NAME} #if (${Super_class_name} != \"\")extends ${Super_class_name}#end #if (${Implemented_interface_name} != \"\")implements ${Implemented_interface_name}#end{\n" +
                     " \n" +
                     "}\n" +
                     "}";
    String prevText =
      JSTestUtils
        .modifyTemplate(ActionScriptCreateClassOrInterfaceFix.ACTION_SCRIPT_CLASS_WITH_SUPERS_TEMPLATE_NAME, newText, getProject());
    try {
      doCreateClassOrInterfaceTestWithCheck("Abc", true);
    }
    finally {
      JSTestUtils
        .modifyTemplate(ActionScriptCreateClassOrInterfaceFix.ACTION_SCRIPT_CLASS_WITH_SUPERS_TEMPLATE_NAME, prevText, getProject());
    }
  }

  public void testCreateClassOrInterfaceAction8() throws Exception {
    String newText = "package ${PACKAGE_NAME}#if (${PACKAGE_NAME} != \"\") #end{\n" +
                     "${Access_modifier} class ${NAME} #if (${Super_class_name} != \"\")extends ${Super_class_name}#end #if (${Implemented_interface_name} != \"\")implements ${Implemented_interface_name}#end{\n" +
                     " \n" +
                     "/**\n" +
                     "* constr\n" +
                     "*/\n" +
                     "    public function ${NAME}() {\n" +
                     "        #if (${Super_class_name} != \"\")super();#end\n" +
                     "        // body\n" +
                     "    }" +
                     "}\n" +
                     "}";
    String prevText =
      JSTestUtils
        .modifyTemplate(ActionScriptCreateClassOrInterfaceFix.ACTION_SCRIPT_CLASS_WITH_SUPERS_TEMPLATE_NAME, newText, getProject());
    try {
      doCreateClassOrInterfaceTestWithCheck("Abc", false);
    }
    finally {
      JSTestUtils
        .modifyTemplate(ActionScriptCreateClassOrInterfaceFix.ACTION_SCRIPT_CLASS_WITH_SUPERS_TEMPLATE_NAME, prevText, getProject());
    }
  }

  public void testCreateClassOrInterfaceAction9() throws Exception {
    doCreateClassOrInterfaceTestWithCheck("Abc", true);
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testCreateClassOrInterfaceAction10() throws Exception {
    doCreateClassOrInterfaceTestWithCheck("Abc", true);
  }

  public void testCreateClassOrInterfaceAction11() throws Exception {
    doCreateClassOrInterfaceTestWithCheck("Abc", false);
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testCreateClassOrInterfaceAction12() throws Exception {
    doCreateClassOrInterfaceTestWithCheck("Foo", true);
  }

  private void doCreateClassOrInterfaceTest(final String name,
                                            boolean classNotInterface,
                                            boolean complementaryAvailable,
                                            boolean assertNoErrors)
    throws IncorrectOperationException {
    final Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".js2");
    String classIntentionName = JavaScriptBundle.message("javascript.create.class.intention.name", name);
    String interfaceIntentionName = JavaScriptBundle.message("javascript.create.interface.intention.name", name);
    String actionName = classNotInterface ? classIntentionName : interfaceIntentionName;
    final IntentionAction action = findIntentionAction(infoCollection, actionName, myEditor, myFile);

    assertNotNull(actionName, action);

    String complementaryActionName = classNotInterface ? interfaceIntentionName : classIntentionName;
    IntentionAction complementaryAction = findIntentionAction(infoCollection, complementaryActionName, myEditor, myFile);
    if (complementaryAvailable) {
      assertNotNull(String.format("Expected action with name %s but found null", complementaryActionName), complementaryAction);
    }
    else {
      assertNull(String.format("Expected no action with name %s but found one", complementaryActionName), complementaryAction);
    }

    WriteCommandAction.runWriteCommandAction(null, () -> action.invoke(myProject, myEditor, myFile));

    JSTestUtils.initJSIndexes(getProject());

    if (assertNoErrors) {
      List<HighlightInfo> infos = JSDaemonAnalyzerTestCase.filterUnwantedInfos(doHighlighting(), this);
      assertEquals(String.format("Expected no highlights, but was [%s]", StringUtil.join(infos, ", ")), 0, infos.size());
    }
  }

  public void testGenerics2() {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testVectorLiteral() {
    defaultTest();
  }

  public void testFunctionWithModifierNotUnderClass() {
    defaultTest();
  }

  public void testReportMissingReturn() {
    enableInspectionTool(new FunctionWithInconsistentReturnsJSInspection());
    defaultTest();
  }

  public void testReportMissingReturnFix() throws Exception {
    enableInspectionTool(new FunctionWithInconsistentReturnsJSInspection());
    doSimpleHighlightingWithInvokeFixAndCheckResult("Set return type to void");
  }

  public void testReportMissingReturnFix_2() throws Exception {
    enableInspectionTool(new FunctionWithInconsistentReturnsJSInspection());
    doSimpleHighlightingWithInvokeFixAndCheckResult("Add return statement");
  }

  public void testValidateFunctionAndPackageName() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Set package name to ''");
  }

  public void testValidateFunctionAndPackageName2() throws Exception {
    final String testName = getTestName(false);
    final Collection<HighlightInfo> infoCollection =
      doTestFor(true, new File(getTestDataPath() + getBasePath() + File.separatorChar + testName), (Runnable)null,
                testName + "/aaa/" + testName + ".js2");
    findAndInvokeActionWithExpectedCheck("Set package name to 'aaa'", "js2", infoCollection);
  }

  public void testMissingOverrideWithFix() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Add override modifier");
  }

  public void testMissingOverride_2() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Remove override modifier");
  }

  public void testMissingOverrideWithFix2() throws Exception {
    final Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".js2");

    IntentionAction action = findIntentionAction(infoCollection, "Add override modifier", myEditor, myFile);
    assertNull(action);

    findAndInvokeActionWithExpectedCheck("Remove access modifier", "js2", infoCollection);
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testOverridingMarkers() {
    doTestFor(true, () -> {
      final PsiElement at = invokeGotoSuperMethodAction("AAA");
      invokeShowImplementations(JSFunction.class, at);
    }, getTestName(false) + ".js2");
  }

  public void testOverrideTest() throws Exception {
    doOverrideMethodTestWithResultFileCheck();
  }

  public void testOverrideTestCustomNs() throws Exception {
    doOverrideMethodTestWithResultFileCheck();
  }

  public void testOverrideTestCustomNs_2() throws Exception {
    doOverrideMethodTestWithResultFileCheck();
  }

  public void testOverrideInInterface() {
    defaultTest();
  }

  public void testOverrideInInterfaceFix() {
    configureByFiles(null, BASE_PATH + getTestName(false) + ".js2");
    JSTestUtils.checkThatActionDisabled("OverrideMethods");
    JSTestUtils.checkThatActionDisabled("ImplementMethods");
  }

  public void testOverridePackageLocal() throws Exception {
    doOverrideMethodTestWithResultFileCheck();
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testOverrideFunWithRestParameter() throws Exception {
    doOverrideMethodTestWithResultFileCheck();
  }

  private void doOverrideMethodTestWithResultFileCheck() throws Exception {
    configureByFiles(null, BASE_PATH + getTestName(false) + ".js2");
    invokeNamedActionWithExpectedFileCheck(getTestName(false), "OverrideMethods", "js2");
  }

  public void testOverrideInInterfaceFix2() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Add override modifier");
  }

  public void testImplementTest() throws Exception {
    configureByFiles(null, BASE_PATH + getTestName(false) + ".js2");
    invokeNamedActionWithExpectedFileCheck(getTestName(false), "ImplementMethods", "js2");
  }

  @JSTestOptions(JSTestOption.WithJsSupportLoader)
  public void testShowImplementationsForStatic() {
    doTestFor(true, () -> {
                final PsiElement at = myFile.findElementAt(myEditor.getCaretModel().getOffset());
                JSTestUtils.testIncludeSelfInGoToImplementation(at, true);
                assertEquals(0, JSTestUtils.getGoToImplementationsResults(at).size());
              },
              getTestName(false) + ".js2");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testLocalClasses() {
    final String fileName = getTestName(false) + ".js2";
    doTestFor(true, () -> {
      PsiElement at = myFile.findElementAt(myEditor.getCaretModel().getOffset());
      invokeShowImplemenationsForLineMarker(at, 1);
      PsiElement elt = JSTestUtils.invokeShowImplementations(JSClass.class, at, 1, false);

      assertNull(ActionScriptClassResolver.findClassByQNameStatic("BaseType", elt));
      PsiElement byQName = JSClassResolver.findClassFromNamespace("BaseType", elt);
      assertEquals(fileName, byQName.getContainingFile().getName());

      byQName = JSClassResolver.findClassFromNamespace("DerivedType", elt);
      assertEquals(fileName, byQName.getContainingFile().getName());
    }, fileName, getTestName(false) + "_2.js2");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testGotoSuperWorksFromClass() {
    doTestFor(true, () -> {
      final PsiElement at = invokeGotoSuperMethodAction("AAA");
      JSTestUtils.invokeShowImplementations(JSClass.class, at, 1, false);
      invokeShowImplemenationsForLineMarker(at, 1);
    }, getTestName(false) + ".js2");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testShowImplementationsFromInterface() {
    doTestFor(true, () -> {
      final PsiElement at = myFile.findElementAt(myEditor.getCaretModel().getOffset());
      JSTestUtils.invokeShowImplementations(JSClass.class, at, 3, false);
      invokeShowImplemenationsForLineMarker(at, 3);
    }, getTestName(false) + ".js2");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testShowImplementationsFromInterfaceCall() {
    doTestFor(true, () -> {
      final PsiElement at = myFile.findElementAt(myEditor.getCaretModel().getOffset());
      JSTestUtils.invokeShowImplementations(JSFunction.class, at, 1, false);
    }, getTestName(false) + ".js2");
  }

  private PsiElement invokeGotoSuperMethodAction(@NonNls String destinationClassName) {
    return invokeActionWithCheck(destinationClassName, IdeActions.ACTION_GOTO_SUPER);
  }

  private PsiElement invokeActionWithCheck(@NonNls String destinationClassName, @NonNls final String actionName) {
    PlatformTestUtil.invokeNamedAction(actionName);
    final PsiElement at = myFile.findElementAt(myEditor.getCaretModel().getOffset());
    JSClass clazz = PsiTreeUtil.getParentOfType(at, JSClass.class);

    assertEquals(destinationClassName, clazz.getName());

    return at;
  }

  public static PsiElement invokeShowImplementations(final Class<? extends JSNamedElement> destinationClazz, final PsiElement at) {
    return JSTestUtils.invokeShowImplementations(destinationClazz, at, 1, true);
  }

  public static void invokeShowImplemenationsForLineMarker(PsiElement at, int num) {
    JSClass c = PsiTreeUtil.getParentOfType(at, JSClass.class);
    int items = (c.isInterface()
                 ? JavaScriptLineMarkerProvider.ourInterfaceImplementationsNavHandler
                 : JavaScriptLineMarkerProvider.ourClassInheritorsNavHandler).search(c).findAll().size();
    assertEquals(num, items);
  }

  @JSTestOptions(JSTestOption.WithoutWarnings)
  public void testMoreThanOneTopLevelSymbolDeclared() {
    final Collection<HighlightInfo> infoCollection = doTestFor(false);
    final IntentionAction action = findIntentionAction(infoCollection, "Remove externally visible symbol", myEditor, myFile);
    assertNotNull(action);
  }

  @JSTestOptions({/*JSTestOption.WithoutSourceRoot,*/ JSTestOption.WithFlexSdk})
  public void testMoreThanOneTopLevelSymbolDeclared2() {
    doTestFor(true);
  }

  @JSTestOptions({JSTestOption.WithUnusedImports})
  public void testImportInClass() {
    enableInspectionTool(new BadExpressionStatementJSInspection());
    doTestFor(true);
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testImplementingInterface() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, (Runnable)null, testName + ".js2", testName + "_2.js2");
    invokeNamedActionWithExpectedFileCheck(testName, "ImplementMethods", "js2");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testImplementingInterface2() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, (Runnable)null, testName + ".js2", testName + "_2.js2");
    invokeNamedActionWithExpectedFileCheck(testName, "ImplementMethods", "js2");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testImplementingInterfaceVector() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, (Runnable)null, testName + ".js2", testName + "_2.js2");
    invokeNamedActionWithExpectedFileCheck(testName, "ImplementMethods", "js2");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk, JSTestOption.WithUnusedImports})
  public void testOptimizeImports() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    final String testName = getTestName(false);
    Collection<HighlightInfo> infos = doTestFor(true, (Runnable)null, testName + ".js2", testName + "_2.js2");
    findAndInvokeIntentionAction(infos, "Optimize imports", myEditor, myFile);
    checkResultByFile(BASE_PATH + getTestName(false) + "_after.js2");
  }

  public void testNonImplementedInterface() throws Exception {
    final Collection<HighlightInfo> infoCollection = doTestFor(false);
    findAndInvokeIntentionAction(infoCollection, JavaScriptBundle.message("javascript.fix.implement.methods"), myEditor, myFile);
    checkResultByFile(BASE_PATH + getTestName(false) + "_after.js2");
    JSTestUtils.initJSIndexes(getProject());

    final Collection<HighlightInfo> infoCollection1 = filterUnwantedInfos(doHighlighting(), this);
    assertEquals(infoCollection.size() - 1, infoCollection1.size());
  }

  public void testNonImplementedInterface2() {
    doTestFor(false, () -> {
      final int offset = myEditor.getCaretModel().getOffset();
      invokeGotoSuperMethodAction("FirstClass");
      myEditor.getCaretModel().moveToOffset(offset);
      invokeActionWithCheck("FirstInterface", "GotoTypeDeclaration");
    });
  }

  public void testNonImplementedInterface3() {
    final String testName = getTestName(false);

    doTestFor(false, testName + ".js2", testName + "_2.js2");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testUnresolvedThisInCallback() {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    final String testName = getTestName(false);

    doTestFor(true, testName + ".js2");
  }

  public void testThisTypeInTopLevelFunction() {
    defaultTest();
  }

  public void testHighlightThingsFromUnopenedNamespaces() {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    final String testName = getTestName(false);

    doTestFor(true, testName + ".js2");
  }

  public void testDoNotResolveStuffFromObject() {
    defaultTest();
  }

  public void testHighlightInternalThingsFromOtherPackage() {
    defaultTest();
  }

  public void testImportWithStar() throws Exception {
    doTest(BASE_PATH + getTestName(false) + ".js2", true, false, true);
  }

  public void testWithStatement() throws Exception {
    doTest(BASE_PATH + getTestName(false) + ".js2", true, false, true);
  }

  public void testStringMethodsBug() {
    defaultTest();
  }

  public void testDynamicResolve() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithFlexFacet)
  public void testConditionalBlocks() {
    FlexTestUtils.modifyBuildConfiguration(myModule, bc -> bc.getCompilerOptions()
      .setAllOptions(
        Collections.singletonMap("compiler.define", "CONFIG::debugging\t")));

    enableInspectionTool(new BadExpressionStatementJSInspection());
    defaultTest();
  }

  public void testConstWithBindAnnotation() {
    defaultTest();
  }

  public void testGetMethod() {
    defaultTest();
  }

  public void testHighlightInternalThingsFromOtherFiles() {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    final String testName = getTestName(false);

    doTestFor(true, testName + ".js2", testName + "_2.js2");
  }

  public void testUsingNonPublicNamespace() {
    doTestFor(true, getTestName(false) + ".as", getTestName(false) + "_2.as");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithUnusedImports})
  public void testMethodFromNamespace() {
    doTestFor(true, getTestName(false) + ".as");
  }

  public void testNumberToString() {
    doTestFor(true, getTestName(false) + ".as");
  }

  @JSTestOptions({JSTestOption.WithGumboSdk})
  public void testVectorElementTypeIncompatible() {
    doTestFor(true, getTestName(false) + ".as");
  }


  @JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithUnusedImports})
  public void testNoFqnReplaceInsideNamesake() { // IDEADEV-37712
    doTestFor(true, getTestName(false) + ".as", getTestName(false) + "_2.as");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testImplicitImplementMarker_() {
    doTestFor(true, () -> {
      final PsiElement element =
        JSTestUtils.invokeShowImplementations(JSFunction.class, myFile.findElementAt(myEditor.getCaretModel().getOffset()), 1, false);
      assertTrue(element instanceof JSFunction);
      assertTrue(element.getParent() instanceof JSClass);
      assertEquals("Base", ((JSClass)element.getParent()).getQualifiedName());
    }, getTestName(false) + ".as");
  }


  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testQualified() {
    doTestFor(true, getTestName(false) + ".as", getTestName(false) + "_2.as");
  }

  public void testResolveToPackage() {
    doTestFor(true, getTestName(false) + ".js2", getTestName(false) + "_2.js2");
  }

  @JSTestOptions({JSTestOption.WithUnusedImports})
  public void testEmptyImport() {
    doTestFor(true, getTestName(false) + ".as", getTestName(false) + "_2.as");
  }

  public void testCastAmbiguousType() throws Exception {
    final Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".js2", getTestName(false) + "_2.js2");
    findAndInvokeActionWithExpectedCheck("Insert cast", "js2", infoCollection);
  }

  public void testStaticMethodInInterface() throws Exception {
    final Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck(JavaScriptBundle.message("javascript.fix.remove.static.modifier"), "as", infoCollection);
  }

  public void testMultipleVarsInPackage() throws Exception {
    final Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck(JavaScriptBundle.message("javascript.fix.remove.externally.visible.symbol"), "as", infoCollection);
  }

  public void testFieldCannotOverride() throws Exception {
    final Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck(JavaScriptBundle.message("javascript.fix.remove.override.modifier"), "as", infoCollection);
  }

  public void testNoStaticFunctionWithoutClass() {
    defaultTest();
  }

  public void testInaccessibleRefsReported() {
    Collection<HighlightInfo> highlightInfos = defaultTest();
    assertInaccessible(highlightInfos, "foo.InaccessibleRefsReported?");
    assertInaccessible(highlightInfos, "Create class 'InaccessibleRefsReported'");
  }

  public void testNamespaceDeclarationIsImplicitlyStatic() {
    defaultTest();
  }

  public void testStaticMethodCannotBeFinal() {
    defaultTest();
  }

  public void testReportWarningWhenUsingFieldOrMethodOnSomeDynamicClassInstance() {
    defaultTest();
  }

  public void testReportMultipleDynamicModifiers() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Remove dynamic modifier");
  }

  public void testAs2() {
    defaultTest();
  }

  public void testOverrideVisibility() throws Exception {
    final Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".as", getTestName(false) + "_2.as");
    findAndInvokeActionWithExpectedCheck(JavaScriptBundle.message("javascript.fix.set.element.visibility", "method 'foo'", "protected"), "as",
                                         infoCollection);
  }

  public void testOverrideVisibility2() throws Exception {
    final Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".as", getTestName(false) + "_2.as");
    findAndInvokeActionWithExpectedCheck(JavaScriptBundle.message("javascript.fix.set.element.visibility", "method 'foo'", "public"), "as",
                                         infoCollection);
  }

  @JSTestOptions(JSTestOption.WithUnusedImports)
  public void testNoPackageNameReplacement() {
    final String testName = getTestName(false);
    doTestFor(true, new File(getTestDataPath() + getBasePath() + File.separatorChar + testName), (Runnable)null,
              testName + "/com/view/Test.as",
              testName + "/com/view.as");
  }

  @JSTestOptions(JSTestOption.WithUnusedImports)
  public void testNoPackageNameReplacement2() {
    final String testName = getTestName(false);
    doTestFor(true, new File(getTestDataPath() + getBasePath() + File.separatorChar + testName), (Runnable)null,
              testName + "/com/zzz/Foo.as", testName + "/com/view/Test.as", testName + "/com/view.as");
  }

  @JSTestOptions(JSTestOption.WithoutSourceRoot)
  public void testNoCreateClassFixWithNoSourceRoots() {
    Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".as");
    IntentionAction intentionAction = findIntentionAction(infoCollection, "Create class Base", myEditor, myFile);
    assertNull(intentionAction);
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testConditionalCompileBlock() throws Exception {
    Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck("flash.events.KeyboardEvent?", "as", infoCollection);
  }

  public void testStaticBlock() {
    doTestFor(true, getTestName(false) + ".as");
  }

  // IDEA-56342
  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testMultinamesInDecompiledSwc() {
    myAfterCommitRunnable =
      () -> FlexTestUtils.addLibrary(myModule, "playerglobal", FlexImporterTest.getTestDataPath(), "PlayerGlobal10.swc", null, null);
    doTestFor(true, getTestName(false) + ".as");
  }

  public void testSetterOptionalParam() throws Exception {
    Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck("Remove parameter default value", "as", infoCollection);
  }

  public void testDelegateMethodsDisabled() {
    configureByFiles(null, BASE_PATH + getTestName(false) + ".js2");
    JSTestUtils.checkThatActionDisabled("DelegateMethods");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testDelegateMethods() throws Exception {
    String testName = getTestName(false);
    configureByFiles(null, BASE_PATH + testName + ".js2", BASE_PATH + testName + "_2.js2");
    invokeNamedActionWithExpectedFileCheck(testName, "DelegateMethods", "js2");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testDelegateMethods2() throws Exception {
    String testName = getTestName(false);
    configureByFiles(null, BASE_PATH + testName + ".js2", BASE_PATH + testName + "_2.js2");
    invokeNamedActionWithExpectedFileCheck(testName, "DelegateMethods", "js2");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testDelegateMethods3() throws Exception {
    String testName = getTestName(false);
    configureByFiles(null, BASE_PATH + testName + ".js2", BASE_PATH + testName + "_2.js2");
    invokeNamedActionWithExpectedFileCheck(testName, "DelegateMethods", "js2");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testDelegateMethods4() throws Exception {
    String testName = getTestName(false);
    configureByFiles(null, BASE_PATH + testName + ".js2", BASE_PATH + testName + "_2.js2", BASE_PATH + testName + "_3.js2");
    invokeNamedActionWithExpectedFileCheck(testName, "DelegateMethods", "js2");
  }

  public void testDelegateMethods5() throws Exception {
    String testName = getTestName(false);
    configureByFiles(null, BASE_PATH + testName + ".js2", BASE_PATH + testName + "_2.js2");
    invokeNamedActionWithExpectedFileCheck(testName, "DelegateMethods", "js2");
  }

  public void testRemoveGetterParameter() throws Exception {
    final Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck(JavaScriptBundle.message("javascript.fix.remove.parameter"), "as", infoCollection);
  }

  public void testRemoveGetterParameters() throws Exception {
    final Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck(JavaScriptBundle.message("javascript.fix.remove.parameters"), "as", infoCollection);
  }

  public void testImportForNeighbourClass() {
    doTestFor(true, getTestName(false) + ".as", getTestName(false) + "_2.as");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testInvalidAttribute() {
    doTestFor(true, getTestName(false) + ".as");
  }

  public void testNoTypeGuessFromAsdoc() throws Exception {
    final Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck(JavaScriptBundle.message("javascript.create.field.intention.name", "aa"), "as", infos);
  }

  public void testNoTypeGuessFromAsdoc2() throws Exception {
    final Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck(JavaScriptBundle.message("javascript.create.field.intention.name", "aa"), "as", infos);
  }

  public void testCreateFieldInOtherFile() throws Exception {
    final String testName = getTestName(false);
    final Collection<HighlightInfo> infos = doTestFor(true, testName + ".as", testName + "_other.as");
    findAndInvokeIntentionAction(infos, JavaScriptBundle.message("javascript.create.field.intention.name", "zzz"), myEditor, myFile);
    setActiveEditor(createEditor(myFile.getVirtualFile().findFileByRelativePath("../" + getTestName(false) + "_other.as")));
    checkResultByFile(BASE_PATH + getTestName(false) + "_other_after.as");
  }

  public void testNoImportSuggestForTestClass() {
    myAfterCommitRunnable = () -> {
      final VirtualFile testsRoot = findVirtualFile(BASE_PATH + getTestName(false) + "_2");
      PsiTestUtil.addSourceContentToRoots(myModule, testsRoot, true);
    };
    final Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".as");
    assertInaccessible(infos, "a.TestClass?");
  }

  public void testImportSuggestForProductionClass() {
    myAfterCommitRunnable = () -> {
      final ContentEntry[] contentEntries = ModuleRootManager.getInstance(myModule).getContentEntries();
      final VirtualFile file = contentEntries[0].getFile();
      PsiTestUtil.removeContentEntry(myModule, contentEntries[0].getFile());
      PsiTestUtil.addSourceContentToRoots(myModule, file, true);
      final VirtualFile productionRoot = findVirtualFile(BASE_PATH + getTestName(false) + "_2");
      PsiTestUtil.addSourceContentToRoots(myModule, productionRoot, false);
    };
    final Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".as");
    assertNotNull(findIntentionAction(infos, "a.ProductionClass?", myEditor, myFile));
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testExtraEmptyLineInImport() throws Exception {
    final Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".as", getTestName(false) + "_2.as");
    findAndInvokeActionWithExpectedCheck("cocoa.plaf.LookAndFeelUtil?", "as", infoCollection);
  }

  @JSTestOptions(JSTestOption.WithGumboSdk)
  public void testNoChangeSignatureForLibraryMethod() {
    final Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".js2");
    assertInaccessible(infos, "Change decodeURI() signature");
  }

  public void testStripQuotes1() {
    checkActionAvailable("Insert cast", true);
  }

  public void testStripQuotes2() {
    checkActionAvailable("Insert cast", true);
  }

  public void testStripQuotes3() {
    checkActionAvailable("Insert cast", true);
  }

  public void testStripQuotes4() {
    checkActionAvailable("Insert cast", true);
  }

  public void testStripQuotes5() {
    checkActionAvailable("Insert cast", false);
  }

  public void testStripQuotes6() {
    checkActionAvailable("Insert cast", false);
  }

  public void testStripQuotes7() {
    checkActionAvailable("Insert cast", false);
  }

  private void checkActionAvailable(String name, boolean available) {
    Collection<HighlightInfo> infos = defaultTest();
    IntentionAction action = findIntentionAction(infos, name, myEditor, myFile);
    if (available) {
      assertNotNull(action);
    }
    else {
      assertNull(action);
    }
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testNoCreateFieldInSdkClass() {
    Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".as");
    assertInaccessible(infos, JavaScriptBundle.message("javascript.create.field.intention.name", "foo"));
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testNoCreateMethodInLibraryClass() {
    myAfterCommitRunnable =
      () -> FlexTestUtils.addLibrary(myModule, "Lib", getTestDataPath() + BASE_PATH, "ImplementingMarkerFromSwc.swc", null, null);
    Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".as");
    assertInaccessible(infos, JavaScriptBundle.message("javascript.create.method.intention.name", "bar"));
  }

  public void testCreateMethodForClassInPackage() throws Exception {
    Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck("Create method 'baz'", "as", infos);
  }

  public void testInternalClassFromFileLocal() throws Exception {
    Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".as");
    assertInaccessible(infos, JavaScriptBundle.message("javascript.create.method.intention.name", "z"));
    findAndInvokeActionWithExpectedCheck("Make method 'z' public", "as", infos);
  }

  public void testInternalClassFromFileLocal2() throws Exception {
    Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck("Make class 'InternalClassFromFileLocal2' public", "as", infos);
  }

  public void testRelaxVisibilityFix() throws Exception {
    Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".as");
    assertInaccessible(infos, JavaScriptBundle.message("javascript.create.field.intention.name", "v"));
    assertInaccessible(infos, JavaScriptBundle.message("javascript.create.constant.field.intention.name", "v"));
    findAndInvokeActionWithExpectedCheck("Make field 'v' internal", "as", infos);
  }

  public void testRelaxVisibilityFix2() throws Exception {
    Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".as");
    assertInaccessible(infos, JavaScriptBundle.message("javascript.create.method.intention.name", "foo"));
    findAndInvokeActionWithExpectedCheck("Make method 'foo' public", "as", infos);
  }

  public void testRelaxVisibilityFix3() throws Exception {
    Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck("Make field 'foo' internal", "as", infos);
  }

  public void testRelaxVisibilityFix4() throws Exception {
    Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck("Make field 'foo' protected", "as", infos);
  }

  public void testRelaxVisibilityFix5() throws Exception {
    Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck("Make field 'foo' protected", "as", infos);
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testChangeVarTypeFix1() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change 'v2' type to 'flash.display.Sprite'");
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testChangeVarTypeFix2() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change 'v' type to 'mypackage.IResourceManager'");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testChangeVarTypeFix3() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change 'v2' type to 'Vector.<String>'");
  }

  public void testChangeParamTypeFix() throws Exception {
    final Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".js2");
    assertInaccessible(infoCollection, "Change 'i' type to 'ChangeParamTypeFix'");
    findAndInvokeActionWithExpectedCheck("Change ChangeParamTypeFix.foo() signature", "js2", infoCollection);
  }

  public void testChangeReturnTypeFix1() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Make 'foo' return 'String'");
  }

  public void testChangeReturnTypeFix2() throws Exception {
    final Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".js2");
    assertInaccessible(infoCollection, "Make 'foo' return 'String'");
    findAndInvokeActionWithExpectedCheck("Change Foo123.foo() signature", "js2", infoCollection);
  }

  public void testChangeReturnTypeFix3() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Make 'foo' return 'ChangeReturnTypeFix3'");
  }

  public void testCreateImplementsFix1() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Make 'CreateImplementsFix1' implement 'com.foo.MyInt'");
  }

  public void testCreateImplementsFix2() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Make 'CreateImplementsFix2' implement 'com.foo.MyInt'");
  }

  public void testCreateExtendsFix1() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Make 'CreateExtendsFix1' extend 'C'");
  }

  public void testCreateExtendsFix2() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Make 'CreateExtendsFix2' extend 'C'");
  }

  public void testCreateExtendsFix3() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Make 'C' extend 'com.D'");
  }

  public void testCreateExtendsFix4() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Make 'I2' extend 'I1'");
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testChangeSignatureFixForIncompatibleReturnType1() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change ChangeSignatureFixForIncompatibleReturnType1.foo() signature");
  }

  public void testChangeSignatureFixForIncompatibleReturnType2() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change ChangeSignatureFixForIncompatibleReturnType2.foo() signature");
  }

  public void testNoChangeSignatureFixForVoidType() {
    Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".as");
    assertInaccessible(infos, "Change NoChangeSignatureFixForVoidType.bar() signature");
  }

  public void testNoChangeTypeFixForVoidType() {
    Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".as");
    assertInaccessible(infos, "Change 'v' type to 'void'");
  }

  private void assertInaccessible(Collection<HighlightInfo> infos, String actionName) {
    assertNull("Action '" + actionName + "' should not be available", findIntentionAction(infos, actionName, myEditor, myFile));
  }

  public void testBadResolveOfSuperclass() throws Exception {
    final Module module2 = doCreateRealModuleIn("module2", myProject, FlexModuleType.getInstance());
    final Ref<VirtualFile> fileFromModule2 = new Ref<>();
    final Ref<Sdk> sdk1 = new Ref<>();
    final Ref<Sdk> sdk2 = new Ref<>();
    myAfterCommitRunnable = () -> {
      sdk1.set(FlexTestUtils.createSdk(getTestDataPath() + BASE_PATH + "fake_sdk", "4.0.0", getTestRootDisposable()));
      {
        SdkModificator m = sdk1.get().getSdkModificator();
        m.removeAllRoots();
        m.addRoot(sdk1.get().getHomeDirectory().findChild("common_root"), OrderRootType.CLASSES);
        m.addRoot(sdk1.get().getHomeDirectory().findChild("flex_root"), OrderRootType.CLASSES);
        m.commitChanges();
      }
      sdk2.set(FlexTestUtils.createSdk(getTestDataPath() + BASE_PATH + "fake_sdk", "4.0.0", getTestRootDisposable()));
      {
        SdkModificator m = sdk2.get().getSdkModificator();
        m.removeAllRoots();
        m.addRoot(sdk2.get().getHomeDirectory().findChild("common_root"), OrderRootType.CLASSES);
        m.addRoot(sdk2.get().getHomeDirectory().findChild("air_root"), OrderRootType.CLASSES);
        m.commitChanges();
      }
      fileFromModule2.set(copyFileToModule(module2, getTestDataPath() + BASE_PATH + getTestName(false) + "2.as"));
    };
    configureByFile(BASE_PATH + getTestName(false) + "1.as");

    FlexTestUtils.modifyConfigs(myProject, e -> {
      FlexTestUtils.setSdk(e.getConfigurations(myModule)[0], sdk1.get());
      FlexTestUtils.setSdk(e.getConfigurations(module2)[0], sdk2.get());
    });

    checkHighlighting(new ExpectedHighlightingData(myEditor.getDocument(), true, true, false));

    myFile = PsiManager.getInstance(myProject).findFile(fileFromModule2.get());
    myEditor = createEditor(fileFromModule2.get());
    checkHighlighting(new ExpectedHighlightingData(myEditor.getDocument(), true, true, false));
  }

  private VirtualFile copyFileToModule(Module module, String filePath) {
    try {
      VirtualFile dir = getOrCreateProjectBaseDir().createChildDirectory(this, "module2");
      PsiTestUtil.addSourceRoot(module, dir);
      VirtualFile f = LocalFileSystem.getInstance().findFileByPath(filePath);
      return VfsUtilCore.copyFile(this, f, dir);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void testUnusedVariableValues() {
    enableInspectionTool(new JSUnusedAssignmentInspection());
    enableInspectionTool(new JSUnusedGlobalSymbolsInspection());

    defaultTest();
  }

  public void testUnusedVariableValues2() {
    enableInspectionTool(new JSUnusedAssignmentInspection());

    defaultTest();
  }

  public void testUnresolvedReferencePattern() {
    defaultTest();
  }

  public void testImplicitImplementationByPublicBindableProperty() {
    defaultTest();
  }

  public void testInitializeParameterFix() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult("Initialize parameter", "as");
  }

  public void testSuperclassResolveMixedRoots() {
    // we need two SDKs so that flash.net.FileReference from SDK 4.6/playerglobal.swc
    // has more recent timestamp than its brother from SDK 4.5/airglobal.swc
    final Sdk sdk45 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.5"), null, true, getTestRootDisposable());
    final Sdk sdk46 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.6"), null, false, getTestRootDisposable());
    FlexTestUtils.modifyConfigs(myProject, e -> {
      ModifiableFlexBuildConfiguration bc1 = e.getConfigurations(myModule)[0];
      bc1.setTargetPlatform(TargetPlatform.Desktop);
      FlexTestUtils.setSdk(bc1, sdk45);

      ModifiableFlexBuildConfiguration bc2 = e.createConfiguration(myModule);
      bc2.setName("2");
      bc1.setTargetPlatform(TargetPlatform.Mobile);
      FlexTestUtils.setSdk(bc2, sdk46);
    });
    defaultTest();
  }

  public void testFieldResolveMixedRoots() {
    // same as testSuperclassResolveMixedRoots()
    final Sdk sdk45 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.5"), null, true, getTestRootDisposable());
    final Sdk sdk46 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.6"), null, false, getTestRootDisposable());
    FlexTestUtils.modifyConfigs(myProject, e -> {
      ModifiableFlexBuildConfiguration bc1 = e.getConfigurations(myModule)[0];
      bc1.setTargetPlatform(TargetPlatform.Desktop);
      FlexTestUtils.setSdk(bc1, sdk45);

      ModifiableFlexBuildConfiguration bc2 = e.createConfiguration(myModule);
      bc2.setName("2");
      bc1.setTargetPlatform(TargetPlatform.Mobile);
      FlexTestUtils.setSdk(bc2, sdk46);
    });
    defaultTest();
  }

  public void testHangingCommaInRefList() {
    defaultTest();
  }

  public void testAddOverrideModifierWithMetadata() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult("Add override modifier", "as");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testFileLocalClassInheritance() {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testCorrectScopeForSuperclassCheck() {
    doTestFor(true, getTestName(false) + ".js2", getTestName(false) + "_2.js2");
  }

  public void testSameClassAndPackage() {
    doTestFor(true, getTestName(false) + ".js2", getTestName(false) + "_2.js2");
  }

  public void testNonExistingMethodAfterNew() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult(JavaScriptBundle.message("javascript.create.method.intention.name", "zzz"), "js2");
  }

  public void testCorrectScopeForSuperclassCheck2() { //
    final Sdk sdk46 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.6"), null, false, getTestRootDisposable());
    FlexTestUtils.modifyConfigs(myProject, e -> {
      ModifiableFlexBuildConfiguration bc1 = e.getConfigurations(myModule)[0];
      bc1.setName("web");
      bc1.setTargetPlatform(TargetPlatform.Web);
      FlexTestUtils.setSdk(bc1, sdk46);

      ModifiableFlexBuildConfiguration bc2 = e.createConfiguration(myModule);
      bc2.setName("air");
      bc1.setTargetPlatform(TargetPlatform.Desktop);
      FlexTestUtils.setSdk(bc2, sdk46);
    });
    FlexBuildConfigurationManager m = FlexBuildConfigurationManager.getInstance(myModule);
    m.setActiveBuildConfiguration(m.findConfigurationByName("air"));
    defaultTest();
  }

  public void testCorrectScopeForSuperclassCheck3() { // IDEA-91539
    final Sdk sdk46 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.6"), null, false, getTestRootDisposable());
    FlexTestUtils.modifyConfigs(myProject, e -> {
      ModifiableFlexBuildConfiguration bc1 = e.getConfigurations(myModule)[0];
      bc1.setTargetPlatform(TargetPlatform.Desktop);
      FlexTestUtils.setSdk(e.getConfigurations(myModule)[0], sdk46);
    });
    doTestFor(true, getTestName(false) + ".js2", getTestName(false) + "_2.mxml");
  }

  public void testCorrectScopeForSuperclassCheck4() {
    // two dependent modules, different SDKs
    final Module module2 = doCreateRealModuleIn("module2", myProject, FlexModuleType.getInstance());
    final Sdk sdk45 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.5"), null, false, getTestRootDisposable());
    final Sdk sdk46 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.6"), null, false, getTestRootDisposable());
    FlexTestUtils.modifyConfigs(myProject, e -> {
      ModifiableFlexBuildConfiguration bc1 = e.getConfigurations(myModule)[0];
      FlexTestUtils.setSdk(bc1, sdk46);

      ModifiableFlexBuildConfiguration bc2 = e.getConfigurations(module2)[0];
      bc2.setOutputType(OutputType.Library);
      FlexTestUtils.setSdk(bc2, sdk45);

      bc1.getDependencies().getModifiableEntries().add(e.createBcEntry(bc1.getDependencies(), bc2, null));
    });

    myAfterCommitRunnable = () -> copyFileToModule(module2, getTestDataPath() + BASE_PATH + getTestName(false) + "_2.js2");
    doTestFor(true, getTestName(false) + ".js2");
  }

  public void testCorrectScopeForSuperclassCheck5() {
    // same fqn in SDK and library:
    // Module depends on an swc that has SDK classes compiled in.
    // Library classes (swc compiled with SDK 4.6) have newer timestamp than classes of SDK that module depends on (SDK 4.5)
    // SDK should have no sources attached (otherwise getNavigationElement() for both classes will bring us to the same file)
    // see testData\js2_highlighting\CorrectScopeForSuperclassCheck5_src.zip

    final Sdk sdk45 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.5"), null, false, getTestRootDisposable());
    FlexTestUtils.addFlexLibrary(false, myModule, "foobar", true, getTestDataPath(), BASE_PATH + getTestName(false) + ".swc", null, null,
                                 LinkageType.Merged);

    FlexTestUtils.modifyConfigs(myProject, e -> {
      ModifiableFlexBuildConfiguration bc1 = e.getConfigurations(myModule)[0];
      FlexTestUtils.setSdk(bc1, sdk45);
    });
    doTestFor(true, getTestName(false) + ".js2");
  }

  public void testCorrectScopeForSuperclassCheck6() {
    // monkey patching SDK class
    final Sdk sdk45 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.5"), null, false, getTestRootDisposable());

    FlexTestUtils.modifyConfigs(myProject, e -> {
      ModifiableFlexBuildConfiguration bc1 = e.getConfigurations(myModule)[0];
      FlexTestUtils.setSdk(bc1, sdk45);
    });
    doTestFor(true, getTestName(false) + ".js2", getTestName(false) + "_2.js2");
  }

  public void testCorrectScopeForSuperclassCheck7() {
    // same FQNs in different modules
    final Module module2 = doCreateRealModuleIn("module2", myProject, FlexModuleType.getInstance());

    myAfterCommitRunnable = () -> copyFileToModule(module2, getTestDataPath() + BASE_PATH + getTestName(false) + "_2.js2");

    configureByFiles(null, BASE_PATH + getTestName(false) + ".js2");
    final JSClassResolver resolver =
      JSDialectSpecificHandlersFactory.forLanguage(JavaScriptSupportLoader.ECMA_SCRIPT_L4).getClassResolver();
    final PsiElement class1 = resolver.findClassByQName("com.foo.Foo", myModule.getModuleScope());
    assertNotNull(class1);
    final PsiElement class2 = resolver.findClassByQName("com.foo.Foo", module2.getModuleScope());
    assertNotNull(class2);
    assertFalse(class1.isEquivalentTo(class2));
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testCreateVarOfArrayType() throws Exception {
    final Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck(JavaScriptBundle.message("javascript.create.variable.intention.name", "x"), "as", infoCollection);
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testCreateObjectVar() throws Exception {
    final Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck(JavaScriptBundle.message("javascript.create.variable.intention.name", "foo"), "as", infoCollection);
  }

  public void testVectorWithSdk() {
    final Sdk sdk45 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.5"), null, true, getTestRootDisposable());
    FlexTestUtils.modifyConfigs(myProject, editor -> {
      ModifiableFlexBuildConfiguration bc1 = editor.getConfigurations(myModule)[0];
      FlexTestUtils.setSdk(bc1, sdk45);
    });
    doTestFor(true, getTestName(false) + ".as");
  }

  public void testOptionalParams() {
    doTestFor(true, getTestName(false) + ".as");
  }

  public void testAccessInternalMemberFromProperty() {
    // IDEA-139240
    doTestFor(true, getTestName(false) + ".as", getTestName(false) + "_2.as");
  }

  public void testDoNotResolveClassMemberWhereXmlTagExpected() {
    // IDEA-138900
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testProtectedMembersWithImplicitInheritance() {
    defaultTest(); // IDEA-146722
  }

  public void testArgumentsInParenthesis() {
    defaultTest(); // IDEA-153275
  }

  public void testNoHtmlInspectionsForXmlLiteral() {
    enableInspectionTools(new HtmlUnknownTagInspection(),
                          new HtmlUnknownAttributeInspection(),
                          new HtmlUnknownBooleanAttributeInspection());
    defaultTest();
  }
}
