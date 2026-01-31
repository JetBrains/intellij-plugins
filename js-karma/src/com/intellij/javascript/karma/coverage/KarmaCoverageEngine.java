// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.coverage;

import com.intellij.coverage.CoverageAnnotator;
import com.intellij.coverage.CoverageDataManager;
import com.intellij.coverage.CoverageEngine;
import com.intellij.coverage.CoverageFileProvider;
import com.intellij.coverage.CoverageRunner;
import com.intellij.coverage.CoverageSuite;
import com.intellij.coverage.CoverageSuitesBundle;
import com.intellij.coverage.view.CoverageListRootNode;
import com.intellij.coverage.view.CoverageViewExtension;
import com.intellij.coverage.view.DirectoryCoverageViewExtension;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.WrappingRunConfiguration;
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.javascript.karma.execution.KarmaRunConfiguration;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.html.HtmlFileImpl;
import com.intellij.rt.coverage.data.ProjectData;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class KarmaCoverageEngine extends CoverageEngine {
  public static final String ID = "KarmaJavaScriptTestRunnerCoverage";

  @Override
  public boolean isApplicableTo(@NotNull RunConfigurationBase configuration) {
    return WrappingRunConfiguration.unwrapRunProfile(configuration) instanceof KarmaRunConfiguration;
  }

  @Override
  public @NotNull CoverageEnabledConfiguration createCoverageEnabledConfiguration(@NotNull RunConfigurationBase configuration) {
    return new KarmaCoverageEnabledConfiguration(configuration);
  }

  @Override
  public @NotNull CoverageSuite createCoverageSuite(@NotNull String name,
                                                    @NotNull Project project,
                                                    @NotNull CoverageRunner runner,
                                                    @NotNull CoverageFileProvider fileProvider,
                                                    long timestamp) {
    return new KarmaCoverageSuite(name, project, runner, fileProvider, timestamp, this);
  }

  @Override
  public CoverageSuite createCoverageSuite(@NotNull String name,
                                           @NotNull Project project,
                                           @NotNull CoverageRunner runner,
                                           @NotNull CoverageFileProvider fileProvider,
                                           long timestamp,
                                           @NotNull CoverageEnabledConfiguration config) {
    if (config instanceof KarmaCoverageEnabledConfiguration) {
      return new KarmaCoverageSuite(name, project, runner, fileProvider, timestamp, this);
    }
    return null;
  }

  @Override
  public CoverageSuite createEmptyCoverageSuite(@NotNull CoverageRunner coverageRunner) {
    return new KarmaCoverageSuite(this);
  }

  @Override
  public @NotNull CoverageAnnotator getCoverageAnnotator(@NotNull Project project) {
    return KarmaCoverageAnnotator.getInstance(project);
  }

  @Override
  public boolean coverageEditorHighlightingApplicableTo(@NotNull PsiFile psiFile) {
    return psiFile instanceof JSFile || psiFile instanceof HtmlFileImpl;
  }

  @Override
  protected String getQualifiedName(@NotNull File outputFile, @NotNull PsiFile sourceFile) {
    return getQName(sourceFile);
  }

  private static @Nullable String getQName(@NotNull PsiFile sourceFile) {
    final VirtualFile file = sourceFile.getVirtualFile();
    if (file == null) {
      return null;
    }
    return file.getPath();
  }

  @Override
  public @NotNull Set<String> getQualifiedNames(@NotNull PsiFile sourceFile) {
    final String qName = getQName(sourceFile);
    return qName != null ? Collections.singleton(qName) : Collections.emptySet();
  }

  @Override
  public List<Integer> collectSrcLinesForUntouchedFile(@NotNull File classFile, @NotNull CoverageSuitesBundle suite) {
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
  public CoverageViewExtension createCoverageViewExtension(final Project project, final CoverageSuitesBundle suiteBundle) {
    return new DirectoryCoverageViewExtension(project, getCoverageAnnotator(project), suiteBundle) {
      @Override
      public @NotNull AbstractTreeNode<?> createRootNode() {
        VirtualFile rootDir = findRootDir(project, suiteBundle);
        if (rootDir == null) {
          rootDir = ProjectUtil.guessProjectDir(myProject);
        }
        assert rootDir != null;
        PsiDirectory psiRootDir = PsiManager.getInstance(myProject).findDirectory(rootDir);
        return new CoverageListRootNode(myProject, Objects.requireNonNull(psiRootDir), mySuitesBundle);
      }
    };
  }

  /**
   * Finds a root directory for Coverage toolwindow view.
   * Returns a content root containing at least one covered file.
   */
  private static @Nullable VirtualFile findRootDir(final @NotNull Project project, final @NotNull CoverageSuitesBundle suitesBundle) {
    return ReadAction.compute(() -> {
      CoverageDataManager coverageDataManager = CoverageDataManager.getInstance(project);
      for (CoverageSuite suite : suitesBundle.getSuites()) {
        ProjectData data = suite.getCoverageData(coverageDataManager);
        if (data != null) {
          for (String path : data.getClasses().keySet()) {
            VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path);
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
