package com.intellij.flex.completion;

import com.intellij.codeInsight.completion.impl.CamelHumpMatcher;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.injected.editor.EditorWindow;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.javascript.flex.mxml.schema.AnnotationBackedDescriptorImpl;
import com.intellij.javascript.flex.mxml.schema.FlexSchemaHandler;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.javascript.BaseJSCompletionTestCase;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.ReferenceSupport;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexBuildConfiguration;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProfilingUtil;
import com.intellij.util.ThrowableRunnable;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

public class FlexCompletionTest extends BaseJSCompletionTestCase {
  static final String BASE_PATH = "/flex_completion/";

  @NonNls private static final String MXML_EXTENSION = "mxml";

  protected Runnable myAfterCommitRunnable = null;

  protected Runnable myCompletionPerformer = null;

  {
    ContainerUtil.addAll(myTestsWithSaveAndLoadCaches, "CompletionInMxml");

    myTestsWithJSSupportLoader.addAll(
      Arrays.asList("CompletionInMxml5", "EnumeratedCompletionInMxml", "CompleteAfterThisInMxml", "CompleteAfterThisInMxml2",
                    "CompleteAfterThisInMxml3"));

    mySmartCompletionTests.addAll(
      Arrays.asList("CompletionInMxml2", "CompletionInMxml3", "PickupArrayElementType", "PickupArrayElementType2",
                    "PickupArrayElementType3", "CompleteAfterThisInMxml", "BindingCompletion"));
  }

  @Override
  protected String getBasePath() {
    return BASE_PATH;
  }

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
    CamelHumpMatcher.forceStartMatching(getTestRootDisposable());
    myAfterCommitRunnable = null;

