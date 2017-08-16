package com.jetbrains.dart.analysisServer;

import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.jetbrains.lang.dart.ide.documentation.DartDocumentationProvider;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.dartlang.analysis.server.protocol.HoverInformation;

public class DartServerDocUtilTest extends CodeInsightFixtureTestCase {
  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, myFixture.getTestRootDisposable(), true);
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
  }

  public void testAbstractClassSig() {
    doTest("<code>abstract class Foo extends Bar<br></code>", "abstract class <caret>Foo extends Bar { }\nclass Bar { }");
  }

  public void testClassMultilineDoc1() {
    doTest("<code>class A<br></code>\n" +
           "<p>doc1\n" +
           "doc2\n" +
           " doc3</p>\n" +
           "\n" +
           "<p>   doc4</p>\n" +
           "\n" +
           "<pre><code>    code\n" +
           "</code></pre>",

           "/** 1 */\n"     +
           "/**\n"          +
           " *      doc1\n" +
           " * doc2\n"      +
           " *  doc3\n"     +
           " *\n"           +
           " *    doc4\n"   +
           " * \n"          +
           " *     code\n"  +
           " */\n"          +
           "// non-doc\n"   +
           "class <caret>A{}");
  }

  public void testClassMultilineDoc2() {
    doTest("<code>abstract class A<br></code>\n<p>doc1\n" +
           "doc2\n"                                                           +
           " doc3\n"                                                          +
           "doc4\n"                                                           +
           "doc5\n"                                                           +
           " doc6</p>",

           "@deprecated\n"  +
           "/**\n"          +
           "*doc1\n"        +
           "* doc2\n"       +
           "*  doc3\n"      +
           "     *doc4\n"   +
           "     * doc5\n"  +
           "     *  doc6\n" +
           " */\n"          +
           "abstract class <caret>A{}");
  }

  public void testClassSingleLineDocs1() {
    doTest("<code>class A<br></code>\n<p>doc1\n" +
           "doc2</p>",

           "// not doc \n"    +
           "///   doc1  \n"   +
           " /* not doc */\n" +
           "   /// doc2   \n" +
           " // not doc \n"   +
           "class <caret>A{}");
  }

  public void testClassSingleLineDocs2() {
    doTest("<code>class A<br></code>\n<p>doc1\n" +
           "doc2</p>",

           "@deprecated"      +
           "// not doc \n"    +
           "///   doc1  \n"   +
           " /* not doc */\n" +
           "   /// doc2   \n" +
           " // not doc \n"   +
           "class <caret>A{}");
  }

  public void testConstructorSig() {
    doTest("<code>Z() → Z<br><br><b>Containing class:</b> Z<br></code>", "class Z { <caret>Z(); }");
  }

  public void testEnumSig() {
    doTest("<code>enum Foo<br></code>", "enum <caret>Foo { BAR }");
  }

  public void testFieldSig1() {
    doTest("<code>int y<br><br><b>Containing class:</b> Z<br><br><b>Static type:</b> int<br></code>",
           "class Z { int <caret>y = 42; }");
  }

  public void testFieldSig2() {
    doTest("<code>int y<br><br><b>Containing class:</b> Z<br><br><b>Static type:</b> int<br></code>",
           "class Z { int <caret>y; }");
  }

  public void testFunctionDoc1() {
    doTest("<code>foo(int x) → void<br></code>\n<p>A function on [x]s.</p>",
           "/// A function on [x]s.\nvoid <caret>foo(int x) { }");
  }

  public void testFunctionDoc2() {
    doTest("<code>foo(int x) → void<br></code>\n<p>Good for:</p>\n" +
           "\n" +
           "<ul>\n" +
           "<li>this</li>\n" +
           "<li>that</li>\n" +
           "</ul>", "/** Good for:\n\n" +
                      " * * this\n" +
                      " * * that\n" +
                      "*/\n" +
                      "\nvoid <caret>foo(int x) { }");
  }

  //public void testMetaClassSig2() throws Exception {
  //  doTest("<code>class <b>A</b><br><br><b>Containing library:</b> test.dart<br></code>",
  //         "@Meta(\'foo\') class <caret>A {};\n" +
  //         "class Meta {\n" +
  //         "  final String name;\n" +
  //         "  const Meta([this.name]);\n" +
  //         "}");
  //}

  public void testFunctionSig1() {
    doTest("<code>calc(int x) → int<br></code>", "int <caret>calc(int x) => x + 42;");
  }

  public void testFunctionSig10() {
    doTest("<code>x({bool b}) → void<br></code>", "void <caret>x({bool b}){};");
  }

  public void testFunctionSig2() {
    doTest("<code>foo([int x = 3]) → dynamic<br></code>", "<caret>foo([int x = 3]) { print(x); }");
  }

  public void testFunctionSig3() {
    doTest("<code>foo([int x = 3]) → void<br></code>", "void <caret>foo([int x = 3]) { print(x); }");
  }

  public void testFunctionSig4() {
    doTest("<code>foo(int x, {int y, int z}) → void<br></code>", "void <caret>foo(int x, {int y, int z}) { }");
  }

  public void testFunctionSig5() {
    doTest("<code>x(List&lt;dynamic&gt; e) → dynamic<br></code>", "E <caret>x(List<E> e) { }");
  }

  public void testFunctionSig6() {
    doTest("<code>calc(() → int x) → int<br></code>", "int <caret>calc(int x()) => null;");
  }

  public void testFunctionSig7() {
    doTest("<code>foo(Map&lt;int, String&gt; p) → Map&lt;String, int&gt;<br></code>",
           "Map<String, int> <caret>foo(Map<int, String> p) => null;");
  }

  public void testFunctionSig8() {
    doTest("<code>x() → dynamic<br></code>", "<caret>x() => null;");
  }

  public void testFunctionSig9() {
    doTest("<code>x({bool b: true}) → dynamic<br></code>", "<caret>x({bool b: true}){};");
  }

  public void testGetterSig() {
    doTest("<code>get x → int<br><br><b>Containing class:</b> Z<br></code>", "class Z { int get <caret>x => 0; }");
  }

  public void testImplementsSig1() {
    doTest("<code>abstract class Foo implements Bar<br></code>",
           "abstract class <caret>Foo implements Bar<T> { }\nclass Bar { }");
  }

  public void testLibraryClassDoc() {
    doTest("<code>class A<br><br><b>Containing library:</b> c.b.a<br></code>", "library c.b.a;\nclass <caret>A {}");
  }

  public void testMetaClassSig1() {
    doTest("<code>class A<br></code>", " @deprecated class <caret>A {}");
  }

  public void testMethodMultilineDoc() {
    doTest("<code>foo() → dynamic<br><br><b>Containing class:</b> A<br></code>\n" +
           "<p>doc1\n" +
           "doc2\n" +
           " doc3</p>\n" +
           "\n" +
           "<p>   doc4</p>\n" +
           "\n" +
           "<pre><code>    code\n" +
           "</code></pre>",

           "class A{\n"       +
           "/** 1 */\n"       +
           "/**\n"            +
           " *      doc1\n"   +
           " * doc2\n"        +
           " *  doc3\n"       +
           " *\n"             +
           " *    doc4\n"     +
           " * \n"            +
           " *     code\n"    +
           " */\n"            +
           "// non-doc\n"     +
           "<caret>foo(){}\n" +
           "}");
  }

  public void testMethodSig1() {
    doTest("<code>y() → int<br><br><b>Containing class:</b> Z<br></code>", "class Z { int <caret>y() => 42; }");
  }

  public void testMethodSingleLineDocs() {
    doTest("<code>foo() → dynamic<br><br><b>Containing class:</b> A<br></code>\n<p>doc1\n" +
           "doc2</p>", "class A{\n" +
                         "// not doc \n" +
                         "///   doc1  \n" +
                         " /* not doc */\n" +
                         "   /// doc2   \n" +
                         " // not doc \n" +
                         "<caret>foo(){}\n" +
                         "}");
  }

  public void testMixinSig1() {
    doTest("<code>class Foo2 extends Bar1 with Baz1, Baz2<br></code>",
           "class Bar1 {} class Baz1{} class Baz2 {} class <caret>Foo2 = Bar1<E> with Baz1<K>, Baz2");
  }

  public void testMixinSig2() {
    doTest("<code>class X extends Y with Z<br></code>", "class Y {} class Z {} class <caret>X extends Y with Z { }");
  }

  public void testNamedConstructorSig() {
    doTest("<code>Z.z() → Z<br><br><b>Containing class:</b> Z<br></code>", "class Z { <caret>Z.z(); }");
  }

  public void testParamClassSig() {
    doTest("<code>class Foo&lt;T&gt;<br></code>", "class <caret>Foo<T>{ }");
  }

  public void testParamClassSig2() {
    doTest("<code>class Foo&lt;T, Z&gt;<br></code>", "class <caret>Foo<T,Z>{ }");
  }

  public void testParamClassSig3() {
    doTest("<code>class Foo implements Bar<br></code>", "class <caret>Foo implements Bar { }<br/>class Bar { }");
  }

  public void testParamClassSig4() {
    doTest("<code>class Foo implements Bar, Baz<br></code>",
           "class <caret>Foo implements Bar, Baz { }<br/>class Bar { }<br/>class Baz { }");
  }

  public void testParamClassSig5() {
    doTest("<code>class Foo&lt;A, B&gt; extends Bar&lt;A, B&gt;<br></code>",
           "class <caret>Foo<A,B> extends Bar<A,B> { }<br/>class Bar<A,B> { }");
  }

  public void testParamClassSig6() {
    doTest("<code>List&lt;String&gt; ids<br><br><b>Static type:</b> List&lt;String&gt;<br></code>",
           "class A { foo() { List<String> <caret>ids; }}");
  }

  public void testParamClassSig7() {
    doTest(
      "<code>List&lt;Map&lt;String, int&gt;&gt; ids<br><br><b>Static type:</b> List&lt;Map&lt;String, int&gt;&gt;<br></code>",
      "class A { foo() { List<Map<String, int>> <caret>ids; }}");
  }

  public void testParamClassSig8() {
    doTest("<code>List&lt;List&lt;Map&lt;String, List&lt;Object&gt;&gt;&gt;&gt; list<br><br>" +
           "<b>Static type:</b> List&lt;List&lt;Map&lt;String, List&lt;Object&gt;&gt;&gt;&gt;<br></code>",
           "class A { foo() { List<List<Map<String, List<Object>>>> <caret>list; }}");
  }

  public void testSetterSig() {
    doTest("<code>set x(int x) → void<br><br><b>Containing class:</b> Z<br></code>",
           "class Z { void set <caret>x(int x) { } }");
  }

  public void testTopLevelVarDoc1() {
    doTest("<code>dynamic x<br><br><b>Containing library:</b> a.b.c<br><br>" +
           "<b>Static type:</b> dynamic<br><b>Propagated type:</b> String<br></code>\n<p>docs1\n" +
           "docs2</p>", "library a.b.c;\n" +
                          "/// docs1\n" +
                          "/// docs2\n" +
                          "@deprecated var <caret>x = 'foo';");
  }

  public void testTopLevelVarDoc2() {
    doTest("<code>int x<br><br><b>Containing library:</b> a.b.c<br><br><b>Static type:</b> int<br></code>",
           "library a.b.c;\nint <caret>x = 3;\n");
  }

  public void testTypedefSig() {
    doTest("<code>typedef F(int x) → int<br></code>", "typedef int <caret>F(int x);");
  }

  private void doTest(String expectedDoc, String fileContents) {
    final int caretOffset = fileContents.indexOf("<caret>");
    assertTrue(caretOffset != -1);
    final String realContents = fileContents.substring(0, caretOffset) + fileContents.substring(caretOffset + "<caret>".length());
    final PsiFile psiFile = myFixture.configureByText("test.dart", realContents);
    //System.out.println(psiFile.getText());
    myFixture.doHighlighting(); // warm up
    //final DartComponent dartComponent = PsiTreeUtil.getParentOfType(psiFile.findElementAt(caretOffset), DartComponent.class);
    //assertNotNull("target element not found at offset " + caretOffset, dartComponent);
    final HoverInformation hover = DartDocumentationProvider.getSingleHover(psiFile, caretOffset);
    assertNotNull(hover);
    //System.out.println(hover);
    final String doc = DartDocumentationProvider.generateDocServer(hover);
    assertEquals(expectedDoc, doc);
    //for (int i = 0; i < 10; i++) {
    //  final HoverInformation hover = DartDocumentationProvider.getSingleHover(psiFile, caretOffset);
    //  System.out.println(hover);
    //  if (hover != null) {
    //    final String doc = DartDocumentationProvider.generateDocServer(hover);
    //    assertEquals(expectedDoc, doc);
    //    System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    //    break;
    //  }
    //  Uninterruptibles.sleepUninterruptibly(100, TimeUnit.MILLISECONDS);
    //}
  }
}
