// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.assists;

import com.intellij.CommonBundle;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.psi.DartFile;
import org.dartlang.analysis.server.protocol.SourceChange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This intention is registered in plugin.xml to make sure that it appears in Preferences (Settings) UI and can be switched on/off.
 * But the instance created this way (using default constructor) always says that it is not available.
 * Real intentions are added dynamically in {@link DartAnalysisServerService#registerQuickAssistIntentions()}.
 * We need to register them not via plugin.xml for 2 reasons:
 * <ul>
 * <li>intentions amount, text and behavior are loaded dynamically</li>
 * <li>intentions registered via plugin.xml are wrapped in IntentionActionWrapper that doesn't implement Comparable, but order is important for us</li>
 * </ul>
 */
public class DartQuickAssistIntention implements IntentionAction, Comparable<IntentionAction> {
  @Nullable private final QuickAssistSet quickAssistSet;
  private final int index;
  @Nullable private SourceChange sourceChange;

  // invoked by Platform because registered in plugin.xml
  @SuppressWarnings("unused")
  public DartQuickAssistIntention() {
    quickAssistSet = null;
    index = -1;
  }

  public DartQuickAssistIntention(@NotNull final QuickAssistSet quickAssistSet, final int index) {
    this.quickAssistSet = quickAssistSet;
    this.index = index;
  }

  @Override
  public int compareTo(IntentionAction o) {
    if (o instanceof DartQuickAssistIntention) {
      final DartQuickAssistIntention other = (DartQuickAssistIntention)o;
      return index - other.index;
    }
    return 0;
  }

  @NotNull
  @Override
  public String getFamilyName() {
    final String message = DartBundle.message("dart.quick.assist.family.name");
    // a bit hacky way to make inspections enabling/disabling work, see IntentionActionWrapper.getFullFamilyName()
    return quickAssistSet == null ? message : "Dart/" + message;
  }

  @NotNull
  @Override
  public String getText() {
    return sourceChange == null ? "" : sourceChange.getMessage();
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
    if (editor == null || file == null) {
      // not sure this can ever happen
      return;
    }

    if (sourceChange != null) {
      DartAnalysisServerService.getInstance(project).fireBeforeQuickAssistIntentionInvoked(this, editor, file);
      try {
        AssistUtils.applySourceChange(project, sourceChange, true);
      }
      catch (DartSourceEditException e) {
        CommonRefactoringUtil.showErrorHint(project, editor, e.getMessage(), CommonBundle.getErrorTitle(), null);
      }
    }
  }

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
    if (editor == null || file == null) {
      // not sure this can ever happen
      return false;
    }

    if (quickAssistSet == null || !(file instanceof DartFile) || !DartAnalysisServerService.isLocalAnalyzableFile(file.getVirtualFile())) {
      return false;
    }

    final List<SourceChange> sourceChanges = quickAssistSet.getQuickAssists(editor, file);
    if (sourceChanges.size() <= index) {
      sourceChange = null;
      return false;
    }
    sourceChange = sourceChanges.get(index);
    return true;
  }

  @Override
  public boolean startInWriteAction() {
    return true;
  }
}
