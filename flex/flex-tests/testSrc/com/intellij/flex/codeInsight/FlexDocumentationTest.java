// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.codeInsight;

import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.idea.IJIgnore;
import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.lang.javascript.JSDocumentationFixture;
import com.intellij.lang.javascript.JSDocumentationFixture.Check;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeNameValuePair;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.StandardFileSystems;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.ex.http.HttpFileSystem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.descriptor.CssPropertyDescriptorStub;
import com.intellij.psi.css.impl.util.CssDocumentationProvider;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.JSDocumentationFixture.jsDocumentationFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestApplication
@IJIgnore(issue = "IDEA-387406")
public class FlexDocumentationTest {
  private static final String BASE_PATH = "as_documentation/";

  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSDocumentationFixture> js =
    jsDocumentationFixture(projectFixture, new JSFixtureOptions(FlexTestUtils.getTestDataPath(""), "js2")
        .withBasePath(BASE_PATH)
        .withModuleType(FlexModuleType.MODULE_TYPE_ID));

  @BeforeEach
  void setUp() {
    FlexTestUtils.allowFlexVfsRootsFor(js.get().getFixture().getTestRootDisposable(), "");
  }

  protected void setUpJdk() {
    WriteAction.runAndWait(() -> FlexTestUtils.setupFlexSdk(js.get().getFixture().getModule(), js.get().getTestName(false), getClass(), js.get().getFixture().getTestRootDisposable()));
  }

  private PsiElement getDocElementForLookupItem(DocumentationProvider provider, String fileName) {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    WriteAction.runAndWait(() -> myFixture.configureByFile(BASE_PATH + fileName));
    return ReadAction.compute(() -> {
      PsiElement originalElement = myFixture.getFile().findElementAt(myFixture.getEditor().getCaretModel().getOffset());
      return provider.getDocumentationElementForLookupItem(js.get().getFixture().getPsiManager(), originalElement.getText(), originalElement);
    });
  }

  @Test
  public void testJSDocs6() {
    js.get().defaultTest();
  }

  @Test
  public void testJSDocs14() {
    js.get().defaultTest();
  }

  @Test
  public void testJSDocs15() {
    js.get().defaultTest();
  }

  @Test
  public void testJSDocs25() {
    js.get().defaultTest();
  }

  @Test
  public void testJSDocs31() {
    js.get().defaultTest();
  }

  @Test
  public void testAsSimpleDocs() {
    js.get().doTestWithLinkNavigationCheck("as", 1);
  }

  @Test
  public void testAsSimpleDocs2() {
    js.get().doTest(js.get().getTestName(false), "as");
  }

  @Test
  public void testAsSimpleDocs3() {
    js.get().doTest(js.get().getTestName(false), "as");
  }

