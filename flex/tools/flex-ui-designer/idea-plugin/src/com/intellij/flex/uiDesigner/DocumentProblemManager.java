package com.intellij.flex.uiDesigner;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.impl.IdeFrameImpl;
import com.intellij.ui.BrowserHyperlinkListener;
import com.intellij.util.StringBuilderSpinAllocator;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DocumentProblemManager {
  public static DocumentProblemManager getInstance() {
    return ServiceManager.getService(DocumentProblemManager.class);
  }

  private static StringBuilder appendTitle(StringBuilder builder) {
    return builder.append("<b>").append(FlashUIDesignerBundle.message("plugin.name")).append("</b>");
  }

  public void report(@Nullable Project project, ProblemsHolder problems) {
    report(project, toString(problems.getProblems()), MessageType.ERROR);
  }

  protected static StringBuilder toString(ProblemDescriptor problem, StringBuilder builder) {
    builder.append(problem.getMessage());
    if (problem.hasLineNumber()) {
      LogMessageUtil.appendLineNumber(builder, problem);
    }

    return builder;
  }

  @SuppressWarnings("MethodMayBeStatic")
  public String toString(List<ProblemDescriptor> problems) {
    final StringBuilder builder = StringBuilderSpinAllocator.alloc();
    try {
      appendTitle(builder).append("<ul>");
      for (ProblemDescriptor problem : problems) {
        builder.append("<li>");
        toString(problem, builder).append("</li>");
      }
      builder.append("</ul>");
      return builder.toString();
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
    //Notification notification = new Notification(FlashUIDesignerBundle.message("plugin.name"),
    //  title == null ? FlashUIDesignerBundle.message("plugin.name") : title, message, NotificationType.ERROR);
    //notification.notify(project);

    final Balloon balloon = JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(message, messageType, new BrowserHyperlinkListener()).setShowCallout(false)
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