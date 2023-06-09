// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.actions;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.hint.HintManagerImpl;
import com.intellij.codeInsight.hint.HintUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import com.intellij.ui.LightweightHint;
import com.intellij.util.SmartList;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkLibUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 * An abstract {@link AnAction} for processing a single Dart file open in the editor,
 * or a group of selected Dart files.
 */
public abstract class AbstractDartFileProcessingAction extends AnAction implements DumbAware {

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Override
  public void actionPerformed(@NotNull final AnActionEvent event) {
    final Project project = event.getProject();
    if (project == null) return;

    final DartSdk sdk = DartSdk.getDartSdk(project);
    if (sdk == null || !DartAnalysisServerService.isDartSdkVersionSufficient(sdk)) return;

    PsiDocumentManager.getInstance(project).commitAllDocuments();
    final Editor editor = event.getData(CommonDataKeys.EDITOR);

    if (editor != null) {
      final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
      if (psiFile == null || !isApplicableFile(project, psiFile.getVirtualFile())) return;
      runOverEditor(project, editor, psiFile);
    }
    else {
      final VirtualFile[] filesAndDirs = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
      if (filesAndDirs != null && DartAnalysisServerService.getInstance(project).serverReadyForRequest()) {
        final List<VirtualFile> files = getApplicableVirtualFiles(project, filesAndDirs);
        runOverFiles(project, files);
      }
    }
  }

  @NotNull
  protected abstract @Nls String getActionTextForEditor();

  @NotNull
  protected abstract @Nls String getActionTextForFiles();

  protected abstract void runOverEditor(@NotNull final Project project,
                                        @NotNull final Editor editor,
                                        @NotNull final PsiFile psiFile);

  protected abstract void runOverFiles(@NotNull final Project project, @NotNull final List<VirtualFile> dartFiles);

  @Override
  public void update(@NotNull final AnActionEvent event) {
    final Presentation presentation = event.getPresentation();
    final Project project = event.getProject();
    if (project == null) {
      presentation.setEnabledAndVisible(false);
      return;
    }

    final DartSdk sdk = DartSdk.getDartSdk(project);
    if (sdk == null || !DartAnalysisServerService.isDartSdkVersionSufficient(sdk)) {
      presentation.setEnabledAndVisible(false);
      return;
    }

    final Editor editor = event.getData(CommonDataKeys.EDITOR);
    if (editor != null) {
      final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
      // visible for any Dart file, but enabled for applicable only
      presentation.setVisible(psiFile != null && psiFile.getFileType() == DartFileType.INSTANCE);
      presentation.setEnabled(psiFile != null && isApplicableFile(project, psiFile.getVirtualFile()));
      presentation.setText(getActionTextForEditor());
      return;
    }

    final VirtualFile[] filesAndDirs = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
    if (filesAndDirs == null) {
      presentation.setEnabledAndVisible(false);
      return;
    }

    presentation.setEnabledAndVisible(mayHaveApplicableDartFiles(project, filesAndDirs));
    presentation.setText(getActionTextForFiles());
  }

  public static void showHintLater(@NotNull final Editor editor, @NotNull final @NlsContexts.HintText String text, final boolean error) {
    ApplicationManager.getApplication().invokeLater(() -> {
      final JComponent component = error ? HintUtil.createErrorLabel(text) : HintUtil.createInformationLabel(text);
      final LightweightHint hint = new LightweightHint(component);
      HintManagerImpl.getInstanceImpl().showEditorHint(hint, editor, HintManager.UNDER, HintManager.HIDE_BY_ANY_KEY |
                                                                                        HintManager.HIDE_BY_TEXT_CHANGE |
                                                                                        HintManager.HIDE_BY_SCROLLING, 0, false);
    }, ModalityState.nonModal(), o -> editor.isDisposed() || !editor.getComponent().isShowing());
  }

  @NotNull
  private static List<VirtualFile> getApplicableVirtualFiles(@NotNull final Project project,
                                                             final VirtualFile @NotNull [] filesAndDirs) {
    final List<VirtualFile> result = new SmartList<>();

    GlobalSearchScope dirScope = null;

    for (VirtualFile fileOrDir : filesAndDirs) {
      if (fileOrDir.isDirectory()) {
        if (dirScope == null) {
          dirScope = GlobalSearchScopesCore.directoryScope(project, fileOrDir, true);
        }
        else {
          dirScope = dirScope.union(GlobalSearchScopesCore.directoryScope(project, fileOrDir, true));
        }
      }
      else if (isApplicableFile(project, fileOrDir)) {
        result.add(fileOrDir);
      }
    }

    if (dirScope != null) {
      for (VirtualFile file : FileTypeIndex
        .getFiles(DartFileType.INSTANCE, GlobalSearchScope.projectScope(project).intersectWith(dirScope))) {
        if (isApplicableFile(project, file)) {
          result.add(file);
        }
      }
    }

    return result;
  }

  private static boolean isApplicableFile(@NotNull final Project project, @Nullable final VirtualFile file) {
    if (file == null || !FileTypeRegistry.getInstance().isFileOfType(file, DartFileType.INSTANCE)) return false;
    if (!ProjectFileIndex.getInstance(project).isInContent(file)) return false;

    final Module module = ModuleUtilCore.findModuleForFile(file, project);
    if (module == null) return false;
    if (!DartSdkLibUtil.isDartSdkEnabled(module)) return false;

    return true;
  }

  private static boolean mayHaveApplicableDartFiles(@NotNull final Project project,
                                                    final VirtualFile @NotNull [] files) {
    for (VirtualFile fileOrDir : files) {
      if (!fileOrDir.isDirectory() && isApplicableFile(project, fileOrDir)) {
        return true;
      }

      if (fileOrDir.isDirectory() &&
          ProjectRootManager.getInstance(project).getFileIndex().isInContent(fileOrDir) &&
          FileTypeIndex.containsFileOfType(DartFileType.INSTANCE, GlobalSearchScopesCore.directoryScope(project, fileOrDir, true))) {
        return true;
      }
    }

    return false;
  }
}
