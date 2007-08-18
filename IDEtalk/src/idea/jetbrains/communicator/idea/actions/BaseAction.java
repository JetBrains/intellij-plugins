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

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.commands.NamedUserCommand;
import jetbrains.communicator.core.commands.UserCommand;
import jetbrains.communicator.idea.IDEtalkContainerRegistry;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.picocontainer.MutablePicoContainer;

import java.awt.*;

/**
 * @author Kir Maximov
 */
public class BaseAction<T extends UserCommand> extends AnAction {
  private static final Logger LOG = Logger.getLogger(BaseAction.class);
  private final Class<T> myCommandClass;

  public BaseAction(Class<T> commandClass) {
    myCommandClass = commandClass;
  }

  @Nullable
  public T getCommand(AnActionEvent e) {
    return getCommand(getContainer(e));
  }

  @Nullable
  public T getCommand(MutablePicoContainer container) {
    if (container == null) {
      return null;
    }

    try {
      return Pico.getCommandManager().getCommand(myCommandClass, container);
    } catch (Throwable ex) {
      LOG.error(ex.getMessage(), ex);
    }
    return null;
  }

  @Nullable
  public static MutablePicoContainer getContainer(AnActionEvent e) {
    return getContainer(getProject(e));
  }

  @Nullable
  public static Project getProject(AnActionEvent e) {
    return (DataKeys.PROJECT.getData(e.getDataContext()));
  }

  @Nullable
  public static MutablePicoContainer getContainer(Component c) {
    Project project = DataKeys.PROJECT.getData(DataManager.getInstance().getDataContext(c));
    return getContainer(project);
  }

  @Nullable
  public static MutablePicoContainer getContainer(Project project) {
    if (project != null) {
      IDEtalkContainerRegistry registry = project.getComponent(IDEtalkContainerRegistry.class);
      assert registry != null;
      return registry.getContainer();
    }
    return null;
  }

  public void actionPerformed(AnActionEvent e) {
    T t = getCommand(e);
    if (t != null) {
      t.execute();
    }
  }

  public void update(AnActionEvent e) {
    super.update(e);
    UserCommand command = getCommand(e);
    e.getPresentation().setEnabled(command != null && command.isEnabled());
    if (command instanceof NamedUserCommand) {
      NamedUserCommand userCommand = (NamedUserCommand) command;
      e.getPresentation().setText(userCommand.getName(), true);
      e.getPresentation().setIcon(userCommand.getIcon());
    }
  }

}
