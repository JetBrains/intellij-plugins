// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.completion;

import com.intellij.codeInsight.completion.impl.CamelHumpMatcher;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.injected.editor.EditorWindow;
import com.intellij.javascript.flex.mxml.schema.AnnotationBackedDescriptorImpl;
import com.intellij.lang.javascript.ActionScriptFileType;
import com.intellij.lang.javascript.JSCompletionFixture;
import com.intellij.lang.javascript.JSCompletionTestHelperSimple;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.ReferenceSupport;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexBuildConfiguration;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.StandardFileSystems;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.PerformanceUnitTest;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.tools.ide.metrics.benchmark.Benchmark;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.performancePlugin.yourkit.YourKitProfilerHandler;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.intellij.lang.javascript.JSCodeInsightFixture.assertSameElements;
import static com.intellij.lang.javascript.JSCompletionFixture.assertStartsWith;
import static com.intellij.lang.javascript.JSCompletionFixture.checkWeHaveInCompletion;
import static com.intellij.lang.javascript.JSCompletionFixture.isStrictMatched;
import static com.intellij.flex.completion.FlexCompletionFixture.flexCompletionFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestApplication
public class FlexCompletionTest {
  public static final boolean PROFILING = false;
  static final String BASE_PATH = "/flex_completion/";

  @NonNls private static final String MXML_EXTENSION = "mxml";

  private final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<FlexCompletionFixture> js =
    flexCompletionFixture(projectFixture, new JSFixtureOptions(FlexTestUtils.getTestDataPath(BASE_PATH), "as")
        .withModuleType(FlexModuleType.MODULE_TYPE_ID));

  @BeforeEach
  void setUp() {
    js.get().getSmartCompletionTests().addAll(
      Arrays.asList("CompletionInMxml2", "CompletionInMxml3", "PickupArrayElementType", "PickupArrayElementType2",
                    "PickupArrayElementType3", "CompleteAfterThisInMxml", "BindingCompletion"));
    CodeInsightTestFixture myFixture = js.get().getFixture();
    FlexTestUtils.allowFlexVfsRootsFor(myFixture.getTestRootDisposable(), "");
    CamelHumpMatcher.forceStartMatching(myFixture.getTestRootDisposable());
    setUpJdk();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface NeedsJavaModule {
  }

  private boolean needsJavaModule() {
    return JSTestUtils.getTestMethod(getClass(), js.get().getTestName(false)).getAnnotation(NeedsJavaModule.class) != null;
  }

  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(js.get().getFixture().getModule(), js.get().getTestName(false), getClass(), js.get().getFixture().getTestRootDisposable());
  }

  private Runnable createMultiCompletionPerformerWithVariantsCheck() {
    return createMultiCompletionPerformerWithVariantsCheck(true);
  }

