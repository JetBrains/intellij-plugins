package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
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
    final VirtualFile dartFile = getRunnableDartFileFromContext(context, true);
    if (dartFile != null) {
      configuration.getRunnerParameters().setFilePath(dartFile.getPath());
      configuration.getRunnerParameters()
        .setWorkingDirectory(DartCommandLineRunnerParameters.suggestDartWorkingDir(context.getProject(), dartFile));

      configuration.setGeneratedName();

      sourceElement.set(sourceElement.isNull() ? null : sourceElement.get().getContainingFile());
      return true;
    }
    return false;
  }

  @Override
  public boolean isConfigurationFromContext(final @NotNull DartCommandLineRunConfiguration configuration,
                                            final @NotNull ConfigurationContext context) {
    final VirtualFile dartFile = getDartFileFromContext(context);
    return dartFile != null && dartFile.getPath().equals(configuration.getRunnerParameters().getFilePath());
  }

  @Nullable
  public static VirtualFile getRunnableDartFileFromContext(@NotNull final ConfigurationContext context,
                                                           final boolean checkBrowserSpecificImports) {
    final PsiElement psiLocation = context.getPsiLocation();
    final PsiFile psiFile = psiLocation == null ? null : psiLocation.getContainingFile();
    final VirtualFile virtualFile = DartResolveUtil.getRealVirtualFile(psiFile);

    if (psiFile instanceof DartFile &&
        virtualFile != null &&
        ProjectRootManager.getInstance(context.getProject()).getFileIndex().isInContent(virtualFile) &&
        !DartWritingAccessProvider.isInDartSdkOrDartPackagesFolder(psiFile.getProject(), virtualFile) &&
        DartResolveUtil.getMainFunction(psiFile) != null &&
        (!checkBrowserSpecificImports || !hasImport((DartFile)psiFile,
                                                    "dart:html", "dart:html_common", "dart:indexed_db", "dart:js",
                                                    "dart:svg", "dart:web_audio", "dart:web_gl", "dart:web_sql"))) {
      return virtualFile;
    }

    return null;
  }

  @Nullable
  private static VirtualFile getDartFileFromContext(final @NotNull ConfigurationContext context) {
    final PsiElement psiLocation = context.getPsiLocation();
    final PsiFile psiFile = psiLocation == null ? null : psiLocation.getContainingFile();
    final VirtualFile virtualFile = DartResolveUtil.getRealVirtualFile(psiFile);
    return psiFile instanceof DartFile && virtualFile != null ? virtualFile : null;
  }

  private static boolean hasImport(final @NotNull DartFile psiFile, final @NotNull String... importTexts) {
    final DartImportStatement[] importStatements = PsiTreeUtil.getChildrenOfType(psiFile, DartImportStatement.class);
    if (importStatements == null) return false;

    for (DartImportStatement importStatement : importStatements) {
      if (ArrayUtil.contains(importStatement.getUriString(), importTexts)) return true;
    }

    return false;
  }
}
