// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.completion;

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.completion.impl.CamelHumpMatcher;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.flex.editor.FlexProjectDescriptor;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javaee.ExternalResourceManagerExImpl;
import com.intellij.lang.javascript.BaseJSCompletionTestCase;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSAttributeImpl;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.TestLookupElementPresentation;
import com.intellij.util.Consumer;
import com.intellij.util.ThrowableRunnable;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;

@SuppressWarnings({"ALL"})
public class ActionScriptCompletionTest extends BaseJSCompletionTestCase {
  protected static final String BASE_PATH = "/js2_completion/";
  public static final LightProjectDescriptor JAVA_PROJECT_DESCRIPTOR = new LightProjectDescriptor() {
    @NotNull
    @Override
    public ModuleType getModuleType() {
      return StdModuleTypes.JAVA;
    }
  };

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

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD})
  public @interface NeedsJavaModule {
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
      FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), myFixture.getTestRootDisposable());
    }
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return needsJavaModule() ?
           JAVA_PROJECT_DESCRIPTOR :
           FlexProjectDescriptor.DESCRIPTOR;
  }

  public final void testBasic() throws Exception {
    doTest("");
    doTest("_2");
    doTest("_3");
    doTest("_4");
  }

  public final void testBoldForClassMembers() throws Exception {
    LookupElement[] elements = doTest("");
    assertNotNull(elements);
    assertEquals("____", elements[0].getLookupString());

    assertTrue(getBoldStatus(elements[0]));
    assertFalse(getBoldStatus(elements[1]));

    elements = doTest("_2");
    assertNotNull(elements);
    assertEquals("____", elements[0].getLookupString());
    assertTrue(getBoldStatus(elements[0]));
    assertFalse(getBoldStatus(elements[1]));

    elements = doTest("_3");
    assertNotNull(elements);
    assertEquals("____", elements[3].getLookupString());
    assertTrue(getBoldStatus(elements[3]));
    assertEquals("lVar", elements[0].getLookupString());
    assertTrue(getBoldStatus(elements[0]));
    assertEquals("param", elements[1].getLookupString());
    assertTrue(getBoldStatus(elements[1]));

    assertTrue(!getBoldStatus(elements[4]));
    assertEquals("Bar", elements[4].getLookupString());
  }

  public final void testKeywords() throws Exception {
    doTest("");
  }

  public final void testKeywords2() throws Exception {
    final LookupElement[] elements = doTest("");
    checkWeHaveInCompletion(elements, "return", "function", "if");
  }

  @JSTestOptions(selectLookupItem = 0)
  public final void testKeywordsInContext() throws Exception {
    final LookupElement[] lookupElements = doTestForFiles(getTestName(false) + ".js2", getTestName(false) + "_2.js2");
    assertEquals("extends", lookupElements[0].getLookupString());
    assertTrue(getBoldStatus(lookupElements[0]));
    assertTrue("Test expected to have other options for completion", lookupElements.length > 1);
  }

  public final void testKeywordsInContext2() throws Exception {
    final LookupElement[] lookupElements = doTestForFiles(getTestName(false) + ".js2", getTestName(false) + "_2.js2");
    assertEquals("public", lookupElements[0].getLookupString());
    assertTrue(getBoldStatus(lookupElements[0]));
    assertTrue("Test expected to have other options for completion", lookupElements.length > 1);
  }

  public void testKeywordsInContext3() throws Exception {
    final LookupElement[] lookupElements = defaultTest();
    assertStartsWith(lookupElements, "zzz");
    checkWeHaveInCompletion(lookupElements, "true", "null", "new");
  }

  public void testKeywordsInContext4() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = JSTestOption.WithLoadingAndSavingCaches)
  public final void testVarTypePickedUp() throws Exception {
    doTest("");
    doTest("_2");
  }

  @JSTestOptions(value = JSTestOption.WithLoadingAndSavingCaches)
  public final void testVarTypePickedUp2() throws Exception {
    doTestForFiles(getTestName(false) + ".js2", getTestName(false) + "_2.js2");
  }

  public final void testCustomMeta() throws Exception {
    String testName = getTestName(false);
    myFixture.configureByFiles(testName + ".js2", testName + ".dtd");
    final VirtualFile relativeFile = VfsUtil.findRelativeFile(testName + ".dtd", myFixture.getFile().getVirtualFile());
    ExternalResourceManagerExImpl.registerResourceTemporarily(JSAttributeImpl.URN_FLEX_META, relativeFile.getPath(), getTestRootDisposable());

    complete();
    checkResultByFile("", getExtension());

    myFixture.configureByFiles(testName + "2.js2");
    complete();
    checkResultByFile("2", getExtension());
  }

  public final void testCompleteAttrName() throws Exception {
    defaultTest();
  }

  public final void testFuncTypePickedUp() throws Exception {
    doTest("");
  }

  public final void testFuncTypePickedUp2() throws Exception {
    doTest("");
  }

  public final void testFuncTypePickedUp3() throws Exception {
    doTestForFiles(getTestName(false) + ".js2", getTestName(false) + "_2.js2");
  }

  public final void testFuncTypePickedUp4() throws Exception {
    doTest("");
  }

  public final void testFuncTypePickedUp5() throws Exception {
    doTest("");
  }

  public final void testCompleteAfterThis() throws Exception {
    doTest("");
  }

  public final void testCompleteAfterThis2() throws Exception {
    doTest("");
    mySmartCompletionTests.remove(getTestName(false));
    FileDocumentManager.getInstance().saveAllDocuments();
    doTest("");
  }

  public final void testCompleteAfterThis3() throws Exception {
    doTest("");
  }

  public final void testCompleteAfterType() throws Exception {
    doTest("");
  }

  public final void testCompleteAfterThis4() throws Exception {
    doTest("");
  }

  public final void testCompleteAfterSuper() throws Exception {
    doTest("");
  }

  public final void testCompleteWithoutQualifier() throws Exception {
    doTest("");
    doTest("_2");
    doTest("_3");
  }

  public final void testNoComplete() throws Exception {
    doTest("");
  }

  public final void testCompleteNS() throws Exception {
    doTest("");
  }

  public final void testCompleteNS2() throws Exception {
    doTest("");
  }

  public final void testCompleteNS3() throws Exception {
    doTest("");
  }

  public final void testCompleteNS4() throws Exception {
    doTest("");
    assertNotNull(myFixture.getLookupElements());
    assertEquals("MyNamespace", myFixture.getLookupElementStrings().get(0));
  }

  public final void testCompleteType4() throws Exception {
    doTest("");
    doTest("_2");
  }

  public final void testCompleteType5() throws Exception {
    doTest("");
  }

  public final void testCompleteType6() throws Exception {
    doTest("");
  }

  public final void testCompleteType7() throws Exception {
    doTest("");
  }

  public final void testCompleteType7_2() throws Exception {
    doTest("");
  }

  public final void testCompleteType7_3() throws Exception {
    doTest("");
  }

  public final void testCompleteType7_4() throws Exception {
    doTest("");
  }

  public final void testCompleteType8() throws Exception {
    doTest("");
  }

  public final void testCompleteType9() throws Exception {
    doTest("");
  }

  public final void testCompleteFunInAddEventListener() throws Exception {
    doTest("");
  }

  public final void testCompleteAttributes() throws Exception {
    doTest("");
  }

  public final void testCompleteClassName() throws Exception {
    doTest("");
  }

  public final void testCompleteConstructorName() throws Exception {
    doTest("");
  }

  public final void testInsertImport() throws Exception {
    final String testName = getTestName(false);
    doTestForFiles(testName + ".js2", testName + "_2.js2");
  }

  public final void testInsertImport2() throws Exception {
    doTest("");
  }

  public final void testInsertImport3() throws Exception {
    doTest("");
  }

  public final void testInsertImportAmbiguous1() throws Exception {
    final LookupElement[] items = doTest("");
    assertQNames(items, "bar.ClassA", "foo.ClassA");
    myFixture.getLookup().setCurrentItem(items[0]);
    myFixture.type('\n');
    myFixture.checkResultByFile(getTestName(false) + "_after2.js2");
  }

  public final void testInsertImportAmbiguous2() throws Exception {
    final LookupElement[] items = doTest("");
    assertQNames(items, "bar.ClassA", "foo.ClassA");
    myFixture.getLookup().setCurrentItem(items[0]);
    myFixture.type('\n');
    myFixture.checkResultByFile(getTestName(false) + "_after2.js2");
  }

  public final void testInsertImportAmbiguous3() throws Exception {
    doTest("");
  }

  public final void testInsertImportAmbiguous4() throws Exception {
    doTest("");
  }

  public final void testInsertImportAmbiguous5() throws Exception {
    doTest("");
  }

  public final void testInsertImportAmbiguous6() throws Exception {
    final LookupElement[] items = doTest("");
    assertQNames(items, "ClassA", "bar.ClassA");
    myFixture.getLookup().setCurrentItem(items[0]);
    myFixture.checkResultByFile(getTestName(false) + "_after2.js2");
  }

  // Looks like tested functionality has never worked in run time, previously test passed because of a trick in base test class
  public final void _testInsertImportForStaticField() throws Exception {
    final String testName = getTestName(false);
    doTestForFiles(testName + ".js2", testName + "_2.js2");
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testConstTypePickedUp() throws Exception {
    final LookupElement[] items = doTest("");
    assertNotNull(items);
    assertEquals("addNamespace", items[0].getLookupString());
    assertEquals("appendChild", items[1].getLookupString());
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testConstTypePickedUp2() throws Exception {
    final LookupElement[] items = doTest("");
    assertNotNull(items);
    assertEquals("attribute", items[0].getLookupString());
    assertEquals("attributes", items[1].getLookupString());
    assertEquals("child", items[2].getLookupString());
    assertEquals("children", items[3].getLookupString());
  }

  public final void testConstrTypePickedUp() throws Exception {
    doTest("");
  }

  public final void testVarTypePickedUp3() throws Exception {
    doTest("");
    assertNotNull(myFixture.getLookupElements());
  }

  public final void testVarTypePickedUp4() throws Exception {
    doTest("");
    assertNotNull(myFixture.getLookupElements());
  }

  public final void testVarTypePickedUp5() throws Exception {
    doTest("");
    assertNotNull(myFixture.getLookupElements());
  }

  @JSTestOptions(selectLookupItem = 0)
  public final void testCompletePackage() throws Exception {
    doTest("");
  }

  public final void testCompletePackage2() throws Exception {
    doTest("");
  }

  public final void testCompletePackage3() throws Exception {
    doTest("");
  }

  @JSTestOptions(value = JSTestOption.WithLoadingAndSavingCaches)
  public final void testCompletePackage4() throws Exception {
    doTestForFiles(getTestName(false) + ".js2", getTestName(false) + "_2.js2");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public final void testCompleteType() throws Exception {
    setUpJdk();
    doTest("");
    assertEquals("*", myFixture.getLookupElements()[0].getLookupString());
    assertEquals("MyClass", myFixture.getLookupElements()[1].getLookupString());
    assertEquals("Array", myFixture.getLookupElements()[14].getLookupString());
    assertEquals("int", myFixture.getLookupElements()[58].getLookupString());
    assertTrue(myFixture.getLookupElements().length < 110);
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public final void testCompleteType2() throws Exception {
    setUpJdk();
    doTest("");
  }

  public final void testCompleteType3() throws Exception {
    doTest("");
  }

  public final void testCompleteTypeInNew() throws Exception {
    doTestForFiles(getTestName(false) + ".js2", getTestName(false) + "_2.js2");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public final void testCompleteTypeInNew2() throws Exception {
    setUpJdk();
    doTest("");
  }

  public final void testCompleteBeforeLocalVar() throws Exception {
    doTestForFiles(getTestName(false) + ".js2", getTestName(false) + "_2.js2");
  }

  public final void testNoCompletionForDHTML() throws Exception {
    doTest("", "js2");
    doTest("_2", "js2");
  }

  public final void testNoCompletionForKeywordsInInlineMxml() throws Exception {
    doTest("", "js2");
  }

  public final void testECMACompletion() throws Exception {
    doTest("", "js2");
  }

  public final void testECMACompletion2() throws Exception {
    doTest("", "js2");
    doTest("_2", "js2");
  }

  public final void testCompleteVarInNsPlace() throws Exception {
    doTest("", "js2");
    doTest("_2", "js2");
    doTest("_3", "js2");
  }

  public final void testCompleteFunction() throws Exception {
    doTest("", "js2");
  }

  public final void testNoTypeAtNsPosition() throws Exception {
    doTest("", "js2");
    doTest("_2", "js2");
    doTest("_3", "js2");
  }

  public final void testNoKeywordAtPosition() throws Exception {
    doTest("", "js2");
  }

  public final void testNoKeywordAtPosition2() throws Exception {
    doTest("", "js2");
  }

  public final void testCompleteKeyword() throws Exception {
    doTest("", "js2");
  }

  @JSTestOptions(JSTestOption.WithSmartCompletion)
  public final void testCompletionInXmlTag() throws Exception {
    LookupElement[] items = doTest("");
    assertEmpty(items);
  }

  public void testGenerics() throws Exception {
    doTest("");
  }

  public void testGenerics2() throws Exception {
    doTest("");
  }

  public void testGenerics2_2() throws Exception {
    doTest("");
  }

  public void testPrivateMethodCompletion() throws Exception {
    doTest("");
  }

  public void testPrivateMethodCompletion_2() throws Exception {
    doTest("");
  }

  public void testPrivateMethodCompletion_3() throws Exception {
    doTest("");
  }

  public void testProtectedMethodCompletion() throws Exception {
    doTest("");
  }

  public void testProtectedMethodCompletion_2() throws Exception {
    doTest("");
  }

  public void testProtectedMethodCompletion_3() throws Exception {
    doTest("");
  }

  public void testProtectedMethodCompletion_4() throws Exception {
    doTest("");
  }

  public void testCompleteInReferenceList() throws Exception {
    doTest("");
  }

  public void testCompleteInReferenceList_2() throws Exception {
    doTest("");
  }

  public void testCompleteInReferenceList_3() throws Exception {
    doTest("");
  }

  public void testCompleteInReferenceList_4() throws Exception {
    doTest("");
  }

  public void testCompleteInReferenceList_5() throws Exception {
    doTest("");
  }

  public void testStaticConstCompletion() throws Exception {
    doTest("");
  }

  public void testCompleteAfterCast() throws Exception {
    doTest("");
  }

  public void testInternalKeywordCompletion() throws Exception {
    doTest("");
  }

  public void testProtectedKeywordCompletion() throws Exception {
    doTest("");
  }

  @JSTestOptions(selectLookupItem = 0)
  public void testCompleteOverriddenName() throws Exception {
    final LookupElement[] elements = doTest("");
    assertEquals(2, elements.length);
  }

  @NeedsJavaModule
  public void testCompleteStyleNameInString() throws Exception {
    final String base = getTestName(false);
    doTestForFiles(base + ".js2", base + "_2.js2");
  }

  public void testCompleteAfterUnknownVariable() throws Exception {
    doTest("");
  }

  public void testCompleteTopLevelPackage() throws Exception {
    doTest("");
  }

  public void testCompleteInsertProperty() throws Exception {
    doTest("");
  }

  public void testSkipClassWithExcludedAnnotation() throws Exception {
    doTest("");
  }

  public final void testJSDoc() throws Exception {
    doTest("");
    doTest("_2");
  }

  public final void testCompleteSkipsUnopenedNamespaces() throws Exception {
    doTest("");
    doTest("_2");
    doTest("_3");
  }

  public void testCompleteWithSemicolon() throws Exception {
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

  public final void testIncludedMemberTypePickedUp() throws Exception {
    doTestForFiles(getTestName(false) + ".js2", getTestName(false) + "_included.js2");
  }

  @JSTestOptions(selectLookupItem = 0)
  public final void testSOE() throws Exception {
    defaultTest();
  }

  public final void testCompleteAmbiguousClass() throws Exception {
    doTest("");
    assertNotNull(myFixture.getLookupElements());
    assertEquals(2, myFixture.getLookupElements().length);
    assertLookupElement(myFixture.getLookupElements()[0], "ClassA", " (com.bar)", "CompleteAmbiguousClass.js2");
    assertLookupElement(myFixture.getLookupElements()[1], "ClassA", " (com.foo)", "CompleteAmbiguousClass.js2");

    myFixture.getLookup().setCurrentItem(myFixture.getLookupElements()[1]);
    myFixture.type('\n');
    myFixture.checkResultByFile(getTestName(false) + "_2_after.js2");
  }

  public final void testCompleteStaticFunction() throws Exception {
    doTest("");
    assertNotNull(myFixture.getLookupElements());
    assertEquals(2, myFixture.getLookupElements().length);
    assertLookupElement(myFixture.getLookupElements()[0], "getXXX", "() (test)", "Class");
    assertLookupElement(myFixture.getLookupElements()[1], "getYYY", "() (test2)", "Class");
  }

  public final void testCompleteStaticFunction2() throws Exception {
    doTest("");
  }

  public final void testCompleteClassWithConstructor() throws Exception {
    doTest("");
  }

  public void testNamesakeConstructors() throws Exception {
    doTest("");
    assertNotNull(myFixture.getLookupElements());
    assertEquals(2, myFixture.getLookupElements().length);
    assertEquals("Test", myFixture.getLookupElements()[0].getLookupString());
    assertEquals("Test", myFixture.getLookupElements()[1].getLookupString());
  }

  public void testUnambiguousConstructor() throws Exception {
    doTest("");
  }

  public void testAmbiguousConstructor() throws Exception {
    doTest("");

    assertNotNull(myFixture.getLookupElements());
    assertEquals(2, myFixture.getLookupElements().length);
    assertLookupElement(myFixture.getLookupElements()[0], "MyClass", " (bar)", "AmbiguousConstructor.js2");
    assertLookupElement(myFixture.getLookupElements()[1], "MyClass", " (foo)", "AmbiguousConstructor.js2");

    myFixture.getLookup().setCurrentItem(myFixture.getLookupElements()[0]);
    myFixture.type('\n');
    myFixture.checkResultByFile(getTestName(false) + "_after2.js2");
  }

  public void testStaticField() throws Exception {
    doTest("");
  }

  public void testParentClassField() throws Exception {
    doTest("");
  }

  public void testClassHierarchyMembersOrder() throws Exception {
    final LookupElement[] lookupElements = doTest("");
    assertStartsWith(lookupElements, "zbb", "zzz", "zaa", "constructor");
    checkWeHaveInCompletion(lookupElements, "toSource");
  }

  public void testClassHierarchyMembersOrder2() throws Exception {
    final LookupElement[] lookupElements = doTest("");
    assertStartsWith(lookupElements, "e", "param", "Extended", "a", "Base", "zbb", "zzz", "Object", "zaa", "constructor");
  }

  public void testUseKeyword() throws Exception {
    doTest("");
  }

  @Override
  protected String getExtension() {
    return "js2";
  }

  protected String getBasePath() {
    return BASE_PATH;
  }

  private void assertQNames(LookupElement[] items, String... qnames) {
    assertEquals(qnames.length, items.length);
    for (int i = 0; i < items.length; i++) {
      assertEquals(qnames[i], getQName(items[i]));
    }
  }

  private String getQName(LookupElement item) {
    return ((JSQualifiedNamedElement)item.getObject()).getQualifiedName();
  }

  public final void testMoveCursorInsideConstructor() throws Exception {
    doTest("");
  }

  public final void testCompleteConst() throws Exception {
    doTest("");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithSmartCompletion})
  public final void testEventName() throws Exception {
    setUpJdk();
    doTest("");
    doTest("_2");
    doTest("_3");
    doTest("_4");
    doTest("_5");
  }

  public void testConditionalCompileBlock() throws Exception {
    doTest("");
  }

  public void testStaticBlock() throws Exception {
    doTest("");
  }

  public void testStaticBlock2() throws Exception {
    doTest("");
  }

  public void testPrefix() throws Exception {
    doTest("");
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion_2() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion_3() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion_4() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion, JSTestOption.WithFlexFacet})
  public final void testSmartCompletion2_2() throws Exception {
    setUpJdk();
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_3() throws Exception {
    // TODO consider parseInt() returning Number etc expect to appear. + 2_36 and 2_36_2. see JSTypeUtils.typeCanBeAssignedWithoutCoercion
    setUpJdk();
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_4() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_5() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_6() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_7() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_8() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_9() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_10() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_11() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_12() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_13() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_14() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_15() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_16() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_17() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_18() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_19() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_20() throws Exception {
    setUpJdk();
    LookupElement[] lookupElements = defaultTest();
    assertEquals(2, lookupElements.length);
    assertEquals("<Boolean>[]", lookupElements[0].getLookupString());
    myFixture.getLookup().setCurrentItem(lookupElements[1]);
    myFixture.type('\n');
    checkResultByFile("_2", "js2");
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_21() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_22() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_23() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_24() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_25() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_26() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_27() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_27_2() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_28() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_29() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion, JSTestOption.WithFlexSdk})
  public final void testSmartCompletion2_30() throws Exception {
    LookupElement[] lookupElements = defaultTest();
    for (LookupElement l : lookupElements) {
      assertFalse("toString".equals(l.getLookupString()));
    }
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_31() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_32() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_33() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_34() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_35() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_36() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_36_2() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_37() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_38() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_38_2() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_38_3() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_39() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_40() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSmartCompletion2_41() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSuggestInterfaceAfterIs() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testVectorReverse() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testNoGetterSetterWhenFunExpectedType() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSuggestIntOrUintWhenNumberExpected() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testSuggestAnyInRestParameter() throws Exception {
    defaultTest();
  }

  public final void testCompleteFunName() throws Exception {
    defaultTest();
  }

  public final void testCompleteFunName2() throws Exception {
    defaultTest();
  }

  public final void testNoComplete2() throws Exception {
    defaultTest();
  }

  public final void testCompleteClassBeforeVar() throws Exception {
    defaultTest();
  }

  public final void testNoCompleteFinalClassInExtends() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testCompleteInNew() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testNoInheritor() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testVectorAfterNew() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testNoJs() throws Exception {
    String testName = getTestName(false);
    doTestForFiles(new String[] {testName + ".js2", testName + ".js"}, "", "js2");
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testCompleteInRHSOfAsBinOp() throws Exception {
    String testName = getTestName(false);
    doTestForFiles(new String[] {testName + ".js2", testName + "_2.js2"}, "", "js2");
  }

  public final void testCompleteStaticConst() throws Exception {
    defaultTest();
  }

  public final void testCompleteNoDuplicates() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testCompleteInCase() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testNoInaccessibleCompletion() throws Exception {
    String testName = getTestName(false);
    doTestForFiles(new String[] {testName + ".js2", testName + "_2.js2"}, "", "js2");
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testNoInaccessibleCompletion2() throws Exception {
    defaultTest();
  }

  public final void testNoKeywordsInLiteral() throws Exception { defaultTest(); }

  public final void testNoKeywordsInLiteral_2() throws Exception { defaultTest(); }

  public final void testNoKeywordsInComments() throws Exception { defaultTest(); }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testNoParens() throws Exception { defaultTest(); }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testCompleteTypeInAs() throws Exception {
    defaultTest();
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testCompleteWithTab() throws Exception {
    LookupElement[] lookupElements = defaultTest();
    myFixture.getLookup().setCurrentItem(lookupElements[0]);
    myFixture.type(Lookup.REPLACE_SELECT_CHAR);
    myFixture.checkResultByFile(getTestName(false) + "_after2.js2");
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testCompleteWithCompleteStatement() throws Exception {
    LookupElement[] lookupElements = defaultTest();
    myFixture.getLookup().setCurrentItem(lookupElements[0]);
    myFixture.type(Lookup.COMPLETE_STATEMENT_SELECT_CHAR);
    myFixture.checkResultByFile(getTestName(false) + "_after2.js2");
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testCompleteWithCompleteStatement2() throws Exception {
    LookupElement[] lookupElements = defaultTest();
    myFixture.getLookup().setCurrentItem(lookupElements[0]);
    myFixture.type(Lookup.COMPLETE_STATEMENT_SELECT_CHAR);
    myFixture.checkResultByFile(getTestName(false) + "_after2.js2");
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testCompleteWithCompleteStatement2_2() throws Exception {
    LookupElement[] lookupElements = defaultTest();
    myFixture.getLookup().setCurrentItem(lookupElements[0]);
    myFixture.type(Lookup.COMPLETE_STATEMENT_SELECT_CHAR);
    myFixture.checkResultByFile(getTestName(false) + "_after2.js2");
  }

  public final void testCompleteNearParen() throws Exception {
    LookupElement[] lookupElements = defaultTest();
    myFixture.getLookup().setCurrentItem(lookupElements[0]);
    myFixture.type('\n');
    myFixture.checkResultByFile(getTestName(false) + "_after2.js2");
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public final void testDetectExprType() throws Exception { defaultTest(); }

  @JSTestOptions(value = {JSTestOption.WithFlexSdk})
  public final void testQualifiedReference() throws Exception {
    setUpJdk();
    defaultTest();
  }

  public void testTwoSdks() throws Exception {
    final Sdk sdk45 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.5"), null, true, myFixture.getTestRootDisposable());
    final Sdk sdk46 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.6"), null, false, myFixture.getTestRootDisposable());
    FlexTestUtils.modifyConfigs(getProject(), new Consumer<FlexProjectConfigurationEditor>() {
      @Override
      public void consume(final FlexProjectConfigurationEditor editor) {
        ModifiableFlexBuildConfiguration bc1 = editor.getConfigurations(myModule)[0];
        bc1.setName("1");
        FlexTestUtils.setSdk(bc1, sdk45);
        ModifiableFlexBuildConfiguration bc2 = editor.createConfiguration(myModule);
        bc2.setName("2");
        FlexTestUtils.setSdk(bc2, sdk46);
      }
    });
    final FlexBuildConfigurationManager m = FlexBuildConfigurationManager.getInstance(myModule);

    class TestZZ implements ThrowableRunnable<Exception> {
      private final String myBcName;
      private final String myExpectedTypeText;

      TestZZ(final String bcName, final String expectedTypeText) {
        myBcName = bcName;
        myExpectedTypeText = expectedTypeText;
      }

      @Override
      public void run() throws Exception {
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

  public void testSameParameterName() throws Exception {
    LookupElement[] lookupElements = doTest("", "js2");
    assertStartsWith(lookupElements, "x", "a");
  }

  public void testSameParameterName2() throws Exception {
    String testName = getTestName(false);
    final LookupElement[] lookupElements = doTestForFiles(new String[] {testName + ".js2", testName + "_2.js2"}, "", "js2");
    assertStartsWith(lookupElements, "x", "a");
  }

  @JSTestOptions(value = {JSTestOption.WithSmartCompletion})
  public void testSameParameterName3() throws Exception {
    final LookupElement[] lookupElements = defaultTest();
    assertStartsWith(lookupElements, "x");
    checkNoCompletion(lookupElements, "a");
  }

  public void testTypeContext() throws Exception {
    final LookupElement[] lookupElements = defaultTest();
    assertStartsWith(lookupElements, "W", "Z");
    checkWeHaveInCompletion(lookupElements, "A", "Array");
  }

  public void testNullInIfStatement() throws Exception {
    final LookupElement[] lookupElements = defaultTest();
    checkWeHaveInCompletion(lookupElements, "null", "true", "false", "this");
    checkNoCompletion(lookupElements, "function", "interface", "extends", "static", "var", "const", "break", "try", "return", "in", "throw",
                      "void", "int");
  }

  public void testFinalClass() throws Exception {
    final LookupElement[] lookupElements = defaultTest();
    checkWeHaveInCompletion(lookupElements, "final");
  }

  public void testNoKeywordsInImportStatement() throws Exception {
    final LookupElement[] lookupElements = defaultTest();
    checkNoCompletion(lookupElements, "function", "interface", "extends", "static", "var", "const", "break", "try", "return", "in", "throw",
                      "void", "use", "package", "override");
  }

  public void testNoKeywordsInReturnType() throws Exception {
    final LookupElement[] lookupElements = defaultTest();
    checkNoCompletion(lookupElements, "function", "interface", "extends", "static", "var", "const", "break", "try", "return", "in", "throw",
                      "use", "package", "override");
  }

  @NeedsJavaModule
  public void testObjectVariants() throws Exception {
    final LookupElement[] elements = defaultTest();
    checkWeHaveInCompletion(elements, "hasOwnProperty", "length", "isPrototypeOf", "toString");
  }
}
