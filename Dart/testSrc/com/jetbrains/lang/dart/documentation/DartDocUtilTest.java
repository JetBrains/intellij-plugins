package com.jetbrains.lang.dart.documentation;

import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.ide.documentation.DartDocUtil;
import com.jetbrains.lang.dart.psi.DartComponent;

import static com.jetbrains.lang.dart.util.DartPresentableUtil.RIGHT_ARROW;


public class DartDocUtilTest extends DartCodeInsightFixtureTestCase {

  private void doTest(String expectedDoc, String fileContents) throws Exception {
    final int caretOffset = fileContents.indexOf("<caret>");
    assertTrue(caretOffset != -1);
    final String realContents = fileContents.substring(0, caretOffset) + fileContents.substring(caretOffset + "<caret>".length());
    final PsiFile psiFile = myFixture.addFileToProject("test.dart", realContents);
    final DartComponent dartComponent = PsiTreeUtil.getParentOfType(psiFile.findElementAt(caretOffset), DartComponent.class);
    assertNotNull("target element not found at offset " + caretOffset, dartComponent);
    assertEquals(expectedDoc, DartDocUtil.generateDoc(dartComponent));
  }

  public void testAbstractClassSig() throws Exception {
    doTest("<code>abstract class Foo<br/>extends Bar</code>",
           "<caret>abstract class Foo extends Bar { }\nclass Bar { }");
  }

  public void testParamClassSig() throws Exception {
    doTest("<code>class Foo&lt;T&gt;</code>",
           "<caret>class Foo<T>{ }");
  }

  public void testParamClassSig2() throws Exception {
    doTest("<code>class Foo&lt;T,Z&gt;</code>",
           "<caret>class Foo<T,Z>{ }");
  }

  public void testParamClassSig3() throws Exception {
    doTest("<code>class Foo<br/>implements Bar</code>",
           "<caret>class Foo implements Bar { }<br/>class Bar { }");
  }

  public void testParamClassSig4() throws Exception {
    doTest("<code>class Foo<br/>implements Bar, Baz</code>",
           "<caret>class Foo implements Bar, Baz { }<br/>class Bar { }<br/>class Baz { }");
  }

  public void testParamClassSig5() throws Exception {
    doTest("<code>class Foo&lt;A,B&gt;<br/>extends Bar&lt;A,B&gt;</code>",
           "class <caret>Foo<A,B> extends Bar<A,B> { }<br/>class Bar<A,B> { }");
  }


  public void testMetaClassSig1() throws Exception {
    doTest("<code>@deprecated<br/>class A</code>",
           " @deprecated class <caret>A {}");
  }

  public void testMetaClassSig2() throws Exception {
    doTest("<code>@Meta('foo')<br/>class A</code>",
           "@Meta(\'foo\') class <caret>A {};<br/>" +
           "class Meta {<br/>" +
           "  final String name;\n" +
           "  const Meta([this.name]);\n" +
           "}");
  }

  public void testLibraryClassDoc() throws Exception {
    doTest("<b>c.b.a</b><br/><br/><code>class A</code>",
           "library c.b.a;\nclass <caret>A {}");
  }

  public void testMixinSig1() throws Exception {
    doTest("<code>class Foo2 with Baz1&lt;K&gt;, Baz2</code>",
           "<caret>class Foo2 = Bar1<E> with Baz1<K>, Baz2");
  }

  public void testFunctionSig1() throws Exception {
    doTest("<code>calc(int x) " + RIGHT_ARROW + " int</code>",
           "<caret>int calc(int x) => x + 42;");
  }

  public void testFunctionSig2() throws Exception {
    doTest("<code>foo([int x])</code>",
           "<caret>foo([int x = 3]) { print(x); }");
  }

  public void testFunctionSig3() throws Exception {
    doTest("<code>foo([int x]) " + RIGHT_ARROW + " void</code>",
           "<caret>void foo([int x = 3]) { print(x); }");
  }

  public void testFunctionSig4() throws Exception {
    doTest("<code>foo(int x, {int y, int z}) " + RIGHT_ARROW + " void</code>",
           "<caret>void foo(int x, {int y, int z}) { }");
  }

  public void testFunctionSig5() throws Exception {
    doTest("<code>calc(x() " + RIGHT_ARROW + " int) " + RIGHT_ARROW + " int</code>",
           "<caret>int calc(int x()) => null;");
  }

  public void testTypedefSig() throws Exception {
    doTest("<code>typedef a(int x) " + RIGHT_ARROW + " int</code>",
           "<caret>typedef int a(int x);");
  }

  public void testFieldSig1() throws Exception {
    doTest("<code>Z<br/><br/>int y</code>",
           "class Z { <caret>int y = 42; }");
  }

  public void testFieldSig2() throws Exception {
    doTest("<code>Z<br/><br/>int y</code>",
           "class Z { <caret>int y; }");
  }

  public void testMethodSig1() throws Exception {
    doTest("<code>Z<br/><br/>y() " + RIGHT_ARROW + " int</code>",
           "class Z { <caret>int y() => 42; }");
  }

  public void testNamedConstructorSig() throws Exception {
    doTest("<code>Z<br/><br/>Z.z() " + RIGHT_ARROW + " Z</code>",
           "class Z { <caret>Z.z(); }");
  }

  public void testConstructorSig() throws Exception {
    doTest("<code>Z<br/><br/>Z() " + RIGHT_ARROW + " Z</code>",
           "class Z { <caret>Z(); }");
  }

  public void testGetterSig() throws Exception {
    doTest("<code>Z<br/><br/>get x() " + RIGHT_ARROW + " int</code>",
           "class Z { <caret>int get x => 0; }");
  }

  public void testSetterSig() throws Exception {
    doTest("<code>Z<br/><br/>set x(int x) " + RIGHT_ARROW + " void</code>",
           "class Z { <caret>void set x(int x) { } }");
  }

  public void testTopLevelVarDoc1() throws Exception {
    doTest("<b>a.b.c</b><br/><br/><code>@deprecated<br/>var x</code>",
           "library a.b.c;\n<caret>@deprecated var x = 'foo';\n");
  }

  public void testTopLevelVarDoc2() throws Exception {
    doTest("<b>a.b.c</b><br/><br/><code>int x</code>",
           "library a.b.c;\n<caret>int x = 3;\n");
  }

  public void testFunctionDoc1() throws Exception {
    doTest("<code>foo(int x) " + RIGHT_ARROW + " void</code>" +
           "<br/><br/><p>A function on [x]s.</p>\n",
           "/// A function on [x]s.\n<caret>void foo(int x) { }");
  }


  public void testFunctionDoc2() throws Exception {
    doTest("<code>foo(int x) " + RIGHT_ARROW + " void</code>" +
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
    doTest("<code>class A</code><br/><br/><p>     doc1\n" +
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
    doTest("<code>@deprecated<br/>" +
           "abstract class A</code><br/><br/><p>doc1\n" +
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
    doTest("<code>class A</code><br/><br/><p>doc1\n" +
           "doc2</p>\n",

           "// not doc \n" +
           "///   doc1  \n" +
           " /* not doc */\n" +
           "   /// doc2   \n" +
           " // not doc \n" +
           "<caret>class A{}");
  }

  public void testClassSingleLineDocs2() throws Exception {
    doTest("<code>@deprecated<br/>" +
           "class A</code><br/><br/><p>doc1\n" +
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
    doTest("<code>A<br/><br/>foo()</code><br/><br/><p>     doc1\n" +
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
    doTest("<code>A<br/><br/>foo()</code><br/><br/><p>doc1\n" +
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
