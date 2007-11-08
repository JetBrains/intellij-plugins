/*
 * Copyright 2000-2006 JetBrains s.r.o.
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
package jetbrains.communicator.idea.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.vfs.VirtualFile;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.core.vfs.CodePointer;

/**
 * @author Kir
 */
public class ActionUtil {
  private ActionUtil() {
  }

  static VirtualFile getFile(AnActionEvent e) {
    return (PlatformDataKeys.VIRTUAL_FILE.getData(e.getDataContext()));
  }

  static Editor getEditor(AnActionEvent e) {
    return (DataKeys.EDITOR.getData(e.getDataContext()));
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
