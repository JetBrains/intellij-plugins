// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.completion;

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.completion.impl.CamelHumpMatcher;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.editor.FlexProjectDescriptor;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javaee.ExternalResourceManagerExImpl;
import com.intellij.lang.javascript.BaseJSCompletionTestCase;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexBuildConfiguration;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSAttributeImpl;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.TestLookupElementPresentation;
import com.intellij.util.ThrowableRunnable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;

public class ActionScriptCompletionTest extends BaseJSCompletionTestCase {
  protected static final String BASE_PATH = "/js2_completion/";
  {
    mySmartCompletionTests.addAll(Arrays.asList(
      "VarTypePickedUp", "VarTypePickedUp2", "FuncTypePickedUp", "FuncTypePickedUp2", "FuncTypePickedUp3", "CompleteAfterThis",
      "CompleteAfterThis2", "CompleteAfterSuper", "ConstrTypePickedUp", "CompleteWithoutQualifier"
    ));

    myTestsWithSaveAndLoadCaches.addAll(Arrays.asList(
      "FuncTypePickedUp", "FuncTypePickedUp2", "FuncTypePickedUp3", "ConstTypePickedUp",
      "ConstTypePickedUp2", "VarTypePickedUp3", "VarTypePickedUp4", "VarTypePickedUp5"
    ));
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    FlexTestUtils.allowFlexVfsRootsFor(myFixture.getTestRootDisposable(), "");
    CamelHumpMatcher.forceStartMatching(myFixture.getTestRootDisposable());
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      FlexTestUtils.modifyConfigs(getProject(), editor -> {
        if (ModuleType.get(getModule()) == FlexModuleType.getInstance()) {
          for (ModifiableFlexBuildConfiguration bc : editor.getConfigurations(getModule())) {
            bc.getDependencies().setSdkEntry(null);
          }
        }
      });
    }
    catch (Throwable e) {
      addSuppressedException(e);
    }
    finally {
      super.tearDown();
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  @interface NeedsJavaModule {
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath(getBasePath());
  }

  private boolean needsJavaModule() {
    return JSTestUtils.getTestMethod(getClass(), getTestName(false)).getAnnotation(NeedsJavaModule.class) != null;
  }

  protected void setUpJdk() {
    if (!needsJavaModule()) {
      FlexTestUtils.setupFlexSdk(getModule(), getTestName(false), getClass(), myFixture.getTestRootDisposable());
    }
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return needsJavaModule() ?
           LightJavaCodeInsightFixtureTestCase.JAVA_1_7 :
           FlexProjectDescriptor.DESCRIPTOR;
  }

  public final void testBasic() {
    doTest("");
    doTest("_2");
    doTest("_3");
    doTest("_4");
  }

  public final void testBoldForClassMembers() {
    LookupElement[] elements = doTest("");
    assertNotNull(elements);
    assertEquals("____", elements[0].getLookupString());

    assertTrue(isStrictMatched(elements[0]));
    assertTrue(isStrictMatched(elements[1]));

    elements = doTest("_2");
    assertNotNull(elements);
    assertEquals("____", elements[0].getLookupString());
    assertTrue(isStrictMatched(elements[0]));
    assertTrue(isStrictMatched(elements[1]));

    elements = doTest("_3");
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

  public final void testKeywords() {
    doTest("");
  }

  public final void testKeywords2() {
    final LookupElement[] elements = doTest("");
    checkWeHaveInCompletion(elements, "return", "function", "if");
  }

  @JSTestOptions(selectLookupItem = 0)
  public final void testKeywordsInContext() {
    final LookupElement[] lookupElements = doTestForFiles(getTestName(false) + ".js2", getTestName(false) + "_2.js2");
    assertEquals("extends", lookupElements[0].getLookupString());
    assertTrue(isStrictMatched(lookupElements[0]));
    assertTrue("Test expected to have other options for completion", lookupElements.length > 1);
  }

  public final void testKeywordsInContext2() {
    final LookupElement[] lookupElements = doTestForFiles(getTestName(false) + ".js2", getTestName(false) + "_2.js2");
    assertEquals("public", lookupElements[0].getLookupString());
    assertTrue(isStrictMatched(lookupElements[0]));
    assertTrue("Test expected to have other options for completion", lookupElements.length > 1);
  }

  public void testKeywordsInContext3() {
    final LookupElement[] lookupElements = defaultTest();
    assertStartsWith(lookupElements, "zzz");
    checkWeHaveInCompletion(lookupElements, "true", "null", "new");
  }

  @JSTestOptions(selectLookupItem = 0)
  public void testKeywordsInContext4() {
    defaultTest();
  }

  public final void testVarTypePickedUp() {
    doTest("");
    doTest("_2");
  }

  public final void testVarTypePickedUp2() {
    doTestForFiles(getTestName(false) + ".js2", getTestName(false) + "_2.js2");
  }

  public final void testCustomMeta() {
    String testName = getTestName(false);
    myFixture.configureByFiles(testName + ".js2", testName + ".dtd");
    final VirtualFile relativeFile = VfsUtilCore.findRelativeFile(testName + ".dtd", myFixture.getFile().getVirtualFile());
    ExternalResourceManagerExImpl.registerResourceTemporarily(JSAttributeImpl.URN_FLEX_META, relativeFile.getPath(), getTestRootDisposable());

    complete();
    checkResultByFile("", getExtension());

    myFixture.configureByFiles(testName + "2.js2");
    complete();
    checkResultByFile("2", getExtension());
  }

  public final void testCompleteAttrName() {
    defaultTest();
  }

  public final void testFuncTypePickedUp() {
    doTest("");
  }

  public final void testFuncTypePickedUp2() {
    doTest("");
  }

  public final void testFuncTypePickedUp3() {
    doTestForFiles(getTestName(false) + ".js2", getTestName(false) + "_2.js2");
  }

  public final void testFuncTypePickedUp4() {
    doTest("");
  }

  public final void testFuncTypePickedUp5() {
    doTest("");
  }

  public final void testCompleteAfterThis() {
    doTest("");
  }

  public final void testCompleteAfterThis2() {
    doTest("");
    mySmartCompletionTests.remove(getTestName(false));
    FileDocumentManager.getInstance().saveAllDocuments();
    doTest("");
  }

  public final void testCompleteAfterThis3() {
    doTest("");
  }

  public final void testCompleteAfterType() {
    doTest("");
  }

  public final void testCompleteAfterThis4() {
    doTest("");
  }

  public final void testCompleteAfterSuper() {
    doTest("");
  }

  public final void testCompleteWithoutQualifier() {
    doTest("");
    doTest("_2");
    doTest("_3");
  }

  public final void testNoComplete() {
    doTest("");
  }

  public final void testCompleteNS() {
    doTest("");
  }

  public final void testCompleteNS2() {
    doTest("");
  }

  public final void testCompleteNS3() {
    doTest("");
  }

  public final void testCompleteNS4() {
    doTest("");
    assertNotNull(myFixture.getLookupElements());
    assertEquals("MyNamespace", myFixture.getLookupElementStrings().get(0));
  }

  public final void testCompleteType4() {
    doTest("");
    doTest("_2");
  }

  public final void testCompleteType5() {
    doTest("");
  }

  public final void testCompleteType6() {
    doTest("");
  }

  public final void testCompleteType7() {
    doTest("");
  }

  public final void testCompleteType7_2() {
    doTest("");
  }

  public final void testCompleteType7_3() {
    doTest("");
  }

  public final void testCompleteType7_4() {
    doTest("");
  }

  public final void testCompleteType8() {
    doTest("");
  }

  public final void testCompleteType9() {
    doTest("");
  }

  public final void testCompleteFunInAddEventListener() {
    doTest("");
  }

  public final void testCompleteAttributes() {
    doTest("");
  }

  public final void testCompleteClassName() {
    doTest("");
  }

  public final void testCompleteConstructorName() {
    doTest("");
  }

  public final void testInsertImport() {
    final String testName = getTestName(false);
    doTestForFiles(testName + ".js2", testName + "_2.js2");
  }

  public final void testInsertImport2() {
    doTest("");
  }

  public final void testInsertImport3() {
    doTest("");
  }

  public final void testInsertImportAmbiguous1() {
    final LookupElement[] items = doTest("");
    assertQNames(items, "foo.ClassA", "bar.ClassA");
    myFixture.getLookup().setCurrentItem(items[1]);
    myFixture.type('\n');
    myFixture.checkResultByFile(getTestName(false) + "_after2.js2");
  }

  public final void testInsertImportAmbiguous2() {
    final LookupElement[] items = doTest("");
    assertQNames(items, "foo.ClassA", "bar.ClassA");
    myFixture.getLookup().setCurrentItem(items[1]);
    myFixture.type('\n');
    myFixture.checkResultByFile(getTestName(false) + "_after2.js2");
  }

  public final void testInsertImportAmbiguous3() {
    doTest("");
  }

  public final void testInsertImportAmbiguous4() {
    doTest("");
  }

  public final void testInsertImportAmbiguous5() {
    doTest("");
  }

  public final void testInsertImportAmbiguous6() {
    final LookupElement[] items = doTest("");
    assertQNames(items, "ClassA", "bar.ClassA");
    myFixture.getLookup().setCurrentItem(items[0]);
    myFixture.checkResultByFile(getTestName(false) + "_after2.js2");
  }

  // Looks like tested functionality has never worked in run time, previously test passed because of a trick in base test class
  public final void _testInsertImportForStaticField() {
    final String testName = getTestName(false);
    doTestForFiles(testName + ".js2", testName + "_2.js2");
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testConstTypePickedUp() {
    final LookupElement[] items = doTest("");
    assertNotNull(items);
    assertEquals("addNamespace", items[0].getLookupString());
    assertEquals("appendChild", items[1].getLookupString());
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testConstTypePickedUp2() {
    final LookupElement[] items = doTest("");
    assertNotNull(items);
    assertEquals("attribute", items[0].getLookupString());
    assertEquals("attributes", items[1].getLookupString());
    assertEquals("child", items[2].getLookupString());
    assertEquals("children", items[3].getLookupString());
  }

  public final void testConstrTypePickedUp() {
    doTest("");
  }

  public final void testVarTypePickedUp3() {
    doTest("");
    assertNotNull(myFixture.getLookupElements());
  }

  public final void testVarTypePickedUp4() {
    doTest("");
    assertNotNull(myFixture.getLookupElements());
  }

  public final void testVarTypePickedUp5() {
    doTest("");
    assertNotNull(myFixture.getLookupElements());
  }

  @JSTestOptions(selectLookupItem = 0)
  public final void testCompletePackage() {
    doTest("");
  }

  public final void testCompletePackage2() {
    doTest("");
  }

  public final void testCompletePackage3() {
    doTest("");
  }

  public final void testCompletePackage4() {
    doTestForFiles(getTestName(false) + ".js2", getTestName(false) + "_2.js2");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public final void testCompleteType() {
    setUpJdk();
    doTest("");
    assertEquals("*", myFixture.getLookupElements()[0].getLookupString());
    assertEquals("MyClass", myFixture.getLookupElements()[1].getLookupString());
    assertEquals("Array", myFixture.getLookupElements()[14].getLookupString());
    assertEquals("int", myFixture.getLookupElements()[58].getLookupString());
    assertTrue(myFixture.getLookupElements().length < 110);
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public final void testCompleteType2() {
    setUpJdk();
    doTest("");
  }

  public final void testCompleteType3() {
    doTest("");
  }

  public final void testCompleteTypeInNew() {
    doTestForFiles(getTestName(false) + ".js2", getTestName(false) + "_2.js2");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public final void testCompleteTypeInNew2() {
    setUpJdk();
    doTest("");
  }

  public final void testCompleteBeforeLocalVar() {
    doTestForFiles(getTestName(false) + ".js2", getTestName(false) + "_2.js2");
  }

  public final void testNoCompletionForDHTML() {
    doTest("", "js2");
    doTest("_2", "js2");
  }

  public final void testNoCompletionForKeywordsInInlineMxml() {
    doTest("", "js2");
  }

  public final void testECMACompletion() {
    doTest("", "js2");
  }

  public final void testECMACompletion2() {
    doTest("", "js2");
    doTest("_2", "js2");
  }

  public final void testCompleteVarInNsPlace() {
    doTest("", "js2");
    doTest("_2", "js2");
    doTest("_3", "js2");
  }

  public final void testCompleteFunction() {
    doTest("", "js2");
  }

  public final void testNoTypeAtNsPosition() {
    doTest("", "js2");
    doTest("_2", "js2");
    doTest("_3", "js2");
  }

  public final void testNoKeywordAtPosition() {
    doTest("", "js2");
  }

  public final void testNoKeywordAtPosition2() {
    doTest("", "js2");
  }

  public final void testCompleteKeyword() {
    doTest("", "js2");
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testCompletionInXmlTag() {
    LookupElement[] items = doTest("");
    assertEmpty(items);
  }

  public void testGenerics() {
    doTest("");
  }

  public void testGenerics2() {
    doTest("");
  }

  public void testGenerics2_2() {
    doTest("");
  }

  public void testPrivateMethodCompletion() {
    doTest("");
  }

  public void testPrivateMethodCompletion_2() {
    doTest("");
  }

  public void testPrivateMethodCompletion_3() {
    doTest("");
  }

  public void testProtectedMethodCompletion() {
    doTest("");
  }

  public void testProtectedMethodCompletion_2() {
    doTest("");
  }

  public void testProtectedMethodCompletion_3() {
    doTest("");
  }

  public void testProtectedMethodCompletion_4() {
    doTest("");
  }

  public void testCompleteInReferenceList() {
    doTest("");
  }

  public void testCompleteInReferenceList_2() {
    doTest("");
  }

  public void testCompleteInReferenceList_3() {
    doTest("");
  }

  public void testCompleteInReferenceList_4() {
    doTest("");
  }

  public void testCompleteInReferenceList_5() {
    doTest("");
  }

  public void testStaticConstCompletion() {
    doTest("");
  }

  public void testCompleteAfterCast() {
    doTest("");
  }

  public void testInternalKeywordCompletion() {
    doTest("");
  }

  public void testProtectedKeywordCompletion() {
    doTest("");
  }

  @JSTestOptions(selectLookupItem = 0)
  public void testCompleteOverriddenName() {
    final LookupElement[] elements = doTest("");
    assertEquals(2, elements.length);
  }

  public void testCompleteAfterUnknownVariable() {
    doTest("");
  }

  public void testCompleteTopLevelPackage() {
    doTest("");
  }

  public void testCompleteInsertProperty() {
    doTest("");
  }

  public void testSkipClassWithExcludedAnnotation() {
    doTest("");
  }

  public final void testJSDoc() {
    doTest("");
    doTest("_2");
  }

  public final void testCompleteSkipsUnopenedNamespaces() {
    doTest("");
    doTest("_2");
    doTest("_3");
  }

  public void testCompleteWithSemicolon() {
    boolean old = CodeInsightSettings.getInstance().AUTOCOMPLETE_ON_CODE_COMPLETION;
    CodeInsightSettings.getInstance().AUTOCOMPLETE_ON_CODE_COMPLETION = false;
    try {
      final String testName = getTestName(false);

      myFixture.configureByFile(testName + ".js2");
      complete();
      myFixture.getLookup().setCurrentItem(myFixture.getLookupElements()[0]);
      myFixture.type(';');
      myFixture.checkResultByFile(testName + "_after.js2");
    }
    finally {
      CodeInsightSettings.getInstance().AUTOCOMPLETE_ON_CODE_COMPLETION = old;
    }
  }

  public final void testIncludedMemberTypePickedUp() {
    doTestForFiles(getTestName(false) + ".js2", getTestName(false) + "_included.js2");
  }

  @JSTestOptions(selectLookupItem = 0)
  public final void testSOE() {
    defaultTest();
  }

  public final void testCompleteAmbiguousClass() {
    doTest("");
    assertNotNull(myFixture.getLookupElements());
    assertEquals(2, myFixture.getLookupElements().length);
    assertLookupElement(myFixture.getLookupElements()[0], "ClassA", " (com.foo)", "CompleteAmbiguousClass.js2");
    assertLookupElement(myFixture.getLookupElements()[1], "ClassA", " (com.bar)", "CompleteAmbiguousClass.js2");

    myFixture.getLookup().setCurrentItem(myFixture.getLookupElements()[0]);
    myFixture.type('\n');
    myFixture.checkResultByFile(getTestName(false) + "_2_after.js2");
  }

  public final void testCompleteStaticFunction() {
    doTest("");
    assertNotNull(myFixture.getLookupElements());
    assertEquals(2, myFixture.getLookupElements().length);
    assertLookupElement(myFixture.getLookupElements()[0], "getXXX", "() (test)", "Class");
    assertLookupElement(myFixture.getLookupElements()[1], "getYYY", "() (test2)", "Class");
  }

  public final void testCompleteStaticFunction2() {
    doTest("");
  }

  public final void testCompleteClassWithConstructor() {
    doTest("");
  }

  public void testNamesakeConstructors() {
    doTest("");
    assertNotNull(myFixture.getLookupElements());
    assertEquals(2, myFixture.getLookupElements().length);
    assertEquals("Test", myFixture.getLookupElements()[0].getLookupString());
    assertEquals("Test", myFixture.getLookupElements()[1].getLookupString());
  }

  public void testUnambiguousConstructor() {
    doTest("");
  }

  public void testAmbiguousConstructor() {
    doTest("");

    assertNotNull(myFixture.getLookupElements());
    assertEquals(2, myFixture.getLookupElements().length);
    assertLookupElement(myFixture.getLookupElements()[0], "MyClass", " (bar)", "AmbiguousConstructor.js2");
    assertLookupElement(myFixture.getLookupElements()[1], "MyClass", " (foo)", "AmbiguousConstructor.js2");

    myFixture.getLookup().setCurrentItem(myFixture.getLookupElements()[0]);
    myFixture.type('\n');
    myFixture.checkResultByFile(getTestName(false) + "_after2.js2");
  }

  public void testStaticField() {
    doTest("");
  }

  public void testParentClassField() {
    doTest("");
  }

  public void testClassHierarchyMembersOrder() {
    final LookupElement[] lookupElements = doTest("");
    assertStartsWith(lookupElements, "zbb", "zzz", "zaa", "constructor");
    checkWeHaveInCompletion(lookupElements, "toSource");
  }

  public void testClassHierarchyMembersOrder2() {
    final LookupElement[] lookupElements = doTest("");
    assertStartsWith(lookupElements, "e", "param", "return", "Extended", "a", "Base", "zbb", "zzz", "Object", "zaa", "constructor");
  }

  public void testUseKeyword() {
    doTest("");
  }

  @Override
  protected String getExtension() {
    return "js2";
  }

  @Override
  protected String getBasePath() {
    return BASE_PATH;
  }

  private static void assertQNames(LookupElement[] items, String... qnames) {
    assertEquals(qnames.length, items.length);
    for (int i = 0; i < items.length; i++) {
      assertEquals(qnames[i], getQName(items[i]));
    }
  }

  private static String getQName(LookupElement item) {
    return ((JSQualifiedNamedElement)item.getPsiElement()).getQualifiedName();
  }

  public final void testMoveCursorInsideConstructor() {
    doTest("");
  }

  public final void testCompleteConst() {
    doTest("");
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public final void testEventName() {
    setUpJdk();
    doTest("");
    doTest("_2");
    doTest("_3");
    doTest("_4");
    doTest("_5");
  }

  public void testConditionalCompileBlock() {
    doTest("");
  }

  public void testStaticBlock() {
    doTest("");
  }

  public void testStaticBlock2() {
    doTest("");
  }

  public void testPrefix() {
    doTest("");
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion_2() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion_3() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion_4() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public final void testSmartCompletion2_2() {
    setUpJdk();
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public final void testSmartCompletion2_3() {
    // TODO consider parseInt() returning Number etc expect to appear. + 2_36 and 2_36_2. see JSTypeUtils.typeCanBeAssignedWithoutCoercion
    setUpJdk();
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_4() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_5() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_6() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_7() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_8() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_9() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_10() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_11() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_12() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_13() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_14() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_15() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_16() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_17() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_18() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_19() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_20() {
    setUpJdk();
    LookupElement[] lookupElements = defaultTest();
    assertEquals(2, lookupElements.length);
    assertEquals("<Boolean>[]", lookupElements[0].getLookupString());
    myFixture.getLookup().setCurrentItem(lookupElements[1]);
    myFixture.type('\n');
    checkResultByFile("_2", "js2");
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_21() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_22() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_23() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_24() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_25() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_26() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_27() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_27_2() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_28() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_29() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public final void testSmartCompletion2_30() {
    LookupElement[] lookupElements = defaultTest();
    for (LookupElement l : lookupElements) {
      assertFalse("toString".equals(l.getLookupString()));
    }
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_31() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_32() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_33() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_34() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_35() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_36() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_36_2() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_37() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_38() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_38_2() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_38_3() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_39() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_40() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSmartCompletion2_41() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSuggestInterfaceAfterIs() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testVectorReverse() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testNoGetterSetterWhenFunExpectedType() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSuggestIntOrUintWhenNumberExpected() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testSuggestAnyInRestParameter() {
    defaultTest();
  }

  public final void testCompleteFunName() {
    defaultTest();
  }

  public final void testCompleteFunName2() {
    defaultTest();
  }

  public final void testNoComplete2() {
    defaultTest();
  }

  public final void testCompleteClassBeforeVar() {
    defaultTest();
  }

  public final void testNoCompleteFinalClassInExtends() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testCompleteInNew() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testNoInheritor() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testVectorAfterNew() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testNoJs() {
    String testName = getTestName(false);
    doTestForFiles(new String[] {testName + ".js2", testName + ".js"}, "", "js2");
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testCompleteInRHSOfAsBinOp() {
    String testName = getTestName(false);
    doTestForFiles(new String[] {testName + ".js2", testName + "_2.js2"}, "", "js2");
  }

  public final void testCompleteStaticConst() {
    defaultTest();
  }

  public final void testCompleteNoDuplicates() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testCompleteInCase() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testNoInaccessibleCompletion() {
    String testName = getTestName(false);
    doTestForFiles(new String[] {testName + ".js2", testName + "_2.js2"}, "", "js2");
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testNoInaccessibleCompletion2() {
    defaultTest();
  }

  public final void testNoKeywordsInLiteral() { defaultTest(); }

  public final void testNoKeywordsInLiteral_2() { defaultTest(); }

  public final void testNoKeywordsInComments() { defaultTest(); }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testNoParens() { defaultTest(); }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testCompleteTypeInAs() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testCompleteWithTab() {
    LookupElement[] lookupElements = defaultTest();
    myFixture.getLookup().setCurrentItem(lookupElements[0]);
    myFixture.type(Lookup.REPLACE_SELECT_CHAR);
    myFixture.checkResultByFile(getTestName(false) + "_after2.js2");
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testCompleteWithCompleteStatement() {
    LookupElement[] lookupElements = defaultTest();
    myFixture.getLookup().setCurrentItem(lookupElements[0]);
    myFixture.type(Lookup.COMPLETE_STATEMENT_SELECT_CHAR);
    myFixture.checkResultByFile(getTestName(false) + "_after2.js2");
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testCompleteWithCompleteStatement2() {
    LookupElement[] lookupElements = defaultTest();
    myFixture.getLookup().setCurrentItem(lookupElements[0]);
    myFixture.type(Lookup.COMPLETE_STATEMENT_SELECT_CHAR);
    myFixture.checkResultByFile(getTestName(false) + "_after2.js2");
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testCompleteWithCompleteStatement2_2() {
    LookupElement[] lookupElements = defaultTest();
    myFixture.getLookup().setCurrentItem(lookupElements[0]);
    myFixture.type(Lookup.COMPLETE_STATEMENT_SELECT_CHAR);
    myFixture.checkResultByFile(getTestName(false) + "_after2.js2");
  }

  public final void testCompleteNearParen() {
    LookupElement[] lookupElements = defaultTest();
    myFixture.getLookup().setCurrentItem(lookupElements[0]);
    myFixture.type('\n');
    myFixture.checkResultByFile(getTestName(false) + "_after2.js2");
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testDetectExprType() { defaultTest(); }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public final void testQualifiedReference() {
    setUpJdk();
    defaultTest();
  }

  public void testTwoSdks() {
    final Sdk sdk45 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.5"), null, true, myFixture.getTestRootDisposable());
    final Sdk sdk46 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.6"), null, false, myFixture.getTestRootDisposable());
    FlexTestUtils.modifyConfigs(getProject(), editor -> {
      ModifiableFlexBuildConfiguration bc1 = editor.getConfigurations(getModule())[0];
      bc1.setName("1");
      FlexTestUtils.setSdk(bc1, sdk45);
      ModifiableFlexBuildConfiguration bc2 = editor.createConfiguration(getModule());
      bc2.setName("2");
      FlexTestUtils.setSdk(bc2, sdk46);
    });
    final FlexBuildConfigurationManager m = FlexBuildConfigurationManager.getInstance(getModule());

    class TestZZ implements ThrowableRunnable<Exception> {
      private final String myBcName;
      private final String myExpectedTypeText;

      TestZZ(final String bcName, final String expectedTypeText) {
        myBcName = bcName;
        myExpectedTypeText = expectedTypeText;
      }

      @Override
      public void run() {
        m.setActiveBuildConfiguration(m.findConfigurationByName(myBcName));
        defaultTest();
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

  public void testSameParameterName() {
    LookupElement[] lookupElements = doTest("", "js2");
    assertStartsWith(lookupElements, "x", "a");
  }

  public void testSameParameterName2() {
    String testName = getTestName(false);
    final LookupElement[] lookupElements = doTestForFiles(new String[] {testName + ".js2", testName + "_2.js2"}, "", "js2");
    assertStartsWith(lookupElements, "x", "a");
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public void testSameParameterName3() {
    final LookupElement[] lookupElements = defaultTest();
    assertStartsWith(lookupElements, "x");
    checkNoCompletion(lookupElements, "a");
  }

  public void testTypeContext() {
    final LookupElement[] lookupElements = defaultTest();
    assertStartsWith(lookupElements, "W", "Z");
    checkWeHaveInCompletion(lookupElements, "A", "Array");
  }

  public void testNullInIfStatement() {
    final LookupElement[] lookupElements = defaultTest();
    checkWeHaveInCompletion(lookupElements, "null", "true", "false", "this");
    checkNoCompletion(lookupElements, "function", "interface", "extends", "static", "var", "const", "break", "try", "return", "in", "throw",
                      "void", "int");
  }

  public void testFinalClass() {
    final LookupElement[] lookupElements = defaultTest();
    checkWeHaveInCompletion(lookupElements, "final");
  }

  public void testNoKeywordsInImportStatement() {
    final LookupElement[] lookupElements = defaultTest();
    checkNoCompletion(lookupElements, "function", "interface", "extends", "static", "var", "const", "break", "try", "return", "in", "throw",
                      "void", "use", "package", "override");
  }

  public void testNoKeywordsInReturnType() {
    final LookupElement[] lookupElements = defaultTest();
    checkNoCompletion(lookupElements, "function", "interface", "extends", "static", "var", "const", "break", "try", "return", "in", "throw",
                      "use", "package", "override");
  }

  @NeedsJavaModule
  public void testObjectVariants() {
    final LookupElement[] elements = defaultTest();
    checkWeHaveInCompletion(elements, "hasOwnProperty", "length", "isPrototypeOf", "toString");
  }

  public void testRestartOnTypingIfOverflow() {
    LookupElement[] lookupElements = defaultTest();
    checkNoCompletion(lookupElements, "Array"); // there are so many local classes that there's no place for Array within the limit
    myFixture.type('r');
    lookupElements = myFixture.getLookupElements();
    checkWeHaveInCompletion(lookupElements, "Array");
  }

  public final void testAnnotatedWithObjectGetsAnyCompletion() {
    String [] vFiles = new String[]{
      getTestName(false) + ".js2"
    };
    doTestForFiles(vFiles, "", "js2");
  }
}
