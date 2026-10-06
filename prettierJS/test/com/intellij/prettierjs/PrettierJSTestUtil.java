// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.CommonDataKeys;
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
   * Updates the action on a background thread under a read lock.
   * If the update makes the action enabled and visible, the method performs the action on the EDT.
   * The action system uses the same threads for an action with {@link ActionUpdateThread#BGT}.
   * {@link CodeInsightTestFixture#performEditorAction} updates the action on the EDT instead.
   * The method then waits until all non-blocking read actions complete.
   *
   * @return the presentation after the update
   */
  @RequiresEdt
  public static @NotNull Presentation updateAndPerformAction(@NotNull String actionId,
                                                             @NotNull Project project,
                                                             @NotNull VirtualFile file) {
    var action = ActionManager.getInstance().getAction(actionId);
    var dataContext = SimpleDataContext.builder()
      .add(CommonDataKeys.PROJECT, project)
      .add(CommonDataKeys.VIRTUAL_FILE, file)
      .build();
    var event = TestActionEvent.createTestEvent(action, dataContext);
    PlatformTestUtil.waitForFuture(ApplicationManager.getApplication().executeOnPooledThread(
      () -> ReadAction.runBlocking(() -> ActionUtil.updateAction(action, event))));
    if (event.getPresentation().isEnabledAndVisible()) {
      ActionUtil.performAction(action, event);
      NonBlockingReadActionImpl.waitForAsyncTaskCompletion();
    }
    return event.getPresentation();
  }

  private static String getContribPath() {
    final String homePath = IdeaTestExecutionPolicy.getHomePathWithPolicy();
    if (new File(homePath, "contrib/.gitignore").isFile()) {
      return homePath + File.separatorChar + "contrib";
    }
    return homePath;
  }
}
