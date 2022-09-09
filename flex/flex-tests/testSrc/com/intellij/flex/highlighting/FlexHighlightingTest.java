// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.highlighting;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoFilter;
import com.intellij.codeInsight.daemon.impl.analysis.XmlPathReferenceInspection;
import com.intellij.codeInsight.daemon.impl.analysis.XmlUnusedNamespaceInspection;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.flex.util.ActionScriptDaemonAnalyzerTestCase;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.flex.util.FlexUnitLibs;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.javascript.flex.css.FlexCSSDialect;
import com.intellij.javascript.flex.mxml.schema.FlexMxmlNSDescriptor;
import com.intellij.lang.actionscript.psi.ActionScriptPsiImplUtil;
import com.intellij.lang.css.CssDialect;
import com.intellij.lang.css.CssDialectMappings;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.*;
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory;
import com.intellij.lang.javascript.flex.AnnotationBackedDescriptor;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitTestCreator;
import com.intellij.lang.javascript.flex.projectStructure.FlexProjectLevelCompilerOptionsHolder;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.ModuleOrProjectCompilerOptions;
import com.intellij.lang.javascript.index.JavaScriptIndex;
import com.intellij.lang.javascript.inspections.JSMethodCanBeStaticInspection;
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection;
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection;
import com.intellij.lang.javascript.inspections.UnterminatedStatementJSInspection;
import com.intellij.lang.javascript.inspections.actionscript.JSFieldCanBeLocalInspection;
import com.intellij.lang.javascript.inspections.actionscript.JSUntypedDeclarationInspection;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSUseNamespaceDirective;
import com.intellij.lang.javascript.psi.resolve.ActionScriptResolveUtil;
import com.intellij.lang.javascript.psi.resolve.JSClassResolver;
import com.intellij.lang.properties.PropertiesBundle;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.XmlHighlighterColors;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.DumbServiceImpl;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.css.CssBundle;
import com.intellij.psi.css.inspections.CssNegativeValueInspection;
import com.intellij.psi.css.inspections.CssUnknownPropertyInspection;
import com.intellij.psi.css.inspections.CssUnusedSymbolInspection;
import com.intellij.psi.css.inspections.bugs.CssNoGenericFontNameInspection;
import com.intellij.psi.css.inspections.invalid.CssInvalidFunctionInspection;
import com.intellij.psi.css.inspections.invalid.CssInvalidHtmlTagReferenceInspection;
import com.intellij.psi.css.inspections.invalid.CssInvalidPropertyValueInspection;
import com.intellij.psi.css.inspections.invalid.CssUnknownTargetInspection;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.refactoring.rename.RenameProcessor;
import com.intellij.spellchecker.inspections.SpellCheckingInspection;
import com.intellij.spellchecker.quickfixes.RenameTo;
import com.intellij.testFramework.ExpectedHighlightingData;
import com.intellij.testFramework.ExtensionTestUtil;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestUtil;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlNSDescriptor;
import com.intellij.xml.impl.dtd.XmlNSDescriptorImpl;
import com.intellij.xml.util.CheckXmlFileWithXercesValidatorInspection;
import com.sixrr.inspectjs.validity.BadExpressionStatementJSInspection;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.intellij.codeInsight.daemon.impl.HighlightInfoFilter.EXTENSION_POINT_NAME;

public class FlexHighlightingTest extends ActionScriptDaemonAnalyzerTestCase {
  @NonNls static final String BASE_PATH = "flex_highlighting";

  private Runnable myAfterCommitRunnable = null;

  {
    myTestsWithJSSupportLoader.addAll(
      Arrays.asList("Flex", "Flex2", "FlexWithLocalCss", "DuplicatedIdsInMxml", "PathesInMxml", "ReferencingClass", "EnumeratedValues"));

    myTestsWithCssLoader.addAll(Arrays.asList("Flex", "FlexWithLocalCss"));
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface NeedsJavaModule {
  }

  @NotNull
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  private boolean needsJavaModule() {
    final Method method = JSTestUtils.getTestMethod(getClass(), getTestName(false));
    assertNotNull(method);
    return method.getAnnotation(NeedsJavaModule.class) != null;
  }

  @Override
  protected void setUp() throws Exception {
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "");
    super.setUp();
    myAfterCommitRunnable = null;
    enableInspectionTool(new XmlPathReferenceInspection());
    suppressXmlNSAnnotator();
  }

  private void suppressXmlNSAnnotator() {
    HighlightInfoFilter filter = (info, file) -> info.forcedTextAttributesKey != XmlHighlighterColors.XML_NS_PREFIX;
    ExtensionTestUtil.maskExtensions(EXTENSION_POINT_NAME, Collections.singletonList(filter), getTestRootDisposable());
  }

  @Override
  protected void tearDown() throws Exception {
    myAfterCommitRunnable = null;
    super.tearDown();
  }

  @NotNull
  @Override
  protected ModuleType getModuleType() {
    return needsJavaModule() ? StdModuleTypes.JAVA : FlexModuleType.getInstance();
  }

