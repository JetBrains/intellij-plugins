// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.analysis.problemsView.AnalysisProblem;
import com.intellij.analysis.problemsView.AnalysisProblemsTableModel;
import com.intellij.analysis.problemsView.AnalysisProblemsViewPanel;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.OpenSourceUtil;
import com.intellij.util.SmartList;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.assists.AssistUtils;
import com.jetbrains.lang.dart.assists.DartSourceEditException;
import com.jetbrains.lang.dart.ide.annotator.DartAnnotator;
import icons.DartIcons;
import org.dartlang.analysis.server.protocol.AnalysisError;
import org.dartlang.analysis.server.protocol.AnalysisErrorFixes;
import org.dartlang.analysis.server.protocol.SourceChange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DartProblemsViewPanel extends AnalysisProblemsViewPanel {
  private static final LayeredIcon DART_ERRORS_ICON;
  private static final LayeredIcon DART_WARNINGS_ICON;

  static {
    DART_ERRORS_ICON = new LayeredIcon(2);
    DART_ERRORS_ICON.setIcon(DartIcons.Dart_13, 0);
    DART_ERRORS_ICON.setIcon(AllIcons.Ide.ErrorPoint, 1, SwingConstants.SOUTH_WEST);

    DART_WARNINGS_ICON = new LayeredIcon(2);
    DART_WARNINGS_ICON.setIcon(DartIcons.Dart_13, 0);
    DART_WARNINGS_ICON.setIcon(DartIcons.Warning_point, 1, SwingConstants.SOUTH_WEST);
  }

  DartProblemsViewPanel(@NotNull final Project project,
                        @NotNull final DartProblemsPresentationHelper presentationHelper) {
    super(project, presentationHelper);
  }

  @Override
  protected @NotNull AnalysisProblemsTableModel createTableModel() {
    return new DartProblemsTableModel(myPresentationHelper);
  }

  @Override
  protected void addQuickFixActions(@NotNull final DefaultActionGroup group, @Nullable final AnalysisProblem problem) {
    final VirtualFile selectedVFile = problem != null ? problem.getFile() : null;
    if (selectedVFile == null) return;

    final List<SourceChange> selectedProblemSourceChangeFixes = new SmartList<>();
    DartAnalysisServerService.getInstance(myProject)
      .askForFixesAndWaitABitIfReceivedQuickly(selectedVFile, problem.getOffset(), fixes -> {
        for (AnalysisErrorFixes fix : fixes) {
          if (fix.getError().getCode().equals(problem.getCode())) {
            selectedProblemSourceChangeFixes.addAll(fix.getFixes());
          }
        }
      });

    if (selectedProblemSourceChangeFixes.isEmpty()) return;

    group.addSeparator();

    for (final SourceChange sourceChangeFix : selectedProblemSourceChangeFixes) {
      if (sourceChangeFix == null) continue;
      group.add(new AnAction(sourceChangeFix.getMessage(), null, AllIcons.Actions.QuickfixBulb) {
        @Override
        public void actionPerformed(@NotNull final AnActionEvent event) {
          OpenSourceUtil.navigate(PsiNavigationSupport.getInstance().createNavigatable(myProject, selectedVFile, problem.getOffset()));
          try {
            WriteAction.run(() -> AssistUtils.applySourceChange(myProject, sourceChangeFix, true));
          }
          catch (DartSourceEditException ignored) {/**/}
        }
      });
    }
  }

  @Override
  protected int getConvertedOffset(@NotNull VirtualFile vFile, @NotNull AnalysisProblem diagnosticMessage) {
    return DartAnalysisServerService.getInstance(myProject).getConvertedOffset(vFile, diagnosticMessage.getOffset());
  }

  @Override
  protected void updateStatusDescription() {
    final AnalysisProblemsTableModel model = getModel();
    DartProblemsView problemsView = DartProblemsView.getInstance(myProject);
    problemsView.setHeaderText(model.getStatusText());
    problemsView.setToolWindowIcon(getStatusIcon());
  }

  private static void addReanalyzeActions(@NotNull final DefaultActionGroup group) {
    final AnAction restartAction = ActionManager.getInstance().getAction("Dart.Restart.Analysis.Server");
    if (restartAction != null) {
      group.add(restartAction);
    }
  }

  @Override
  protected void addActionsTo(@NotNull DefaultActionGroup group) {
    addReanalyzeActions(group);
    group.addAction(new DartProblemsViewPanel.AnalysisServerSettingsAction());
    group.addSeparator();

    addAutoScrollToSourceAction(group);
    addGroupBySeverityAction(group);
    group.addAction(new DartProblemsViewPanel.FilterProblemsAction());
    group.addSeparator();
  }

  @Override
  protected @NotNull Icon getStatusIcon() {
    final AnalysisProblemsTableModel model = getModel();
    return model.hasErrors() ? DART_ERRORS_ICON : model.hasWarnings() ? DART_WARNINGS_ICON : DartIcons.Dart_13;
  }

  DartProblemsPresentationHelper getPresentationHelper() {
    return (DartProblemsPresentationHelper)myPresentationHelper;
  }

  private void showFiltersPopup() {
    final DartProblemsFilterForm filterForm = new DartProblemsFilterForm();
    filterForm.reset(getPresentationHelper());
    filterForm.addListener(new DartProblemsFilterForm.FilterListener() {
      @Override
      public void filtersChanged() {
        getPresentationHelper().updateFromFilterSettingsUI(filterForm);
        fireGroupingOrFilterChanged();
      }

      @Override
      public void filtersResetRequested() {
        getPresentationHelper().resetAllFilters();
        filterForm.reset(getPresentationHelper());
        fireGroupingOrFilterChanged();
      }
    });

    createAndShowPopup("Dart Problems Filter", filterForm.getMainPanel());
  }

  private void showAnalysisServerSettingsPopup() {
    final DartAnalysisServerSettingsForm serverSettingsForm = new DartAnalysisServerSettingsForm(myProject);
    serverSettingsForm.reset(getPresentationHelper());

    serverSettingsForm.addListener(() -> {
      getPresentationHelper().updateFromServerSettingsUI(serverSettingsForm);
      DartAnalysisServerService.getInstance(myProject).ensureAnalysisRootsUpToDate();
    });

    createAndShowPopup(DartBundle.message("analysis.server.settings.title"), serverSettingsForm.getMainPanel());
  }

  public void setErrors(@NotNull final Map<String, List<? extends AnalysisError>> filePathToErrors) {
    final DartProblemsViewSettings.ScopedAnalysisMode scopedAnalysisMode = getPresentationHelper().getScopedAnalysisMode();
    final List<AnalysisProblem> problemsToAdd = new ArrayList<>();
    for (Map.Entry<String, List<? extends AnalysisError>> entry : filePathToErrors.entrySet()) {
      final String filePath = entry.getKey();
      final VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(filePath);
      final boolean fileOk = vFile != null && (scopedAnalysisMode != DartProblemsViewSettings.ScopedAnalysisMode.All ||
                                               ProjectFileIndex.getInstance(myProject).isInContent(vFile));
      List<? extends AnalysisError> errors = fileOk ? entry.getValue() : AnalysisError.EMPTY_LIST;

      for (AnalysisError analysisError : errors) {
        if (DartAnnotator.shouldIgnoreMessageFromDartAnalyzer(filePath, analysisError.getLocation().getFile())) {
          continue;
        }

        final AnalysisProblem problem = new DartProblem(myProject, analysisError);
        problemsToAdd.add(problem);
      }
    }

    AnalysisProblem oldSelectedProblem = myTable.getSelectedObject();
    DartProblemsTableModel model = getModel();
    AnalysisProblem updatedSelectedProblem = model.setErrorsAndReturnReplacementForSelection(filePathToErrors.keySet(), problemsToAdd, oldSelectedProblem);

    if (updatedSelectedProblem != null) {
      myTable.setSelection(Collections.singletonList(updatedSelectedProblem));
    }

    updateStatusDescription();
  }

  @NotNull
  @Override
  public DartProblemsTableModel getModel() {
    return (DartProblemsTableModel)super.getModel();
  }

  private class FilterProblemsAction extends DumbAwareAction implements Toggleable {
    FilterProblemsAction() {
      super(DartBundle.lazyMessage("filter.problems"), DartBundle.lazyMessage("filter.problems.description"), AllIcons.General.Filter);
    }

    @Override
    public void update(@NotNull final AnActionEvent e) {
      // show icon as toggled on if any filter is active
      Toggleable.setSelected(e.getPresentation(), getPresentationHelper().areFiltersApplied());
    }

    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {
      showFiltersPopup();
    }
  }

  private class AnalysisServerSettingsAction extends DumbAwareAction implements Toggleable {
    AnalysisServerSettingsAction() {
      super(DartBundle.lazyMessage("analysis.server.settings"), DartBundle.lazyMessage("analysis.server.settings.description"),
            AllIcons.General.GearPlain);
    }

    @Override
    public void update(@NotNull final AnActionEvent e) {
      final boolean asByDefault = getPresentationHelper().getScopedAnalysisMode() == DartProblemsViewSettings.SCOPED_ANALYSIS_MODE_DEFAULT;
      Toggleable.setSelected(e.getPresentation(), !asByDefault);
    }

    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {
      showAnalysisServerSettingsPopup();
    }
  }
}
