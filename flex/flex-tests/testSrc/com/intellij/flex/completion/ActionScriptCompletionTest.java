// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.completion;

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.completion.impl.CamelHumpMatcher;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javaee.ExternalResourceManagerExBase;
import com.intellij.lang.javascript.JSCompletionFixture;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexBuildConfiguration;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSAttributeImpl;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.TestLookupElementPresentation;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.util.ThrowableRunnable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;

import static com.intellij.lang.javascript.JSCompletionFixture.assertLookupElement;
import static com.intellij.lang.javascript.JSCompletionFixture.assertStartsWith;
import static com.intellij.lang.javascript.JSCompletionFixture.checkNoCompletion;
import static com.intellij.lang.javascript.JSCompletionFixture.checkWeHaveInCompletion;
import static com.intellij.lang.javascript.JSCompletionFixture.isStrictMatched;
import static com.intellij.lang.javascript.JSCompletionFixture.jsCompletionFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestApplication
public class ActionScriptCompletionTest {
  protected static final String BASE_PATH = "/js2_completion/";

  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSCompletionFixture> js =
    jsCompletionFixture(projectFixture, new JSFixtureOptions(FlexTestUtils.getTestDataPath(BASE_PATH), "js2")
        .withModuleType(FlexModuleType.MODULE_TYPE_ID));

  @BeforeEach
  void setUp() {
    js.get().getSmartCompletionTests().addAll(Arrays.asList(
      "VarTypePickedUp", "VarTypePickedUp2", "FuncTypePickedUp", "FuncTypePickedUp2", "FuncTypePickedUp3", "CompleteAfterThis",
      "CompleteAfterThis2", "CompleteAfterSuper", "ConstrTypePickedUp", "CompleteWithoutQualifier"
    ));
    CodeInsightTestFixture myFixture = js.get().getFixture();
    FlexTestUtils.allowFlexVfsRootsFor(myFixture.getTestRootDisposable(), "");
    CamelHumpMatcher.forceStartMatching(myFixture.getTestRootDisposable());
  }

  @AfterEach
  void tearDown() {
    FlexTestUtils.modifyConfigs(js.get().getProject(), editor -> {
      if (ModuleType.get(js.get().getFixture().getModule()) == FlexModuleType.getInstance()) {
        for (ModifiableFlexBuildConfiguration bc : editor.getConfigurations(js.get().getFixture().getModule())) {
          bc.getDependencies().setSdkEntry(null);
        }
      }
    });
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  @interface NeedsJavaModule {
  }

  private boolean needsJavaModule() {
    return JSTestUtils.getTestMethod(getClass(), js.get().getTestName(false)).getAnnotation(NeedsJavaModule.class) != null;
  }

  protected void setUpJdk() {
    if (!needsJavaModule()) {
      FlexTestUtils.setupFlexSdk(js.get().getFixture().getModule(), js.get().getTestName(false), getClass(), js.get().getFixture().getTestRootDisposable());
    }
  }

  @Test
  public final void testBasic() {
    js.get().doTest("");
    js.get().doTest("_2");
    js.get().doTest("_3");
    js.get().doTest("_4");
  }

  @Test
  public final void testBoldForClassMembers() {
    LookupElement[] elements = js.get().doTest("");
    assertNotNull(elements);
    assertEquals("____", elements[0].getLookupString());

    assertTrue(isStrictMatched(elements[0]));
    assertTrue(isStrictMatched(elements[1]));

    elements = js.get().doTest("_2");
    assertNotNull(elements);
    assertEquals("____", elements[0].getLookupString());
    assertTrue(isStrictMatched(elements[0]));
    assertTrue(isStrictMatched(elements[1]));

    elements = js.get().doTest("_3");
    assertNotNull(elements);
    assertEquals("____", elements[4].getLookupString());
    assertTrue(isStrictMatched(elements[4]));
    assertEquals("lVar", elements[0].getLookupString());
    assertTrue(isStrictMatched(elements[0]));
    assertEquals("param", elements[1].getLookupString());
    assertTrue(isStrictMatched(elements[1]));

    assertTrue(isStrictMatched(elements[5]));
    assertEquals("Bar", elements[5].getLookupString());
  }

  @Test
  public final void testKeywords() {
    js.get().doTest("");
  }

  @Test
  public final void testKeywords2() {
    final LookupElement[] elements = js.get().doTest("");
    checkWeHaveInCompletion(elements, "return", "function", "if");
  }

  @JSTestOptions(selectLookupItem = 0)
  @Test
  public final void testKeywordsInContext() {
    final LookupElement[] lookupElements = js.get().doTestForFiles(js.get().getTestName(false) + ".js2", js.get().getTestName(false) + "_2.js2");
    assertEquals("extends", lookupElements[0].getLookupString());
    assertTrue(isStrictMatched(lookupElements[0]));
    assertTrue(lookupElements.length > 1, "Test expected to have other options for completion");
  }

  @Test
  public final void testKeywordsInContext2() {
    final LookupElement[] lookupElements = js.get().doTestForFiles(js.get().getTestName(false) + ".js2", js.get().getTestName(false) + "_2.js2");
    assertEquals("public", lookupElements[0].getLookupString());
    assertTrue(isStrictMatched(lookupElements[0]));
    assertTrue(lookupElements.length > 1, "Test expected to have other options for completion");
  }

  @Test
  public void testKeywordsInContext3() {
    final LookupElement[] lookupElements = js.get().defaultTest();
    assertStartsWith(lookupElements, "zzz");
    checkWeHaveInCompletion(lookupElements, "true", "null", "new");
  }

  @JSTestOptions(selectLookupItem = 0)
  @Test
  public void testKeywordsInContext4() {
    js.get().defaultTest();
  }

  @Test
  public final void testVarTypePickedUp() {
    js.get().doTest("");
    js.get().doTest("_2");
  }

  @Test
  public final void testVarTypePickedUp2() {
    js.get().doTestForFiles(js.get().getTestName(false) + ".js2", js.get().getTestName(false) + "_2.js2");
  }

  @Test
  public final void testCustomMeta() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    String testName = js.get().getTestName(false);
    myFixture.configureByFiles(testName + ".js2", testName + ".dtd");
    final VirtualFile relativeFile = VfsUtilCore.findRelativeFile(testName + ".dtd", myFixture.getFile().getVirtualFile());
    ExternalResourceManagerExBase.registerResourceTemporarily(JSAttributeImpl.URN_FLEX_META, relativeFile.getPath(), js.get().getTestRootDisposable());

    js.get().complete();
    js.get().checkResultByFile("", js.get().getExtension());

    myFixture.configureByFiles(testName + "2.js2");
    js.get().complete();
    js.get().checkResultByFile("2", js.get().getExtension());
  }

