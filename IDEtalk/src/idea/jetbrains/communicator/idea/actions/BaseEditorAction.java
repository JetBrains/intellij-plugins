// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;
import jetbrains.communicator.commands.FileCommand;
import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.ide.UserListComponent;
import jetbrains.communicator.idea.VFSUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.picocontainer.MutablePicoContainer;

/**
 * @author Kir
 */
public class BaseEditorAction<T extends FileCommand> extends BaseAction<T> {
  @NonNls
  private static final Logger LOG = Logger.getInstance(BaseEditorAction.class);

  public BaseEditorAction(Class<T> commandClass) {
    super(commandClass);
  }

  @Override
  public void update(@NotNull AnActionEvent e) {

    FileEditorManager editorManager = FileEditorManager.getInstance(getProject(e));
    Editor editor = editorManager.getSelectedTextEditor();

    T command = getCommand(e);
    MutablePicoContainer container = getContainer(e);

    if (command != null && editor != null && container != null) {

      VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
      if (file != null) {
        VFile vFile = VFSUtil.createFileFrom(file, editor.getProject());
        if (vFile == null) {
          return;
        }

        command.setVFile(vFile);
        setUser(container, command);

        prepareCommand(command, editor, container);
      }
    }

    super.update(e);
  }

  protected void prepareCommand(T command, Editor editor, MutablePicoContainer container) {
  }

  private void setUser(MutablePicoContainer container, T command) {
    UserListComponent userList = (UserListComponent) container.getComponentInstanceOfType(UserListComponent.class);
    command.setUser(userList.getSelectedUser());
  }
}
