package com.jetbrains.lang.dart.ide.runner.unittest;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.jetbrains.lang.dart.psi.DartArgumentList;
import com.jetbrains.lang.dart.psi.DartCallExpression;
import com.jetbrains.lang.dart.psi.DartExpression;
import com.jetbrains.lang.dart.psi.DartFile;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DartUnitRunConfigurationProducer extends RunConfigurationProducer<DartUnitRunConfiguration> {
  public DartUnitRunConfigurationProducer() {
    super(DartUnitRunConfigurationType.getInstance());
  }

  @Override
  protected boolean setupConfigurationFromContext(DartUnitRunConfiguration configuration,
                                                  ConfigurationContext context,
                                                  Ref<PsiElement> sourceElement) {
    final PsiElement contextElement = context.getPsiLocation();
    final VirtualFile file = contextElement == null ? null : PsiUtilCore.getVirtualFile(contextElement);
    final VirtualFile packagesFolder = file == null ? null : PubspecYamlUtil.getDartPackagesFolder(context.getProject(), file);
    if (packagesFolder == null || packagesFolder.findChild("unittest") == null) return false;

    final PsiElement element = findTestElement(contextElement);
    if (element == null || !setupRunConfiguration(configuration, element)) {
      return false;
    }

    configuration.setName("Test: " + configuration.getRunnerParameters().getTestName());
    return true;
  }

  @Override
  public boolean isConfigurationFromContext(DartUnitRunConfiguration configuration, ConfigurationContext context) {
    final PsiElement testElement = findTestElement(context.getPsiLocation());
    final DartUnitRunnerParameters parameters = configuration.getRunnerParameters();
    if (parameters.getScope() == DartUnitRunnerParameters.Scope.ALL && testElement != null) {
      final VirtualFile virtualFile = testElement.getContainingFile().getVirtualFile();
      return virtualFile != null && parameters.getFilePath().equals(virtualFile.getPath());
    }
    else if (parameters.getScope() != DartUnitRunnerParameters.Scope.ALL && testElement instanceof DartCallExpression) {
      return StringUtil.equalsIgnoreCase(parameters.getTestName(), findTestName((DartCallExpression)testElement));
    }
    return false;
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
  private static PsiElement findTestElement(@Nullable PsiElement element) {
    DartCallExpression callExpression = PsiTreeUtil.getParentOfType(element, DartCallExpression.class);
    while (callExpression != null) {
      if (isGroup(callExpression) || isTest(callExpression)) {
        return callExpression;
      }
      callExpression = PsiTreeUtil.getParentOfType(callExpression, DartCallExpression.class, true);
    }
    return element != null ? element.getContainingFile() : null;
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
}
