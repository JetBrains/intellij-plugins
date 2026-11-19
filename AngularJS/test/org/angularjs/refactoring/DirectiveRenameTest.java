package org.angularjs.refactoring;

import com.intellij.openapi.project.Project;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.angularjs.AngularTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightProjectFixture;

/**
 * @author Dennis.Ushakov
 */
@TestApplication
public class DirectiveRenameTest {
  private final TestFixture<Project> projectFixture = codeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsight = codeInsightFixture(projectFixture);

  @BeforeEach
  void setUpTestDataPath() {
    codeInsight.get().setTestDataPath(AngularTestUtil.getBaseTestDataPath(getClass()) + "rename");
  }

  @Test
  public void testTag() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("tag.html", "tag.js", "angular.js");
    EdtTestUtil.runInEdtAndWait(() -> myFixture.testRename("tag.after.html", "foo-bar2"));
    myFixture.checkResultByFile("tag.js", "tag.after.js", false);
  }

  @Test
  public void testTagNormalized() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("tag.html", "tag.js", "angular.js");
    EdtTestUtil.runInEdtAndWait(() -> myFixture.testRename("tag.after.html", "fooBar2"));
    myFixture.checkResultByFile("tag.js", "tag.after.js", false);
  }

  @Test
  public void testAttribute() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("attribute.html", "attribute.js", "angular.js");
    EdtTestUtil.runInEdtAndWait(() -> myFixture.testRename("attribute.after.html", "foo-bar2"));
    myFixture.checkResultByFile("attribute.js", "attribute.after.js", false);
  }

  @Test
  public void testAttributeNormalized() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("attribute.html", "attribute.js", "angular.js");
    EdtTestUtil.runInEdtAndWait(() -> myFixture.testRename("attribute.after.html", "fooBar2"));
    myFixture.checkResultByFile("attribute.js", "attribute.after.js", false);
  }

}
