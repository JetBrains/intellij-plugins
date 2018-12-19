// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.impl.ProjectViewPane;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.*;
import com.intellij.notification.impl.NotificationSettings;
import com.intellij.notification.impl.NotificationsConfigurationImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.text.StringUtil;
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
  private final DartProblemsPresentationHelper myPresentationHelper;
  private DartProblemsViewPanel myPanel;

  private final Object myLock = new Object(); // use this lock to access myScheduledFilePathToErrors and myAlarm
  private final Map<String, List<AnalysisError>> myScheduledFilePathToErrors = new THashMap<>();
  private final Alarm myAlarm;

  private ToolWindow myToolWindow;
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

      myPanel.setErrors(filePathToErrors);
    }
  };

  public DartProblemsView(@NotNull final Project project, @NotNull final ToolWindowManager toolWindowManager) {
    myProject = project;
    myPresentationHelper = new DartProblemsPresentationHelper(project);
    myAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, project);
    Disposer.register(project, myAlarm);

    UIUtil.invokeLaterIfNeeded(() -> {
      if (project.isDisposed()) {
        return;
      }

      myPanel = new DartProblemsViewPanel(project, myPresentationHelper);

      myToolWindow = toolWindowManager.registerToolWindow(TOOLWINDOW_ID, false, ToolWindowAnchor.BOTTOM, project, true);
      myToolWindow.setHelpId("reference.toolWindow.DartAnalysis");
      myCurrentIcon = DartIcons.Dart_13;
      updateIcon();

      final Content content = ContentFactory.SERVICE.getInstance().createContent(myPanel, "", false);
      myToolWindow.getContentManager().addContent(content);

      ToolWindowEx toolWindowEx = (ToolWindowEx)myToolWindow;
      toolWindowEx.setTitleActions(new AnalysisServerFeedbackAction());

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

  public DartProblemsViewSettings.ScopedAnalysisMode getScopeAnalysisMode() {
    return myPresentationHelper.getScopedAnalysisMode();
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
          ToolWindowManager.getInstance(myProject).getToolWindow(TOOLWINDOW_ID).activate(null);
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
    if (myPanel != null) {
      myPanel.fireGroupingOrFilterChanged();
    }
  }

  /**
   * Unlike {@link #setCurrentFile(VirtualFile)} this method may be called not from the EDT
   */
  public void setInitialCurrentFileBeforeServerStart(@Nullable final VirtualFile file) {
    myPresentationHelper.setCurrentFile(file);
  }

  public void setCurrentFile(@Nullable final VirtualFile file) {
    ApplicationManager.getApplication().assertIsDispatchThread();

    if (myPresentationHelper.setCurrentFile(file) &&
        myPresentationHelper.getFileFilterMode() != DartProblemsViewSettings.FileFilterMode.All) {
      if (myPanel != null) {
        myPanel.fireGroupingOrFilterChanged();
      }
    }

    DartAnalysisServerService.getInstance(myProject).ensureAnalysisRootsUpToDate();
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

    if (myPanel != null) {
      myPanel.clearAll();
    }
  }

  interface ToolWindowUpdater {
    void setIcon(@NotNull final Icon icon);

    void setHeaderText(@NotNull final String headerText);
  }
}
