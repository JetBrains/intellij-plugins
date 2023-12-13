package com.jetbrains.lang.dart.ide.completion;

import com.intellij.CommonBundle;
import com.intellij.codeInsight.editorActions.smartEnter.SmartEnterProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.assists.AssistUtils;
import com.jetbrains.lang.dart.assists.DartSourceEditException;
import org.dartlang.analysis.server.protocol.Position;
import org.dartlang.analysis.server.protocol.SourceChange;
import org.dartlang.analysis.server.protocol.SourceEdit;
import org.dartlang.analysis.server.protocol.SourceFileEdit;
import org.jetbrains.annotations.NotNull;

public final class DartServerStatementCompletionProcessor extends SmartEnterProcessor {

  @Override
  public boolean process(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile psiFile) {
    final int offset = editor.getCaretModel().getOffset();
    final DartAnalysisServerService service = DartAnalysisServerService.getInstance(psiFile.getProject());
    service.updateFilesContent();
    SourceChange sourceChange = service.edit_getStatementCompletion(psiFile.getVirtualFile(), offset);
    if (sourceChange != null && !isNoop(sourceChange)) {
      try {
        AssistUtils.applySourceChange(project, sourceChange, true);
        Position position = sourceChange.getSelection();
        if (position != null) {
          // The position should never be null but it might be if unit tests are flaky.
          editor.getCaretModel().moveToOffset(service.getConvertedOffset(psiFile.getVirtualFile(), position.getOffset()));
        }
      }
      catch (DartSourceEditException e) {
        CommonRefactoringUtil.showErrorHint(project, editor, e.getMessage(), CommonBundle.getErrorTitle(), null);
      }
      return true;
    }
    return false;
  }

  private static boolean isNoop(@NotNull final SourceChange sourceChange) {
    for (SourceFileEdit fileEdit : sourceChange.getEdits()) {
      for (SourceEdit edit : fileEdit.getEdits()) {
        if (edit.getLength() != 0 || !edit.getReplacement().isEmpty()) {
          return false;
        }
      }
    }
    return true;
  }
}
