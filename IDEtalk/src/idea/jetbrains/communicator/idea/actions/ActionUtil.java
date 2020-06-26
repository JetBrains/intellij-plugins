// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.vfs.VirtualFile;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.core.vfs.CodePointer;

/**
 * @author Kir
 */
public final class ActionUtil {
  private ActionUtil() {
  }

  static VirtualFile getFile(AnActionEvent e) {
    return e.getData(CommonDataKeys.VIRTUAL_FILE);
  }

  static Editor getEditor(AnActionEvent e) {
    return e.getData(CommonDataKeys.EDITOR);
  }

  static UserModel getUserModel() {
    return ((UserModel) Pico.getInstance().getComponentInstanceOfType(UserModel.class));
  }

  static CodePointer getCodePointer(Editor editor) {
    CodePointer codePointer;
    int selectionStart = editor.getSelectionModel().getSelectionStart();
    int selectionEnd = editor.getSelectionModel().getSelectionEnd();
    if (selectionStart != selectionEnd) {
      LogicalPosition start = editor.offsetToLogicalPosition(selectionStart);
      LogicalPosition end = editor.offsetToLogicalPosition(selectionEnd);
      codePointer = new CodePointer(start.line, start.column, end.line, end.column);
    }
    else {
      LogicalPosition pos = editor.getCaretModel().getLogicalPosition();
      codePointer = new CodePointer(pos.line, pos.column);
    }
    return codePointer;
  }
}
