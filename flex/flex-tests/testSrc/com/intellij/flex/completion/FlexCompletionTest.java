// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.completion;

import com.intellij.codeInsight.completion.impl.CamelHumpMatcher;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.flex.editor.FlexProjectDescriptor;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.injected.editor.EditorWindow;
import com.intellij.javascript.flex.mxml.schema.AnnotationBackedDescriptorImpl;
import com.intellij.lang.javascript.*;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.ReferenceSupport;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexBuildConfiguration;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.util.ArrayUtil;
import com.jetbrains.performancePlugin.profilers.YourKitProfilerHandler;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.*;

import static com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase.JAVA_1_7;

public class FlexCompletionTest extends BaseJSCompletionTestCase {
  static final String BASE_PATH = "/flex_completion/";

  @NonNls private static final String MXML_EXTENSION = "mxml";

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
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath(BASE_PATH);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    FlexTestUtils.allowFlexVfsRootsFor(myFixture.getTestRootDisposable(), "");
    CamelHumpMatcher.forceStartMatching(myFixture.getTestRootDisposable());
    myCompletionPerformer = () -> super.complete();
    setUpJdk();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD})
  public @interface NeedsJavaModule {
  }

  private boolean needsJavaModule() {
    return JSTestUtils.getTestMethod(getClass(), getTestName(false)).getAnnotation(NeedsJavaModule.class) != null;
  }

  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(getModule(), getTestName(false), getClass(), myFixture.getTestRootDisposable());
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return needsJavaModule() ?
           JAVA_1_7 :
           FlexProjectDescriptor.DESCRIPTOR;
  }

  @Override
  protected LookupElement[] complete() {
    if (myCompletionPerformer != null) {
      myCompletionPerformer.run();
    }
    
    return myFixture.getLookupElements();
  }

  private Runnable createMultiCompletionPerformerWithVariantsCheck() {
    return createMultiCompletionPerformerWithVariantsCheck(true);
  }

  private Runnable createMultiCompletionPerformerWithVariantsCheck(final boolean strict) {
    return () -> {
      final LinkedHashMap<Integer, String> map = JSTestUtils.extractPositionMarkers(getProject(), myFixture.getEditor().getDocument());
      for (Map.Entry<Integer, String> entry : map.entrySet()) {
        myFixture.getEditor().getCaretModel().moveToOffset(entry.getKey());

        super.complete();

        final String[] expected = entry.getValue().length() == 0 ? new String[]{} : entry.getValue().split(",");

        final String[] variants = new String[myFixture.getLookupElements() == null ? 0 : myFixture.getLookupElements().length];
        if (myFixture.getLookupElements() != null && myFixture.getLookupElements().length > 0) {
          for (int i = 0; i < myFixture.getLookupElements().length; i++) {
            variants[i] = myFixture.getLookupElements()[i].getLookupString();
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
  public final void testCompletionInMxml() {
    defaultTest();
    //    doTest("_2", MXML_EXTENSION);
    //    doTest("_3", MXML_EXTENSION);
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public final void testCompleteInsertsQualifiedNameInItemRenderer() {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public final void testSkinClassAsAttributeWithSpaces() {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public final void testSkinClassAsSubTag() {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public final void testCompleteInsertsQualifiedNameInEventType() {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testSimpleCompleteInAddEventListener() {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testCompletionInMxml2() {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testCompletionInMxml3() {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testCompleteBeforeVariable() {
    doTest("", "as");
  }

  public void testCompleteBeforeVariable2() {
    doTest("", "as");
    LookupElement[] elements = doTest("_2", "as");
    assertTrue(elements != null && elements.length > 0);
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet}, selectLookupItem = 0)
  public final void testCompletionInMxml4() {
    defaultTest();
  }

  @Override
  protected LookupElement[] defaultTest() {
    return doTest("", MXML_EXTENSION);
  }

  public final void testCompletionInMxml5() {
    withNoAbsoluteReferences(() -> defaultTest());
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testCompletionInMxml6() {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testCompletionInMxml7() {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testCompletionInMxml8() {
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
  public final void testCompletionInMxml9() {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public final void testCompletionInMxml10() {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk}, selectLookupItem = 0)
  public void testMxmlColorAttributeValueCompletion1() {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testMxmlColorAttributeValueCompletion2() {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testMxmlColorAttributeValueCompletion3() {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testMxmlColorAttributeValueCompletion4() {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk}, selectLookupItem = 0)
  public void testMxmlColorAttributeValueCompletion5() {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public final void testStateNameCompletion() {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck();
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testCompletionOfMethodOfCustomComponent() {
    final String testName = getTestName(false);

    doTestForFiles(new String[]{testName + ".mxml", testName + "_2.mxml"}, "", MXML_EXTENSION);
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testCompletionOfPackageLocalClass() {
    final String testName = getTestName(false);

    VirtualFile directory = myFixture.copyDirectoryToProject(testName, "");
    myFixture.configureFromExistingVirtualFile(directory.findFileByRelativePath("/aaa/" + testName + ".mxml"));
    complete();
    checkResultByFile("", "mxml", null);
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testCompletionOfMxmlInAnotherDirectory() {
    final String testName = getTestName(false);
    VirtualFile directory = myFixture.copyDirectoryToProject(testName, "");
    myFixture.configureFromExistingVirtualFile(directory.findFileByRelativePath(testName + ".as"));
    complete();
    checkResultByFile("", "as", null);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public final void testCompleteAfterThisInMxml() {
    defaultTest();
  }

  public final void testCompleteAfterThisInMxml2() {
    defaultTest();
  }

  public final void testCompleteAfterThisInMxml3() {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk, JSTestOption.WithSmartCompletion})
  public final void testSmartCompletionInMxml() {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk, JSTestOption.WithSmartCompletion})
  public final void testSmartCompletionInMxml_2() {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk, JSTestOption.WithSmartCompletion})
  public final void testSmartCompletionInMxml_3() {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk, JSTestOption.WithSmartCompletion})
  public final void testSmartCompletionInMxml_4() {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk, JSTestOption.WithSmartCompletion})
  public final void testSmartCompletionInMxml_5() {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk, JSTestOption.WithSmartCompletion})
  public final void testSmartCompletionInMxml_6() {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public final void testClassRefInSkinClass() {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public final void testCompleteAnnotationParameter() {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testCompleteWithoutQualifier() {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader})
  @NeedsJavaModule
  public void testCompleteStyleNameInString() {
    doTestForFiles(new String[]{getTestName(false) + ".mxml",
      getTestName(false) + ".as"}, "", MXML_EXTENSION);
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testReplaceChar() {
    myFixture.configureByFile(getTestName(false) + ".as");
    complete();
    assertNotNull(myFixture.getLookupElements());
    myFixture.getLookup().setCurrentItem(myFixture.getLookupElements()[0]);
    myFixture.type(Lookup.REPLACE_SELECT_CHAR);
    myFixture.checkResultByFile(getTestName(false) + "_after.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public final void testEnumeratedCompletionInMxml() {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testCustomComponentCompletionInMxml() {
    doTestForFiles(new String[] {getTestName(false) + ".mxml", getTestName(false) + ".as"}, "", MXML_EXTENSION);
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet, JSTestOption.ClassNameCompletion})
  public final void testCustomComponentCompletionInMxml2() {
    doTestForFiles(new String[]{getTestName(false) + ".mxml", getTestName(false) + ".as"}, "", MXML_EXTENSION);
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testCustomComponentCompletionInMxml6() {
    doTestForFiles(new String[]{getTestName(false) + ".as", "MyComponent.mxml"}, "", "as");
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet, JSTestOption.ClassNameCompletion},
    selectLookupItem = 0)
  public final void testCustomComponentCompletionInMxml3() {
    doTestForFiles(new String[]{getTestName(false) + ".mxml"}, "", MXML_EXTENSION);
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet, JSTestOption.ClassNameCompletion})
  public final void testCustomComponentCompletionInMxml4() {
    doTestForFiles(new String[]{getTestName(false) + ".mxml"}, "", MXML_EXTENSION);
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testCompleteResourceReferences() {
    doTestForFiles(new String[]{getTestName(false) + ".mxml", "test.properties"}, "", MXML_EXTENSION);
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testCustomComponentCompletionInMxml5() {
    doTestForFiles(new String[]{getTestName(false) + ".mxml"}, "", MXML_EXTENSION);
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testCustomComponentCompletionInMxml7() {
    doTestForFiles(new String[]{getTestName(false) + ".mxml", getTestName(false) + "_2.mxml"}, "", MXML_EXTENSION);
  }

  public final void testCompleteInFileName() {
    withNoAbsoluteReferences(() -> doTest("", "as"));
  }

  public final void testNoComplete() {
    defaultTest();
  }

  public final void testAs2Completion() {
    doTest("", "as");
  }

  public final void testAs2Completion2() {
    doTestForFiles(getTestName(false) + ".as", getTestName(false) + "_2.as");
  }

  public final void testAs2Completion3() {
    doTestForFiles(getTestName(false) + ".as");
  }

  @JSTestOptions(selectLookupItem = 0)
  public final void testAs2Completion4() {
    doTestForFiles(getTestName(false) + ".as");
  }

  public final void testAs2Completion5() {
    doTestForFiles(getTestName(false) + ".as", getTestName(false) + "_2.as");
  }

  public final void testAs2Completion5_3() {
    doTestForFiles(new String[]{getTestName(false) + ".as"}, "", "as");
  }

  @JSTestOptions(
    {JSTestOption.WithLoadingAndSavingCaches})
  public final void testAs2Completion6() {
    doTestForFiles(getTestName(false) + ".as");
  }

  @JSTestOptions(
    {JSTestOption.WithLoadingAndSavingCaches})
  public final void testAs2Completion6_2() {
    final String testName = getTestName(false);
    doTestForFiles(new String[]{testName + ".as", testName.substring(0, testName.length() - 2) + "_3.as"}, "", "as");
  }

  public final void testPickupArrayElementType() {
    doTestForFiles(getTestName(false) + ".as");
  }

  public final void testPickupArrayElementType2() {
    doTestForFiles(getTestName(false) + ".as");
  }

  // Looks like tested functionality has never worked in run time, previously test passed because of a trick in base test class
  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public final void _testInsertImportForStaticField() {
    final String testName = getTestName(false);
    doTestForFiles(new String[]{testName + ".mxml", testName + "_2.as"}, "", "mxml");
  }

  // Looks like tested functionality has never worked in run time, previously test passed because of a trick in base test class
  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public final void _testInsertImportForStaticField_3() {
    final String testName = getTestName(false);
    doTestForFiles(new String[]{testName + ".mxml", testName.substring(0, testName.length() - 2) + "_2.as"}, "", "mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public final void testSuggestOnlyInterfaces() {
    final String testName = getTestName(false);
    doTestForFiles(new String[]{testName + ".mxml", testName + "_2.as"}, "", "mxml");
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk}, selectLookupItem = 0)
  public final void testSuggestOnlyDescendants() {
    final String testName = getTestName(false);
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck(true);
    String[] files = new String[]{testName + ".mxml",testName + "_2.as"};
    doTestForFiles(files, "", "mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public final void testSuggestOnlyDescendants2() {
    final String testName = getTestName(false);
    String[] files = new String[]{testName + ".mxml",testName + "_2.as"};
    doTestForFiles(files, "", "mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public final void testSuggestOnlyDescendants3() {
    final String testName = getTestName(false);
    String[] files = new String[]{testName + ".mxml",testName + "_2.as"};
    doTestForFiles(files, "", "mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public final void testSuggestOnlyDescendants4() {
    final String testName = getTestName(false);
    String[] files = new String[]{testName + ".mxml",testName + "_2.as"};
    doTestForFiles(files, "", "mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public final void testCompleteInComponent() {
    final String testName = getTestName(false);
    String[] files =
      new String[]{testName + ".mxml",testName + "_2.mxml"};
    doTestForFiles(files, "", "mxml");
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testRemoteObject() {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testRemoteObject_2() {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithFlexSdk, JSTestOption.WithJsSupportLoader})
  public void testCompleteOnlyPackages() {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithFlexSdk, JSTestOption.WithJsSupportLoader})
  public void testCompleteOnlyPackages_2() {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithFlexSdk, JSTestOption.WithJsSupportLoader})
  public void testCompleteOnlyPackages_3() {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithFlexSdk, JSTestOption.WithJsSupportLoader})
  public void testCompleteOnlyPackages_4() {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithFlexSdk, JSTestOption.WithJsSupportLoader})
  public void testCompleteOnlyPackages_5() {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testInheritorCompletion() {
    final String testName = getTestName(false);
    String[] files = new String[]{testName + ".mxml",testName + ".as"};
    doTestForFiles(files, "", MXML_EXTENSION);
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testInheritorCompletion2() {
    defaultTest();
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testOnlyMembersCompletion() {
    final String[] vFiles = new String[]{getTestName(false) + ".mxml",
      getTestName(false) + ".as"};
    LookupElement[] elements = doTestForFiles(vFiles, "", "mxml");
    assertEquals(1, elements.length);
    assertEquals("tabs", elements[0].getLookupString());
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testOnlyMembersCompletion2() {
    String[] vFiles = new String[]{getTestName(false) + ".mxml",
      getTestName(false) + ".as"};
    LookupElement[] elements = doTestForFiles(vFiles, "", "mxml");
    assertNull(elements);
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testOnlyMembersCompletion2_2() {
    String[] vFiles = new String[]{getTestName(false) + ".mxml",
      "OnlyMembersCompletion2.as"};
    doTestForFiles(vFiles, "", "mxml");
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testCompleteBindingAttr() {
    doTest("", "mxml");
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testImportedMember() {
    final String[] vFiles = new String[]{getTestName(false) + ".mxml",
      getTestName(false) + "_2.mxml",getTestName(false) + "_2_script.as"};
    doTestForFiles(vFiles, "", "mxml");
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testPackagesInCompilerConfig() {
    doTest("", "xml");
  }

  @JSTestOptions(
    {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testClassesInCompilerConfig() {
    doTest("", "xml");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testComponentFromManifestCompletion() {
    final String name = getTestName(false);

    FlexTestUtils.modifyBuildConfiguration(getModule(), bc -> {
      final String manifest = getTestDataPath() + "/" + "/" + name + "_manifest.xml";
      bc.getCompilerOptions().setAllOptions(Collections.singletonMap("compiler.namespaces.namespace", "http://MyNamespace\t" + manifest));
    });

    String[] files = new String[]{name + ".mxml",name + "_other.as"};
    doTestForFiles(files, "", MXML_EXTENSION);
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testComponentFromManifestCompletionWithNamespaceAutoInsert() {
    final String name = getTestName(false);

    FlexTestUtils.modifyBuildConfiguration(getModule(), bc -> {
      final String manifest = getTestDataPath() + "/" + "/" + name + "_manifest.xml";
      bc.getCompilerOptions().setAllOptions(Collections.singletonMap("compiler.namespaces.namespace",
                                                                     "schema://www.MyNamespace.com/2010\t" + manifest));
    });

    final String testName = getTestName(false);
    String[] files =
      new String[]{testName + ".mxml",testName + "_other.as"};
    doTestForFiles(files, "", MXML_EXTENSION);
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testInlineComponent() {
    doTest("", "mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testNamedInlineComponent() {
    doTest("", "mxml");
  }

  @JSTestOptions(value = {JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk}, selectLookupItem = 0)
  public void testNamedInlineComponent2() {
    doTest("", "mxml");
    FileDocumentManager.getInstance().saveAllDocuments();
    LookupElement[] elements = doTest("", "mxml");
    assertEquals(3, elements.length);
    assertEquals("MyEditor", elements[0].getLookupString());
    assertEquals("MyEditor2", elements[1].getLookupString());
    assertEquals("MyEditor3", elements[2].getLookupString());
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testAssetFromAnotherSourceRoot() {
    final String testName = getTestName(false);
    final VirtualFile secondSourceRoot = VfsUtil.findFileByIoFile(new File(getTestDataPath() + testName), false);
    PsiTestUtil.addSourceRoot(getModule(), secondSourceRoot);
    Disposer.register(myFixture.getTestRootDisposable(), () -> PsiTestUtil.removeContentEntry(getModule(), secondSourceRoot));
    withNoAbsoluteReferences(() -> doTest("", "mxml"));
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testCompleteStandardMxmlImport() {
    doTest("", "mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testCompleteUIComponentInItemRenderer() {
    doTest("", "mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testCompleteComponentsInUIComponent() {
    doTest("", "mxml");
    doTest("_2", "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testResourceBundleFromSdk() {
    final String testName = getTestName(false);

    final Sdk flexSdk = FlexUtils.getSdkForActiveBC(getModule());
    final SdkModificator sdkModificator = flexSdk.getSdkModificator();
    final VirtualFile swcFile =
      LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + "/" + testName + ".swc");
    sdkModificator.addRoot(JarFileSystem.getInstance().getJarRootForLocalFile(swcFile), OrderRootType.CLASSES);
    sdkModificator.commitChanges();

    doTest("", "as");
    doTest("", "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testResourceBundleFromLib() {
    FlexTestUtils.addFlexLibrary(false, getModule(), "Lib", false, getTestDataPath(), getTestName(false) + ".swc", null,
                                 null);
    Disposer.register(myFixture.getTestRootDisposable(), () -> FlexTestUtils.removeLibrary(getModule(), "Lib"));
    doTest("", "as");
    doTest("", "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testIgnoreClassesFromOnlyLibSources() {
    final String testName = getTestName(false);

    FlexTestUtils.addLibrary(getModule(), "library", getTestDataPath() + "/", testName + "/empty.swc", testName + "/LibSources.zip",
                             null);
    //Disposer.register(myFixture.getTestRootDisposable(), () -> );


    assertEmpty(doTest("_1", "as"));
    assertEmpty(doTest("_2", "as"));
    assertEmpty(doTest("_3", "as"));
    FlexTestUtils.removeLibrary(getModule(), "library");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testIgnoreClassesFromOnlySdkSources() {
    final String testName = getTestName(false);

    final VirtualFile srcFile =
      LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + "/" + testName + "_sdk_src/");

    final Sdk flexSdk = FlexUtils.getSdkForActiveBC(getModule());
    final SdkModificator modificator = flexSdk.getSdkModificator();
    modificator.addRoot(srcFile, OrderRootType.SOURCES);
    modificator.commitChanges();

    assertEmpty(doTest("_1", MXML_EXTENSION));
    assertEmpty(doTest("_2", MXML_EXTENSION));
    assertEmpty(doTest("_3", MXML_EXTENSION));
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testImportInsertPosition1() {
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testImportInsertPosition2() {
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testImportInsertPosition3() {
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testImportInsertPosition4() {
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testImportInsertPosition5() {
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testImportInsertPosition6() {
    mxmlTest();
  }

  private void mxmlTest() {
    doTest("", MXML_EXTENSION);
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testImportInsertPosition7() {
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testImportInsertPosition8() {
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testImportInsertPosition9() {
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testDoNotCompleteMembersInType() {
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testDoNotCompleteMembersInType2() {
    mxmlTest();
  }

  public void testDoNotCompleteMembersInType3() {
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testTwoCompletions() {
    myCompletionPerformer = () -> {
      super.complete();
      super.complete();
    };
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testObsoleteContext() {
    myCompletionPerformer = () -> {
      final int offset = myFixture.getEditor().getCaretModel().getOffset();

      super.complete();

      assertEquals("\n" + "    var v = myButtonOne;\n" + "  ", myFixture.getEditor().getDocument().getText());

      replace("myButtonOne", "myButton", myFixture.getEditor());
      replace("myButtonOne", "myButtonTwo", ((EditorWindow)myFixture.getEditor()).getDelegate());

      myFixture.getEditor().getCaretModel().moveToOffset(offset);

      super.complete();
    };
    mxmlTest();
  }

  private static void replace(final String original, final String replacement, final Editor editor) {
    final int offset = editor.getDocument().getText().indexOf(original);
    assertTrue(offset != -1);
    WriteCommandAction.runWriteCommandAction(editor.getProject(), () -> editor.getDocument().replaceString(offset, offset + original.length(), replacement));

    PsiDocumentManager.getInstance(editor.getProject()).commitDocument(editor.getDocument());

    //myFixture.getEditor().getCaretModel().moveToOffset(offset + original.length());
    //
    //for (int i = 0; i < original.length(); i++) {
    //  JSBaseEditorTestCase.performTypingAction(myFixture.getEditor(), JSBaseEditorTestCase.BACKSPACE_FAKE_CHAR);
    //}
    //
    //for (int i = 0; i < replacement.length(); i++) {
    //  JSBaseEditorTestCase.performTypingAction(myFixture.getEditor(), replacement.charAt(i));
    //}
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testConditionalCompilationConstantsInAs() {
    FlexTestUtils.modifyBuildConfiguration(getModule(), bc -> {
      bc.getCompilerOptions()
        .setAdditionalConfigFilePath(getTestDataPath() + "/" + "/" + getTestName(false) + "_custom_config.xml");
      bc.getCompilerOptions().setAllOptions(Collections.singletonMap("compiler.define", ""));
    });
    // following is ignored because overridden at bc level
    FlexBuildConfigurationManager.getInstance(getModule()).getModuleLevelCompilerOptions()
      .setAllOptions(Collections.singletonMap("compiler.define", "UNKNOWN::defined1\tfalse"));

    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck();
    doTest("", "as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testConditionalCompilationConstantsInMxml() {
    FlexTestUtils.modifyBuildConfiguration(getModule(), bc -> {
      bc.getCompilerOptions().setAllOptions(Collections.singletonMap("compiler.define", "CONFIG1::defined1\t\nCONFIG1::defined2\t-1"));
      bc.getCompilerOptions().setAdditionalOptions("-compiler.define=CONFIG2::defined3,true");
    });

    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck();
    doTest("", "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testPredefinedTagsInsideRootTagWithDefaultProperty() {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck();
    mxmlTest();
  }

  @JSTestOptions(value = {JSTestOption.WithFlexFacet}, selectLookupItem = 0)
  public void testInFlex3RootTag() {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck(false);
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testPredefinedTagsInsideNonContainerRootTag() {
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testPredefinedTagsInsideNotRootTagWithDefaultProperty() {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck();
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testXmlSourceAndFormatAttrs() {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck();
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testXmlListInXmlListCollection() {
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testNoCompletionInFxPrivate() {
    myCompletionPerformer = () -> {
      final LinkedHashMap<Integer, String> map = JSTestUtils.extractPositionMarkers(getProject(), myFixture.getEditor().getDocument());
      for (Map.Entry<Integer, String> entry : map.entrySet()) {
        myFixture.getEditor().getCaretModel().moveToOffset(entry.getKey());
        super.complete();
        assertEmpty(myFixture.getLookupElements());
      }
    };

    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk, JSTestOption.WithJsSupportLoader})
  public void testFxLibraryAndFxDefinition() {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck();
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public final void testMxmlFieldReference() {
    final String testName = getTestName(false);

    doTestForFiles(new String[]{testName + "/aaa/" + testName + ".mxml"}, "", "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public final void testMatchingClassFromSomePackage() {
    final String testName = getTestName(false);

    String[] vFiles =
      new String[]{testName + ".mxml",testName + ".as"};
    doTestForFiles(vFiles, "", "mxml");
  }

  @JSTestOptions(selectLookupItem = 0)
  public void testCompletionDoesNotCorruptCode() {
    doTest("", "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testDoNotSuggestFlex3NamespaceInFlex4Context() {
    commonFlex3NamespaceInFlex4Context("library://ns.adobe.com/flex/mx", "mx.containers.*");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testSuggestClassInAllSuitableNamespaces() {
    commonFlex3NamespaceInFlex4Context("http://www.adobe.com/2006/mxml", "mx.containers.*", "library://ns.adobe.com/flex/mx");
  }

  private void commonFlex3NamespaceInFlex4Context(final String namespaceToSelect, final String... otherExpectedNamespaces)
    {
    myFixture.configureByFile(getTestName(false) + ".mxml");
    complete();

    final String[] expectedNamespaces = ArrayUtil.mergeArrays(new String[]{namespaceToSelect}, otherExpectedNamespaces);

    assertEquals(expectedNamespaces.length, myFixture.getLookupElements().length);
    final String[] namespaces = new String[myFixture.getLookupElements().length];
    LookupElement selectedElement = null;

    for (int i = 0; i < myFixture.getLookupElements().length; i++) {
      final LookupElement lookupElement = myFixture.getLookupElements()[i];
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
    myFixture.getLookup().setCurrentItem(selectedElement);
    myFixture.type(Lookup.REPLACE_SELECT_CHAR);
    myFixture.checkResultByFile(getTestName(false) + "_after.mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testSuggestCorrectPrefix() {
    final String testName = getTestName(false);

    doTestForFiles(new String[]{testName + ".mxml", testName + "_other.as"}, "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testEnumeratedMetadataAttributeValue() {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testTypeAttributeOfEventMetadata() {
    doTest("", "as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testImplicitImport() {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testNoApplicationPropertyCompletionInsideFxDeclarations() {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testNonVisualComponentInsideFxDeclarations() {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testIdAttribute() {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck();
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testPropertySpecifiedByMxmlIdAttribute() {
    final String testName = getTestName(false);
    doTestForFiles(new String[]{testName + ".mxml", testName + "_other.mxml"}, "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testPropertySpecifiedByMxmlIdAttributeInParent() {
    final String testName = getTestName(false);
    doTestForFiles(
      new String[]{testName + ".mxml", testName + "_other.mxml", testName + "_otherSuper.as", testName + "_otherSuperSuper.mxml"},
      "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testFxgComponentCompletion() {
    doTestForFiles(new String[]{getTestName(false) + ".mxml", "CommonFxgComponent.fxg"}, "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testFxgAttributeCompletion() {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck(false);
    doTestForFiles(new String[]{getTestName(false) + ".mxml", "CommonFxgComponent.fxg"}, "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testFxgSubTagCompletion() {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck(false);
    doTestForFiles(new String[]{getTestName(false) + ".mxml", "CommonFxgComponent.fxg"}, "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testFxgClassCompletion() {
    doTestForFiles(new String[]{getTestName(false) + ".mxml", "CommonFxgComponent.fxg"}, "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testFxgClassMembersCompletion() {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck(false);
    doTestForFiles(new String[]{getTestName(false) + ".as", "CommonFxgComponent.fxg"}, "as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testFxComponent() {
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testFxComponentChildren() {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck(true);
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testFxReparent() {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck(true);
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testStaticBlock() {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testStaticBlock2() {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testRootTagCompletion() {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck(true);
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testRootTagReferencingToThisMxmlItself() {
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testInsideTagWithDefaultPropertyOfObjectType() {
    mxmlTest();
    // need to check that it was really mx:DataGridColumn completion, but not incomplete DataGridColumn completion from mx.controls.dataGridClasses.* namespace
    assertNull(myFixture.getLookupElements());
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testInsideTagWithDefaultPropertyOfAnyType() {
    final String testName = getTestName(false);
    doTestForFiles(new String[]{testName + ".mxml", testName + "_other.as"}, "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testIdAndPredefinedAttributes() {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck(false);
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testLanguageTagsInInlineRenderer() {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck(true);
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public final void testBindingCompletion() {
    mxmlTest();
    assertEquals(2, myFixture.getLookupElements().length);
    assertEquals("BaseListData", myFixture.getLookupElements()[0].getLookupString());
    assertEquals("TreeListData", myFixture.getLookupElements()[1].getLookupString());
    myFixture.getLookup().setCurrentItem(myFixture.getLookupElements()[0]);
    myFixture.type('\n');
    checkResultByFile("_after2", MXML_EXTENSION, null);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public final void testMobileApplicationChildren() {
    mxmlTest();
    assertNull(myFixture.getLookupElements());
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public final void testInternalPropertiesInMxml() {
    final String testName = getTestName(false);
    doTestForFiles(new String[]{testName + ".mxml",testName + "_other.as"},
                   "", "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public final void testMxmlIdValueSuggestion() {
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
  public void testParentGetter() {
    final String testName = getTestName(false);
    doTestForFiles(new String[]{testName + ".mxml", testName + "_2.mxml"}, "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public final void testVectorAttributes() {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck(true);
    mxmlTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testLowercasedMxml() {
    final String testName = getTestName(false);
    doTestForFiles(new String[]{testName + ".mxml", Character.toLowerCase(testName.charAt(0)) + testName.substring(1) + "_other.mxml"},
                   "mxml");
  }

  @JSTestOptions({JSTestOption.WithGumboSdk, JSTestOption.WithFlexLib, JSTestOption.WithSmartCompletion})
  public void testNonApplicableInheritors() {
    FlexTestUtils.addFlexLibrary(false, getModule(), "Flex Lib", true, getTestDataPath() + "../flexlib", "flexlib.swc", null, null);

    LookupElement[] elements = doTest("", "as");

    assertEquals(3, elements.length);
    assertEquals("Image", elements[0].getLookupString());
    assertEquals("Base64Image", elements[1].getLookupString());
    assertEquals("ImageMap", elements[2].getLookupString());

    FlexTestUtils.modifyConfigs(getProject(), editor -> {
      final ModifiableFlexBuildConfiguration bc1 = editor.getConfigurations(getModule())[0];
      bc1.getDependencies().getModifiableEntries().clear();
    });

    elements = doTest("", "as");

    assertEquals(1, elements.length);
    assertEquals("Image", elements[0].getLookupString());
  }

  private static void withNoAbsoluteReferences(Runnable r) {
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
  public void testDoNotSuggestClassesWithoutDefaultConstructor() {
    final String testName = getTestName(false);
    doTestForFiles(new String[]{testName + ".mxml", testName + "_other.as"}, "mxml");
  }

  @JSTestOptions({JSTestOption.WithGumboSdk})
  public void testMxmlFromOtherPackage() {
    final String testName = getTestName(false);
    doTestForFiles(new String[]
                     {testName + ".mxml", testName + "/" + "CustomButton.mxml"},
                   "", "mxml");
  }

  public void testFontFaceProperties() {
    defaultTest();
    checkWeHaveInCompletion(myFixture.getLookupElements(), "embedAsCFF", "advancedAntiAliasing");
  }

  public void testKeywords() {
    defaultTest();
    checkWeHaveInCompletion(myFixture.getLookupElements(), "for", "if");
  }

  public void testPropertyKey() {
    final String testName = getTestName(false);
    doTestForFiles(new String[]{testName + ".mxml", testName + "_2.properties"}, "mxml");
  }

  @JSTestOptions({JSTestOption.WithGumboSdk})
  public void testStateGroups() {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck(true);
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public final void testStarlingEvent() {
    myCompletionPerformer = createMultiCompletionPerformerWithVariantsCheck(false);
    doTest("", "as");
  }

  @JSTestOptions({JSTestOption.WithGumboSdk})
  public final void testNoDuplicateVariants() {
    doTest("", "as");
  }

  public void testVectorObject() {
    final Sdk sdk45 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.5"), null, true, myFixture.getTestRootDisposable());
    FlexTestUtils.modifyConfigs(getProject(), editor -> {
      ModifiableFlexBuildConfiguration bc1 = editor.getConfigurations(getModule())[0];
      FlexTestUtils.setSdk(bc1, sdk45);
    });
    final LookupElement[] elements = doTest("", "as");
    assertStartsWith(elements, "concat", "every", "filter");
    assertTrue(isStrictMatched(elements[0]));
  }

  public void testCompletionPerformance() {
    FlexTestUtils.addFlexLibrary(false, getModule(), "playerglobal", false, getTestDataPath(), "playerglobal.swc",
                                 null, null);
    Disposer.register(myFixture.getTestRootDisposable(), () -> FlexTestUtils.removeLibrary(getModule(), "playerglobal"));

    //final PsiElement clazz = ActionScriptClassResolver
      //  .findClassByQNameStatic("flash.display3D.textures.CubeTexture", GlobalSearchScope.moduleWithLibrariesScope(myModule));
      //clazz.getNode(); // this is required to switch from stubs to AST for library.swf from playerglobal.swc

    myFixture.configureByFile(getTestName(false) + ".as");

    final boolean doProfiling = false;
    if (doProfiling) YourKitProfilerHandler.startCPUProfiling();
    try {
      PlatformTestUtil.startPerformanceTest("ActionScript class completion", 300, () -> complete())
        .setup(() -> getPsiManager().dropPsiCaches())
        .reattemptUntilJitSettlesDown()
        .assertTiming();
    }
    finally {
      if (doProfiling) YourKitProfilerHandler.captureCPUSnapshot();
    }
  }

  public void testOnlyValidPackageNamesInCompletion() {
    myFixture.configureByText(ActionScriptFileType.INSTANCE, "var a: String = new <caret>");

    VirtualFile srcRoot = ModuleRootManager.getInstance(getModule()).getSourceRoots(false)[0];
    WriteCommandAction.runWriteCommandAction(getProject(), ()-> {
      try {
        srcRoot.createChildDirectory(null, ".idea");
        srcRoot.createChildDirectory(null, ".foo");
        srcRoot.createChildDirectory(null, "a b");
        srcRoot.createChildDirectory(null, "###");
        srcRoot.createChildDirectory(null, "abc123");
      } catch (IOException e) {
        throw new Error(e);
      }
    });

    complete();
    List<String> items = ContainerUtil.map(myFixture.getLookupElements(), (e) -> e.getLookupString());
    assertDoesntContain(items, "", "\"\"", "a b", "###");
    assertContainsElements(items, "abc123");
  }
}
