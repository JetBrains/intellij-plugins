package com.jetbrains.lang.dart.documentation;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.ide.documentation.DartDocUtil;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartFile;
import com.jetbrains.lang.dart.psi.DartFunctionDeclarationWithBodyOrNative;
import com.jetbrains.lang.dart.psi.DartFunctionTypeAlias;


public class DartDocUtilTest extends DartCodeInsightFixtureTestCase {

  private static final String TEST_FILE_NAME = "_test.dart";

  //Signature tests

  public void testAbstractClassSig() throws Exception {
    assertClassDocEquals("abstract class <b>Foo</b> extends Bar",
                         "abstract class Foo extends Bar { }\nclass Bar { }");
  }

  public void testParamClassSig() throws Exception {
    assertClassDocEquals("class <b>Foo</b>&lt;T&gt;",
                         "class Foo<T>{ }");
  }

  public void testParamClassSig2() throws Exception {
    assertClassDocEquals("class <b>Foo</b>&lt;T,Z&gt;",
                         "class Foo<T,Z>{ }");
  }

  public void testParamClassSig3() throws Exception {
    assertClassDocEquals("class <b>Foo</b> implements Bar",
                         "class Foo implements Bar { }\nclass Bar { }");
  }

  public void testFunctionSig1() throws Exception {
    assertFunctionDocEquals("calc(int x) " + DartDocUtil.RIGHT_ARROW + " int",
                            "int calc(int x) => x + 42;");
  }

  public void testFunctionSig2() throws Exception {
    assertFunctionDocEquals("foo([int x])",
                            "foo([int x = 3]) { print(x); }");
  }

  public void testFunctionSig3() throws Exception {
    assertFunctionDocEquals("foo([int x]) " + DartDocUtil.RIGHT_ARROW + " void",
                            "void foo([int x = 3]) { print(x); }");
  }

  public void testFunctionSig4() throws Exception {
    assertFunctionDocEquals("foo(int x, {int y, int z}) " + DartDocUtil.RIGHT_ARROW + " void",
                            "void foo(int x, {int y, int z}) { }");
  }

  public void testTypedefSig() throws Exception {
    assertTypedefDocEquals("typedef a(int x) " + DartDocUtil.RIGHT_ARROW + " int",
                           "typedef int a(int x);");

  }

  //Doc tests

  public void testFunctionDoc1() throws Exception {
    assertFunctionDocEquals("foo(int x) " + DartDocUtil.RIGHT_ARROW + " void" +
                            "<br/><br/><p>A function on [x]s.</p>\n",
                            "/// A function on [x]s.\nvoid foo(int x) { }");
  }

  /// methods, fields



  /// ... some real markdown here




  private void assertClassDocEquals(String expected, String fileContents) throws Exception {
    assertEquals(expected, genDoc(fileContents, DartClass.class));
  }

  private void assertFunctionDocEquals(String expected, String fileContents) throws Exception {
    assertEquals(expected, genDoc(fileContents, DartFunctionDeclarationWithBodyOrNative.class));
  }

  private void assertTypedefDocEquals(String expected, String fileContents) throws Exception {
    assertEquals(expected, genDoc(fileContents, DartFunctionTypeAlias.class));
  }

  private <T extends PsiElement> String genDoc(final String fileContents, final Class<T> cls) {
    myFixture.addFileToProject(TEST_FILE_NAME, fileContents);
    return DartDocUtil.generateDoc(getChild(cls));
  }

  private <T extends PsiElement> T getChild(Class<? extends T> cls) {
    return PsiTreeUtil.findChildrenOfType(getFile(), cls).iterator().next();
  }

  private DartFile getFile() {
    return (DartFile)myFixture.configureByFile(TEST_FILE_NAME);
  }

}
