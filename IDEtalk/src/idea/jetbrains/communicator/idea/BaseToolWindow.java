/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package jetbrains.communicator.idea;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import icons.IdeTalkCoreIcons;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.dispatcher.LocalMessageDispatcher;
import jetbrains.communicator.util.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author Kir
 */
public abstract class BaseToolWindow implements ProjectComponent {
  protected final ToolWindowManager myToolWindowManager;
  protected final ActionManager myActionManager;
  protected final Project myProject;

  protected JComponent myPanel;
  protected ToolWindow myToolWindow;
  protected Window myWindow;

  protected BaseToolWindow(ToolWindowManager toolWindowManager, ActionManager actionManager, Project project) {
    myProject = project;
    myToolWindowManager = toolWindowManager;
    myActionManager = actionManager;

    myPanel = new JPanel(new BorderLayout()) {
      @Override
      public void addNotify() {
        super.addNotify();
        myWindow = SwingUtilities.getWindowAncestor(this);
      }
    };
  }

  protected abstract String getToolWindowId();
  protected abstract void createToolWindowComponent();
  protected abstract ToolWindowAnchor getAnchor();

  @Override
  public void projectOpened() {
    myPanel.setOpaque(true);
    myPanel.setBackground(com.intellij.util.ui.UIUtil.getListBackground());

    createToolWindowComponent();
    myToolWindow = myToolWindowManager.registerToolWindow(getToolWindowId(), myPanel, getAnchor(), myProject, true);
    myToolWindow.setHelpId("reference.toolWindows.idetalk");
    myToolWindow.setIcon(IdeTalkCoreIcons.IdeTalk.User_toolwindow);
  }

  public void expandToolWindow() {
    final Semaphore semaphore = new Semaphore(1);
    semaphore.tryAcquire();
    UIUtil.invokeLater(() -> {
      ToolWindow window = myToolWindowManager.getToolWindow(getToolWindowId());
      if (window != null) {
        window.show(() -> semaphore.release());
      }
      else {
        semaphore.release();
      }
    });
    waitForOpen(semaphore);
  }

  private static void waitForOpen(Semaphore semaphore) {
    if (!EventQueue.isDispatchThread()) {
      try {
        semaphore.tryAcquire(1, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        //
      }
    }
  }

  public static LocalMessageDispatcher getLocalDispatcher() {
    return (LocalMessageDispatcher) Pico.getInstance().getComponentInstanceOfType(LocalMessageDispatcher.class);
  }
}
