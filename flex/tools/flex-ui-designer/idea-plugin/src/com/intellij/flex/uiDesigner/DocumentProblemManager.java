package com.intellij.flex.uiDesigner;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.impl.IdeFrameImpl;
import com.intellij.util.StringBuilderSpinAllocator;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class DocumentProblemManager {
  public static DocumentProblemManager getInstance() {
    return ServiceManager.getService(DocumentProblemManager.class);
  }

  private static StringBuilder appendTitle(StringBuilder builder) {
    return builder.append("<b>").append(FlexUIDesignerBundle.message("plugin.name")).append("</b>");
  }

  public void report(final Project project, String message) {
    StringBuilder builder = StringBuilderSpinAllocator.alloc();
    try {
      report(project, appendTitle(builder).append("<p>").append(message).append("</p>").toString(), MessageType.ERROR);
    }
    finally {
      StringBuilderSpinAllocator.dispose(builder);
    }
  }

  public void report(Project project, ProblemsHolder problems) {
    report(project, problems.getResultList());
  }

  public void report(Project project, String[] problems) {
    StringBuilder builder = StringBuilderSpinAllocator.alloc();
    try {
      appendTitle(builder).append("<ul>");
      for (String problem : problems) {
        builder.append("<li>").append(problem).append("</li>");
      }
      builder.append("</ul>");
      report(project, builder.toString(), MessageType.ERROR);
    }
    finally {
      StringBuilderSpinAllocator.dispose(builder);
    }
  }

  // Notification.notify is not suitable for us —
  // 1) it is not suitable for content with <ul> tags (due to <p> around message, see NotificationsUtil.buildHtml)
  // 2) it is buggy — balloon disappeared while user selects message text
  // 3) in any case, event log cannot show our message, may be due to <ul> tags?
  // todo fix platform Notification impl or how use it correctly?
  public void report(@Nullable final Project project, String message, MessageType messageType) {
    //Notification notification = new Notification(FlexUIDesignerBundle.message("plugin.name"),
    //  title == null ? FlexUIDesignerBundle.message("plugin.name") : title, message, NotificationType.ERROR);
    //notification.notify(project);

    final Balloon balloon = JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(message, messageType, null).setShowCallout(false)
      .setHideOnAction(false).createBalloon();
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        Window window = WindowManager.getInstance().getFrame(project);
        if (window == null) {
          window = JOptionPane.getRootFrame();
        }
        if (window instanceof IdeFrameImpl) {
          ((IdeFrameImpl)window).getBalloonLayout().add(balloon);
        }
      }
    });
  }
}