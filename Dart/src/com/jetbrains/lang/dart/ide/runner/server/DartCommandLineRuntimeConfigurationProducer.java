package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.ide.DartWritingAccessProvider;
import com.jetbrains.lang.dart.psi.DartFile;
import com.jetbrains.lang.dart.psi.DartImportStatement;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartCommandLineRuntimeConfigurationProducer extends RunConfigurationProducer<DartCommandLineRunConfiguration> {
  public DartCommandLineRuntimeConfigurationProducer() {
    super(DartCommandLineRunConfigurationType.getInstance());
  }

  @Override
  protected boolean setupConfigurationFromContext(final @NotNull DartCommandLineRunConfiguration configuration,
                                                  final @NotNull ConfigurationContext context,
                                                  final @NotNull Ref<PsiElement> sourceElement) {
    final VirtualFile dartFile = findRunnableDartFile(context);
    if (dartFile != null) {
      configuration.setFilePath(dartFile.getPath());
      configuration.setName(configuration.suggestedName());
      return true;
    }
    return false;
  }

  @Override
  public boolean isConfigurationFromContext(final @NotNull DartCommandLineRunConfiguration configuration,
                                            final @NotNull ConfigurationContext context) {
    final VirtualFile dartFile = findRunnableDartFile(context);
    return dartFile != null && dartFile.getPath().equals(configuration.getFilePath());
  }

  @Nullable
  public static VirtualFile findRunnableDartFile(final @NotNull ConfigurationContext context) {
    final PsiElement psiLocation = context.getPsiLocation();
    final PsiFile psiFile = psiLocation == null ? null : psiLocation.getContainingFile();
    final VirtualFile virtualFile = DartResolveUtil.getRealVirtualFile(psiFile);

    if (psiFile instanceof DartFile &&
        virtualFile != null &&
        ProjectRootManager.getInstance(context.getProject()).getFileIndex().isInContent(virtualFile) &&
        !DartWritingAccessProvider.isInDartSdkOrDartPackagesFolder(psiFile.getProject(), virtualFile) &&
        DartResolveUtil.getMainFunction(psiFile) != null &&
        !hasImport((DartFile)psiFile, "dart:html")) {
      return virtualFile;
    }

    return null;
  }

  private static boolean hasImport(final @NotNull DartFile psiFile, final @NotNull String importText) {
    final DartImportStatement[] importStatements = PsiTreeUtil.getChildrenOfType(psiFile, DartImportStatement.class);
    if (importStatements == null) return false;

    for (DartImportStatement importStatement : importStatements) {
      if (importText.equals(importStatement.getUri())) return true;
    }

    return false;
  }
}
