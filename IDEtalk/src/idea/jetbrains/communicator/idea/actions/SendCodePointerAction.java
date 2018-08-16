// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea.actions;

import com.intellij.openapi.editor.Editor;
import jetbrains.communicator.commands.FileCommand;
import jetbrains.communicator.commands.SendCodePointerCommand;
import jetbrains.communicator.core.vfs.CodePointer;
import org.picocontainer.MutablePicoContainer;

/**
 * @author Kir
 */
public class SendCodePointerAction extends BaseEditorAction {
  public SendCodePointerAction() {
    super(SendCodePointerCommand.class);
  }

  @Override
  protected void prepareCommand(FileCommand command, Editor editor, MutablePicoContainer container) {
    CodePointer codePointer = ActionUtil.getCodePointer(editor);
    ((SendCodePointerCommand) command).setCodePointer(codePointer);
  }
}
