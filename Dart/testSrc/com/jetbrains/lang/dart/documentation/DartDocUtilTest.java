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
    doTest("abstract class <b>Foo</b> extends Bar",
           "<caret>abstract class Foo extends Bar { }\nclass Bar { }");
  }

  public void testParamClassSig() throws Exception {
    doTest("class <b>Foo</b>&lt;T&gt;",
           "<caret>class Foo<T>{ }");
  }

  public void testParamClassSig2() throws Exception {
    doTest("class <b>Foo</b>&lt;T,Z&gt;",
           "<caret>class Foo<T,Z>{ }");
  }

  public void testParamClassSig3() throws Exception {
    doTest("class <b>Foo</b> implements Bar",
           "<caret>class Foo implements Bar { }\nclass Bar { }");
  }

  public void testFunctionSig1() throws Exception {
    doTest("calc(int x) " + RIGHT_ARROW + " int",
           "<caret>int calc(int x) => x + 42;");
  }

  public void testFunctionSig2() throws Exception {
    doTest("foo([int x])",
           "<caret>foo([int x = 3]) { print(x); }");
  }

  public void testFunctionSig3() throws Exception {
    doTest("foo([int x]) " + RIGHT_ARROW + " void",
           "<caret>void foo([int x = 3]) { print(x); }");
  }

  public void testFunctionSig4() throws Exception {
    doTest("foo(int x, {int y, int z}) " + RIGHT_ARROW + " void",
           "<caret>void foo(int x, {int y, int z}) { }");
  }

  public void testFunctionSig5() throws Exception {
    doTest("calc(x() " + RIGHT_ARROW + " int) " + RIGHT_ARROW + " int",
           "<caret>int calc(int x()) => null;");
  }

  public void testTypedefSig() throws Exception {
    doTest("typedef a(int x) " + RIGHT_ARROW + " int",
           "<caret>typedef int a(int x);");
  }

  public void testFieldSig1() throws Exception {
    doTest("Z<br/><br/>int y",
           "class Z { <caret>int y = 42; }");
  }

  public void testFieldSig2() throws Exception {
    doTest("Z<br/><br/>int y",
           "class Z { <caret>int y; }");
  }

  public void testMethodSig1() throws Exception {
    doTest("Z<br/><br/>y() " + RIGHT_ARROW + " int",
           "class Z { <caret>int y() => 42; }");
  }

  public void testNamedConstructorSig() throws Exception {
    doTest("Z<br/><br/>Z.z() " + RIGHT_ARROW + " Z",
           "class Z { <caret>Z.z(); }");
  }

  public void testConstructorSig() throws Exception {
    doTest("Z<br/><br/>Z() " + RIGHT_ARROW + " Z",
           "class Z { <caret>Z(); }");
  }

  public void testGetterSig() throws Exception {
    doTest("Z<br/><br/>get x() " + RIGHT_ARROW + " int",
           "class Z { <caret>int get x => 0; }");
  }

  public void testSetterSig() throws Exception {
    doTest("Z<br/><br/>set x(int x) " + RIGHT_ARROW + " void",
           "class Z { <caret>void set x(int x) { } }");
  }

  public void testFunctionDoc1() throws Exception {
    doTest("foo(int x) " + RIGHT_ARROW + " void" +
           "<br/><br/><p>A function on [x]s.</p>\n",
           "/// A function on [x]s.\n<caret>void foo(int x) { }");
  }

  public void testFunctionDoc2() throws Exception {
    doTest("foo(int x) " + RIGHT_ARROW + " void" +
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
    doTest("class <b>A</b><br/><br/><p>     doc1\n" +
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
    doTest("abstract class <b>A</b><br/><br/><p>doc1\n" +
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
    doTest("class <b>A</b><br/><br/><p>doc1\n" +
           "doc2</p>\n",

           "// not doc \n" +
           "///   doc1  \n" +
           " /* not doc */\n" +
           "   /// doc2   \n" +
           " // not doc \n" +
           "<caret>class A{}");
  }

  public void testClassSingleLineDocs2() throws Exception {
    doTest("class <b>A</b><br/><br/><p>doc1\n" +
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
    doTest("A<br/><br/>foo()<br/><br/><p>     doc1\n" +
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
    doTest("A<br/><br/>foo()<br/><br/><p>doc1\n" +
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
