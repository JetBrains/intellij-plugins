// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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

public class DartQuickAssistIntention implements IntentionAction, Comparable<IntentionAction> {
  private final int myIndex;
  private @Nullable SourceChange sourceChange;

  public DartQuickAssistIntention(int index) {
    myIndex = index;
  }

  @Override
  public int compareTo(IntentionAction o) {
    if (o instanceof DartQuickAssistIntention) {
      final DartQuickAssistIntention other = (DartQuickAssistIntention)o;
      return myIndex - other.myIndex;
    }
    return 0;
  }

  @Override
  public @NotNull String getFamilyName() {
    return DartBundle.message("dart.quick.assist.family.name");
  }

  @Override
  public @NotNull String getText() {
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

    if (!(file instanceof DartFile) || !DartAnalysisServerService.isLocalAnalyzableFile(file.getVirtualFile())) {
      return false;
    }

    final List<SourceChange> sourceChanges = DartQuickAssistSet.getInstance().getQuickAssists(editor, file);
    if (sourceChanges.size() <= myIndex) {
      sourceChange = null;
      return false;
    }
    sourceChange = sourceChanges.get(myIndex);
    return true;
  }

  @Override
  public boolean startInWriteAction() {
    return true;
  }

  //@formatter:off
  public static class DartQuickAssistIntention0  extends DartQuickAssistIntention{ public DartQuickAssistIntention0()  {super(0); } }
  public static class DartQuickAssistIntention1  extends DartQuickAssistIntention{ public DartQuickAssistIntention1()  {super(1); } }
  public static class DartQuickAssistIntention2  extends DartQuickAssistIntention{ public DartQuickAssistIntention2()  {super(2); } }
  public static class DartQuickAssistIntention3  extends DartQuickAssistIntention{ public DartQuickAssistIntention3()  {super(3); } }
  public static class DartQuickAssistIntention4  extends DartQuickAssistIntention{ public DartQuickAssistIntention4()  {super(4); } }
  public static class DartQuickAssistIntention5  extends DartQuickAssistIntention{ public DartQuickAssistIntention5()  {super(5); } }
  public static class DartQuickAssistIntention6  extends DartQuickAssistIntention{ public DartQuickAssistIntention6()  {super(6); } }
  public static class DartQuickAssistIntention7  extends DartQuickAssistIntention{ public DartQuickAssistIntention7()  {super(7); } }
  public static class DartQuickAssistIntention8  extends DartQuickAssistIntention{ public DartQuickAssistIntention8()  {super(8); } }
  public static class DartQuickAssistIntention9  extends DartQuickAssistIntention{ public DartQuickAssistIntention9()  {super(9); } }
  public static class DartQuickAssistIntention10 extends DartQuickAssistIntention{ public DartQuickAssistIntention10() {super(10);} }
  public static class DartQuickAssistIntention11 extends DartQuickAssistIntention{ public DartQuickAssistIntention11() {super(11);} }
  public static class DartQuickAssistIntention12 extends DartQuickAssistIntention{ public DartQuickAssistIntention12() {super(12);} }
  public static class DartQuickAssistIntention13 extends DartQuickAssistIntention{ public DartQuickAssistIntention13() {super(13);} }
  public static class DartQuickAssistIntention14 extends DartQuickAssistIntention{ public DartQuickAssistIntention14() {super(14);} }
  //@formatter:on
}