  private Runnable createMultiCompletionPerformerWithVariantsCheck(final boolean strict) {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    return () -> {
      final LinkedHashMap<Integer, String> map = JSTestUtils.extractPositionMarkers(js.get().getProject(), myFixture.getEditor().getDocument());
      for (Map.Entry<Integer, String> entry : map.entrySet()) {
        myFixture.getEditor().getCaretModel().moveToOffset(entry.getKey());

        js.get().helper().superComplete();

        final String[] expected = entry.getValue().length() == 0 ? ArrayUtil.EMPTY_STRING_ARRAY : entry.getValue().split(",");

        final String[] variants = new String[myFixture.getLookupElements() == null ? 0 : myFixture.getLookupElements().length];
        if (myFixture.getLookupElements() != null && myFixture.getLookupElements().length > 0) {
          for (int i = 0; i < myFixture.getLookupElements().length; i++) {
            variants[i] = myFixture.getLookupElements()[i].getLookupString();
          }
        }

        if (strict || expected.length == 0) {
          assertThat(variants).containsExactlyInAnyOrder(expected);
        }
        else {
          for (final String variant : expected) {
            assertTrue(ArrayUtil.contains(variant, variants), "Missing from completion list: " + variant);
          }
        }
      }
    };
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public final void testCompletionInMxml() {
    js.get().defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public final void testCompleteInsertsQualifiedNameInItemRenderer() {
    js.get().defaultTest();
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public final void testSkinClassAsAttributeWithSpaces() {
    js.get().defaultTest();
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public final void testSkinClassAsSubTag() {
    js.get().defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public final void testCompleteInsertsQualifiedNameInEventType() {
    js.get().defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public final void testSimpleCompleteInAddEventListener() {
    js.get().defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public final void testCompletionInMxml2() {
    js.get().defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public final void testCompletionInMxml3() {
    js.get().defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testCompleteBeforeVariable() {
    js.get().doTest("", "as");
  }

  @Test
  public void testCompleteBeforeVariable2() {
    js.get().doTest("", "as");
    LookupElement[] elements = js.get().doTest("_2", "as");
    assertTrue(elements != null && elements.length > 0);
  }

  @JSTestOptions(selectLookupItem = 0)
  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public final void testCompletionInMxml4() {
    js.get().defaultTest();
  }

  @Test
  public final void testCompletionInMxml5() {
    withNoAbsoluteReferences(() -> js.get().defaultTest());
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public final void testCompletionInMxml6() {
    js.get().defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public final void testCompletionInMxml7() {
    js.get().defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public final void testCompletionInMxml8() {
    LookupElement[] items = js.get().doTest("", MXML_EXTENSION);
    assertNotNull(items);
    assertTrue(items.length < 50);

    js.get().doTest("_3", MXML_EXTENSION);

    items = js.get().doTest("_2", MXML_EXTENSION);
    assertNotNull(items);
    for (LookupElement li : items) {
      assertNotEquals("arity", li.getLookupString());
    }
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public final void testCompletionInMxml9() {
    js.get().defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public final void testCompletionInMxml10() {
    js.get().defaultTest();
  }

  @JSTestOptions(selectLookupItem = 0)
  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testMxmlColorAttributeValueCompletion1() {
    js.get().defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testMxmlColorAttributeValueCompletion2() {
    js.get().defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testMxmlColorAttributeValueCompletion3() {
    js.get().defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testMxmlColorAttributeValueCompletion4() {
    js.get().defaultTest();
  }

  @JSTestOptions(selectLookupItem = 0)
  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testMxmlColorAttributeValueCompletion5() {
    js.get().defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public final void testStateNameCompletion() {
    js.get().setCompletionPerformer(createMultiCompletionPerformerWithVariantsCheck());
    js.get().defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public final void testCompletionOfMethodOfCustomComponent() {
    final String testName = js.get().getTestName(false);

    js.get().doTestForFiles(new String[]{testName + ".mxml", testName + "_2.mxml"}, "", MXML_EXTENSION);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public final void testCompletionOfPackageLocalClass() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    final String testName = js.get().getTestName(false);

    VirtualFile directory = myFixture.copyDirectoryToProject(testName, "");
    myFixture.configureFromExistingVirtualFile(directory.findFileByRelativePath("/aaa/" + testName + ".mxml"));
    js.get().complete();
    js.get().checkResultByFile("", "mxml", null);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public final void testCompletionOfMxmlInAnotherDirectory() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    final String testName = js.get().getTestName(false);
    VirtualFile directory = myFixture.copyDirectoryToProject(testName, "");
    myFixture.configureFromExistingVirtualFile(directory.findFileByRelativePath(testName + ".as"));
    js.get().complete();
    js.get().checkResultByFile("", "as", null);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public final void testCompleteAfterThisInMxml() {
    js.get().defaultTest();
  }

  @Test
  public final void testCompleteAfterThisInMxml2() {
    js.get().defaultTest();
  }

  @Test
  public final void testCompleteAfterThisInMxml3() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public final void testSmartCompletionInMxml() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public final void testSmartCompletionInMxml_2() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public final void testSmartCompletionInMxml_3() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public final void testSmartCompletionInMxml_4() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public final void testSmartCompletionInMxml_5() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public final void testSmartCompletionInMxml_6() {
    js.get().defaultTest();
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public final void testClassRefInSkinClass() {
    js.get().defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public final void testCompleteAnnotationParameter() {
    js.get().defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public final void testCompleteWithoutQualifier() {
    js.get().defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public final void testReplaceChar() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    myFixture.configureByFile(js.get().getTestName(false) + ".as");
    js.get().complete();
    assertNotNull(myFixture.getLookupElements());
    EdtTestUtil.runInEdtAndWait(() -> myFixture.getLookup().setCurrentItem(myFixture.getLookupElements()[0]));
    myFixture.type(Lookup.REPLACE_SELECT_CHAR);
    myFixture.checkResultByFile(js.get().getTestName(false) + "_after.as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public final void testEnumeratedCompletionInMxml() {
    js.get().defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public final void testCustomComponentCompletionInMxml() {
    js.get().doTestForFiles(new String[] {js.get().getTestName(false) + ".mxml", js.get().getTestName(false) + ".as"}, "", MXML_EXTENSION);
  }

  @JSTestOptions(JSTestOption.ClassNameCompletion)
  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public final void testCustomComponentCompletionInMxml2() {
    js.get().doTestForFiles(new String[]{js.get().getTestName(false) + ".mxml", js.get().getTestName(false) + ".as"}, "", MXML_EXTENSION);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public final void testCustomComponentCompletionInMxml6() {
    js.get().doTestForFiles(new String[]{js.get().getTestName(false) + ".as", "MyComponent.mxml"}, "", "as");
  }

  @JSTestOptions(value = JSTestOption.ClassNameCompletion, selectLookupItem = 0)
  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public final void testCustomComponentCompletionInMxml3() {
    js.get().doTestForFiles(new String[]{js.get().getTestName(false) + ".mxml"}, "", MXML_EXTENSION);
  }

  @JSTestOptions(JSTestOption.ClassNameCompletion)
  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public final void testCustomComponentCompletionInMxml4() {
    js.get().doTestForFiles(new String[]{js.get().getTestName(false) + ".mxml"}, "", MXML_EXTENSION);
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testCompleteResourceReferences() {
    js.get().doTestForFiles(new String[]{js.get().getTestName(false) + ".mxml", "test.properties"}, "", MXML_EXTENSION);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public final void testCustomComponentCompletionInMxml5() {
    js.get().doTestForFiles(new String[]{js.get().getTestName(false) + ".mxml"}, "", MXML_EXTENSION);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public final void testCustomComponentCompletionInMxml7() {
    js.get().doTestForFiles(new String[]{js.get().getTestName(false) + ".mxml", js.get().getTestName(false) + "_2.mxml"}, "", MXML_EXTENSION);
  }

  @Test
  public final void testCompleteInFileName() {
    withNoAbsoluteReferences(() -> js.get().doTest("", "as"));
  }

  @Test
  public final void testNoComplete() {
    js.get().defaultTest();
  }

  @Test
  public final void testAs2Completion() {
    js.get().doTest("", "as");
  }

  @Test
  public final void testAs2Completion2() {
    js.get().doTestForFiles(js.get().getTestName(false) + ".as", js.get().getTestName(false) + "_2.as");
  }

  @Test
  public final void testAs2Completion3() {
    js.get().doTestForFiles(js.get().getTestName(false) + ".as");
  }

  @JSTestOptions(selectLookupItem = 0)
  @Test
  public final void testAs2Completion4() {
    js.get().doTestForFiles(js.get().getTestName(false) + ".as");
  }

  @Test
  public final void testAs2Completion5() {
    js.get().doTestForFiles(js.get().getTestName(false) + ".as", js.get().getTestName(false) + "_2.as");
  }

  @Test
  public final void testAs2Completion5_3() {
    js.get().doTestForFiles(new String[]{js.get().getTestName(false) + ".as"}, "", "as");
  }

  @Test
  public final void testAs2Completion6() {
    js.get().doTestForFiles(js.get().getTestName(false) + ".as");
  }

  @Test
  public final void testAs2Completion6_2() {
    final String testName = js.get().getTestName(false);
    js.get().doTestForFiles(new String[]{testName + ".as", testName.substring(0, testName.length() - 2) + "_3.as"}, "", "as");
  }

  @Test
  public final void testPickupArrayElementType() {
    js.get().doTestForFiles(js.get().getTestName(false) + ".as");
  }

  @Test
  public final void testPickupArrayElementType2() {
    js.get().doTestForFiles(js.get().getTestName(false) + ".as");
  }

  // Looks like tested functionality has never worked in run time, previously test passed because of a trick in base test class
  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public final void _testInsertImportForStaticField() {
    final String testName = js.get().getTestName(false);
    js.get().doTestForFiles(new String[]{testName + ".mxml", testName + "_2.as"}, "", "mxml");
  }

  // Looks like tested functionality has never worked in run time, previously test passed because of a trick in base test class
  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public final void _testInsertImportForStaticField_3() {
    final String testName = js.get().getTestName(false);
    js.get().doTestForFiles(new String[]{testName + ".mxml", testName.substring(0, testName.length() - 2) + "_2.as"}, "", "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public final void testSuggestOnlyInterfaces() {
    final String testName = js.get().getTestName(false);
    js.get().doTestForFiles(new String[]{testName + ".mxml", testName + "_2.as"}, "", "mxml");
  }

  @JSTestOptions(selectLookupItem = 0)
  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public final void testSuggestOnlyDescendants() {
    final String testName = js.get().getTestName(false);
    js.get().setCompletionPerformer(createMultiCompletionPerformerWithVariantsCheck(true));
    String[] files = new String[]{testName + ".mxml",testName + "_2.as"};
    js.get().doTestForFiles(files, "", "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public final void testSuggestOnlyDescendants2() {
    final String testName = js.get().getTestName(false);
    String[] files = new String[]{testName + ".mxml",testName + "_2.as"};
    js.get().doTestForFiles(files, "", "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public final void testSuggestOnlyDescendants3() {
    final String testName = js.get().getTestName(false);
    String[] files = new String[]{testName + ".mxml",testName + "_2.as"};
    js.get().doTestForFiles(files, "", "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public final void testSuggestOnlyDescendants4() {
    final String testName = js.get().getTestName(false);
    String[] files = new String[]{testName + ".mxml",testName + "_2.as"};
    js.get().doTestForFiles(files, "", "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public final void testCompleteInComponent() {
    final String testName = js.get().getTestName(false);
    String[] files =
      new String[]{testName + ".mxml",testName + "_2.mxml"};
    js.get().doTestForFiles(files, "", "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testRemoteObject() {
    js.get().defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testRemoteObject_2() {
    js.get().defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testCompleteOnlyPackages() {
    js.get().defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testCompleteOnlyPackages_2() {
    js.get().defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testCompleteOnlyPackages_3() {
    js.get().defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testCompleteOnlyPackages_4() {
    js.get().defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testCompleteOnlyPackages_5() {
    js.get().defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testInheritorCompletion() {
    final String testName = js.get().getTestName(false);
    String[] files = new String[]{testName + ".mxml",testName + ".as"};
    js.get().doTestForFiles(files, "", MXML_EXTENSION);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testInheritorCompletion2() {
    js.get().defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testOnlyMembersCompletion() {
    final String[] vFiles = new String[]{js.get().getTestName(false) + ".mxml",
      js.get().getTestName(false) + ".as"};
    LookupElement[] elements = js.get().doTestForFiles(vFiles, "", "mxml");
    assertEquals(1, elements.length);
    assertEquals("tabs", elements[0].getLookupString());
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testOnlyMembersCompletion2() {
    String[] vFiles = new String[]{js.get().getTestName(false) + ".mxml",
      js.get().getTestName(false) + ".as"};
    LookupElement[] elements = js.get().doTestForFiles(vFiles, "", "mxml");
    assertNull(elements);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testOnlyMembersCompletion2_2() {
    String[] vFiles = new String[]{js.get().getTestName(false) + ".mxml",
      "OnlyMembersCompletion2.as"};
    js.get().doTestForFiles(vFiles, "", "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testCompleteBindingAttr() {
    js.get().doTest("", "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testImportedMember() {
    final String[] vFiles = new String[]{js.get().getTestName(false) + ".mxml",
      js.get().getTestName(false) + "_2.mxml",js.get().getTestName(false) + "_2_script.as"};
    js.get().doTestForFiles(vFiles, "", "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testPackagesInCompilerConfig() {
    js.get().doTest("", "xml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testClassesInCompilerConfig() {
    js.get().doTest("", "xml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testComponentFromManifestCompletion() {
    final String name = js.get().getTestName(false);

    FlexTestUtils.modifyBuildConfiguration(js.get().getFixture().getModule(), bc -> {
      final String manifest = js.get().getTestDataPath() + "/" + "/" + name + "_manifest.xml";
      bc.getCompilerOptions().setAllOptions(Collections.singletonMap("compiler.namespaces.namespace", "http://MyNamespace\t" + manifest));
    });

    String[] files = new String[]{name + ".mxml",name + "_other.as"};
    js.get().doTestForFiles(files, "", MXML_EXTENSION);
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testComponentFromManifestCompletionWithNamespaceAutoInsert() {
    final String name = js.get().getTestName(false);

    FlexTestUtils.modifyBuildConfiguration(js.get().getFixture().getModule(), bc -> {
      final String manifest = js.get().getTestDataPath() + "/" + "/" + name + "_manifest.xml";
      bc.getCompilerOptions().setAllOptions(Collections.singletonMap("compiler.namespaces.namespace",
                                                                     "schema://www.MyNamespace.com/2010\t" + manifest));
    });

    final String testName = js.get().getTestName(false);
    String[] files =
      new String[]{testName + ".mxml",testName + "_other.as"};
    js.get().doTestForFiles(files, "", MXML_EXTENSION);
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testInlineComponent() {
    js.get().doTest("", "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testNamedInlineComponent() {
    js.get().doTest("", "mxml");
  }

  @JSTestOptions(selectLookupItem = 0)
  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testNamedInlineComponent2() {
    js.get().doTest("", "mxml");
    FileDocumentManager.getInstance().saveAllDocuments();
    LookupElement[] elements = js.get().doTest("", "mxml");
    assertEquals(3, elements.length);
    assertEquals("MyEditor", elements[0].getLookupString());
    assertEquals("MyEditor2", elements[1].getLookupString());
    assertEquals("MyEditor3", elements[2].getLookupString());
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testAssetFromAnotherSourceRoot() {
    final String testName = js.get().getTestName(false);
    final VirtualFile secondSourceRoot = VfsUtil.findFileByIoFile(new File(js.get().getTestDataPath() + testName), false);
    PsiTestUtil.addSourceRoot(js.get().getFixture().getModule(), secondSourceRoot);
    Module module = js.get().getFixture().getModule();
    Disposer.register(js.get().getFixture().getTestRootDisposable(), () -> PsiTestUtil.removeContentEntry(module, secondSourceRoot));
    withNoAbsoluteReferences(() -> js.get().doTest("", "mxml"));
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testCompleteStandardMxmlImport() {
    js.get().doTest("", "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testCompleteUIComponentInItemRenderer() {
    js.get().doTest("", "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testCompleteComponentsInUIComponent() {
    js.get().doTest("", "mxml");
    js.get().doTest("_2", "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testResourceBundleFromSdk() {
    final String testName = js.get().getTestName(false);

    final Sdk flexSdk = FlexUtils.getSdkForActiveBC(js.get().getFixture().getModule());
    final SdkModificator sdkModificator = flexSdk.getSdkModificator();
    final VirtualFile swcFile =
      StandardFileSystems.local().findFileByPath(js.get().getTestDataPath() + "/" + testName + ".swc");
    sdkModificator.addRoot(JarFileSystem.getInstance().getJarRootForLocalFile(swcFile), OrderRootType.CLASSES);
    ApplicationManager.getApplication().runWriteAction(() -> sdkModificator.commitChanges());

    js.get().doTest("", "as");
    js.get().doTest("", "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testResourceBundleFromLib() {
    FlexTestUtils.addFlexLibrary(false, js.get().getFixture().getModule(), "Lib", false, js.get().getTestDataPath(), js.get().getTestName(false) + ".swc", null,
                                 null);
    Module module = js.get().getFixture().getModule();
    Disposer.register(js.get().getFixture().getTestRootDisposable(), () -> FlexTestUtils.removeLibrary(module, "Lib"));
    js.get().doTest("", "as");
    js.get().doTest("", "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testIgnoreClassesFromOnlyLibSources() {
    final String testName = js.get().getTestName(false);

    FlexTestUtils.addLibrary(js.get().getFixture().getModule(), "library", js.get().getTestDataPath() + "/", testName + "/empty.swc", testName + "/LibSources.zip",
                             null);
    //Disposer.register(myFixture.getTestRootDisposable(), () -> );


    assertThat(js.get().doTest("_1", "as")).isNullOrEmpty();
    assertThat(js.get().doTest("_2", "as")).isNullOrEmpty();
    assertThat(js.get().doTest("_3", "as")).isNullOrEmpty();
    FlexTestUtils.removeLibrary(js.get().getFixture().getModule(), "library");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testIgnoreClassesFromOnlySdkSources() {
    final String testName = js.get().getTestName(false);

    final VirtualFile srcFile =
      StandardFileSystems.local().findFileByPath(js.get().getTestDataPath() + "/" + testName + "_sdk_src/");

    final Sdk flexSdk = FlexUtils.getSdkForActiveBC(js.get().getFixture().getModule());
    final SdkModificator modificator = flexSdk.getSdkModificator();
    modificator.addRoot(srcFile, OrderRootType.SOURCES);
    ApplicationManager.getApplication().runWriteAction(() -> modificator.commitChanges());

    assertThat(js.get().doTest("_1", MXML_EXTENSION)).isNullOrEmpty();
    assertThat(js.get().doTest("_2", MXML_EXTENSION)).isNullOrEmpty();
    assertThat(js.get().doTest("_3", MXML_EXTENSION)).isNullOrEmpty();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testImportInsertPosition1() {
    mxmlTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testImportInsertPosition2() {
    mxmlTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testImportInsertPosition3() {
    mxmlTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testImportInsertPosition4() {
    mxmlTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testImportInsertPosition5() {
    mxmlTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testImportInsertPosition6() {
    mxmlTest();
  }

  private void mxmlTest() {
    js.get().doTest("", MXML_EXTENSION);
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testImportInsertPosition7() {
    mxmlTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testImportInsertPosition8() {
    mxmlTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testImportInsertPosition9() {
    mxmlTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testDoNotCompleteMembersInType() {
    mxmlTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testDoNotCompleteMembersInType2() {
    mxmlTest();
  }

  @Test
  public void testDoNotCompleteMembersInType3() {
    mxmlTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testTwoCompletions() {
    js.get().setCompletionPerformer(() -> {
      js.get().helper().superComplete();
      js.get().helper().superComplete();
    });
    mxmlTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testObsoleteContext() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    js.get().setCompletionPerformer(() -> {
      final int offset = myFixture.getEditor().getCaretModel().getOffset();

      js.get().helper().superComplete();

      assertEquals("""

                         var v = myButtonOne;
                       \
                     """, myFixture.getEditor().getDocument().getText());

      replace("myButtonOne", "myButton", myFixture.getEditor());
      replace("myButtonOne", "myButtonTwo", ((EditorWindow)myFixture.getEditor()).getDelegate());

      myFixture.getEditor().getCaretModel().moveToOffset(offset);

      js.get().helper().superComplete();
    });
    mxmlTest();
  }

  private static void replace(final String original, final String replacement, final Editor editor) {
    final int offset = editor.getDocument().getText().indexOf(original);
    assertTrue(offset != -1);
    WriteCommandAction.runWriteCommandAction(editor.getProject(), () -> editor.getDocument().replaceString(offset, offset + original.length(), replacement));

    PsiDocumentManager.getInstance(editor.getProject()).commitDocument(editor.getDocument());
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testConditionalCompilationConstantsInAs() {
    FlexTestUtils.modifyBuildConfiguration(js.get().getFixture().getModule(), bc -> {
      bc.getCompilerOptions()
        .setAdditionalConfigFilePath(js.get().getTestDataPath() + "/" + "/" + js.get().getTestName(false) + "_custom_config.xml");
      bc.getCompilerOptions().setAllOptions(Collections.singletonMap("compiler.define", ""));
    });
    // following is ignored because overridden at bc level
    FlexBuildConfigurationManager.getInstance(js.get().getFixture().getModule()).getModuleLevelCompilerOptions()
      .setAllOptions(Collections.singletonMap("compiler.define", "UNKNOWN::defined1\tfalse"));

    js.get().setCompletionPerformer(createMultiCompletionPerformerWithVariantsCheck());
    js.get().doTest("", "as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testConditionalCompilationConstantsInMxml() {
    FlexTestUtils.modifyBuildConfiguration(js.get().getFixture().getModule(), bc -> {
      bc.getCompilerOptions().setAllOptions(Collections.singletonMap("compiler.define", "CONFIG1::defined1\t\nCONFIG1::defined2\t-1"));
      bc.getCompilerOptions().setAdditionalOptions("-compiler.define=CONFIG2::defined3,true");
    });

    js.get().setCompletionPerformer(createMultiCompletionPerformerWithVariantsCheck());
    js.get().doTest("", "mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testPredefinedTagsInsideRootTagWithDefaultProperty() {
    js.get().setCompletionPerformer(createMultiCompletionPerformerWithVariantsCheck());
    mxmlTest();
  }

  @JSTestOptions(selectLookupItem = 0)
  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testInFlex3RootTag() {
    js.get().setCompletionPerformer(createMultiCompletionPerformerWithVariantsCheck(false));
    mxmlTest();
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testPredefinedTagsInsideNonContainerRootTag() {
    mxmlTest();
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testPredefinedTagsInsideNotRootTagWithDefaultProperty() {
    js.get().setCompletionPerformer(createMultiCompletionPerformerWithVariantsCheck());
    mxmlTest();
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testXmlSourceAndFormatAttrs() {
    js.get().setCompletionPerformer(createMultiCompletionPerformerWithVariantsCheck());
    mxmlTest();
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testXmlListInXmlListCollection() {
    mxmlTest();
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testNoCompletionInFxPrivate() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    js.get().setCompletionPerformer(() -> {
      final LinkedHashMap<Integer, String> map = JSTestUtils.extractPositionMarkers(js.get().getProject(), myFixture.getEditor().getDocument());
      for (Map.Entry<Integer, String> entry : map.entrySet()) {
        myFixture.getEditor().getCaretModel().moveToOffset(entry.getKey());
        js.get().helper().superComplete();
        assertThat(myFixture.getLookupElements()).isNullOrEmpty();
      }
    });

    mxmlTest();
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testFxLibraryAndFxDefinition() {
    js.get().setCompletionPerformer(createMultiCompletionPerformerWithVariantsCheck());
    mxmlTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public final void testMxmlFieldReference() {
    final String testName = js.get().getTestName(false);

    js.get().doTestForFiles(new String[]{testName + "/aaa/" + testName + ".mxml"}, "", "mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public final void testMatchingClassFromSomePackage() {
    final String testName = js.get().getTestName(false);

    String[] vFiles =
      new String[]{testName + ".mxml",testName + ".as"};
    js.get().doTestForFiles(vFiles, "", "mxml");
  }

  @JSTestOptions(selectLookupItem = 0)
  @Test
  public void testCompletionDoesNotCorruptCode() {
    js.get().doTest("", "mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testDoNotSuggestFlex3NamespaceInFlex4Context() {
    commonFlex3NamespaceInFlex4Context("library://ns.adobe.com/flex/mx", "mx.containers.*");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testSuggestClassInAllSuitableNamespaces() {
    commonFlex3NamespaceInFlex4Context("http://www.adobe.com/2006/mxml", "mx.containers.*", "library://ns.adobe.com/flex/mx");
  }

  private void commonFlex3NamespaceInFlex4Context(final String namespaceToSelect, final String... otherExpectedNamespaces)
    {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    myFixture.configureByFile(js.get().getTestName(false) + ".mxml");
    js.get().complete();

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

    assertThat(namespaces).containsExactlyInAnyOrder(expectedNamespaces);
    assertNotNull(selectedElement);
    final LookupElement elementToSelect = selectedElement;
    EdtTestUtil.runInEdtAndWait(() -> myFixture.getLookup().setCurrentItem(elementToSelect));
    myFixture.type(Lookup.REPLACE_SELECT_CHAR);
    myFixture.checkResultByFile(js.get().getTestName(false) + "_after.mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testSuggestCorrectPrefix() {
    final String testName = js.get().getTestName(false);

    js.get().doTestForFiles(new String[]{testName + ".mxml", testName + "_other.as"}, "mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testEnumeratedMetadataAttributeValue() {
    js.get().defaultTest();
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testTypeAttributeOfEventMetadata() {
    js.get().doTest("", "as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testImplicitImport() {
    js.get().defaultTest();
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testNoApplicationPropertyCompletionInsideFxDeclarations() {
    js.get().defaultTest();
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testNonVisualComponentInsideFxDeclarations() {
    js.get().defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testIdAttribute() {
    js.get().setCompletionPerformer(createMultiCompletionPerformerWithVariantsCheck());
    mxmlTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testPropertySpecifiedByMxmlIdAttribute() {
    final String testName = js.get().getTestName(false);
    js.get().doTestForFiles(new String[]{testName + ".mxml", testName + "_other.mxml"}, "mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testPropertySpecifiedByMxmlIdAttributeInParent() {
    final String testName = js.get().getTestName(false);
    js.get().doTestForFiles(
      new String[]{testName + ".mxml", testName + "_other.mxml", testName + "_otherSuper.as", testName + "_otherSuperSuper.mxml"},
      "mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testFxgComponentCompletion() {
    js.get().doTestForFiles(new String[]{js.get().getTestName(false) + ".mxml", "CommonFxgComponent.fxg"}, "mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testFxgAttributeCompletion() {
    js.get().setCompletionPerformer(createMultiCompletionPerformerWithVariantsCheck(false));
    js.get().doTestForFiles(new String[]{js.get().getTestName(false) + ".mxml", "CommonFxgComponent.fxg"}, "mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testFxgSubTagCompletion() {
    js.get().setCompletionPerformer(createMultiCompletionPerformerWithVariantsCheck(false));
    js.get().doTestForFiles(new String[]{js.get().getTestName(false) + ".mxml", "CommonFxgComponent.fxg"}, "mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testFxgClassCompletion() {
    js.get().doTestForFiles(new String[]{js.get().getTestName(false) + ".mxml", "CommonFxgComponent.fxg"}, "mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testFxgClassMembersCompletion() {
    js.get().setCompletionPerformer(createMultiCompletionPerformerWithVariantsCheck(false));
    js.get().doTestForFiles(new String[]{js.get().getTestName(false) + ".as", "CommonFxgComponent.fxg"}, "as");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testFxComponent() {
    mxmlTest();
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testFxComponentChildren() {
    js.get().setCompletionPerformer(createMultiCompletionPerformerWithVariantsCheck(true));
    mxmlTest();
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testFxReparent() {
    js.get().setCompletionPerformer(createMultiCompletionPerformerWithVariantsCheck(true));
    mxmlTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testStaticBlock() {
    js.get().defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testStaticBlock2() {
    js.get().defaultTest();
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testRootTagCompletion() {
    js.get().setCompletionPerformer(createMultiCompletionPerformerWithVariantsCheck(true));
    mxmlTest();
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testRootTagReferencingToThisMxmlItself() {
    mxmlTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testInsideTagWithDefaultPropertyOfObjectType() {
    mxmlTest();
    // need to check that it was really mx:DataGridColumn completion, but not incomplete DataGridColumn completion from mx.controls.dataGridClasses.* namespace
    assertNull(js.get().getFixture().getLookupElements());
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testInsideTagWithDefaultPropertyOfAnyType() {
    final String testName = js.get().getTestName(false);
    js.get().doTestForFiles(new String[]{testName + ".mxml", testName + "_other.as"}, "mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testIdAndPredefinedAttributes() {
    js.get().setCompletionPerformer(createMultiCompletionPerformerWithVariantsCheck(false));
    mxmlTest();
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testLanguageTagsInInlineRenderer() {
    js.get().setCompletionPerformer(createMultiCompletionPerformerWithVariantsCheck(true));
    mxmlTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public final void testBindingCompletion() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    mxmlTest();
    assertEquals(2, myFixture.getLookupElements().length);
    assertEquals("BaseListData", myFixture.getLookupElements()[0].getLookupString());
    assertEquals("TreeListData", myFixture.getLookupElements()[1].getLookupString());
    EdtTestUtil.runInEdtAndWait(() -> myFixture.getLookup().setCurrentItem(myFixture.getLookupElements()[0]));
    myFixture.type('\n');
    js.get().checkResultByFile("_after2", MXML_EXTENSION, null);
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public final void testMobileApplicationChildren() {
    mxmlTest();
    assertNull(js.get().getFixture().getLookupElements());
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public final void testInternalPropertiesInMxml() {
    final String testName = js.get().getTestName(false);
    js.get().doTestForFiles(new String[]{testName + ".mxml",testName + "_other.as"},
                            "", "mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public final void testMxmlIdValueSuggestion() {
    js.get().setCompletionPerformer(createMultiCompletionPerformerWithVariantsCheck(true));
    mxmlTest();
  }

  @Test
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
    assertThat(AnnotationBackedDescriptorImpl.suggestIdValues(value, type)).containsExactlyInAnyOrder(expected);
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testParentGetter() {
    final String testName = js.get().getTestName(false);
    js.get().doTestForFiles(new String[]{testName + ".mxml", testName + "_2.mxml"}, "mxml");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public final void testVectorAttributes() {
    js.get().setCompletionPerformer(createMultiCompletionPerformerWithVariantsCheck(true));
    mxmlTest();
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testLowercasedMxml() {
    final String testName = js.get().getTestName(false);
    js.get().doTestForFiles(new String[]{testName + ".mxml", Character.toLowerCase(testName.charAt(0)) + testName.substring(1) + "_other.mxml"},
                            "mxml");
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @FlexTestOptions({FlexTestOption.WithGumboSdk, FlexTestOption.WithFlexLib})
  @Test
  public void testNonApplicableInheritors() {
    FlexTestUtils.addFlexLibrary(false, js.get().getFixture().getModule(), "Flex Lib", true, js.get().getTestDataPath() + "../flexlib", "flexlib.swc", null, null);

    LookupElement[] elements = js.get().doTest("", "as");

    assertEquals(3, elements.length);
    assertEquals("Image", elements[0].getLookupString());
    assertEquals("Base64Image", elements[1].getLookupString());
    assertEquals("ImageMap", elements[2].getLookupString());

    FlexTestUtils.modifyConfigs(js.get().getProject(), editor -> {
      final ModifiableFlexBuildConfiguration bc1 = editor.getConfigurations(js.get().getFixture().getModule())[0];
      bc1.getDependencies().getModifiableEntries().clear();
    });

    elements = js.get().doTest("", "as");

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

  @FlexTestOptions(FlexTestOption.WithGumboSdk)
  @Test
  public void testDoNotSuggestClassesWithoutDefaultConstructor() {
    final String testName = js.get().getTestName(false);
    js.get().doTestForFiles(new String[]{testName + ".mxml", testName + "_other.as"}, "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithGumboSdk)
  @Test
  public void testMxmlFromOtherPackage() {
    final String testName = js.get().getTestName(false);
    js.get().doTestForFiles(new String[]
                     {testName + ".mxml", testName + "/" + "CustomButton.mxml"},
                            "", "mxml");
  }

  @Test
  public void testFontFaceProperties() {
    js.get().defaultTest();
    checkWeHaveInCompletion(js.get().getFixture().getLookupElements(), "embedAsCFF", "advancedAntiAliasing");
  }

  @Test
  public void testKeywords() {
    js.get().defaultTest();
    checkWeHaveInCompletion(js.get().getFixture().getLookupElements(), "for", "if");
  }

  @Test
  public void testPropertyKey() {
    final String testName = js.get().getTestName(false);
    js.get().doTestForFiles(new String[]{testName + ".mxml", testName + "_2.properties"}, "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithGumboSdk)
  @Test
  public void testStateGroups() {
    js.get().setCompletionPerformer(createMultiCompletionPerformerWithVariantsCheck(true));
    js.get().defaultTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public final void testStarlingEvent() {
    js.get().setCompletionPerformer(createMultiCompletionPerformerWithVariantsCheck(false));
    js.get().doTest("", "as");
  }

  @FlexTestOptions(FlexTestOption.WithGumboSdk)
  @Test
  public final void testNoDuplicateVariants() {
    js.get().doTest("", "as");
  }

  @Test
  public void testVectorObject() {
    final Sdk sdk45 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.5"), null, true, js.get().getFixture().getTestRootDisposable());
    FlexTestUtils.modifyConfigs(js.get().getProject(), editor -> {
      ModifiableFlexBuildConfiguration bc1 = editor.getConfigurations(js.get().getFixture().getModule())[0];
      FlexTestUtils.setSdk(bc1, sdk45);
    });
    final LookupElement[] elements = js.get().doTest("", "as");
    assertStartsWith(elements, "concat", "every", "filter");
    assertTrue(isStrictMatched(elements[0]));
  }

  @PerformanceUnitTest
  @Test
  public void testCompletionPerformance() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    FlexTestUtils.addFlexLibrary(false, js.get().getFixture().getModule(), "playerglobal", false, js.get().getTestDataPath(), "playerglobal.swc",
                                 null, null);
    Module module = js.get().getFixture().getModule();
    Disposer.register(myFixture.getTestRootDisposable(), () -> FlexTestUtils.removeLibrary(module, "playerglobal"));

    myFixture.configureByFile(js.get().getTestName(false) + ".as");

    if (PROFILING) YourKitProfilerHandler.startCPUProfiling();
    try {
      Benchmark.newBenchmark("ActionScript class completion", () -> js.get().complete())
        .setup(() -> EdtTestUtil.runInEdtAndWait(() -> myFixture.getPsiManager().dropPsiCaches())).runAsStressTest()
        .start();
    }
    finally {
      if (PROFILING) YourKitProfilerHandler.captureCPUSnapshot();
    }
  }

  @Test
  public void testOnlyValidPackageNamesInCompletion() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    myFixture.configureByText(ActionScriptFileType.INSTANCE, "var a: String = new <caret>");

    VirtualFile srcRoot = ModuleRootManager.getInstance(js.get().getFixture().getModule()).getSourceRoots(false)[0];
    WriteCommandAction.runWriteCommandAction(js.get().getProject(), ()-> {
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

    js.get().complete();
    List<String> items = ContainerUtil.map(myFixture.getLookupElements(), (e) -> e.getLookupString());
    assertThat(items).doesNotContain("", "\"\"", "a b", "###");
    assertThat(items).contains("abc123");
  }
}
