package com.jetbrains.lang.dart.ide.runner.base;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.EvaluationMode;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.jetbrains.lang.dart.DartFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author: Fedor.Korotkov
 */
public class DartDebuggerEditorsProvider extends XDebuggerEditorsProvider {
  @NotNull
  public FileType getFileType() {
    return DartFileType.INSTANCE;
  }

  @NotNull
  public Document createDocument(@NotNull final Project project,
                                 @NotNull final String text, @Nullable final XSourcePosition position, @NotNull EvaluationMode mode) {
    return DartDebuggerSupportUtils.createDocument(
      text,
      project,
      position != null ? position.getFile() : null, position != null ? position.getOffset() : -1
    );
  }
}
