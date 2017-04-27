package com.jetbrains.lang.dart.ide.completion;

import com.intellij.CommonBundle;
import com.intellij.codeInsight.editorActions.smartEnter.SmartEnterProcessor;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.assists.AssistUtils;
import com.jetbrains.lang.dart.assists.DartSourceEditException;
import org.dartlang.analysis.server.protocol.Position;
import org.dartlang.analysis.server.protocol.SourceChange;
import org.jetbrains.annotations.NotNull;

public class DartServerStatementCompletionProcessor extends SmartEnterProcessor {

  @Override
  public boolean process(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile psiFile) {
    final Caret currentCaret = editor.getCaretModel().getPrimaryCaret();
    final int offset = currentCaret.getSelectionStart();
    final DartAnalysisServerService service = DartAnalysisServerService.getInstance(psiFile.getProject());
    service.updateFilesContent();
    SourceChange sourceChange = service.edit_getStatementCompletion(psiFile.getVirtualFile(), offset);
    if (sourceChange != null) {
      try {
        AssistUtils.applySourceChange(project, sourceChange, true);
        Position position = sourceChange.getSelection();
        editor.getCaretModel().moveToOffset(position.getOffset());
      }
      catch (DartSourceEditException e) {
        CommonRefactoringUtil.showErrorHint(project, editor, e.getMessage(), CommonBundle.getErrorTitle(), null);
      }
      return true;
    }
    return false;
  }
}
