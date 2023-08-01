// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.coverage;

import com.intellij.coverage.*;
import com.intellij.coverage.view.CoverageListRootNode;
import com.intellij.coverage.view.CoverageViewExtension;
import com.intellij.coverage.view.CoverageViewManager;
import com.intellij.coverage.view.DirectoryCoverageViewExtension;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.WrappingRunConfiguration;
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration;
import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.javascript.karma.execution.KarmaRunConfiguration;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.html.HtmlFileImpl;
import com.intellij.rt.coverage.data.ProjectData;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class KarmaCoverageEngine extends CoverageEngine {

  public static final String ID = "KarmaJavaScriptTestRunnerCoverage";

  @Override
  public boolean isApplicableTo(@NotNull RunConfigurationBase configuration) {
    return WrappingRunConfiguration.unwrapRunProfile(configuration) instanceof KarmaRunConfiguration;
  }

  @Override
  public boolean canHavePerTestCoverage(@NotNull RunConfigurationBase configuration) {
    return false;
  }

  @NotNull
  @Override
  public CoverageEnabledConfiguration createCoverageEnabledConfiguration(@NotNull RunConfigurationBase configuration) {
    return new KarmaCoverageEnabledConfiguration(configuration);
  }

  @Override
  public CoverageSuite createCoverageSuite(@NotNull CoverageRunner covRunner,
                                           @NotNull String name,
                                           @NotNull CoverageFileProvider coverageDataFileProvider,
                                           String @Nullable [] filters,
                                           long lastCoverageTimeStamp,
                                           @Nullable String suiteToMerge,
                                           boolean coverageByTestEnabled,
                                           boolean branchCoverage,
                                           boolean trackTestFolders,
                                           Project project) {
    return new KarmaCoverageSuite(covRunner, name, coverageDataFileProvider, lastCoverageTimeStamp,
                                  coverageByTestEnabled, branchCoverage,
                                  trackTestFolders, project, this);
  }

  @Override
  public CoverageSuite createCoverageSuite(@NotNull CoverageRunner covRunner,
                                           @NotNull String name,
                                           @NotNull CoverageFileProvider coverageDataFileProvider,
                                           @NotNull CoverageEnabledConfiguration config) {
    if (config instanceof KarmaCoverageEnabledConfiguration) {
      Project project = config.getConfiguration().getProject();
      return createCoverageSuite(covRunner, name, coverageDataFileProvider, null,
                                 new Date().getTime(), null, false, false, true, project);
    }
    return null;
  }

  @Override
  public CoverageSuite createEmptyCoverageSuite(@NotNull CoverageRunner coverageRunner) {
    return new KarmaCoverageSuite(this);
  }

  @NotNull
  @Override
  public CoverageAnnotator getCoverageAnnotator(@NotNull Project project) {
    return KarmaCoverageAnnotator.getInstance(project);
  }

  @Override
  public boolean coverageEditorHighlightingApplicableTo(@NotNull PsiFile psiFile) {
    return psiFile instanceof JSFile || psiFile instanceof HtmlFileImpl;
  }

  @Override
  public boolean acceptedByFilters(@NotNull PsiFile psiFile, @NotNull CoverageSuitesBundle suite) {
    return true;
  }

  @Override
  public boolean recompileProjectAndRerunAction(@NotNull Module module,
                                                @NotNull CoverageSuitesBundle suite,
                                                @NotNull Runnable chooseSuiteAction) {
    return false;
  }

  @Override
  public String getQualifiedName(@NotNull File outputFile, @NotNull PsiFile sourceFile) {
    return getQName(sourceFile);
  }

  @Nullable
  private static String getQName(@NotNull PsiFile sourceFile) {
    final VirtualFile file = sourceFile.getVirtualFile();
    if (file == null) {
      return null;
    }
    return file.getPath();
  }

  @NotNull
  @Override
  public Set<String> getQualifiedNames(@NotNull PsiFile sourceFile) {
    final String qName = getQName(sourceFile);
    return qName != null ? Collections.singleton(qName) : Collections.emptySet();
  }

  @Override
  public boolean includeUntouchedFileInCoverage(final @NotNull String qualifiedName,
                                                final @NotNull File outputFile,
                                                final @NotNull PsiFile sourceFile,
                                                final @NotNull CoverageSuitesBundle suite) {
    return super.includeUntouchedFileInCoverage(qualifiedName, outputFile, sourceFile, suite);
  }

  @Override
  public boolean isReportGenerationAvailable(@NotNull Project project,
                                             @NotNull DataContext dataContext,
                                             @NotNull CoverageSuitesBundle currentSuite) {
    return super.isReportGenerationAvailable(project, dataContext, currentSuite);
  }

  @Override
  public void generateReport(final @NotNull Project project,
                             final @NotNull DataContext dataContext,
                             final @NotNull CoverageSuitesBundle currentSuite) {
    super.generateReport(project, dataContext, currentSuite);
  }

  @Override
  public List<Integer> collectSrcLinesForUntouchedFile(@NotNull File classFile, @NotNull CoverageSuitesBundle suite) {
    return null;
  }

  @Override
  public List<PsiElement> findTestsByNames(String @NotNull [] testNames, @NotNull Project project) {
    return Collections.emptyList();
  }

  @Override
  public String getTestMethodName(@NotNull PsiElement element, @NotNull AbstractTestProxy testProxy) {
    return null;
  }

  @Override
  public @Nls String getPresentableText() {
    return ID;
  }

  @Override
  public boolean coverageProjectViewStatisticsApplicableTo(final VirtualFile fileOrDir) {
    return !fileOrDir.isDirectory();
  }

  @Override
  public CoverageViewExtension createCoverageViewExtension(final Project project,
                                                           final CoverageSuitesBundle suiteBundle,
                                                           CoverageViewManager.StateBean stateBean) {
    return new DirectoryCoverageViewExtension(project, getCoverageAnnotator(project), suiteBundle, stateBean) {
      @NotNull
      @Override
      public AbstractTreeNode createRootNode() {
        VirtualFile rootDir = findRootDir(project, suiteBundle);
        if (rootDir == null) {
          rootDir = ProjectUtil.guessProjectDir(myProject);
        }
        PsiDirectory psiRootDir = PsiManager.getInstance(myProject).findDirectory(rootDir);
        return new CoverageListRootNode(myProject, psiRootDir, mySuitesBundle, myStateBean);
      }
    };
  }

  /**
   * Finds a root directory for Coverage toolwindow view.
   * Returns a content root containing at least one covered file.
   */
  @Nullable
  private static VirtualFile findRootDir(@NotNull final Project project, @NotNull final CoverageSuitesBundle suitesBundle) {
    return ReadAction.compute(() -> {
      CoverageDataManager coverageDataManager = CoverageDataManager.getInstance(project);
      for (CoverageSuite suite : suitesBundle.getSuites()) {
        ProjectData data = suite.getCoverageData(coverageDataManager);
        if (data != null) {
          for (String path : data.getClasses().keySet()) {
            VirtualFile file = VfsUtil.findFileByIoFile(new File(path), false);
            if (file != null && file.isValid()) {
              ProjectFileIndex projectFileIndex = ProjectFileIndex.getInstance(project);
              VirtualFile contentRoot = projectFileIndex.getContentRootForFile(file);
              if (contentRoot != null && contentRoot.isDirectory() && contentRoot.isValid()) {
                return contentRoot;
              }
            }
          }
        }
      }
      return null;
    });
  }
}