  @Override
  protected void setUpJdk() {
    if (!needsJavaModule()) {
      FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), getTestRootDisposable());
    }
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testHtmlCss() {
    enableInspectionTool(new CssInvalidPropertyValueInspection());
    enableInspectionTool(new CssInvalidHtmlTagReferenceInspection());
    enableInspectionTool(new CssUnknownPropertyInspection());
    doTestFor(true, getTestName(false) + ".html");
  }

  @NeedsJavaModule
  public void testFlex() {
    enableInspectionTool(new JSMethodCanBeStaticInspection());
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + ".css");
    final List<PsiReference> cssRefs = collectCssRefs();

    assertEquals(1, cssRefs.size());
    final PsiElement element = cssRefs.get(0).resolve();
    assertNotNull(element);
    assertEquals(element.getContainingFile().getName(), testName + ".css");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testFlexSpecificFunctionsInCss() {
    enableInspectionTool(new CssInvalidPropertyValueInspection());
    enableInspectionTool(new CssInvalidFunctionInspection());
    doTestFor(true, getTestName(false) + ".css");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testFlexWithCss() {
    //enableInspectionTool(new FlexCssStrayBraceInspection());
    registerCommonCssInspections();
    enableInspectionTool(new CssUnknownTargetInspection());

    defaultTest();
    doTestFor(true, getTestName(false) + ".css");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testFlexWithCss2() {
    enableInspectionTool(new CssUnusedSymbolInspection());
    defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testFlexWithCss3() throws Exception {
    enableInspectionTool(new CssNegativeValueInspection());
    enableInspectionTool(new CssNoGenericFontNameInspection());
    enableInspectionTool((LocalInspectionTool)Class.forName("org.jetbrains.w3validators.css.W3CssValidatorInspection").newInstance());
    doTestFor(true, getTestName(false) + ".css");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testFlexWithCss4() {
    enableInspectionTool(new CssInvalidPropertyValueInspection());
    myAfterCommitRunnable =
      () -> FlexTestUtils
        .addLibrary(myModule, "library", getTestDataPath() + getBasePath() + "/", "LibForCss.swc", "LibForCss_src.zip", null);
    doTestFor(true, getTestName(false) + ".css");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testFlexWithCss5() {
    enableInspectionTool(new CssUnusedSymbolInspection());
    defaultTest();
    doTestFor(true, getTestName(false) + ".css");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testFlexWithCss6() {
    registerCommonCssInspections();
    myAfterCommitRunnable =
      () -> FlexTestUtils
        .addLibrary(myModule, "library", getTestDataPath() + getBasePath() + "/", "LibForCss.swc", "LibForCss_src.zip", null);
    defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testFlexWithCss7() {
    enableInspectionTool(new CssInvalidPropertyValueInspection());
    enableInspectionTool(new CssInvalidHtmlTagReferenceInspection());
    doTestFor(true, getTestName(false) + ".css");
  }

  public void testFlexCssShouldNotComplainAboutUnitlessLength() {
    enableInspectionTool(new CssInvalidPropertyValueInspection());
    doTestFor(true, getTestName(false) + ".css");
  }

  public void testCssDialectMappings() {
    enableInspectionTool(new CssInvalidPropertyValueInspection());
    enableInspectionTool(new CssUnknownPropertyInspection());
    try {
      CssDialectMappings.getInstance(getProject()).setMapping(null, CssDialect.CLASSIC.getName());
      doTestFor(true, getTestName(false) + ".css");
    }
    finally {
      CssDialectMappings.getInstance(getProject()).cleanupForNextTest();
    }
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testSwitchToFlexCssQuickFix1() throws Exception {
    enableInspectionTool(new CssInvalidHtmlTagReferenceInspection());
    try {
      CssDialectMappings.getInstance(getProject()).setMapping(null, CssDialect.CLASSIC.getName());
      doHighlightingWithInvokeFixAndCheckResult(
        CssBundle.message("switch.to.css.dialect.quickfix.name", FlexCSSDialect.getInstance().getDisplayName()), "css");
    }
    finally {
      CssDialectMappings.getInstance(getProject()).cleanupForNextTest();
    }
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testSwitchToFlexCssQuickFix2() throws Exception {
    try {
      CssDialectMappings.getInstance(getProject()).setMapping(null, CssDialect.CLASSIC.getName());
      enableInspectionTool(new CssUnknownPropertyInspection());
      doHighlightingWithInvokeFixAndCheckResult(
        CssBundle.message("switch.to.css.dialect.quickfix.name", FlexCSSDialect.getInstance().getDisplayName()), "css");
    }
    finally {
      CssDialectMappings.getInstance(getProject()).cleanupForNextTest();
    }
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testSwitchToFlexCssQuickFix3() throws Exception {
    enableInspectionTool(new CssInvalidHtmlTagReferenceInspection());
    try {
      CssDialectMappings.getInstance(getProject()).setMapping(null, CssDialect.CLASSIC.getName());

      configureByFile(BASE_PATH + '/' + getTestName(false) + '.' + "html");
      doHighlighting();
      final List<IntentionAction> actions = CodeInsightTestFixtureImpl.getAvailableIntentions(myEditor, myFile);
      final String text = CssBundle.message("switch.to.css.dialect.quickfix.name", FlexCSSDialect.getInstance().getDisplayName());
      final IntentionAction action = CodeInsightTestUtil.findIntentionByText(actions, text);
      assertNull(action);
    }
    finally {
      CssDialectMappings.getInstance(getProject()).cleanupForNextTest();
    }
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testSwitchToClassicCssQuickFix1() throws Exception {
    enableInspectionTool(new CssInvalidHtmlTagReferenceInspection());
    doHighlightingWithInvokeFixAndCheckResult(
      CssBundle.message("switch.to.css.dialect.quickfix.name", CssDialect.CLASSIC.getDisplayName()), "css");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testSwitchToClassicCssQuickFix2() throws Exception {
    enableInspectionTool(new CssUnknownPropertyInspection());
    doHighlightingWithInvokeFixAndCheckResult(
      CssBundle.message("switch.to.css.dialect.quickfix.name", CssDialect.CLASSIC.getDisplayName()), "css");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testSwitchToClassicCssQuickFix3() throws Exception {
    enableInspectionTool(new CssInvalidHtmlTagReferenceInspection());
    configureByFile(BASE_PATH + '/' + getTestName(false) + '.' + "mxml");
    doHighlighting();
    final List<IntentionAction> actions = CodeInsightTestFixtureImpl.getAvailableIntentions(myEditor, myFile);
    final String text = CssBundle.message("switch.to.css.dialect.quickfix.name", CssDialect.CLASSIC.getDisplayName());
    final IntentionAction action = CodeInsightTestUtil.findIntentionByText(actions, text);
    assertNull(action);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testFlexWithCssIdSelectorAfterClassSelector() {
    enableInspectionTool(new CssUnusedSymbolInspection());
    defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testFlexCssFileInsideLibrary() throws Exception {
    enableInspectionTool(new CssInvalidPropertyValueInspection());
    final String libFileName = "CssInside.zip";
    final String basePath = getTestDataPath() + getBasePath() + "/";
    myAfterCommitRunnable = () -> FlexTestUtils.addLibrary(myModule, "lib", basePath, libFileName, null, null);

    // warm up
    configureByFile(getBasePath() + "/FlexWithCss7.css");

    VirtualFile libRoot = null;

    for (OrderEntry entry : ModuleRootManager.getInstance(myModule).getOrderEntries()) {
      if (entry instanceof LibraryOrderEntry) {
        final LibraryOrderEntry libraryOrderEntry = (LibraryOrderEntry)entry;
        if ("lib".equals(libraryOrderEntry.getLibraryName())) {
          VirtualFile[] files = libraryOrderEntry.getRootFiles(OrderRootType.CLASSES);
          libRoot = files[0];
        }
      }
    }

    VirtualFile file = libRoot.findFileByRelativePath("dir/default.css");

    configureByExistingFile(file);
    doHighlighting();
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testHtmlCssFile1() {
    registerCommonCssInspections();

    doTestFor(true, getTestName(false) + ".css");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testHtmlCssFile2() {
    registerCommonCssInspections();

    doTestFor(true, getTestName(false) + ".css", "HtmlFileReferringToCss.html", "MxmlFileReferringToCss.mxml");
  }

  private List<PsiReference> collectCssRefs() {
    final List<PsiReference> cssRefs = new ArrayList<>();

    myFile.acceptChildren(new XmlRecursiveElementVisitor() {
      @Override
      public void visitXmlAttribute(final @NotNull XmlAttribute attr) {
        if ("styleName".equals(attr.getName())) {
          final XmlAttributeValue value = attr.getValueElement();
          if (value != null) ContainerUtil.addAll(cssRefs, value.getReferences());
        }
      }
    });
    return cssRefs;
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCallbackSignatureMismatch() {
    doTestFor(true, getTestName(false) + ".mxml", getTestName(false) + "_2.as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCallbackSignatureMismatch2() {
    doTestFor(true, getTestName(false) + ".as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCircularDependency() {
    defaultTest();
  }

  @NeedsJavaModule
  public void testFlexWithLocalCss() {
    defaultTest();
    final List<PsiReference> cssRefs = collectCssRefs();

    assertEquals(1, cssRefs.size());
    final PsiElement element = cssRefs.get(0).resolve();
    assertNotNull(element);
    assertEquals(element.getContainingFile().getName(), getTestName(false) + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testFlexWithMockFlex() throws Exception {
    final String testName = getTestName(false);
    doHighlightingWithInvokeFixAndCheckResult("Add override modifier", "mxml", testName + ".mxml", testName + "_2.as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testItemRendererAsAttribute() {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + "_other.mxml");
  }

  @FlexTestOptions(FlexTestOption.WithGumboSdk)
  public void testFlexWithMockFlex2009() {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", "FlexWithMockFlex_2.as");
  }

  @FlexTestOptions(FlexTestOption.WithGumboSdk)
  public void testSkinFile2009() {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testSkinFile2009_2() {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithGumboSdk)
  public void testEnhancedStatesSyntax2009() {
    final String name = getTestName(false);
    final String manifest = getTestDataPath() + "/" + getBasePath() + "/" + name + "_manifest.xml";
    final Map<String, String> options = Collections.singletonMap("compiler.namespaces.namespace", "http://MyNamespace\t" + manifest);
    FlexProjectLevelCompilerOptionsHolder.getInstance(myProject).getProjectLevelCompilerOptions().setAllOptions(options);

    doTestFor(true, name + ".mxml", name + "_other.as");
  }

  @FlexTestOptions(FlexTestOption.WithGumboSdk)
  public void testFlexWithMockFlex2009_2() {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + "_2.mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testFlexWithModelTag() {
    defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testCheckUniquenessInPackage() {
    String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + ".as");
    doTestFor(true, testName + ".as", testName + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testFlexWithMockFlex2() {
    final String testName = getTestName(false);
    doTestFor(true, null, highlightInfos -> {
      myFile.accept(new XmlRecursiveElementVisitor() {
        @Override
        public void visitXmlAttribute(final @NotNull XmlAttribute attribute) {
          final XmlAttributeDescriptor descriptor = attribute.getDescriptor();
          if (descriptor instanceof AnnotationBackedDescriptor) {
            assertNotNull(descriptor.getDeclaration());
          }
        }
      });

      try {
        final String actionName = JavaScriptBundle.message("javascript.create.method.intention.name", "bar");
        findAndInvokeIntentionAction(highlightInfos, actionName, myEditor, myFile);
        assertNull(findIntentionAction(doHighlighting(), actionName, myEditor, myFile));
      }
      catch (IncorrectOperationException ex) {
        throw new RuntimeException(ex);
      }
      return null;
    }, testName + ".mxml", testName + "_2.mxml", testName + "_2.as", testName + "_3.mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testFlexWithMockFlex3() {
    defaultTest();
  }

  @FlexTestOptions({FlexTestOption.WithFlexSdk, FlexTestOption.WithUnusedImports})
  public void testFlexWithMockFlex3_2() throws Exception {
    final String testName = getTestName(false);
    final Collection<HighlightInfo> infoCollection = doTestFor(true, testName + ".mxml", testName + "_2.as");
    findAndInvokeIntentionAction(infoCollection, "foo.bar.IFoo?", myEditor, myFile);
    checkResultByFile(BASE_PATH + "/" + testName + "_after.mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testInclude() {
    final String testName = getTestName(false);

    doTestFor(true, null, () -> {
      PsiElement element =
        InjectedLanguageManager.getInstance(myProject).findInjectedElementAt(myFile, myEditor.getCaretModel().getOffset());
      PsiReference reference = PsiTreeUtil.getParentOfType(element, JSReferenceExpression.class);
      assertNotNull(reference);
      assertNotNull(reference.resolve());
    }, testName + ".mxml", testName + "_2.as", testName + "_3.as", testName + "_4.as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testInheritingFromAnotherMxmlComponent() {
    final String testName = getTestName(false);

    doTestFor(true, getDefaultProjectRoot(testName), (Runnable)null, testName + File.separatorChar + testName + ".mxml",
              testName + File.separatorChar + "xxx" + File.separatorChar + testName + "2.mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testNonImplementedInterface() throws Exception {
    final String testName = getTestName(false);
    doImplementsTest(testName);
    doImplementsTest(testName + "_2");
    doImplementsTest(testName + "_3");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testNonImplementedInterface2() {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + "_2.as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testRegress() {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + "_2.as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testDefaultProperty() {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithGumboSdk)
  public void testDefaultPropertyMxml() {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + "_1.mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testUsingClassFactory() {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testStateReferences() {
    enableInspectionTool(new BadExpressionStatementJSInspection());
    defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testNoImportWhenComponentReferenced() {
    defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testNonImplementedInterface3() throws Exception {
    final String testName = getTestName(false);
    doImplementsTest(testName);
  }

  private void doImplementsTest(final String testName) throws Exception {
    final Collection<HighlightInfo> infoCollection = doTestFor(false, testName + ".mxml", "I" + getTestName(false) + ".as");
    findAndInvokeIntentionAction(infoCollection, JavaScriptBundle.message("javascript.fix.implement.methods"), myEditor, myFile);
    checkResultByFile(BASE_PATH + "/" + testName + "_after.mxml");
    JSTestUtils.initJSIndexes(getProject());

    List<HighlightInfo> infoCollection1 = doHighlighting();
    assertEquals(0, countNonInformationHighlights(infoCollection1));
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testFlexWithMockFlex3AsSdk() {
    final String testName = "FlexWithMockFlex3";
    doTestFor(true, testName + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testFlexWithMockFlex4() {
    defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testFlexWithMockFlex4_2() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithoutSourceRoot)
  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testNoSourceRoot() {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testFlex2() {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithGumboSdk)
  public void testInheritedPercentage() {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testDuplicatedIdsInMxml() {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testSchemaProblem() {
    String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + "_2.as", testName + "_3.mxml", testName + "_4.mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testEmbedInNodes() throws Exception {
    enableInspectionTool(new UnterminatedStatementJSInspection());
    final String testName = getTestName(false);
    final Collection<HighlightInfo> infoCollection = doTestFor(true, testName + ".mxml");
    findAndInvokeIntentionAction(infoCollection, "Terminate statement", myEditor, myFile);
    checkResultByFile(BASE_PATH + "/" + testName + "_after.mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testReferencingClass() {
    doTestFor(true, getTestName(false) + ".mxml", getTestName(false) + "_2.as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testConstructorNotAllowedInMxml() {
    doTestFor(true, getTestName(false) + ".mxml", getTestName(false) + "_2.as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testNativeConstructorAllowedInMxml() {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  /*, WithoutSourceRoot*/
  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testEmbedWithAbsLocation() {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  /*, WithoutSourceRoot*/
  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testReferencingClassWithNonEmptyPackage() {
    final String testName = getTestName(false);
    final String packageName = "aaa";

    doTestFor(true, getDefaultProjectRoot(testName), (Runnable)null, testName + "/" + packageName + "/" + testName + "M.mxml",
              testName + "/" + packageName + "/" + testName + ".as", testName + "/bbb/" + testName + "M2.mxml",
              testName + "/bbb/_Underscore.mxml", testName + "/bbb/lowercase.mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testReferencingComponentWithNonEmptyPackage() {
    final String testName = getTestName(false);
    final String packageName = "aaa";

    doTestFor(true, getDefaultProjectRoot(testName), (Runnable)null, testName + "/" + packageName + "/" + testName + ".as",
              testName + "/" + packageName + "/" + testName + "M.mxml", testName + "/bbb/" + testName + "M2.mxml");
  }

  private File getDefaultProjectRoot(final String testName) {
    return new File(getTestDataPath() + getBasePath() + File.separatorChar + testName);
  }

  public void testRefsInEmbed() {
    doTestFor(true, getTestName(false) + ".as");
    enableInspectionTool(new CssUnknownTargetInspection());
    doTestFor(true, getTestName(false) + ".css");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testPathsInMxml() {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testRegExpInAttribute() {
    defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testEnumeratedValues() {
    defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testDefaultPropertyWithArrayType() {
    String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + "_2.as", testName + "_3.as");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testClassesInType() {
    doTestFor(true, getTestName(false) + ".as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testAsSpecific() {
    disableInspectionTool(JSUnusedLocalSymbolsInspection.SHORT_NAME);
    final String testName = getTestName(false);
    doTestFor(true, testName + ".as", testName + "_2.as");
  }

  public void testUsingSwcStubs() {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".as", testName + ".swc");
    checkNavigatableSymbols("layoutObject");

    final Set<String> resolvedNses = new HashSet<>();
    final Ref<String> foundConst = new Ref<>();
    final Ref<String> foundConst2 = new Ref<>();

    myFile.acceptChildren(new JSRecursiveElementVisitor() {
      @Override
      public void visitJSReferenceExpression(JSReferenceExpression node) {
        super.visitJSReferenceExpression(node);
        final PsiElement resolve = node.resolve();

        if (node.getParent() instanceof JSUseNamespaceDirective) {
          foundConst2.set(ActionScriptPsiImplUtil.calcNamespaceReference(node.getParent()));
        }
        else if (resolve instanceof JSVariable && ((JSVariable)resolve).isConst()) {
          foundConst.set(StringUtil.stripQuotesAroundValue(((JSVariable)resolve).getInitializer().getText()));
        }
        if (resolve instanceof JSAttributeListOwner) {
          final JSAttributeList attributeList = ((JSAttributeListOwner)resolve).getAttributeList();
          final String ns = ActionScriptPsiImplUtil.resolveNamespaceValue(attributeList);
          if (ns != null) resolvedNses.add(ns);
        }
      }
    });

    String ns = "http://www.adobe.com/2006/flex/mx/internal";
    assertEquals(1, resolvedNses.size());
    assertEquals(ns, foundConst.get());
    assertEquals(ns, foundConst2.get());
    assertTrue(resolvedNses.contains(ns));
  }

  private void checkNavigatableSymbols(String s) {
    JavaScriptIndex scriptIndex = JavaScriptIndex.getInstance(myProject);
    String[] strings = JSIndexTest.getSymbolNames(myProject);
    Arrays.sort(strings);

    assertTrue(Arrays.binarySearch(strings, s) >= 0);

    NavigationItem[] navigationItems = scriptIndex.getSymbolsByName(s, true);
    assertTrue(navigationItems.length > 0);

    boolean containedInLibrarySwf = false;
    for (NavigationItem navigationItem : navigationItems) {
      if (navigationItem instanceof JSNamedElement &&
          "library.swf".equals(((PsiElement)navigationItem).getContainingFile().getName())) {
        containedInLibrarySwf = true;
        break;
      }
    }

    assertTrue(containedInLibrarySwf);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testUsingImportFromScriptInAttribute() {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + "_2.as", testName + "_2.mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testReferencingMxmlFromActionScript() {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".as", "MxmlInTopLevelPackage.mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testUsingSwcStubs2() {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + ".swc");
    checkNavigatableSymbols("getQualifiedClassName");
  }

  @FlexTestOptions(
    {FlexTestOption.WithFlexFacet, FlexTestOption.WithUnusedImports})
  public void testUsingSwcStubs3() {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".as", testName + ".swc");
  }

  public void testAs2Specific() throws Exception {
    disableInspectionTool(JSUnusedLocalSymbolsInspection.SHORT_NAME);
    final String fileName = getTestName(false);
    runUntypedDeclarationInspectionTestWithFix(fileName, new String[]{fileName + ".as", fileName + "_2.as"}, "as");
  }

  public void testAs2Specific2() throws Exception {
    final String fileName = getTestName(false);
    runUntypedDeclarationInspectionTestWithFix(fileName, new String[]{fileName + ".as"}, "as");
  }

  public void testImportClassIntention() throws Exception {
    runImportClassIntentionTest("com.ImportClassIntention", "", "as");
  }

  public void testImportClassIntention2() throws Exception {
    runImportClassIntentionTest("com.ImportClassIntention2", false, "", "as");
  }

  private void runImportClassIntentionTest(String className, String suffix, String ext, String... moreFiles) throws Exception {
    runImportClassIntentionTest(className, true, suffix, ext, moreFiles);
  }

  private void runImportClassIntentionTest(String className, boolean shouldBeAvailable, String suffix, String ext, String... moreFiles)
    throws Exception {
    final String testName = getTestName(false);

    String[] files = new String[1 + (moreFiles != null ? moreFiles.length : 0)];
    if (moreFiles != null) System.arraycopy(moreFiles, 0, files, 1, moreFiles.length);
    files[0] = testName + suffix + "." + ext;

    final Collection<HighlightInfo> infoCollection = doTestFor(true, files);
    String actionName = className + "?";
    if (!shouldBeAvailable) {
      IntentionAction intentionAction = findIntentionAction(infoCollection, actionName, myEditor, myFile);
      assertNull(intentionAction);
      return;
    }
    findAndInvokeIntentionAction(infoCollection, actionName, myEditor, myFile);
    checkResultByFile(BASE_PATH + "/" + testName + suffix + "_after." + ext);

    JSTestUtils.initJSIndexes(getProject());

    final Collection<HighlightInfo> infosAfterApplyingAction = doHighlighting();
    assertEquals(countNonInformationHighlights(infoCollection) - 1, countNonInformationHighlights(infosAfterApplyingAction));
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testImportClassIntentionInMxml() throws Exception {
    runImportClassIntentionTest("com.MyClass", "", "mxml", getTestName(false) + "_2.as");
  }

  //@FlexTestOptions({JSTestOption.WithFlexSdk})
  //public void testImportClassIntentionInMxml_2() throws Exception {
  //  runImportClassIntentionTest("com.MyClass", "","mxml", "ImportClassIntentionInMxml_2.as");
  //}

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testImportClassIntentionInMxml_3() throws Exception {
    runImportClassIntentionTest("com.MyClass", "", "mxml", "ImportClassIntentionInMxml_2.as");
  }

  @Override
  protected String getBasePath() {
    return BASE_PATH;
  }

  @Override
  protected String getExtension() {
    return "as";
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testRemoteObject() {
    defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testDuplicates() {
    defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testOverridingMethods() throws Exception {
    overrideMethodTest(getTestName(false) + "_2");
    overrideMethodTest(getTestName(false));
  }

  private void overrideMethodTest(String testName) throws Exception {
    configureByFiles(null, BASE_PATH + "/" + testName + ".mxml");
    invokeNamedActionWithExpectedFileCheck(testName, "OverrideMethods", "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testImplementingMarkers() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, (Runnable)null, testName + ".mxml", testName + ".as");
    invokeNamedActionWithExpectedFileCheck(testName, "ImplementMethods", "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testreferencingLowercasedComponent() {
    defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testImplementingMarkers2() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, (Runnable)null, testName + ".mxml", testName.substring(0, testName.length() - 1) + ".as");
    invokeNamedActionWithExpectedFileCheck(testName, "ImplementMethods", "mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexSdk, FlexTestOption.WithUnusedImports})
  public void testOptimizeImports() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    final String testName = getTestName(false);
    doTestFor(true, (Runnable)null, testName + ".mxml", testName + "_2.as");
    invokeNamedActionWithExpectedFileCheck(testName, "OptimizeImports", "mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexSdk, FlexTestOption.WithUnusedImports})
  public void testOptimizeImportsFqn() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    final String testName = getTestName(true);
    doTestFor(true, (Runnable)null, testName + ".as", testName + "_2.as");
    invokeNamedActionWithExpectedFileCheck(testName, "OptimizeImports", "as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testGenerateGetterAndSetter() throws Exception {
    doGenerateTest("Generate.GetSetAccessor.JavaScript", "mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testGenerateGetterAndSetterIntentions() throws Exception {
    final String name = getTestName(false);
    doTestIntention(name + "Getter", "as", JavaScriptBundle.message("javascript.intention.create.getter", "foo"));
    doTestIntention(name + "Setter", "mxml", JavaScriptBundle.message("javascript.intention.create.setter", "foo"));
    doTestIntention(name + "GetterAndSetter", "as", JavaScriptBundle.message("javascript.intention.create.getter.setter", "foo"));
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testGenerateEventHandlerInMxml() throws Exception {
    doGenerateTest("Generate.EventHandler.Actionscript", "_1", "mxml");
    doGenerateTest("Generate.EventHandler.Actionscript", "_2", "mxml");
    doGenerateTest("Generate.EventHandler.Actionscript", "_3", "mxml");
    doGenerateTest("Generate.EventHandler.Actionscript", "_4", "mxml");
    doGenerateTest("Generate.EventHandler.Actionscript", "_5", "mxml");
    doGenerateTest("Generate.EventHandler.Actionscript", "_6", "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testGenerateEventHandlerInAs() throws Exception {
    doGenerateTest("Generate.EventHandler.Actionscript", "_1", "as");
    doGenerateTest("Generate.EventHandler.Actionscript", "_2", "as");
    doGenerateTest("Generate.EventHandler.Actionscript", "_3", "as");
    doGenerateTest("Generate.EventHandler.Actionscript", "_4", "as");
    doGenerateTest("Generate.EventHandler.Actionscript", "_5", "as");
    doGenerateTest("Generate.EventHandler.Actionscript", "_6", "as");
    doGenerateTest("Generate.EventHandler.Actionscript", "_7", "as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCreateEventHandlerIntention() throws Exception {
    final String intention = FlexBundle.message("intention.create.event.handler");
    doTestIntention("GenerateEventHandlerInMxml_1", "mxml", intention);
    doTestIntention("GenerateEventHandlerInMxml_2", "mxml", intention);
    //doTestCreateEventHandlerIntention("GenerateEventHandlerInMxml_3", "mxml"); not applicable for intention
    //doTestIntention("GenerateEventHandlerInMxml_4", "mxml", intention); not applicable for intention
    doTestIntention("GenerateEventHandlerInMxml_5", "mxml", intention);
    doTestIntention("GenerateEventHandlerInMxml_6", "mxml", intention);
    //doTestCreateEventHandlerIntention("GenerateEventHandlerInMxml_1", "as"); not applicable for intention
    doTestIntention("GenerateEventHandlerInAs_2", "as", intention);
    doTestIntention("GenerateEventHandlerInAs_3", "as", intention);
    doTestIntention("GenerateEventHandlerInAs_4", "as", intention);
    doTestIntention("GenerateEventHandlerInAs_5", "as", intention);
    doTestIntention("GenerateEventHandlerInAs_6", "as", intention);
    doTestIntention("GenerateEventHandlerInAs_7", "as", intention);
  }

  private void doTestIntention(final String fileName, final String extension, final String intention) throws Exception {
    findAndInvokeIntentionAction(doTestFor(true, fileName + "." + extension), intention, myEditor, myFile);
    checkResultByFile(BASE_PATH + "/" + fileName + "_after." + extension);
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testCompilerConfigHighlighting() {
    enableInspectionTool(new CheckXmlFileWithXercesValidatorInspection());
    doTestFor(true, getTestName(false) + ".xml");
    doTestFor(true, getTestName(false) + "_2.xml");
    doTestFor(true, getTestName(false) + "_3.xml");
    doTestFor(true, getTestName(false) + "_4.xml");
    doTestFor(true, getTestName(false) + "_5.xml", getTestName(false) + "_4.xml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testReferencesInCompilerConfig() {
    doTestFor(true, getTestName(false) + ".xml");
    doTestFor(true, getTestName(false) + "_2.xml");
  }

  @FlexTestOptions(
    {FlexTestOption.WithFlexSdk, FlexTestOption.WithFlexLib})
  public void testUsingUrlForOtherLibrary() {
    defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testStatesProblem() {
    defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testProblemWithInstanceVarFromStaticMethod() {
    defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testSpellChecker() throws Exception {
    enableInspectionTool(new SpellCheckingInspection());
    configureByFile(getBasePath() + "/" + getTestName(false) + ".mxml");
    ExpectedHighlightingData expectedHighlightingData = new ExpectedHighlightingData(myEditor.getDocument(), true, true, false);
    Collection<HighlightInfo> infoCollection = checkHighlighting(expectedHighlightingData);
    assertEquals(1, countNonInformationHighlights(infoCollection));
    findAndInvokeActionWithExpectedCheck(RenameTo.getFixName(), "mxml", infoCollection);
  }

  @NeedsJavaModule
  public void testUsingUrlForOtherLibrary2() {
    defaultTest();
    doTestFor(true, getTestName(false) + "_2.mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testObjectCanHaveAnyAttr() {
    defaultTest();
  }

  @Override
  protected Collection<HighlightInfo> defaultTest() {
    return doTestFor(true, getTestName(false) + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testSomeFeedbackFromAdobe() {
    defaultTest();
  }

  @FlexTestOptions({FlexTestOption.WithGumboSdk, FlexTestOption.WithFlexFacet})
  public void testSomeFeedbackFromAdobe_2() {
    String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + "_2.as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testBindingDestinationCanHaveThisModifier() {
    defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testSomeFeedbackFromAdobe2() {
    String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", StringUtil.decapitalize(testName) + "_2.mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testResourceReferences() {
    Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".mxml", "mybundle.properties", "mybundle3.properties");
    findAndInvokeIntentionAction(infoCollection, PropertiesBundle.message("create.property.quickfix.text"), myEditor, myFile);

    List<HighlightInfo> infoList = filterUnwantedInfos(doHighlighting(), this);
    assertEquals(infoCollection.size() - 1, infoList.size());
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testResourceReferences2() {
    doTestFor(true, getTestName(false) + ".mxml", "mybundle.properties", "mybundle3.properties");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testResourceReferencesWithPackages() {
    String testName = getTestName(false);
    doTestFor(true, getDefaultProjectRoot(testName), (Runnable)null,
              testName + "/" + testName + ".mxml",
              testName + "/resourcepackage/mybundle4.properties");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testResolveWithInclude() {
    String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + "_2.as");
  }

  @Override
  protected void doCommitModel(@NotNull final ModifiableRootModel rootModel) {
    super.doCommitModel(rootModel);
    FlexTestUtils.setupFlexLib(myProject, getClass(), getTestName(false));
    FlexTestUtils.addFlexUnitLib(getClass(), getTestName(false), getModule(), FlexTestUtils.getTestDataPath("flexUnit"),
                                 FlexUnitLibs.FLEX_UNIT_0_9_SWC, FlexUnitLibs.FLEX_UNIT_4_SWC);
    if (myAfterCommitRunnable != null) {
      myAfterCommitRunnable.run();
    }
  }


  //@FlexTestOptions({WithJsSupportLoader})
  //public void testImportScopeOverriddenByBaseClass() throws Exception {
  //  doTestFor(true, getTestName(false) +".as", getTestName(false) +"_2.as", getTestName(false) +"_3.as");
  //}

  @FlexTestOptions(FlexTestOption.WithUnusedImports)
  public void testFqnUsingStar() {
    doTestFor(true, getTestName(false) + ".as", getTestName(false) + "_2.as", getTestName(false) + "_3.as");
  }

  @FlexTestOptions(FlexTestOption.WithGumboSdk)
  public void testNamespacesAndManifestFiles() {
    final String name = getTestName(false);

    FlexTestUtils.modifyBuildConfiguration(myModule, bc -> {
      final String manifest = getTestDataPath() + "/" + getBasePath() + "/" + name + "_manifest.xml";
      bc.getCompilerOptions().setAllOptions(Collections.singletonMap("compiler.namespaces.namespace", "http://MyNamespace\t" + manifest));
    });

    doTestFor(true, name + ".mxml", name + "_other.mxml", name + "_other2.as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testNamespacesAndManifestsFromCustomConfigFile() {
    final String testName = getTestName(false);

    FlexTestUtils.modifyBuildConfiguration(myModule, bc -> {
      final String path = getTestDataPath() + "/" + getBasePath() + "/" + testName + "_custom_config.xml";
      bc.getCompilerOptions().setAdditionalConfigFilePath(path);
    });

    doTestFor(true, testName + ".mxml", testName + "_other.as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testManifestClassHighlighting() {
    doTestFor(true, getTestName(false) + ".xml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testNamespacesFromSdkSwcs() {
    final String name = getTestName(false);

    final SdkModificator sdkModificator = FlexTestUtils.getFlexSdkModificator(getModule());
    final VirtualFile swcFile = LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + "/" + getBasePath() + "/" + name + ".swc");
    sdkModificator.addRoot(JarFileSystem.getInstance().getJarRootForLocalFile(swcFile), OrderRootType.CLASSES);
    sdkModificator.commitChanges();

    doTestFor(true, name + ".mxml");
  }

  @FlexTestOptions(
    {FlexTestOption.WithFlexSdk, FlexTestOption.WithUnusedImports})
  public void testPredefinedFunReference() {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    doTestFor(true, getTestName(false) + ".mxml", "getFoo.swc");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testInlineComponentsClassName() {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testInlineComponentsOuterFieldAccess() {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testResolveTypeToComponent() {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testInheritanceCheck() {
    doTestFor(true, getTestName(false) + ".mxml", getTestName(false) + "_2.as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testAssets() {
    final String testName = getTestName(false);
    final String fileRelPath = "pack/" + testName + ".mxml";

    final VirtualFile swcFile =
      LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + getBasePath() + "/" + testName + "/" + testName + ".swc");
    final SdkModificator modificator = FlexTestUtils.getFlexSdkModificator(getModule());
    modificator.addRoot(JarFileSystem.getInstance().getJarRootForLocalFile(swcFile), OrderRootType.CLASSES);
    modificator.commitChanges();

    final Module dependentModule = doCreateRealModule("dependent");
    final VirtualFile contentRoot =
      LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + getBasePath() + "/" + testName + "_dependent_module");

    PsiTestUtil.addSourceRoot(dependentModule, contentRoot);


    final String absPath = LocalFileSystem.getInstance().findFileByPath("").getPath();
    myAfterCommitRunnable = () -> ApplicationManager.getApplication().runWriteAction(() -> {
      replaceText(fileRelPath, "${SOME_ABSOLUTE_PATH}", absPath);
      ModuleRootModificationUtil.addDependency(myModule, dependentModule);
    });

    doTestFor(true, getDefaultProjectRoot(testName), (Runnable)null, testName + "/" + fileRelPath);
  }

  private void replaceText(final String fileRelativePath, final String oldText, final String newText) {
    final VirtualFile file = VfsUtilCore.findRelativeFile(fileRelativePath, ModuleRootManager.getInstance(myModule).getSourceRoots()[0]);
    try {
      final String newContent = StringUtil.convertLineSeparators(new String(file.contentsToByteArray(),
                                                                            StandardCharsets.UTF_8)).replace(oldText, newText);
      createEditor(file).getDocument().setText(newContent);
      PsiDocumentManager.getInstance(myProject).commitAllDocuments();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testMxScriptAndStyleSource() {
    final String testName = getTestName(false);
    final String fileRelPath = "pack/" + testName + ".mxml";

    final String absPath = LocalFileSystem.getInstance().findFileByPath("").getPath();
    myAfterCommitRunnable = () -> {
      FlexTestUtils.addLibrary(myModule, "lib", getTestDataPath() + getBasePath() + "/", testName + "/" + testName + ".swc", null, null);
      replaceText(fileRelPath, "${SOME_ABSOLUTE_PATH}", absPath);
    };

    doTestFor(true, getDefaultProjectRoot(testName), (Runnable)null, testName + "/" + fileRelPath);
  }

  @FlexTestOptions({})
  public void testExtendMultipleClasses() {
    doTestFor(true, getTestName(false) + ".as");
  }

  @FlexTestOptions({})
  public void testExtendFinalClass() {
    doTestFor(true, getTestName(false) + ".as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testClassAndPackageNestedInMxml() {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testHttpService() {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testNamespace() {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testResourceBundleInSdkSources() {
    final String testName = getTestName(false);

    final VirtualFile file =
      LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + "/" + getBasePath() + "/" + testName + "/" + testName + ".swc");
    final VirtualFile swcFile = JarFileSystem.getInstance().getJarRootForLocalFile(file);
    final VirtualFile srcFile = LocalFileSystem.getInstance()
      .findFileByPath(getTestDataPath() + getBasePath() + "/" + testName + "/sdk_src/" + testName + "_sdk_src.as");

    FlexTestUtils.setupCustomSdk(myModule, swcFile, srcFile.getParent(), null);

    setActiveEditor(createEditor(srcFile));
    doDoTest(true, false, true);
  }

  @FlexTestOptions
  public void testResourceBundleInLibSources() throws Exception {
    final String testName = getTestName(false);

    final VirtualFile srcZipFile =
      LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + getBasePath() + "/" + testName + ".zip");
    final VirtualFile srcFile = JarFileSystem.getInstance().getJarRootForLocalFile(srcZipFile).findChild(testName + "_sdk_src.as");

    myAfterCommitRunnable =
      () -> FlexTestUtils
        .addLibrary(myModule, "library", getTestDataPath() + getBasePath() + "/", testName + ".swc", testName + ".zip", null);
    configureByFile(getBasePath() + "/" + testName + ".as"); // file not used; we need just to have content root configured

    setActiveEditor(createEditor(srcFile));
    doDoTest(true, false, true);
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testIgnoreClassesFromOnlyLibSources() {
    final String testName = getTestName(false);

    myAfterCommitRunnable = () -> FlexTestUtils
      .addLibrary(myModule, "library", getTestDataPath() + getBasePath() + "/", testName + "/empty.swc", testName + "/LibSources.zip",
                  null);

    doTestFor(true, testName + "/" + testName + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testIgnoreClassesFromOnlySdkSources() {
    final String testName = getTestName(false);

    final VirtualFile srcFile =
      LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + getBasePath() + "/" + testName + "/sdk_src/");

    final SdkModificator modificator = FlexTestUtils.getFlexSdkModificator(myModule);
    modificator.addRoot(srcFile, OrderRootType.SOURCES);
    modificator.commitChanges();

    doTestFor(true, testName + "/" + testName + ".mxml");
  }

  public void testComponentsFromLibsResolvedWhenSdkNotSet() {
    FlexTestUtils.addFlexLibrary(false, myModule, "lib", true, getTestDataPath() + getBasePath(), getTestName(false) + ".swc", null, null);
    doTestFor(true, getTestName(false) + ".mxml");
  }

  public void testIntroduceFieldAmbiguous() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult(JavaScriptBundle.message("javascript.create.field.intention.name", "v"), "as",
                                              getTestName(false) + ".as", getTestName(false) + "_2.as");
  }

  public void testAddTypeToDeclarationAmbiguous() throws Exception {
    enableInspectionTool(new JSUntypedDeclarationInspection());
    doHighlightingWithInvokeFixAndCheckResult("Add Type to Declaration", "as", getTestName(false) + ".as", getTestName(false) + "_2.as");
  }

  public void testCreateFunctionAmbiguous() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult(JavaScriptBundle.message("javascript.create.method.intention.name", "foo"), "as",
                                              getTestName(false) + ".as", getTestName(false) + "_2.as");
  }

  public void testImplementMethodsAmbiguous() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult(JavaScriptBundle.message("javascript.fix.implement.methods"), "as", getTestName(false) + ".as",
                                              getTestName(false) + "_2.as",
                                              getTestName(false) + "_3.as");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testFlex4Namespaces() {
    doTestFor(true, getTestName(false) + ".mxml", getTestName(false) + "_other.as");
  }

  public void testQualifiedConstructorStillNeedsImport() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult("com.foo.MyClass?", "as", getTestName(false) + ".as",
                                              getTestName(false) + "_2.as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testConditionalCompilationDefinitions() {
    final String testName = getTestName(false);
    FlexTestUtils.modifyBuildConfiguration(myModule, bc -> {
      bc.getCompilerOptions()
        .setAdditionalConfigFilePath(getTestDataPath() + "/" + getBasePath() + "/" + testName + "_custom_config.xml");
      bc.getCompilerOptions().setAdditionalOptions(
        "-some_path=\"with space\" -define=CONFIG::Object,'string_value' -define+=CONFIG::defined2,true -define CONFIG::defined3 -1");
      bc.getCompilerOptions().setAllOptions(Collections.singletonMap("compiler.define", "CONFIG::defined4\tfalse\nCONFIG::defined5\t"));
    });

    final ModuleOrProjectCompilerOptions projectLevelOptions =
      FlexProjectLevelCompilerOptionsHolder.getInstance(myProject).getProjectLevelCompilerOptions();
    final ModuleOrProjectCompilerOptions moduleLevelOptions =
      FlexBuildConfigurationManager.getInstance(myModule).getModuleLevelCompilerOptions();

    moduleLevelOptions.setAdditionalOptions("-define=CONFIG::defined6,true");
    projectLevelOptions.setAdditionalOptions("-define=CONFIG::defined7,true -debug");

    moduleLevelOptions.setAllOptions(Collections.singletonMap("compiler.define", "CONFIG::not_defined1\tfalse"));
    projectLevelOptions.setAllOptions(Collections.singletonMap("compiler.define", "CONFIG::not_defined2\tfalse"));

    doTestFor(true, testName + ".mxml");
  }

  public void testConditionalCompilationDefinitionsInNonProjectFiles() throws Exception {
    final String testName = getTestName(false);
    final VirtualFile file =
      LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + "/" + getBasePath() + "/" + testName + ".as");
    final File toDirIO = createTempDirectory();
    final VirtualFile toDir = getVirtualFile(toDirIO);
    VirtualFile copyRef =
      copy(file, toDir, file.getName());

    setActiveEditor(createEditor(copyRef));
    doDoTest(true, false, true);
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testPredefinedTagsInsideRootTagWithDefaultProperty() {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testPredefinedTagsInsideNotRootTagWithDefaultProperty() {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testXmlRelatedTags() {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  public void testNamespaceInInterface() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult("Remove namespace reference", "as", getTestName(false) + ".as", "MyNs.as");
  }

  @FlexTestOptions(FlexTestOption.WithGumboSdk)
  public void testStaticSettersCanNotBeAttributes() {
    doTestFor(true, getTestName(false) + ".mxml", getTestName(false) + "_other.as");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testFxComponent() {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testNoResolveToJsVarOrFun() {
    doTestFor(true, getTestName(false) + ".mxml", getTestName(false) + ".js");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testFxPrivate() {
    doTestFor(true, getTestName(false) + ".mxml");

    final JSClassResolver resolver =
      JSDialectSpecificHandlersFactory.forLanguage(JavaScriptSupportLoader.ECMA_SCRIPT_L4).getClassResolver();
    final JSClass jsClass =
      (JSClass)resolver.findClassByQName("mx.controls.CheckBox", GlobalSearchScope.moduleWithLibrariesScope(myModule));
    final Collection<PsiReference> usages = ReferencesSearch.search(jsClass, GlobalSearchScope.moduleScope(myModule)).findAll();
    assertEquals(1, usages.size());
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testModelTag() {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testInheritDefaultProperty() {
    doTestFor(true, getTestName(false) + ".mxml", getTestName(false) + "_other.as");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testFxLibraryAndFxDefinition() {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testSameShortDifferentFullClassNames() {
    // example taken from IDEA-52241
    doTestFor(true, getTestName(false) + ".mxml", getTestName(false) + "_other.as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testPackagesAsNamespaces() {
    final String testName = getTestName(false);
    myAfterCommitRunnable = () -> {
      final VirtualFile srcRoot =
        LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + getBasePath() + "/" + testName + "/");
      PsiTestUtil.addSourceRoot(myModule, srcRoot);
    };
    doTestFor(true, testName + "/" + testName + ".mxml");

    final PsiReference psiReference = myFile.findReferenceAt(myEditor.getCaretModel().getOffset());
    assertEquals(1, ((PsiPolyVariantReference)psiReference).multiResolve(false).length);
    final PsiElement element = psiReference.resolve();
    assertTrue(element instanceof JSClass);
    assertEquals("one.two.three.MyComponent", ((JSClass)element).getQualifiedName());
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testComponentIdInsideRepeaterResolvedToArray() {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml");

    PsiElement element = InjectedLanguageManager.getInstance(myProject).findInjectedElementAt(myFile, myEditor.getCaretModel().getOffset());
    JSExpression expression = PsiTreeUtil.getParentOfType(element, JSExpression.class);
    assertEquals("mx.containers.Panel[]", ActionScriptResolveUtil.getQualifiedExpressionType(expression, myFile));
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public final void testSuggestCorrectMxNamespace() throws Exception {
    doTestIntention(getTestName(false), "mxml", "Create namespace declaration");
  }

  @FlexTestOptions(FlexTestOption.WithGumboSdk)
  public final void testUserNamespacesWithoutScheme() {
    final String name = getTestName(false);

    final String manifest = getTestDataPath() + "/" + getBasePath() + "/" + name + "_manifest.xml";
    FlexBuildConfigurationManager.getInstance(myModule).getModuleLevelCompilerOptions()
      .setAllOptions(Collections.singletonMap("compiler.namespaces.namespace", "SomeNamespace1\t" + manifest));

    doTestFor(true, name + ".mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public final void testFlex3AllowsNonVisualComponentsOnlyUnderRootTag() {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + "_other1.mxml", testName + "_other2.mxml", testName + "_other3.as");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public final void testFlex4AllowsNonVisualComponentsOnlyUnderDeclarationsTag() {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testImplicitImport() {
    defaultTest();
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk, FlexTestOption.WithUnusedImports})
  public void testFlex3ImplicitImport() {
    defaultTest();
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk, FlexTestOption.WithUnusedImports})
  public void testFlex4ImplicitImport() {
    defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public final void testDoNotForgetSdkComponents() {
    final String testName = getTestName(false);
    myAfterCommitRunnable = () -> FlexTestUtils
      .addLibrary(myModule, "library", getTestDataPath() + getBasePath() + "/", testName + ".swc", null, null);
    doTestFor(true, testName + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testPropertiesSpecifiedByMxmlIdAttributes() {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + "_other.mxml", testName + "_otherSuper.mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testFxg() {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + "_other.fxg");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testWebAndHttpServiceRequestTagContent() {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testFxReparent() {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testStaticBlock() {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testLanguageNamespacesMixed() throws Exception {
    final String testName = getTestName(false);
    final Collection<HighlightInfo> infoCollection = doTestFor(true, testName + ".mxml");
    findAndInvokeIntentionAction(infoCollection, "Remove Namespace Declaration", myEditor, myFile);
    findAndInvokeIntentionAction(doHighlighting(), "Remove Namespace Declaration", myEditor, myFile);
    assertNull(findIntentionAction(doHighlighting(), "Remove Namespace Declaration", myEditor, myFile));
    checkResultByFile(BASE_PATH + "/" + testName + "_after.mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testLanguageNamespaceAbsent() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult("Declare Namespace http://ns.adobe.com/mxml/2009", "mxml", getTestName(false) + ".mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testMonkeyPatching() {
    myAfterCommitRunnable = () -> FlexTestUtils
      .addLibrary(myModule, "library", getTestDataPath() + getBasePath() + "/", getTestName(false) + ".swc", null, null);
    doTestFor(true, getTestName(false) + ".mxml", getTestName(false) + "_2.as");
  }

  public void testNotResolveTestClasses() {
    final String testName = getTestName(false);

    myAfterCommitRunnable = new Runnable() {
      @Override
      public void run() {
        try {
          final VirtualFile testSrcRoot =
            ModuleRootManager.getInstance(myModule).getContentRoots()[0].createChildDirectory(this, "testSrc");
          final VirtualFile testClassFile =
            LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + getBasePath() + "/" + testName + "Test.as");
          assertNotNull(testClassFile);
          testClassFile.copy(this, testSrcRoot, testClassFile.getName());
          PsiTestUtil.addSourceRoot(myModule, testSrcRoot, true);
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    };
    doTestFor(true, testName + ".as");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testNoReferenceToAnyContentTags() {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testUnusedNamespaces() {
    enableInspectionTool(new XmlUnusedNamespaceInspection());
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testMxmlRootTagLeadsToFinalClass() {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testCreateSkinByAttributeValue() throws Exception {
    final String testName = getTestName(false);
    doHighlightingWithInvokeFixAndCheckResult("Create Skin 'MySkin'", "mxml");
    final VirtualFile verificationFile =
      LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + getBasePath() + "/" + testName + "_Skin.mxml");
    final VirtualFile skinFile =
      VfsUtilCore.findRelativeFile("/foo/MySkin.mxml", ModuleRootManager.getInstance(myModule).getSourceRoots()[0]);
    assertNotNull(verificationFile);
    assertNotNull(skinFile);

    FileDocumentManager.getInstance().saveAllDocuments();
    assertEquals(StringUtil.convertLineSeparators(VfsUtilCore.loadText(verificationFile)),
                 StringUtil.convertLineSeparators(VfsUtilCore.loadText(skinFile)));
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testCreateMobileViewByTagValue() throws Exception {
    final String testName = getTestName(false);
    doHighlightingWithInvokeFixAndCheckResult("Create View 'MyView'", "mxml");
    final VirtualFile verificationFile =
      LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + getBasePath() + "/" + testName + "_View.mxml");
    final VirtualFile createdFile =
      VfsUtilCore.findRelativeFile("/foo/bar/MyView.mxml", ModuleRootManager.getInstance(myModule).getSourceRoots()[0]);
    assertNotNull(verificationFile);
    assertNotNull(createdFile);

    FileDocumentManager.getInstance().saveAllDocuments();
    assertEquals(StringUtil.convertLineSeparators(VfsUtilCore.loadText(verificationFile)),
                 StringUtil.convertLineSeparators(VfsUtilCore.loadText(createdFile)));
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testCreateMobileViewByUnresolvedRef() throws Exception {
    final String testName = getTestName(false);
    doHighlightingWithInvokeFixAndCheckResult("Create View 'MyView'", "mxml");
    final VirtualFile verificationFile =
      LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + getBasePath() + "/" + testName + "_View.mxml");
    final VirtualFile createdFile =
      VfsUtilCore.findRelativeFile("foo/MyView.mxml", ModuleRootManager.getInstance(myModule).getSourceRoots()[0]);
    assertNotNull(verificationFile);
    assertNotNull(createdFile);

    FileDocumentManager.getInstance().saveAllDocuments();
    assertEquals(StringUtil.convertLineSeparators(VfsUtilCore.loadText(verificationFile)),
                 StringUtil.convertLineSeparators(VfsUtilCore.loadText(createdFile)));
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testCreateClassByAttributeValue() throws Exception {
    FlexTestUtils.disableFileHeadersInTemplates(getProject());
    final String testName = getTestName(false);
    doHighlightingWithInvokeFixAndCheckResult(JavaScriptBundle.message("javascript.create.class.intention.name", "Foo"), "mxml");
    final VirtualFile verificationFile =
      LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + getBasePath() + "/" + testName + "_Foo.as");
    final VirtualFile createdFile =
      VfsUtilCore.findRelativeFile("/foo/Foo.as", ModuleRootManager.getInstance(myModule).getSourceRoots()[0]);
    assertNotNull(verificationFile);
    assertNotNull(createdFile);

    FileDocumentManager.getInstance().saveAllDocuments();
    assertEquals(StringUtil.convertLineSeparators(VfsUtilCore.loadText(verificationFile)),
                 StringUtil.convertLineSeparators(VfsUtilCore.loadText(createdFile)));
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testAddingModuleDependencyAffectsHighlighting() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml");
    WriteCommandAction.writeCommandAction(getProject()).run(() -> {
      final Module otherModule =
        createModuleFromTestData(getTestDataPath() + getBasePath() + "/" + testName + "_other_module", "OtherModule",
                                 FlexModuleType.getInstance(), true);
      FlexTestUtils.addFlexModuleDependency(myModule, otherModule);
      Disposer.register(getTestRootDisposable(), otherModule);
    });
    assertEmpty(doHighlighting());
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testResolveToClassWithBiggestTimestamp() {
    final String testName = getTestName(false);
    myAfterCommitRunnable = () -> FlexTestUtils
      .addLibrary(myModule, "lib_1", getTestDataPath() + getBasePath() + "/", testName + "_lib_1.swc", testName + "_lib_1_src.zip",
                  null);
    doTestFor(true, testName + ".as");
    FlexTestUtils.addLibrary(myModule, "lib_2", getTestDataPath() + getBasePath() + "/", testName + "_lib_2.swc", null, null);

    final List<HighlightInfo> highlightingInfo = filterUnwantedInfos(doHighlighting(), this);
    assertEquals(1, highlightingInfo.size());
    assertEquals("functionFromLib1", highlightingInfo.get(0).getText());
  }

  @FlexTestOptions(FlexTestOption.WithGumboSdk)
  public void testResolveToClassWithBiggestTimestamp2() {
    // older lib (lib2) contains SomeClass not implementing anything
    // newer lib (lib1) contains SomeClass implementing SomeInterface
    // must resolve to newer SomeClass
    final String testName = getTestName(false);
    myAfterCommitRunnable = () -> {
      FlexTestUtils.addLibrary(myModule, "lib_1", getTestDataPath() + getBasePath() + "/", testName + "_lib1.swc", null, null);
      FlexTestUtils.addLibrary(myModule, "lib_2", getTestDataPath() + getBasePath() + "/", testName + "_lib2.swc", null, null);
    };
    doTestFor(true, testName + ".as");
  }

  public void testOverrideCustomNsFromSwc() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".as", testName + ".swc");
    invokeNamedActionWithExpectedFileCheck(testName, "OverrideMethods", "as");

    doTestFor(true, testName + "_2.as", testName + ".swc");
    invokeNamedActionWithExpectedFileCheck(testName + "_2", "OverrideMethods", "as");
  }

  @JSTestOptions(JSTestOption.WithoutWarnings)
  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testSqlInjection1() {
    doTestFor(false, getTestName(false) + ".as");
  }

  private void testCreateSubclass(boolean subclass) throws Exception {
    FlexTestUtils.disableFileHeadersInTemplates(getProject());
    final String testName = getTestName(false);
    findAndInvokeIntentionAction(doTestFor(true, testName + ".as"), subclass ? "Create Subclass" : "Implement Interface", myEditor, myFile);

    VirtualFile subclassFile = myFile.getVirtualFile().getParent().findFileByRelativePath("foo/" + testName + "Impl.as");
    myEditor = FileEditorManager.getInstance(myProject).openTextEditor(new OpenFileDescriptor(myProject, subclassFile), true);
    myFile = PsiDocumentManager.getInstance(myProject).getPsiFile(myEditor.getDocument());
    checkResultByFile(getBasePath() + "/" + testName + (subclass ? "_subclass.as" : "_implementation.as"));
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCreateSubclassIntention() throws Exception {
    testCreateSubclass(true);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCreateSubclassIntention2() throws Exception {
    testCreateSubclass(true);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCreateSubclassIntention3() throws Exception {
    testCreateSubclass(true);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testImplementInterfaceIntention() throws Exception {
    testCreateSubclass(false);
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testLanguageTagsInInlineRenderer() {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testChangeSignatureForEventHandler() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change Foo.bar() signature", getTestName(false) + ".as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testChangeSignatureForEventHandler_2() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change Foo.bar() signature", getTestName(false) + ".as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testChangeSignatureForEventHandler2() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change Foo.<anonymous>() signature", getTestName(false) + ".as");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testReferenceInlineComponentId() {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testMxmlConstructor() {
    enableInspectionTool(new JSUnusedGlobalSymbolsInspection());
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testIdAndPredefinedAttributes() {
    doTestFor(true, getTestName(false) + ".mxml", getTestName(false) + "_other.as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testInlineComponentsImports() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult("mypackage.List?", "mxml", getTestName(false) + ".mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testImportForNeighbourClass() {
    doTestFor(true, getTestName(false) + ".mxml", getTestName(false) + "_2.as");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @SuppressWarnings("ConstantConditions")
  public void testAfterRename() throws Exception {
    configureByFile(getBasePath() + "/afterRename.xml");
    XmlNSDescriptor nsDescriptor = ((XmlFile)getFile()).getDocument().getRootTagNSDescriptor();
    assertTrue(nsDescriptor.toString(), nsDescriptor instanceof XmlNSDescriptorImpl);
    doDoTest(true, true);
    new RenameProcessor(getProject(), getFile(), "afterRename.mxml", false, false).run();
    assertEquals("afterRename.mxml", getFile().getName());
    assertFalse(myFile.isValid());
    myFile = myPsiManager.findFile(getFile().getVirtualFile().getParent().findChild("afterRename.mxml"));
    assertTrue(myFile.isValid());
    nsDescriptor = ((XmlFile)getFile()).getDocument().getRootTagNSDescriptor();
    assertTrue(nsDescriptor.toString(), nsDescriptor instanceof FlexMxmlNSDescriptor);
    doDoTest(true, true);
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @SuppressWarnings("ConstantConditions")
  public void testDumbMode() throws Exception {
    DumbServiceImpl.getInstance(getProject()).setDumb(true);
    ((DaemonCodeAnalyzerImpl)DaemonCodeAnalyzer.getInstance(getProject())).mustWaitForSmartMode(false, getTestRootDisposable());
    XmlNSDescriptor nsDescriptor;
    try {
      assertTrue(DumbService.isDumb(getProject()));
      configureByFile(getBasePath() + "/dumbMode.mxml");
      nsDescriptor = ((XmlFile)getFile()).getDocument().getRootTagNSDescriptor();
      assertTrue(nsDescriptor.toString(), nsDescriptor instanceof FlexMxmlNSDescriptor);
    }
    finally {
      DumbServiceImpl.getInstance(getProject()).setDumb(false);
    }
    nsDescriptor = ((XmlFile)getFile()).getDocument().getRootTagNSDescriptor();
    assertTrue(nsDescriptor.toString(), nsDescriptor instanceof FlexMxmlNSDescriptor);
    doDoTest(true, true);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testMxmlAttrPreferResolveToEvent() {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + "_2.as");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testBindingType() throws Exception {
    final String testName = getTestName(false);
    Collection<HighlightInfo> highlightInfos = doTestFor(true, (Runnable)null, testName + ".mxml");
    findAndInvokeIntentionAction(highlightInfos, JavaScriptBundle.message("javascript.create.method.intention.name", "getMsg"), myEditor, myFile);
    checkResultByFile(BASE_PATH + "/" + testName + "_after.mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testBindingType2() throws Exception {
    final String testName = getTestName(false);
    Collection<HighlightInfo> highlightInfos = doTestFor(true, (Runnable)null, testName + ".mxml");
    findAndInvokeIntentionAction(highlightInfos, JavaScriptBundle.message("javascript.create.field.intention.name", "label"), myEditor, myFile);
    checkResultByFile(BASE_PATH + "/" + testName + "_after.mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCreateClassFromMetadataAttr() throws Exception {
    FlexTestUtils.disableFileHeadersInTemplates(getProject());
    final String testName = getTestName(false);
    findAndInvokeIntentionAction(doTestFor(true, testName + ".as"), JavaScriptBundle.message("javascript.create.class.intention.name", "Baz"), myEditor, myFile);
    assertEmpty(filterUnwantedInfos(doHighlighting(), this));
    final VirtualFile createdFile = VfsUtilCore.findRelativeFile("foo/Baz.as", myFile.getVirtualFile().getParent());
    myEditor = FileEditorManager.getInstance(myProject).openTextEditor(new OpenFileDescriptor(myProject, createdFile), true);
    myFile = PsiDocumentManager.getInstance(myProject).getPsiFile(myEditor.getDocument());
    checkResultByFile(getBasePath() + "/" + testName + "_created.as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCreateEventClassFromMetadataAttr() throws Exception {
    FlexTestUtils.disableFileHeadersInTemplates(getProject());
    final String testName = getTestName(false);
    findAndInvokeIntentionAction(doTestFor(true, testName + ".as"),
                                 JavaScriptBundle.message("javascript.create.class.intention.name", "MyEvent"), myEditor, myFile);
    assertEmpty(filterUnwantedInfos(doHighlighting(), this));
    final VirtualFile createdFile = VfsUtilCore.findRelativeFile("foo/MyEvent.as", myFile.getVirtualFile().getParent());
    myEditor = FileEditorManager.getInstance(myProject).openTextEditor(new OpenFileDescriptor(myProject, createdFile), true);
    myFile = PsiDocumentManager.getInstance(myProject).getPsiFile(myEditor.getDocument());
    checkResultByFile(getBasePath() + "/" + testName + "_created.as");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testImport() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult("mx.rpc.http.mxml.HTTPService?", "mxml", getTestName(false) + ".mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public final void testMobileApplicationChildren() {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public final void testInternalPropertiesInMxml() {
    final String testName = getTestName(false);
    doTestFor(true, getDefaultProjectRoot(testName), (Runnable)null, testName + "/pack/" + testName + ".mxml",
              testName + "/InRootPackage.mxml", testName + "/Child.as", testName + "/pack/ParentInPack.as", testName + "/pack/InPack.mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testRequireImportInParamList() {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithUnusedImports})
  public void testOptimizeImportInParamList() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml");
    invokeNamedActionWithExpectedFileCheck(getTestName(false), "OptimizeImports", "mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testHostComponentImplicitField() {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + "_Skin.mxml");
  }

  private void doTestQuickFixForRedMxmlAttribute(final String quickFixName, final String otherFileExtension) throws IOException {
    final String testName = getTestName(false);
    final Collection<HighlightInfo> highlightInfos = doTestFor(true, testName + ".mxml", testName + "_other." + otherFileExtension);
    findAndInvokeIntentionAction(highlightInfos, quickFixName, myEditor, myFile);
    assertEmpty(doHighlighting());

    final VirtualFile verificationFile = LocalFileSystem.getInstance()
      .findFileByPath(getTestDataPath() + getBasePath() + "/" + testName + "_other_after." + otherFileExtension);
    assertEquals(StringUtil.convertLineSeparators(VfsUtilCore.loadText(verificationFile)),
                 FileEditorManager.getInstance(myProject).getSelectedTextEditor().getDocument().getText());
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testCreateFieldByMxmlAttribute() throws Exception {
    doTestQuickFixForRedMxmlAttribute(JavaScriptBundle.message("javascript.create.field.intention.name", "foo"), "as");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testCreateFieldByMxmlAttribute2() throws Exception {
    doTestQuickFixForRedMxmlAttribute(JavaScriptBundle.message("javascript.create.field.intention.name", "foo"), "mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testCreateSetterByMxmlAttribute() throws Exception {
    doTestQuickFixForRedMxmlAttribute(JavaScriptBundle.message("javascript.create.set.property.intention.name", "foo"), "as");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testCreateSetterByMxmlAttribute2() throws Exception {
    doTestQuickFixForRedMxmlAttribute(JavaScriptBundle.message("javascript.create.set.property.intention.name", "foo"), "mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testDeclareEventByMxmlAttribute() throws Exception {
    doTestQuickFixForRedMxmlAttribute("Declare Event 'foo'", "as");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testDeclareEventByMxmlAttribute2() throws Exception {
    doTestQuickFixForRedMxmlAttribute("Declare Event 'foo'", "mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testDeclareEventByMxmlAttribute3() throws Exception {
    doTestQuickFixForRedMxmlAttribute("Declare Event 'foo'", "mxml");
  }

  private void useTestSourceRoot() {
    myAfterCommitRunnable = () -> {
      final ModifiableRootModel model = ModuleRootManager.getInstance(myModule).getModifiableModel();
      final ContentEntry contentEntry = model.getContentEntries()[0];
      final SourceFolder sourceFolder = contentEntry.getSourceFolders()[0];
      contentEntry.clearSourceFolders();
      contentEntry.addSourceFolder(sourceFolder.getUrl(), true);
      model.commit();
    };
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithFlexUnit4})
  public void testGenerateTestMethod() throws Exception {
    useTestSourceRoot();
    doGenerateTest("Generate.TestMethod.Actionscript", "as");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithFlexUnit1})
  public void testGenerateSetUpMethod() throws Exception {
    useTestSourceRoot();
    doGenerateTest("Generate.SetUp.Actionscript", "as");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithFlexUnit4})
  public void testGenerateTearDownMethod() throws Exception {
    useTestSourceRoot();
    doGenerateTest("Generate.TearDown.Actionscript", "as");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithFlexUnit4})
  public void testGenerateTestClass() throws Exception {
    FlexTestUtils.disableFileHeadersInTemplates(getProject());
    final String testName = getTestName(false);
    configureByFiles(BASE_PATH + "/" + testName, BASE_PATH + "/" + testName + "/pack/" + testName + ".mxml");
    final FlexUnitTestCreator testCreator = new FlexUnitTestCreator();
    assertTrue(testCreator.isAvailable(myProject, myEditor, myFile));

    testCreator.createTest(myProject, myEditor, myFile);

    final VirtualFile testClassFile =
      ModuleRootManager.getInstance(myModule).getSourceRoots()[0].findFileByRelativePath("pack/" + testName + "Test.as");
    myEditor = FileEditorManager.getInstance(myProject).openTextEditor(new OpenFileDescriptor(myProject, testClassFile), true);
    myFile = PsiDocumentManager.getInstance(myProject).getPsiFile(myEditor.getDocument());
    checkResultByFile(BASE_PATH + "/" + testName + "/pack/" + testName + "_after.as");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testSpacesInGenericVector() {
    doTestFor(true, getTestName(false) + ".as");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testStyleValues() {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + "_other.as");
  }

  @FlexTestOptions(FlexTestOption.WithGumboSdk)
  public void testOutOfSourceRoot() {
    myAfterCommitRunnable = () -> {
      final ModifiableRootModel model = ModuleRootManager.getInstance(myModule).getModifiableModel();
      final ContentEntry contentEntry = model.getContentEntries()[0];
      contentEntry.clearSourceFolders();
      model.commit();
    };
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testMakeMethodStatic() throws Exception {
    JSMethodCanBeStaticInspection inspection = new JSMethodCanBeStaticInspection();
    JSTestUtils.setInspectionHighlightLevel(myProject, inspection, HighlightDisplayLevel.WARNING, getTestRootDisposable());
    enableInspectionTool(inspection);
    doHighlightingWithInvokeFixAndCheckResult("Make 'static'", "mxml", getTestName(false) + ".mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testImplementsNonPublicInterface() throws Exception {
    String testName = getTestName(false);
    String prefix = testName + "/";
    String secondFilePath = prefix + "foo/" + testName + "_2.as";
    final Collection<HighlightInfo> infoCollection =
      doTestFor(true, new File(getTestDataPath() + BASE_PATH + "/" + prefix), (Runnable)null, prefix + "foo/" + testName + ".mxml",
                secondFilePath);
    findAndInvokeIntentionAction(infoCollection, "Make interface 'INonPublic' public", myEditor, myFile);
    FileDocumentManager.getInstance().saveAllDocuments();
    VirtualFile f = ContainerUtil.find(FileEditorManager.getInstance(myProject).getOpenFiles(),
                                       virtualFile -> virtualFile.getName().endsWith("as"));
    VirtualFile expectedFile = findVirtualFile(BASE_PATH + "/" + testName + "_2_after.as");
    assertEquals(StringUtil.convertLineSeparators(VfsUtilCore.loadText(expectedFile)), StringUtil.convertLineSeparators(
      VfsUtilCore.loadText(f)));
  }

  public void testBadResolveOfSuperClass() {
    final Sdk sdk = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.5"), null, true, getTestRootDisposable());

    WriteAction.run(() -> FlexTestUtils.modifyConfigs(myProject, editor -> {
      final ModifiableFlexBuildConfiguration bc1 = editor.getConfigurations(myModule)[0];
      FlexTestUtils.setSdk(bc1, sdk);

      final ModifiableFlexBuildConfiguration bc2 = editor.createConfiguration(myModule);
      bc2.setNature(new BuildConfigurationNature(TargetPlatform.Desktop, false, OutputType.Application));
      bc2.setName("2");
      FlexTestUtils.setSdk(bc2, sdk);
    }));

    doTestFor(true, getTestName(false) + ".mxml");
  }

  public void testAddLeadingSlashForEmbeddedAsset() {
    final String testName = getTestName(false);
    final Collection<HighlightInfo> infoCollection =
      doTestFor(true, new File(getTestDataPath() + BASE_PATH + "/" + testName), (Runnable)null,
                testName + "/pack/" + testName + ".as",
                testName + "/assets/foo.txt");
    findAndInvokeIntentionAction(infoCollection, "Add leading slash", myEditor, myFile);
  }

  @FlexTestOptions(FlexTestOption.WithGumboSdk)
  public void testUseOfTestClass() {
    myAfterCommitRunnable = () -> ApplicationManager.getApplication().runWriteAction(() -> {
      ModifiableRootModel m = ModuleRootManager.getInstance(myModule).getModifiableModel();
      ContentEntry contentEntry = m.getContentEntries()[0];
      SourceFolder sourceFolder = contentEntry.getSourceFolders()[0];
      contentEntry.removeSourceFolder(sourceFolder);
      contentEntry.addSourceFolder(contentEntry.getFile().findChild("src"), false);
      contentEntry.addSourceFolder(contentEntry.getFile().findChild("tests"), true);
      m.commit();
    });

    File projectRoot = new File(getTestDataPath() + getBasePath() + "/" + getTestName(false));
    doTestFor(false, projectRoot, (Runnable)null, getTestName(false) + "/src/Main.mxml");
  }

  @FlexTestOptions(FlexTestOption.WithGumboSdk)
  public void testReportAccessorProblems() {
    defaultTest();
  }

  @FlexTestOptions({FlexTestOption.WithGumboSdk, FlexTestOption.WithFlexLib})
  public void testGuessFrameworkNamespace() throws Throwable {
    doTestIntention(getTestName(false), "mxml", "Create namespace declaration");
  }

  public void testFontFaceProperties() {
    final Sdk sdk = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.5"), null, true, getTestRootDisposable());
    WriteAction.run(() -> FlexTestUtils.modifyBuildConfiguration(myModule, bc -> FlexTestUtils.setSdk(bc, sdk)));

    enableInspectionTool(new CssUnknownPropertyInspection());
    defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithGumboSdk)
  public void testUnresolvedClassReference() throws Throwable {
    FlexTestUtils.disableFileHeadersInTemplates(getProject());
    final String testName = getTestName(false);
    doHighlightingWithInvokeFixAndCheckResult(JavaScriptBundle.message("javascript.create.class.intention.name", "MyZuperClass"), "mxml");

    final VirtualFile expectedFile =
      LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + getBasePath() + "/" + testName + "_2.as");
    final VirtualFile createdFile =
      VfsUtilCore.findRelativeFile("/foo/MyZuperClass.as", ModuleRootManager.getInstance(myModule).getSourceRoots()[0]);
    assertNotNull(expectedFile);
    assertNotNull(createdFile);

    FileDocumentManager.getInstance().saveAllDocuments();
    assertEquals(StringUtil.convertLineSeparators(VfsUtilCore.loadText(expectedFile)),
                 StringUtil.convertLineSeparators(VfsUtilCore.loadText(createdFile)));
  }

  @FlexTestOptions(FlexTestOption.WithGumboSdk)
  public void testUnresolvedClassReference2() throws Throwable {
    FlexTestUtils.disableFileHeadersInTemplates(getProject());
    final String testName = getTestName(false);
    doHighlightingWithInvokeFixAndCheckResult("Create MXML Component 'Missing'", "mxml");

    final VirtualFile expectedFile =
      LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + getBasePath() + "/" + testName + "_2.mxml");
    final VirtualFile createdFile =
      VfsUtilCore.findRelativeFile("/foo/boo2/Missing.mxml", ModuleRootManager.getInstance(myModule).getSourceRoots()[0]);
    assertNotNull(expectedFile);
    assertNotNull(createdFile);

    FileDocumentManager.getInstance().saveAllDocuments();
    String expected = StringUtil.convertLineSeparators(VfsUtilCore.loadText(expectedFile));
    Properties properties = FileTemplateManager.getInstance(getProject()).getDefaultProperties();
    expected = FileTemplateUtil.mergeTemplate(properties, expected, false);
    assertEquals(expected,
                 StringUtil.convertLineSeparators(VfsUtilCore.loadText(createdFile)));
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testTypeOfMethodNamedCall() {
    doTestFor(true, getTestName(false) + ".as");
  }

  @FlexTestOptions(FlexTestOption.WithGumboSdk)
  public void testStateGroups() {
    defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithGumboSdk)
  public void testStarlingEvent() {
    doTestFor(true, getTestName(false) + ".as");
  }

  @FlexTestOptions(FlexTestOption.WithGumboSdk)
  public void testImplicitCoercion() {
    doTestFor(true, getTestName(false) + ".as");
  }

  public void testNewObjectClassFunction() {
    doTestFor(true, getTestName(false) + ".as");
  }

  @FlexTestOptions(FlexTestOption.WithGumboSdk)
  public void testDoNotResolveNotImportedSuperClass() {
    doTestFor(true, getTestName(false) + ".as", getTestName(false) + "_other.as");
  }

  @FlexTestOptions(FlexTestOption.WithGumboSdk)
  public void testCanBeLocalInMxml() {
    enableInspectionTool(new JSFieldCanBeLocalInspection());
    doTestFor(true, getTestName(false) + ".mxml", getTestName(false) + "_other.as");
  }

  public void testPrivateMemberAccessibleWithinFile() {
    doTestFor(true, getTestName(false) + ".as", getTestName(false) + "_other.as");
  }

  @FlexTestOptions(FlexTestOption.WithGumboSdk)
  public void testPercentProxyInOverridden() {
    defaultTest();
  }

  public void testCreateMethodAfterCallExpression() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult(JavaScriptBundle.message("javascript.create.method.intention.name", "bar"), "as");
  }
}
