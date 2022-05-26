package com.jetbrains.plugins.meteor.tsStubs;

import com.intellij.lang.javascript.BaseJSCompletionTestCase;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.plugins.meteor.spacebars.templates.MeteorTemplateIndex;

public class MeteorCompletionTest extends BaseJSCompletionTestCase {

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
    PlatformTestUtil.dispatchAllEventsInIdeEventQueue();
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
