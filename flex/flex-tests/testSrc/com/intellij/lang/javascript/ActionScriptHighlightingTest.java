package com.intellij.lang.javascript;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzerSettings;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.highlighting.HighlightUsagesHandler;
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupEx;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.codeInsight.template.impl.TemplateManagerImpl;
import com.intellij.flex.FlexTestUtils;
import com.intellij.flex.model.bc.LinkageType;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.javascript.flex.mxml.schema.FlexSchemaHandler;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexBuildConfiguration;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.lang.javascript.highlighting.JavaScriptLineMarkerProvider;
import com.intellij.lang.javascript.inspections.JSMethodCanBeStaticInspection;
import com.intellij.lang.javascript.inspections.JSUnusedAssignmentInspection;
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection;
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection;
import com.intellij.lang.javascript.inspections.actionscript.JSImplicitlyInternalDeclarationInspection;
import com.intellij.lang.javascript.inspections.actionscript.JSUntypedDeclarationInspection;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSNamedElement;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSClassResolver;
import com.intellij.lang.javascript.validation.fixes.CreateClassOrInterfaceFix;
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
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.spellchecker.inspections.SpellCheckingInspection;
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

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

public class ActionScriptHighlightingTest extends ActionScriptDaemonAnalyzerTestCase {
  @NonNls private static final String BASE_PATH = "/js2_highlighting/";

