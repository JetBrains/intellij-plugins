package com.jetbrains.lang.dart.ide.runner.util;

import com.intellij.execution.Location;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.psi.DartId;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DartTestLocationProviderTest extends DartCodeInsightFixtureTestCase {

  private void doTest(@NotNull final String locationHint, @NotNull final String fileContents) {
    final int caretOffset = fileContents.indexOf("<caret>");
    assertTrue(caretOffset != -1);

    final String realContents = fileContents.substring(0, caretOffset) + fileContents.substring(caretOffset + "<caret>".length());
    myFixture.configureByText("test.dart", realContents);
    final PsiFile file = myFixture.getFile();
    final PsiElement elementAtOffset = file.findElementAt(caretOffset);
    final PsiElement targetId = elementAtOffset == null ? null : elementAtOffset.getParent();

    final List<Location> locations = DartTestLocationProvider.INSTANCE.getLocation(locationHint, file);
    assertEquals(1, locations.size());

    final Location location = locations.get(0);
    final PsiElement element = location.getPsiElement();

    final DartId foundId = PsiTreeUtil.findChildOfType(element, DartId.class);

    assertEquals(targetId, foundId);
  }


  public void testSingleTest() throws Exception {
    doTest("foo", "main() {\n" +
                  "  <caret>test('foo', () => expect(true, true));\n" +
                  "}");
  }

  public void testMultipleTests() throws Exception {
    doTest("bar", "main() {\n" +
                  "  test('foo', () => expect(true, true));\n" +
                  "  <caret>test('bar', () => expect(true, true));\n" +
                  "}");
  }

  public void testMultipleTests2() throws Exception {
    doTest("foo", "main() {\n" +
                  "  <caret>test('foo', () => expect(true, true));\n" +
                  "  test('bar', () => expect(true, true));\n" +
                  "}");
  }

  public void testGroupedTest() throws Exception {
    doTest("foo/bar", "main() {\n" +
                  "  group('foo', () {\n" +
                  "    <caret>test('bar', () => expect(true, true));\n" +
                  "  });\n" +
                  "}");
  }

  public void testGroupedTest2() throws Exception {
    doTest("foo/bar", "main() {\n" +
                  "  group('foo', () {\n" +
                  "    <caret>test('bar', () => expect(true, true));\n" +
                  "  });\n" +
                  "  group('bar', () {\n" +
                  "    test('baz', () => expect(true, true));\n" +
                  "  });\n" +
                  "}");
  }

  public void testGroupedTest3() throws Exception {
    doTest("baz/bar", "main() {\n" +
                  "  group('foo', () {\n" +
                  "    test('bar', () => expect(true, true));\n" +
                  "  });\n" +
                  "  group('baz', () {\n" +
                  "    <caret>test('bar', () => expect(true, true));\n" +
                  "  });\n" +
                  "}");
  }

  public void testGroupedTest4() throws Exception {
    doTest("foo/bar", "main() {\n" +
                      "  group('foo', () {\n" +
                      "    <caret>test('bar', () => expect(true, true));\n" +
                      "  });\n" +
                      "  test('bar', () => expect(true, true));\n" +
                      "}");
  }

  public void testGroupedTest5() throws Exception {
    doTest("foo/bar/baz", "main() {\n" +
                      "  group('foo', () {\n" +
                      "    group('bar', () {\n" +
                      "      <caret>test('baz', () => expect(true, true));\n" +
                      "    });\n" +
                      "  });\n" +
                      "}");
  }


  public void testGroup() throws Exception {
    doTest("foo", "main() {\n" +
                      "  <caret>group('foo', () {\n" +
                      "    test('bar', () => expect(true, true));\n" +
                      "  });\n" +
                      "}");
  }

}
