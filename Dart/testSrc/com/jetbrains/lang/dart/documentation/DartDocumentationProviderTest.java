// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.documentation;

import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.ide.documentation.DartDocumentationProvider;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.sdk.DartSdk;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

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

  private void doTestDocUrl(@NotNull final String expectedUrl, @NotNull final String fileRelPath, @NotNull final String declText) {
    final String filePath = DartSdk.getDartSdk(getProject()).getHomePath() + "/lib/" + fileRelPath;
    final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);
    final PsiFile psiFile = PsiManager.getInstance(getProject()).findFile(file);
    final int caretOffset = psiFile.getText().indexOf(declText);
    assertTrue(caretOffset != -1);
    final PsiElement element = PsiTreeUtil.getParentOfType(psiFile.findElementAt(caretOffset), DartComponent.class);
    assertNotNull("target element not found at offset " + caretOffset, element);
    assertSameElements(myProvider.getUrlFor(element, element), Collections.singletonList(expectedUrl));
  }

  public void testFieldRef() {
    doTestQuickNavigateInfo("int <b>x</b>", "class A { int <caret>x; foo() => x; }");
  }

  public void testFunctionRef() {
    doTestQuickNavigateInfo("<b>f</b>() " + RIGHT_ARROW + " dynamic", "<caret>f(); g() => f();");
  }

  public void testEnumRef() {
    doTestQuickNavigateInfo("E <b>E1</b>", "enum E { <caret>E1 } var e = E.E1;");
  }

  public void testDocUrls() {
    doTestDocUrl("https://api.dartlang.org/stable/dart-core/int-class.html",
                 "core/int.dart",
                 "abstract class int extends num {");
    doTestDocUrl("https://api.dartlang.org/stable/dart-core/String/String.fromCharCodes.html",
                 "core/string.dart",
                 "external factory String.fromCharCodes(Iterable<int> charCodes,");
    doTestDocUrl("https://api.dartlang.org/stable/dart-core/List/List.html",
                 "core/list.dart",
                 "external factory List([int length]);");
    doTestDocUrl("https://api.dartlang.org/stable/dart-core/int/int.fromEnvironment.html",
                 "core/int.dart",
                 "external const factory int.fromEnvironment(String name, {int defaultValue});");
    doTestDocUrl("https://api.dartlang.org/stable/dart-math/cos.html",
                 "math/math.dart",
                 "external double cos(num radians);");
    doTestDocUrl("https://api.dartlang.org/stable/dart-core/List/length.html",
                 "core/list.dart",
                 "set length(int newLength);");
  }
}
