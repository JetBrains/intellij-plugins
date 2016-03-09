package com.intellij.lang.javascript;

import com.intellij.flex.FlexTestUtils;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;

import java.util.concurrent.Callable;

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
    doTestWithJSSupport(new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        FlexTestUtils.setupFlexSdk(getModule(), getTestName(false), ActionScriptStatementMoverTest.this.getClass());
        JSTestUtils.initJSIndexes(getProject());

        doMoveStatementTest("mxml");
        return null;
      }
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
