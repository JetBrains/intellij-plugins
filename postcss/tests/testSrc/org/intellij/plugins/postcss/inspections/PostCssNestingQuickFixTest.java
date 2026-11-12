package org.intellij.plugins.postcss.inspections;

import com.intellij.openapi.project.Project;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.intellij.plugins.postcss.PostCssFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;
import static org.intellij.plugins.postcss.PostCssFixtureKt.postCssFixture;

@TestApplication
@TestDataPath("$PROJECT_ROOT/contrib/postcss/tests/testData/inspections/nesting")
public class PostCssNestingQuickFixTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<PostCssFixture> postCss = postCssFixture(projectFixture);

  @BeforeEach
  void setUp() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.enableInspections(PostCssNestingInspection.class);
  }

  @Test
  public void testDeleteAmpersand() {
    doTestDeleteAmpersand();
  }

  @Test
  public void testDeleteAmpersandFromClass() {
    doTestDeleteAmpersand();
  }

  @Test
  public void testDeleteAmpersandFromId() {
    doTestDeleteAmpersand();
  }

  @Test
  public void testDeleteAmpersandFromSimpleSelector() {
    doTestDeleteAmpersand();
  }

  @Test
  public void testDeleteAmpersandFromPseudoClass() {
    doTestDeleteAmpersand();
  }

  @Test
  public void testDeleteAmpersandFromPseudoFunction() {
    doTestDeleteAmpersand();
  }

  @Test
  public void testDeleteAmpersandFromAttribute() {
    doTestDeleteAmpersand();
  }

  @Test
  public void testDeleteNest() {
    postCss.get().doTestQuickFix("Delete '@nest'");
  }

  @Test
  public void testAddAmpersand() {
    postCss.get().doTestQuickFix("Add '&' to selector");
  }

  @Test
  public void testAddNest() {
    postCss.get().doTestQuickFix("Add '@nest' to selector");
  }

  private void doTestDeleteAmpersand(){
    postCss.get().doTestQuickFix("Delete '&'");
  }
}