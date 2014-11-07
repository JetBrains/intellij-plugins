package com.jetbrains.lang.dart.ide.documentation;

import com.intellij.codeInsight.documentation.DocumentationManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.psi.DartId;
import com.jetbrains.lang.dart.psi.DartReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DartDocumentationProviderTest extends DartCodeInsightFixtureTestCase {

  private void doTest(@Nullable final String expectedDocUrl, @NotNull final String fileContents) throws Exception {
    final int caretOffset = fileContents.indexOf("<caret>");
    assertTrue(caretOffset != -1);

    final String realContents = fileContents.substring(0, caretOffset) + fileContents.substring(caretOffset + "<caret>".length());
    myFixture.configureByText("test.dart", realContents);

    final PsiElement elementAtOffset = myFixture.getFile().findElementAt(caretOffset);
    final PsiElement dartId = elementAtOffset == null ? null : elementAtOffset.getParent();
    final PsiElement parent = dartId instanceof DartId ? dartId.getParent() : null;

    final PsiElement docTarget;
    if (parent instanceof DartReference) {
      docTarget = ((DartReference)parent).resolve();
      assertNotNull(docTarget);
    }
    else if (parent instanceof DartComponentName) {
      docTarget = parent;
    }
    else {
      fail("Element to look docs for not found");
      return;
    }

    final List<String> docUrls = DocumentationManager.getProviderFromElement(docTarget).getUrlFor(docTarget, null);

    if (expectedDocUrl == null) {
      assertNull(docUrls);
    }
    else {
      assertNotNull(docUrls);
      assertEquals(StringUtil.join(docUrls, "; "), expectedDocUrl);
    }
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

  public void testListLengthGetter() throws Exception {
    doTest("http://api.dartlang.org/docs/releases/latest/dart_core/List.html#id_length",
           "f() { int len = [].<caret>length; }\n");
  }

  public void testListLengthSetter() throws Exception {
    doTest("http://api.dartlang.org/docs/releases/latest/dart_core/List.html#id_length=",
           "f() { [].<caret>length = 3; }\n");
  }

  public void testNoDocUrl() throws Exception {
    doTest(null, "class Fo<caret>o {}");
  }
}
