// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.integration;


import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.execution.testframework.sm.UITestUtil;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerTestTreeView;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.util.ExecUtil;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.JavaAwareProjectJdkTableImpl;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.HeavyPlatformTestCase;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.model.MavenExplicitProfiles;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.plugins.cucumber.java.run.CucumberJavaFeatureRunConfigurationProducer;
import org.jetbrains.plugins.cucumber.java.run.CucumberJavaRunConfigurationProducer;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.intellij.openapi.util.io.FileUtil.copyDirContent;
import static com.intellij.testFramework.PlatformTestUtil.assertTreeEqual;

public class CucumberJavaIntegrationTest extends HeavyPlatformTestCase {
  private static final String EXPECTED_TREE_OF_TESTS =
    "-[root]\n" +
    " -Feature: test\n" +
    "  -Scenario: passing\n" +
    "   Given normal step\n" +
    "   And step with parameter \"param\"\n" +
    "  -Scenario: failing\n" +
    "   Given normal step\n" +
    "   Given failing step\n" +
    "  -Scenario: failing comparison\n" +
    "   Given normal step\n" +
    "   Given failing comparing step\n" +
    "  -Scenario: pending\n" +
    "   Given normal step\n" +
    "   Given pending step\n" +
    "  -Scenario: undefined\n" +
    "   Given normal step\n" +
    "   Given undefined step\n" +
    "  -Scenario: lambda passing\n" +
    "   Given normal step lambda\n" +
    "  -Scenario: lambda failing\n" +
    "   Given normal step lambda\n" +
    "   Given failing step lambda\n" +
    "  -Scenario: lambda pending\n" +
    "   Given normal step lambda\n" +
    "   Given pending step lambda\n" +
    "  -Scenario Outline: outline\n" +
    "   -Examples:\n" +
    "    -Scenario: Line: 39\n" +
    "     Given normal step\n" +
    "     And step with parameter \"value1\"\n" +
    "     And step with parameter \"value1\"\n" +
    "    -Scenario: Line: 40\n" +
    "     Given normal step\n" +
    "     And step with parameter \"value2\"\n" +
    "     And step with parameter \"value2\"";

  public void testCucumber_java_1_0() throws ExecutionException, InterruptedException {
    doTest();
  }

  public void testCucumber_java_1_2() throws ExecutionException, InterruptedException {
    doTest();
  }

  public void testCucumber_java_2_0() throws ExecutionException, InterruptedException {
    doTest();
  }

  public void testCucumber_java_2_4() throws ExecutionException, InterruptedException {
    doTest();
  }

  public void testCucumber_java_3_0() throws ExecutionException, InterruptedException {
    doTest();
  }

  public void testCucumber_java_4_5() throws ExecutionException, InterruptedException {
    doTest();
  }

  public void testCucumber_java_4_7() throws ExecutionException, InterruptedException {
    doTest();
  }

  public void testCucumber_java_5_0() throws ExecutionException, InterruptedException {
    doTest();
  }

  protected void doTest() throws ExecutionException, InterruptedException {
    String mavenHomePath = System.getenv("maven.path");
    if (mavenHomePath == null) {
      fail("missing system property 'maven.path'");
    }
    String mvnRelativePath = "/bin/mvn";
    if (SystemInfo.isWindows) {
      mvnRelativePath += ".cmd";
    }
    runExternalCommand(mavenHomePath + mvnRelativePath, "package");

    Project project = getProject();
    CucumberJavaRunConfigurationProducer cucumberJavaRunConfigurationProducer = new CucumberJavaFeatureRunConfigurationProducer();
    PsiElement element = PlatformTestUtil.findElementBySignature("my feature", "src/test/resources/test.feature", project);
    RunConfiguration runConfiguration = PlatformTestUtil.getRunConfiguration(element, cucumberJavaRunConfigurationProducer);
    RunContentDescriptor runContentDescriptor = PlatformTestUtil.executeConfiguration(runConfiguration).getContentToReuse();

    SMTRunnerTestTreeView smtRunnerTestTreeView = UITestUtil.getTreeOfTests(runContentDescriptor);
    assertTreeEqual(smtRunnerTestTreeView, EXPECTED_TREE_OF_TESTS);
  }

  @Nullable
  @Override
  protected Sdk getTestProjectJdk() {
    return JavaAwareProjectJdkTableImpl.getInstanceEx().getInternalJdk();
  }

  protected void runExternalCommand(@NotNull String... command) throws ExecutionException {
    GeneralCommandLine commandLine = new GeneralCommandLine(command);

    commandLine.setWorkDirectory(getProject().getBasePath());
    ProcessOutput output = ExecUtil.execAndGetOutput(commandLine);
    assertEquals(String.format("Exit code of the '%s' command is: %d, output: %s", commandLine.getCommandLineString(), output.getExitCode(), output.getStdout()), 0, output.getExitCode());
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

    UIUtil.invokeAndWaitIfNeeded((Runnable)() -> {
      mavenProjectsManager.waitForResolvingCompletion();
      mavenProjectsManager.scheduleImportInTests(pomFiles);
      mavenProjectsManager.importProjects();
    });

    return ReadAction.compute(() -> ModuleManager.getInstance(myProject).findModuleByName("cucumber-java-sample"));
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
}
