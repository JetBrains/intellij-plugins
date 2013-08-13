package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.psi.DartFile;
import org.jetbrains.annotations.Nullable;

public class DartCommandLineRuntimeConfigurationProducer extends RunConfigurationProducer<DartCommandLineRunConfiguration> {
  public DartCommandLineRuntimeConfigurationProducer() {
    super(DartCommandLineRunConfigurationType.getInstance());
  }

  @Override
  protected boolean setupConfigurationFromContext(DartCommandLineRunConfiguration configuration,
                                                  ConfigurationContext context,
                                                  Ref<PsiElement> sourceElement) {
    final String path = findDartPath(context);
    if (path != null) {
      configuration.setName(VfsUtil.extractFileName(path));
      configuration.setFilePath(path);
      return true;
    }
    return false;
  }

  @Override
  public boolean isConfigurationFromContext(DartCommandLineRunConfiguration configuration, ConfigurationContext context) {
    final String contextPath = findDartPath(context);
    final String configurationPath = configuration.getFilePath();
    return configurationPath != null && configurationPath.equals(contextPath);
  }

  @Nullable
  private static String findDartPath(ConfigurationContext context) {
    final PsiElement psiLocation = context.getPsiLocation();
    final PsiFile containingFile = psiLocation != null ? psiLocation.getContainingFile() : null;
    if (!(containingFile instanceof DartFile)) return null;
    final VirtualFile virtualFile = containingFile.getVirtualFile();
    return virtualFile != null ? virtualFile.getPath() : null;
  }
}
