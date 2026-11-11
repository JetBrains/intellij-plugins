package com.intellij.flex.codeInsight;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.JSStatementMoverFixture;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.JSStatementMoverFixture.jsStatementMoverFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;

@TestApplication
public class ActionScriptStatementMoverTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSStatementMoverFixture> mover =
    jsStatementMoverFixture(projectFixture, new JSFixtureOptions(FlexTestUtils.getTestDataPath("statementMover/")));

  @BeforeEach
  void setUp() {
    FlexTestUtils.allowFlexVfsRootsFor(mover.get().getTestRootDisposable(), "statementMover/");
  }

  @Test
  public void testMoveStatement7() {
    mover.get().doMoveStatementTest("js2");
  }

  @Test
  public void testMoveStatementInMxml() {
    JSStatementMoverFixture moverFixture = mover.get();
    FlexTestUtils.setupFlexSdk(moverFixture.getFixture().getModule(), moverFixture.getTestName(false), this.getClass(),
                               moverFixture.getTestRootDisposable());
    JSTestUtils.initJSIndexes(moverFixture.getProject());

    moverFixture.getFixture().setCaresAboutInjection(false);
    moverFixture.doMoveStatementTest("mxml");
  }

  @Test
  public void testMoveFunctionInClass() {
    mover.get().doMoveStatementTest("js2");
  }

  @Test
  public void testMoveAttribute() {
    mover.get().doMoveStatementTest("js2");
  }

  @Test
  public void testIdea_70049() {
    mover.get().doMoveStatementTest("as");
  }

  @Test
  public void testMoveStatement11() {
    mover.get().doMoveStatementTest("js2");
  }

  @Test
  public void testMoveStatement13() {
    mover.get().doMoveStatementTest("js2");
  }
}
