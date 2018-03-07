package com.intellij.flex.codeInsight;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.lang.javascript.JSStatementMoverTestBase;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

public class ActionScriptStatementMoverTest extends JSStatementMoverTestBase {
  @Override
  protected void setUp() throws Exception {
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));
    super.setUp();
  }

  @NotNull
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("statementMover/");
  }

  public void testMoveStatement7() {
    doMoveStatementTest("js2");
  }

  public void testMoveStatementInMxml() {
    FlexTestUtils.setupFlexSdk(myFixture.getModule(), getTestName(false), this.getClass(), getTestRootDisposable());
    JSTestUtils.initJSIndexes(getProject());

    myFixture.setCaresAboutInjection(false);
    doMoveStatementTest("mxml");
  }

  public void testMoveFunctionInClass() {
    doMoveStatementTest("js2");
  }

  public void testMoveAttribute() {
    doMoveStatementTest("js2");
  }

  public void testIdea_70049() {
    doMoveStatementTest("as");
  }

  public void testMoveStatement11() {
    doMoveStatementTest("js2");
  }

  public void testMoveStatement13() {
    doMoveStatementTest("js2");
  }
}
