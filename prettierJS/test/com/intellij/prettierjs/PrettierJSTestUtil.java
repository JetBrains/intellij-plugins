// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.AnActionResult;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.impl.NonBlockingReadActionImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.TestActionEvent;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy;
import com.intellij.util.concurrency.annotations.RequiresEdt;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public final class PrettierJSTestUtil {

  private PrettierJSTestUtil() {
  }

  public static String getTestDataPath() {
    return getContribPath() + "/prettierJS/testData/";
  }

  /**
   * Updates the action with {@link #updateAction} and a context of {@link #fileDataContext}.
   * If the update makes the action enabled and visible, the method performs the action with {@link #performAction}.
   * The method then waits until all non-blocking read actions complete.
   * Do not use it for actions that must overlap, because the wait completes each action before the next one starts.
   *
   * @return the presentation after the update
   */
  @RequiresEdt
  public static @NotNull Presentation updateAndPerformAction(@NotNull String actionId,
                                                             @NotNull Project project,
                                                             @NotNull VirtualFile file) {
    var event = updateAction(actionId, fileDataContext(project, file));
    if (event.getPresentation().isEnabledAndVisible()) {
      performAction(actionId, event);
      NonBlockingReadActionImpl.waitForAsyncTaskCompletion();
    }
    return event.getPresentation();
  }

  /**
   * Updates the action on a background thread under a read lock.
   * The action system uses the same thread for an action with {@link ActionUpdateThread#BGT}.
   * {@link CodeInsightTestFixture#performEditorAction} updates the action on the EDT instead.
   *
   * @return the event of the update, for {@link #performAction}
   */
  @RequiresEdt
  public static @NotNull AnActionEvent updateAction(@NotNull String actionId, @NotNull DataContext dataContext) {
    var action = ActionManager.getInstance().getAction(actionId);
    var event = TestActionEvent.createTestEvent(action, dataContext);
    PlatformTestUtil.waitForFuture(ApplicationManager.getApplication().executeOnPooledThread(
      () -> ReadAction.runBlocking(() -> ActionUtil.updateAction(action, event))));
    return event;
  }

  /**
   * Performs the action on the EDT with the event of {@link #updateAction}.
   * The method does not wait for the asynchronous work that the action starts.
   */
  @RequiresEdt
  public static @NotNull AnActionResult performAction(@NotNull String actionId, @NotNull AnActionEvent event) {
    return ActionUtil.performAction(ActionManager.getInstance().getAction(actionId), event);
  }

  /**
   * Creates the context that an action gets for a file selected in the Project view.
   */
  public static @NotNull DataContext fileDataContext(@NotNull Project project, @NotNull VirtualFile file) {
    return SimpleDataContext.builder()
      .add(CommonDataKeys.PROJECT, project)
      .add(CommonDataKeys.VIRTUAL_FILE, file)
      .build();
  }

  private static String getContribPath() {
    final String homePath = IdeaTestExecutionPolicy.getHomePathWithPolicy();
    if (new File(homePath, "contrib/.gitignore").isFile()) {
      return homePath + File.separatorChar + "contrib";
    }
    return homePath;
  }
}
