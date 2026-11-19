package org.angularjs.performance;

import com.intellij.openapi.project.Project;
import com.intellij.testFramework.PerformanceUnitTest;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.StressTestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.tools.ide.metrics.benchmark.Benchmark;
import org.angularjs.AngularTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightProjectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.testNameFixture;

/**
 * @author Konstantin.Ulitin
 */
@StressTestApplication
@PerformanceUnitTest
public class AngularJSPerformanceTest {
  private final TestFixture<Project> projectFixture = codeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsight = codeInsightFixture(projectFixture);
  private final TestFixture<String> testNameUpper = testNameFixture(false);

  @BeforeEach
  void setUpTestDataPath() {
    codeInsight.get().setTestDataPath(AngularTestUtil.getBaseTestDataPath(getClass()));
  }

  @Test
  public void testManyInjectionsHighlighting() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("manyInjections.highlight.html", "angular.js", "custom.js");
    Benchmark.newBenchmark(testNameUpper.get(), () -> myFixture.checkHighlighting()).attempts(1).start();
  }

}
