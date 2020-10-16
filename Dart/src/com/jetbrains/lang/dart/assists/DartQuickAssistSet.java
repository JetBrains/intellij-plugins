// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.assists;

import com.google.dart.server.utilities.general.ObjectUtilities;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import org.dartlang.analysis.server.protocol.SourceChange;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DartQuickAssistSet {
  private List<SourceChange> lastSourceChanges = new ArrayList<>();
  private long lastPsiModificationCount;
  private String lastFilePath;
  private int lastOffset;
  private int lastLength;

  static @NotNull DartQuickAssistSet getInstance() {
    return ApplicationManager.getApplication().getService(DartQuickAssistSet.class);
  }

  public synchronized List<SourceChange> getQuickAssists(@NotNull final Editor editor, @NotNull final PsiFile psiFile) {
    final long psiModificationCount = psiFile.getManager().getModificationTracker().getModificationCount();
    final String filePath = psiFile.getVirtualFile().getPath();
    final Caret currentCaret = editor.getCaretModel().getPrimaryCaret();
    final int offset = currentCaret.getSelectionStart();
    final int length = currentCaret.getSelectionEnd() - offset;
    if (lastPsiModificationCount == psiModificationCount &&
        ObjectUtilities.equals(lastFilePath, filePath) &&
        lastOffset == offset &&
        lastLength == length) {
      return lastSourceChanges;
    }

    final DartAnalysisServerService service = DartAnalysisServerService.getInstance(psiFile.getProject());
    service.updateFilesContent();
    lastSourceChanges = service.edit_getAssists(psiFile.getVirtualFile(), offset, length);

    lastFilePath = filePath;
    lastOffset = offset;
    lastLength = length;
    lastPsiModificationCount = psiModificationCount;

    return lastSourceChanges;
  }
}
