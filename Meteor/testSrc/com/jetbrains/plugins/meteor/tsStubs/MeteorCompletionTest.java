package com.jetbrains.plugins.meteor.tsStubs;

import com.intellij.lang.javascript.BaseJSCompletionTestCase;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.ui.UIUtil;
import com.jetbrains.plugins.meteor.spacebars.templates.MeteorTemplateIndex;

public class MeteorCompletionTest extends BaseJSCompletionTestCase {
  @Override
  protected boolean runInDispatchThread() {
    return false;
  }

  @Override
  protected String getTestDataPath() {
    return MeteorTestUtil.getTestDataPath() + "/testCompletion/completion";
  }

  @Override
  protected String getExtension() {
    return "js";
  }

  public void _testTemplatesCompletion() {
    //final String fullPath = getTestDataPath() + "module";
    //final VirtualFile module = LocalFileSystem.getInstance().findFileByPath(fullPath.replace(File.separatorChar, '/'));
    //addSourceContentToRoots(myModule, module);

    doTest("");
  }

  public void testTemplatesNotCompletion() {
    doTest("");
  }

  @Override
  protected void setUp() throws Exception {
    MeteorTestUtil.enableMeteor();
    super.setUp();
    FileBasedIndex.getInstance().requestRebuild(MeteorTemplateIndex.METEOR_TEMPLATES_INDEX);
    UIUtil.pump();
    MeteorProjectTestBase.initMeteorDirs(getProject());
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      MeteorTestUtil.disableMeteor();
    }
    catch (Throwable e) {
      addSuppressedException(e);
    }
    finally {
      super.tearDown();
    }
  }
}
