package com.jetbrains.plugins.meteor.tsStubs;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.IndexingTestUtil;
import com.intellij.util.concurrency.ThreadingAssertions;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.ui.UIUtil;
import com.jetbrains.plugins.meteor.spacebars.templates.MeteorTemplateIndex;

import static com.jetbrains.plugins.meteor.ide.action.MeteorLibraryUpdaterKt.findAndInitMeteorRoots;

public final class MeteorTestUtil {

  public static String getTestDataPath() {
    return getPluginPath() + getShortTestPath();
  }

  public static String getPluginPath() {
    return PathManager.getHomePath() + getShortPluginPath();
  }

  public static String getShortPluginPath() {
    return "/contrib/Meteor";
  }

  public static String getShortTestPath() {
    return "/testData";
  }

  public static String getBasePath() {
    return getShortPluginPath() + getShortTestPath();
  }

  public static void enableMeteor() {
    System.setProperty("meteor.js", "enable");
  }

  public static void disableMeteor() {
    System.setProperty("meteor.js", "disable");
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
}
