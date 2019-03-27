// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.google.jstestdriver.idea.coverage;

import com.google.jstestdriver.idea.execution.JstdRunConfiguration;
import com.intellij.codeEditor.printing.ExportToHTMLSettings;
import com.intellij.coverage.*;
import com.intellij.coverage.view.CoverageViewExtension;
import com.intellij.coverage.view.CoverageViewManager;
import com.intellij.coverage.view.DirectoryCoverageViewExtension;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration;
import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.javascript.testFramework.coverage.CoverageSerializationUtils;
import com.intellij.javascript.testFramework.coverage.LcovCoverageReport;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.rt.coverage.data.ClassData;
import com.intellij.rt.coverage.data.LineData;
import com.intellij.rt.coverage.data.ProjectData;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Sergey Simonchik
 */
public class JstdCoverageEngine extends CoverageEngine {

  public static final String ID = "JstdCoverage";
  private static final Logger LOG = Logger.getInstance(JstdCoverageEngine.class);

  @Override
  public boolean isApplicableTo(@Nullable RunConfigurationBase configuration) {
    return configuration instanceof JstdRunConfiguration;
  }

  @Override
  public boolean canHavePerTestCoverage(@Nullable RunConfigurationBase configuration) {
    return false;
  }

  @NotNull
  @Override
  public CoverageEnabledConfiguration createCoverageEnabledConfiguration(@Nullable RunConfigurationBase configuration) {
    return new JstdCoverageEnabledConfiguration((JstdRunConfiguration) configuration);
  }

  @Override
  public CoverageSuite createCoverageSuite(@NotNull CoverageRunner covRunner,
                                           @NotNull String name,
                                           @NotNull CoverageFileProvider coverageDataFileProvider,
                                           @Nullable String[] filters,
                                           long lastCoverageTimeStamp,
                                           @Nullable String suiteToMerge,
                                           boolean coverageByTestEnabled,
                                           boolean tracingEnabled,
                                           boolean trackTestFolders,
                                           Project project) {
    return new JstdCoverageSuite(covRunner, name, coverageDataFileProvider, lastCoverageTimeStamp,
                                 coverageByTestEnabled, tracingEnabled,
                                 trackTestFolders, project, this);
  }

  @Override
  public CoverageSuite createCoverageSuite(@NotNull CoverageRunner covRunner,
                                           @NotNull String name,
                                           @NotNull CoverageFileProvider coverageDataFileProvider,
                                           @NotNull CoverageEnabledConfiguration config) {
    if (config instanceof JstdCoverageEnabledConfiguration) {
      Project project = config.getConfiguration().getProject();
      return createCoverageSuite(covRunner, name, coverageDataFileProvider, null,
                                 new Date().getTime(), null, false, false, true, project);
    }
    return null;
  }

  @Override
  public CoverageSuite createEmptyCoverageSuite(@NotNull CoverageRunner coverageRunner) {
    return new JstdCoverageSuite(this);
  }

  @NotNull
  @Override
  public CoverageAnnotator getCoverageAnnotator(@NotNull Project project) {
    return JstdCoverageAnnotator.getInstance(project);
  }

  @Override
  public boolean coverageEditorHighlightingApplicableTo(@NotNull PsiFile psiFile) {
    return psiFile instanceof JSFile;
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
    final String filePath = file.getPath();
    if (filePath == null) {
      return null;
    }
    return SimpleCoverageAnnotator.getFilePath(filePath);
  }

  @NotNull
  @Override
  public Set<String> getQualifiedNames(@NotNull PsiFile sourceFile) {
    final String qName = getQName(sourceFile);
    return qName != null ? Collections.singleton(qName) : Collections.emptySet();
  }

  @Override
  public boolean includeUntouchedFileInCoverage(@NotNull String qualifiedName,
                                                @NotNull File outputFile,
                                                @NotNull PsiFile sourceFile,
                                                @NotNull CoverageSuitesBundle suite) {
    return false;
  }

  @Override
  public boolean isReportGenerationAvailable(@NotNull Project project,
                                             @NotNull DataContext dataContext,
                                             @NotNull CoverageSuitesBundle currentSuite) {
    return true;
  }

