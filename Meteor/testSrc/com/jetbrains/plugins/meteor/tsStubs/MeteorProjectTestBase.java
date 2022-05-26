package com.jetbrains.plugins.meteor.tsStubs;

import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.builders.ModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.ui.UIUtil;
import com.jetbrains.plugins.meteor.ide.action.MeteorLibraryUpdater;
import com.jetbrains.plugins.meteor.spacebars.templates.MeteorTemplateIndex;

public abstract class MeteorProjectTestBase extends CodeInsightFixtureTestCase<ModuleFixtureBuilder<?>> {
  @Override
  protected String getBasePath() {
    return MeteorTestUtil.getBasePath();
  }

  @Override
  protected void setUp() throws Exception {
    MeteorTestUtil.enableMeteor();
    super.setUp();
    initMeteorDirs(getProject());
  }

  public static void initMeteorDirs(Project project) {
    FileBasedIndex.getInstance().ensureUpToDate(MeteorTemplateIndex.METEOR_TEMPLATES_INDEX, project, GlobalSearchScope.allScope(project));
    MeteorLibraryUpdater.findAndInitMeteorRoots(project);
    UIUtil.dispatchAllInvocationEvents(); // invokelater in com.jetbrains.plugins.meteor.MeteorProjectStartupActivity.findMeteorRoots
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
