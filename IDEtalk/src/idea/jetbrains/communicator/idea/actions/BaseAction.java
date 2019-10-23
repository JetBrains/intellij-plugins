// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea.actions;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.commands.NamedUserCommand;
import jetbrains.communicator.core.commands.UserCommand;
import jetbrains.communicator.idea.IDEtalkContainerRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.picocontainer.MutablePicoContainer;

import java.awt.*;

/**
 * @author Kir Maximov
 */
public class BaseAction<T extends UserCommand> extends AnAction {
  private static final Logger LOG = Logger.getInstance(BaseAction.class);
  private final Class<? extends T> myCommandClass;

  public BaseAction(Class<? extends T> commandClass) {
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
    return (e.getProject());
  }

  @Nullable
  public static MutablePicoContainer getContainer(Component c) {
    Project project = CommonDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext(c));
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

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    T t = getCommand(e);
    if (t != null) {
      t.execute();
    }
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    UserCommand command = getCommand(e);
    e.getPresentation().setEnabled(command != null && command.isEnabled());
    if (command instanceof NamedUserCommand) {
      NamedUserCommand userCommand = (NamedUserCommand) command;
      e.getPresentation().setText(userCommand.getName(), true);
      e.getPresentation().setIcon(userCommand.getIcon());
    }
  }

}
