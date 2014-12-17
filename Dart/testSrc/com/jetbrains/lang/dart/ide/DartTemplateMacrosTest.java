package com.jetbrains.lang.dart.ide;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.ide.template.macro.DartClassNameMacro;
import com.jetbrains.lang.dart.ide.template.macro.DartMethodNameMacro;
import com.jetbrains.lang.dart.ide.template.macro.DartMethodParametersMacro;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class DartTemplateMacrosTest extends DartCodeInsightFixtureTestCase {

  @NotNull
  private PsiElement findElementAtCaret(@NotNull final String source) {
    final int caretOffset = source.indexOf("<caret>");
    assertTrue(caretOffset != -1);
    final String contents = source.substring(0, caretOffset) + source.substring(caretOffset + "<caret>".length());
    final PsiFile psiFile = myFixture.addFileToProject("test.dart", contents);
    final PsiElement psiElement = psiFile.findElementAt(caretOffset);
    assertNotNull(psiElement);
    return psiElement;
  }

  private void assertContainingClassNameEquals(final String className, final String source) {
    assertEquals(className, new DartClassNameMacro().getContainingClassName(findElementAtCaret(source)));
  }

  private void assertContainingFunctionNameEquals(final String functionName, final String source) {
    assertEquals(functionName, new DartMethodNameMacro().getContainingFunctionName(findElementAtCaret(source)));
  }

  private void assertContainingFunctionParameterNamesEqual(final String [] params, final String source) {
    final List<String> parameterNames = new DartMethodParametersMacro().getContainingMethodParameterNames(findElementAtCaret(source));
    if (params == null) {
      assertNull(parameterNames);
      return;
    }
    assertNotNull(parameterNames);
    assertContainsElements(Arrays.asList(params), parameterNames);
  }

  public void testClassNameMacro0() {
    assertContainingClassNameEquals(null,  "f() { <caret> }");
  }

  public void testClassNameMacro1() {
    assertContainingClassNameEquals("A",  "class A { f() { <caret> } }");
  }

  public void testMethodNameMacro0() {
    assertContainingFunctionNameEquals(null, "class A { <caret> }");
  }

  public void testMethodNameMacro1() {
    assertContainingFunctionNameEquals("f", "f() { <caret> }");
  }

  public void testMethodNameMacro2() {
    assertContainingFunctionNameEquals("f", "class A { f() { <caret> } }");
  }

  public void testMethodNameMacro3() {
    assertContainingFunctionNameEquals("g", "g(f()) => g(() => <caret> null);");
  }

  public void testMethodParametersMacro0() {
    assertContainingFunctionParameterNamesEqual(null, "class A { <caret> }");
  }

  public void testMethodParametersMacro1() {
    assertContainingFunctionParameterNamesEqual(new String[]{"a", "b"}, "f(a, b) { <caret> }");
  }

  public void testMethodParametersMacro2() {
    assertContainingFunctionParameterNamesEqual(new String[]{"a", "b"}, "f(a, {b}) { <caret> }");
  }

  public void testMethodParametersMacro3() {
    assertContainingFunctionParameterNamesEqual(new String[]{"a", "b"}, "f(a, [b]) { <caret> }");
  }

  public void testMethodParametersMacro4() {
    assertContainingFunctionParameterNamesEqual(new String[]{}, "f() { <caret> }");
  }

}
