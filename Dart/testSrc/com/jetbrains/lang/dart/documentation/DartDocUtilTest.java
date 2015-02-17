package com.jetbrains.lang.dart.documentation;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.ide.documentation.DartDocUtil;
import com.jetbrains.lang.dart.psi.DartComponent;

import static com.jetbrains.lang.dart.util.DartPresentableUtil.RIGHT_ARROW;

public class DartDocUtilTest extends DartCodeInsightFixtureTestCase {

  private void doTest(String expectedDoc, String fileContents) {
    final int caretOffset = fileContents.indexOf("<caret>");
    assertTrue(caretOffset != -1);
    final String realContents = fileContents.substring(0, caretOffset) + fileContents.substring(caretOffset + "<caret>".length());
    final PsiFile psiFile = myFixture.addFileToProject("test.dart", realContents);
    final DartComponent dartComponent = PsiTreeUtil.getParentOfType(psiFile.findElementAt(caretOffset), DartComponent.class);
    assertNotNull("target element not found at offset " + caretOffset, dartComponent);
    assertEquals(expectedDoc, DartDocUtil.generateDoc(dartComponent));
  }

  public void testAbstractClassSig() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code>abstract class <b>Foo</b><br/>extends Bar</code>",
           "<caret>abstract class Foo extends Bar { }\nclass Bar { }");
  }

  public void testParamClassSig() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code>class <b>Foo</b>&lt;T&gt;</code>",
           "<caret>class Foo<T>{ }");
  }

  public void testParamClassSig2() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code>class <b>Foo</b>&lt;T, Z&gt;</code>",
           "<caret>class Foo<T,Z>{ }");
  }

  public void testParamClassSig3() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code>class <b>Foo</b><br/>implements Bar</code>",
           "<caret>class Foo implements Bar { }<br/>class Bar { }");
  }

  public void testParamClassSig4() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code>class <b>Foo</b><br/>implements Bar, Baz</code>",
           "<caret>class Foo implements Bar, Baz { }<br/>class Bar { }<br/>class Baz { }");
  }

  public void testParamClassSig5() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code>class <b>Foo</b>&lt;A, B&gt;<br/>extends Bar&lt;A,B&gt;</code>",
           "class <caret>Foo<A,B> extends Bar<A,B> { }<br/>class Bar<A,B> { }");
  }

  public void testParamClassSig6() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code>A<br/>List&lt;String&gt; <b>ids</b></code>",
           "class A { foo() { List<String> <caret>ids; }}");
  }

  public void testParamClassSig7() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code>A<br/>List&lt;Map&lt;String, int&gt;&gt; <b>ids</b></code>",
           "class A { foo() { List<Map<String, int>> <caret>ids; }}");
  }

  public void testParamClassSig8() throws Exception {
    doTest(
      "<code><small><b>test.dart</b></small></code><br/><br/><code>A<br/>List&lt;List&lt;Map&lt;String, List&lt;Object&gt;&gt;&gt;&gt; <b>list</b></code>",
      "class A { foo() { List<List<Map<String, List<Object>>>> <caret>list; }}");
  }

  public void testMetaClassSig1() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code>@deprecated<br/>class <b>A</b></code>",
           " @deprecated class <caret>A {}");
  }

  public void testMetaClassSig2() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code>@Meta(&#39;foo&#39;)<br/>class <b>A</b></code>",
           "@Meta(\'foo\') class <caret>A {};\n" +
           "class Meta {\n" +
           "  final String name;\n" +
           "  const Meta([this.name]);\n" +
           "}");
  }

  public void testLibraryClassDoc() throws Exception {
    doTest("<code><small><b>c.b.a</b></small></code><br/><br/><code>class <b>A</b></code>",
           "library c.b.a;\nclass <caret>A {}");
  }

  public void testImplementsSig1() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code>abstract class <b>Foo</b><br/>implements Bar&lt;T&gt;</code>",
           "<caret>abstract class Foo implements Bar<T> { }\nclass Bar { }");
  }

  public void testMixinSig1() throws Exception {
    doTest(
      "<code><small><b>test.dart</b></small></code><br/><br/><code>class <b>Foo2</b><br/>extends Bar1&lt;E&gt; with Baz1&lt;K&gt;, Baz2</code>",
      "<caret>class Foo2 = Bar1<E> with Baz1<K>, Baz2");
  }

  public void testMixinSig2() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code>class <b>X</b><br/>extends Y with Z</code>",
           "<caret>class X extends Y with Z { }");
  }

  public void testEnumSig() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code>enum <b>Foo</b></code>",
           "<caret>enum Foo { BAR }");
  }

  public void testFunctionSig1() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code><b>calc</b>(int x) " + RIGHT_ARROW + " int</code>",
           "<caret>int calc(int x) => x + 42;");
  }

  public void testFunctionSig2() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code><b>foo</b>([int x]) " + RIGHT_ARROW + " void</code>",
           "<caret>foo([int x = 3]) { print(x); }");
  }

  public void testFunctionSig3() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code><b>foo</b>([int x]) " + RIGHT_ARROW + " void</code>",
           "<caret>void foo([int x = 3]) { print(x); }");
  }

  public void testFunctionSig4() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code><b>foo</b>(int x, {int y, int z}) " + RIGHT_ARROW + " void</code>",
           "<caret>void foo(int x, {int y, int z}) { }");
  }

  public void testFunctionSig5() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code><b>x</b>(List&lt;E&gt; e) " + RIGHT_ARROW + " E</code>",
           "E <caret>x(List<E> e) { }");
  }

  public void testFunctionSig6() throws Exception {
    doTest(
      "<code><small><b>test.dart</b></small></code><br/><br/><code><b>calc</b>(x() " + StringUtil.escapeXml(RIGHT_ARROW) + " int) "
      + RIGHT_ARROW + " int</code>",
      "<caret>int calc(int x()) => null;");
  }

  public void testFunctionSig7() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code><b>foo</b>(Map&lt;int, String&gt; p) " +
           RIGHT_ARROW +
           " Map&lt;String, int&gt;</code>",
           "Map<String, int> <caret>foo(Map<int, String> p) => null;");
  }

  public void testFunctionSig8() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code><b>x</b>() " + RIGHT_ARROW + " void</code>",
           "<caret>x() => null;");
  }

  public void testFunctionSig9() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code><b>x</b>({bool b}) " + RIGHT_ARROW + " void</code>",
           "<caret>x({bool b: true}){};");
  }

  public void testFunctionSig10() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code><b>x</b>({bool b}) " + RIGHT_ARROW + " void</code>",
           "<caret>x({bool b}){};");
  }

  public void testTypedefSig() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code>typedef <b>a</b>(int x) " + RIGHT_ARROW + " int</code>",
           "<caret>typedef int a(int x);");
  }

  public void testFieldSig1() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code>Z<br/>int <b>y</b></code>",
           "class Z { <caret>int y = 42; }");
  }

  public void testFieldSig2() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code>Z<br/>int <b>y</b></code>",
           "class Z { <caret>int y; }");
  }

  public void testMethodSig1() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code>Z<br/><b>y</b>() " + RIGHT_ARROW + " int</code>",
           "class Z { <caret>int y() => 42; }");
  }

  public void testNamedConstructorSig() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code>Z<br/><b>Z.</b><b>z</b>() " + RIGHT_ARROW + " Z</code>",
           "class Z { <caret>Z.z(); }");
  }

  public void testConstructorSig() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code>Z<br/><b>Z</b>() " + RIGHT_ARROW + " Z</code>",
           "class Z { <caret>Z(); }");
  }

  public void testGetterSig() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code>Z<br/>get <b>x</b>() " + RIGHT_ARROW + " int</code>",
           "class Z { <caret>int get x => 0; }");
  }

  public void testSetterSig() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code>Z<br/>set <b>x</b>(int x) " + RIGHT_ARROW + " void</code>",
           "class Z { <caret>void set x(int x) { } }");
  }

  public void testTopLevelVarDoc1() throws Exception {
    doTest("<code><small><b>a.b.c</b></small></code><br/><br/><code>@deprecated<br/>var <b>x</b></code><br/><br/><p>docs1\ndocs2</p>\n",
           "library a.b.c;\n" +
           "/// docs1\n" +
           "/// docs2\n" +
           "<caret>@deprecated var x = 'foo';");
  }

  public void testTopLevelVarDoc2() throws Exception {
    doTest("<code><small><b>a.b.c</b></small></code><br/><br/><code>int <b>x</b></code>",
           "library a.b.c;\n<caret>int x = 3;\n");
  }

  public void testFunctionDoc1() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code><b>foo</b>(int x) " + RIGHT_ARROW +
           " void</code><br/><br/><p>A function on [x]s.</p>\n",
           "/// A function on [x]s.\n<caret>void foo(int x) { }");
  }


  public void testFunctionDoc2() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code><b>foo</b>(int x) " + RIGHT_ARROW + " void</code>" +
           "<br/><br/><p> Good for:</p>\n\n" +
           "<ul>\n" +
           "<li>this</li>\n" +
           "<li>that</li>\n" +
           "</ul>\n",
           "/** Good for:\n\n" +
           " * * this\n" +
           " * * that\n" +
           "*/\n" +
           "\n<caret>void foo(int x) { }");
  }

  public void testClassMultilineDoc1() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code>class <b>A</b></code><br/><br/><p>     doc1\n" +
           "doc2\n" +
           " doc3</p>\n" +
           "\n" +
           "<p>   doc4</p>\n" +
           "\n" +
           "<pre><code>    code\n" +
           "</code></pre>\n",

           "/** 1 */\n" +
           "/**\n" +
           " *      doc1\n" +
           " * doc2\n" +
           " *  doc3\n" +
           " *\n" +
           " *    doc4\n" +
           " * \n" +
           " *     code\n" +
           " */\n" +
           "// non-doc\n" +
           "<caret>class A{}");
  }

  public void testClassMultilineDoc2() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code>@deprecated<br/>abstract class <b>A</b></code><br/><br/><p>doc1\n" +
           "doc2\n" +
           " doc3\n" +
           "doc4\n" +
           "doc5\n" +
           " doc6</p>\n",

           "@deprecated\n" +
           "/**\n" +
           "*doc1\n" +
           "* doc2\n" +
           "*  doc3\n" +
           "     *doc4\n" +
           "     * doc5\n" +
           "     *  doc6\n" +
           " */\n" +
           "<caret>abstract class A{}");
  }

  public void testClassSingleLineDocs1() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code>class <b>A</b></code><br/><br/><p>doc1\n" +
           "doc2</p>\n",

           "// not doc \n" +
           "///   doc1  \n" +
           " /* not doc */\n" +
           "   /// doc2   \n" +
           " // not doc \n" +
           "<caret>class A{}");
  }

  public void testClassSingleLineDocs2() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code>@deprecated<br/>class <b>A</b></code><br/><br/><p>doc1\n" +
           "doc2</p>\n",

           "@deprecated" +
           "// not doc \n" +
           "///   doc1  \n" +
           " /* not doc */\n" +
           "   /// doc2   \n" +
           " // not doc \n" +
           "<caret>class A{}");
  }

  public void testMethodMultilineDoc() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code>A<br/><b>foo</b>() " + RIGHT_ARROW + " void</code>" +
           "<br/><br/><p>     doc1\n" +
           "doc2\n" +
           " doc3</p>\n" +
           "\n" +
           "<p>   doc4</p>\n" +
           "\n" +
           "<pre><code>    code\n" +
           "</code></pre>\n",

           "class A{\n" +
           "/** 1 */\n" +
           "/**\n" +
           " *      doc1\n" +
           " * doc2\n" +
           " *  doc3\n" +
           " *\n" +
           " *    doc4\n" +
           " * \n" +
           " *     code\n" +
           " */\n" +
           "// non-doc\n" +
           "<caret>foo(){}\n" +
           "}");
  }

  public void testMethodSingleLineDocs() throws Exception {
    doTest("<code><small><b>test.dart</b></small></code><br/><br/><code>A<br/><b>foo</b>() " + RIGHT_ARROW + " void</code>" +
           "<br/><br/><p>doc1\n" +
           "doc2</p>\n",

           "class A{\n" +
           "// not doc \n" +
           "///   doc1  \n" +
           " /* not doc */\n" +
           "   /// doc2   \n" +
           " // not doc \n" +
           "<caret>foo(){}\n" +
           "}");
  }
}
