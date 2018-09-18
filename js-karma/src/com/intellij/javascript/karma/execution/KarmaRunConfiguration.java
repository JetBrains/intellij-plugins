// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.karma.execution;

import com.intellij.execution.Executor;
import com.intellij.execution.RunManager;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.javascript.JSRunProfileWithCompileBeforeLaunchOption;
import com.intellij.javascript.karma.scope.KarmaScopeKind;
import com.intellij.javascript.karma.server.KarmaJsSourcesLocator;
import com.intellij.javascript.karma.util.KarmaUtil;
import com.intellij.javascript.nodejs.interpreter.NodeInterpreterUtil;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageDescriptor;
import com.intellij.javascript.testFramework.PreferableRunConfiguration;
import com.intellij.javascript.testFramework.util.JsTestFqn;
import com.intellij.javascript.testing.JsTestRunConfigurationProducer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.util.ObjectUtils;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.text.SemVer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class KarmaRunConfiguration extends LocatableConfigurationBase implements RefactoringListenerProvider,
                                                                                 PreferableRunConfiguration,
                                                                                 JSRunProfileWithCompileBeforeLaunchOption {

  private KarmaRunSettings myRunSettings = new KarmaRunSettings.Builder().build();

  protected KarmaRunConfiguration(@NotNull Project project, @NotNull ConfigurationFactory factory, @NotNull String name) {
    super(project, factory, name);
  }

  @NotNull
  @Override
  public KarmaRunConfigurationEditor getConfigurationEditor() {
    return new KarmaRunConfigurationEditor(getProject());
  }

  @Override
  public void readExternal(@NotNull Element element) throws InvalidDataException {
    super.readExternal(element);
    boolean templateRunConfiguration = isTemplate(element);
    myRunSettings = KarmaRunSettingsSerializationUtil.readXml(element, getProject(), templateRunConfiguration);
    NodePackage karmaPackage = myRunSettings.getKarmaPackage();
    if (templateRunConfiguration && karmaPackage != null && karmaPackage.isEmptyPath()) {
      myRunSettings = myRunSettings.toBuilder().setKarmaPackage(null).build();
    }
  }

  @Override
  public void writeExternal(@NotNull Element element) throws WriteExternalException {
    super.writeExternal(element);
    boolean templateRunConfiguration = isTemplate(element);
    KarmaRunSettingsSerializationUtil.writeXml(element, myRunSettings, templateRunConfiguration);
  }

  @NotNull
  public NodePackage getKarmaPackage() {
    if (isTemplate()) {
      NodePackage pkg = myRunSettings.getKarmaPackage();
      return pkg != null ? pkg : new NodePackage("");
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
      NodeJsInterpreter interpreter = myRunSettings.getInterpreterRef().resolve(project);
      pkg = KarmaUtil.PKG_DESCRIPTOR.findFirstDirectDependencyPackage(project, interpreter, null);
      if (shouldPreferKarmaPackage(pkg)) {
        pkg = new NodePackageDescriptor(KarmaUtil.KARMA_PACKAGE_NAME).findFirstDirectDependencyPackage(project, interpreter, null);
      }
      if (!pkg.isEmptyPath() && !KarmaUtil.isPathUnderContentRoots(project, pkg)) {
        NodePackage projectKarmaPackage = KarmaProjectSettings.getKarmaPackage(project);
        if (projectKarmaPackage.isEmptyPath()) {
          KarmaProjectSettings.setKarmaPackage(project, pkg);
        }
        pkg = new NodePackage("");
      }
      myRunSettings = myRunSettings.toBuilder().setKarmaPackage(pkg).build();
    }
    return pkg;
  }

  private boolean shouldPreferKarmaPackage(@NotNull NodePackage pkg) {
    if (!SystemInfo.isWindows || !pkg.getSystemIndependentPath().endsWith("/" + KarmaUtil.ANGULAR_CLI__PACKAGE_NAME)) {
      return false;
    }
    SemVer version = pkg.getVersion();
    if (version == null || version.getMajor() >= 6) {
      return false;
    }
    String path1 = getProject().getBasePath();
    String path2 = KarmaJsSourcesLocator.getInstance().getKarmaIntellijPackageDir().getAbsolutePath();
    // @angular/cli < 6 doesn't work when karma-intellij on a different drive ()
    return path1 != null && FileUtil.isWindowsAbsolutePath(path1) && FileUtil.isWindowsAbsolutePath(path2) &&
           !path1.substring(0, 1).equals(path2.substring(0, 1));
  }

  private boolean isTemplate() {
    return RunManager.getInstance(getProject()).isTemplate(this);
  }

  private static boolean isTemplate(@NotNull Element element) {
    return "true".equals(element.getAttributeValue("default"));
  }

  @Override
  public void onNewConfigurationCreated() {
    if (myRunSettings.getWorkingDirectorySystemDependent().isEmpty()) {
      VirtualFile workingDir = JsTestRunConfigurationProducer.guessWorkingDirectory(getProject(), myRunSettings.getConfigPathSystemDependent());
      if (workingDir != null) {
        myRunSettings = myRunSettings.toBuilder().setWorkingDirectory(workingDir.getPath()).build();
      }
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
      throw new RuntimeConfigurationError("Unspecified " + pathLabelName);
    }
    File file = new File(path);
    if (!file.isAbsolute() ||
        (fileExpected && !file.isFile()) ||
        (!fileExpected && !file.isDirectory())) {
      throw new RuntimeConfigurationError("No such " + pathLabelName);
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
    if (!KarmaUtil.isPathUnderContentRoots(project, newKarmaPackage)) {
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
    PsiFile psiFile = ObjectUtils.tryCast(sourceElement, PsiFile.class);
    if (psiFile != null) {
      VirtualFile virtualFile = psiFile.getVirtualFile();
      if (virtualFile != null) {
        return KarmaUtil.isKarmaConfigFile(virtualFile.getNameSequence(), true);
      }
    }
    return false;
  }
}