  private Runnable myAfterCommitRunnable = null;

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @Override
  protected void setUp() throws Exception {
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexSchemaHandler.class.getResource("z.xsd"))),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));
    super.setUp();
    myAfterCommitRunnable = null;
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass());
  }

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

  public void testNsOrModifierNotUnderClass() throws Exception {
    Collection<HighlightInfo> infoCollection = defaultTest();
    IntentionAction action =
      findIntentionAction(infoCollection, JSBundle.message("javascript.fix.remove.access.modifier"), myEditor, myFile);
    assertNotNull(action);

    action = findIntentionAction(infoCollection, JSBundle.message("javascript.fix.remove.namespace.reference"), myEditor, myFile);
    assertNotNull(action);
  }

  public void testCreateParameter() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Create parameter");
  }

  public void testCreateParameter_2() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Create parameter");
  }

  public void testCreateParameter_3() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Create parameter");
  }

  public void testCreateParameter2() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Create parameter");
  }

  public void testReferencingPrivatesAndIncludeMembers() throws Exception {
    doTestFor(true, getTestName(false) + ".js2", getTestName(false) + "_2.js2");
  }

  public void testHighlightExtends() throws Exception {
    doTestFor(true, null, () -> {
      HighlightUsagesHandlerBase<PsiElement> handler = HighlightUsagesHandler.createCustomHandler(myEditor, myFile);
      assertNotNull(handler);
      List<PsiElement> targets = handler.getTargets();
      assertEquals(1, targets.size());
      assertTrue(targets.get(0).getText().indexOf("class Foo") != -1);
      handler.computeUsages(targets);
      List<TextRange> readUsages = handler.getReadUsages();
      assertEquals(1, readUsages.size());
      String text = myFile.getText();
      assertEquals("foo", fileTextOfRange(readUsages.get(0)));
    }, getTestName(false) + ".js2");
  }

  public void testHighlightImplements() throws Exception {
    doTestFor(true, null, () -> {
      HighlightUsagesHandlerBase<PsiElement> handler = HighlightUsagesHandler.createCustomHandler(myEditor, myFile);
      assertNotNull(handler);
      List<PsiElement> targets = handler.getTargets();
      assertEquals(2, targets.size());
      assertTrue(targets.get(0).getText().indexOf("interface IFoo") != -1);
      assertTrue(targets.get(1).getText().indexOf("interface IBar") != -1);
      handler.computeUsages(targets);
      List<TextRange> readUsages = handler.getReadUsages();
      assertEquals(2, readUsages.size());
      String text = myFile.getText();
      assertEquals("foo", fileTextOfRange(readUsages.get(0)));
      assertEquals("baz", fileTextOfRange(readUsages.get(1)));
    }, getTestName(false) + ".js2");
  }

  public void testParsleyAndRobotlegsMetadata() throws Exception {
    defaultTest();
  }

  public void testSwizMetadata() throws Exception {
    defaultTest();
  }

  public void testSpringActionScriptMetadata() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithUnusedImports})
  public void testReferencingNameInE4X() throws Exception {
    defaultTest();
  }

  public void testMethodCanBeStatic() throws Exception {
    enableInspectionTool(new JSMethodCanBeStaticInspection());
    doSimpleHighlightingWithInvokeFixAndCheckResult("Make 'static'");
  }

  public void testMethodCanBeStatic2() throws Exception {
    enableInspectionTool(new JSMethodCanBeStaticInspection());
    doSimpleHighlightingWithInvokeFixAndCheckResult("Make 'static'");
  }

  public void testMethodCanBeStatic3() throws Exception {
    enableInspectionTool(new JSMethodCanBeStaticInspection());
    doSimpleHighlightingWithInvokeFixAndCheckResult("Make 'static'");
  }

  public void testMethodCanBeStatic4() throws Exception {
    enableInspectionTool(new JSMethodCanBeStaticInspection());
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithGumboSdk})
  public void testMethodCanBeStaticImplicitVars() throws Exception {
    enableInspectionTool(new JSMethodCanBeStaticInspection());
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithUnusedImports})
  public void testCDATAInE4X() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithUnusedImports, JSTestOption.WithFlexSdk})
  public void testReferencingNameInE4X2() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithUnusedImports})
  public void testQNameProblem() throws Exception {
    defaultTest();
  }

  public void testReferencingInternalClass() throws Exception {
    String testName = getTestName(false);
    doTestFor(true, testName + ".js2", testName + "_2.js2");
  }

  public void testSpellChecker() throws Exception {
    enableInspectionTool(new SpellCheckingInspection());
    doSimpleHighlightingWithInvokeFixAndCheckResult("Typo: Rename to...");
  }

  public void testSpellChecker_2() throws Exception {
    runRenameSpellcheckerFixWithChooseVariant("typo");
  }

  private void runRenameSpellcheckerFixWithChooseVariant(String variantName) throws Exception {
    TemplateManagerImpl.setTemplateTesting(getProject(), getTestRootDisposable());
    enableInspectionTool(new SpellCheckingInspection());

    doSimpleHighlightingWithInvokeFixAndCheckResult("Typo: Rename to...");

    final LookupEx lookup = LookupManager.getActiveLookup(myEditor);
    assertNotNull(lookup);
    boolean selected = false;

    for (LookupElement l : lookup.getItems()) {
      if (variantName.equals(l.getLookupString())) {
        lookup.setCurrentItem(l);
        selected = true;
        WriteCommandAction.runWriteCommandAction(null, () -> ((LookupImpl)lookup).finishLookup(Lookup.AUTO_INSERT_SELECT_CHAR));

      }
    }

    assertTrue(selected);

    LookupManager.getInstance(myProject).hideActiveLookup();

    checkResultByFile(BASE_PATH + getTestName(false) + "_after2.js2");
  }

  public void testSpellChecker2() throws Exception {
    runRenameSpellcheckerFixWithChooseVariant("xxix");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testProxy() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithUnusedImports})
  public void testAnyAttrInE4X() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    defaultTest();
  }

  public void testCheckingCast() throws Exception {
    defaultTest();
  }

  public void testE4XPredefines() throws Exception {
    defaultTest();
  }

  public void testGetObjectAndOtherMethodsWithPropertyRefs() throws Exception {
    defaultTest();
  }

  public void testPrivateMethodForInterfaceImplementation() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Make method bar public");
  }

  public void testDefaultProperty() throws Exception {
    defaultTest();
  }

  public void testAS3NsOpenedByDefault() throws Exception {
    String testName = getTestName(false);
    doTestFor(true, testName + ".js2", testName + "_2.js2");
  }

  public void testVirtualKeyword() throws Exception {
    defaultTest();
  }

  public void testDeprecated() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Replace deprecated code with ResourceManager.getInstance().getResourceBundle()");
  }

  public void testDeprecated2() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Replace deprecated code with Bar.yyy");
  }

  public void testObjectMembers() throws Exception {
    defaultTest();
  }

  public void testCircularDependency() throws Exception {
    defaultTest();
  }

  public void testNestedClass() throws Exception {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testTypeEvalFails() throws Exception {
    defaultTest();
  }

  public void testClassRefsInArrayElementType() throws Exception {
    defaultTest();
  }

  public void testCreateClassAccessingProperty() throws Exception {
    defaultTest();
  }

  public void testTypeReferenceInAs() throws Exception {
    defaultTest();
  }

  public void testTypedArray() throws Exception {
    defaultTest();
  }

  public void testNamespaceReferencedWithoutImport() throws Exception {
    defaultTest();
  }

  public void testReportAboutUsingInterfaceInNew() throws Exception {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testBindableClassImplicitlyImplementsIEventDispatcher() throws Exception {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testBindableClassImplicitlyImplementsIEventDispatcher2() throws Exception {
    doTestFor(true, new File(getTestDataPath() + BASE_PATH + getTestName(false)), (Runnable)null, getTestName(false) + "/Main.js2");
    final JSClassResolver resolver = JSDialectSpecificHandlersFactory.forLanguage(JavaScriptSupportLoader.ECMA_SCRIPT_L4).getClassResolver();
    assertNotNull(((JSClass)resolver.findClassByQName("OtherClass", myModule.getModuleScope())).getStub());
    assertNotNull(((JSClass)resolver.findClassByQName("OtherClass2", myModule.getModuleScope())).getStub());
  }

  public void testImplicitGetSet() throws Exception {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testHighlightQualifiedNameWithoutImport() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("mx.messaging.messages.AbstractMessage?");
  }

  public void testUsingFunctionDeclarations() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    defaultTest();
  }

  public void testReportAccessorProblems() throws Exception {
    defaultTest();
  }

  public void testReportAccessorProblems2() throws Exception {
    final Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck(JSBundle.message("javascript.fix.set.element.visibility", "getter foo", "internal"), "as",
                                         infoCollection);
  }

  public void testReportAccessorProblems3() throws Exception {
    final Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck(JSBundle.message("javascript.fix.set.element.visibility", "setter foo", "public"), "as",
                                         infoCollection);
  }

  public void testReportAccessorProblems4() throws Exception {
    doTestFor(true, getTestName(false) + ".as");
  }

  public void testTypeReferenceInNewWithFunExpr() throws Exception {
    defaultTest();
  }

  public void testAssignmentToConst() throws Exception {
    defaultTest();
  }

  public void testIncludedMembers() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".js2", testName + "_2.js2");
  }

  @JSTestOptions(JSTestOption.WithGumboSdk)
  public void testIncompatibleOverride() throws Exception {
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

  public void testSuperConstructorCall() throws Exception {
    defaultTest();
  }

  public void testDefaultConstructorVisibility() throws Exception {
    defaultTest();
  }

  public void testFinalModifiers() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Remove final modifier");
  }

  public void testFinalModifiers2() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testImportBeforePackage() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".js2");
  }

  @JSTestOptions({JSTestOption.WithUnusedImports})
  public void testIncompatibleImplementation() throws Exception {
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

  public void testJSDoc() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    final String testName = getTestName(false);
    doTestFor(true, testName + ".js2", testName + "_2.js2");
  }

  public void testJSDoc2() throws Exception {
    defaultTest();
  }

  public void testDuplicates() throws Exception {
    defaultTest();
  }

  public void testDuplicatesSmall() throws Exception {
    defaultTest();
  }

  public void testFunctionSignatureMismatch() throws Exception {
    defaultTest();
  }

  public void testFunctionSignatureMismatch2() throws Exception {
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

  public void testFunctionSignatureMismatchFixInaccessible1() throws Exception {
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
    JSCodeStyleSettings jsCodeStyleSettings = CodeStyleSettingsManager.getSettings(myProject).getCustomSettings(JSCodeStyleSettings.class);
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

  public void testUnresolvedMembers2() throws Exception {
    doTestFor(true, getTestName(false) + ".js2", getTestName(false) + "_2.js2");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testRegress() throws Exception {
    defaultTest();
  }

  public void testRegress2() throws Exception {
    defaultTest();
    final PsiReference ref = myFile.findReferenceAt(myEditor.getCaretModel().getOffset());
    assertTrue(ref instanceof PsiPolyVariantReference);
    final ResolveResult[] resolveResults = ((PsiPolyVariantReference)ref).multiResolve(true);
    assertTrue(2 == resolveResults.length);
  }

  public void testDynamicAttribute() throws Exception {
    defaultTest();
  }

  public void testSeveralVisibilityModifiers() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Remove visibility modifier");
  }

  public void testQualifiedThings() throws Exception {
    String testName = getTestName(false);
    doTestFor(true, testName + ".js2", testName + "_2.js2");
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testThisTypeIsDynamic() throws Exception {
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

  public void testAssignmentTypeMismatch_4() throws Exception {
    checkActionAvailable("Insert cast", false);
  }

  public void testAssignmentTypeMismatch_5() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Insert cast");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testAssignmentTypeMismatch2() throws Exception {
    defaultTest();
  }

  public void testAssignmentTypeMismatch3() throws Exception {
    defaultTest();
  }

  public void testBinaryArgTypeMismatch() throws Exception {
    defaultTest();
  }

  public void testReportingVoidTypeIssues() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Remove return value");
  }

  public void testCheckAnnotationAttributes() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testForInTypeMismatch() throws Exception {
    defaultTest();
  }

  public void testReturnTypeMismatch() throws Exception {
    defaultTest();
  }

  public void testInvokedVariableTypeMismatch() throws Exception {
    defaultTest();
  }

  public void testMissingReturnArgument() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithUnusedImports})
  public void testCorrectScopeOfImports() throws Exception {
    doTestFor(true, getTestName(false) + ".js2", getTestName(false) + "_2.js2");
  }

  @JSTestOptions({JSTestOption.WithUnusedImports, JSTestOption.WithFlexSdk})
  public void testCorrectScopeOfImports2() throws Exception {
    defaultTest();
  }

  public void testUsingPropertyAsFunctionCall() throws Exception {
    Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".js2");
    findAndInvokeIntentionAction(infoCollection, "Remove argument list", myEditor, myFile);
    checkResultByFile(BASE_PATH + getTestName(false) + "_after.js2");
  }

  public void testImplicitlyDefined() throws Exception {
    defaultTest();
  }

  public void testNSDeclarationUnderPackage() throws Exception {
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
  public void testNoSourceRoot() throws Exception {
    defaultTest();
  }

  public void testCheckReadWrite() throws Exception {
    defaultTest();
  }

  public void testNamespaceElementReferences() throws Exception {
    defaultTest();
  }

  public void testNamespaceElementReferences2() throws Exception {
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

  public void testRefsInIncludes() throws Exception {
    Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".js2");
    findAndInvokeIntentionAction(infos, "Create File RefsInIncludesz.js2", myEditor, myFile);
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testUnusedSymbols() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    defaultTest();
  }

  public void testUnusedSymbols2() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    defaultTest();
  }

  public void testUnusedSymbols3() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithLineMarkers)
  public void testUnusedSymbols4() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    doSimpleHighlightingWithInvokeFixAndCheckResult("Remove unused class 'Foo'");
  }

  public void testUnusedSymbols4_2() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    doSimpleHighlightingWithInvokeFixAndCheckResult("Remove unused namespace 'baz'");
  }

  public void testUnusedGlobalSymbols() throws Exception {
    globalUnusedTestWith2Files();
  }

  public void testUnusedGlobalSymbols2() throws Exception {
    globalUnusedTestWith2Files();
  }

  public void testUnusedGlobalSymbols3() throws Exception {
    globalUnusedTestWith2Files();
  }

  public void testUnusedGlobalSymbols4() throws Exception {
    enableInspectionTool(new JSUnusedGlobalSymbolsInspection());
    defaultTest();
  }

  public void testUnusedGlobalSymbols5() throws Exception {
    enableInspectionTool(new JSUnusedGlobalSymbolsInspection());
    doTestFor(true, getTestName(false) + ".as");
  }

  private void globalUnusedTestWith2Files() throws IOException {
    enableInspectionTool(new JSUnusedGlobalSymbolsInspection());
    String testName = getTestName(false);
    doTestFor(true, testName + ".js2", testName + "_2.js2");
  }

  public void testUnusedParameterHasCreateFieldQuickFix() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    doSimpleHighlightingWithInvokeFixAndCheckResult("Create Field 'xxx'");
  }

  public void testUnusedParameterHasCreateFieldQuickFix_2() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    doSimpleHighlightingWithInvokeFixAndCheckResult("Create Field '_bar'");
  }

  public void testUnusedParameterHasCreateFieldQuickFix_3() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    doSimpleHighlightingWithInvokeFixAndCheckResult("Create Field 'value'");
  }

  public void testUnusedParameterHasCreateFieldQuickFix_4() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    doSimpleHighlightingWithInvokeFixAndCheckResult("Create Field '_value'");
  }

  public void testUnusedParameterHasCreateFieldQuickFix_5() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    doSimpleHighlightingWithInvokeFixAndCheckResult("Create Field '_bar'");
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

  public void testDuplicatedSymbols() throws Exception {
    defaultTest();
  }

  public void testThisInStaticFunction() throws Exception {
    defaultTest();
  }

  public void testDynamicUsageNoTypeCheck() throws Exception {
    String testName = getTestName(false);
    doTestFor(true, testName + ".js2", testName + "_2.js2");
  }

  public void testParametersInStatics() throws Exception {
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

  public void testImplicitlyInternalDeclaration3() throws Exception {
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

  public void testAllowReferenceAnyFieldFromObjectType() throws Exception {
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

  public void testDoNotImportJSDefinition() throws Exception {
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
  public void testNoImportFunction() throws Exception {
    Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".js2");
    IntentionAction action = findIntentionAction(infoCollection, "Bar.foo?", myEditor, myFile);
    assertNull(action);
    infoCollection = doTestFor(true, getTestName(false) + "_2.js2");
    action = findIntentionAction(infoCollection, "Bar.foo?", myEditor, myFile);
    assertNull(action);
  }

  public void testCreateClassOrInterfaceAction() throws Exception {
    doCreateClassOrInterfaceTest("YYY", true, true, true);
  }

  public void testCreateClassOrInterfaceAction2() throws Exception {
    doCreateClassOrInterfaceTest("YYY", false, true, true);
  }

  public void testCreateClassOrInterfaceAction3() throws Exception {
    doCreateClassOrInterfaceTest("Abc", true, false, true);
  }

  public void testCreateClassOrInterfaceAction4() throws Exception {
    doCreateClassOrInterfaceTest("Abc", false, false, true);
  }

  public void testCreateClassOrInterfaceAction5() throws Exception {
    doCreateClassOrInterfaceTest("Abc", false, false, true);
  }

  @SuppressWarnings({"ConstantConditions"})
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
    String prevText = JSTestUtils.modifyTemplate(CreateClassOrInterfaceFix.ACTION_SCRIPT_CLASS_WITH_SUPERS_TEMPLATE_NAME, newText, getProject());
    try {
      doCreateClassOrInterfaceTestWithCheck("Abc", true);
    }
    finally {
      JSTestUtils.modifyTemplate(CreateClassOrInterfaceFix.ACTION_SCRIPT_CLASS_WITH_SUPERS_TEMPLATE_NAME, prevText, getProject());
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
    String prevText = JSTestUtils.modifyTemplate(CreateClassOrInterfaceFix.ACTION_SCRIPT_CLASS_WITH_SUPERS_TEMPLATE_NAME, newText, getProject());
    try {
      doCreateClassOrInterfaceTestWithCheck("Abc", false);
    }
    finally {
      JSTestUtils.modifyTemplate(CreateClassOrInterfaceFix.ACTION_SCRIPT_CLASS_WITH_SUPERS_TEMPLATE_NAME, prevText, getProject());
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

  private void doCreateClassOrInterfaceTest(final String name,
                                            boolean classNotInterface,
                                            boolean complementaryAvailable,
                                            boolean assertNoErrors)
    throws IOException, IncorrectOperationException {
    final Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".js2");
    String classIntentionName = "Create class '" + name + "'";
    String interfaceIntentionName = "Create interface '" + name + "'";
    String actionName = classNotInterface ? classIntentionName : interfaceIntentionName;
    final IntentionAction action = findIntentionAction(infoCollection, actionName, myEditor, myFile);

    assertNotNull(actionName, action);

    String complementaryActionName = classNotInterface ? interfaceIntentionName : classIntentionName;
    IntentionAction complementaryAction = findIntentionAction(infoCollection, complementaryActionName, myEditor, myFile);
    if (complementaryAvailable) {
      assertNotNull(complementaryActionName, complementaryAction);
    }
    else {
      assertNull(complementaryActionName, complementaryAction);
    }

    WriteCommandAction.runWriteCommandAction(null, () -> action.invoke(myProject, myEditor, myFile));

    JSTestUtils.initJSIndexes(getProject());

    if (assertNoErrors) {
      assertEquals(0, JSDaemonAnalyzerTestCase.filterUnwantedInfos(doHighlighting(), this).size());
    }
  }

  @JSTestOptions({JSTestOption.WithLineMarkers})
  public void testHighlightStaticInstanceMembers() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithLineMarkers})
  public void testGenerics() throws Exception {
    defaultTest();
  }

  public void testGenerics2() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testVectorLiteral() throws Exception {
    defaultTest();
  }

  public void testFunctionWithModifierNotUnderClass() throws Exception {
    defaultTest();
  }

  public void testReportMissingReturn() throws Exception {
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
  public void testOverridingMarkers() throws Exception {
    doTestFor(true, () -> {
      final PsiElement at = invokeGotoSuperMethodAction("AAA");
      invokeShowImplementations(JSFunction.class, at);
    }, getTestName(false) + ".js2");
  }

  @JSTestOptions({JSTestOption.WithLineMarkers, JSTestOption.WithJsSupportLoader})
  public void testOverridingMarkersWithLineMarkers() throws Exception {
    //enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    defaultTest();
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

  @JSTestOptions({JSTestOption.WithLineMarkers})
  public void testNoOverrideForInternal() throws Exception {
    DaemonCodeAnalyzerSettings myDaemonCodeAnalyzerSettings = DaemonCodeAnalyzerSettings.getInstance();
    myDaemonCodeAnalyzerSettings.SHOW_METHOD_SEPARATORS = true;
    try {
      defaultTest();
    }
    finally {
      myDaemonCodeAnalyzerSettings.SHOW_METHOD_SEPARATORS = false;
    }
  }

  public void testOverrideInInterface() throws Exception {
    defaultTest();
  }

  public void testOverrideInInterfaceFix() throws Exception {
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

  @JSTestOptions({JSTestOption.WithLineMarkers, JSTestOption.WithJsSupportLoader})
  public void testOverridingMarkers2() throws Exception {
    //enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    doTestFor(true, () -> {
      checkSetProperty(myFile.findElementAt(myEditor.getCaretModel().getOffset()));
      PsiElement at = invokeGotoSuperMethodAction("AAA");
      checkSetProperty(at);
      at = invokeShowImplementations(JSFunction.class, at);
      checkSetProperty(at);
      at = invokeGotoSuperMethodAction("IAAA");
      checkSetProperty(at);
    }, getTestName(false) + ".js2");
  }

  @JSTestOptions({JSTestOption.WithLineMarkers, JSTestOption.WithJsSupportLoader})
  public void testOverridingMarkers3() throws Exception {
    //enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    doTestFor(true, () -> {
      checkGetProperty(myFile.findElementAt(myEditor.getCaretModel().getOffset()));
      PsiElement at = invokeGotoSuperMethodAction("AAA");
      checkGetProperty(at);
      at = invokeShowImplementations(JSFunction.class, at);
      checkGetProperty(at);
      at = invokeGotoSuperMethodAction("IAAA");
      checkGetProperty(at);
    }, getTestName(false) + ".js2");
  }

  @JSTestOptions(JSTestOption.WithJsSupportLoader)
  public void testShowImplementationsForStatic() throws Exception {
    doTestFor(true, () -> JSTestUtils
      .invokeShowImplementations(JSFunction.class, myFile.findElementAt(myEditor.getCaretModel().getOffset()), 0, true), getTestName(false) + ".js2");
  }

  private static void checkSetProperty(final PsiElement at) {
    final JSFunction parentOfType = PsiTreeUtil.getParentOfType(at, JSFunction.class, false);
    assertNotNull(parentOfType);
    assertTrue(parentOfType.isSetProperty());
  }

  private static void checkGetProperty(final PsiElement at) {
    final JSFunction parentOfType = PsiTreeUtil.getParentOfType(at, JSFunction.class, false);
    assertNotNull(parentOfType);
    assertTrue(parentOfType.isGetProperty());
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testLocalClasses() throws Exception {
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
  public void testGotoSuperWorksFromClass() throws Exception {
    doTestFor(true, () -> {
      final PsiElement at = invokeGotoSuperMethodAction("AAA");
      JSTestUtils.invokeShowImplementations(JSClass.class, at, 1, false);
      invokeShowImplemenationsForLineMarker(at, 1);
    }, getTestName(false) + ".js2");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithLineMarkers})
  public void testImplementsAndImplementedMarkers() throws Exception {
    //enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    doTestFor(true, () -> {
      PsiElement at = invokeGotoSuperMethodAction("SecondInterface");
      JSTestUtils.invokeShowImplementations(JSFunction.class, at, 3, false);
      invokeShowImplemenationsForLineMarker(at, 5);
    }, getTestName(false) + ".js2");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testShowImplementationsFromInterface() throws Exception {
    doTestFor(true, () -> {
      final PsiElement at = myFile.findElementAt(myEditor.getCaretModel().getOffset());
      JSTestUtils.invokeShowImplementations(JSClass.class, at, 3, false);
      invokeShowImplemenationsForLineMarker(at, 3);
    }, getTestName(false) + ".js2");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithLineMarkers})
  public void testShowImplementationsFromInterface2() throws Exception {
    doTestFor(true, () -> {
      final PsiElement at = myFile.findElementAt(myEditor.getCaretModel().getOffset());
      JSTestUtils.invokeShowImplementations(JSClass.class, at, 2, false);
      invokeShowImplemenationsForLineMarker(at, 2);
    }, getTestName(false) + ".js2");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testShowImplementationsFromInterfaceCall() throws Exception {
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

  private static PsiElement invokeShowImplementations(final Class<? extends JSNamedElement> destinationClazz, final PsiElement at) {
    return JSTestUtils.invokeShowImplementations(destinationClazz, at, 1, true);
  }

  private static void invokeShowImplemenationsForLineMarker(PsiElement at, int num) {
    JSClass c = PsiTreeUtil.getParentOfType(at, JSClass.class);
    int items = (c.isInterface()
                 ? JavaScriptLineMarkerProvider.ourInterfaceImplementationsNavHandler
                 : JavaScriptLineMarkerProvider.ourClassInheritorsNavHandler).search(c).findAll().size();
    assertEquals(num, items);
  }

  @JSTestOptions(JSTestOption.WithoutWarnings)
  public void testMoreThanOneTopLevelSymbolDeclared() throws Exception {
    final Collection<HighlightInfo> infoCollection = doTestFor(false);
    final IntentionAction action = findIntentionAction(infoCollection, "Remove externally visible symbol", myEditor, myFile);
    assertNotNull(action);
  }

  @JSTestOptions({/*JSTestOption.WithoutSourceRoot,*/ JSTestOption.WithFlexSdk})
  public void testMoreThanOneTopLevelSymbolDeclared2() throws Exception {
    doTestFor(true);
  }

  @JSTestOptions({JSTestOption.WithUnusedImports})
  public void testImportInClass() throws Exception {
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
    findAndInvokeIntentionAction(infoCollection, "Implement Methods", myEditor, myFile);
    checkResultByFile(BASE_PATH + getTestName(false) + "_after.js2");
    JSTestUtils.initJSIndexes(getProject());

    final Collection<HighlightInfo> infoCollection1 = filterUnwantedInfos(doHighlighting(), this);
    assertEquals(infoCollection.size() - 1, infoCollection1.size());
  }

  public void testNonImplementedInterface2() throws Exception {
    doTestFor(false, () -> {
      final int offset = myEditor.getCaretModel().getOffset();
      invokeGotoSuperMethodAction("FirstClass");
      myEditor.getCaretModel().moveToOffset(offset);
      invokeActionWithCheck("FirstInterface", "GotoTypeDeclaration");
    });
  }

  public void testNonImplementedInterface3() throws Exception {
    final String testName = getTestName(false);

    doTestFor(false, testName + ".js2", testName + "_2.js2");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testUnresolvedThisInCallback() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    final String testName = getTestName(false);

    doTestFor(true, testName + ".js2");
  }

  public void testThisTypeInTopLevelFunction() throws Exception {
    defaultTest();
  }

  public void testHighlightThingsFromUnopenedNamespaces() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    final String testName = getTestName(false);

    doTestFor(true, testName + ".js2");
  }

  public void testDoNotResolveStuffFromObject() throws Exception {
    defaultTest();
  }

  public void testHighlightInternalThingsFromOtherPackage() throws Exception {
    defaultTest();
  }

  public void testImportWithStar() throws Exception {
    doTest(BASE_PATH + getTestName(false) + ".js2", true, false, true);
  }

  public void testWithStatement() throws Exception {
    doTest(BASE_PATH + getTestName(false) + ".js2", true, false, true);
  }

  public void testStringMethodsBug() throws Exception {
    defaultTest();
  }

  public void testDynamicResolve() throws Exception {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithFlexFacet)
  public void testConditionalBlocks() throws Exception {
    FlexTestUtils.modifyBuildConfiguration(myModule, bc -> bc.getCompilerOptions().setAllOptions(Collections.singletonMap("compiler.define", "CONFIG::debugging\t")));

    enableInspectionTool(new BadExpressionStatementJSInspection());
    defaultTest();
  }

  public void testConstWithBindAnnotation() throws Exception {
    defaultTest();
  }

  public void testGetMethod() throws Exception {
    defaultTest();
  }

  public void testHighlightInternalThingsFromOtherFiles() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    final String testName = getTestName(false);

    doTestFor(true, testName + ".js2", testName + "_2.js2");
  }

  @Override
  protected String getBasePath() {
    return BASE_PATH.substring(0, BASE_PATH.length() - 1);
  }

  @Override
  @NonNls
  protected String getExtension() {
    return "js2";
  }

  public void testUsingNonPublicNamespace() throws Exception {
    doTestFor(true, getTestName(false) + ".as", getTestName(false) + "_2.as");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithUnusedImports})
  public void testMethodFromNamespace() throws Exception {
    doTestFor(true, getTestName(false) + ".as");
  }

  public void testNumberToString() throws Exception {
    doTestFor(true, getTestName(false) + ".as");
  }

  @JSTestOptions({JSTestOption.WithGumboSdk})
  public void testVectorElementTypeIncompatible() throws Exception {
    doTestFor(true, getTestName(false) + ".as");
  }

  @JSTestOptions({JSTestOption.WithLineMarkers})
  public void testImplementingMarkerFromSwc() throws Exception {
    myAfterCommitRunnable =
      () -> FlexTestUtils.addLibrary(myModule, "Lib", getTestDataPath() + BASE_PATH, "ImplementingMarkerFromSwc.swc", null, null);
    doTestFor(true, getTestName(false) + ".as");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithUnusedImports})
  public void testNoFqnReplaceInsideNamesake() throws Exception { // IDEADEV-37712
    doTestFor(true, getTestName(false) + ".as", getTestName(false) + "_2.as");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithLineMarkers})
  public void testImplicitImplementMarker() throws Exception {
    doTestFor(true, getTestName(false) + ".as");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testImplicitImplementMarker_() throws Exception {
    doTestFor(true, () -> {
      final PsiElement element =
        JSTestUtils.invokeShowImplementations(JSFunction.class, myFile.findElementAt(myEditor.getCaretModel().getOffset()), 1, false);
      assertTrue(element instanceof JSFunction);
      assertTrue(element.getParent() instanceof JSClass);
      assertEquals("Base", ((JSClass)element.getParent()).getQualifiedName());
    }, getTestName(false) + ".as");
  }


  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testQualified() throws Exception {
    doTestFor(true, getTestName(false) + ".as", getTestName(false) + "_2.as");
  }

  public void testResolveToPackage() throws Exception {
    doTestFor(true, getTestName(false) + ".js2", getTestName(false) + "_2.js2");
  }

  @JSTestOptions({JSTestOption.WithUnusedImports})
  public void testEmptyImport() throws Exception {
    doTestFor(true, getTestName(false) + ".as", getTestName(false) + "_2.as");
  }

  public void testCastAmbiguousType() throws Exception {
    final Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".js2", getTestName(false) + "_2.js2");
    findAndInvokeActionWithExpectedCheck("Insert cast", "js2", infoCollection);
  }

  public void testStaticMethodInInterface() throws Exception {
    final Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck(JSBundle.message("javascript.fix.remove.static.modifier"), "as", infoCollection);
  }

  public void testMultipleVarsInPackage() throws Exception {
    final Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck(JSBundle.message("javascript.fix.remove.externally.visible.symbol"), "as", infoCollection);
  }

  public void testFieldCannotOverride() throws Exception {
    final Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck(JSBundle.message("javascript.fix.remove.override.modifier"), "as", infoCollection);
  }

  public void testNoStaticFunctionWithoutClass() throws Exception {
    defaultTest();
  }

  public void testInaccessibleRefsReported() throws Exception {
    Collection<HighlightInfo> highlightInfos = defaultTest();
    assertInaccessible(highlightInfos, "foo.InaccessibleRefsReported?");
    assertInaccessible(highlightInfos, "Create class 'InaccessibleRefsReported'");
  }

  public void testNamespaceDeclarationIsImplicitlyStatic() throws Exception {
    defaultTest();
  }

  public void testStaticMethodCannotBeFinal() throws Exception {
    defaultTest();
  }

  public void testReportWarningWhenUsingFieldOrMethodOnSomeDynamicClassInstance() throws Exception {
    defaultTest();
  }

  public void testReportMultipleDynamicModifiers() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Remove dynamic modifier");
  }

  public void testAs2() throws Exception {
    defaultTest();
  }

  public void testOverrideVisibility() throws Exception {
    final Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".as", getTestName(false) + "_2.as");
    findAndInvokeActionWithExpectedCheck(JSBundle.message("javascript.fix.set.element.visibility", "method foo", "protected"), "as",
                                         infoCollection);
  }

  public void testOverrideVisibility2() throws Exception {
    final Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".as", getTestName(false) + "_2.as");
    findAndInvokeActionWithExpectedCheck(JSBundle.message("javascript.fix.set.element.visibility", "method foo", "public"), "as",
                                         infoCollection);
  }

  @JSTestOptions(JSTestOption.WithUnusedImports)
  public void testNoPackageNameReplacement() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, new File(getTestDataPath() + getBasePath() + File.separatorChar + testName), (Runnable)null,
              testName + "/com/view/Test.as",
              testName + "/com/view.as");
  }

  @JSTestOptions(JSTestOption.WithUnusedImports)
  public void testNoPackageNameReplacement2() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, new File(getTestDataPath() + getBasePath() + File.separatorChar + testName), (Runnable)null,
              testName + "/com/zzz/Foo.as", testName + "/com/view/Test.as", testName + "/com/view.as");
  }

  @JSTestOptions(JSTestOption.WithoutSourceRoot)
  public void testNoCreateClassFixWithNoSourceRoots() throws Exception {
    Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".as");
    IntentionAction intentionAction = findIntentionAction(infoCollection, "Create class Base", myEditor, myFile);
    assertNull(intentionAction);
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testConditionalCompileBlock() throws Exception {
    Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck("flash.events.KeyboardEvent?", "as", infoCollection);
  }

  public void testStaticBlock() throws Exception {
    doTestFor(true, getTestName(false) + ".as");
  }

  @JSTestOptions({JSTestOption.WithLineMarkers})
  public void testNullQualifiedName() throws Exception {
    doTestFor(true, getTestName(false) + ".as");
  }

  // IDEA-56342
  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testMultinamesInDecompiledSwc() throws Exception {
    myAfterCommitRunnable =
      () -> FlexTestUtils.addLibrary(myModule, "playerglobal", FlexImporterTest.getTestDataPath(), "PlayerGlobal10.swc", null, null);
    doTestFor(true, getTestName(false) + ".as");
  }

  public void testSetterOptionalParam() throws Exception {
    Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck("Remove parameter default value", "as", infoCollection);
  }

  public void testDelegateMethodsDisabled() throws Exception {
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
    findAndInvokeActionWithExpectedCheck(JSBundle.message("javascript.fix.remove.parameter"), "as", infoCollection);
  }

  public void testRemoveGetterParameters() throws Exception {
    final Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck(JSBundle.message("javascript.fix.remove.parameters"), "as", infoCollection);
  }

  public void testImportForNeighbourClass() throws Exception {
    doTestFor(true, getTestName(false) + ".as", getTestName(false) + "_2.as");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testInvalidAttribute() throws Exception {
    doTestFor(true, getTestName(false) + ".as");
  }

  @JSTestOptions(JSTestOption.WithLineMarkers)
  public void testStaticMethodDoesNotImplement() throws Exception {
    final Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".as");
    invokeGotoSuperMethodAction("Impl");
    findAndInvokeActionWithExpectedCheck(JSBundle.message("javascript.fix.implement.methods"), "as", infos);
  }

  @JSTestOptions({JSTestOption.WithLineMarkers, JSTestOption.WithoutSourceRoot})
  public void testLineMarkersInLibrarySource() throws Exception {
    myAfterCommitRunnable = new Runnable() {
      @Override
      public void run() {
        VirtualFile file = ModuleRootManager.getInstance(myModule).getContentEntries()[0].getFile();
        VirtualFile fakeClassFile = getVirtualFile(BASE_PATH + "/" + getTestName(false) + "_2.js2");
        try {
          VirtualFile classesDir = file.createChildDirectory(this, "classes");
          VfsUtilCore.copyFile(this, fakeClassFile, classesDir);
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }
        FlexTestUtils.addFlexLibrary(true, myModule, "lib", true, file.getPath(), "classes", "", null);

        ModifiableRootModel model = ModuleRootManager.getInstance(myModule).getModifiableModel();
        model.removeContentEntry(model.getContentEntries()[0]);
        model.commit();
      }
    };

    doTestFor(true, getTestName(false) + ".js2");
  }

  public void testNoTypeGuessFromAsdoc() throws Exception {
    final Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck("Create Field 'aa'", "as", infos);
  }

  public void testNoTypeGuessFromAsdoc2() throws Exception {
    final Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck("Create Field 'aa'", "as", infos);
  }

  public void testNoImportSuggestForTestClass() throws Exception {
    myAfterCommitRunnable = () -> {
      final VirtualFile testsRoot = getVirtualFile(BASE_PATH + getTestName(false) + "_2");
      PsiTestUtil.addSourceContentToRoots(myModule, testsRoot, true);
    };
    final Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".as");
    assertInaccessible(infos, "a.TestClass?");
  }

  public void testImportSuggestForProductionClass() throws Exception {
    myAfterCommitRunnable = () -> {
      final ContentEntry[] contentEntries = ModuleRootManager.getInstance(myModule).getContentEntries();
      final VirtualFile file = contentEntries[0].getFile();
      PsiTestUtil.removeContentEntry(myModule, contentEntries[0].getFile());
      PsiTestUtil.addSourceContentToRoots(myModule, file, true);
      final VirtualFile productionRoot = getVirtualFile(BASE_PATH + getTestName(false) + "_2");
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
  public void testNoChangeSignatureForLibraryMethod() throws Exception {
    final Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".js2");
    assertInaccessible(infos, "Change decodeURI() signature");
  }

  public void testStripQuotes1() throws Exception {
    checkActionAvailable("Insert cast", true);
  }

  public void testStripQuotes2() throws Exception {
    checkActionAvailable("Insert cast", true);
  }

  public void testStripQuotes3() throws Exception {
    checkActionAvailable("Insert cast", true);
  }

  public void testStripQuotes4() throws Exception {
    checkActionAvailable("Insert cast", true);
  }

  public void testStripQuotes5() throws Exception {
    checkActionAvailable("Insert cast", false);
  }

  public void testStripQuotes6() throws Exception {
    checkActionAvailable("Insert cast", false);
  }

  public void testStripQuotes7() throws Exception {
    checkActionAvailable("Insert cast", false);
  }

  private void checkActionAvailable(String name, boolean available) throws IOException {
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
  public void testNoCreateFieldInSdkClass() throws Exception {
    Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".as");
    assertInaccessible(infos, "Create field 'foo'");
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testNoCreateMethodInLibraryClass() throws Exception {
    myAfterCommitRunnable =
      () -> FlexTestUtils.addLibrary(myModule, "Lib", getTestDataPath() + BASE_PATH, "ImplementingMarkerFromSwc.swc", null, null);
    Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".as");
    assertInaccessible(infos, "Create Method 'bar'");
  }

  public void testInternalClassFromFileLocal() throws Exception {
    Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".as");
    assertInaccessible(infos, "Create Method 'z'");
    findAndInvokeActionWithExpectedCheck("Make method z public", "as", infos);
  }

  public void testInternalClassFromFileLocal2() throws Exception {
    Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck("Make class InternalClassFromFileLocal2 public", "as", infos);
  }

  public void testRelaxVisibilityFix() throws Exception {
    Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".as");
    assertInaccessible(infos, "Create Field 'v'");
    assertInaccessible(infos, "Create Constant Field 'v'");
    findAndInvokeActionWithExpectedCheck("Make field v internal", "as", infos);
  }

  public void testRelaxVisibilityFix2() throws Exception {
    Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".as");
    assertInaccessible(infos, "Create Method 'foo'");
    findAndInvokeActionWithExpectedCheck("Make method foo public", "as", infos);
  }

  public void testRelaxVisibilityFix3() throws Exception {
    Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck("Make field foo internal", "as", infos);
  }

  public void testRelaxVisibilityFix4() throws Exception {
    Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck("Make field foo protected", "as", infos);
  }

  public void testRelaxVisibilityFix5() throws Exception {
    Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck("Make field foo protected", "as", infos);
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

  public void testNoChangeSignatureFixForVoidType() throws Exception {
    Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".as");
    assertInaccessible(infos, "Change NoChangeSignatureFixForVoidType.bar() signature");
  }

  public void testNoChangeTypeFixForVoidType() throws Exception {
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
      sdk1.set(FlexTestUtils.createSdk(getTestDataPath() + BASE_PATH + "fake_sdk", "4.0.0"));
      {
        SdkModificator m = sdk1.get().getSdkModificator();
        m.removeAllRoots();
        m.addRoot(sdk1.get().getHomeDirectory().findChild("common_root"), OrderRootType.CLASSES);
        m.addRoot(sdk1.get().getHomeDirectory().findChild("flex_root"), OrderRootType.CLASSES);
        m.commitChanges();
      }
      sdk2.set(FlexTestUtils.createSdk(getTestDataPath() + BASE_PATH + "fake_sdk", "4.0.0"));
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

    checkHighlighting(new ExpectedHighlightingData(myEditor.getDocument(), true, true, false, myFile));

    myFile = PsiManager.getInstance(myProject).findFile(fileFromModule2.get());
    myEditor = createEditor(fileFromModule2.get());
    checkHighlighting(new ExpectedHighlightingData(myEditor.getDocument(), true, true, false, myFile));
  }

  private VirtualFile copyFileToModule(Module module, String filePath) {
    try {
      VirtualFile dir = myProject.getBaseDir().createChildDirectory(this, "module2");
      PsiTestUtil.addSourceRoot(module, dir);
      VirtualFile f = LocalFileSystem.getInstance().findFileByPath(filePath);
      return VfsUtilCore.copyFile(this, f, dir);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void testUnusedVariableValues() throws Exception {
    enableInspectionTool(new JSUnusedAssignmentInspection());
    enableInspectionTool(new JSUnusedGlobalSymbolsInspection());

    defaultTest();
  }

  public void testUnusedVariableValues2() throws Exception {
    enableInspectionTool(new JSUnusedAssignmentInspection());

    defaultTest();
  }

  public void testUnresolvedReferencePattern() throws Exception {
    defaultTest();
  }

  public void testImplicitImplementationByPublicBindableProperty() throws Exception {
    defaultTest();
  }

  public void testInitializeParameterFix() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult("Initialize parameter", "as");
  }

  public void testSuperclassResolveMixedRoots() throws Exception {
    // we need two SDKs so that flash.net.FileReference from SDK 4.6/playerglobal.swc
    // has more recent timestamp than its brother from SDK 4.5/airglobal.swc
    final Sdk sdk45 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.5"), null, true);
    final Sdk sdk46 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.6"), null, false);
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

  public void testFieldResolveMixedRoots() throws Exception {
    // same as testSuperclassResolveMixedRoots()
    final Sdk sdk45 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.5"), null, true);
    final Sdk sdk46 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.6"), null, false);
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

  public void testHangingCommaInRefList() throws Exception {
    defaultTest();
  }

  public void testAddOverrideModifierWithMetadata() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult("Add override modifier", "as");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testFileLocalClassInheritance() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testCorrectScopeForSuperclassCheck() throws Exception {
    doTestFor(true, getTestName(false) + ".js2", getTestName(false) + "_2.js2");
  }

  public void testSameClassAndPackage() throws Exception {
    doTestFor(true, getTestName(false) + ".js2", getTestName(false) + "_2.js2");
  }

  public void testNonExistingMethodAfterNew() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult("Create Method 'zzz'", "js2");
  }

  public void testCorrectScopeForSuperclassCheck2() throws Exception { //
    final Sdk sdk46 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.6"), null, false);
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

  public void testCorrectScopeForSuperclassCheck3() throws Exception { // IDEA-91539
    final Sdk sdk46 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.6"), null, false);
    FlexTestUtils.modifyConfigs(myProject, e -> {
      ModifiableFlexBuildConfiguration bc1 = e.getConfigurations(myModule)[0];
      bc1.setTargetPlatform(TargetPlatform.Desktop);
      FlexTestUtils.setSdk(e.getConfigurations(myModule)[0], sdk46);
    });
    doTestFor(true, getTestName(false) + ".js2", getTestName(false) + "_2.mxml");
  }

  public void testCorrectScopeForSuperclassCheck4() throws Exception {
    // two dependent modules, different SDKs
    final Module module2 = doCreateRealModuleIn("module2", myProject, FlexModuleType.getInstance());
    final Sdk sdk45 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.5"), null, false);
    final Sdk sdk46 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.6"), null, false);
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

  public void testCorrectScopeForSuperclassCheck5() throws Exception {
    // same fqn in SDK and library:
    // Module depends on an swc that has SDK classes compiled in.
    // Library classes (swc compiled with SDK 4.6) have newer timestamp than classes of SDK that module depends on (SDK 4.5)
    // SDK should have no sources attached (otherwise getNavigationElement() for both classes will bring us to the same file)
    // see testData\js2_highlighting\CorrectScopeForSuperclassCheck5_src.zip

    final Sdk sdk45 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.5"), null, false);
    FlexTestUtils.addFlexLibrary(false, myModule, "foobar", true, getTestDataPath(), BASE_PATH + getTestName(false) + ".swc", null, null,
                                 LinkageType.Merged);

    FlexTestUtils.modifyConfigs(myProject, e -> {
      ModifiableFlexBuildConfiguration bc1 = e.getConfigurations(myModule)[0];
      FlexTestUtils.setSdk(bc1, sdk45);
    });
    doTestFor(true, getTestName(false) + ".js2");
  }

  public void testCorrectScopeForSuperclassCheck6() throws Exception {
    // monkey patching SDK class
    final Sdk sdk45 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.5"), null, false);

    FlexTestUtils.modifyConfigs(myProject, e -> {
      ModifiableFlexBuildConfiguration bc1 = e.getConfigurations(myModule)[0];
      FlexTestUtils.setSdk(bc1, sdk45);
    });
    doTestFor(true, getTestName(false) + ".js2", getTestName(false) + "_2.js2");
  }

  public void testCorrectScopeForSuperclassCheck7() throws Exception {
    // same FQNs in different modules
    final Module module2 = doCreateRealModuleIn("module2", myProject, FlexModuleType.getInstance());

    myAfterCommitRunnable = () -> copyFileToModule(module2, getTestDataPath() + BASE_PATH + getTestName(false) + "_2.js2");

    configureByFiles(null, BASE_PATH + getTestName(false) + ".js2");
    final JSClassResolver resolver = JSDialectSpecificHandlersFactory.forLanguage(JavaScriptSupportLoader.ECMA_SCRIPT_L4).getClassResolver();
    final PsiElement class1 = resolver.findClassByQName("com.foo.Foo", myModule.getModuleScope());
    assertNotNull(class1);
    final PsiElement class2 = resolver.findClassByQName("com.foo.Foo", module2.getModuleScope());
    assertNotNull(class2);
    assertFalse(class1.isEquivalentTo(class2));
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testCreateVarOfArrayType() throws Exception {
    final Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".as");
    findAndInvokeActionWithExpectedCheck("Create Variable 'x'", "as", infoCollection);
  }

  public void testVectorWithSdk() throws Exception {
    final Sdk sdk45 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.5"), null, true);
    FlexTestUtils.modifyConfigs(myProject, editor -> {
      ModifiableFlexBuildConfiguration bc1 = editor.getConfigurations(myModule)[0];
      FlexTestUtils.setSdk(bc1, sdk45);
    });
    doTestFor(true, getTestName(false) + ".as");
  }

  public void testOptionalParams() throws Exception {
    doTestFor(true, getTestName(false) + ".as");
  }

  public void testAccessInternalMemberFromProperty() throws Exception {
    // IDEA-139240
    doTestFor(true, getTestName(false) + ".as", getTestName(false) + "_2.as");
  }

  public void testDoNotResolveClassMemberWhereXmlTagExpected() throws Exception {
    // IDEA-138900
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testProtectedMembersWithImplicitInheritance() throws Exception {
    defaultTest(); // IDEA-146722
  }

  public void testArgumentsInParenthesis() throws Exception {
    defaultTest(); // IDEA-153275
  }

  @JSTestOptions({JSTestOption.WithLineMarkers, JSTestOption.WithSemanticKeywords})
  public void testSemanticHighlighting() throws Exception {
    defaultTest(); // IDEA-110040
  }
}
