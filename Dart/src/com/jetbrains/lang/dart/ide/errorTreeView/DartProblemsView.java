// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.impl.ProjectViewPane;
import com.intellij.notification.*;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsContexts.TabTitle;
import com.intellij.openapi.util.text.HtmlBuilder;
import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.util.Alarm;
import com.intellij.util.ModalityUiUtil;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.concurrency.ThreadingAssertions;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerMessages;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import icons.DartIcons;
import org.dartlang.analysis.server.protocol.AnalysisError;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(Service.Level.PROJECT)
@State(
  name = "DartProblemsView",
  storages = @Storage(StoragePathMacros.WORKSPACE_FILE)
)
public final class DartProblemsView implements PersistentStateComponent<DartProblemsViewSettings>, Disposable {
  public static final @NonNls String TOOLWINDOW_ID = "Dart Analysis"; // the same as in plugin.xml, this is not a user-visible string

  private static final int TABLE_REFRESH_PERIOD = 300;

  private final Project myProject;
  private final DartProblemsPresentationHelper myPresentationHelper;

  private final Object myLock = new Object(); // use this lock to access myScheduledFilePathToErrors and myAlarm
  private final Map<String, List<? extends AnalysisError>> myScheduledFilePathToErrors = new HashMap<>();
  private final Alarm myAlarm;

  private @NotNull Icon myCurrentIcon = DartIcons.Dart_13;
  private boolean myAnalysisIsBusy;

  private int myFilesWithErrorsHash;
  private Notification myNotification;
  private boolean myDisabledForSession;

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

      final Map<String, List<? extends AnalysisError>> filePathToErrors;
      synchronized (myLock) {
        filePathToErrors = new HashMap<>(myScheduledFilePathToErrors);
        myScheduledFilePathToErrors.clear();
      }

      DartProblemsViewPanel panel = getProblemsViewPanel();
      if (panel == null) {
        return;
      }

