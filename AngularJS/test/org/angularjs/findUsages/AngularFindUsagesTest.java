package org.angularjs.findUsages;

import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.usageView.UsageInfo;
import org.angularjs.AngularTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightProjectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.testNameFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Dennis.Ushakov
 */
@TestApplication
public class AngularFindUsagesTest {
  private final TestFixture<Project> projectFixture = codeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsight = codeInsightFixture(projectFixture);
  private final TestFixture<String> testName = testNameFixture();

  @BeforeEach
  void setUpTestDataPath() {
    codeInsight.get().setTestDataPath(AngularTestUtil.getBaseTestDataPath(getClass()));
  }

  @Test
  public void testDirectiveFromCSS() {
    doTest();
  }

  @Test
  public void testDirectiveFromJS() {
    doTest();
  }

  @Test
  public void testDirectiveFromHtml() {
    doTest();
  }

  private void doTest() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    final Collection<UsageInfo> infos = myFixture.testFindUsages(testName.get() + ".html", "angular.js");
    assertEquals(3, infos.size());
  }
}
