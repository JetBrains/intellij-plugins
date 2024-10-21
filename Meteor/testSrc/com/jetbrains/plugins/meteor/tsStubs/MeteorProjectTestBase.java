package com.jetbrains.plugins.meteor.tsStubs;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.IndexingTestUtil;
import com.intellij.testFramework.builders.ModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.util.concurrency.ThreadingAssertions;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.ui.UIUtil;
import com.jetbrains.plugins.meteor.spacebars.templates.MeteorTemplateIndex;

import static com.jetbrains.plugins.meteor.ide.action.MeteorLibraryUpdaterKt.findAndInitMeteorRoots;

public abstract class MeteorProjectTestBase extends CodeInsightFixtureTestCase<ModuleFixtureBuilder<?>> {
  @Override
  protected String getBasePath() {
    return MeteorTestUtil.getBasePath();
  }

  @Override
  protected boolean runInDispatchThread() {
    return false;
  }

  @Override
  protected void setUp() throws Exception {
    MeteorTestUtil.enableMeteor();
    super.setUp();
    initMeteorDirs(getProject());
  }

  public static void initMeteorDirs(Project project) {
    ThreadingAssertions.assertNoOwnReadAccess();
    ReadAction.run(() -> {
      FileBasedIndex.getInstance().ensureUpToDate(MeteorTemplateIndex.METEOR_TEMPLATES_INDEX, project, GlobalSearchScope.allScope(project));
      findAndInitMeteorRoots(project);
    });
    UIUtil.pump(); // invokelater in com.jetbrains.plugins.meteor.MeteorProjectStartupActivity.findMeteorRoots
    IndexingTestUtil.waitUntilIndexesAreReady(project);
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
