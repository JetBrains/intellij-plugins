// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.impl.ProjectViewPane;
import com.intellij.notification.*;
import com.intellij.notification.impl.NotificationSettings;
import com.intellij.notification.impl.NotificationsConfigurationImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.GuiUtils;
import com.intellij.ui.content.Content;
import com.intellij.util.Alarm;
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
import java.util.List;
import java.util.Map;

@State(
  name = "DartProblemsView",
  storages = @Storage(StoragePathMacros.WORKSPACE_FILE)
)
public class DartProblemsView implements PersistentStateComponent<DartProblemsViewSettings> {
  /**
   * @deprecated Use {@link #getToolwindowId()} instead
   */
  @Deprecated
  public static final String TOOLWINDOW_ID = getToolwindowId();

  private static final NotificationGroup NOTIFICATION_GROUP =
    NotificationGroup.toolWindowGroup(getToolwindowId(), getToolwindowId(), false);

  private static final int TABLE_REFRESH_PERIOD = 300;

  private final Project myProject;
  private final DartProblemsPresentationHelper myPresentationHelper;

  private final Object myLock = new Object(); // use this lock to access myScheduledFilePathToErrors and myAlarm
  private final Map<String, List<AnalysisError>> myScheduledFilePathToErrors = new THashMap<>();
  private final Alarm myAlarm;

  private Icon myCurrentIcon;
  private boolean myAnalysisIsBusy;

  private int myFilesWithErrorsHash;
  private Notification myNotification;
  private boolean myDisabledForSession = false;

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

