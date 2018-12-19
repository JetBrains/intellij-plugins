package com.intellij.flex.highlighting;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoFilter;
import com.intellij.codeInsight.daemon.impl.analysis.XmlPathReferenceInspection;
import com.intellij.codeInsight.daemon.impl.analysis.XmlUnusedNamespaceInspection;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.navigation.ImplementationSearcher;
import com.intellij.codeInspection.LocalInspectionTool;
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
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.JSClassResolver;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.search.JSFunctionsSearch;
import com.intellij.lang.properties.PropertiesBundle;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.XmlHighlighterColors;
import com.intellij.openapi.extensions.Extensions;
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
import com.intellij.psi.css.inspections.CssInvalidElementInspection;
import com.intellij.psi.css.inspections.CssNegativeValueInspection;
import com.intellij.psi.css.inspections.CssUnknownPropertyInspection;
import com.intellij.psi.css.inspections.CssUnusedSymbolInspection;
import com.intellij.psi.css.inspections.bugs.CssNoGenericFontNameInspection;
import com.intellij.psi.css.inspections.bugs.CssUnitlessNumberInspection;
import com.intellij.psi.css.inspections.invalid.CssInvalidFunctionInspection;
import com.intellij.psi.css.inspections.invalid.CssInvalidHtmlTagReferenceInspection;
import com.intellij.psi.css.inspections.invalid.CssInvalidPropertyValueInspection;
import com.intellij.psi.css.inspections.invalid.CssUnknownTargetInspection;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
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
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.*;

import static com.intellij.codeInsight.daemon.impl.HighlightInfoFilter.EXTENSION_POINT_NAME;

public class FlexHighlightingTest extends ActionScriptDaemonAnalyzerTestCase {
  @NonNls static final String BASE_PATH = "flex_highlighting";

  protected Runnable myAfterCommitRunnable = null;

