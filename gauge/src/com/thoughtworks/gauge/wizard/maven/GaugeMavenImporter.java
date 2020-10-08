package com.thoughtworks.gauge.wizard.maven;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.thoughtworks.gauge.wizard.GaugeModuleImporter;
import com.thoughtworks.gauge.wizard.GaugeTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.concurrency.AsyncPromise;
import org.jetbrains.idea.maven.execution.MavenRunner;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.execution.MavenRunnerSettings;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.io.File;
import java.io.IOException;
import java.util.*;

final class GaugeMavenImporter implements GaugeModuleImporter {

  private static final String GAUGE_SELENIUM_ARCHETYPE = "gauge-archetype-selenium";
  private static final String GAUGE_ARCHETYPE = "gauge-archetype-java";
  private static final String GAUGE_ARCHETYPE_GROUP = "com.thoughtworks.gauge.maven";

  private static final String ARCHETYPE_ID = "gauge-test";

  @Override
  public String getId() {
    return "maven";
  }

  @Override
  public AsyncPromise<Void> importModule(@NotNull Module module, GaugeTemplate selectedTemplate) {
    return generateFromArchetype(module, selectedTemplate);
  }

  private static AsyncPromise<Void> generateFromArchetype(@NotNull Module module, GaugeTemplate selectedTemplate) {
    String modulePath = ModuleRootManager.getInstance(module).getContentRoots()[0].getCanonicalPath();
    if (modulePath == null) return new AsyncPromise<>();

    File workingDir;
    try {
      workingDir = FileUtil.createTempDirectory("gauge-archetype", "tmp");
    }
    catch (IOException e) {
      Logger.getInstance(GaugeMavenImporter.class).error(e);
      return new AsyncPromise<>();
    }

    String archetype = null;
    if ("java_maven".equals(selectedTemplate.templateId)) {
      archetype = GAUGE_ARCHETYPE;
    }
    else if ("java_maven_selenium".equals(selectedTemplate.templateId)) {
      archetype = GAUGE_SELENIUM_ARCHETYPE;
    }

    if (archetype == null) return new AsyncPromise<>();

    MavenRunnerParameters params = new MavenRunnerParameters(
      false, workingDir.getPath(), (String)null,
      Collections.singletonList("org.apache.maven.plugins:maven-archetype-plugin:RELEASE:generate"),
      Collections.emptyList());

    MavenRunner runner = MavenRunner.getInstance(module.getProject());
    MavenRunnerSettings settings = runner.getState().clone();
    Map<String, String> props = settings.getMavenProperties();

    props.put("interactiveMode", "false");
    props.put("archetypeGroupId", GAUGE_ARCHETYPE_GROUP);
    props.put("archetypeArtifactId", archetype);

    props.put("groupId", "org.example");
    props.put("artifactId", ARCHETYPE_ID);
    props.put("version", "1.0-SNAPSHOT");

    AsyncPromise<Void> promise = new AsyncPromise<>();
    runner.run(params, settings, () -> {
      copyGeneratedFiles(workingDir, new File(modulePath));
      runAfterSetup(module);

      promise.setResult(null);
    });

    return promise;
  }

  private static void copyGeneratedFiles(File workingDir, File moduleDir) {
    try {
      FileUtil.copyDir(new File(workingDir, ARCHETYPE_ID), moduleDir);
      FileUtil.delete(workingDir);
    }
    catch (Exception e) {
      Logger.getInstance(GaugeMavenImporter.class).error(e);
      return;
    }
    LocalFileSystem.getInstance().refresh(false);
  }

  private static void runAfterSetup(Module module) {
    Project project = module.getProject();

    List<VirtualFile> pomXmls = new ArrayList<>();

    for (VirtualFile contentRoot : ModuleRootManager.getInstance(module).getContentRoots()) {
      collectPomXml(contentRoot, pomXmls);
      if (!pomXmls.isEmpty()) break;
    }

    if (pomXmls.isEmpty()) {
      for (VirtualFile contentRoot : ModuleRootManager.getInstance(module).getContentRoots()) {
        for (VirtualFile child : contentRoot.getChildren()) {
          if (child.isDirectory()) {
            collectPomXml(child, pomXmls);
          }
        }
      }
    }

    if (pomXmls.isEmpty()) {
      return;
    }

    MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(project);
    mavenProjectsManager.addManagedFiles(pomXmls);
  }

  private static void collectPomXml(VirtualFile directoryFrom, Collection<VirtualFile> collectionInto) {
    VirtualFile child = directoryFrom.findChild("pom.xml");
    if (child != null) {
      collectionInto.add(child);
    }
  }
}