  @Override
  public void generateReport(@NotNull Project project,
                             @NotNull DataContext dataContext,
                             @NotNull CoverageSuitesBundle currentSuiteBundle) {
    LcovCoverageReport coverageReport = new LcovCoverageReport();
    for (CoverageSuite suite : currentSuiteBundle.getSuites()) {
      ProjectData projectData = suite.getCoverageData(CoverageDataManager.getInstance(project));
      if (projectData != null) {
        Map<String, ClassData> classDataMap = projectData.getClasses();
        for (Map.Entry<String, ClassData> classDataEntry : classDataMap.entrySet()) {
          String fileName = classDataEntry.getKey();
          ClassData classData = classDataEntry.getValue();
          List<LcovCoverageReport.LineHits> lineHitsList = convertClassDataToLineHits(classData);
          coverageReport.mergeFileReport(null, fileName, lineHitsList);
        }
      }
    }
    final ExportToHTMLSettings settings = ExportToHTMLSettings.getInstance(project);
    final File outputDir = new File(settings.OUTPUT_DIRECTORY);
    FileUtil.createDirectory(outputDir);
    String outputFileName = getOutputFileName(currentSuiteBundle);
    String title = "Coverage Report Generation";
    try {
      File output = new File(outputDir, outputFileName);
      CoverageSerializationUtils.writeLCOV(coverageReport, output);
      refresh(output);
      String url = "http://ltp.sourceforge.net/coverage/lcov.php";
      Messages.showInfoMessage("<html>Coverage report has been successfully saved as '" + outputFileName +
                               "' file.<br>Use <a href='" + url + "'>" + url + "</a>" +
                               " to generate HTML output." +
                               "</html>", title);
    }
    catch (IOException e) {
      LOG.warn("Can not export coverage data", e);
      Messages.showErrorDialog("Can not generate coverage report: " + e.getMessage(), title);
    }
  }

  private static void refresh(@NotNull File file) {
    final VirtualFile vFile = VfsUtil.findFileByIoFile(file, true);
    if (vFile != null) {
      ApplicationManager.getApplication().runWriteAction(() -> vFile.refresh(false, false));
    }
  }

  private static String getOutputFileName(@NotNull CoverageSuitesBundle currentSuitesBundle) {
    StringBuilder name = new StringBuilder();
    for (CoverageSuite suite : currentSuitesBundle.getSuites()) {
      String presentableName = suite.getPresentableName();
      name.append(presentableName);
    }
    name.append(".lcov");
    return name.toString();
  }

  private static List<LcovCoverageReport.LineHits> convertClassDataToLineHits(@NotNull ClassData classData) {
    int lineCount = classData.getLines().length;
    List<LcovCoverageReport.LineHits> lineHitsList = ContainerUtil.newArrayListWithCapacity(lineCount);
    for (int lineInd = 0; lineInd < lineCount; lineInd++) {
      LineData lineData = classData.getLineData(lineInd);
      if (lineData != null) {
        LcovCoverageReport.LineHits lineHits = new LcovCoverageReport.LineHits(
          lineData.getLineNumber(),
          lineData.getHits()
        );
        lineHitsList.add(lineHits);
      }
    }
    return lineHitsList;
  }

  @Override
  public List<Integer> collectSrcLinesForUntouchedFile(@NotNull File classFile, @NotNull CoverageSuitesBundle suite) {
    return null;
  }

  @Override
  public List<PsiElement> findTestsByNames(@NotNull String[] testNames, @NotNull Project project) {
    return Collections.emptyList();
  }

  @Override
  public String getTestMethodName(@NotNull PsiElement element, @NotNull AbstractTestProxy testProxy) {
    return null;
  }

  @Override
  public String getPresentableText() {
    return ID;
  }

  @Override
  public boolean coverageProjectViewStatisticsApplicableTo(final VirtualFile fileOrDir) {
    return !(fileOrDir.isDirectory()) && fileOrDir.getFileType() instanceof JavaScriptFileType;
  }

  @Override
  public CoverageViewExtension createCoverageViewExtension(Project project,
                                                           CoverageSuitesBundle suiteBundle,
                                                           CoverageViewManager.StateBean stateBean) {
    return new DirectoryCoverageViewExtension(project, getCoverageAnnotator(project), suiteBundle, stateBean);
  }
}
