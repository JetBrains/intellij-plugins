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

import javax.swing.*;
import java.awt.*;

public class DocumentProblemManager {
  public static DocumentProblemManager getInstance() {
    return ServiceManager.getService(DocumentProblemManager.class);
  }

  private static StringBuilder appendTitle(StringBuilder builder) {
    return builder.append("<html><b>").append(FlexUIDesignerBundle.message("plugin.name")).append("</b>");
  }

  public void report(final Project project, String message) {
    report(project, message, MessageType.ERROR);
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
      builder.append("</ul></html>");
      report(project, builder.toString());
    }
    finally {
      StringBuilderSpinAllocator.dispose(builder);
    }
  }

  public void reportWithTitle(final Project project, String message) {
    @SuppressWarnings({"MismatchedQueryAndUpdateOfStringBuilder"})
    StringBuilder builder = StringBuilderSpinAllocator.alloc();
    try {
      report(project, appendTitle(builder).append("<p>").append(message).append("</p></html>").toString());
    }
    finally {
      StringBuilderSpinAllocator.dispose(builder);
    }
  }

  public void report(final Project project, String message, MessageType messageType) {
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