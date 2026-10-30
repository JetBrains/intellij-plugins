package com.intellij.flex.editor;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSEditorFixture;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.JSEditorFixture.jsEditorFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;

@TestApplication
public class FlexSelectWordTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSEditorFixture> js =
    jsEditorFixture(projectFixture, new JSFixtureOptions(FlexTestUtils.getTestDataPath("as_editor/")));

  @BeforeEach
  void setUp() {
    FlexTestUtils.allowFlexVfsRootsFor(js.get().getTestRootDisposable(), "as_editor/");
  }

  @Test
  public void testSyntaxSelection3() {
    EdtTestUtil.runInEdtAndWait(() -> {
      CodeInsightTestFixture myFixture = js.get().getFixture();
      String testName = js.get().getTestName(false);
      myFixture.configureByFile(testName + ".js2");
      js.get().performSyntaxSelectionAction();
      myFixture.checkResultByFile(testName + "_after.js2");

      js.get().performSyntaxSelectionAction();
      myFixture.checkResultByFile(testName + "_after1.5.js2");

      js.get().performSyntaxSelectionAction();
      myFixture.checkResultByFile(testName + "_after2.js2");

      js.get().performSyntaxSelectionAction();
      myFixture.checkResultByFile(testName + "_after3.js2");

      js.get().performSyntaxSelectionAction();
      myFixture.checkResultByFile(testName + "_after4.js2");

      myFixture.configureByFile(testName + "_2.js2");
      js.get().performSyntaxSelectionAction();
      myFixture.checkResultByFile(testName + "_2_after.js2");

      myFixture.configureByFile(testName + "_3.js2");
      js.get().performSyntaxSelectionAction();
      myFixture.checkResultByFile(testName + "_3_after.js2");
    });
  }

  @Test
  public void testSyntaxSelection5() {
    EdtTestUtil.runInEdtAndWait(() -> {
      CodeInsightTestFixture myFixture = js.get().getFixture();
      String testName = js.get().getTestName(false);
      myFixture.configureByFile(testName + ".js2");
      js.get().performSyntaxSelectionAction();
      myFixture.checkResultByFile(testName + "_after.js2");

      js.get().performSyntaxSelectionAction();
      myFixture.checkResultByFile(testName + "_after1.5.js2");

      js.get().performSyntaxSelectionAction();
      myFixture.checkResultByFile(testName + "_after2.js2");

      js.get().performSyntaxSelectionAction();
      myFixture.checkResultByFile(testName + "_after3.js2");

      myFixture.configureByFile(testName + "_2.js2");
      js.get().performSyntaxSelectionAction();
      myFixture.checkResultByFile(testName + "_2_after.js2");
    });
  }

  @Test
  public void testSyntaxSelection6() {
    EdtTestUtil.runInEdtAndWait(() -> {
      CodeInsightTestFixture myFixture = js.get().getFixture();
      String testName = js.get().getTestName(false);
      myFixture.configureByFile(testName + ".js2");
      js.get().performSyntaxSelectionAction();
      myFixture.checkResultByFile(testName + "_after.js2");
    });
  }

  @Test
  public void testSyntaxSelection6_2() {
    EdtTestUtil.runInEdtAndWait(() -> {
      CodeInsightTestFixture myFixture = js.get().getFixture();
      String testName = js.get().getTestName(false);
      myFixture.configureByFile(testName + ".js2");
      js.get().performSyntaxSelectionAction();
      myFixture.checkResultByFile(testName + "_after.js2");
    });
  }
}
