package com.jetbrains.lang.dart.documentation;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.ide.documentation.DartDocumentationProvider;
import com.jetbrains.lang.dart.psi.DartComponent;

import static com.jetbrains.lang.dart.util.DartPresentableUtil.RIGHT_ARROW;

public class DartDocumentationProviderTest extends DartCodeInsightFixtureTestCase {

  private final DartDocumentationProvider myProvider = new DartDocumentationProvider();

  private void doTestQuickNavigateInfo(String expectedDoc, String fileContents) {
    final int caretOffset = fileContents.indexOf("<caret>");
    assertTrue(caretOffset != -1);
    final String realContents = fileContents.substring(0, caretOffset) + fileContents.substring(caretOffset + "<caret>".length());
    final PsiFile psiFile = myFixture.addFileToProject("test.dart", realContents);
    //final DartReference reference = PsiTreeUtil.getParentOfType(psiFile.findElementAt(caretOffset), DartReference.class);
    //assertNotNull("reference not found at offset: " + caretOffset, reference);
    //final PsiElement element = reference.resolve();
    final PsiElement element = PsiTreeUtil.getParentOfType(psiFile.findElementAt(caretOffset), DartComponent.class);
    assertNotNull("target element not found at offset " + caretOffset, element);
    assertEquals(expectedDoc, myProvider.getQuickNavigateInfo(element, element));
  }

  public void testFieldRef() throws Exception {
    doTestQuickNavigateInfo("int <b>x</b>", "class A { int <caret>x; foo() => x; }");
  }

  public void _testTypeRef() throws Exception {
    doTestQuickNavigateInfo("abstract class <b>int</b><br/>extends num", "class A { <caret>int x; }");
  }

  public void testFunctionRef() throws Exception {
    doTestQuickNavigateInfo("<b>f</b>() " + RIGHT_ARROW + " dynamic", "<caret>f(); g() => f();");
  }

  public void testEnumRef() throws Exception {
    doTestQuickNavigateInfo("E <b>E1</b>", "enum E { <caret>E1 } var e = E.E1;");
  }
}
