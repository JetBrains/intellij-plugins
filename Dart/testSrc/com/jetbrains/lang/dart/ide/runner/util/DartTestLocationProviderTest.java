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

    final List<Location> locations = DartTestLocationProvider.INSTANCE.getLocationForTest(file, locationHint);
    assertEquals(1, locations.size());

    final Location location = locations.get(0);
    final PsiElement element = location.getPsiElement();

    final DartId foundId = PsiTreeUtil.findChildOfType(element, DartId.class);

    assertEquals(targetId, foundId);
  }

  public void testSingleTest() {
    doTest("[foo]", """
      main() {
        <caret>test('foo', () => expect(true, true));
      }""");
  }

  public void testMultipleTests() {
    doTest("[bar]", """
      main() {
        test('foo', () => expect(true, true));
        <caret>test('bar', () => expect(true, true));
      }""");
  }

  public void testMultipleTests2() {
    doTest("[foo]", """
      main() {
        <caret>test('foo', () => expect(true, true));
        test('bar', () => expect(true, true));
      }""");
  }

  public void testGroupedTest() {
    doTest("[foo,bar]", """
      main() {
        group('foo', () {
          <caret>test('bar', () => expect(true, true));
        });
      }""");
  }

  public void testGroupedTest2() {
    doTest("[foo,bar]", """
      main() {
        group('foo', () {
          <caret>test('bar', () => expect(true, true));
        });
        group('bar', () {
          test('baz', () => expect(true, true));
        });
      }""");
  }

  public void testGroupedTest3() {
    doTest("[baz,bar]", """
      main() {
        group('foo', () {
          test('bar', () => expect(true, true));
        });
        group('baz', () {
          <caret>test('bar', () => expect(true, true));
        });
      }""");
  }

  public void testGroupedTest4() {
    doTest("[foo,bar]", """
      main() {
        group('foo', () {
          <caret>test('bar', () => expect(true, true));
        });
        test('bar', () => expect(true, true));
      }""");
  }

  public void testGroupedTest5() {
    doTest("[foo,bar,baz]", """
      main() {
        group('foo', () {
          group('bar', () {
            <caret>test('baz', () => expect(true, true));
          });
        });
      }""");
  }

  public void testGroup() {
    doTest("[foo]", """
      main() {
        <caret>group('foo', () {
          test('bar', () => expect(true, true));
        });
      }""");
  }
}
