package com.jetbrains.lang.dart.ide.errorTreeView;

import com.google.dart.server.generated.types.AnalysisError;
import com.google.dart.server.generated.types.AnalysisErrorSeverity;
import com.google.dart.server.generated.types.Location;
import com.intellij.icons.AllIcons;
import com.intellij.ide.errorTreeView.ErrorTreeElement;
import com.intellij.ide.errorTreeView.ErrorTreeElementKind;
import com.intellij.ide.errorTreeView.ErrorViewStructure;
import com.intellij.ide.errorTreeView.GroupingElement;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.pom.Navigatable;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ArrayUtil;
import com.intellij.util.concurrency.SequentialTaskExecutor;
import com.intellij.util.ui.MessageCategory;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.ide.PooledThreadExecutor;

import javax.swing.*;
import java.util.*;

public class DartProblemsViewImpl {

  public static class SERVICE {
    private SERVICE() {
    }

    public static DartProblemsViewImpl getInstance(Project project) {
      return ServiceManager.getService(project, DartProblemsViewImpl.class);
    }
  }

  private static final Logger LOG = Logger.getInstance("#com.jetbrains.lang.dart.ide.errorTreeView.DartProblemsViewImpl");
  private static final String PROBLEMS_TOOLWINDOW_ID = "Problems";
  private static final EnumSet<ErrorTreeElementKind> ALL_MESSAGE_KINDS = EnumSet.allOf(ErrorTreeElementKind.class);

  private final DartProblemsViewPanel myPanel;
  private final Project myProject;
  private final SequentialTaskExecutor myViewUpdater = new SequentialTaskExecutor(PooledThreadExecutor.INSTANCE);
  private final Icon myActiveIcon = AllIcons.Toolwindows.Problems;
  private final Icon myPassiveIcon = IconLoader.getDisabledIcon(myActiveIcon);

  private static String[] convertMessage(final String text) {
    if (!text.contains("\n")) {
      return new String[]{text};
    }
    final List<String> lines = new ArrayList<String>();
    StringTokenizer tokenizer = new StringTokenizer(text, "\n", false);
    while (tokenizer.hasMoreTokens()) {
      lines.add(tokenizer.nextToken());
    }
    return ArrayUtil.toStringArray(lines);
  }

  public DartProblemsViewImpl(final Project project, final ToolWindowManager wm) {
    myProject = project;
    myPanel = new DartProblemsViewPanel(project);
    Disposer.register(project, new Disposable() {
      @Override
      public void dispose() {
        Disposer.dispose(myPanel);
      }
    });
    UIUtil.invokeLaterIfNeeded(new Runnable() {
      @Override
      public void run() {
        if (project.isDisposed()) {
          return;
        }
        final ToolWindow tw = wm.registerToolWindow(PROBLEMS_TOOLWINDOW_ID, false, ToolWindowAnchor.BOTTOM, project, true);
        final Content content = ContentFactory.SERVICE.getInstance().createContent(myPanel, "", false);
        // todo: setup content?
        tw.getContentManager().addContent(content);
        Disposer.register(project, new Disposable() {
          @Override
          public void dispose() {
            tw.getContentManager().removeAllContents(true);
          }
        });
        updateIcon();
      }
    });
  }

  public void clearOldMessages(@NotNull final VirtualFile vFile, @NotNull final UUID currentSessionId) {
    myViewUpdater.execute(new Runnable() {
      @Override
      public void run() {
        cleanupChildrenRecursively(myPanel.getErrorViewStructure().getRootElement(), vFile, currentSessionId);
        updateIcon();
        myPanel.reload();
      }
    });
  }

  private void cleanupChildrenRecursively(@NotNull final Object fromElement,
                                          @NotNull final VirtualFile vFile,
                                          @NotNull final UUID currentSessionId) {
    final ErrorViewStructure structure = myPanel.getErrorViewStructure();
    for (ErrorTreeElement element : structure.getChildElements(fromElement)) {
      if (element instanceof GroupingElement) {
        final VirtualFile file = ((GroupingElement)element).getFile();
        if (file != null && !vFile.getUrl().equals(file.getUrl())) {
          continue;
        }
        structure.removeElement(element);
      }
      else {
        structure.removeElement(element);
      }
    }
  }

  public void addMessage(@NotNull VirtualFile virtualFile, @NotNull AnalysisError analysisError, @Nullable UUID sessionId) {
    final Location location = analysisError.getLocation();
    Navigatable navigatable = null;
    final int line = location.getStartLine() - 1; // editor lines are zero-based
    if (line >= 0) {
      navigatable = new OpenFileDescriptor(myProject, virtualFile, line, Math.max(0, location.getStartColumn() - 1));
    }
    if (navigatable == null) {
      navigatable = new OpenFileDescriptor(myProject, virtualFile, -1, -1);
    }
    final int type = translateAnalysisServerSeverity(analysisError.getSeverity());
    final String[] text = convertMessage(analysisError.getMessage());
    final String groupName = virtualFile.getPresentableUrl();
    final String exportText = "line (" + location.getStartLine() + ") ";
    final String rendererTextPrefix = "(" + location.getStartLine() + ", " + location.getStartColumn() + ")";
    addMessage(type, text, groupName, navigatable, exportText, rendererTextPrefix, sessionId);
  }

  private static int translateAnalysisServerSeverity(String severity) {
    if (AnalysisErrorSeverity.ERROR.equals(severity)) {
      return MessageCategory.ERROR;
    }
    else if (AnalysisErrorSeverity.WARNING.equals(severity)) {
      return MessageCategory.WARNING;
    }
    else if (AnalysisErrorSeverity.INFO.equals(severity)) {
      return MessageCategory.INFORMATION;
    }
    LOG.error("Unknown message category: " + severity);
    return 0;
  }

  public void addMessage(final int type,
                         @NotNull final String[] text,
                         @Nullable final String groupName,
                         @Nullable final Navigatable navigatable,
                         @Nullable final String exportTextPrefix,
                         @Nullable final String rendererTextPrefix,
                         @Nullable final UUID sessionId) {

    myViewUpdater.execute(new Runnable() {
      @Override
      public void run() {
        final ErrorViewStructure structure = myPanel.getErrorViewStructure();
        final GroupingElement group = structure.lookupGroupingElement(groupName);
        if (group != null && sessionId != null && !sessionId.equals(group.getData())) {
          structure.removeElement(group);
        }
        if (navigatable != null) {
          myPanel.addMessage(type, text, groupName, navigatable, exportTextPrefix, rendererTextPrefix, sessionId);
        }
        else {
          myPanel.addMessage(type, text, null, -1, -1, sessionId);
        }
        updateIcon();
      }
    });
  }

  private void updateIcon() {
    UIUtil.invokeLaterIfNeeded(new Runnable() {
      @Override
      public void run() {
        if (!myProject.isDisposed()) {
          final ToolWindow tw = ToolWindowManager.getInstance(myProject).getToolWindow(PROBLEMS_TOOLWINDOW_ID);
          if (tw != null) {
            final boolean active = myPanel.getErrorViewStructure().hasMessages(ALL_MESSAGE_KINDS);
            tw.setIcon(active ? myActiveIcon : myPassiveIcon);
          }
        }
      }
    });
  }

  public void setProgress(String text, float fraction) {
    myPanel.setProgress(text, fraction);
  }

  public void setProgress(String text) {
    myPanel.setProgressText(text);
  }

  public void clearProgress() {
    myPanel.clearProgressData();
  }
}
