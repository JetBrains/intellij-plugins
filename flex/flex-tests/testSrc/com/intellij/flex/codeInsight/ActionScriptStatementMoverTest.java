package com.intellij.flex.codeInsight;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.lang.javascript.JSStatementMoverTestBase;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

public class ActionScriptStatementMoverTest extends JSStatementMoverTestBase {
  @Override
  protected void setUp() throws Exception {
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));
    super.setUp();
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("statementMover/");
  }

  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  public void testMoveStatement7() throws Exception {
    doMoveStatementTest("js2");
  }

  public void testMoveStatementInMxml() throws Exception {
    doTestWithJSSupport(() -> {
      FlexTestUtils.setupFlexSdk(getModule(), getTestName(false), this.getClass(), getTestRootDisposable());
      JSTestUtils.initJSIndexes(getProject());

      doMoveStatementTest("mxml");
      return null;
    });
  }

  public void testMoveFunctionInClass() throws Exception {
    doMoveStatementTest("js2");
  }

  public void testMoveAttribute() throws Exception {
    doMoveStatementTest("js2");
  }

  public void testIdea_70049() throws Exception {
    doMoveStatementTest("as");
  }

  public void testMoveStatement11() throws Exception {
    doMoveStatementTest("js2");
  }

  public void testMoveStatement13() throws Exception {
    doMoveStatementTest("js2");
  }
}
