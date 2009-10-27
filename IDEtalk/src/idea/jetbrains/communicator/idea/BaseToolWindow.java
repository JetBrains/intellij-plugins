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
package jetbrains.communicator.idea;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
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
  public static final Icon WORKER_ICON = IconLoader.getIcon("/ideTalk/user.png");

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
      public void addNotify() {
        super.addNotify();
        myWindow = SwingUtilities.getWindowAncestor(this);
      }
    };
  }

  protected abstract String getToolWindowId();
  protected abstract void createToolWindowComponent();
  protected abstract ToolWindowAnchor getAnchor();


  public void projectOpened() {
    myPanel.setOpaque(true);
    myPanel.setBackground(Color.white);

    createToolWindowComponent();
    myToolWindow = myToolWindowManager.registerToolWindow(getToolWindowId(), myPanel, getAnchor(), myProject, true);
    myToolWindow.setIcon(WORKER_ICON);
  }

  public void projectClosed() {
  }

  public void disposeComponent() {
  }

  public void expandToolWindow() {
    final Semaphore semaphore = new Semaphore(1);
    semaphore.tryAcquire();
    UIUtil.invokeLater(new Runnable() {
      public void run() {
        ToolWindow window = myToolWindowManager.getToolWindow(getToolWindowId());
        if (window != null) {
          window.show(new Runnable() {
            public void run() {
              semaphore.release();
            }
          });
        }
        else {
          semaphore.release();
        }
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
