package com.jetbrains.lang.dart.ide.runner.client;

import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.junit.RuntimeConfigurationProducer;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartRuntimeConfigurationProducer extends RuntimeConfigurationProducer {
  private PsiElement mySourceElement;

  public DartRuntimeConfigurationProducer() {
    super(DartDebugConfigurationType.getTypeInstance());
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
    if (!(containingFile instanceof XmlFile)) return null;

    final RunnerAndConfigurationSettings settings = createConfigurationByLocation(location, (XmlFile)containingFile);
    if (settings != null && !getConfigurationFactory().equals(settings.getFactory())) {
      return null;
    }
    if (settings != null) {
      mySourceElement = location.getPsiElement().getContainingFile();
    }
    return settings;
  }

  @Nullable
  private RunnerAndConfigurationSettings createConfigurationByLocation(Location location, XmlFile xmlFile) {
    VirtualFile virtualFile = DartResolveUtil.getRealVirtualFile(xmlFile);
    if (virtualFile == null || !DartResolveUtil.containsDartSources(xmlFile)) {
      return null;
    }

    ConfigurationFactory factory = getConfigurationFactory();

    RunManager runManager = RunManager.getInstance(location.toPsiLocation().getProject());
    RunnerAndConfigurationSettings settings = runManager.createRunConfiguration(virtualFile.getName(), factory);
    final DartDebugConfigurationBase configuration = (DartDebugConfigurationBase)settings.getConfiguration();
    configuration.setFileUrl(virtualFile.getUrl());
    return settings;
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
