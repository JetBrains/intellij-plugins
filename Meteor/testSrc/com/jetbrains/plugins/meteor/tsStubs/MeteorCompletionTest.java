package com.jetbrains.plugins.meteor.tsStubs;

import com.intellij.lang.javascript.JSCompletionFixture;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.IndexingTestUtil;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.ui.UIUtil;
import com.jetbrains.plugins.meteor.spacebars.templates.MeteorTemplateIndex;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.JSCompletionFixture.jsCompletionFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;

@TestApplication
public class MeteorCompletionTest {
  private final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSCompletionFixture> js =
    jsCompletionFixture(projectFixture, new JSFixtureOptions(MeteorTestUtil.getTestDataPath() + "/testCompletion/completion", "js"));

  public void _testTemplatesCompletion() {
    //final String fullPath = getTestDataPath() + "module";
    //final VirtualFile module = LocalFileSystem.getInstance().findFileByPath(fullPath.replace(File.separatorChar, '/'));
    //addSourceContentToRoots(myModule, module);

    js.get().doTest("");
  }

  @Test
  public void testTemplatesNotCompletion() {
    js.get().doTest("");
  }

  @BeforeEach
  void setUp() {
    MeteorTestUtil.enableMeteor();
    FileBasedIndex.getInstance().requestRebuild(MeteorTemplateIndex.METEOR_TEMPLATES_INDEX);
    UIUtil.pump();
    IndexingTestUtil.waitUntilIndexesAreReady(js.get().getProject());
    MeteorTestUtil.initMeteorDirs(js.get().getProject());
  }

  @AfterEach
  void tearDown() {
    MeteorTestUtil.disableMeteor();
  }
}
