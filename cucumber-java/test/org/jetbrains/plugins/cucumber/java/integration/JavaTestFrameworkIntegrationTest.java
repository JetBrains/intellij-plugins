// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.integration;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.execution.testframework.sm.UITestUtil;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerTestTreeView;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.util.ExecUtil;
import com.intellij.maven.testFramework.utils.MavenImportingTestCaseKt;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.JavaAwareProjectJdkTableImpl;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.HeavyPlatformTestCase;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.model.MavenExplicitProfiles;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.intellij.openapi.util.io.FileUtil.copyDirContent;
import static com.intellij.testFramework.PlatformTestUtil.assertTreeEqual;

public abstract class JavaTestFrameworkIntegrationTest extends HeavyPlatformTestCase {
  @Nullable
  @Override
  protected Sdk getTestProjectJdk() {
    return JavaAwareProjectJdkTableImpl.getInstanceEx().getInternalJdk();
  }

  protected void runExternalCommand(String @NotNull ... command) throws ExecutionException {
    GeneralCommandLine commandLine = new GeneralCommandLine(command);

    commandLine.setWorkDirectory(getProject().getBasePath());
    ProcessOutput output = ExecUtil.execAndGetOutput(commandLine);
    LOG.info(
      String.format("Command '%s'\nstdout: %s\nstderr: %s", commandLine.getCommandLineString(), output.getStdout(), output.getStderr()));
  }
  
  @NotNull
  @Override
  protected Module createMainModule() throws IOException {
    copyTestDataToProjectDir();

    VirtualFile pomXmlFile = LocalFileSystem.getInstance().findFileByPath(getProject().getBasePath() + File.separator + "pom.xml");
    List<VirtualFile> pomFiles = Collections.singletonList(pomXmlFile);

    MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(getProject());
    mavenProjectsManager.initForTests();
    mavenProjectsManager.resetManagedFilesAndProfilesInTests(pomFiles, new MavenExplicitProfiles(Collections.emptyList()));
    mavenProjectsManager.waitForReadingCompletion();

    UIUtil.invokeAndWaitIfNeeded(() -> {
      mavenProjectsManager.waitForResolvingCompletion();
    });
    MavenImportingTestCaseKt.importMavenProjectsSync(mavenProjectsManager, pomFiles);

    Module module = ReadAction.compute(() -> ModuleManager.getInstance(myProject).findModuleByName("test-project-sample"));
    ModuleRootModificationUtil.updateModel(module, model -> {
      String sourceUrl = VfsUtilCore.pathToUrl(getProject().getBasePath() + "/test");
      ContentEntry contentEntry = model.addContentEntry(sourceUrl);
      contentEntry.addSourceFolder(sourceUrl + "/java", true);
      
      CompilerModuleExtension moduleExtension = model.getModuleExtension(CompilerModuleExtension.class);
      moduleExtension.inheritCompilerOutputPath(false);
      moduleExtension.setCompilerOutputPathForTests(VfsUtilCore.pathToUrl(getProject().getBasePath()  + File.separator + "target" + File.separator + "test-classes"));
    });

    return module;
  }

  private void copyTestDataToProjectDir() throws IOException {
    String testDataPath = getTestDataPath();
    String basePath = getProject().getBasePath();
    copyDirContent(new File(testDataPath), new File(basePath));
    VirtualFile projectDirectory = LocalFileSystem.getInstance().findFileByPath(basePath);
    LocalFileSystem.getInstance().refreshFiles(Collections.singletonList(projectDirectory));
  }

  @NotNull
  private String getTestDataPath() {
    return FileUtil.toSystemIndependentName(PathManager.getHomePath() + "/contrib/cucumber-java/testData/integration/" + getTestName(true));
  }

  protected void doTest(@NotNull String expectedTreeOfTests) throws ExecutionException, InterruptedException {
    String mavenHomePath = System.getenv("maven.path");
    if (mavenHomePath == null) {
      mavenHomePath  = "mvn";
    } else {
      mavenHomePath += "/bin/mvn";
    }

    String suffix = "";
    if (SystemInfo.isWindows) {
      suffix += ".cmd";
    }
    runExternalCommand(mavenHomePath + suffix, "package");

    RunConfiguration runConfiguration = getRunConfiguration();

    RunContentDescriptor runContentDescriptor = PlatformTestUtil.executeConfigurationAndWait(runConfiguration).getContentToReuse();

    SMTRunnerTestTreeView smtRunnerTestTreeView = UITestUtil.getTreeOfTests(runContentDescriptor);
    assertTreeEqual(smtRunnerTestTreeView, expectedTreeOfTests);
  }
  
  protected abstract RunConfiguration getRunConfiguration();
}
