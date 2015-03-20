package com.jetbrains.lang.dart.ide.errorTreeView;

import com.google.dart.server.generated.types.*;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileUtil;
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
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerAnnotator;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.ide.PooledThreadExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class DartProblemsViewImpl {
  private static final Logger LOG = Logger.getInstance("#com.jetbrains.lang.dart.ide.errorTreeView.DartProblemsViewImpl");
  private static final String TOOLWINDOW_ID = DartBundle.message("dart.analysis.tool.window");

  private final DartProblemsViewPanel myPanel;
  private final Project myProject;
  private final SequentialTaskExecutor myViewUpdater = new SequentialTaskExecutor(PooledThreadExecutor.INSTANCE);

  public static DartProblemsViewImpl getInstance(@NotNull final Project project) {
    return ServiceManager.getService(project, DartProblemsViewImpl.class);
  }

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

  @NotNull
  public static String createGroupName(@NotNull final String path) {
    return FileUtil.toSystemDependentName(path);
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
        final ToolWindow tw = wm.registerToolWindow(TOOLWINDOW_ID, false, ToolWindowAnchor.BOTTOM, project, true);
        tw.setIcon(DartIcons.Dart_13);
        final Content content = ContentFactory.SERVICE.getInstance().createContent(myPanel, "", false);
        tw.getContentManager().addContent(content);
        Disposer.register(project, new Disposable() {
          @Override
          public void dispose() {
            tw.getContentManager().removeAllContents(true);
          }
        });
      }
    });
  }

  public void clearAll() {
    myViewUpdater.execute(new Runnable() {
      @Override
      public void run() {
        myPanel.getErrorViewStructure().clear();
        myPanel.updateTree();
      }
    });
  }

  public void updateErrorsForFile(@NotNull final VirtualFile vFile, @NotNull final List<AnalysisError> errors) {
    myViewUpdater.execute(new Runnable() {
      @Override
      public void run() {
        final String groupName = createGroupName(vFile.getPath());
        myPanel.removeGroup(groupName);

        for (final AnalysisError analysisError : errors) {
          if (analysisError == null || DartAnalysisServerAnnotator.shouldIgnoreMessageFromDartAnalyzer(analysisError)) continue;
          final Location location = analysisError.getLocation();
          final int line = location.getStartLine() - 1; // editor lines are zero-based
          final Navigatable navigatable = line >= 0
                                          ? new OpenFileDescriptor(myProject, vFile, line, Math.max(0, location.getStartColumn() - 1))
                                          : new OpenFileDescriptor(myProject, vFile, -1, -1);
          final int type = translateAnalysisServerSeverity(analysisError.getSeverity());
          final String[] text = convertMessage(analysisError.getMessage());
          final String exportTextPrefix = "line (" + location.getStartLine() + ") ";
          final String rendererTextPrefix = "(" + location.getStartLine() + ", " + location.getStartColumn() + ")";

          myPanel.addMessage(type, text, groupName, navigatable, exportTextPrefix, rendererTextPrefix, null);
        }

        myPanel.updateTree();
      }
    });
  }

  public void removeErrorsForFile(@NotNull final String filePath) {
    myViewUpdater.execute(new Runnable() {
      @Override
      public void run() {
        myPanel.removeGroup(createGroupName(filePath));
        myPanel.updateTree();
      }
    });
  }

  private static int translateAnalysisServerSeverity(@NotNull String severity) {
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

  public void setProgress(@Nullable final AnalysisStatus analysisStatus, @Nullable final PubStatus pubStatus) {
    if (pubStatus != null && pubStatus.isListingPackageDirs()) {
      setProgress("Running pub...");
    }
    else if (analysisStatus != null && analysisStatus.isAnalyzing()) {
      setProgress("Analyzing...");
    }
    else {
      setProgress("");
    }
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
