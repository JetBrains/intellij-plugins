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

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScopesCore;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;

public class ReanalyzeAction extends AnAction implements DumbAware {
  public ReanalyzeAction() {
    super(DartBundle.message("dart.reanalyze.action.name"), DartBundle.message("dart.reanalyze.action.description"), DartIcons.Dart_16);
  }

  private static boolean isEnabled(AnActionEvent event) {
    final Project project = event.getProject();
    if (project == null) {
      return false;
    }

    final Editor editor = event.getData(CommonDataKeys.EDITOR);
    if (editor != null) {
      final Document document = editor.getDocument();
      final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
      return isDartFile(psiFile);
    }

    final VirtualFile[] filesAndDirs = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(event.getDataContext());
    if (filesAndDirs == null) {
      return false;
    }
    return hasDartFile(project, filesAndDirs);
  }

  private static boolean isDartFile(PsiFile file) {
    return file != null && file.getFileType() == DartFileType.INSTANCE;
  }

  private static boolean isDartFile(VirtualFile file) {
    return file != null && file.getFileType() == DartFileType.INSTANCE;
  }

  private static boolean hasDartFile(@NotNull Project project, @NotNull VirtualFile[] files) {
    for (VirtualFile fileOrDir : files) {
      if (!fileOrDir.isDirectory() && isDartFile(fileOrDir)) {
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

  @Override
  public void actionPerformed(AnActionEvent e) {
    DartAnalysisServerService.getInstance().analysis_reanalyze(null);
  }

  @Override
  public void update(AnActionEvent event) {
    final Presentation presentation = event.getPresentation();
    final boolean enabled = isEnabled(event);
    presentation.setEnabledAndVisible(enabled);
  }
}
