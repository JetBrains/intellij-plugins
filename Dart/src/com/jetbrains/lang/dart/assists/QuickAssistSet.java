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
package com.jetbrains.lang.dart.assists;

import com.google.common.collect.Lists;
import com.google.dart.server.utilities.general.ObjectUtilities;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import org.dartlang.analysis.server.protocol.SourceChange;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class QuickAssistSet {
  private List<SourceChange> lastSourceChanges = Lists.newArrayList();
  private long lastPsiModificationCount;
  private String lastFilePath;
  private int lastOffset;
  private int lastLength;

  public List<SourceChange> getQuickAssists(@NotNull final Editor editor, @NotNull final PsiFile psiFile) {
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
    lastFilePath = filePath;
    lastOffset = offset;
    lastLength = length;
    lastPsiModificationCount = psiModificationCount;
    final DartAnalysisServerService service = DartAnalysisServerService.getInstance(psiFile.getProject());
    service.updateFilesContent();
    lastSourceChanges = service.edit_getAssists(psiFile.getVirtualFile(), offset, length);
    return lastSourceChanges;
  }
}
