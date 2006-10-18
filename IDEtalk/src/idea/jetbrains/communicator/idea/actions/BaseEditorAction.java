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
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;
import jetbrains.communicator.commands.FileCommand;
import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.ide.UserListComponent;
import jetbrains.communicator.idea.VFSUtil;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NonNls;
import org.picocontainer.MutablePicoContainer;

/**
 * @author Kir
 */
public class BaseEditorAction<T extends FileCommand> extends BaseAction<T> {
  @NonNls
  private static final Logger LOG = Logger.getLogger(BaseEditorAction.class);

  public BaseEditorAction(Class<T> commandClass) {
    super(commandClass);
  }

  public void update(AnActionEvent e) {

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
