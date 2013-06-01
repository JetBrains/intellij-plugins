package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.configurations.LocatableConfiguration;
import com.intellij.execution.junit.RuntimeConfigurationProducer;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartCommandLineRuntimeConfigurationProducer extends RuntimeConfigurationProducer {
  private PsiElement mySourceElement;

  public DartCommandLineRuntimeConfigurationProducer() {
    super(DartCommandLineRunConfigurationType.getInstance());
  }

  @Override
  public PsiElement getSourceElement() {
    return mySourceElement;
  }

  @Override
  protected RunnerAndConfigurationSettings createConfigurationByElement(Location location, ConfigurationContext context) {
    if (!(location instanceof PsiLocation)) return null;

    PsiElement element = location.getPsiElement();
    PsiFile containingFile = element.getContainingFile();
    if (!DartResolveUtil.isLibraryRoot(containingFile)) return null;

    final RunnerAndConfigurationSettings settings = cloneTemplateConfiguration(location.getProject(), context);
    final LocatableConfiguration runConfig = (LocatableConfiguration)settings.getConfiguration();
    if (!(runConfig instanceof DartCommandLineRunConfiguration)) {
      return null;
    }

    final DartCommandLineRunConfiguration commandLineRunConfiguration = ((DartCommandLineRunConfiguration)runConfig);
    if (!setupRunConfiguration(commandLineRunConfiguration, containingFile)) {
      return null;
    }

    mySourceElement = location.getPsiElement();
    settings.setName(containingFile.getName());
    return settings;
  }

  private static boolean setupRunConfiguration(DartCommandLineRunConfiguration configuration, PsiFile psiFile) {
    VirtualFile virtualFile = DartResolveUtil.getRealVirtualFile(psiFile);
    if (virtualFile == null) {
      return false;
    }
    configuration.setFilePath(FileUtil.toSystemIndependentName(virtualFile.getPath()));
    return true;
  }

  @Nullable
  @Override
  protected RunnerAndConfigurationSettings findExistingByElement(Location location,
                                                                 @NotNull RunnerAndConfigurationSettings[] existingConfigurations,
                                                                 ConfigurationContext context) {
    final PsiElement element = location.getPsiElement();
    final PsiFile containingFile = element.getContainingFile();
    final String name = containingFile.getName();
    return ContainerUtil.find(existingConfigurations, new Condition<RunnerAndConfigurationSettings>() {
      @Override
      public boolean value(RunnerAndConfigurationSettings settings) {
        return name.equals(settings.getName());
      }
    });
  }

  @Override
  public int compareTo(Object o) {
    return PREFERED;
  }
}