  @Test
  public final void testCompleteAttrName() {
    js.get().defaultTest();
  }

  @Test
  public final void testFuncTypePickedUp() {
    js.get().doTest("");
  }

  @Test
  public final void testFuncTypePickedUp2() {
    js.get().doTest("");
  }

  @Test
  public final void testFuncTypePickedUp3() {
    js.get().doTestForFiles(js.get().getTestName(false) + ".js2", js.get().getTestName(false) + "_2.js2");
  }

  @Test
  public final void testFuncTypePickedUp4() {
    js.get().doTest("");
  }

  @Test
  public final void testFuncTypePickedUp5() {
    js.get().doTest("");
  }

  @Test
  public final void testCompleteAfterThis() {
    js.get().doTest("");
  }

  @Test
  public final void testCompleteAfterThis2() {
    js.get().doTest("");
    js.get().getSmartCompletionTests().remove(js.get().getTestName(false));
    FileDocumentManager.getInstance().saveAllDocuments();
    js.get().doTest("");
  }

  @Test
  public final void testCompleteAfterThis3() {
    js.get().doTest("");
  }

  @Test
  public final void testCompleteAfterType() {
    js.get().doTest("");
  }

  @Test
  public final void testCompleteAfterThis4() {
    js.get().doTest("");
  }

  @Test
  public final void testCompleteAfterSuper() {
    js.get().doTest("");
  }

  @Test
  public final void testCompleteWithoutQualifier() {
    js.get().doTest("");
    js.get().doTest("_2");
    js.get().doTest("_3");
  }

  @Test
  public final void testNoComplete() {
    js.get().doTest("");
  }

  @Test
  public final void testCompleteNS() {
    js.get().doTest("");
  }

  @Test
  public final void testCompleteNS2() {
    js.get().doTest("");
  }

  @Test
  public final void testCompleteNS3() {
    js.get().doTest("");
  }