  {
    myTestsWithJSSupportLoader.addAll(
      Arrays.asList("Flex", "Flex2", "FlexWithLocalCss", "DuplicatedIdsInMxml", "PathesInMxml", "ReferencingClass", "EnumeratedValues"));

    myTestsWithCssLoader.addAll(Arrays.asList("Flex", "FlexWithLocalCss"));
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD})
  public @interface NeedsJavaModule {
  }

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
    Extensions.getRootArea().getExtensionPoint(EXTENSION_POINT_NAME).registerExtension(filter);
    Disposer.register(getTestRootDisposable(),
                      () -> Extensions.getRootArea().getExtensionPoint(EXTENSION_POINT_NAME).unregisterExtension(filter));
  }


  @Override
  protected void tearDown() throws Exception {
    myAfterCommitRunnable = null;
    super.tearDown();
  }

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

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testHtmlCss() throws Exception {
    enableInspectionTool(new CssInvalidPropertyValueInspection());
    enableInspectionTool(new CssInvalidHtmlTagReferenceInspection());
    enableInspectionTool(new CssUnknownPropertyInspection());
    doTestFor(true, getTestName(false) + ".html");
  }

  @NeedsJavaModule
  public void testFlex() throws Exception {
    enableInspectionTool(new JSMethodCanBeStaticInspection());
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + ".css");
    final List<PsiReference> cssRefs = collectCssRefs();

    assertEquals(1, cssRefs.size());
    final PsiElement element = cssRefs.get(0).resolve();
    assertNotNull(element);
    assertEquals(element.getContainingFile().getName(), testName + ".css");
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testFlexSpecificFunctionsInCss() throws Exception {
    enableInspectionTool(new CssInvalidElementInspection());
    enableInspectionTool(new CssInvalidPropertyValueInspection());
    enableInspectionTool(new CssInvalidFunctionInspection());
    doTestFor(true, getTestName(false) + ".css");
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testFlexWithCss() throws Exception {
    //enableInspectionTool(new FlexCssStrayBraceInspection());
    registerCommonCssInspections();
    enableInspectionTool(new CssUnknownTargetInspection());

    defaultTest();
    doTestFor(true, getTestName(false) + ".css");
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testFlexWithCss2() throws Exception {
    enableInspectionTool(new CssUnusedSymbolInspection());
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testFlexWithCss3() throws Exception {
    enableInspectionTool(new CssUnitlessNumberInspection());
    enableInspectionTool(new CssNegativeValueInspection());
    enableInspectionTool(new CssNoGenericFontNameInspection());
    enableInspectionTool((LocalInspectionTool)Class.forName("org.jetbrains.w3validators.css.W3CssValidatorInspection").newInstance());
    doTestFor(true, getTestName(false) + ".css");
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testFlexWithCss4() throws Exception {
    enableInspectionTool(new CssInvalidPropertyValueInspection());
    myAfterCommitRunnable =
      () -> FlexTestUtils
        .addLibrary(myModule, "library", getTestDataPath() + getBasePath() + "/", "LibForCss.swc", "LibForCss_src.zip", null);
    doTestFor(true, getTestName(false) + ".css");
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testFlexWithCss5() throws Exception {
    enableInspectionTool(new CssUnusedSymbolInspection());
    defaultTest();
    doTestFor(true, getTestName(false) + ".css");
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testFlexWithCss6() throws Exception {
    registerCommonCssInspections();
    myAfterCommitRunnable =
      () -> FlexTestUtils
        .addLibrary(myModule, "library", getTestDataPath() + getBasePath() + "/", "LibForCss.swc", "LibForCss_src.zip", null);
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testFlexWithCss7() throws Exception {
    enableInspectionTool(new CssInvalidPropertyValueInspection());
    enableInspectionTool(new CssInvalidHtmlTagReferenceInspection());
    doTestFor(true, getTestName(false) + ".css");
  }

  public void testFlexCssShouldNotComplainAboutUnitlessLength() throws Exception {
    enableInspectionTool(new CssInvalidPropertyValueInspection());
    doTestFor(true, getTestName(false) + ".css");
  }

  public void testCssDialectMappings() throws Exception {
    enableInspectionTool(new CssInvalidPropertyValueInspection());
    enableInspectionTool(new CssUnknownPropertyInspection());
    try {
      CssDialectMappings.getInstance(getProject()).setMapping(null, CssDialect.CLASSIC);
      doTestFor(true, getTestName(false) + ".css");
    }
    finally {
      CssDialectMappings.getInstance(getProject()).cleanupForNextTest();
    }
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testSwitchToFlexCssQuickFix1() throws Exception {
    enableInspectionTool(new CssInvalidHtmlTagReferenceInspection());
    try {
      CssDialectMappings.getInstance(getProject()).setMapping(null, CssDialect.CLASSIC);
      doHighlightingWithInvokeFixAndCheckResult(
        CssBundle.message("switch.to.css.dialect.quickfix.name", FlexCSSDialect.getInstance().getDisplayName()), "css");
    }
    finally {
      CssDialectMappings.getInstance(getProject()).cleanupForNextTest();
    }
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testSwitchToFlexCssQuickFix2() throws Exception {
    try {
      CssDialectMappings.getInstance(getProject()).setMapping(null, CssDialect.CLASSIC);
      enableInspectionTool(new CssUnknownPropertyInspection());
      doHighlightingWithInvokeFixAndCheckResult(
        CssBundle.message("switch.to.css.dialect.quickfix.name", FlexCSSDialect.getInstance().getDisplayName()), "css");
    }
    finally {
      CssDialectMappings.getInstance(getProject()).cleanupForNextTest();
    }
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testSwitchToFlexCssQuickFix3() throws Exception {
    enableInspectionTool(new CssInvalidHtmlTagReferenceInspection());
    try {
      CssDialectMappings.getInstance(getProject()).setMapping(null, CssDialect.CLASSIC);

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

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testSwitchToClassicCssQuickFix1() throws Exception {
    enableInspectionTool(new CssInvalidHtmlTagReferenceInspection());
    doHighlightingWithInvokeFixAndCheckResult(
      CssBundle.message("switch.to.css.dialect.quickfix.name", CssDialect.CLASSIC.getDisplayName()), "css");
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testSwitchToClassicCssQuickFix2() throws Exception {
    enableInspectionTool(new CssUnknownPropertyInspection());
    doHighlightingWithInvokeFixAndCheckResult(
      CssBundle.message("switch.to.css.dialect.quickfix.name", CssDialect.CLASSIC.getDisplayName()), "css");
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testSwitchToClassicCssQuickFix3() throws Exception {
    enableInspectionTool(new CssInvalidHtmlTagReferenceInspection());
    configureByFile(BASE_PATH + '/' + getTestName(false) + '.' + "mxml");
    doHighlighting();
    final List<IntentionAction> actions = CodeInsightTestFixtureImpl.getAvailableIntentions(myEditor, myFile);
    final String text = CssBundle.message("switch.to.css.dialect.quickfix.name", CssDialect.CLASSIC.getDisplayName());
    final IntentionAction action = CodeInsightTestUtil.findIntentionByText(actions, text);
    assertNull(action);
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testFlexWithCssIdSelectorAfterClassSelector() throws Exception {
    enableInspectionTool(new CssUnusedSymbolInspection());
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
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

  /*@JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testHtmlCssFile() throws Exception {
    enableInspectionTool(new FlexCssStrayBraceInspection());
    enableInspectionTool((LocalInspectionTool)Class.forName("com.intellij.psi.css.inspections.CssInvalidElementInspection").newInstance());
    enableInspectionTool((LocalInspectionTool)Class.forName("com.intellij.psi.css.inspections.CssUnknownPropertyInspection").newInstance());
    enableInspectionTool(
      (LocalInspectionTool)Class.forName("com.intellij.psi.css.inspections.CssInvalidHtmlTagReferenceInspection").newInstance());

    doTestFor(true, getTestName(false) + ".css", "HtmlFileReferringToCss.html");
  }*/

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testHtmlCssFile1() throws Exception {
    registerCommonCssInspections();

    doTestFor(true, getTestName(false) + ".css");
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testHtmlCssFile2() throws Exception {
    registerCommonCssInspections();

    doTestFor(true, getTestName(false) + ".css", "HtmlFileReferringToCss.html", "MxmlFileReferringToCss.mxml");
  }

  private List<PsiReference> collectCssRefs() {
    final List<PsiReference> cssRefs = new ArrayList<>();

    myFile.acceptChildren(new XmlRecursiveElementVisitor() {
      @Override
      public void visitXmlAttribute(final XmlAttribute attr) {
        if ("styleName".equals(attr.getName())) {
          final XmlAttributeValue value = attr.getValueElement();
          if (value != null) ContainerUtil.addAll(cssRefs, value.getReferences());
        }
      }
    });
    return cssRefs;
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testCallbackSignatureMismatch() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml", getTestName(false) + "_2.as");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testCallbackSignatureMismatch2() throws Exception {
    doTestFor(true, getTestName(false) + ".as");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testCircularDependency() throws Exception {
    defaultTest();
  }

  @NeedsJavaModule
  public void testFlexWithLocalCss() throws Exception {
    defaultTest();
    final List<PsiReference> cssRefs = collectCssRefs();

    assertEquals(1, cssRefs.size());
    final PsiElement element = cssRefs.get(0).resolve();
    assertNotNull(element);
    assertEquals(element.getContainingFile().getName(), getTestName(false) + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testFlexWithMockFlex() throws Exception {
    enableInspectionTool(new CssInvalidElementInspection());
    final String testName = getTestName(false);
    doHighlightingWithInvokeFixAndCheckResult("Add override modifier", "mxml", testName + ".mxml", testName + "_2.as");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testItemRendererAsAttribute() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + "_other.mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithGumboSdk})
  public void testFlexWithMockFlex2009() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", "FlexWithMockFlex_2.as");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithGumboSdk})
  public void testSkinFile2009() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testSkinFile2009_2() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithGumboSdk})
  public void testEnhancedStatesSyntax2009() throws Exception {
    final String name = getTestName(false);
    final String manifest = getTestDataPath() + "/" + getBasePath() + "/" + name + "_manifest.xml";
    final Map<String, String> options = Collections.singletonMap("compiler.namespaces.namespace", "http://MyNamespace\t" + manifest);
    FlexProjectLevelCompilerOptionsHolder.getInstance(myProject).getProjectLevelCompilerOptions().setAllOptions(options);

    doTestFor(true, name + ".mxml", name + "_other.as");
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithGumboSdk})
  public void testFlexWithMockFlex2009_2() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + "_2.mxml");
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testFlexWithModelTag() throws Exception {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testCheckUniquenessInPackage() throws Exception {
    String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + ".as");
    doTestFor(true, testName + ".as", testName + ".mxml");
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet, JSTestOption.WithLineMarkers})
  public void testFlexWithMockFlexWithLineMarkers() throws Exception {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testFlexWithMockFlex2() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, null, highlightInfos -> {
      myFile.accept(new XmlRecursiveElementVisitor() {
        @Override
        public void visitXmlAttribute(final XmlAttribute attribute) {
          final XmlAttributeDescriptor descriptor = attribute.getDescriptor();
          if (descriptor instanceof AnnotationBackedDescriptor) {
            assertNotNull(descriptor.getDeclaration());
          }
        }
      });

      try {
        final String actionName = "Create Method 'bar'";
        findAndInvokeIntentionAction(highlightInfos, actionName, myEditor, myFile);
        assertNull(findIntentionAction(doHighlighting(), actionName, myEditor, myFile));
      }
      catch (IncorrectOperationException ex) {
        throw new RuntimeException(ex);
      }
      return null;
    }, testName + ".mxml", testName + "_2.mxml", testName + "_2.as", testName + "_3.mxml");
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testFlexWithMockFlex3() throws Exception {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk, JSTestOption.WithUnusedImports})
  public void testFlexWithMockFlex3_2() throws Exception {
    final String testName = getTestName(false);
    final Collection<HighlightInfo> infoCollection = doTestFor(true, testName + ".mxml", testName + "_2.as");
    findAndInvokeIntentionAction(infoCollection, "foo.bar.IFoo?", myEditor, myFile);
    checkResultByFile(BASE_PATH + "/" + testName + "_after.mxml");
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testInclude() throws Exception {
    final String testName = getTestName(false);

    doTestFor(true, null, () -> {
      PsiElement element =
        InjectedLanguageManager.getInstance(myProject).findInjectedElementAt(myFile, myEditor.getCaretModel().getOffset());
      PsiReference reference = PsiTreeUtil.getParentOfType(element, JSReferenceExpression.class);
      assertNotNull(reference);
      assertNotNull(reference.resolve());
    }, testName + ".mxml", testName + "_2.as", testName + "_3.as", testName + "_4.as");
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testInheritingFromAnotherMxmlComponent() throws Exception {
    final String testName = getTestName(false);

    doTestFor(true, getDefaultProjectRoot(testName), (Runnable)null, testName + File.separatorChar + testName + ".mxml",
              testName + File.separatorChar + "xxx" + File.separatorChar + testName + "2.mxml");
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testNonImplementedInterface() throws Exception {
    final String testName = getTestName(false);
    doImplementsTest(testName);
    doImplementsTest(testName + "_2");
    doImplementsTest(testName + "_3");
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testNonImplementedInterface2() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + "_2.as");
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testRegress() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + "_2.as");
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testDefaultProperty() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithGumboSdk})
  public void testDefaultPropertyMxml() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + "_1.mxml");
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testUsingClassFactory() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml");
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testStateReferences() throws Exception {
    enableInspectionTool(new BadExpressionStatementJSInspection());
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testNoImportWhenComponentReferenced() throws Exception {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testNonImplementedInterface3() throws Exception {
    final String testName = getTestName(false);
    doImplementsTest(testName);
  }

  private void doImplementsTest(final String testName) throws Exception {
    final Collection<HighlightInfo> infoCollection = doTestFor(false, testName + ".mxml", "I" + getTestName(false) + ".as");
    findAndInvokeIntentionAction(infoCollection, "Implement Methods", myEditor, myFile);
    checkResultByFile(BASE_PATH + "/" + testName + "_after.mxml");
    JSTestUtils.initJSIndexes(getProject());

    List<HighlightInfo> infoCollection1 = doHighlighting();
    assertEquals(0, countNonInformationHighlights(infoCollection1));
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testFlexWithMockFlex3AsSdk() throws Exception {
    final String testName = "FlexWithMockFlex3";
    doTestFor(true, testName + ".mxml");
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testFlexWithMockFlex4() throws Exception {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testFlexWithMockFlex4_2() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithoutSourceRoot})
  public void testNoSourceRoot() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testFlex2() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithGumboSdk})
  public void testInheritedPercentage() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions(JSTestOption.WithFlexFacet)
  public void testDuplicatedIdsInMxml() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testSchemaProblem() throws Exception {
    String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + "_2.as", testName + "_3.mxml", testName + "_4.mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithJsSupportLoader})
  public void testEmbedInNodes() throws Exception {
    enableInspectionTool(new UnterminatedStatementJSInspection());
    final String testName = getTestName(false);
    final Collection<HighlightInfo> infoCollection = doTestFor(true, testName + ".mxml");
    findAndInvokeIntentionAction(infoCollection, "Terminate statement", myEditor, myFile);
    checkResultByFile(BASE_PATH + "/" + testName + "_after.mxml");
  }

  @JSTestOptions(JSTestOption.WithFlexFacet)
  public void testReferencingClass() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml", getTestName(false) + "_2.as");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testConstructorNotAllowedInMxml() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml", getTestName(false) + "_2.as");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testNativeConstructorAllowedInMxml() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet/*, WithoutSourceRoot*/})
  public void testEmbedWithAbsLocation() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet/*, WithoutSourceRoot*/})
  public void testReferencingClassWithNonEmptyPackage() throws Exception {
    final String testName = getTestName(false);
    final String packageName = "aaa";

    doTestFor(true, getDefaultProjectRoot(testName), (Runnable)null, testName + "/" + packageName + "/" + testName + "M.mxml",
              testName + "/" + packageName + "/" + testName + ".as", testName + "/bbb/" + testName + "M2.mxml",
              testName + "/bbb/_Underscore.mxml", testName + "/bbb/lowercase.mxml");
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testReferencingComponentWithNonEmptyPackage() throws Exception {
    final String testName = getTestName(false);
    final String packageName = "aaa";

    doTestFor(true, getDefaultProjectRoot(testName), (Runnable)null, testName + "/" + packageName + "/" + testName + ".as",
              testName + "/" + packageName + "/" + testName + "M.mxml", testName + "/bbb/" + testName + "M2.mxml");
  }

  private File getDefaultProjectRoot(final String testName) {
    return new File(getTestDataPath() + getBasePath() + File.separatorChar + testName);
  }

  public void testRefsInEmbed() throws Exception {
    doTestFor(true, getTestName(false) + ".as");
    enableInspectionTool(new CssUnknownTargetInspection());
    doTestFor(true, getTestName(false) + ".css");
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testPathsInMxml() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testRegExpInAttribute() throws Exception {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithFlexFacet)
  public void testEnumeratedValues() throws Exception {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testDefaultPropertyWithArrayType() throws Exception {
    String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + "_2.as", testName + "_3.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testClassesInType() throws Exception {
    doTestFor(true, getTestName(false) + ".as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithLoadingAndSavingCaches})
  public void testAsSpecific() throws Exception {
    disableInspectionTool(JSUnusedLocalSymbolsInspection.SHORT_NAME);
    final String testName = getTestName(false);
    doTestFor(true, testName + ".as", testName + "_2.as");
  }

  @JSTestOptions({JSTestOption.WithLoadingAndSavingCaches})
  public void testUsingSwcStubs() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".as", testName + ".swc");
    checkNavigatableSymbols("layoutObject");

    final Set<String> resolvedNses = new THashSet<>();
    final Ref<String> foundConst = new Ref<>();
    final Ref<String> foundConst2 = new Ref<>();

    myFile.acceptChildren(new JSRecursiveElementVisitor() {
      @Override
      public void visitJSReferenceExpression(JSReferenceExpression node) {
        super.visitJSReferenceExpression(node);
        final PsiElement resolve = node.resolve();

        if (node.getParent() instanceof JSUseNamespaceDirective) {
          foundConst2.set(JSPsiImplUtils.calcNamespaceReference(node.getParent()));
        }
        else if (resolve instanceof JSVariable && ((JSVariable)resolve).isConst()) {
          foundConst.set(StringUtil.stripQuotesAroundValue(((JSVariable)resolve).getInitializer().getText()));
        }
        if (resolve instanceof JSAttributeListOwner) {
          final JSAttributeList attributeList = ((JSAttributeListOwner)resolve).getAttributeList();
          final String ns = attributeList != null ? attributeList.resolveNamespaceValue() : null;
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
    String[] strings = scriptIndex.getSymbolNames();
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

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testUsingImportFromScriptInAttribute() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + "_2.as", testName + "_2.mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testReferencingMxmlFromActionScript() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".as", "MxmlInTopLevelPackage.mxml");
  }

  @JSTestOptions({JSTestOption.WithLoadingAndSavingCaches, JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testUsingSwcStubs2() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + ".swc");
    checkNavigatableSymbols("getQualifiedClassName");
  }

  @JSTestOptions(
    {JSTestOption.WithLoadingAndSavingCaches, JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader, JSTestOption.WithUnusedImports})
  public void testUsingSwcStubs3() throws Exception {
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

  @JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithJsSupportLoader})
  public void testImportClassIntentionInMxml() throws Exception {
    runImportClassIntentionTest("com.MyClass", "", "mxml", getTestName(false) + "_2.as");
  }

  //@JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithJsSupportLoader})
  //public void testImportClassIntentionInMxml_2() throws Exception {
  //  runImportClassIntentionTest("com.MyClass", "","mxml", "ImportClassIntentionInMxml_2.as");
  //}

  @JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithJsSupportLoader})
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

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testRemoteObject() throws Exception {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testDuplicates() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithLineMarkers, JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testOverridingMarkers() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, (Runnable)null, testName + ".mxml", testName + "_2.mxml");
    invokeNamedActionWithExpectedFileCheck(testName, "OverrideMethods", "mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testOverridingMethods() throws Exception {
    overrideMethodTest(getTestName(false) + "_2");
    overrideMethodTest(getTestName(false));
  }

  private void overrideMethodTest(String testName) throws Exception {
    configureByFiles(null, BASE_PATH + "/" + testName + ".mxml");
    invokeNamedActionWithExpectedFileCheck(testName, "OverrideMethods", "mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testImplementingMarkers() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, (Runnable)null, testName + ".mxml", testName + ".as");
    invokeNamedActionWithExpectedFileCheck(testName, "ImplementMethods", "mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testreferencingLowercasedComponent() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testImplementingMarkers2() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, (Runnable)null, testName + ".mxml", testName.substring(0, testName.length() - 1) + ".as");
    invokeNamedActionWithExpectedFileCheck(testName, "ImplementMethods", "mxml");
  }

  @JSTestOptions({JSTestOption.WithLineMarkers, JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testHighlightStaticInstanceMembers() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk, JSTestOption.WithUnusedImports})
  public void testOptimizeImports() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    final String testName = getTestName(false);
    doTestFor(true, (Runnable)null, testName + ".mxml", testName + "_2.as");
    invokeNamedActionWithExpectedFileCheck(testName, "OptimizeImports", "mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk, JSTestOption.WithUnusedImports})
  public void testOptimizeImportsFqn() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    final String testName = getTestName(true);
    doTestFor(true, (Runnable)null, testName + ".as", testName + "_2.as");
    invokeNamedActionWithExpectedFileCheck(testName, "OptimizeImports", "as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testGenerateGetterAndSetter() throws Exception {
    doGenerateTest("Generate.GetSetAccessor.JavaScript", "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testGenerateGetterAndSetterIntentions() throws Exception {
    final String name = getTestName(false);
    doTestIntention(name + "Getter", "as", JSBundle.message("javascript.intention.create.getter", "foo"));
    doTestIntention(name + "Setter", "mxml", JSBundle.message("javascript.intention.create.setter", "foo"));
    doTestIntention(name + "GetterAndSetter", "as", JSBundle.message("javascript.intention.create.getter.setter", "foo"));
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testGenerateEventHandlerInMxml() throws Exception {
    doGenerateTest("Generate.EventHandler.Actionscript", "_1", "mxml");
    doGenerateTest("Generate.EventHandler.Actionscript", "_2", "mxml");
    doGenerateTest("Generate.EventHandler.Actionscript", "_3", "mxml");
    doGenerateTest("Generate.EventHandler.Actionscript", "_4", "mxml");
    doGenerateTest("Generate.EventHandler.Actionscript", "_5", "mxml");
    doGenerateTest("Generate.EventHandler.Actionscript", "_6", "mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testGenerateEventHandlerInAs() throws Exception {
    doGenerateTest("Generate.EventHandler.Actionscript", "_1", "as");
    doGenerateTest("Generate.EventHandler.Actionscript", "_2", "as");
    doGenerateTest("Generate.EventHandler.Actionscript", "_3", "as");
    doGenerateTest("Generate.EventHandler.Actionscript", "_4", "as");
    doGenerateTest("Generate.EventHandler.Actionscript", "_5", "as");
    doGenerateTest("Generate.EventHandler.Actionscript", "_6", "as");
    doGenerateTest("Generate.EventHandler.Actionscript", "_7", "as");
  }

  @JSTestOptions(JSTestOption.WithFlexFacet)
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

  @JSTestOptions(
    {JSTestOption.WithFlexSdk})
  public void testCompilerConfigHighlighting() throws Exception {
    enableInspectionTool(new CheckXmlFileWithXercesValidatorInspection());
    doTestFor(true, getTestName(false) + ".xml");
    doTestFor(true, getTestName(false) + "_2.xml");
    doTestFor(true, getTestName(false) + "_3.xml");
    doTestFor(true, getTestName(false) + "_4.xml");
    doTestFor(true, getTestName(false) + "_5.xml", getTestName(false) + "_4.xml");
  }

  @JSTestOptions(
    {JSTestOption.WithFlexSdk})
  public void testReferencesInCompilerConfig() throws Exception {
    doTestFor(true, getTestName(false) + ".xml");
    doTestFor(true, getTestName(false) + "_2.xml");
  }

  @JSTestOptions(
    {JSTestOption.WithFlexSdk, JSTestOption.WithFlexLib})
  public void testUsingUrlForOtherLibrary() throws Exception {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testStatesProblem() throws Exception {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testProblemWithInstanceVarFromStaticMethod() throws Exception {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testSpellChecker() throws Exception {
    enableInspectionTool(new SpellCheckingInspection());
    configureByFile(getBasePath() + "/" + getTestName(false) + ".mxml");
    ExpectedHighlightingData expectedHighlightingData = new ExpectedHighlightingData(myEditor.getDocument(), true, true, false, myFile);
    Collection<HighlightInfo> infoCollection = checkHighlighting(expectedHighlightingData);
    assertEquals(1, countNonInformationHighlights(infoCollection));
    findAndInvokeActionWithExpectedCheck(RenameTo.FIX_NAME, "mxml", infoCollection);
  }

  @NeedsJavaModule
  public void testUsingUrlForOtherLibrary2() throws Exception {
    defaultTest();
    doTestFor(true, getTestName(false) + "_2.mxml");
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testObjectCanHaveAnyAttr() throws Exception {
    defaultTest();
  }

  @Override
  protected Collection<HighlightInfo> defaultTest() {
    return doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testSomeFeedbackFromAdobe() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithGumboSdk, JSTestOption.WithFlexFacet})
  public void testSomeFeedbackFromAdobe_2() throws Exception {
    String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + "_2.as");
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testBindingDestinationCanHaveThisModifier() throws Exception {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testSomeFeedbackFromAdobe2() throws Exception {
    String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", StringUtil.decapitalize(testName) + "_2.mxml");
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testResourceReferences() throws Exception {
    Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".mxml", "mybundle.properties", "mybundle3.properties");
    findAndInvokeIntentionAction(infoCollection, PropertiesBundle.message("create.property.quickfix.text"), myEditor, myFile);

    List<HighlightInfo> infoList = filterUnwantedInfos(doHighlighting(), this);
    assertEquals(infoCollection.size() - 1, infoList.size());
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testResourceReferences2() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml", "mybundle.properties", "mybundle3.properties");
  }

  @JSTestOptions(JSTestOption.WithFlexFacet)
  public void testResourceReferencesWithPackages() throws Exception {
    String testName = getTestName(false);
    doTestFor(true, getDefaultProjectRoot(testName), (Runnable)null,
              testName + "/" + testName + ".mxml",
              testName + "/resourcepackage/mybundle4.properties");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithJsSupportLoader})
  public void testResolveWithInclude() throws Exception {
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

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk, JSTestOption.WithLineMarkers})
  public void testOverridingMarkersXmlBacked() throws Exception {
    doTestFor(true, getTestName(false) + "_B.mxml", getTestName(false) + "_A.mxml", getTestName(false) + "_C.mxml",
              getTestName(false) + "_D.as", getTestName(false) + "_MyInterface.as");
    int offset = myEditor.getCaretModel().getOffset();
    PsiElement source = InjectedLanguageUtil.findElementAtNoCommit(myFile, offset);
    source = PsiTreeUtil.getParentOfType(source, JSFunction.class);
    PsiElement[] functions = new ImplementationSearcher().searchImplementations(source, myEditor, true, true);
    assertEquals(3, functions.length);
    Collection<String> classNames = new ArrayList<>();
    for (PsiElement function : functions) {
      assertEquals("foo", ((JSFunction)function).getName());
      PsiElement clazz = function.getParent();
      if (clazz instanceof JSFile) {
        clazz = JSResolveUtil.getXmlBackedClass((JSFile)clazz);
      }
      classNames.add(((JSClass)clazz).getName());
    }
    assertTrue(classNames.contains(getTestName(false) + "_B"));
    assertTrue(classNames.contains(getTestName(false) + "_C"));
    assertTrue(classNames.contains(getTestName(false) + "_D"));
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk, JSTestOption.WithLineMarkers})
  public void testOverridingMarkersXmlBacked2() throws Exception {
    doTestFor(true, getTestName(false) + "_MyInterface.as", getTestName(false) + "_A.mxml", getTestName(false) + "_B.mxml");
    int offset = myEditor.getCaretModel().getOffset();
    PsiElement source = myFile.findElementAt(offset);
    source = PsiTreeUtil.getParentOfType(source, JSFunction.class);
    PsiElement[] functions = new ImplementationSearcher().searchImplementations(source, myEditor, true, true);
    assertEquals(2, functions.length);
    //assertEquals(3, functions.length); IDEADEV-34319
    Collection<String> classNames = new ArrayList<>();
    for (PsiElement function : functions) {
      assertEquals("bar", ((JSFunction)function).getName());
      PsiElement clazz = function.getParent();
      if (clazz instanceof JSFile) {
        clazz = JSResolveUtil.getXmlBackedClass((JSFile)clazz);
      }
      classNames.add(((JSClass)clazz).getName());
    }
    assertTrue(classNames.contains(getTestName(false) + "_MyInterface"));
    assertTrue(classNames.contains(getTestName(false) + "_A"));
    //assertTrue(classNames.contains(getTestName(false) +"_B")); IDEADEV-34319
  }

  //@JSTestOptions({WithJsSupportLoader})
  //public void testImportScopeOverriddenByBaseClass() throws Exception {
  //  doTestFor(true, getTestName(false) +".as", getTestName(false) +"_2.as", getTestName(false) +"_3.as");
  //}

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithUnusedImports})
  public void testFqnUsingStar() throws Exception {
    doTestFor(true, getTestName(false) + ".as", getTestName(false) + "_2.as", getTestName(false) + "_3.as");
  }

  @JSTestOptions({JSTestOption.WithGumboSdk})
  public void testNamespacesAndManifestFiles() throws Exception {
    final String name = getTestName(false);

    FlexTestUtils.modifyBuildConfiguration(myModule, bc -> {
      final String manifest = getTestDataPath() + "/" + getBasePath() + "/" + name + "_manifest.xml";
      bc.getCompilerOptions().setAllOptions(Collections.singletonMap("compiler.namespaces.namespace", "http://MyNamespace\t" + manifest));
    });

    doTestFor(true, name + ".mxml", name + "_other.mxml", name + "_other2.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testNamespacesAndManifestsFromCustomConfigFile() throws Exception {
    final String testName = getTestName(false);

    FlexTestUtils.modifyBuildConfiguration(myModule, bc -> {
      final String path = getTestDataPath() + "/" + getBasePath() + "/" + testName + "_custom_config.xml";
      bc.getCompilerOptions().setAdditionalConfigFilePath(path);
    });

    doTestFor(true, testName + ".mxml", testName + "_other.as");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testManifestClassHighlighting() throws Exception {
    doTestFor(true, getTestName(false) + ".xml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testNamespacesFromSdkSwcs() throws Exception {
    final String name = getTestName(false);

    final SdkModificator sdkModificator = FlexTestUtils.getFlexSdkModificator(getModule());
    final VirtualFile swcFile = LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + "/" + getBasePath() + "/" + name + ".swc");
    sdkModificator.addRoot(JarFileSystem.getInstance().getJarRootForLocalFile(swcFile), OrderRootType.CLASSES);
    sdkModificator.commitChanges();

    doTestFor(true, name + ".mxml");
  }

  @JSTestOptions(
    {JSTestOption.WithFlexSdk, JSTestOption.WithJsSupportLoader, JSTestOption.WithUnusedImports})
  public void testPredefinedFunReference() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    doTestFor(true, getTestName(false) + ".mxml", "getFoo.swc");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk, JSTestOption.WithLineMarkers})
  public void testOverridingMarkersInlineComponents() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml");

    int offset = myEditor.getCaretModel().getOffset();
    PsiElement source = InjectedLanguageUtil.findElementAtNoCommit(myFile, offset);
    source = PsiTreeUtil.getParentOfType(source, JSFunction.class);
    Collection<JSClass> classes = JSInheritanceUtil.findDeclaringClasses((JSFunction)source);
    assertEquals(1, classes.size());
    assertEquals("mx.core.UIComponent", classes.iterator().next().getQualifiedName());
    JSFunction baseFunction = classes.iterator().next().findFunctionByName(((JSFunction)source).getName());
    Collection<JSFunction> implementations = JSFunctionsSearch.searchOverridingFunctions(baseFunction, true).findAll();
    assertEquals(2, implementations.size());
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testInlineComponentsClassName() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testInlineComponentsOuterFieldAccess() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testResolveTypeToComponent() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testInheritanceCheck() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml", getTestName(false) + "_2.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testAssets() throws Throwable {
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
      final String newContent = StringUtil.convertLineSeparators(new String(file.contentsToByteArray())).replace(oldText, newText);
      createEditor(file).getDocument().setText(newContent);
      PsiDocumentManager.getInstance(myProject).commitAllDocuments();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testMxScriptAndStyleSource() throws Throwable {
    final String testName = getTestName(false);
    final String fileRelPath = "pack/" + testName + ".mxml";

    final String absPath = LocalFileSystem.getInstance().findFileByPath("").getPath();
    myAfterCommitRunnable = () -> {
      FlexTestUtils.addLibrary(myModule, "lib", getTestDataPath() + getBasePath() + "/", testName + "/" + testName + ".swc", null, null);
      replaceText(fileRelPath, "${SOME_ABSOLUTE_PATH}", absPath);
    };

    doTestFor(true, getDefaultProjectRoot(testName), (Runnable)null, testName + "/" + fileRelPath);
  }

  @JSTestOptions({})
  public void testExtendMultipleClasses() throws Throwable {
    doTestFor(true, getTestName(false) + ".as");
  }

  @JSTestOptions({})
  public void testExtendFinalClass() throws Throwable {
    doTestFor(true, getTestName(false) + ".as");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testClassAndPackageNestedInMxml() throws Throwable {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithLineMarkers})
  public void testOverriddenMarkersInMxml1() throws Throwable {
    doTestFor(true, getTestName(false) + ".mxml", getTestName(false) + "_2.as");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithLineMarkers})
  public void testOverriddenMarkersInMxml2() throws Throwable {
    doTestFor(true, getTestName(false) + ".mxml", getTestName(false) + "_2.as");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testHttpService() throws Throwable {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testNamespace() throws Throwable {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
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

  @JSTestOptions()
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

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testIgnoreClassesFromOnlyLibSources() throws Exception {
    final String testName = getTestName(false);

    myAfterCommitRunnable = () -> FlexTestUtils
      .addLibrary(myModule, "library", getTestDataPath() + getBasePath() + "/", testName + "/empty.swc", testName + "/LibSources.zip",
                  null);

    doTestFor(true, testName + "/" + testName + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testIgnoreClassesFromOnlySdkSources() throws Exception {
    final String testName = getTestName(false);

    final VirtualFile srcFile =
      LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + getBasePath() + "/" + testName + "/sdk_src/");

    final SdkModificator modificator = FlexTestUtils.getFlexSdkModificator(myModule);
    modificator.addRoot(srcFile, OrderRootType.SOURCES);
    modificator.commitChanges();

    doTestFor(true, testName + "/" + testName + ".mxml");
  }

  public void testComponentsFromLibsResolvedWhenSdkNotSet() throws Exception {
    FlexTestUtils.addFlexLibrary(false, myModule, "lib", true, getTestDataPath() + getBasePath(), getTestName(false) + ".swc", null, null);
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithLineMarkers})
  public void testImplicitImplementMarker() throws Exception {
    doTestFor(true, getTestName(false) + "Interface.as", getTestName(false) + "Base.mxml", getTestName(false) + ".mxml");
  }

  public void testIntroduceFieldAmbiguous() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult("Create Field 'v'", "as", getTestName(false) + ".as", getTestName(false) + "_2.as");
  }

  public void testAddTypeToDeclarationAmbiguous() throws Exception {
    enableInspectionTool(new JSUntypedDeclarationInspection());
    doHighlightingWithInvokeFixAndCheckResult("Add Type to Declaration", "as", getTestName(false) + ".as", getTestName(false) + "_2.as");
  }

  public void testCreateFunctionAmbiguous() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult("Create Method 'foo'", "as", getTestName(false) + ".as", getTestName(false) + "_2.as");
  }

  public void testImplementMethodsAmbiguous() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult("Implement Methods", "as", getTestName(false) + ".as", getTestName(false) + "_2.as",
                                              getTestName(false) + "_3.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testFlex4Namespaces() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml", getTestName(false) + "_other.as");
  }

  public void testQualifiedConstructorStillNeedsImport() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult("com.foo.MyClass?", "as", getTestName(false) + ".as",
                                              getTestName(false) + "_2.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testConditionalCompilationDefinitions() throws Exception {
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

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testPredefinedTagsInsideRootTagWithDefaultProperty() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testPredefinedTagsInsideNotRootTagWithDefaultProperty() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testXmlRelatedTags() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  public void testNamespaceInInterface() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult("Remove namespace reference", "as", getTestName(false) + ".as", "MyNs.as");
  }

  @JSTestOptions({JSTestOption.WithGumboSdk})
  public void testStaticSettersCanNotBeAttributes() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml", getTestName(false) + "_other.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testFxComponent() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testNoResolveToJsVarOrFun() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml", getTestName(false) + ".js");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testFxPrivate() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml");

    final JSClassResolver resolver =
      JSDialectSpecificHandlersFactory.forLanguage(JavaScriptSupportLoader.ECMA_SCRIPT_L4).getClassResolver();
    final JSClass jsClass =
      (JSClass)resolver.findClassByQName("mx.controls.CheckBox", GlobalSearchScope.moduleWithLibrariesScope(myModule));
    final Collection<PsiReference> usages = ReferencesSearch.search(jsClass, GlobalSearchScope.moduleScope(myModule)).findAll();
    assertEquals(1, usages.size());
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testModelTag() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testInheritDefaultProperty() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml", getTestName(false) + "_other.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testFxLibraryAndFxDefinition() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testSameShortDifferentFullClassNames() throws Exception {
    // example taken from IDEA-52241
    doTestFor(true, getTestName(false) + ".mxml", getTestName(false) + "_other.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testPackagesAsNamespaces() throws Exception {
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

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testComponentIdInsideRepeaterResolvedToArray() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml");

    PsiElement element = InjectedLanguageManager.getInstance(myProject).findInjectedElementAt(myFile, myEditor.getCaretModel().getOffset());
    JSExpression expression = PsiTreeUtil.getParentOfType(element, JSExpression.class);
    assertEquals("mx.containers.Panel[]", JSResolveUtil.getQualifiedExpressionType(expression, myFile));
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public final void testSuggestCorrectMxNamespace() throws Exception {
    doTestIntention(getTestName(false), "mxml", "Create namespace declaration");
  }

  @JSTestOptions({JSTestOption.WithGumboSdk})
  public final void testUserNamespacesWithoutScheme() throws Exception {
    final String name = getTestName(false);

    final String manifest = getTestDataPath() + "/" + getBasePath() + "/" + name + "_manifest.xml";
    FlexBuildConfigurationManager.getInstance(myModule).getModuleLevelCompilerOptions()
      .setAllOptions(Collections.singletonMap("compiler.namespaces.namespace", "SomeNamespace1\t" + manifest));

    doTestFor(true, name + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public final void testFlex3AllowsNonVisualComponentsOnlyUnderRootTag() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + "_other1.mxml", testName + "_other2.mxml", testName + "_other3.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public final void testFlex4AllowsNonVisualComponentsOnlyUnderDeclarationsTag() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testImplicitImport() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk, JSTestOption.WithUnusedImports})
  public void testFlex3ImplicitImport() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk, JSTestOption.WithUnusedImports})
  public void testFlex4ImplicitImport() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public final void testDoNotForgetSdkComponents() throws Exception {
    final String testName = getTestName(false);
    myAfterCommitRunnable = () -> FlexTestUtils
      .addLibrary(myModule, "library", getTestDataPath() + getBasePath() + "/", testName + ".swc", null, null);
    doTestFor(true, testName + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testPropertiesSpecifiedByMxmlIdAttributes() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + "_other.mxml", testName + "_otherSuper.mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testFxg() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + "_other.fxg");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testWebAndHttpServiceRequestTagContent() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testFxReparent() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testStaticBlock() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testLanguageNamespacesMixed() throws Exception {
    final String testName = getTestName(false);
    final Collection<HighlightInfo> infoCollection = doTestFor(true, testName + ".mxml");
    findAndInvokeIntentionAction(infoCollection, "Remove Namespace Declaration", myEditor, myFile);
    findAndInvokeIntentionAction(doHighlighting(), "Remove Namespace Declaration", myEditor, myFile);
    assertNull(findIntentionAction(doHighlighting(), "Remove Namespace Declaration", myEditor, myFile));
    checkResultByFile(BASE_PATH + "/" + testName + "_after.mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testLanguageNamespaceAbsent() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult("Declare Namespace http://ns.adobe.com/mxml/2009", "mxml", getTestName(false) + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testMonkeyPatching() throws Exception {
    myAfterCommitRunnable = () -> FlexTestUtils
      .addLibrary(myModule, "library", getTestDataPath() + getBasePath() + "/", getTestName(false) + ".swc", null, null);
    doTestFor(true, getTestName(false) + ".mxml", getTestName(false) + "_2.as");
  }

  public void testNotResolveTestClasses() throws Exception {
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

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testNoReferenceToAnyContentTags() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testUnusedNamespaces() throws Exception {
    enableInspectionTool(new XmlUnusedNamespaceInspection());
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testMxmlRootTagLeadsToFinalClass() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
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

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
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

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
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

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testCreateClassByAttributeValue() throws Exception {
    JSTestUtils.disableFileHeadersInTemplates(getProject());
    final String testName = getTestName(false);
    doHighlightingWithInvokeFixAndCheckResult(JSBundle.message("javascript.create.class.intention.name", "Foo"), "mxml");
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

  @JSTestOptions({JSTestOption.WithFlexFacet})
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

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testResolveToClassWithBiggestTimestamp() throws Exception {
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

  @JSTestOptions({JSTestOption.WithGumboSdk})
  public void testResolveToClassWithBiggestTimestamp2() throws Exception {
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

  /*@JSTestOptions({JSTestOption.WithoutWarnings, JSTestOption.WithFlexSdk})
  public void testSqlInjection() throws Exception {
    doTestFor(false, getTestName(false) + ".mxml");
  }*/

  @JSTestOptions({JSTestOption.WithoutWarnings, JSTestOption.WithFlexSdk})
  public void testSqlInjection1() throws Exception {
    doTestFor(false, getTestName(false) + ".as");
  }

  private void testCreateSubclass(boolean subclass) throws Exception {
    JSTestUtils.disableFileHeadersInTemplates(getProject());
    final String testName = getTestName(false);
    findAndInvokeIntentionAction(doTestFor(true, testName + ".as"), subclass ? "Create subclass" : "Implement interface", myEditor, myFile);

    VirtualFile subclassFile = myFile.getVirtualFile().getParent().findFileByRelativePath("foo/" + testName + "Impl.as");
    myEditor = FileEditorManager.getInstance(myProject).openTextEditor(new OpenFileDescriptor(myProject, subclassFile), true);
    myFile = PsiDocumentManager.getInstance(myProject).getPsiFile(myEditor.getDocument());
    checkResultByFile(getBasePath() + "/" + testName + (subclass ? "_subclass.as" : "_implementation.as"));
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testCreateSubclassIntention() throws Exception {
    testCreateSubclass(true);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testCreateSubclassIntention2() throws Exception {
    testCreateSubclass(true);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testCreateSubclassIntention3() throws Exception {
    testCreateSubclass(true);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testImplementInterfaceIntention() throws Exception {
    testCreateSubclass(false);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testLanguageTagsInInlineRenderer() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testChangeSignatureForEventHandler() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change Foo.bar() signature", getTestName(false) + ".as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testChangeSignatureForEventHandler_2() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change Foo.bar() signature", getTestName(false) + ".as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testChangeSignatureForEventHandler2() throws Exception {
    doSimpleHighlightingWithInvokeFixAndCheckResult("Change Foo.<anonymous>() signature", getTestName(false) + ".as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testReferenceInlineComponentId() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testMxmlConstructor() throws Exception {
    enableInspectionTool(new JSUnusedGlobalSymbolsInspection());
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testIdAndPredefinedAttributes() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml", getTestName(false) + "_other.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testInlineComponentsImports() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult("mypackage.List?", "mxml", getTestName(false) + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testImportForNeighbourClass() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml", getTestName(false) + "_2.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  @SuppressWarnings({"ConstantConditions"})
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

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  @SuppressWarnings({"ConstantConditions"})
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

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testMxmlAttrPreferResolveToEvent() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + "_2.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testBindingType() throws Exception {
    final String testName = getTestName(false);
    Collection<HighlightInfo> highlightInfos = doTestFor(true, (Runnable)null, testName + ".mxml");
    findAndInvokeIntentionAction(highlightInfos, "Create Method 'getMsg'", myEditor, myFile);
    checkResultByFile(BASE_PATH + "/" + testName + "_after.mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testBindingType2() throws Exception {
    final String testName = getTestName(false);
    Collection<HighlightInfo> highlightInfos = doTestFor(true, (Runnable)null, testName + ".mxml");
    findAndInvokeIntentionAction(highlightInfos, "Create Field 'label'", myEditor, myFile);
    checkResultByFile(BASE_PATH + "/" + testName + "_after.mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testCreateClassFromMetadataAttr() throws Exception {
    JSTestUtils.disableFileHeadersInTemplates(getProject());
    final String testName = getTestName(false);
    findAndInvokeIntentionAction(doTestFor(true, testName + ".as"), JSBundle.message("javascript.create.class.intention.name", "Baz"), myEditor, myFile);
    assertEmpty(filterUnwantedInfos(doHighlighting(), this));
    final VirtualFile createdFile = VfsUtilCore.findRelativeFile("foo/Baz.as", myFile.getVirtualFile().getParent());
    myEditor = FileEditorManager.getInstance(myProject).openTextEditor(new OpenFileDescriptor(myProject, createdFile), true);
    myFile = PsiDocumentManager.getInstance(myProject).getPsiFile(myEditor.getDocument());
    checkResultByFile(getBasePath() + "/" + testName + "_created.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testCreateEventClassFromMetadataAttr() throws Exception {
    JSTestUtils.disableFileHeadersInTemplates(getProject());
    final String testName = getTestName(false);
    findAndInvokeIntentionAction(doTestFor(true, testName + ".as"),
                                 JSBundle.message("javascript.create.class.intention.name", "MyEvent"), myEditor, myFile);
    assertEmpty(filterUnwantedInfos(doHighlighting(), this));
    final VirtualFile createdFile = VfsUtilCore.findRelativeFile("foo/MyEvent.as", myFile.getVirtualFile().getParent());
    myEditor = FileEditorManager.getInstance(myProject).openTextEditor(new OpenFileDescriptor(myProject, createdFile), true);
    myFile = PsiDocumentManager.getInstance(myProject).getPsiFile(myEditor.getDocument());
    checkResultByFile(getBasePath() + "/" + testName + "_created.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testImport() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult("mx.rpc.http.mxml.HTTPService?", "mxml", getTestName(false) + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public final void testMobileApplicationChildren() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions(JSTestOption.WithFlexFacet)
  public final void testInternalPropertiesInMxml() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, getDefaultProjectRoot(testName), (Runnable)null, testName + "/pack/" + testName + ".mxml",
              testName + "/InRootPackage.mxml", testName + "/Child.as", testName + "/pack/ParentInPack.as", testName + "/pack/InPack.mxml");
  }

  @JSTestOptions(JSTestOption.WithFlexFacet)
  public void testRequireImportInParamList() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithUnusedImports})
  public void testOptimizeImportInParamList() throws Exception {
    doTestFor(true, getTestName(false) + ".mxml");
    invokeNamedActionWithExpectedFileCheck(getTestName(false), "OptimizeImports", "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testHostComponentImplicitField() throws Exception {
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

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testCreateFieldByMxmlAttribute() throws Exception {
    doTestQuickFixForRedMxmlAttribute("Create Field 'foo'", "as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testCreateFieldByMxmlAttribute2() throws Exception {
    doTestQuickFixForRedMxmlAttribute("Create Field 'foo'", "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testCreateSetterByMxmlAttribute() throws Exception {
    doTestQuickFixForRedMxmlAttribute("Create Set Property 'foo'", "as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testCreateSetterByMxmlAttribute2() throws Exception {
    doTestQuickFixForRedMxmlAttribute("Create Set Property 'foo'", "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testDeclareEventByMxmlAttribute() throws Exception {
    doTestQuickFixForRedMxmlAttribute("Declare Event 'foo'", "as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testDeclareEventByMxmlAttribute2() throws Exception {
    doTestQuickFixForRedMxmlAttribute("Declare Event 'foo'", "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
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

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testGenerateTestMethod() throws Exception {
    useTestSourceRoot();
    doGenerateTest("Generate.TestMethod.Actionscript", "as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit1})
  public void testGenerateSetUpMethod() throws Exception {
    useTestSourceRoot();
    doGenerateTest("Generate.SetUp.Actionscript", "as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testGenerateTearDownMethod() throws Exception {
    useTestSourceRoot();
    doGenerateTest("Generate.TearDown.Actionscript", "as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testGenerateTestClass() throws Exception {
    JSTestUtils.disableFileHeadersInTemplates(getProject());
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

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testSpacesInGenericVector() throws Exception {
    doTestFor(true, getTestName(false) + ".as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testStyleValues() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml", testName + "_other.as");
  }

  @JSTestOptions({JSTestOption.WithGumboSdk})
  public void testOutOfSourceRoot() throws Exception {
    myAfterCommitRunnable = () -> {
      final ModifiableRootModel model = ModuleRootManager.getInstance(myModule).getModifiableModel();
      final ContentEntry contentEntry = model.getContentEntries()[0];
      contentEntry.clearSourceFolders();
      model.commit();
    };
    final String testName = getTestName(false);
    doTestFor(true, testName + ".mxml");
  }

  @JSTestOptions(JSTestOption.WithFlexFacet)
  public void testMakeMethodStatic() throws Exception {
    enableInspectionTool(new JSMethodCanBeStaticInspection());
    doHighlightingWithInvokeFixAndCheckResult("Make 'static'", "mxml", getTestName(false) + ".mxml");
  }

  @JSTestOptions(JSTestOption.WithFlexFacet)
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
    VirtualFile expectedFile = getVirtualFile(BASE_PATH + "/" + testName + "_2_after.as");
    assertEquals(StringUtil.convertLineSeparators(VfsUtilCore.loadText(expectedFile)), StringUtil.convertLineSeparators(
      VfsUtilCore.loadText(f)));
  }

  public void testBadResolveOfSuperClass() throws Exception {
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

  public void testAddLeadingSlashForEmbeddedAsset() throws Exception {
    final String testName = getTestName(false);
    final Collection<HighlightInfo> infoCollection =
      doTestFor(true, new File(getTestDataPath() + BASE_PATH + "/" + testName), (Runnable)null,
                testName + "/pack/" + testName + ".as",
                testName + "/assets/foo.txt");
    findAndInvokeIntentionAction(infoCollection, "Add leading slash", myEditor, myFile);
  }

  @JSTestOptions(JSTestOption.WithGumboSdk)
  public void testUseOfTestClass() throws Exception {
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

  @JSTestOptions(JSTestOption.WithGumboSdk)
  public void testReportAccessorProblems() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithGumboSdk, JSTestOption.WithFlexLib})
  public void testGuessFrameworkNamespace() throws Throwable {
    doTestIntention(getTestName(false), "mxml", "Create namespace declaration");
  }

  public void testFontFaceProperties() throws Throwable {
    final Sdk sdk = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.5"), null, true, getTestRootDisposable());
    WriteAction.run(() -> FlexTestUtils.modifyBuildConfiguration(myModule, bc -> FlexTestUtils.setSdk(bc, sdk)));

    enableInspectionTool(new CssUnknownPropertyInspection());
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithGumboSdk})
  public void testUnresolvedClassReference() throws Throwable {
    enableInspectionTool(new CssInvalidElementInspection());
    JSTestUtils.disableFileHeadersInTemplates(getProject());
    final String testName = getTestName(false);
    doHighlightingWithInvokeFixAndCheckResult(JSBundle.message("javascript.create.class.intention.name", "MyZuperClass"), "mxml");

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

  @JSTestOptions({JSTestOption.WithGumboSdk})
  public void testUnresolvedClassReference2() throws Throwable {
    enableInspectionTool(new CssInvalidElementInspection());
    JSTestUtils.disableFileHeadersInTemplates(getProject());
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

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testTypeOfMethodNamedCall() throws Exception {
    doTestFor(true, getTestName(false) + ".as");
  }

  @JSTestOptions({JSTestOption.WithGumboSdk})
  public void testStateGroups() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithGumboSdk})
  public void testStarlingEvent() throws Exception {
    doTestFor(true, getTestName(false) + ".as");
  }

  @JSTestOptions({JSTestOption.WithGumboSdk})
  public void testImplicitCoercion() throws Exception {
    doTestFor(true, getTestName(false) + ".as");
  }

  public void testNewObjectClassFunction() throws Exception {
    doTestFor(true, getTestName(false) + ".as");
  }

  @JSTestOptions({JSTestOption.WithGumboSdk})
  public void testDoNotResolveNotImportedSuperClass() throws Exception {
    doTestFor(true, getTestName(false) + ".as", getTestName(false) + "_other.as");
  }

  @JSTestOptions({JSTestOption.WithGumboSdk})
  public void testCanBeLocalInMxml() throws Exception {
    enableInspectionTool(new JSFieldCanBeLocalInspection());
    doTestFor(true, getTestName(false) + ".mxml", getTestName(false) + "_other.as");
  }

  public void testPrivateMemberAccessibleWithinFile() throws Exception {
    doTestFor(true, getTestName(false) + ".as", getTestName(false) + "_other.as");
  }

  @JSTestOptions({JSTestOption.WithGumboSdk})
  public void testPercentProxyInOverridden() throws Exception {
    defaultTest();
  }

  public void testCreateMethodAfterCallExpression() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult("Create Method 'bar'", "as");
  }
}
