// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.intellij.flex.codeInsight;

import com.intellij.flex.editor.FlexProjectDescriptor;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javascript.flex.documentation.FlexDocumentationProvider;
import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.lang.javascript.JSAbstractDocumentationTest;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.documentation.JSDocumentationProvider;
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
  private static final String BASE_PATH = "/as_documentation/";

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
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), myFixture.getTestRootDisposable());
  }

  private PsiElement getDocElementForLookupItem(DocumentationProvider provider, String fileName) {
    myFixture.configureByFile(BASE_PATH + fileName);
    PsiElement originalElement = myFixture.getFile().findElementAt(myFixture.getEditor().getCaretModel().getOffset());
    return provider.getDocumentationElementForLookupItem(getPsiManager(), originalElement.getText(), originalElement);
  }

  @Override
  protected JSDocumentationProvider createDocumentationProvider() {
    return new FlexDocumentationProvider();
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

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testMxmlDoc() {
    setUpJdk();
    doTest(getTestName(false), "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testMxmlDoc2() {
    setUpJdk();
    doTest(getTestName(false), "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testMxmlDoc3() {
    setUpJdk();
    doTest(getTestName(false), "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testMxmlDoc4() {
    setUpJdk();
    doTest(getTestName(false), "mxml");
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testMxmlDoc5() {
    PsiElement element = getDocElementForLookupItem(new CssDocumentationProvider(), getTestName(false) + ".mxml");
    assertNull(element);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testMxmlDoc6() {
    setUpJdk();
    doTest(getTestName(false), "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testMxmlDoc7() {
    doTest(getTestName(false), "mxml");
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testFlexCssSelector() {
    setUpJdk();
    DocumentationProvider cssDocumentationProvider = new CssDocumentationProvider();
    PsiElement docElement = getDocElementForLookupItem(cssDocumentationProvider, getTestName(false) + ".css");
    assertInstanceOf(docElement, JSClass.class);
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testFlexCssSelectorMultiDocumentation() {
    testWithLibrary("MyLib1.swc", "MyLib1_src.zip", null, () -> doTest(getTestName(false), "css"));
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testFlexCssSelectorMultiDocumentationInLookup() {
    testWithLibrary("MyLib1.swc", "MyLib1_src.zip", null, () -> {
      String doc = testOne(new CssDocumentationProvider(), getTestName(false) + ".css");
      assertTrue(doc.contains("p1.MyClass"));
      assertTrue(doc.contains("p2.MyClass"));
    });
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testFlexMetadataStyle() {
    String testName = getTestName(false);
    String s = BASE_PATH + testName;
    doTest(new String[]{s + ".css", s + ".mxml"}, testName, false, Check.Content);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testFlexMetadataStyleMultidoc() {
    String testName = getTestName(false);
    String s = BASE_PATH + testName;
    doTest(new String[]{s + ".css", s + "1.mxml", s + "2.mxml"}, testName, false, Check.Content);
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testFlexCssProperty() {
    setUpJdk();
    DocumentationProvider cssDocumentationProvider = new CssDocumentationProvider();
    PsiElement docElement = getDocElementForLookupItem(cssDocumentationProvider, getTestName(false) + ".css");
    assertInstanceOf(docElement, JSAttributeNameValuePair.class);
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testFlexCssPropertyMultiDocumentation() {
    setUpJdk();
    doTest(getTestName(false), "css");
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testFlexCssPropertyMultiDocumentationInLookup() {
    setUpJdk();
    DocumentationProvider cssDocProvider = new CssDocumentationProvider();
    String doc = testOne(cssDocProvider, getTestName(false) + ".css", new CssPropertyDescriptorStub("borderColor"));
    assertNotNull(doc);
    assertTrue("Container's borderColor property missing", doc.contains("Container"));
    assertTrue("Button's borderColor property missing", doc.contains("Button"));
    assertTrue("UIComponent's borderColor property missing", doc.contains("UIComponent"));
  }

  public void testQuickNavigateInfo() {
    final String testName = getTestName(false);
    doNavigateTest(testName, "js2", "public class xxx.AAA extends ZZZ<br>implements yyy.XXX");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testQuickNavigateInfoWithMxml() {
    setUpJdk();
    doNavigateTest(getTestName(false), "mxml", "flash.display.Sprite<br>Event mouseDown" + file("MockFlex.as"));
  }

  @JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithJsSupportLoader})
  public void testQuickNavigateInfoWithMxml2() {
    doNavigateTest(getTestName(false), "mxml", "id xxx", false);
  }

  public void testQuickNavigateInfo_2() {
    doNavigateTest(getTestName(false), "js2", "public var xxx.AAA.ttt:* = new Object");
  }

  public void testQuickNavigateInfo_3() {
    doNavigateTest(getTestName(false), "js2", "public static function xxx.<a href=\"psi_element://AAA\">AAA</a>.yyy(p:Object):AAA");
  }

  public void testQuickNavigateInfo_4() {
    doNavigateTest(getTestName(false), "js2", "(property) public static function xxx.AAA.yyy:Object");
  }

  public void testQuickNavigateInfo_5() {
    doNavigateTest(getTestName(false), "js2", "public function xxx.getTimer()");
  }

  public void testQuickNavigateInfo_5_2() {
    doNavigateTest(getTestName(false), "js2", "public function xxx.getTimer()");
  }

  public void testQuickNavigateInfo_6() {
    doNavigateTest(getTestName(false), "js2", "testData.documentation<br>namespace XXX = \"\"", false);
  }

  public void testQuickNavigateInfo_7() {
    doNavigateTest(getTestName(false), "js2", "xxx static const Foo.XXX = &quot;111&quot;");
  }

  public void testQuickNavigateInfo_9() {
    doNavigateTest(getTestName(false), "js2",
                   "var xxx:int");
  }

  public void testQuickNavigateInfo_10() {
    doNavigateTest(getTestName(false), "js2",
                   "var xxx:Vector.&lt;int&gt; = new Vector.&lt;int&gt;()");
  }

  public void testQuickNavigateInfo_11() {
    doNavigateTest(getTestName(false), "js2",
                   "function foo():Vector.&lt;int&gt;");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testSeeAlso() {
    setUpJdk();
    doTestWithLinkNavigationCheck("as", 6);
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testWithLibrary1() {
    testWithLibrary("MyLib.swc", "MyLib_src.zip", null, () -> doTest(getTestName(false), "as"));
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testWithLibrary2() {
    testWithLibrary("MyLib.swc", "MyLib_src.zip", null, () -> doTest(getTestName(false), "as"));
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testWithAsDoc1() {
    testWithLibrary("LibWithAsdoc.swc", null, "LibWithAsdoc_docs.zip", () -> doTest(getTestName(false), "as", "WithAsDoc", true, Check.Content));
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testWithAsDoc2() {
    testWithLibrary("LibWithAsdoc.swc", null, "LibWithAsdoc_docs.zip", () -> doTest(getTestName(false), "mxml", "WithAsDoc", true, Check.Content));
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testMxml1() {
    doTest(getTestName(false), "mxml", getTestName(false), true, Check.Null);
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testPackageWrapper() {
    doTest(getTestName(false), "mxml", getTestName(false), true, Check.Null);
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testSeeClassInSamePackage() {
    String fullName = BASE_PATH + getTestName(false);
    final String[] files = {fullName + ".as", fullName + "_2.as", fullName + "_3.as"};
    doTest(files, getTestName(false), false, Check.Content);
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testSeeTopLevelClass() {
    String fullName = BASE_PATH + getTestName(false);
    final String[] files = {fullName + ".as", fullName + "_2.as", fullName + "_3.as"};
    doTest(files, getTestName(false), false, Check.Content);
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testSeeTopLevelClassFromDefaultPackage() {
    String fullName = BASE_PATH + getTestName(false);
    final String[] files = {fullName + ".as", fullName + "_2.as"};
    doTest(files, getTestName(false), false, Check.Content);
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexSdk})
  public void testExternalDoc1() {
    setUpJdk();
    String fullName = BASE_PATH + getTestName(false);
    final String[] files = {fullName + ".mxml"};

    VirtualFile swc = LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + BASE_PATH + "CustomSdk.swc");
    swc = JarFileSystem.getInstance().getJarRootForLocalFile(swc);
    VirtualFile asdoc = HttpFileSystem.getInstance().findFileByPath("livedocs.adobe.com/flex/3/langref");
    FlexTestUtils.setupCustomSdk(myModule, swc, null, asdoc);

    doTest(files, getTestName(false), false, Check.Url);
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testDocsInsideFunction() {
    doTest(getTestName(false), "as");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testDocsInsideFunction2() {
    doTest(getTestName(false), "as");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testDocsBeforeBindable() {
    doTest(getTestName(false), "as");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testDocsBeforeBindable2() {
    doTest(getTestName(false), "as");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testDocsBeforeBindable3() {
    doTest(getTestName(false), "as");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testDocsBeforeBindable4() {
    doTest(getTestName(false), "as");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testDocsBeforeBindable5() {
    doTest(getTestName(false), "as");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testDocsBeforeBindable6() {
    doTest(getTestName(false), "as");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testDocsInsideClass() {
    String testName = getTestName(false);
    doTest(testName, "as", testName, false, Check.Content);
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testClassInheritDoc1() {
    doTest(getTestName(false), "as");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testClassInheritDoc2() {
    String testName = getTestName(false);
    doTest(new String[]{testName, testName + "_2", testName + "_3"}, "as", testName);
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testClassInheritDoc3() {
    String testName = getTestName(false);
    doTest(new String[]{testName, testName + "_2"}, "as", testName);
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testClassInheritDoc4() {
    String testName = getTestName(false);
    doTest(new String[]{testName, testName + "_2"}, "as", testName);
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testClassInheritDoc5() {
    String testName = getTestName(false);
    doTest(new String[]{testName, testName + "_2", testName + "_3"}, "as", testName);
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testMethodInheritDoc1() {
    String testName = getTestName(false);
    doTest(new String[]{testName, testName + "_2", testName + "_3", testName + "_4"}, "as", testName);
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testMethodInheritDoc2() {
    String testName = getTestName(false);
    doTest(new String[]{testName, testName + "_2", testName + "_3"}, "as", testName);
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testMethodInheritDoc3() {
    String testName = getTestName(false);
    doTest(new String[]{testName, testName + "_2"}, "as", testName);
  }

  public void testQuickNavigateInfoFromSource() {
    testWithLibrary("MyLib.swc", "MyLib_src.zip", null, () -> {
      final String testName = getTestName(false);
      doNavigateTest(testName, "js2", "public function LibraryMain.someMethod(param:int):int" + file("LibraryMain.as"));
    });
  }

  private void testWithLibrary(String swc, String sources, String docs, Runnable test) {
    FlexTestUtils.addLibrary(myModule, "TestLib", getTestDataPath() + BASE_PATH, swc, sources, docs);
    try {
      test.run();
    } finally {
      FlexTestUtils.removeLibrary(myModule, "TestLib");
    }
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testGenericType() {
    setUpJdk();
    final String testName = getTestName(false);
    FlexTestUtils.addASDocToSdk(myModule, getClass(), testName);
    doTest(new String[]{testName}, "as", testName);
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testEndOfLineComment() {
    String testName = getTestName(false);
    doTest(new String[]{testName}, "as", testName);
  }
}
