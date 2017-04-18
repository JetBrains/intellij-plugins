/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.impl.ProjectViewPane;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.Alarm;
import com.intellij.util.ui.UIUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerMessages;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import gnu.trove.THashMap;
import icons.DartIcons;
import org.dartlang.analysis.server.protocol.AnalysisError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@State(
  name = "DartProblemsView",
  storages = @Storage(StoragePathMacros.WORKSPACE_FILE)
)
public class DartProblemsView implements PersistentStateComponent<DartProblemsViewSettings> {
  public static final String TOOLWINDOW_ID = DartBundle.message("dart.analysis.tool.window");

  private static final NotificationGroup NOTIFICATION_GROUP =
    NotificationGroup.toolWindowGroup(TOOLWINDOW_ID, TOOLWINDOW_ID, false);

  private static final int TABLE_REFRESH_PERIOD = 300;

  private final Project myProject;
  private final DartProblemsFilter myFilter;
  private DartProblemsViewPanel myPanel;

  private final Object myLock = new Object(); // use this lock to access myScheduledFilePathToErrors and myAlarm
  private final Map<String, List<AnalysisError>> myScheduledFilePathToErrors = new THashMap<>();
  private final Alarm myAlarm;
  private DartProblemsViewSettings mySettings = new DartProblemsViewSettings();

  private ToolWindow myToolWindow;
  private Icon myCurrentIcon;
  private boolean myAnalysisIsBusy;

  private int myFilesWithErrorsHash;
  private Notification myNotification;

  private final Runnable myUpdateRunnable = new Runnable() {
    @Override
    public void run() {
      if (ProjectViewPane.ID.equals(ProjectView.getInstance(myProject).getCurrentViewId())) {
        final int hash = DartAnalysisServerService.getInstance(myProject).getFilePathsWithErrorsHash();
        if (myFilesWithErrorsHash != hash) {
          // refresh red squiggles managed by com.jetbrains.lang.dart.projectView.DartNodeDecorator
          myFilesWithErrorsHash = hash;
          ProjectView.getInstance(myProject).refresh();
        }
      }

      final Map<String, List<AnalysisError>> filePathToErrors;
      synchronized (myLock) {
        filePathToErrors = new THashMap<>(myScheduledFilePathToErrors);
        myScheduledFilePathToErrors.clear();
      }

      myPanel.setErrors(filePathToErrors);
    }
  };

  public DartProblemsView(@NotNull final Project project, @NotNull final ToolWindowManager toolWindowManager) {
    myProject = project;
    myFilter = new DartProblemsFilter(project);
    myAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, project);
    Disposer.register(project, myAlarm);

    UIUtil.invokeLaterIfNeeded(() -> {
      if (project.isDisposed()) {
        return;
      }

      myPanel = new DartProblemsViewPanel(project, myFilter, mySettings);

      myToolWindow = toolWindowManager.registerToolWindow(TOOLWINDOW_ID, false, ToolWindowAnchor.BOTTOM, project, true);
      myCurrentIcon = DartIcons.Dart_13;
      updateIcon();

      final Content content = ContentFactory.SERVICE.getInstance().createContent(myPanel, "", false);
      myToolWindow.getContentManager().addContent(content);

      ToolWindowEx toolWindowEx = (ToolWindowEx)myToolWindow;
      toolWindowEx.setTitleActions(new AnalysisServerStatusAction());
      ArrayList<AnAction> gearActions = new ArrayList<>();
      gearActions.add(new AnalysisServerDiagnosticsAction());
      toolWindowEx.setAdditionalGearActions(new DefaultActionGroup(gearActions));

      myPanel.setToolWindowUpdater(new ToolWindowUpdater() {
        @Override
        public void setIcon(@NotNull Icon icon) {
          myCurrentIcon = icon;
          updateIcon();
        }

        @Override
        public void setHeaderText(@NotNull String headerText) {
          content.setDisplayName(headerText);
        }
      });

      if (PropertiesComponent.getInstance(project).getBoolean("dart.analysis.tool.window.force.activate", true)) {
        PropertiesComponent.getInstance(project).setValue("dart.analysis.tool.window.force.activate", false, true);
        myToolWindow.activate(null, false);
      }

      Disposer.register(project, () -> myToolWindow.getContentManager().removeAllContents(true));
    });

