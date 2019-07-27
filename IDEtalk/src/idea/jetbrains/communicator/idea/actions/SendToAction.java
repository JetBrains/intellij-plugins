// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea.actions;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import jetbrains.communicator.commands.SendCodePointerCommand;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.vfs.CodePointer;
import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.idea.VFSUtil;
import jetbrains.communicator.util.CommunicatorStrings;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NonNls;

/**
 * @author Kir
 *
 */
public class SendToAction extends BaseEditorPopup {
  @NonNls
  private static final Logger LOG = Logger.getLogger(SendToAction.class);

  @Override
  protected String getActionDescription(User user, VirtualFile file) {
    return CommunicatorStrings.getMsg("code_pointer.description", user.getDisplayName());
  }

  @Override
  protected void doActionCommand(final User user, final VirtualFile file, final Editor editor) {
    VFile vFile = VFSUtil.createFileFrom(file, editor.getProject());
    if (vFile == null) {
      LOG.info("Unable to send code pointer for " + file);
      return;
    }

    CodePointer codePointer = ActionUtil.getCodePointer(editor);

    SendCodePointerCommand command =
        Pico.getCommandManager().getCommand(SendCodePointerCommand.class, BaseAction.getContainer(editor.getProject()));
    command.setCodePointer(codePointer);
    command.setVFile(vFile);
    command.setUser(user);
    command.execute();
  }

  @Override
  protected boolean shouldAddUserToChoiceList(User user) {
    return true;
  }

}
