// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.codeInsight;

import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.editor.FlexProjectDescriptor;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.lang.javascript.JSAbstractDocumentationTest;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeNameValuePair;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.ex.http.HttpFileSystem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.descriptor.CssPropertyDescriptorStub;
import com.intellij.psi.css.impl.util.CssDocumentationProvider;
import com.intellij.testFramework.LightProjectDescriptor;

public class FlexDocumentationTest extends JSAbstractDocumentationTest {
  private static final String BASE_PATH = "as_documentation/";

  @Override
  protected String getBasePath() {
    return BASE_PATH;
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @Override
  protected String getExtension() {
    return "js2";
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return FlexProjectDescriptor.DESCRIPTOR;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    FlexTestUtils.allowFlexVfsRootsFor(myFixture.getTestRootDisposable(), "");
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(getModule(), getTestName(false), getClass(), myFixture.getTestRootDisposable());
  }

  private PsiElement getDocElementForLookupItem(DocumentationProvider provider, String fileName) {
    myFixture.configureByFile(BASE_PATH + fileName);
    PsiElement originalElement = myFixture.getFile().findElementAt(myFixture.getEditor().getCaretModel().getOffset());
    return provider.getDocumentationElementForLookupItem(getPsiManager(), originalElement.getText(), originalElement);
  }

  public void testJSDocs6() {
    defaultTest();
  }

  public void testJSDocs14() {
    defaultTest();
  }

  public void testJSDocs15() {
    defaultTest();
  }

  public void testJSDocs25() {
    defaultTest();
  }

  public void testJSDocs31() {
    defaultTest();
  }

  public void testJSDocs32() {
    defaultTest();
  }

  public void testAsSimpleDocs() {
    doTestWithLinkNavigationCheck("as", 1);
  }

  public void testAsSimpleDocs2() {
    doTest(getTestName(false), "as");
  }

  public void testAsSimpleDocs3() {
    doTest(getTestName(false), "as");
  }

  public void testAsSimpleDocs4() {
    doTest(getTestName(false), "as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testMxmlDoc() {
    setUpJdk();
    doTest(getTestName(false), "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testMxmlDoc2() {
    setUpJdk();
    doTest(getTestName(false), "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testMxmlDoc3() {
    setUpJdk();
    doTest(getTestName(false), "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testMxmlDoc4() {
    setUpJdk();
    doTest(getTestName(false), "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testMxmlDoc5() {
    PsiElement element = getDocElementForLookupItem(new CssDocumentationProvider(), getTestName(false) + ".mxml");
    assertNull(element);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testMxmlDoc6() {
    setUpJdk();
    doTest(getTestName(false), "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testMxmlDoc7() {
    doTest(getTestName(false), "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testFlexCssSelector() {
    setUpJdk();
    DocumentationProvider cssDocumentationProvider = new CssDocumentationProvider();
    PsiElement docElement = getDocElementForLookupItem(cssDocumentationProvider, getTestName(false) + ".css");
    assertInstanceOf(docElement, JSClass.class);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testFlexCssSelectorMultiDocumentation() {
    testWithLibrary("MyLib1.swc", "MyLib1_src.zip", null, () -> doTest(getTestName(false), "css"));
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testFlexCssSelectorMultiDocumentationInLookup() {
    testWithLibrary("MyLib1.swc", "MyLib1_src.zip", null, () -> {
      String doc = testOne(getTestName(false) + ".css");
      assertTrue(doc.contains("p1.MyClass"));
      assertTrue(doc.contains("p2.MyClass"));
    });
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testFlexMetadataStyle() {
    String testName = getTestName(false);
    String s = BASE_PATH + testName;
    doTest(new String[]{s + ".css", s + ".mxml"}, testName, false, Check.Content);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testFlexMetadataStyleMultidoc() {
    String testName = getTestName(false);
    String s = BASE_PATH + testName;
    doTest(new String[]{s + ".css", s + "1.mxml", s + "2.mxml"}, testName, false, Check.Content);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testFlexCssProperty() {
    setUpJdk();
    DocumentationProvider cssDocumentationProvider = new CssDocumentationProvider();
    PsiElement docElement = getDocElementForLookupItem(cssDocumentationProvider, getTestName(false) + ".css");
    assertInstanceOf(docElement, JSAttributeNameValuePair.class);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testFlexCssPropertyMultiDocumentation() {
    setUpJdk();
    doTest(getTestName(false), "css");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testFlexCssPropertyMultiDocumentationInLookup() {
    setUpJdk();
    DocumentationProvider cssDocProvider = new CssDocumentationProvider();
    String doc = testOne(getTestName(false) + ".css", new CssPropertyDescriptorStub("borderColor"));
    assertNotNull(doc);
    assertTrue("Container's borderColor property missing", doc.contains("Container"));
    assertTrue("Button's borderColor property missing", doc.contains("Button"));
    assertTrue("UIComponent's borderColor property missing", doc.contains("UIComponent"));
  }

  public void testQuickNavigateInfo() {
    doQuickNavigateTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testQuickNavigateInfoWithMxml() {
    setUpJdk();
    doQuickNavigateTest(getTestName(false), "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testQuickNavigateInfoWithMxml2() {
    doQuickNavigateTest(getTestName(false), "mxml");
  }

  public void testQuickNavigateInfo_2() {
    doQuickNavigateTest();
  }

  public void testQuickNavigateInfo_3() {
    doQuickNavigateTest();
  }

  public void testQuickNavigateInfo_4() {
    doQuickNavigateTest();
  }

  public void testQuickNavigateInfo_5() {
    doQuickNavigateTest();
  }

  public void testQuickNavigateInfo_5_2() {
    doQuickNavigateTest();
  }

  public void testQuickNavigateInfo_6() {
    doQuickNavigateTest();
  }

  public void testQuickNavigateInfo_7() {
    doQuickNavigateTest();
  }

  public void testQuickNavigateInfo_9() {
    doQuickNavigateTest();
  }

  public void testQuickNavigateInfo_10() {
    doQuickNavigateTest();
  }

  public void testQuickNavigateInfo_11() {
    doQuickNavigateTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testSeeAlso() {
    setUpJdk();
    doTestWithLinkNavigationCheck("as", 4);
  }

  public void testWithLibrary1() {
    testWithLibrary("MyLib.swc", "MyLib_src.zip", null, () -> doTest(getTestName(false), "as"));
  }

  public void testWithLibrary2() {
    testWithLibrary("MyLib.swc", "MyLib_src.zip", null, () -> doTest(getTestName(false), "as"));
  }

  public void testWithAsDoc1() {
    testWithLibrary("LibWithAsdoc.swc", null, "LibWithAsdoc_docs.zip", () -> doTest(getTestName(false), "as", "WithAsDoc", true, Check.Content));
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testWithAsDoc2() {
    testWithLibrary("LibWithAsdoc.swc", null, "LibWithAsdoc_docs.zip", () -> doTest(getTestName(false), "mxml", "WithAsDoc", true, Check.Content));
  }

  public void testMxml1() {
    doTest(getTestName(false), "mxml", getTestName(false), true, Check.Null);
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testPackageWrapper() {
    doTest(getTestName(false), "mxml", getTestName(false), true, Check.Null);
  }

  public void testSeeClassInSamePackage() {
    String fullName = BASE_PATH + getTestName(false);
    final String[] files = {fullName + ".as", fullName + "_2.as", fullName + "_3.as"};
    doTest(files, getTestName(false), false, Check.Content);
  }

  public void testSeeTopLevelClass() {
    String fullName = BASE_PATH + getTestName(false);
    final String[] files = {fullName + ".as", fullName + "_2.as", fullName + "_3.as"};
    doTest(files, getTestName(false), false, Check.Content);
  }

  public void testSeeTopLevelClassFromDefaultPackage() {
    String fullName = BASE_PATH + getTestName(false);
    final String[] files = {fullName + ".as", fullName + "_2.as"};
    doTest(files, getTestName(false), false, Check.Content);
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testExternalDoc1() {
    setUpJdk();
    String fullName = BASE_PATH + getTestName(false);
    final String[] files = {fullName + ".mxml"};

    VirtualFile swc = LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + BASE_PATH + "CustomSdk.swc");
    swc = JarFileSystem.getInstance().getJarRootForLocalFile(swc);
    VirtualFile asdoc = HttpFileSystem.getInstance().findFileByPath("livedocs.adobe.com/flex/3/langref");
    FlexTestUtils.setupCustomSdk(getModule(), swc, null, asdoc);

    doTest(files, getTestName(false), false, Check.Url);
  }

  public void testDocsInsideFunction() {
    doTest(getTestName(false), "as");
  }

  public void testDocsInsideFunction2() {
    doTest(getTestName(false), "as");
  }

  public void testDocsBeforeBindable() {
    doTest(getTestName(false), "as");
  }

  public void testDocsBeforeBindable2() {
    doTest(getTestName(false), "as");
  }

  public void testDocsBeforeBindable3() {
    doTest(getTestName(false), "as");
  }

  public void testDocsBeforeBindable4() {
    doTest(getTestName(false), "as");
  }

  public void testDocsBeforeBindable5() {
    doTest(getTestName(false), "as");
  }

  public void testDocsBeforeBindable6() {
    doTest(getTestName(false), "as");
  }

  public void testDocsInsideClass() {
    String testName = getTestName(false);
    doTest(testName, "as", testName, false, Check.Content);
  }

  public void testClassInheritDoc1() {
    doTest(getTestName(false), "as");
  }

  public void testClassInheritDoc2() {
    String testName = getTestName(false);
    doTest(new String[]{testName, testName + "_2", testName + "_3"}, "as", testName);
  }

  public void testClassInheritDoc3() {
    String testName = getTestName(false);
    doTest(new String[]{testName, testName + "_2"}, "as", testName);
  }

  public void testClassInheritDoc4() {
    String testName = getTestName(false);
    doTest(new String[]{testName, testName + "_2"}, "as", testName);
  }

  public void testClassInheritDoc5() {
    String testName = getTestName(false);
    doTest(new String[]{testName, testName + "_2", testName + "_3"}, "as", testName);
  }

  public void testMethodInheritDoc1() {
    String testName = getTestName(false);
    doTest(new String[]{testName, testName + "_2", testName + "_3", testName + "_4"}, "as", testName);
  }

  public void testMethodInheritDoc2() {
    String testName = getTestName(false);
    doTest(new String[]{testName, testName + "_2", testName + "_3"}, "as", testName);
  }

  public void testMethodInheritDoc3() {
    String testName = getTestName(false);
    doTest(new String[]{testName, testName + "_2"}, "as", testName);
  }

  public void testQuickNavigateInfoFromSource() {
    testWithLibrary("MyLib.swc", "MyLib_src.zip", null, () -> {
      final String testName = getTestName(false);
      doQuickNavigateTest();
    });
  }

  private void testWithLibrary(String swc, String sources, String docs, Runnable test) {
    FlexTestUtils.addLibrary(getModule(), "TestLib", getTestDataPath() + BASE_PATH, swc, sources, docs);
    try {
      test.run();
    } finally {
      FlexTestUtils.removeLibrary(getModule(), "TestLib");
    }
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testGenericType() {
    setUpJdk();
    final String testName = getTestName(false);
    FlexTestUtils.addASDocToSdk(getModule(), getClass(), testName);
    doTest(new String[]{testName}, "as", testName);
  }

  public void testEndOfLineComment() {
    String testName = getTestName(false);
    doTest(new String[]{testName}, "as", testName);
  }
}
