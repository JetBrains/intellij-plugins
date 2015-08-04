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

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.psi.DartFile;
import org.dartlang.analysis.server.protocol.SourceChange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DartQuickAssistIntention implements IntentionAction, Comparable<IntentionAction> {
  private final QuickAssistSet quickAssistSet;
  private final int index;
  @Nullable private SourceChange sourceChange;

  public DartQuickAssistIntention(final QuickAssistSet quickAssistSet, final int index) {
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
    //noinspection DialogTitleCapitalization
    return DartBundle.message("dart.quick.assist.family.name");
  }

  @NotNull
  @Override
  public String getText() {
    return sourceChange == null ? "" : sourceChange.getMessage();
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
    if (sourceChange != null) {
      AssistUtils.applySourceChange(project, sourceChange);
    }
  }

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
    if (!(file instanceof DartFile)) {
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