  @Test
  public void testAsSimpleDocs4() {
    js.get().doTest(js.get().getTestName(false), "as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testMxmlDoc() {
    setUpJdk();
    js.get().doTest(js.get().getTestName(false), "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testMxmlDoc2() {
    setUpJdk();
    js.get().doTest(js.get().getTestName(false), "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testMxmlDoc3() {
    setUpJdk();
    js.get().doTest(js.get().getTestName(false), "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testMxmlDoc4() {
    setUpJdk();
    js.get().doTest(js.get().getTestName(false), "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testMxmlDoc5() {
    PsiElement element = getDocElementForLookupItem(new CssDocumentationProvider(), js.get().getTestName(false) + ".mxml");
    assertNull(element);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testMxmlDoc6() {
    setUpJdk();
    js.get().doTest(js.get().getTestName(false), "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testMxmlDoc7() {
    js.get().doTest(js.get().getTestName(false), "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testFlexCssSelector() {
    setUpJdk();
    DocumentationProvider cssDocumentationProvider = new CssDocumentationProvider();
    PsiElement docElement = getDocElementForLookupItem(cssDocumentationProvider, js.get().getTestName(false) + ".css");
    assertInstanceOf(JSClass.class, docElement);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testFlexCssSelectorMultiDocumentation() {
    testWithLibrary("MyLib1.swc", "MyLib1_src.zip", null, () -> js.get().doTest(js.get().getTestName(false), "css"));
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testFlexCssSelectorMultiDocumentationInLookup() {
    testWithLibrary("MyLib1.swc", "MyLib1_src.zip", null, () -> {
      String doc = js.get().testOne(js.get().getTestName(false) + ".css");
      assertTrue(doc.contains("p1.MyClass"));
      assertTrue(doc.contains("p2.MyClass"));
    });
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testFlexMetadataStyle() {
    String testName = js.get().getTestName(false);
    String s = BASE_PATH + testName;
    js.get().doTest(new String[]{s + ".css", s + ".mxml"}, testName, Check.Content);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testFlexMetadataStyleMultidoc() {
    String testName = js.get().getTestName(false);
    String s = BASE_PATH + testName;
    js.get().doTest(new String[]{s + ".css", s + "1.mxml", s + "2.mxml"}, testName, Check.Content);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testFlexCssProperty() {
    setUpJdk();
    DocumentationProvider cssDocumentationProvider = new CssDocumentationProvider();
    PsiElement docElement = getDocElementForLookupItem(cssDocumentationProvider, js.get().getTestName(false) + ".css");
    assertInstanceOf(JSAttributeNameValuePair.class, docElement);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testFlexCssPropertyMultiDocumentation() {
    setUpJdk();
    js.get().doTest(js.get().getTestName(false), "css");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testFlexCssPropertyMultiDocumentationInLookup() {
    setUpJdk();
    DocumentationProvider cssDocProvider = new CssDocumentationProvider();
    String doc = js.get().testOne(js.get().getTestName(false) + ".css", new CssPropertyDescriptorStub("borderColor"));
    assertNotNull(doc);
    assertTrue(doc.contains("Container"), "Container's borderColor property missing");
    assertTrue(doc.contains("Button"), "Button's borderColor property missing");
    assertTrue(doc.contains("UIComponent"), "UIComponent's borderColor property missing");
  }

  @Test
  public void testQuickNavigateInfo() {
    js.get().doQuickNavigateTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testQuickNavigateInfoWithMxml() {
    setUpJdk();
    js.get().doQuickNavigateTest(js.get().getTestName(false), "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testQuickNavigateInfoWithMxml2() {
    js.get().doQuickNavigateTest(js.get().getTestName(false), "mxml");
  }

  @Test
  public void testQuickNavigateInfo_2() {
    js.get().doQuickNavigateTest();
  }

  @Test
  public void testQuickNavigateInfo_3() {
    js.get().doQuickNavigateTest();
  }

  @Test
  public void testQuickNavigateInfo_4() {
    js.get().doQuickNavigateTest();
  }

  @Test
  public void testQuickNavigateInfo_5() {
    js.get().doQuickNavigateTest();
  }

  @Test
  public void testQuickNavigateInfo_5_2() {
    js.get().doQuickNavigateTest();
  }

  @Test
  public void testQuickNavigateInfo_6() {
    js.get().doQuickNavigateTest();
  }

  @Test
  public void testQuickNavigateInfo_7() {
    js.get().doQuickNavigateTest();
  }

  @Test
  public void testQuickNavigateInfo_9() {
    js.get().doQuickNavigateTest();
  }

  @Test
  public void testQuickNavigateInfo_10() {
    js.get().doQuickNavigateTest();
  }

  @Test
  public void testQuickNavigateInfo_11() {
    js.get().doQuickNavigateTest();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testSeeAlso() {
    setUpJdk();
    js.get().doTestWithLinkNavigationCheck("as", 4);
  }

  @Test
  public void testWithLibrary1() {
    testWithLibrary("MyLib.swc", "MyLib_src.zip", null, () -> js.get().doTest(js.get().getTestName(false), "as"));
  }

  @Test
  public void testWithLibrary2() {
    testWithLibrary("MyLib.swc", "MyLib_src.zip", null, () -> js.get().doTest(js.get().getTestName(false), "as"));
  }

  @Test
  public void testWithAsDoc1() {
    testWithLibrary("LibWithAsdoc.swc", null, "LibWithAsdoc_docs.zip", () -> js.get().doTest(js.get().getTestName(false), "as", "WithAsDoc", Check.Content));
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testWithAsDoc2() {
    testWithLibrary("LibWithAsdoc.swc", null, "LibWithAsdoc_docs.zip", () -> js.get().doTest(js.get().getTestName(false), "mxml", "WithAsDoc", Check.Content));
  }

  @Test
  public void testMxml1() {
    js.get().doTest(js.get().getTestName(false), "mxml", js.get().getTestName(false), Check.Null);
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testPackageWrapper() {
    js.get().doTest(js.get().getTestName(false), "mxml", js.get().getTestName(false), Check.Null);
  }

  @Test
  public void testSeeClassInSamePackage() {
    String fullName = BASE_PATH + js.get().getTestName(false);
    final String[] files = {fullName + ".as", fullName + "_2.as", fullName + "_3.as"};
    js.get().doTest(files, js.get().getTestName(false), Check.Content);
  }

  @Test
  public void testSeeTopLevelClass() {
    String fullName = BASE_PATH + js.get().getTestName(false);
    final String[] files = {fullName + ".as", fullName + "_2.as", fullName + "_3.as"};
    js.get().doTest(files, js.get().getTestName(false), Check.Content);
  }

  @Test
  public void testSeeTopLevelClassFromDefaultPackage() {
    String fullName = BASE_PATH + js.get().getTestName(false);
    final String[] files = {fullName + ".as", fullName + "_2.as"};
    js.get().doTest(files, js.get().getTestName(false), Check.Content);
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testExternalDoc1() {
    setUpJdk();
    String fullName = BASE_PATH + js.get().getTestName(false);
    final String[] files = {fullName + ".mxml"};

    VirtualFile swc = StandardFileSystems.local().findFileByPath(js.get().getTestDataPath() + BASE_PATH + "CustomSdk.swc");
    VirtualFile asdoc = HttpFileSystem.getInstance().findFileByPath("livedocs.adobe.com/flex/3/langref");
    WriteAction.runAndWait(() -> FlexTestUtils.setupCustomSdk(js.get().getFixture().getModule(), JarFileSystem.getInstance().getJarRootForLocalFile(swc), null, asdoc));

    js.get().doTest(files, js.get().getTestName(false), Check.Url);
  }

  @Test
  public void testDocsInsideFunction() {
    js.get().doTest(js.get().getTestName(false), "as");
  }

  @Test
  public void testDocsInsideFunction2() {
    js.get().doTest(js.get().getTestName(false), "as");
  }

  @Test
  public void testDocsBeforeBindable() {
    js.get().doTest(js.get().getTestName(false), "as");
  }

  @Test
  public void testDocsBeforeBindable2() {
    js.get().doTest(js.get().getTestName(false), "as");
  }

  @Test
  public void testDocsBeforeBindable3() {
    js.get().doTest(js.get().getTestName(false), "as");
  }

  @Test
  public void testDocsBeforeBindable4() {
    js.get().doTest(js.get().getTestName(false), "as");
  }

  @Test
  public void testDocsBeforeBindable5() {
    js.get().doTest(js.get().getTestName(false), "as");
  }

  @Test
  public void testDocsBeforeBindable6() {
    js.get().doTest(js.get().getTestName(false), "as");
  }

  @Test
  public void testDocsInsideClass() {
    String testName = js.get().getTestName(false);
    js.get().doTest(testName, "as", testName, Check.Content);
  }

  @Test
  public void testClassInheritDoc1() {
    js.get().doTest(js.get().getTestName(false), "as");
  }

  @Test
  public void testClassInheritDoc2() {
    String testName = js.get().getTestName(false);
    js.get().doTest(new String[]{testName, testName + "_2", testName + "_3"}, "as", testName);
  }

  @Test
  public void testClassInheritDoc3() {
    String testName = js.get().getTestName(false);
    js.get().doTest(new String[]{testName, testName + "_2"}, "as", testName);
  }

  @Test
  public void testClassInheritDoc4() {
    String testName = js.get().getTestName(false);
    js.get().doTest(new String[]{testName, testName + "_2"}, "as", testName);
  }

  @Test
  public void testClassInheritDoc5() {
    String testName = js.get().getTestName(false);
    js.get().doTest(new String[]{testName, testName + "_2", testName + "_3"}, "as", testName);
  }

  @Test
  public void testMethodInheritDoc1() {
    String testName = js.get().getTestName(false);
    js.get().doTest(new String[]{testName, testName + "_2", testName + "_3", testName + "_4"}, "as", testName);
  }

  @Test
  public void testMethodInheritDoc2() {
    String testName = js.get().getTestName(false);
    js.get().doTest(new String[]{testName, testName + "_2", testName + "_3"}, "as", testName);
  }

  @Test
  public void testMethodInheritDoc3() {
    String testName = js.get().getTestName(false);
    js.get().doTest(new String[]{testName, testName + "_2"}, "as", testName);
  }

  @Test
  public void testQuickNavigateInfoFromSource() {
    testWithLibrary("MyLib.swc", "MyLib_src.zip", null, () -> {
      final String testName = js.get().getTestName(false);
      js.get().doQuickNavigateTest();
    });
  }

  private void testWithLibrary(String swc, String sources, String docs, Runnable test) {
    WriteAction.runAndWait(() -> FlexTestUtils.addLibrary(js.get().getFixture().getModule(), "TestLib", js.get().getTestDataPath() + BASE_PATH, swc, sources, docs));
    try {
      test.run();
    }
    finally {
      WriteAction.runAndWait(() -> FlexTestUtils.removeLibrary(js.get().getFixture().getModule(), "TestLib"));
    }
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testGenericType() {
    setUpJdk();
    final String testName = js.get().getTestName(false);
    WriteAction.runAndWait(() -> FlexTestUtils.addASDocToSdk(js.get().getFixture().getModule(), getClass(), testName));
    js.get().doTest(new String[]{testName}, "as", testName);
  }

  @Test
  public void testEndOfLineComment() {
    String testName = js.get().getTestName(false);
    js.get().doTest(new String[]{testName}, "as", testName);
  }
}
