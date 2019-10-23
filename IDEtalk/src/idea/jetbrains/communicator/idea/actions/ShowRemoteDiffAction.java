// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea.actions;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import jetbrains.communicator.commands.ShowDiffCommand;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.idea.VFSUtil;
import jetbrains.communicator.util.CommunicatorStrings;
import org.jetbrains.annotations.NonNls;

/**
 * @author Kir
 *
 */
public class ShowRemoteDiffAction extends BaseEditorPopup {
  @NonNls
  private static final Logger LOG = Logger.getInstance(ShowRemoteDiffAction.class);

  @Override
  protected String getActionDescription(User user, VirtualFile file) {
    return CommunicatorStrings.getMsg("show.diff.description", file.getPresentableName(), user.getDisplayName());
  }

  @Override
  protected void doActionCommand(final User user, final VirtualFile file, final Editor editor) {
    VFile vFile = VFSUtil.createFileFrom(file, editor.getProject());
    if (vFile == null) {
      LOG.info("Unable to get file " + file);
      return;
    }

    ShowDiffCommand command = Pico.getCommandManager().getCommand(ShowDiffCommand.class, BaseAction.getContainer(editor.getProject()));
    command.setUser(user);
    command.setVFile(vFile);
    command.execute();
  }

  @Override
  protected boolean shouldAddUserToChoiceList(User user) {
    return user.isOnline();
  }

}
