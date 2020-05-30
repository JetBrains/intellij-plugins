// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.analyzer;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.notification.*;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.HyperlinkEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * A class to log Dart analysis server errors to the IntelliJ event log, and present additional details
 * to users on demand.
 */
public class DartAnalysisServerErrorHandler {
  private static final Logger LOG = Logger.getInstance(DartAnalysisServerErrorHandler.class);

  private static final int MAX_REPORTS_PER_SESSION = 100;

  // NOTIFICATION_GROUP is used to add an error to Event Log tool window.
  private static final NotificationGroup NOTIFICATION_GROUP =
    new NotificationGroup("Dart analysis issue", NotificationDisplayType.NONE, true);

  @NotNull private final Project myProject;
  private int issueCount = 0;

  DartAnalysisServerErrorHandler(@NotNull Project project) {
    myProject = project;
  }

  public void handleError(@NotNull String message,
                          @Nullable String stackTrace,
                          boolean isFatal,
                          @Nullable String sdkVersion,
                          @Nullable String debugLog) {
    if (issueCount > MAX_REPORTS_PER_SESSION) {
      return;
    }

    final int issueNumber = ++issueCount;

    String messageOneLine = StringUtil.splitByLines(message)[0];

    NotificationListener listener = new NotificationListener.Adapter() {
      @Override
      protected void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent e) {
        String content = calculateIssueText(messageOneLine, sdkVersion, message, stackTrace, debugLog);

        try {
          // 'issue-1.md'
          File ioFile = FileUtil.createTempFile("issue-" + issueNumber, ".md", true);
          FileUtil.createParentDirs(ioFile);
          FileUtil.writeToFile(ioFile, content);

          VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(ioFile);
          if (file != null) {
            FileEditorManager.getInstance(myProject).openFile(file, true);
          }
        }
        catch (IOException ioe) {
          LOG.warn("Error creating issue file", ioe);
        }
      }
    };

    String content = messageOneLine + " (<a href=\"\">view details</a>)";

    Notification notification = NOTIFICATION_GROUP
      .createNotification(NOTIFICATION_GROUP.getDisplayId(), content, isFatal ? NotificationType.ERROR : NotificationType.WARNING,
                          listener);
    // This writes to Event Log tool window but doesn't show a balloon.
    notification.notify(myProject);
  }

  private static String calculateIssueText(@NotNull String messageOneLine,
                                           @Nullable String sdkVersion,
                                           @NotNull String message,
                                           @Nullable String stackTrace,
                                           @Nullable String debugLog) {
    StringWriter writer = new StringWriter();
    //noinspection IOResourceOpenedButNotSafelyClosed
    PrintWriter text = new PrintWriter(writer);

    // message
    text.println("## Dart analysis issue");
    text.println("");
    text.println(messageOneLine);
    text.println("");

    // version info
    final ApplicationInfo platform = ApplicationInfo.getInstance();
    final IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(PluginId.findId("Dart"));

    text.println("## Version information");
    text.println("");
    if (sdkVersion != null) {
      text.println("- Dart SDK " + sdkVersion);
    }
    if (plugin != null) {
      text.println("- Dart IntelliJ " + plugin.getVersion());
    }
    text.println("- " + platform.getVersionName() + " " + platform.getFullVersion());
    text.println("- " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
    text.println("");

    // stacktrace
    text.println("## Details");
    text.println("");
    text.println(message.trim());
    if (stackTrace != null) {
      text.println("");
      text.println("```");
      text.println(stackTrace.trim());
      text.println("```");
    }
    text.println("");

    // debug log
    if (debugLog != null) {
      text.println("## Debug log");
      text.println("");
      text.println("```");
      text.println(debugLog.trim());
      text.println("```");
    }

    return writer.toString();
  }
}