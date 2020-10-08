package com.thoughtworks.gauge.wizard.gradle;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.thoughtworks.gauge.GaugeBundle;
import com.thoughtworks.gauge.wizard.GaugeModuleImporter;
import com.thoughtworks.gauge.wizard.GaugeTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.concurrency.AsyncPromise;
import org.jetbrains.plugins.gradle.service.project.open.GradleProjectImportUtil;
import org.jetbrains.plugins.gradle.util.GradleConstants;

import java.io.File;
import java.io.IOException;

final class GaugeGradleImporter implements GaugeModuleImporter {
  @Override
  public String getId() {
    return "gradle";
  }

  @Override
  public AsyncPromise<Void> importModule(@NotNull Module module, GaugeTemplate selectedTemplate) {
    return runAfterSetup(module);
  }

  private static AsyncPromise<Void> runAfterSetup(Module module) {
    Project project = module.getProject();

    File gradleFile = findGradleFile(module);
    if (gradleFile == null) return new AsyncPromise<>();

    String rootDirectory = gradleFile.getParent();
    fixGradlewExecutableFlag(gradleFile.getParentFile());

    WriteCommandAction.runWriteCommandAction(project, GaugeBundle.message("gauge.gav.parameters"), GaugeBundle.GAUGE, () -> {
      fixGroupArtifactId(module);
    });

    if (!GradleProjectImportUtil.canLinkAndRefreshGradleProject(rootDirectory, project)) return new AsyncPromise<>();
    GradleProjectImportUtil.linkAndRefreshGradleProject(rootDirectory, project);

    AsyncPromise<Void> promise = new AsyncPromise<>();
    promise.setResult(null);
    return promise;
  }

  private static File findGradleFile(@NotNull Module module) {
    for (VirtualFile contentRoot : ModuleRootManager.getInstance(module).getContentRoots()) {
      File baseDir = VfsUtilCore.virtualToIoFile(contentRoot);

      File file = new File(baseDir, GradleConstants.DEFAULT_SCRIPT_NAME);
      if (file.exists()) return file;

      file = new File(baseDir, GradleConstants.KOTLIN_DSL_SCRIPT_NAME);
      if (file.exists()) return file;
    }
    return null;
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  private static void fixGradlewExecutableFlag(File containingDir) {
    File toFix = new File(containingDir, "gradlew");
    if (toFix.exists()) {
      toFix.setExecutable(true, false);
    }
  }

  private static void fixGroupArtifactId(Module module) {
    VirtualFile modulePathFile = ModuleRootManager.getInstance(module).getContentRoots()[0];

    VirtualFile buildGradle = modulePathFile.findChild("build.gradle");
    if (buildGradle != null) {
      try {
        String buildText = VfsUtilCore.loadText(buildGradle)
          .replace("'Gradle_example'", "'org.example'")
          .replace("'com.thoughtworks.gauge:gauge-java:0.7.2'",
                   "'com.thoughtworks.gauge:gauge-java:" + MINIMAL_GAUGE_JAVA_VERSION + "'"); // patch version
        VfsUtil.saveText(buildGradle, buildText);
      }
      catch (IOException e) {
        Logger.getInstance(GaugeGradleImporter.class).error(e);
      }
    }

    VirtualFile settingsGradle = modulePathFile.findChild("settings.gradle");
    if (settingsGradle != null) {
      try {
        String settingsText = VfsUtilCore.loadText(settingsGradle)
          .replace("'Gradle_example'", "'gauge-example'");
        VfsUtil.saveText(settingsGradle, settingsText);
      }
      catch (IOException e) {
        Logger.getInstance(GaugeGradleImporter.class).error(e);
      }
    }
  }
}
