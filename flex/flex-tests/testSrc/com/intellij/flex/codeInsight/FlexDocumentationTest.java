package com.intellij.flex.codeInsight;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.idea.Bombed;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.javascript.flex.documentation.FlexDocumentationProvider;
import com.intellij.javascript.flex.mxml.schema.FlexSchemaHandler;
import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.lang.javascript.JSAbstractDocumentationTest;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.documentation.JSDocumentationProvider;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeNameValuePair;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.WebModuleType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.ex.http.HttpFileSystem;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.descriptor.CssPropertyDescriptorStub;
import com.intellij.psi.css.impl.util.CssDocumentationProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

public class FlexDocumentationTest extends JSAbstractDocumentationTest {
  private static final String BASE_PATH = "/as_documentation/";

  private Runnable myAfterCommitRunnable = null;

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

  protected ModuleType getModuleType() {
    boolean hasFlex = JSTestUtils.testMethodHasOption(getClass(), getTestName(false), JSTestOption.WithFlexSdk, JSTestOption.WithFlexFacet);
    return hasFlex ? FlexModuleType.getInstance() : WebModuleType.getInstance();
  }

  @Override
  protected void setUp() throws Exception {
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexSchemaHandler.class.getResource("z.xsd"))),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));
    super.setUp();
    myAfterCommitRunnable = null;
  }

  @Override
  protected void tearDown() throws Exception {
    myAfterCommitRunnable = null;
    super.tearDown();
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), getTestRootDisposable());
  }

  private PsiElement getDocElementForLookupItem(DocumentationProvider provider, String fileName) throws Exception {
    configureByFile(BASE_PATH + fileName);
    PsiElement originalElement = myFile.findElementAt(myEditor.getCaretModel().getOffset());
    return provider.getDocumentationElementForLookupItem(myPsiManager, originalElement.getText(), originalElement);
  }

  @Override
  protected void doCommitModel(@NotNull ModifiableRootModel rootModel) {
    super.doCommitModel(rootModel);
    if (myAfterCommitRunnable != null) {
      myAfterCommitRunnable.run();
    }
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
    doTest(getTestName(false), "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testMxmlDoc2() {
    doTest(getTestName(false), "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testMxmlDoc3() {
    doTest(getTestName(false), "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testMxmlDoc4() {
    doTest(getTestName(false), "mxml");
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testMxmlDoc5() throws Exception {
    PsiElement element = getDocElementForLookupItem(new CssDocumentationProvider(), getTestName(false) + ".mxml");
    assertNull(element);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testMxmlDoc6() {
    doTest(getTestName(false), "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testMxmlDoc7() {
    doTest(getTestName(false), "mxml");
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testFlexCssSelector() throws Exception {
    DocumentationProvider cssDocumentationProvider = new CssDocumentationProvider();
    PsiElement docElement = getDocElementForLookupItem(cssDocumentationProvider, getTestName(false) + ".css");
    assertInstanceOf(docElement, JSClass.class);
  }

  @Bombed(month = Calendar.JANUARY, day = 29, user = "denofevil", description = BombReason.MULTI_DOC)
  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testFlexCssSelectorMultiDocumentation() {
    myAfterCommitRunnable =
      () -> FlexTestUtils.addLibrary(myModule, "TestLib", getTestDataPath() + BASE_PATH, "MyLib1.swc", "MyLib1_src.zip", null);
    doTest(getTestName(false), "css");
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testFlexCssSelectorMultiDocumentationInLookup() throws Exception {
    myAfterCommitRunnable =
      () -> FlexTestUtils.addLibrary(myModule, "TestLib", getTestDataPath() + BASE_PATH, "MyLib1.swc", "MyLib1_src.zip", null);
    String doc = testOne(new CssDocumentationProvider(), getTestName(false) + ".css");
    assertTrue(doc.indexOf("p1.MyClass") >= 0);
    assertTrue(doc.indexOf("p2.MyClass") >= 0);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testFlexMetadataStyle() {
    String testName = getTestName(false);
    String s = BASE_PATH + testName;
    doTest(new String[]{s + ".css", s + ".mxml"}, testName, false, Check.Content);
  }

  @Bombed(month = Calendar.JANUARY, day = 29, user = "denofevil", description = BombReason.MULTI_DOC)
  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testFlexMetadataStyleMultidoc() {
    String testName = getTestName(false);
    String s = BASE_PATH + testName;
    doTest(new String[]{s + ".css", s + "1.mxml", s + "2.mxml"}, testName, false, Check.Content);
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testFlexCssProperty() throws Exception {
    DocumentationProvider cssDocumentationProvider = new CssDocumentationProvider();
    PsiElement docElement = getDocElementForLookupItem(cssDocumentationProvider, getTestName(false) + ".css");
    assertInstanceOf(docElement, JSAttributeNameValuePair.class);
  }

  @Bombed(month = Calendar.JANUARY, day = 29, user = "denofevil", description = BombReason.MULTI_DOC)
  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testFlexCssPropertyMultiDocumentation() {
    doTest(getTestName(false), "css");
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testFlexCssPropertyMultiDocumentationInLookup() throws Exception {
    DocumentationProvider cssDocProvider = new CssDocumentationProvider();
    String doc = testOne(cssDocProvider, getTestName(false) + ".css", new CssPropertyDescriptorStub("borderColor"));
    assertNotNull(doc);
    assertTrue("Container's borderColor property missing", doc.indexOf("Container") >= 0);
    assertTrue("Button's borderColor property missing", doc.indexOf("Button") >= 0);
    assertTrue("UIComponent's borderColor property missing", doc.indexOf("UIComponent") >= 0);
  }

  public void testQuickNavigateInfo() throws Exception {
    final String testName = getTestName(false);
    doNavigateTest(testName, "js2", "public class xxx.AAA extends ZZZ\n" + "implements yyy.XXX");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testQuickNavigateInfoWithMxml() throws Exception {
    doNavigateTest(getTestName(false), "mxml", "flash.display.Sprite\n" + "Event mouseDown" + file("MockFlex.as"));
  }

  @JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithJsSupportLoader})
  public void testQuickNavigateInfoWithMxml2() throws Exception {
    doNavigateTest(getTestName(false), "mxml", "id xxx", false);
  }

  public void testQuickNavigateInfo_2() throws Exception {
    doNavigateTest(getTestName(false), "js2", "public var xxx.AAA.ttt:* = new Object");
  }

  public void testQuickNavigateInfo_3() throws Exception {
    doNavigateTest(getTestName(false), "js2", "public static function xxx.AAA.yyy(p:Object):AAA");
  }

  public void testQuickNavigateInfo_4() throws Exception {
    doNavigateTest(getTestName(false), "js2", "(property) public static function xxx.AAA.yyy:Object");
  }

  public void testQuickNavigateInfo_5() throws Exception {
    doNavigateTest(getTestName(false), "js2", "public function xxx.getTimer()");
  }

  public void testQuickNavigateInfo_5_2() throws Exception {
    doNavigateTest(getTestName(false), "js2", "public function xxx.getTimer()");
  }

  public void testQuickNavigateInfo_6() throws Exception {
    doNavigateTest(getTestName(false), "js2", "testData.documentation\n" + "namespace XXX = \"\"", false);
  }

  public void testQuickNavigateInfo_7() throws Exception {
    doNavigateTest(getTestName(false), "js2", "xxx static const Foo.XXX = &quot;111&quot;");
  }

  public void testQuickNavigateInfo_9() throws Exception {
    doNavigateTest(getTestName(false), "js2",
                   "var xxx:int");
  }

  public void testQuickNavigateInfo_10() throws Exception {
    doNavigateTest(getTestName(false), "js2",
                   "var xxx:Vector.&lt;int&gt; = new Vector.&lt;int&gt;()");
  }

  public void testQuickNavigateInfo_11() throws Exception {
    doNavigateTest(getTestName(false), "js2",
                   "function foo():Vector.&lt;int&gt;");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testSeeAlso() {
    doTestWithLinkNavigationCheck("as", 6);
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testWithLibrary1() {
    myAfterCommitRunnable =
      () -> FlexTestUtils.addLibrary(myModule, "TestLib", getTestDataPath() + BASE_PATH, "MyLib.swc", "MyLib_src.zip", null);
    doTest(getTestName(false), "as");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testWithLibrary2() {
    myAfterCommitRunnable =
      () -> FlexTestUtils.addLibrary(myModule, "TestLib", getTestDataPath() + BASE_PATH, "MyLib.swc", "MyLib_src.zip", null);
    doTest(getTestName(false), "as");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testWithAsDoc1() {
    myAfterCommitRunnable = () -> FlexTestUtils
      .addLibrary(myModule, "TestLib", getTestDataPath() + BASE_PATH, "LibWithAsdoc.swc", null, "LibWithAsdoc_docs.zip");

    doTest(getTestName(false), "as", "WithAsDoc", true, Check.Content);
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testWithAsDoc2() {
    myAfterCommitRunnable = () -> FlexTestUtils
      .addLibrary(myModule, "TestLib", getTestDataPath() + BASE_PATH, "LibWithAsdoc.swc", null, "LibWithAsdoc_docs.zip");
    doTest(getTestName(false), "mxml", "WithAsDoc", true, Check.Content);
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
    doTest(testName, "as", testName, false, Check.Null);
  }

  @Bombed(month = Calendar.JANUARY, day = 29, user = "denofevil", description = BombReason.MULTI_DOC)
  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testClassInheritDoc1() {
    doTest(getTestName(false), "as");
  }

  @Bombed(month = Calendar.JANUARY, day = 29, user = "denofevil", description = BombReason.MULTI_DOC)
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

  public void testQuickNavigateInfoFromSource() throws Exception {
    myAfterCommitRunnable =
      () -> FlexTestUtils.addLibrary(myModule, "TestLib", getTestDataPath() + BASE_PATH, "MyLib.swc", "MyLib_src.zip", null);
    final String testName = getTestName(false);
    doNavigateTest(testName, "js2", "public function LibraryMain.someMethod(param:int):int" + file("LibraryMain.as"));
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testGenericType() {
    final String testName = getTestName(false);
    FlexTestUtils.addASDocToSdk(getModule(), getClass(), testName);
    doTest(new String[]{testName}, "as", testName);
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testEndOfLineComment() {
    String testName = getTestName(false);
    doTest(new String[]{testName}, "as", testName);
  }
}
