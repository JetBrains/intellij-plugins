package com.intellij.javascript.karma.execution;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.javascript.karma.util.KarmaUtil;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.testFramework.PreferableRunConfiguration;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.util.ObjectUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Objects;

public class KarmaRunConfiguration extends LocatableConfigurationBase implements RefactoringListenerProvider,
                                                                                 PreferableRunConfiguration {

  private static final Logger LOG = Logger.getInstance(KarmaRunConfiguration.class);

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
  public void readExternal(Element element) throws InvalidDataException {
    super.readExternal(element);
    myRunSettings = KarmaRunSettingsSerializationUtil.readXml(element);
    NodePackage karmaPackage = myRunSettings.getKarmaPackage();
    if ("true".equals(element.getAttributeValue("default")) && karmaPackage != null && karmaPackage.isEmptyPath()) {
      myRunSettings = myRunSettings.builder().setKarmaPackage(null).build();
    }
  }

  @Override
  public void writeExternal(Element element) throws WriteExternalException {
    super.writeExternal(element);
    KarmaRunSettingsSerializationUtil.writeXml(element, myRunSettings);
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
      NodeJsLocalInterpreter interpreter = NodeJsLocalInterpreter.tryCast(myRunSettings.getInterpreterRef().resolve(project));
      pkg = NodePackage.findPreferredPackage(project, KarmaUtil.NODE_PACKAGE_NAME, interpreter);
      if (!pkg.isEmptyPath() && !KarmaUtil.isPathUnderContentRoots(project, pkg)) {
        NodePackage projectKarmaPackage = KarmaProjectSettings.getKarmaPackage(project);
        if (projectKarmaPackage.isEmptyPath()) {
          KarmaProjectSettings.setKarmaPackage(project, pkg);
        }
        pkg = new NodePackage("");
      }
      myRunSettings = myRunSettings.builder().setKarmaPackage(pkg).build();
    }
    return pkg;
  }

  private boolean isTemplate() {
    return getTemplateRunConfiguration(getProject()) == this;
  }

  @Nullable
  private static KarmaRunConfiguration getTemplateRunConfiguration(@NotNull Project project) {
    if (project.isDisposed()) {
      return null;
    }
    RunManager runManager = RunManager.getInstance(project);
    RunnerAndConfigurationSettings templateSettings = runManager.getConfigurationTemplate(KarmaConfigurationType.getFactory());
    RunConfiguration rc = templateSettings.getConfiguration();
    if (rc instanceof KarmaRunConfiguration) {
      return (KarmaRunConfiguration)rc;
    }
    LOG.warn("No Karma template run configuration found: " + rc);
    return null;
  }

  @Nullable
  @Override
  public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
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
    NodeJsInterpreter interpreter = myRunSettings.getInterpreterRef().resolve(getProject());
    NodeJsLocalInterpreter.checkForRunConfiguration(interpreter);
    karmaPackage.validateForRunConfiguration(KarmaUtil.NODE_PACKAGE_NAME);
    String configPath = myRunSettings.getConfigPath();
    if (configPath.trim().isEmpty()) {
      throw new RuntimeConfigurationError("Please specify config file path");
    }
    File configFile = new File(configPath);
    if (!configFile.exists()) {
      throw new RuntimeConfigurationError("Configuration file does not exist");
    }
    if (!configFile.isFile()) {
      throw new RuntimeConfigurationError("Please specify config file path correctly");
    }
  }

  @NotNull
  public KarmaRunSettings getRunSettings() {
    return myRunSettings;
  }

  public void setRunSettings(@NotNull KarmaRunSettings runSettings) {
    NodePackage newKarmaPackage = Objects.requireNonNull(runSettings.getKarmaPackage());
    NodePackage oldKarmaPackage = myRunSettings.getKarmaPackage();
    if (newKarmaPackage.equals(oldKarmaPackage)) {
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
    myRunSettings = runSettings.builder().setKarmaPackage(newKarmaPackage).build();
  }

  public void setConfigFilePath(@NotNull String configFilePath) {
    myRunSettings = myRunSettings.builder().setConfigPath(configFilePath).build();
  }

  @Override
  public String suggestedName() {
    File file = new File(myRunSettings.getConfigPath());
    return file.getName();
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