      DartProblemsViewPanel panel = getProblemsViewPanel();
      if (panel != null) {
        panel.setErrors(filePathToErrors);
      }
    }
  };

  public DartProblemsView(@NotNull Project project) {
    myProject = project;
    myPresentationHelper = new DartProblemsPresentationHelper(project);
    myAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, project);
    myCurrentIcon = DartIcons.Dart_13;

    project.getMessageBus().connect().subscribe(
      DartAnalysisServerMessages.DART_ANALYSIS_TOPIC, new DartAnalysisServerMessages.DartAnalysisNotifier() {
        @Override
        public void analysisStarted() {
          myAnalysisIsBusy = true;
          GuiUtils.invokeLaterIfNeeded(() -> updateIcon(), ModalityState.NON_MODAL, myProject.getDisposed());
        }

        @Override
        public void analysisFinished() {
          myAnalysisIsBusy = false;
          GuiUtils.invokeLaterIfNeeded(() -> updateIcon(), ModalityState.NON_MODAL, myProject.getDisposed());
        }
      }
    );
  }

  DartProblemsPresentationHelper getPresentationHelper() {
    return myPresentationHelper;
  }

  @Nullable
  private ToolWindow getDartAnalysisToolWindow() {
    return ToolWindowManager.getInstance(myProject).getToolWindow(getToolwindowId());
  }

  @Nullable
  private DartProblemsViewPanel getProblemsViewPanel() {
    ToolWindow toolWindow = getDartAnalysisToolWindow();
    Content content = toolWindow != null ? toolWindow.getContentManager().getContent(0) : null;
    return content != null ? (DartProblemsViewPanel)content.getComponent() : null;
  }

  void setHeaderText(@NotNull String headerText) {
    ToolWindow toolWindow = getDartAnalysisToolWindow();
    Content content = toolWindow != null ? toolWindow.getContentManager().getContent(0) : null;
    if (content != null) {
      content.setDisplayName(headerText);
    }
  }

  void setToolWindowIcon(@NotNull Icon icon) {
    myCurrentIcon = icon;
    updateIcon();
  }

  private void updateIcon() {
    ToolWindow toolWindow = getDartAnalysisToolWindow();
    if (toolWindow == null) return;

    if (myAnalysisIsBusy) {
      toolWindow.setIcon(ExecutionUtil.getLiveIndicator(myCurrentIcon));
    }
    else {
      toolWindow.setIcon(myCurrentIcon);
    }
  }

  public static DartProblemsView getInstance(@NotNull final Project project) {
    return ServiceManager.getService(project, DartProblemsView.class);
  }

  public static DartProblemsViewSettings.ScopedAnalysisMode getScopeAnalysisMode(@NotNull final Project project) {
    if (!DartAnalysisServerService.getInstance(project).isServerProcessActive()) {
      return DartProblemsViewSettings.SCOPED_ANALYSIS_MODE_DEFAULT;
    }
    return getInstance(project).myPresentationHelper.getScopedAnalysisMode();
  }

  public VirtualFile getCurrentFile() {
    return myPresentationHelper.getCurrentFile();
  }

  @SuppressWarnings("unused")
  public void showWarningNotification(@NotNull String title, @Nullable String content, @Nullable Icon icon) {
    showNotification(NotificationType.WARNING, title, content, icon, false);
  }

  public void showErrorNotificationTerse(@NotNull String title) {
    showNotification(NotificationType.ERROR, title, null, null, true);
  }

  public void showErrorNotification(@NotNull String title, @Nullable String content, @Nullable Icon icon) {
    showNotification(NotificationType.ERROR, title, content, icon, false);
  }

  public void clearNotifications() {
    if (myNotification != null) {
      myNotification.expire();
      myNotification = null;
    }
  }

  public static final String OPEN_DART_ANALYSIS_LINK = "open.dart.analysis";

  private void showNotification(@NotNull NotificationType notificationType,
                                @NotNull String title,
                                @Nullable String content,
                                @Nullable Icon icon,
                                boolean terse) {
    clearNotifications();

    if (myDisabledForSession) return;

    content = StringUtil.notNullize(content);
    if (!terse) {
      if (!content.endsWith("<br>")) content += "<br>";
      content += "<br><a href='disable.for.session'>Don't show for this session</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                 "<a href='never.show.again'>Never show again</a>";
    }

    myNotification = NOTIFICATION_GROUP.createNotification(title, content, notificationType, new NotificationListener.Adapter() {
      @Override
      protected void hyperlinkActivated(@NotNull final Notification notification, @NotNull final HyperlinkEvent e) {
        notification.expire();

        if (OPEN_DART_ANALYSIS_LINK.equals(e.getDescription())) {
          ToolWindow toolWindow = getDartAnalysisToolWindow();
          if (toolWindow != null) {
            toolWindow.activate(null);
          }
        }
        else if ("disable.for.session".equals(e.getDescription())) {
          myDisabledForSession = true;
        }
        else if ("never.show.again".equals(e.getDescription())) {
          NOTIFICATION_GROUP.createNotification("Warning disabled.",
                                                "You can enable it back in the <a href=''>Event Log</a> settings.",
                                                NotificationType.INFORMATION, new Adapter() {
              @Override
              protected void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent e) {
                notification.expire();
                final ToolWindow toolWindow = EventLog.getEventLog(myProject);
                if (toolWindow != null) toolWindow.activate(null);
              }
            }).notify(myProject);

          final NotificationSettings oldSettings = NotificationsConfigurationImpl.getSettings(notification.getGroupId());
          NotificationsConfigurationImpl.getInstanceImpl().changeSettings(oldSettings.getGroupId(), NotificationDisplayType.NONE,
                                                                          oldSettings.isShouldLog(), oldSettings.isShouldReadAloud());
        }
      }
    });

    if (icon != null) {
      myNotification.setIcon(icon);
    }

    myNotification.notify(myProject);
  }

  @Override
  public DartProblemsViewSettings getState() {
    return myPresentationHelper.getSettings();
  }

  @Override
  public void loadState(@NotNull DartProblemsViewSettings state) {
    myPresentationHelper.setSettings(state);
  }

  /**
   * Unlike {@link #setCurrentFile(VirtualFile)} this method may be called not from the EDT
   */
  public void setInitialCurrentFileBeforeServerStart(@Nullable final VirtualFile file) {
    myPresentationHelper.setCurrentFile(file);
  }

  public void setCurrentFile(@Nullable final VirtualFile file) {
    ApplicationManager.getApplication().assertIsDispatchThread();

    // Calling getProblemsViewPanel() here also ensures that the tool window contents becomes visible when Analysis server starts
    DartProblemsViewPanel panel = getProblemsViewPanel();
    if (panel != null &&
        myPresentationHelper.setCurrentFile(file) &&
        myPresentationHelper.getFileFilterMode() != DartProblemsViewSettings.FileFilterMode.All) {
      panel.fireGroupingOrFilterChanged();
    }

    if (myPresentationHelper.getScopedAnalysisMode() == DartProblemsViewSettings.ScopedAnalysisMode.DartPackage) {
      DartAnalysisServerService.getInstance(myProject).ensureAnalysisRootsUpToDate();
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

    DartProblemsViewPanel panel = getProblemsViewPanel();
    if (panel != null) {
      panel.clearAll();
    }
  }

  public static String getToolwindowId() {
    return DartBundle.message("dart.analysis.tool.window");
  }
}