  @Test
  public final void testCompleteNS4() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    js.get().doTest("");
    assertNotNull(myFixture.getLookupElements());
    assertEquals("MyNamespace", myFixture.getLookupElementStrings().get(0));
  }

  @Test
  public final void testCompleteType4() {
    js.get().doTest("");
    js.get().doTest("_2");
  }

  @Test
  public final void testCompleteType5() {
    js.get().doTest("");
  }

  @Test
  public final void testCompleteType6() {
    js.get().doTest("");
  }

  @Test
  public final void testCompleteType7() {
    js.get().doTest("");
  }

  @Test
  public final void testCompleteType7_2() {
    js.get().doTest("");
  }

  @Test
  public final void testCompleteType7_3() {
    js.get().doTest("");
  }

  @Test
  public final void testCompleteType7_4() {
    js.get().doTest("");
  }

  @Test
  public final void testCompleteType8() {
    js.get().doTest("");
  }

  @Test
  public final void testCompleteType9() {
    js.get().doTest("");
  }

  @Test
  public final void testCompleteFunInAddEventListener() {
    js.get().doTest("");
  }

  @Test
  public final void testCompleteAttributes() {
    js.get().doTest("");
  }

  @Test
  public final void testCompleteClassName() {
    js.get().doTest("");
  }

  @Test
  public final void testCompleteConstructorName() {
    js.get().doTest("");
  }

  @Test
  public final void testInsertImport() {
    final String testName = js.get().getTestName(false);
    js.get().doTestForFiles(testName + ".js2", testName + "_2.js2");
  }

  @Test
  public final void testInsertImport2() {
    js.get().doTest("");
  }

  @Test
  public final void testInsertImport3() {
    js.get().doTest("");
  }

  @Test
  public final void testInsertImportAmbiguous1() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    final LookupElement[] items = js.get().doTest("");
    assertQNames(items, "foo.ClassA", "bar.ClassA");
    EdtTestUtil.runInEdtAndWait(() -> myFixture.getLookup().setCurrentItem(items[1]));
    myFixture.type('\n');
    myFixture.checkResultByFile(js.get().getTestName(false) + "_after2.js2");
  }

  @Test
  public final void testInsertImportAmbiguous2() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    final LookupElement[] items = js.get().doTest("");
    assertQNames(items, "foo.ClassA", "bar.ClassA");
    EdtTestUtil.runInEdtAndWait(() -> myFixture.getLookup().setCurrentItem(items[1]));
    myFixture.type('\n');
    myFixture.checkResultByFile(js.get().getTestName(false) + "_after2.js2");
  }

  @Test
  public final void testInsertImportAmbiguous3() {
    js.get().doTest("");
  }

  @Test
  public final void testInsertImportAmbiguous4() {
    js.get().doTest("");
  }

  @Test
  public final void testInsertImportAmbiguous5() {
    js.get().doTest("");
  }

  @Test
  public final void testInsertImportAmbiguous6() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    final LookupElement[] items = js.get().doTest("");
    assertQNames(items, "ClassA", "bar.ClassA");
    EdtTestUtil.runInEdtAndWait(() -> myFixture.getLookup().setCurrentItem(items[0]));
    myFixture.checkResultByFile(js.get().getTestName(false) + "_after2.js2");
  }

  // Looks like tested functionality has never worked in run time, previously test passed because of a trick in base test class
  public final void _testInsertImportForStaticField() {
    final String testName = js.get().getTestName(false);
    js.get().doTestForFiles(testName + ".js2", testName + "_2.js2");
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testConstTypePickedUp() {
    final LookupElement[] items = js.get().doTest("");
    assertNotNull(items);
    assertEquals("addNamespace", items[0].getLookupString());
    assertEquals("appendChild", items[1].getLookupString());
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testConstTypePickedUp2() {
    final LookupElement[] items = js.get().doTest("");
    assertNotNull(items);
    assertEquals("attribute", items[0].getLookupString());
    assertEquals("attributes", items[1].getLookupString());
    assertEquals("child", items[2].getLookupString());
    assertEquals("children", items[3].getLookupString());
  }

  @Test
  public final void testConstrTypePickedUp() {
    js.get().doTest("");
  }

  @Test
  public final void testVarTypePickedUp3() {
    js.get().doTest("");
    assertNotNull(js.get().getFixture().getLookupElements());
  }

  @Test
  public final void testVarTypePickedUp4() {
    js.get().doTest("");
    assertNotNull(js.get().getFixture().getLookupElements());
  }

  @Test
  public final void testVarTypePickedUp5() {
    js.get().doTest("");
    assertNotNull(js.get().getFixture().getLookupElements());
  }

  @JSTestOptions(selectLookupItem = 0)
  @Test
  public final void testCompletePackage() {
    js.get().doTest("");
  }

  @Test
  public final void testCompletePackage2() {
    js.get().doTest("");
  }

  @Test
  public final void testCompletePackage3() {
    js.get().doTest("");
  }

  @Test
  public final void testCompletePackage4() {
    js.get().doTestForFiles(js.get().getTestName(false) + ".js2", js.get().getTestName(false) + "_2.js2");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public final void testCompleteType() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    setUpJdk();
    js.get().doTest("");
    assertEquals("*", myFixture.getLookupElements()[0].getLookupString());
    assertEquals("MyClass", myFixture.getLookupElements()[1].getLookupString());
    assertEquals("Array", myFixture.getLookupElements()[14].getLookupString());
    assertEquals("int", myFixture.getLookupElements()[58].getLookupString());
    assertTrue(myFixture.getLookupElements().length < 110);
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public final void testCompleteType2() {
    setUpJdk();
    js.get().doTest("");
  }

  @Test
  public final void testCompleteType3() {
    js.get().doTest("");
  }

  @Test
  public final void testCompleteTypeInNew() {
    js.get().doTestForFiles(js.get().getTestName(false) + ".js2", js.get().getTestName(false) + "_2.js2");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public final void testCompleteTypeInNew2() {
    setUpJdk();
    js.get().doTest("");
  }

  @Test
  public final void testCompleteBeforeLocalVar() {
    js.get().doTestForFiles(js.get().getTestName(false) + ".js2", js.get().getTestName(false) + "_2.js2");
  }

  @Test
  public final void testNoCompletionForDHTML() {
    js.get().doTest("", "js2");
    js.get().doTest("_2", "js2");
  }

  @Test
  public final void testNoCompletionForKeywordsInInlineMxml() {
    js.get().doTest("", "js2");
  }

  @Test
  public final void testECMACompletion() {
    js.get().doTest("", "js2");
  }

  @Test
  public final void testECMACompletion2() {
    js.get().doTest("", "js2");
    js.get().doTest("_2", "js2");
  }

  @Test
  public final void testCompleteVarInNsPlace() {
    js.get().doTest("", "js2");
    js.get().doTest("_2", "js2");
    js.get().doTest("_3", "js2");
  }

  @Test
  public final void testCompleteFunction() {
    js.get().doTest("", "js2");
  }

  @Test
  public final void testNoTypeAtNsPosition() {
    js.get().doTest("", "js2");
    js.get().doTest("_2", "js2");
    js.get().doTest("_3", "js2");
  }

  @Test
  public final void testNoKeywordAtPosition() {
    js.get().doTest("", "js2");
  }

  @Test
  public final void testNoKeywordAtPosition2() {
    js.get().doTest("", "js2");
  }

  @Test
  public final void testCompleteKeyword() {
    js.get().doTest("", "js2");
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testCompletionInXmlTag() {
    LookupElement[] items = js.get().doTest("");
    assertThat(items).isNullOrEmpty();
  }

  @Test
  public void testGenerics() {
    js.get().doTest("");
  }

  @Test
  public void testGenerics2() {
    js.get().doTest("");
  }

  @Test
  public void testGenerics2_2() {
    js.get().doTest("");
  }

  @Test
  public void testPrivateMethodCompletion() {
    js.get().doTest("");
  }

  @Test
  public void testPrivateMethodCompletion_2() {
    js.get().doTest("");
  }

  @Test
  public void testPrivateMethodCompletion_3() {
    js.get().doTest("");
  }

  @Test
  public void testProtectedMethodCompletion() {
    js.get().doTest("");
  }

  @Test
  public void testProtectedMethodCompletion_2() {
    js.get().doTest("");
  }

  @Test
  public void testProtectedMethodCompletion_3() {
    js.get().doTest("");
  }

  @Test
  public void testProtectedMethodCompletion_4() {
    js.get().doTest("");
  }

  @Test
  public void testCompleteInReferenceList() {
    js.get().doTest("");
  }

  @Test
  public void testCompleteInReferenceList_2() {
    js.get().doTest("");
  }

  @Test
  public void testCompleteInReferenceList_3() {
    js.get().doTest("");
  }

  @Test
  public void testCompleteInReferenceList_4() {
    js.get().doTest("");
  }

  @Test
  public void testCompleteInReferenceList_5() {
    js.get().doTest("");
  }

  @Test
  public void testStaticConstCompletion() {
    js.get().doTest("");
  }

  @Test
  public void testCompleteAfterCast() {
    js.get().doTest("");
  }

  @Test
  public void testInternalKeywordCompletion() {
    js.get().doTest("");
  }

  @Test
  public void testProtectedKeywordCompletion() {
    js.get().doTest("");
  }

  @JSTestOptions(selectLookupItem = 0)
  @Test
  public void testCompleteOverriddenName() {
    final LookupElement[] elements = js.get().doTest("");
    assertEquals(2, elements.length);
  }

  @Test
  public void testCompleteAfterUnknownVariable() {
    js.get().doTest("");
  }

  @Test
  public void testCompleteTopLevelPackage() {
    js.get().doTest("");
  }

  @Test
  public void testCompleteInsertProperty() {
    js.get().doTest("");
  }

  @Test
  public void testSkipClassWithExcludedAnnotation() {
    js.get().doTest("");
  }

  @Test
  public final void testJSDoc() {
    js.get().doTest("");
    js.get().doTest("_2");
  }

  @Test
  public void testSeeFunctionInSamePackage() {
    String prefix = js.get().getTestName(false);
    js.get().getFixture().configureByFiles(prefix + ".as", prefix + "_2.as", prefix + "_3.as");
    LookupElement[] elements = js.get().complete();
    assertNotNull(elements);
    assertEquals(2, elements.length);
    assertEquals("fooInPackage", elements[0].getLookupString());
    assertEquals("fooTopLevel", elements[1].getLookupString());
    js.get().selectItem(elements[0]);
    js.get().checkResultByFile(prefix + "_after.as");
  }

  @Test
  public void testSeeTopLevelFunction() {
    String name = js.get().getTestName(false);
    js.get().doTestForFiles(new String[]{name + ".as", name + "_2.as", name + "_3.as"}, "as");
  }

  @Test
  public final void testCompleteSkipsUnopenedNamespaces() {
    js.get().doTest("");
    js.get().doTest("_2");
    js.get().doTest("_3");
  }

  @Test
  public void testCompleteWithSemicolon() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    boolean old = CodeInsightSettings.getInstance().AUTOCOMPLETE_ON_CODE_COMPLETION;
    CodeInsightSettings.getInstance().AUTOCOMPLETE_ON_CODE_COMPLETION = false;
    try {
      final String testName = js.get().getTestName(false);

      myFixture.configureByFile(testName + ".js2");
      js.get().complete();
      EdtTestUtil.runInEdtAndWait(() -> myFixture.getLookup().setCurrentItem(myFixture.getLookupElements()[0]));
      myFixture.type(';');
      myFixture.checkResultByFile(testName + "_after.js2");
    }
    finally {
      CodeInsightSettings.getInstance().AUTOCOMPLETE_ON_CODE_COMPLETION = old;
    }
  }

  @Test
  public final void testIncludedMemberTypePickedUp() {
    js.get().doTestForFiles(js.get().getTestName(false) + ".js2", js.get().getTestName(false) + "_included.js2");
  }

  @JSTestOptions(selectLookupItem = 0)
  @Test
  public final void testSOE() {
    js.get().defaultTest();
  }

  @Test
  public final void testCompleteAmbiguousClass() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    js.get().doTest("");
    assertNotNull(myFixture.getLookupElements());
    assertEquals(2, myFixture.getLookupElements().length);
    assertLookupElement(myFixture.getLookupElements()[0], "ClassA", " (com.foo)", "CompleteAmbiguousClass.js2");
    assertLookupElement(myFixture.getLookupElements()[1], "ClassA", " (com.bar)", "CompleteAmbiguousClass.js2");

    EdtTestUtil.runInEdtAndWait(() -> myFixture.getLookup().setCurrentItem(myFixture.getLookupElements()[0]));
    myFixture.type('\n');
    myFixture.checkResultByFile(js.get().getTestName(false) + "_2_after.js2");
  }

  @Test
  public final void testCompleteStaticFunction() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    js.get().doTest("");
    assertNotNull(myFixture.getLookupElements());
    assertEquals(2, myFixture.getLookupElements().length);
    assertLookupElement(myFixture.getLookupElements()[0], "getXXX", "() (test)", "Class");
    assertLookupElement(myFixture.getLookupElements()[1], "getYYY", "() (test2)", "Class");
  }

  @Test
  public final void testCompleteStaticFunction2() {
    js.get().doTest("");
  }

  @Test
  public final void testCompleteClassWithConstructor() {
    js.get().doTest("");
  }

  @Test
  public void testNamesakeConstructors() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    js.get().doTest("");
    assertNotNull(myFixture.getLookupElements());
    assertEquals(2, myFixture.getLookupElements().length);
    assertEquals("Test", myFixture.getLookupElements()[0].getLookupString());
    assertEquals("Test", myFixture.getLookupElements()[1].getLookupString());
  }

  @Test
  public void testUnambiguousConstructor() {
    js.get().doTest("");
  }

  @Test
  public void testAmbiguousConstructor() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    js.get().doTest("");

    assertNotNull(myFixture.getLookupElements());
    assertEquals(2, myFixture.getLookupElements().length);
    assertLookupElement(myFixture.getLookupElements()[0], "MyClass", " (bar)", "AmbiguousConstructor.js2");
    assertLookupElement(myFixture.getLookupElements()[1], "MyClass", " (foo)", "AmbiguousConstructor.js2");

    EdtTestUtil.runInEdtAndWait(() -> myFixture.getLookup().setCurrentItem(myFixture.getLookupElements()[0]));
    myFixture.type('\n');
    myFixture.checkResultByFile(js.get().getTestName(false) + "_after2.js2");
  }

  @Test
  public void testStaticField() {
    js.get().doTest("");
  }

  @Test
  public void testParentClassField() {
    js.get().doTest("");
  }

  @Test
  public void testClassHierarchyMembersOrder() {
    final LookupElement[] lookupElements = js.get().doTest("");
    assertStartsWith(lookupElements, "zbb", "zzz", "zaa", "constructor");
    checkWeHaveInCompletion(lookupElements, "toSource");
  }

  @Test
  public void testClassHierarchyMembersOrder2() {
    final LookupElement[] lookupElements = js.get().doTest("");
    assertStartsWith(lookupElements, "e", "param");
    checkWeHaveInCompletion(lookupElements, "return", "Extended", "a", "Base", "zbb", "zzz", "Object", "zaa", "constructor");
    checkNoCompletion(lookupElements, "basePrivate");
  }

  @Test
  public void testUseKeyword() {
    js.get().doTest("");
  }

  private static void assertQNames(LookupElement[] items, String... qnames) {
    assertEquals(qnames.length, items.length);
    for (int i = 0; i < items.length; i++) {
      assertEquals(qnames[i], getQName(items[i]));
    }
  }

  private static String getQName(LookupElement item) {
    return ReadAction.computeBlocking(() -> ((JSQualifiedNamedElement)item.getPsiElement()).getQualifiedName());
  }

  @Test
  public final void testMoveCursorInsideConstructor() {
    js.get().doTest("");
  }

  @Test
  public final void testCompleteConst() {
    js.get().doTest("");
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public final void testEventName() {
    setUpJdk();
    js.get().doTest("");
    js.get().doTest("_2");
    js.get().doTest("_3");
    js.get().doTest("_4");
    js.get().doTest("_5");
  }

  @Test
  public void testConditionalCompileBlock() {
    js.get().doTest("");
  }

  @Test
  public void testStaticBlock() {
    js.get().doTest("");
  }

  @Test
  public void testStaticBlock2() {
    js.get().doTest("");
  }

  @Test
  public void testPrefix() {
    js.get().doTest("");
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion_2() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion_3() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion_4() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public final void testSmartCompletion2_2() {
    setUpJdk();
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public final void testSmartCompletion2_3() {
    // TODO consider parseInt() returning Number etc expect to appear. + 2_36 and 2_36_2. see JSTypeUtils.typeCanBeAssignedWithoutCoercion
    setUpJdk();
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_4() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_5() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_6() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_7() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_8() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_9() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_10() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_11() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_12() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_13() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_14() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_15() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_16() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_17() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_18() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_19() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_20() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    setUpJdk();
    LookupElement[] lookupElements = js.get().defaultTest();
    assertEquals(2, lookupElements.length);
    assertEquals("<Boolean>[]", lookupElements[0].getLookupString());
    EdtTestUtil.runInEdtAndWait(() -> myFixture.getLookup().setCurrentItem(lookupElements[1]));
    myFixture.type('\n');
    js.get().checkResultByFile("_2", "js2");
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_21() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_22() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_23() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_24() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_25() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_26() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_27() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_27_2() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_28() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_29() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public final void testSmartCompletion2_30() {
    LookupElement[] lookupElements = js.get().defaultTest();
    for (LookupElement l : lookupElements) {
      assertFalse("toString".equals(l.getLookupString()));
    }
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_31() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_32() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_33() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_34() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_35() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_36() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_36_2() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_37() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_38() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_38_2() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_38_3() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_39() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_40() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSmartCompletion2_41() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSuggestInterfaceAfterIs() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testVectorReverse() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testNoGetterSetterWhenFunExpectedType() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSuggestIntOrUintWhenNumberExpected() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testSuggestAnyInRestParameter() {
    js.get().defaultTest();
  }

  @Test
  public final void testCompleteFunName() {
    js.get().defaultTest();
  }

  @Test
  public final void testCompleteFunName2() {
    js.get().defaultTest();
  }

  @Test
  public final void testNoComplete2() {
    js.get().defaultTest();
  }

  @Test
  public final void testCompleteClassBeforeVar() {
    js.get().defaultTest();
  }

  @Test
  public final void testNoCompleteFinalClassInExtends() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testCompleteInNew() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testNoInheritor() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testVectorAfterNew() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testNoJs() {
    String testName = js.get().getTestName(false);
    js.get().doTestForFiles(new String[] {testName + ".js2", testName + ".js"}, "", "js2");
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testCompleteInRHSOfAsBinOp() {
    String testName = js.get().getTestName(false);
    js.get().doTestForFiles(new String[] {testName + ".js2", testName + "_2.js2"}, "", "js2");
  }

  @Test
  public final void testCompleteStaticConst() {
    js.get().defaultTest();
  }

  @Test
  public final void testCompleteNoDuplicates() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testCompleteInCase() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testNoInaccessibleCompletion() {
    String testName = js.get().getTestName(false);
    js.get().doTestForFiles(new String[] {testName + ".js2", testName + "_2.js2"}, "", "js2");
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testNoInaccessibleCompletion2() {
    js.get().defaultTest();
  }

  @Test
  public final void testNoKeywordsInLiteral() { js.get().defaultTest(); }

  @Test
  public final void testNoKeywordsInLiteral_2() { js.get().defaultTest(); }

  @Test
  public final void testNoKeywordsInComments() { js.get().defaultTest(); }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testNoParens() { js.get().defaultTest(); }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testCompleteTypeInAs() {
    js.get().defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testCompleteWithTab() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    LookupElement[] lookupElements = js.get().defaultTest();
    EdtTestUtil.runInEdtAndWait(() -> myFixture.getLookup().setCurrentItem(lookupElements[0]));
    myFixture.type(Lookup.REPLACE_SELECT_CHAR);
    myFixture.checkResultByFile(js.get().getTestName(false) + "_after2.js2");
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testCompleteWithCompleteStatement() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    LookupElement[] lookupElements = js.get().defaultTest();
    EdtTestUtil.runInEdtAndWait(() -> myFixture.getLookup().setCurrentItem(lookupElements[0]));
    myFixture.type(Lookup.COMPLETE_STATEMENT_SELECT_CHAR);
    myFixture.checkResultByFile(js.get().getTestName(false) + "_after2.js2");
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testCompleteWithCompleteStatement2() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    LookupElement[] lookupElements = js.get().defaultTest();
    EdtTestUtil.runInEdtAndWait(() -> myFixture.getLookup().setCurrentItem(lookupElements[0]));
    myFixture.type(Lookup.COMPLETE_STATEMENT_SELECT_CHAR);
    myFixture.checkResultByFile(js.get().getTestName(false) + "_after2.js2");
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testCompleteWithCompleteStatement2_2() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    LookupElement[] lookupElements = js.get().defaultTest();
    EdtTestUtil.runInEdtAndWait(() -> myFixture.getLookup().setCurrentItem(lookupElements[0]));
    myFixture.type(Lookup.COMPLETE_STATEMENT_SELECT_CHAR);
    myFixture.checkResultByFile(js.get().getTestName(false) + "_after2.js2");
  }

  @Test
  public final void testCompleteNearParen() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    LookupElement[] lookupElements = js.get().defaultTest();
    EdtTestUtil.runInEdtAndWait(() -> myFixture.getLookup().setCurrentItem(lookupElements[0]));
    myFixture.type('\n');
    myFixture.checkResultByFile(js.get().getTestName(false) + "_after2.js2");
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public final void testDetectExprType() { js.get().defaultTest(); }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public final void testQualifiedReference() {
    setUpJdk();
    js.get().defaultTest();
  }

  @Test
  public void testTwoSdks() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    final Sdk sdk45 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.5"), null, true, myFixture.getTestRootDisposable());
    final Sdk sdk46 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.6"), null, false, myFixture.getTestRootDisposable());
    FlexTestUtils.modifyConfigs(js.get().getProject(), editor -> {
      ModifiableFlexBuildConfiguration bc1 = editor.getConfigurations(js.get().getFixture().getModule())[0];
      bc1.setName("1");
      FlexTestUtils.setSdk(bc1, sdk45);
      ModifiableFlexBuildConfiguration bc2 = editor.createConfiguration(js.get().getFixture().getModule());
      bc2.setName("2");
      FlexTestUtils.setSdk(bc2, sdk46);
    });
    final FlexBuildConfigurationManager m = FlexBuildConfigurationManager.getInstance(js.get().getFixture().getModule());

    class TestZZ implements ThrowableRunnable<Exception> {
      private final String myBcName;
      private final String myExpectedTypeText;

      TestZZ(final String bcName, final String expectedTypeText) {
        myBcName = bcName;
        myExpectedTypeText = expectedTypeText;
      }

      @Override
      public void run() {
        EdtTestUtil.runInEdtAndWait(() -> m.setActiveBuildConfiguration(m.findConfigurationByName(myBcName)));
        js.get().defaultTest();
        for (LookupElement item : myFixture.getLookupElements()) {
          final TestLookupElementPresentation p = TestLookupElementPresentation.renderReal(item);
          assertEquals(myExpectedTypeText, p.getTypeText());
        }
      }
    }

    new TestZZ("1", sdk45.getName()).run();
    new TestZZ("2", sdk46.getName()).run();
    new TestZZ("1", sdk45.getName()).run();
  }

  @Test
  public void testSameParameterName() {
    LookupElement[] lookupElements = js.get().doTest("", "js2");
    assertStartsWith(lookupElements, "x", "a");
  }

  @Test
  public void testSameParameterName2() {
    String testName = js.get().getTestName(false);
    final LookupElement[] lookupElements = js.get().doTestForFiles(new String[] {testName + ".js2", testName + "_2.js2"}, "", "js2");
    assertStartsWith(lookupElements, "x", "a");
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @Test
  public void testSameParameterName3() {
    final LookupElement[] lookupElements = js.get().defaultTest();
    assertStartsWith(lookupElements, "x");
    checkNoCompletion(lookupElements, "a");
  }

  @Test
  public void testTypeContext() {
    final LookupElement[] lookupElements = js.get().defaultTest();
    assertStartsWith(lookupElements, "W", "Z");
    checkWeHaveInCompletion(lookupElements, "A", "Array");
  }

  @Test
  public void testNullInIfStatement() {
    final LookupElement[] lookupElements = js.get().defaultTest();
    checkWeHaveInCompletion(lookupElements, "null", "true", "false", "this");
    checkNoCompletion(lookupElements, "function", "interface", "extends", "static", "var", "const", "break", "try", "return", "in", "throw",
                      "void", "int");
  }

  @Test
  public void testFinalClass() {
    final LookupElement[] lookupElements = js.get().defaultTest();
    checkWeHaveInCompletion(lookupElements, "final");
  }

  @Test
  public void testNoKeywordsInImportStatement() {
    final LookupElement[] lookupElements = js.get().defaultTest();
    checkNoCompletion(lookupElements, "function", "interface", "extends", "static", "var", "const", "break", "try", "return", "in", "throw",
                      "void", "use", "package", "override");
  }

  @Test
  public void testNoKeywordsInReturnType() {
    final LookupElement[] lookupElements = js.get().defaultTest();
    checkNoCompletion(lookupElements, "function", "interface", "extends", "static", "var", "const", "break", "try", "return", "in", "throw",
                      "use", "package", "override");
  }

  @NeedsJavaModule
  @Test
  public void testObjectVariants() {
    final LookupElement[] elements = js.get().defaultTest();
    checkWeHaveInCompletion(elements, "hasOwnProperty", "length", "isPrototypeOf", "toString");
  }

  @Test
  public void testRestartOnTypingIfOverflow() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    LookupElement[] lookupElements = js.get().defaultTest();
    checkNoCompletion(lookupElements, "Array"); // there are so many local classes that there's no place for Array within the limit
    myFixture.type('r');
    lookupElements = myFixture.getLookupElements();
    checkWeHaveInCompletion(lookupElements, "Array");
  }

  @Test
  public final void testAnnotatedWithObjectGetsAnyCompletion() {
    String [] vFiles = new String[]{
      js.get().getTestName(false) + ".js2"
    };
    js.get().doTestForFiles(vFiles, "", "js2");
  }
}