    project.getMessageBus().connect().subscribe(
      DartAnalysisServerMessages.DART_ANALYSIS_TOPIC, new DartAnalysisServerMessages.DartAnalysisNotifier() {
        @Override
        public void analysisStarted() {
          myAnalysisIsBusy = true;
          UIUtil.invokeLaterIfNeeded(() -> updateIcon());
        }

        @Override
        public void analysisFinished() {
          myAnalysisIsBusy = false;
          UIUtil.invokeLaterIfNeeded(() -> updateIcon());
        }
      }
    );
  }

  void updateIcon() {
    if (myAnalysisIsBusy) {
      myToolWindow.setIcon(ExecutionUtil.getLiveIndicator(myCurrentIcon));
    }
    else {
      myToolWindow.setIcon(myCurrentIcon);
    }
  }

  public static DartProblemsView getInstance(@NotNull final Project project) {
    return ServiceManager.getService(project, DartProblemsView.class);
  }

  @SuppressWarnings("unused")
  public void showWarningNotification(@NotNull Project project, @NotNull String title, @NotNull String htmlContent, @Nullable Icon icon) {
    showNotification(NotificationType.WARNING, project, title, htmlContent, icon);
  }

  public void showErrorNotification(@NotNull Project project, @NotNull String title, @NotNull String htmlContent, @Nullable Icon icon) {
    showNotification(NotificationType.ERROR, project, title, htmlContent, icon);
  }

  public void clearNotifications() {
    if (myNotification != null) {
      myNotification.expire();
      myNotification = null;
    }
  }

  private void showNotification(@NotNull NotificationType notificationType,
                                @NotNull Project project,
                                @NotNull String title,
                                @NotNull String htmlContent,
                                @Nullable Icon icon) {
    clearNotifications();

    myNotification = NOTIFICATION_GROUP.createNotification(
      title, htmlContent, notificationType, new NotificationListener.Adapter() {
        @Override
        protected void hyperlinkActivated(@NotNull final Notification notification, @NotNull final HyperlinkEvent e) {
          notification.expire();
          ToolWindowManager.getInstance(project).getToolWindow(TOOLWINDOW_ID).activate(null);
        }
      }
    );
    if (icon != null) {
      myNotification.setIcon(icon);
    }
    myNotification.notify(project);
  }

  @Override
  public DartProblemsViewSettings getState() {
    return mySettings;
  }

  @Override
  public void loadState(DartProblemsViewSettings state) {
    mySettings = state;

    // Update children.
    if (myPanel != null) {
      myPanel.updateFromSettings(mySettings);
    }
  }

  public void setCurrentFile(@Nullable final VirtualFile file) {
    if (myFilter.setCurrentFile(file) && myFilter.getFileFilterMode() != DartProblemsFilter.FileFilterMode.All) {
      if (myPanel != null) {
        myPanel.fireGroupingOrFilterChanged();
      }
    }
  }

  public void updateErrorsForFile(@NotNull final String filePath, @NotNull final List<AnalysisError> errors) {
    synchronized (myLock) {
      if (myScheduledFilePathToErrors.isEmpty()) {
        myAlarm.addRequest(myUpdateRunnable, TABLE_REFRESH_PERIOD, ModalityState.NON_MODAL);
      }

      myScheduledFilePathToErrors.put(filePath, errors);
    }
  }

  public void clearAll() {
    ApplicationManager.getApplication().assertIsDispatchThread();

    ProjectView.getInstance(myProject).refresh(); // refresh red waves managed by com.jetbrains.lang.dart.projectView.DartNodeDecorator

    synchronized (myLock) {
      myAlarm.cancelAllRequests();
      myScheduledFilePathToErrors.clear();
    }

    myPanel.clearAll();
  }

  interface ToolWindowUpdater {
    void setIcon(@NotNull final Icon icon);

    void setHeaderText(@NotNull final String headerText);
  }
}