      ReadAction.nonBlocking(() -> {
          DartProblemsViewSettings.ScopedAnalysisMode scopedAnalysisMode =
            getInstance(myProject).myPresentationHelper.getScopedAnalysisMode();
          Map<String, List<DartProblem>> filePathToDartProblems = new HashMap<>();

          for (Map.Entry<String, List<? extends AnalysisError>> entry : filePathToErrors.entrySet()) {
            String filePath = entry.getKey();
            List<? extends AnalysisError> analysisErrors = entry.getValue();

            VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(filePath);
            boolean fileOk = vFile != null && (scopedAnalysisMode != DartProblemsViewSettings.ScopedAnalysisMode.All ||
                                               ProjectFileIndex.getInstance(myProject).isInContent(vFile));
            List<DartProblem> dartProblems = fileOk
                                             ? ContainerUtil.map(analysisErrors, analysisError -> new DartProblem(myProject, analysisError))
                                             : Collections.emptyList();
            filePathToDartProblems.put(filePath, dartProblems);
          }

          return filePathToDartProblems;
        })
        .expireWith(getInstance(myProject))
        .finishOnUiThread(ModalityState.nonModal(), filePathToDartProblems -> {
          panel.setErrors(filePathToDartProblems);
        })
        .submit(AppExecutorUtil.getAppExecutorService());
    }
  };

  public DartProblemsView(@NotNull Project project) {
    myProject = project;
    myPresentationHelper = new DartProblemsPresentationHelper(project);
    myAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, this);

    project.getMessageBus().connect().subscribe(
      DartAnalysisServerMessages.DART_ANALYSIS_TOPIC, new DartAnalysisServerMessages.DartAnalysisNotifier() {
        @Override
        public void analysisStarted() {
          myAnalysisIsBusy = true;
          ModalityUiUtil.invokeLaterIfNeeded(ModalityState.nonModal(), myProject.getDisposed(), () -> updateIcon());
        }

        @Override
        public void analysisFinished() {
          myAnalysisIsBusy = false;
          ModalityUiUtil.invokeLaterIfNeeded(ModalityState.nonModal(), myProject.getDisposed(), () -> updateIcon());
        }
      }
    );
  }

  DartProblemsPresentationHelper getPresentationHelper() {

    return myPresentationHelper;
  }

  private @Nullable ToolWindow getDartAnalysisToolWindow() {
    return ToolWindowManager.getInstance(myProject).getToolWindow(TOOLWINDOW_ID);
  }

  private @Nullable DartProblemsViewPanel getProblemsViewPanel() {
    ToolWindow toolWindow = getDartAnalysisToolWindow();
    Content content = toolWindow != null ? toolWindow.getContentManager().getContent(0) : null;
    return content != null ? (DartProblemsViewPanel)content.getComponent() : null;
  }

  void setTabTitle(@TabTitle @NotNull String tabTitle) {
    ToolWindow toolWindow = getDartAnalysisToolWindow();
    Content content = toolWindow != null ? toolWindow.getContentManager().getContent(0) : null;
    if (content != null) {
      content.setDisplayName(tabTitle);
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

  public static @NotNull DartProblemsView getInstance(final @NotNull Project project) {
    return project.getService(DartProblemsView.class);
  }

  public static DartProblemsViewSettings.ScopedAnalysisMode getScopeAnalysisMode(final @NotNull Project project) {
    if (!DartAnalysisServerService.getInstance(project).isServerProcessActive()) {
      return DartProblemsViewSettings.SCOPED_ANALYSIS_MODE_DEFAULT;
    }
    return getInstance(project).myPresentationHelper.getScopedAnalysisMode();
  }

  public VirtualFile getCurrentFile() {
    return myPresentationHelper.getCurrentFile();
  }

  @SuppressWarnings("unused")
  public void showWarningNotification(@NotNull @NlsContexts.NotificationTitle String title,
                                      @Nullable @NlsContexts.NotificationContent String content,
                                      @Nullable Icon icon) {
    showNotification(NotificationType.WARNING, title, content, icon, false);
  }

  public void showErrorNotificationTerse(@NotNull @NlsContexts.NotificationTitle String title) {
    showNotification(NotificationType.ERROR, title, null, null, true);
  }

  public void showErrorNotification(@NotNull @NlsContexts.NotificationTitle String title,
                                    @Nullable @NlsContexts.NotificationContent String content,
                                    @Nullable Icon icon) {
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
                                @NotNull @NlsContexts.NotificationTitle String title,
                                @Nullable @NlsContexts.NotificationContent String content,
                                @Nullable Icon icon,
                                boolean terse) {
    clearNotifications();

    if (myDisabledForSession) return;

    content = StringUtil.notNullize(content);
    if (!terse) {
      if (!content.endsWith("<br>")) content += "<br>";
      content +=
        new HtmlBuilder().br()
          .appendLink("disable.for.session", DartBundle.message("notification.link.don.t.show.for.this.session"))
          .append(HtmlChunk.nbsp(7))
          .appendLink("never.show.again", DartBundle.message("notification.link.never.show.again"));
    }

    myNotification = NotificationGroupManager.getInstance().getNotificationGroup("Dart Analysis")
      .createNotification(title, content, notificationType).setListener(new NotificationListener.Adapter() {
      @Override
      protected void hyperlinkActivated(final @NotNull Notification notification, final @NotNull HyperlinkEvent e) {
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
          NotificationGroupManager.getInstance().getNotificationGroup("Dart Analysis")
            .createNotification(DartBundle.message("notification.title.warning.disabled"),
                                DartBundle.message("notification.content.you.can.enable.it.back.in.the.a.href.event.log.a.settings",
                                                   ActionCenter.getToolwindowName()),
                                NotificationType.INFORMATION).setListener(new Adapter() {
                @Override
                protected void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent e) {
                  notification.expire();
                  final ToolWindow toolWindow = ActionCenter.getToolWindow(myProject);
                  if (toolWindow != null) toolWindow.activate(null);
                }
              }).notify(myProject);

          NotificationsConfiguration.getNotificationsConfiguration().setDisplayType(notification.getGroupId(), NotificationDisplayType.NONE);
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
  public void setInitialCurrentFileBeforeServerStart(final @Nullable VirtualFile file) {
    myPresentationHelper.setCurrentFile(file);
  }

  public void setCurrentFile(final @Nullable VirtualFile file) {
    ThreadingAssertions.assertEventDispatchThread();

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

  public void updateErrorsForFile(final @NotNull String filePath, @NotNull List<? extends AnalysisError> errors) {
    synchronized (myLock) {
      if (myScheduledFilePathToErrors.isEmpty()) {
        myAlarm.addRequest(myUpdateRunnable, TABLE_REFRESH_PERIOD, ModalityState.nonModal());
      }

      myScheduledFilePathToErrors.put(filePath, errors);
    }
  }

  public void clearAll() {
    ThreadingAssertions.assertEventDispatchThread();

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

  @Override
  public void dispose() {}
}
