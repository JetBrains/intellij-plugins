// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.karma.execution;

import com.intellij.execution.Executor;
import com.intellij.execution.RunManager;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.sm.runner.SMRunnerConsolePropertiesProvider;
import com.intellij.javascript.JSRunProfileWithCompileBeforeLaunchOption;
import com.intellij.javascript.karma.KarmaBundle;
import com.intellij.javascript.karma.scope.KarmaScopeKind;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.tree.KarmaTestProxyFilterProvider;
import com.intellij.javascript.karma.util.KarmaUtil;
import com.intellij.javascript.nodejs.execution.AbstractNodeTargetRunProfile;
import com.intellij.javascript.nodejs.interpreter.NodeInterpreterUtil;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.testFramework.PreferableRunConfiguration;
import com.intellij.javascript.testFramework.util.JsTestFqn;
import com.intellij.javascript.testing.JsTestRunConfigurationProducer;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class KarmaRunConfiguration extends AbstractNodeTargetRunProfile
                                   implements RefactoringListenerProvider,
                                              PreferableRunConfiguration,
                                              JSRunProfileWithCompileBeforeLaunchOption,
                                              SMRunnerConsolePropertiesProvider {

  private KarmaRunSettings myRunSettings = new KarmaRunSettings.Builder().build();

  protected KarmaRunConfiguration(@NotNull Project project, @NotNull ConfigurationFactory factory, @NotNull String name) {
    super(project, factory, name);
  }

  @Override
  public void readExternal(@NotNull Element element) throws InvalidDataException {
    super.readExternal(element);
    myRunSettings = KarmaRunSettingsSerializationUtil.readXml(element);
  }

  @Override
  public void writeExternal(@NotNull Element element) throws WriteExternalException {
    super.writeExternal(element);
    KarmaRunSettingsSerializationUtil.writeXml(element, myRunSettings);
  }

  @NotNull
  public NodePackage getKarmaPackage() {
    if (isTemplate()) {
      NodePackage pkg = myRunSettings.getKarmaPackage();
      return pkg != null ? pkg : KarmaUtil.PKG_DESCRIPTOR.createPackage("");
    }
    NodePackage karmaPackage = getOrInitKarmaPackage();
    if (karmaPackage.isEmptyPath()) {
      karmaPackage = KarmaProjectSettings.getKarmaPackage(getProject());
    }
    return karmaPackage;
  }

  @NotNull
  private NodePackage getOrInitKarmaPackage() {
    NodePackage pkg = myRunSettings.getKarmaPackage();
    if (pkg == null) {
      Project project = getProject();
      VirtualFile contextFile = getContextFile();
      pkg = KarmaUtil.PKG_DESCRIPTOR.findFirstDirectDependencyPackage(project, null, contextFile);
      if (!pkg.isEmptyPath() && !KarmaUtil.isPathUnderContentRoots(project, pkg)) {
        NodePackage projectKarmaPackage = KarmaProjectSettings.getKarmaPackage(project);
        if (projectKarmaPackage.isEmptyPath()) {
          KarmaProjectSettings.setKarmaPackage(project, pkg);
        }
        pkg = KarmaUtil.PKG_DESCRIPTOR.createPackage("");
      }
      myRunSettings = myRunSettings.toBuilder().setKarmaPackage(pkg).build();
    }
    return pkg;
  }

  @Nullable
  private VirtualFile getContextFile() {
    VirtualFile f = findFile(myRunSettings.getTestFileSystemDependentPath());
    if (f == null) {
      f = findFile(myRunSettings.getConfigPathSystemDependent());
    }
    if (f == null) {
      f = findFile(myRunSettings.getWorkingDirectorySystemDependent());
    }
    return f;
  }

  @NotNull
  @Override
  public KarmaConsoleProperties createTestConsoleProperties(@NotNull Executor executor) {
    return createTestConsoleProperties(executor, null);
  }

  @NotNull
  public KarmaConsoleProperties createTestConsoleProperties(@NotNull Executor executor, @Nullable KarmaServer server) {
    KarmaTestProxyFilterProvider filterProvider = new KarmaTestProxyFilterProvider(getProject(), server);
    return new KarmaConsoleProperties(this, executor, filterProvider);
  }

  @Nullable
  @Override
  public NodeJsInterpreter getInterpreter() {
    return myRunSettings.getInterpreterRef().resolve(getProject());
  }

  @NotNull
  @Override
  public SettingsEditor<? extends AbstractNodeTargetRunProfile> createConfigurationEditor() {
    return new KarmaRunConfigurationEditor(getProject());
  }

  @Nullable
  private static VirtualFile findFile(@NotNull String path) {
    return FileUtil.isAbsolute(path) ? LocalFileSystem.getInstance().findFileByPath(path) : null;
  }

  private boolean isTemplate() {
    return RunManager.getInstance(getProject()).isTemplate(this);
  }

  @Override
  public void onNewConfigurationCreated() {
    detectWorkingDirectoryIfNeeded();
  }

  private void detectWorkingDirectoryIfNeeded() {
    if (StringUtil.isEmptyOrSpaces(myRunSettings.getWorkingDirectorySystemDependent())
        && !getProject().isDefault()
        && !RunManager.getInstance(getProject()).isTemplate(this)) {
      String configPath = myRunSettings.getConfigPathSystemIndependent();
      VirtualFile workingDir = JsTestRunConfigurationProducer.guessWorkingDirectory(getProject(), configPath);
      String workingDirectory = workingDir != null ? workingDir.getPath() : PathUtil.getParentPath(configPath);
      myRunSettings = myRunSettings.toBuilder().setWorkingDirectory(workingDirectory).build();
    }
  }

  @Nullable
  @Override
  public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) {
    return new KarmaRunProfileState(getProject(),
                                    this,
                                    env,
                                    getKarmaPackage());
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    check(getKarmaPackage());
  }

  private void check(@NotNull NodePackage karmaPackage) throws RuntimeConfigurationException {
    NodeInterpreterUtil.checkForRunConfiguration(myRunSettings.getInterpreterRef().resolve(getProject()));
    karmaPackage.validateForRunConfiguration(KarmaUtil.KARMA_PACKAGE_NAME);
    validatePath("configuration file", myRunSettings.getConfigPathSystemDependent(), true);
    validatePath("working directory", myRunSettings.getWorkingDirectorySystemDependent(), false);
    if (myRunSettings.getScopeKind() == KarmaScopeKind.TEST_FILE) {
      validatePath("test file", myRunSettings.getTestFileSystemDependentPath(), true);
    }
  }

  private static void validatePath(@NotNull String pathLabelName,
                                   @Nullable String path,
                                   boolean fileExpected) throws RuntimeConfigurationException {
    if (StringUtil.isEmptyOrSpaces(path)) {
      throw new RuntimeConfigurationError(KarmaBundle.message("run_configuration.unspecified_field.dialog.message", pathLabelName));
    }
    File file = new File(path);
    if (!file.isAbsolute() ||
        (fileExpected && !file.isFile()) ||
        (!fileExpected && !file.isDirectory())) {
      throw new RuntimeConfigurationError(KarmaBundle.message("run_configuration.no_such_file.dialog.message", pathLabelName));
    }
  }

  @NotNull
  public KarmaRunSettings getRunSettings() {
    return myRunSettings;
  }

  public void setRunSettings(@NotNull KarmaRunSettings runSettings) {
    NodePackage newKarmaPackage = runSettings.getKarmaPackage();
    NodePackage oldKarmaPackage = myRunSettings.getKarmaPackage();
    if (newKarmaPackage == null || newKarmaPackage.equals(oldKarmaPackage)) {
      myRunSettings = runSettings;
      return;
    }
    Project project = getProject();
    if (!project.isDefault() && !KarmaUtil.isPathUnderContentRoots(project, newKarmaPackage)) {
      KarmaProjectSettings.setKarmaPackage(project, newKarmaPackage);
      newKarmaPackage = new NodePackage("");
    }
    if (newKarmaPackage.isEmptyPath() && isTemplate()) {
      newKarmaPackage = null;
    }
    myRunSettings = runSettings.toBuilder().setKarmaPackage(newKarmaPackage).build();
  }

  public void setConfigFilePath(@NotNull String configFilePath) {
    myRunSettings = myRunSettings.toBuilder().setConfigPath(configFilePath).build();
  }

  @Override
  public String suggestedName() {
    KarmaRunSettings settings = myRunSettings;
    KarmaScopeKind scopeKind = settings.getScopeKind();
    if (scopeKind == KarmaScopeKind.ALL) {
      return PathUtil.getFileName(settings.getConfigPathSystemDependent());
    }
    if (scopeKind == KarmaScopeKind.TEST_FILE) {
      return PathUtil.getFileName(settings.getTestFileSystemDependentPath());
    }
    if (scopeKind == KarmaScopeKind.SUITE || scopeKind == KarmaScopeKind.TEST) {
      return JsTestFqn.getPresentableName(settings.getTestNames());
    }
    return super.suggestedName();
  }

  @Nullable
  @Override
  public String getActionName() {
    KarmaScopeKind scopeKind = myRunSettings.getScopeKind();
    if (scopeKind == KarmaScopeKind.SUITE || scopeKind == KarmaScopeKind.TEST) {
      return StringUtil.notNullize(ContainerUtil.getLastItem(myRunSettings.getTestNames()));
    }
    return super.getActionName();
  }

  @Nullable
  @Override
  public RefactoringElementListener getRefactoringElementListener(PsiElement element) {
    return KarmaRunConfigurationRefactoringHandler.getRefactoringElementListener(this, element);
  }

  @Override
  public boolean isPreferredOver(@NotNull RunConfiguration otherRc, @NotNull PsiElement sourceElement) {
    return true;
  }
}
