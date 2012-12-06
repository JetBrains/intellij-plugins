package com.jetbrains.lang.dart.ide.runner.unittest;

import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.configurations.LocatableConfiguration;
import com.intellij.execution.junit.RuntimeConfigurationProducer;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.psi.DartArgumentList;
import com.jetbrains.lang.dart.psi.DartCallExpression;
import com.jetbrains.lang.dart.psi.DartExpression;
import com.jetbrains.lang.dart.psi.DartFile;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author: Fedor.Korotkov
 */
public class DartUnitRuntimeConfigurationProducer extends RuntimeConfigurationProducer {
  private PsiElement mySourceElement;

  public DartUnitRuntimeConfigurationProducer() {
    super(DartUnitRunConfigurationType.getInstance());
  }

  @Override
  public PsiElement getSourceElement() {
    return mySourceElement;
  }

  @Override
  protected RunnerAndConfigurationSettings createConfigurationByElement(Location location, ConfigurationContext context) {
    if (!(location instanceof PsiLocation)) return null;

    final RunnerAndConfigurationSettings settings = cloneTemplateConfiguration(location.getProject(), context);
    final LocatableConfiguration runConfig = (LocatableConfiguration)settings.getConfiguration();
    if (!(runConfig instanceof DartUnitRunConfiguration)) {
      return null;
    }

    final DartUnitRunConfiguration dartUnitRunConfiguration = ((DartUnitRunConfiguration)runConfig);
    final PsiElement element = findTestElement(location.getPsiElement());
    if (element == null || !setupRunConfiguration(dartUnitRunConfiguration, element)) {
      return null;
    }

    mySourceElement = location.getPsiElement();
    settings.setName("Test: " + dartUnitRunConfiguration.getRunnerParameters().getTestName());
    return settings;
  }

  private static boolean setupRunConfiguration(DartUnitRunConfiguration configuration, @NotNull PsiElement expression) {
    if (expression instanceof DartCallExpression) {
      return setupRunConfiguration(configuration, (DartCallExpression)expression);
    }
    else if (expression instanceof DartFile) {
      return setupRunConfiguration(configuration, (DartFile)expression);
    }
    return false;
  }

  private static boolean setupRunConfiguration(DartUnitRunConfiguration configuration, @NotNull DartFile dartFile) {
    final VirtualFile virtualFile = DartResolveUtil.getRealVirtualFile(dartFile);
    if (virtualFile == null || !DartResolveUtil.isLibraryRoot(dartFile)) {
      return false;
    }
    configuration.getRunnerParameters().setTestName(DartResolveUtil.getLibraryName(dartFile));
    configuration.getRunnerParameters().setScope(DartUnitRunnerParameters.Scope.ALL);
    configuration.getRunnerParameters().setFilePath(FileUtil.toSystemIndependentName(virtualFile.getPath()));
    return true;
  }

  @Nullable
  private static String findTestName(@Nullable DartCallExpression expression) {
    String testName;
    final DartArgumentList dartArgumentList = expression == null ? null : expression.getArguments().getArgumentList();
    if (dartArgumentList == null || dartArgumentList.getExpressionList().isEmpty()) {
      return null;
    }
    final DartExpression dartExpression = dartArgumentList.getExpressionList().get(0);
    testName = dartExpression == null ? "" : StringUtil.unquoteString(dartExpression.getText());
    return testName;
  }

  private static boolean setupRunConfiguration(DartUnitRunConfiguration configuration, @NotNull DartCallExpression expression) {
    final String testName = findTestName(expression);
    final List<VirtualFile> virtualFiles = DartResolveUtil.findLibrary(expression.getContainingFile());
    if (testName == null || virtualFiles.isEmpty()) {
      return false;
    }
    configuration.getRunnerParameters().setTestName(testName);
    configuration.getRunnerParameters().setScope(isTest(expression)
                                                 ? DartUnitRunnerParameters.Scope.METHOD
                                                 : DartUnitRunnerParameters.Scope.GROUP);
    final String path = virtualFiles.iterator().next().getPath();
    configuration.getRunnerParameters().setFilePath(FileUtil.toSystemIndependentName(path));
    return true;
  }

  @Nullable
  private static PsiElement findTestElement(PsiElement element) {
    DartCallExpression callExpression = PsiTreeUtil.getParentOfType(element, DartCallExpression.class);
    while (callExpression != null) {
      if (isGroup(callExpression) || isTest(callExpression)) {
        return callExpression;
      }
      callExpression = PsiTreeUtil.getParentOfType(callExpression, DartCallExpression.class, true);
    }
    return element.getContainingFile();
  }

  private static boolean isTest(DartCallExpression expression) {
    return checkLibAndName(expression, "test");
  }

  private static boolean isGroup(DartCallExpression expression) {
    return checkLibAndName(expression, "group");
  }

  private static boolean checkLibAndName(DartCallExpression callExpression, String expectedName) {
    final String name = callExpression.getExpression().getText();
    return expectedName.equalsIgnoreCase(name);
  }

  @Nullable
  @Override
  protected RunnerAndConfigurationSettings findExistingByElement(Location location,
                                                                 @NotNull RunnerAndConfigurationSettings[] existingConfigurations,
                                                                 ConfigurationContext context) {
    final String testName = "Test: " + findTestName(PsiTreeUtil.getParentOfType(location.getPsiElement(), DartCallExpression.class));
    return ContainerUtil.find(existingConfigurations, new Condition<RunnerAndConfigurationSettings>() {
      @Override
      public boolean value(RunnerAndConfigurationSettings settings) {
        return testName.equals(settings.getName());
      }
    });
  }

  @Override
  public int compareTo(Object o) {
    return PREFERED;
  }
}
