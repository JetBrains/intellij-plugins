package org.intellij.plugins.postcss.fileStructure;

import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.FileStructureTestFixture;
import com.intellij.testFramework.FileStructureTestUtil;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.intellij.plugins.postcss.PostCssTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.nio.file.Path;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.moduleFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.tempPathFixture;

@TestApplication
@TestDataPath("$PROJECT_ROOT/contrib/postcss/tests/testData/fileStructure/")
public class PostCssFileStructureTest {
  @SuppressWarnings("deprecation")
  private static final TestFixture<Project> projectFixture = projectFixture(tempPathFixture(), OpenProjectTask.build(), true);

  private final TestFixture<Path> pathFixture = tempPathFixture();
  @SuppressWarnings("unused")
  private final TestFixture<Module> moduleFixture = moduleFixture(projectFixture, pathFixture, true);
  private final TestFixture<CodeInsightTestFixture> codeInsightFixture = codeInsightFixture(projectFixture, pathFixture);

  private CodeInsightTestFixture myFixture;
  private FileStructureTestFixture myPopupFixture;
  private String myTestMethodName;

  @BeforeEach
  void setUp(TestInfo testInfo) {
    myFixture = codeInsightFixture.get();
    myFixture.setTestDataPath(IdeaTestExecutionPolicy.getHomePathWithPolicy() + PostCssTestUtils.getTestDataBasePath(getClass()));
    myPopupFixture = new FileStructureTestFixture(myFixture);
    myTestMethodName = testInfo.getTestMethod().orElseThrow().getName();
  }

  @AfterEach
  void tearDown() {
    EdtTestUtil.runInEdtAndWait(() -> Disposer.dispose(myPopupFixture));
  }

  @Test
  public void testNestAtRules() {
    checkTree();
  }

  @Test
  public void testAmpersand() {
    checkTree();
  }

  @Test
  public void testAtRules() {
    checkTree();
  }

  @Test
  public void testCustomSelectors() {
    checkTree();
  }

  @Test
  public void testCustomSelectorsWithNesting() {
    checkTree();
  }

  @Test
  public void testCustomMedia() {
    checkTree();
  }

  @Test
  public void testCustomMediaWithNesting() {
    checkTree();
  }

  @Test
  public void testApplyAtRule() {
    checkTree();
  }

  @Test
  public void testApplyAtRuleWithNesting() {
    checkTree();
  }

  private void checkTree() {
    FileStructureTestUtil.checkTree(myFixture, myPopupFixture, myTestMethodName, "pcss", null, true);
  }
}
