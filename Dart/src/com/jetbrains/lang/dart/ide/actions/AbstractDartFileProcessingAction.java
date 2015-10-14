/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.lang.dart.ide.actions;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.hint.HintManagerImpl;
import com.intellij.codeInsight.hint.HintUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
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
import com.jetbrains.lang.dart.ide.DartWritingAccessProvider;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkGlobalLibUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 * An abstract {@link AnAction} for processing a single Dart file open in the editor,
 * or a group of selected Dart files.
 */
public abstract class AbstractDartFileProcessingAction extends AnAction implements DumbAware {
  public AbstractDartFileProcessingAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
    super(text, description, icon);
  }

  @Override
  public void actionPerformed(final AnActionEvent event) {
    final Project project = event.getProject();
    if (project == null) return;

    final DartSdk sdk = DartSdk.getDartSdk(project);
    if (sdk == null || !DartAnalysisServerService.isDartSdkVersionSufficient(sdk)) return;

    PsiDocumentManager.getInstance(project).commitAllDocuments();
    final Editor editor = event.getData(CommonDataKeys.EDITOR);

    if (editor != null) {
      final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
      if (!isApplicableFile(psiFile)) return;
      runOverEditor(project, editor, psiFile);
    }
    else {
      final VirtualFile[] filesAndDirs = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(event.getDataContext());
      if (filesAndDirs != null) {
        final List<VirtualFile> files = getApplicableVirtualFiles(project, filesAndDirs);
        runOverFiles(project, files);
      }
    }
  }

  @NotNull
  protected abstract String getActionTextForEditor();

  @NotNull
  protected abstract String getActionTextForFiles();

  protected abstract void runOverEditor(@NotNull final Project project,
                                        @NotNull final Editor editor,
                                        @NotNull final PsiFile psiFile);

  protected abstract void runOverFiles(@NotNull final Project project, @NotNull final List<VirtualFile> dartFiles);

  @Override
  public void update(final AnActionEvent event) {
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
      presentation.setEnabled(isApplicableFile(psiFile));
      presentation.setText(getActionTextForEditor());
      return;
    }

    final VirtualFile[] filesAndDirs = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(event.getDataContext());
    if (filesAndDirs == null) {
      presentation.setEnabledAndVisible(false);
      return;
    }

    presentation.setEnabledAndVisible(mayHaveApplicableDartFiles(project, filesAndDirs));
    presentation.setText(getActionTextForFiles());
  }

  public static void showHintLater(@NotNull final Editor editor, @NotNull final String text, final boolean error) {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        final JComponent component = error ? HintUtil.createErrorLabel(text) : HintUtil.createInformationLabel(text);
        final LightweightHint hint = new LightweightHint(component);
        HintManagerImpl.getInstanceImpl().showEditorHint(hint, editor, HintManager.UNDER, HintManager.HIDE_BY_ANY_KEY |
                                                                                          HintManager.HIDE_BY_TEXT_CHANGE |
                                                                                          HintManager.HIDE_BY_SCROLLING, 0, false);
      }
    }, ModalityState.NON_MODAL, new Condition() {
      @Override
      public boolean value(Object o) {
        return editor.isDisposed() || !editor.getComponent().isShowing();
      }
    });
  }

  @NotNull
  private static List<VirtualFile> getApplicableVirtualFiles(@NotNull final Project project,
                                                             @NotNull final VirtualFile[] filesAndDirs) {
    final List<VirtualFile> result = new SmartList<VirtualFile>();

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

  private static boolean isApplicableFile(@NotNull final Project project, @NotNull final VirtualFile file) {
    if (file.getFileType() != DartFileType.INSTANCE) return false;

    final Module module = ModuleUtilCore.findModuleForFile(file, project);
    if (module == null) return false;

    if (!DartSdkGlobalLibUtil.isDartSdkEnabled(module)) return false;

    if (DartWritingAccessProvider.isInDartSdkOrDartPackagesFolder(project, file)) return false;

    return true;
  }

  private static boolean isApplicableFile(@Nullable final PsiFile psiFile) {
    if (psiFile == null || psiFile.getVirtualFile() == null || psiFile.getFileType() != DartFileType.INSTANCE) return false;

    final Module module = ModuleUtilCore.findModuleForPsiElement(psiFile);
    if (module == null) return false;

    if (!DartSdkGlobalLibUtil.isDartSdkEnabled(module)) return false;

    if (DartWritingAccessProvider.isInDartSdkOrDartPackagesFolder(psiFile)) return false;

    return true;
  }

  private static boolean mayHaveApplicableDartFiles(@NotNull final Project project,
                                                    @NotNull final VirtualFile[] files) {
    for (VirtualFile fileOrDir : files) {
      if (!fileOrDir.isDirectory() && isApplicableFile(project, fileOrDir)) {
        return true;
      }
    }

    for (VirtualFile fileOrDir : files) {
      if (fileOrDir.isDirectory() &&
          FileTypeIndex.containsFileOfType(DartFileType.INSTANCE, GlobalSearchScopesCore.directoryScope(project, fileOrDir, true))) {
        return true;
      }
    }

    return false;
  }
}
