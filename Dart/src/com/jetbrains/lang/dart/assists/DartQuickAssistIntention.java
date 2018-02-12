// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
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
    if (sourceChange != null) {
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
    if (quickAssistSet == null || !(file instanceof DartFile)) return false;

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