    myCompletionPerformer = () -> super.complete();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD})
  public @interface NeedsJavaModule {
  }

  private boolean needsJavaModule() {
    return JSTestUtils.getTestMethod(getClass(), getTestName(false)).getAnnotation(NeedsJavaModule.class) != null;
  }

  protected void setUpJdk() {
    if (!needsJavaModule()) {
      FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass());
    }
  }

  protected ModuleType getModuleType() {
    return needsJavaModule() ? StdModuleTypes.JAVA : FlexModuleType.getInstance();
  }

  @Override
  protected void complete() {
    if (myCompletionPerformer != null) {
      myCompletionPerformer.run();
    }
  }


  @Override
  protected void doCommitModel(@NotNull ModifiableRootModel rootModel) {
    super.doCommitModel(rootModel);
    FlexTestUtils.setupFlexLib(getProject(), getClass(), getTestName(false));
    if (myAfterCommitRunnable != null) {
      myAfterCommitRunnable.run();
    }
  }


  private Runnable createMultiCompletionPerformerWithVariantsCheck() {
    return createMultiCompletionPerformerWithVariantsCheck(true);
  }

  private Runnable createMultiCompletionPerformerWithVariantsCheck(final boolean strict) {
    return () -> {
      final LinkedHashMap<Integer, String> map = JSTestUtils.extractPositionMarkers(myProject, getEditor().getDocument());
      for (Map.Entry<Integer, String> entry : map.entrySet()) {
        myItems = null;
        getEditor().getCaretModel().moveToOffset(entry.getKey());

        Editor savedEditor = myEditor;
        PsiFile savedFile = myFile;

        PsiFile injectedPsi = InjectedLanguageUtil.findInjectedPsiNoCommit(myFile, myEditor.getCaretModel().getOffset());
        if (injectedPsi != null) {
          myEditor = InjectedLanguageUtil.getEditorForInjectedLanguageNoCommit(myEditor, myFile);
          myFile = injectedPsi;
        }

        super.complete();

        myEditor = savedEditor;
        myFile = savedFile;

        final String[] expected = entry.getValue().length() == 0 ? new String[]{} : entry.getValue().split(",");

        final String[] variants = new String[myItems == null ? 0 : myItems.length];
        if (myItems != null && myItems.length > 0) {
          for (int i = 0; i < myItems.length; i++) {
            variants[i] = myItems[i].getLookupString();
          }
        }

        if (strict || expected.length == 0) {
          assertSameElements(variants, expected);
        }
        else {
          for (final String variant : expected) {
            assertTrue("Missing from completion list: " + variant, ArrayUtil.contains(variant, variants));
          }
        }
      }
    };
  }

  @Override
  protected String getExtension() {
    return "as";
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testCompletionInMxml() throws Exception {
    defaultTest();
    //    doTest("_2", MXML_EXTENSION);
    //    doTest("_3", MXML_EXTENSION);
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public final void testCompleteInsertsQualifiedNameInItemRenderer() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public final void testSkinClassAsAttributeWithSpaces() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public final void testSkinClassAsSubTag() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public final void testCompleteInsertsQualifiedNameInEventType() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testSimpleCompleteInAddEventListener() throws Exception {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testCompletionInMxml2() throws Exception {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testCompletionInMxml3() throws Exception {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testCompleteBeforeVariable() throws Exception {
    doTest("", "as");
  }

  public void testCompleteBeforeVariable2() throws Exception {
    doTest("", "as");
    LookupElement[] elements = doTest("_2", "as");
    assertTrue(elements != null && elements.length > 0);
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet}, selectLookupItem = 0)
  public final void testCompletionInMxml4() throws Exception {
    defaultTest();
  }

  @Override
  protected LookupElement[] defaultTest() throws Exception {
    return doTest("", MXML_EXTENSION);
  }

  public final void testCompletionInMxml5() throws Exception {
    withNoAbsoluteReferences(() -> defaultTest());
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testCompletionInMxml6() throws Exception {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testCompletionInMxml7() throws Exception {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testCompletionInMxml8() throws Exception {
    LookupElement[] items = doTest("", MXML_EXTENSION);
    assertNotNull(items);
    assertTrue(items.length < 50);

    doTest("_3", MXML_EXTENSION);

    items = doTest("_2", MXML_EXTENSION);
    assertNotNull(items);
    for (LookupElement li : items) {
      assertTrue(!li.getLookupString().equals("arity"));
    }
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public final void testCompletionInMxml9() throws Exception {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public final void testCompletionInMxml10() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk}, selectLookupItem = 0)
  public void testMxmlColorAttributeValueCompletion1() throws Exception {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testMxmlColorAttributeValueCompletion2() throws Exception {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testMxmlColorAttributeValueCompletion3() throws Exception {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testMxmlColorAttributeValueCompletion4() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk}, selectLookupItem = 0)
  public void testMxmlColorAttributeValueCompletion5() throws Exception {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public final void testStateNameCompletion() throws Exception {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck();
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testCompletionOfMethodOfCustomComponent() throws Exception {
    final String testName = getTestName(false);

    final VirtualFile[] vFiles =
      new VirtualFile[]{getVirtualFile(getBasePath() + testName + ".mxml"), getVirtualFile(getBasePath() + testName + "_2.mxml")};
    doTestForFiles(vFiles, "", MXML_EXTENSION);
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testCompletionOfPackageLocalClass() throws Exception {
    final String testName = getTestName(false);

    doTestForFiles(new VirtualFile[]{getVirtualFile(BASE_PATH + testName + "/aaa/" + testName + ".mxml"),
                     getVirtualFile(BASE_PATH + testName + "/aaa/" + testName + ".as"),}, "", "mxml",
                   new File(getTestDataPath() + getBasePath() + File.separatorChar + testName));
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testCompletionOfMxmlInAnotherDirectory() throws Exception {
    final String testName = getTestName(false);

    doTestForFiles(new VirtualFile[]{getVirtualFile(BASE_PATH + testName + "/" + testName + ".as"),
                     getVirtualFile(BASE_PATH + testName + "/aaa/" + testName + ".mxml")}, "", "as",
                   new File(getTestDataPath() + getBasePath() + File.separatorChar + testName));
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public final void testCompleteAfterThisInMxml() throws Exception {
    defaultTest();
  }

  public final void testCompleteAfterThisInMxml2() throws Exception {
    defaultTest();
  }

  public final void testCompleteAfterThisInMxml3() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk, JSTestOption.WithSmartCompletion})
  public final void testSmartCompletionInMxml() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk, JSTestOption.WithSmartCompletion})
  public final void testSmartCompletionInMxml_2() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk, JSTestOption.WithSmartCompletion})
  public final void testSmartCompletionInMxml_3() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk, JSTestOption.WithSmartCompletion})
  public final void testSmartCompletionInMxml_4() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk, JSTestOption.WithSmartCompletion})
  public final void testSmartCompletionInMxml_5() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk, JSTestOption.WithSmartCompletion})
  public final void testSmartCompletionInMxml_6() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public final void testClassRefInSkinClass() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public final void testCompleteAnnotationParameter() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testCompleteWithoutQualifier() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader})
  @NeedsJavaModule
  public void testCompleteStyleNameInString() throws Exception {
    doTestForFiles(new VirtualFile[]{getVirtualFile(getBasePath() + getTestName(false) + ".mxml"),
      getVirtualFile(getBasePath() + getTestName(false) + ".as")}, "", MXML_EXTENSION);
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testReplaceChar() throws Exception {
    configureByFile(BASE_PATH + getTestName(false) + ".as");
    complete();
    assertNotNull(myItems);
    selectItem(myItems[0], Lookup.REPLACE_SELECT_CHAR);
    checkResultByFile(BASE_PATH + getTestName(false) + "_after.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public final void testEnumeratedCompletionInMxml() throws Exception {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testCustomComponentCompletionInMxml() throws Exception {
    final VirtualFile[] vFiles = new VirtualFile[]{getVirtualFile(getBasePath() + getTestName(false) + ".mxml"),
      getVirtualFile(getBasePath() + getTestName(false) + ".as")};
    doTestForFiles(vFiles, "", MXML_EXTENSION);
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet, JSTestOption.ClassNameCompletion})
  public final void testCustomComponentCompletionInMxml2() throws Exception {
    final VirtualFile[] vFiles = new VirtualFile[]{getVirtualFile(getBasePath() + getTestName(false) + ".mxml"),
      getVirtualFile(getBasePath() + getTestName(false) + ".as")};
    doTestForFiles(vFiles, "", MXML_EXTENSION);
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testCustomComponentCompletionInMxml6() throws Exception {
    final VirtualFile[] vFiles =
      new VirtualFile[]{getVirtualFile(getBasePath() + getTestName(false) + ".as"), getVirtualFile(getBasePath() + "MyComponent.mxml")};
    doTestForFiles(vFiles, "", "as");
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet, JSTestOption.ClassNameCompletion},
    selectLookupItem = 0)
  public final void testCustomComponentCompletionInMxml3() throws Exception {
    final VirtualFile[] vFiles = new VirtualFile[]{getVirtualFile(getBasePath() + getTestName(false) + ".mxml")};
    doTestForFiles(vFiles, "", MXML_EXTENSION);
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet, JSTestOption.ClassNameCompletion})
  public final void testCustomComponentCompletionInMxml4() throws Exception {
    final VirtualFile[] vFiles = new VirtualFile[]{getVirtualFile(getBasePath() + getTestName(false) + ".mxml"),};
    doTestForFiles(vFiles, "", MXML_EXTENSION);
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testCompleteResourceReferences() throws Exception {
    final VirtualFile[] vFiles =
      new VirtualFile[]{getVirtualFile(getBasePath() + getTestName(false) + ".mxml"), getVirtualFile(getBasePath() + "test.properties"),};
    doTestForFiles(vFiles, "", MXML_EXTENSION);
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testCustomComponentCompletionInMxml5() throws Exception {
    final VirtualFile[] vFiles = new VirtualFile[]{getVirtualFile(getBasePath() + getTestName(false) + ".mxml")};
    doTestForFiles(vFiles, "", MXML_EXTENSION);
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testCustomComponentCompletionInMxml7() throws Exception {
    final VirtualFile[] vFiles = new VirtualFile[]{getVirtualFile(getBasePath() + getTestName(false) + ".mxml"),
      getVirtualFile(getBasePath() + getTestName(false) + "_2.mxml")};
    doTestForFiles(vFiles, "", MXML_EXTENSION);
  }

  public final void testCompleteInFileName() throws Exception {
    withNoAbsoluteReferences(() -> doTest("", "as"));
  }

  public final void testNoComplete() throws Exception {
    defaultTest();
  }

  public final void testAs2Completion() throws Exception {
    doTest("", "as");
  }

  public final void testAs2Completion2() throws Exception {
    VirtualFile[] files =
      new VirtualFile[]{getVirtualFile(BASE_PATH + getTestName(false) + ".as"), getVirtualFile(BASE_PATH + getTestName(false) + "_2.as")};
    doTestForFiles(files);
  }

  public final void testAs2Completion3() throws Exception {
    VirtualFile[] files = new VirtualFile[]{getVirtualFile(BASE_PATH + getTestName(false) + ".as")};
    doTestForFiles(files);
  }

  @JSTestOptions(selectLookupItem = 0)
  public final void testAs2Completion4() throws Exception {
    VirtualFile[] files = new VirtualFile[]{getVirtualFile(BASE_PATH + getTestName(false) + ".as")};
    doTestForFiles(files);
  }

  public final void testAs2Completion5() throws Exception {
    VirtualFile[] files =
      new VirtualFile[]{getVirtualFile(BASE_PATH + getTestName(false) + ".as"), getVirtualFile(BASE_PATH + getTestName(false) + "_2.as"),};
    doTestForFiles(files);
  }

  public final void testAs2Completion5_3() throws Exception {
    VirtualFile[] files = new VirtualFile[]{getVirtualFile(BASE_PATH + getTestName(false) + ".as")};
    doTestForFiles(files, "", "as");
  }

  @JSTestOptions(
    {JSTestOption.WithLoadingAndSavingCaches})
  public final void testAs2Completion6() throws Exception {
    VirtualFile[] files = new VirtualFile[]{getVirtualFile(BASE_PATH + getTestName(false) + ".as")};
    doTestForFiles(files);
  }

  @JSTestOptions(
    {JSTestOption.WithLoadingAndSavingCaches})
  public final void testAs2Completion6_2() throws Exception {
    VirtualFile[] files;

    final String testName = getTestName(false);
    files = new VirtualFile[]{getVirtualFile(BASE_PATH + testName + ".as"),
      getVirtualFile(BASE_PATH + testName.substring(0, testName.length() - 2) + "_3.as")};
    doTestForFiles(files, "", "as");
  }

  public final void testPickupArrayElementType() throws Exception {
    VirtualFile[] files = new VirtualFile[]{getVirtualFile(BASE_PATH + getTestName(false) + ".as")};
    doTestForFiles(files);
  }

  public final void testPickupArrayElementType2() throws Exception {
    VirtualFile[] files = new VirtualFile[]{getVirtualFile(BASE_PATH + getTestName(false) + ".as")};
    doTestForFiles(files);
  }

  // Looks like tested functionality has never worked in run time, previously test passed because of a trick in base test class
  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public final void _testInsertImportForStaticField() throws Exception {
    final String testName = getTestName(false);
    VirtualFile[] files = new VirtualFile[]{getVirtualFile(BASE_PATH + testName + ".mxml"), getVirtualFile(BASE_PATH + testName + "_2.as")};
    doTestForFiles(files, "", "mxml");
  }

  // Looks like tested functionality has never worked in run time, previously test passed because of a trick in base test class
  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public final void _testInsertImportForStaticField_3() throws Exception {
    final String testName = getTestName(false);
    VirtualFile[] files = new VirtualFile[]{getVirtualFile(BASE_PATH + testName + ".mxml"),
      getVirtualFile(BASE_PATH + testName.substring(0, testName.length() - 2) + "_2.as")};
    doTestForFiles(files, "", "mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public final void testSuggestOnlyInterfaces() throws Exception {
    final String testName = getTestName(false);
    VirtualFile[] files = new VirtualFile[]{getVirtualFile(BASE_PATH + testName + ".mxml"), getVirtualFile(BASE_PATH + testName + "_2.as")};
    doTestForFiles(files, "", "mxml");
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk}, selectLookupItem = 0)
  public final void testSuggestOnlyDescendants() throws Exception {
    final String testName = getTestName(false);
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck(true);
    VirtualFile[] files = new VirtualFile[]{getVirtualFile(BASE_PATH + testName + ".mxml"), getVirtualFile(BASE_PATH + testName + "_2.as")};
    doTestForFiles(files, "", "mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public final void testSuggestOnlyDescendants2() throws Exception {
    final String testName = getTestName(false);
    VirtualFile[] files = new VirtualFile[]{getVirtualFile(BASE_PATH + testName + ".mxml"), getVirtualFile(BASE_PATH + testName + "_2.as")};
    doTestForFiles(files, "", "mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public final void testSuggestOnlyDescendants3() throws Exception {
    final String testName = getTestName(false);
    VirtualFile[] files = new VirtualFile[]{getVirtualFile(BASE_PATH + testName + ".mxml"), getVirtualFile(BASE_PATH + testName + "_2.as")};
    doTestForFiles(files, "", "mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public final void testSuggestOnlyDescendants4() throws Exception {
    final String testName = getTestName(false);
    VirtualFile[] files = new VirtualFile[]{getVirtualFile(BASE_PATH + testName + ".mxml"), getVirtualFile(BASE_PATH + testName + "_2.as")};
    doTestForFiles(files, "", "mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public final void testCompleteInComponent() throws Exception {
    final String testName = getTestName(false);
    VirtualFile[] files =
      new VirtualFile[]{getVirtualFile(BASE_PATH + testName + ".mxml"), getVirtualFile(BASE_PATH + testName + "_2.mxml")};
    doTestForFiles(files, "", "mxml");
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testRemoteObject() throws Exception {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testRemoteObject_2() throws Exception {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithFlexSdk, JSTestOption.WithJsSupportLoader})
  public void testCompleteOnlyPackages() throws Exception {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithFlexSdk, JSTestOption.WithJsSupportLoader})
  public void testCompleteOnlyPackages_2() throws Exception {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithFlexSdk, JSTestOption.WithJsSupportLoader})
  public void testCompleteOnlyPackages_3() throws Exception {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithFlexSdk, JSTestOption.WithJsSupportLoader})
  public void testCompleteOnlyPackages_4() throws Exception {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithFlexSdk, JSTestOption.WithJsSupportLoader})
  public void testCompleteOnlyPackages_5() throws Exception {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testInheritorCompletion() throws Exception {
    final String testName = getTestName(false);
    VirtualFile[] files = new VirtualFile[]{getVirtualFile(BASE_PATH + testName + ".mxml"), getVirtualFile(BASE_PATH + testName + ".as")};
    doTestForFiles(files, "", MXML_EXTENSION);
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testInheritorCompletion2() throws Exception {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testOnlyMembersCompletion() throws Exception {
    final VirtualFile[] vFiles = new VirtualFile[]{getVirtualFile(getBasePath() + getTestName(false) + ".mxml"),
      getVirtualFile(getBasePath() + getTestName(false) + ".as")};
    LookupElement[] elements = doTestForFiles(vFiles, "", "mxml");
    assertEquals(1, elements.length);
    assertEquals("tabs", elements[0].getLookupString());
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testOnlyMembersCompletion2() throws Exception {
    VirtualFile[] vFiles = new VirtualFile[]{getVirtualFile(getBasePath() + getTestName(false) + ".mxml"),
      getVirtualFile(getBasePath() + getTestName(false) + ".as")};
    LookupElement[] elements = doTestForFiles(vFiles, "", "mxml");
    assertNull(elements);
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testOnlyMembersCompletion2_2() throws Exception {
    VirtualFile[] vFiles = new VirtualFile[]{getVirtualFile(getBasePath() + getTestName(false) + ".mxml"),
      getVirtualFile(getBasePath() + "OnlyMembersCompletion2.as")};
    doTestForFiles(vFiles, "", "mxml");
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testCompleteBindingAttr() throws Exception {
    doTest("", "mxml");
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testImportedMember() throws Exception {
    final VirtualFile[] vFiles = new VirtualFile[]{getVirtualFile(getBasePath() + getTestName(false) + ".mxml"),
      getVirtualFile(getBasePath() + getTestName(false) + "_2.mxml"), getVirtualFile(getBasePath() + getTestName(false) + "_2_script.as")};
    doTestForFiles(vFiles, "", "mxml");
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testPackagesInCompilerConfig() throws Exception {
    doTest("", "xml");
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testClassesInCompilerConfig() throws Exception {
    doTest("", "xml");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testComponentFromManifestCompletion() throws Exception {
    final String name = getTestName(false);

    FlexTestUtils.modifyBuildConfiguration(myModule, bc -> {
      final String manifest = getTestDataPath() + "/" + getBasePath() + "/" + name + "_manifest.xml";
      bc.getCompilerOptions().setAllOptions(Collections.singletonMap("compiler.namespaces.namespace", "http://MyNamespace\t" + manifest));
    });

    VirtualFile[] files = new VirtualFile[]{getVirtualFile(BASE_PATH + name + ".mxml"), getVirtualFile(BASE_PATH + name + "_other.as")};
    doTestForFiles(files, "", MXML_EXTENSION);
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testComponentFromManifestCompletionWithNamespaceAutoInsert() throws Exception {
    final String name = getTestName(false);

    FlexTestUtils.modifyBuildConfiguration(myModule, bc -> {
      final String manifest = getTestDataPath() + "/" + getBasePath() + "/" + name + "_manifest.xml";
      bc.getCompilerOptions().setAllOptions(Collections.singletonMap("compiler.namespaces.namespace",
                                                                     "schema://www.MyNamespace.com/2010\t" + manifest));
    });

    final String testName = getTestName(false);
    VirtualFile[] files =
      new VirtualFile[]{getVirtualFile(BASE_PATH + testName + ".mxml"), getVirtualFile(BASE_PATH + testName + "_other.as")};
    doTestForFiles(files, "", MXML_EXTENSION);
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testInlineComponent() throws Exception {
    doTest("", "mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testNamedInlineComponent() throws Exception {
    doTest("", "mxml");
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk}, selectLookupItem = 0)
  public void testNamedInlineComponent2() throws Exception {
    doTest("", "mxml");
    LookupElement[] elements = doTest("", "mxml");
    assertEquals(3, elements.length);
    assertEquals("MyEditor3", elements[0].getLookupString());
    assertEquals("MyEditor", elements[1].getLookupString());
    assertEquals("MyEditor2", elements[2].getLookupString());
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testAssetFromAnotherSourceRoot() throws Exception {
    final String testName = getTestName(false);
    final VirtualFile secondSourceRoot = getVirtualFile(BASE_PATH + testName);
    PsiTestUtil.addSourceRoot(myModule, secondSourceRoot);
    withNoAbsoluteReferences(() -> doTest("", "mxml"));
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testCompleteStandardMxmlImport() throws Exception {
    doTest("", "mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testCompleteUIComponentInItemRenderer() throws Exception {
    doTest("", "mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testCompleteComponentsInUIComponent() throws Exception {
    doTest("", "mxml");
    doTest("_2", "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testResourceBundleFromSdk() throws Exception {
    final String testName = getTestName(false);

    final Sdk flexSdk = FlexUtils.getSdkForActiveBC(getModule());
    final SdkModificator sdkModificator = flexSdk.getSdkModificator();
    final VirtualFile swcFile =
      LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + "/" + getBasePath() + "/" + testName + ".swc");
    sdkModificator.addRoot(JarFileSystem.getInstance().getJarRootForLocalFile(swcFile), OrderRootType.CLASSES);
    sdkModificator.commitChanges();

    doTest("", "as");
    doTest("", "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testResourceBundleFromLib() throws Exception {
    myAfterCommitRunnable =
      () -> FlexTestUtils
        .addFlexLibrary(false, myModule, "Lib", false, getTestDataPath() + getBasePath(), getTestName(false) + ".swc", null,
                        null);
    doTest("", "as");
    doTest("", "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testIgnoreClassesFromOnlyLibSources() throws Exception {
    final String testName = getTestName(false);

    myAfterCommitRunnable = () -> FlexTestUtils
      .addLibrary(myModule, "library", getTestDataPath() + getBasePath() + "/", testName + "/empty.swc", testName + "/LibSources.zip",
                  null);

    assertNull(doTest("_1", "as"));
    assertNull(doTest("_2", "as"));
    assertNull(doTest("_3", "as"));
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testIgnoreClassesFromOnlySdkSources() throws Exception {
    final String testName = getTestName(false);

    final VirtualFile srcFile =
      LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + getBasePath() + "/" + testName + "_sdk_src/");

    final Sdk flexSdk = FlexUtils.getSdkForActiveBC(myModule);
    final SdkModificator modificator = flexSdk.getSdkModificator();
    modificator.addRoot(srcFile, OrderRootType.SOURCES);
    modificator.commitChanges();

    assertNull(doTest("_1", MXML_EXTENSION));
    assertNull(doTest("_2", MXML_EXTENSION));
    assertNull(doTest("_3", MXML_EXTENSION));
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testImportInsertPosition1() throws Exception {
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testImportInsertPosition2() throws Exception {
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testImportInsertPosition3() throws Exception {
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testImportInsertPosition4() throws Exception {
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testImportInsertPosition5() throws Exception {
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testImportInsertPosition6() throws Exception {
    mxmlTest();
  }

  private void mxmlTest() throws Exception {
    doTest("", MXML_EXTENSION);
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testImportInsertPosition7() throws Exception {
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testImportInsertPosition8() throws Exception {
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testImportInsertPosition9() throws Exception {
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testDoNotCompleteMembersInType() throws Exception {
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testDoNotCompleteMembersInType2() throws Exception {
    mxmlTest();
  }

  public void testDoNotCompleteMembersInType3() throws Exception {
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testTwoCompletions() throws Exception {
    myCompletionPerformer = () -> {
      super.complete();
      super.complete();
    };
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testObsoleteContext() throws Exception {
    myCompletionPerformer = () -> {
      final int offset = myEditor.getCaretModel().getOffset();

      super.complete();

      assertEquals("\n" + "    var v = myButtonOne;\n" + "  ", getEditor().getDocument().getText());

      replace("myButtonOne", "myButton", getEditor());
      replace("myButtonOne", "myButtonTwo", ((EditorWindow)myEditor).getDelegate());

      myEditor.getCaretModel().moveToOffset(offset);

      super.complete();
    };
    mxmlTest();
  }

  private static void replace(final String original, final String replacement, final Editor editor) {
    final int offset = editor.getDocument().getText().indexOf(original);
    assertTrue(offset != -1);
    ApplicationManager.getApplication()
      .runWriteAction(() -> editor.getDocument().replaceString(offset, offset + original.length(), replacement));

    PsiDocumentManager.getInstance(editor.getProject()).commitDocument(editor.getDocument());

    //getEditor().getCaretModel().moveToOffset(offset + original.length());
    //
    //for (int i = 0; i < original.length(); i++) {
    //  JSBaseEditorTestCase.performTypingAction(getEditor(), JSBaseEditorTestCase.BACKSPACE_FAKE_CHAR);
    //}
    //
    //for (int i = 0; i < replacement.length(); i++) {
    //  JSBaseEditorTestCase.performTypingAction(getEditor(), replacement.charAt(i));
    //}
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testConditionalCompilationConstantsInAs() throws Exception {
    FlexTestUtils.modifyBuildConfiguration(myModule, bc -> {
      bc.getCompilerOptions()
        .setAdditionalConfigFilePath(getTestDataPath() + "/" + getBasePath() + "/" + getTestName(false) + "_custom_config.xml");
      bc.getCompilerOptions().setAllOptions(Collections.singletonMap("compiler.define", ""));
    });
    // following is ignored because overridden at bc level
    FlexBuildConfigurationManager.getInstance(myModule).getModuleLevelCompilerOptions()
      .setAllOptions(Collections.singletonMap("compiler.define", "UNKNOWN::defined1\tfalse"));

    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck();
    doTest("", "as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testConditionalCompilationConstantsInMxml() throws Exception {
    FlexTestUtils.modifyBuildConfiguration(myModule, bc -> {
      bc.getCompilerOptions().setAllOptions(Collections.singletonMap("compiler.define", "CONFIG1::defined1\t\nCONFIG1::defined2\t-1"));
      bc.getCompilerOptions().setAdditionalOptions("-compiler.define=CONFIG2::defined3,true");
    });

    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck();
    doTest("", "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testPredefinedTagsInsideRootTagWithDefaultProperty() throws Exception {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck();
    mxmlTest();
  }

  @JSTestOptions(value = {JSTestOption.WithFlexFacet}, selectLookupItem = 0)
  public void testInFlex3RootTag() throws Exception {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck(false);
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testPredefinedTagsInsideNonContainerRootTag() throws Exception {
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testPredefinedTagsInsideNotRootTagWithDefaultProperty() throws Exception {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck();
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testXmlSourceAndFormatAttrs() throws Exception {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck();
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testXmlListInXmlListCollection() throws Exception {
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testNoCompletionInFxPrivate() throws Exception {
    myCompletionPerformer = () -> {
      final LinkedHashMap<Integer, String> map = JSTestUtils.extractPositionMarkers(myProject, getEditor().getDocument());
      for (Map.Entry<Integer, String> entry : map.entrySet()) {
        myItems = null;
        getEditor().getCaretModel().moveToOffset(entry.getKey());
        super.complete();
        assertNull(myItems);
      }
    };

    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk, JSTestOption.WithJsSupportLoader})
  public void testFxLibraryAndFxDefinition() throws Exception {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck();
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public final void testMxmlFieldReference() throws Exception {
    final String testName = getTestName(false);

    doTestForFiles(new VirtualFile[]{getVirtualFile(BASE_PATH + testName + "/aaa/" + testName + ".mxml")}, "", "mxml",
                   new File(getTestDataPath() + getBasePath() + File.separatorChar + testName));
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public final void testMatchingClassFromSomePackage() throws Exception {
    final String testName = getTestName(false);

    VirtualFile[] vFiles =
      new VirtualFile[]{getVirtualFile(getBasePath() + testName + ".mxml"), getVirtualFile(getBasePath() + testName + ".as")};
    doTestForFiles(vFiles, "", "mxml");
  }

  @JSTestOptions(selectLookupItem = 0)
  public void testCompletionDoesNotCorruptCode() throws Exception {
    doTest("", "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testDoNotSuggestFlex3NamespaceInFlex4Context() throws Exception {
    commonFlex3NamespaceInFlex4Context("library://ns.adobe.com/flex/mx", "mx.containers.*");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testSuggestClassInAllSuitableNamespaces() throws Exception {
    commonFlex3NamespaceInFlex4Context("http://www.adobe.com/2006/mxml", "mx.containers.*", "library://ns.adobe.com/flex/mx");
  }

  private void commonFlex3NamespaceInFlex4Context(final String namespaceToSelect, final String... otherExpectedNamespaces)
    throws Exception {
    configureByFile(BASE_PATH + getTestName(false) + ".mxml");
    complete();

    final String[] expectedNamespaces = ArrayUtil.mergeArrays(new String[]{namespaceToSelect}, otherExpectedNamespaces);

    assertEquals(expectedNamespaces.length, myItems.length);
    final String[] namespaces = new String[myItems.length];
    LookupElement selectedElement = null;

    for (int i = 0; i < myItems.length; i++) {
      final LookupElement lookupElement = myItems[i];
      final LookupElementPresentation presentation = new LookupElementPresentation();
      lookupElement.renderElement(presentation);

      assertEquals("Accordion", presentation.getItemText());
      final String namespace = presentation.getTypeText();
      namespaces[i] = namespace;
      if (namespace.equals(namespaceToSelect)) {
        selectedElement = lookupElement;
      }
    }

    assertSameElements(namespaces, expectedNamespaces);
    assertNotNull(selectedElement);
    selectItem(selectedElement, Lookup.REPLACE_SELECT_CHAR);
    checkResultByFile(BASE_PATH + getTestName(false) + "_after.mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testSuggestCorrectPrefix() throws Exception {
    final String testName = getTestName(false);

    doTestForFiles(new String[]{testName + ".mxml", testName + "_other.as"}, "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testEnumeratedMetadataAttributeValue() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testTypeAttributeOfEventMetadata() throws Exception {
    doTest("", "as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testImplicitImport() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testNoApplicationPropertyCompletionInsideFxDeclarations() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testNonVisualComponentInsideFxDeclarations() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testIdAttribute() throws Exception {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck();
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testPropertySpecifiedByMxmlIdAttribute() throws Exception {
    final String testName = getTestName(false);
    doTestForFiles(new String[]{testName + ".mxml", testName + "_other.mxml"}, "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testPropertySpecifiedByMxmlIdAttributeInParent() throws Exception {
    final String testName = getTestName(false);
    doTestForFiles(
      new String[]{testName + ".mxml", testName + "_other.mxml", testName + "_otherSuper.as", testName + "_otherSuperSuper.mxml"},
      "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testFxgComponentCompletion() throws Exception {
    doTestForFiles(new String[]{getTestName(false) + ".mxml", "CommonFxgComponent.fxg"}, "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testFxgAttributeCompletion() throws Exception {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck(false);
    doTestForFiles(new String[]{getTestName(false) + ".mxml", "CommonFxgComponent.fxg"}, "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testFxgSubTagCompletion() throws Exception {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck(false);
    doTestForFiles(new String[]{getTestName(false) + ".mxml", "CommonFxgComponent.fxg"}, "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testFxgClassCompletion() throws Exception {
    doTestForFiles(new String[]{getTestName(false) + ".mxml", "CommonFxgComponent.fxg"}, "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testFxgClassMembersCompletion() throws Exception {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck(false);
    doTestForFiles(new String[]{getTestName(false) + ".as", "CommonFxgComponent.fxg"}, "as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testFxComponent() throws Exception {
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testFxComponentChildren() throws Exception {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck(true);
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testFxReparent() throws Exception {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck(true);
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testStaticBlock() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testStaticBlock2() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testRootTagCompletion() throws Exception {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck(true);
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testRootTagReferencingToThisMxmlItself() throws Exception {
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testInsideTagWithDefaultPropertyOfObjectType() throws Exception {
    mxmlTest();
    // need to check that it was really mx:DataGridColumn completion, but not incomplete DataGridColumn completion from mx.controls.dataGridClasses.* namespace
    assertNull(myItems);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testInsideTagWithDefaultPropertyOfAnyType() throws Exception {
    final String testName = getTestName(false);
    doTestForFiles(new String[]{testName + ".mxml", testName + "_other.as"}, "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testIdAndPredefinedAttributes() throws Exception {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck(false);
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testLanguageTagsInInlineRenderer() throws Exception {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck(true);
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testBindingCompletion() throws Exception {
    mxmlTest();
    assertEquals(2, myItems.length);
    assertEquals("BaseListData", myItems[0].getLookupString());
    assertEquals("TreeListData", myItems[1].getLookupString());
    selectItem(myItems[0]);
    checkResultByFile("_after2", MXML_EXTENSION, null);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public final void testMobileApplicationChildren() throws Exception {
    mxmlTest();
    assertNull(myItems);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public final void testInternalPropertiesInMxml() throws Exception {
    final String testName = getTestName(false);
    doTestForFiles(new VirtualFile[]{getVirtualFile(BASE_PATH + testName + ".mxml"), getVirtualFile(BASE_PATH + testName + "_other.as")},
                   "", "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public final void testMxmlIdValueSuggestion() throws Exception {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck(true);
    mxmlTest();
  }

  public final void testMxmlIdValueSuggestion2() {
    checkIdValueSuggestions("", "Button", "button");
    checkIdValueSuggestions("", "A", "a");
    checkIdValueSuggestions("my", "A", "myA");
    checkIdValueSuggestions("", "URL", "url");
    checkIdValueSuggestions("ur", "URL", "url");
    checkIdValueSuggestions("my", "URL", "myURL");
    checkIdValueSuggestions("se", "HTTPService", "service", "seHTTPService");
    checkIdValueSuggestions("b", "ButtonBarButton", "button", "barButton", "buttonBarButton");
  }

  private static void checkIdValueSuggestions(final String value, final String type, final String... expected) {
    assertSameElements(AnnotationBackedDescriptorImpl.suggestIdValues(value, type), expected);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testParentGetter() throws Exception {
    final String testName = getTestName(false);
    doTestForFiles(new String[]{testName + ".mxml", testName + "_2.mxml"}, "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public final void testVectorAttributes() throws Exception {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck(true);
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testLowercasedMxml() throws Exception {
    final String testName = getTestName(false);
    doTestForFiles(new String[]{testName + ".mxml", Character.toLowerCase(testName.charAt(0)) + testName.substring(1) + "_other.mxml"},
                   "mxml");
  }

  @JSTestOptions({JSTestOption.WithGumboSdk, JSTestOption.WithFlexLib, JSTestOption.WithSmartCompletion})
  public void testNonApplicableInheritors() throws Exception {
    FlexTestUtils.modifyConfigs(myProject, editor -> {
      final ModifiableFlexBuildConfiguration bc1 = editor.getConfigurations(myModule)[0];
      final ModifiableFlexBuildConfiguration bc2 = editor.copyConfiguration(bc1, BuildConfigurationNature.DEFAULT);
      bc2.setName("bc 2");
      bc2.getDependencies().getModifiableEntries().clear();
    });

    LookupElement[] elements = doTest("", "as");

    assertEquals(3, elements.length);
    assertEquals("Image", elements[0].getLookupString());
    assertEquals("Base64Image", elements[1].getLookupString());
    assertEquals("ImageMap", elements[2].getLookupString());

    final FlexBuildConfigurationManager manager = FlexBuildConfigurationManager.getInstance(myModule);
    manager.setActiveBuildConfiguration(manager.findConfigurationByName("bc 2"));

    elements = doTest("", "as");

    assertEquals(1, elements.length);
    assertEquals("Image", elements[0].getLookupString());
  }

  private static void withNoAbsoluteReferences(ThrowableRunnable<Exception> r) throws Exception {
    boolean b = ReferenceSupport.ALLOW_ABSOLUTE_REFERENCES_IN_TESTS;
    ReferenceSupport.ALLOW_ABSOLUTE_REFERENCES_IN_TESTS = false;
    try {
      r.run();
    }
    finally {
      ReferenceSupport.ALLOW_ABSOLUTE_REFERENCES_IN_TESTS = b;
    }
  }

  @JSTestOptions({JSTestOption.WithGumboSdk})
  public void testDoNotSuggestClassesWithoutDefaultConstructor() throws Exception {
    final String testName = getTestName(false);
    doTestForFiles(new String[]{testName + ".mxml", testName + "_other.as"}, "mxml");
  }

  @JSTestOptions({JSTestOption.WithGumboSdk})
  public void testMxmlFromOtherPackage() throws Exception {
    final String testName = getTestName(false);
    doTestForFiles(new VirtualFile[]
                     {getVirtualFile(getBasePath() + testName + ".mxml"),
                       getVirtualFile(getBasePath() + testName + "/" + "CustomButton.mxml")},
                   "", "mxml", new File(getTestDataPath() + getBasePath()));
  }

  public void testFontFaceProperties() throws Exception {
    defaultTest();
    checkWeHaveInCompletion(myItems, "embedAsCFF", "advancedAntiAliasing");
  }

  public void testKeywords() throws Exception {
    defaultTest();
    checkWeHaveInCompletion(myItems, "for", "if");
  }

  public void testPropertyKey() throws Exception {
    final String testName = getTestName(false);
    doTestForFiles(new String[]{testName + ".mxml", testName + "_2.properties"}, "mxml");
  }

  @JSTestOptions({JSTestOption.WithGumboSdk})
  public void testStateGroups() throws Exception {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck(true);
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public final void testStarlingEvent() throws Exception {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck(false);
    doTest("", "as");
  }

  @JSTestOptions({JSTestOption.WithGumboSdk})
  public final void testNoDuplicateVariants() throws Exception {
    doTest("", "as");
  }

  public void testVectorObject() throws Exception {
    final Sdk sdk45 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.5"), null, true, getTestRootDisposable());
    FlexTestUtils.modifyConfigs(myProject, editor -> {
      ModifiableFlexBuildConfiguration bc1 = editor.getConfigurations(myModule)[0];
      FlexTestUtils.setSdk(bc1, sdk45);
    });
    final LookupElement[] elements = doTest("", "as");
    assertStartsWith(elements, "concat", "every", "filter");
    assertTrue(getBoldStatus(elements[0]));
  }

  public void testCompletionPerformance() throws Exception {
    myAfterCommitRunnable = () -> {
      FlexTestUtils.addFlexLibrary(false, myModule, "playerglobal", false, getTestDataPath() + getBasePath(), "playerglobal.swc",
                                   null, null);

      final PsiElement clazz = ActionScriptClassResolver
        .findClassByQNameStatic("flash.display3D.textures.CubeTexture", GlobalSearchScope.moduleWithLibrariesScope(myModule));
      clazz.getNode(); // this is required to switch from stubs to AST for library.swf from playerglobal.swc
    };

    configureByFile(BASE_PATH + getTestName(false) + ".as");

    final boolean doProfiling = false;
    if (doProfiling) ProfilingUtil.startCPUProfiling();
    try {
      PlatformTestUtil.startPerformanceTest("ActionScript class completion", 300, () -> complete())
        .setup(() -> getPsiManager().dropPsiCaches())
        .assertTiming();
    }
    finally {
      if (doProfiling) ProfilingUtil.captureCPUSnapshot();
    }
  }
}
