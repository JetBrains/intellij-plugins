package com.jetbrains.lang.dart.ide.documentation;

import com.intellij.codeInsight.TargetElementUtilBase;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;

public class DartDocumentationProviderTest extends DartCodeInsightFixtureTestCase {


  private static final DartDocumentationProvider myProvider = new DartDocumentationProvider();

  private void doTest(String expectedDoc, String fileContents) throws Exception {
    final int caretOffset = fileContents.indexOf("<caret>");
    assertTrue(caretOffset != -1);

    final String realContents = fileContents.substring(0, caretOffset) + fileContents.substring(caretOffset + "<caret>".length());
    myFixture.configureByText("test.dart", realContents);

    final PsiReference reference = TargetElementUtilBase.findReference(myFixture.getEditor(), caretOffset);
    assertNotNull("No reference at offset " + caretOffset, reference);

    final PsiElement resolvedElement = reference.resolve();
    assertEquals(expectedDoc, myProvider.getUrlFor(resolvedElement, resolvedElement).get(0));
  }

  public void testObjectClass() throws Exception {
    doTest("http://api.dartlang.org/docs/releases/latest/dart_core/Object.html",
           "<caret>Object o;\n");
  }

  public void testObjectHashCode() throws Exception {
    doTest("http://api.dartlang.org/docs/releases/latest/dart_core/Object.html#id_hashCode",
           "int h = new Object().<caret>hashCode;\n");
  }

  public void testObjectToString() throws Exception {
    doTest("http://api.dartlang.org/docs/releases/latest/dart_core/Object.html#id_toString",
           "var s = new Object().<caret>toString();\n");
  }

}
