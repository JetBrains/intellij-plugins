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

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.Alarm;
import com.intellij.util.SmartList;
import com.intellij.util.ui.UIUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartServerErrorsAnnotator;
import gnu.trove.THashMap;
import icons.DartIcons;
import org.dartlang.analysis.server.protocol.AnalysisError;
import org.dartlang.analysis.server.protocol.AnalysisErrorType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class DartProblemsViewImpl2 {
  public static final String TOOLWINDOW_ID = DartBundle.message("dart.analysis.tool.window2");

  private static final Logger LOG = Logger.getInstance(DartProblemsViewImpl2.class.getName());

  private static final int TABLE_REFRESH_PERIOD = 200;

  private final DartProblemsViewPanel2 myPanel;

  private final Object myLock = new Object(); // use this lock to access myScheduledFilePathToErrors and myAlarm
  private final Map<String, List<AnalysisError>> myScheduledFilePathToErrors = new THashMap<String, List<AnalysisError>>();
  private final Alarm myAlarm;

  private final Runnable myUpdateRunnable = new Runnable() {
    @Override
    public void run() {
      final Map<String, List<AnalysisError>> map;
      synchronized (myLock) {
        map = new THashMap<String, List<AnalysisError>>(myScheduledFilePathToErrors);
        myScheduledFilePathToErrors.clear();
      }

      for (Map.Entry<String, List<AnalysisError>> entry : map.entrySet()) {
        myPanel.setErrors(entry.getKey(), entry.getValue());
      }
    }
  };

  public DartProblemsViewImpl2(@NotNull final Project project, @NotNull final ToolWindowManager toolWindowManager) {
    myPanel = new DartProblemsViewPanel2(project);

    myAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, project);
    Disposer.register(project, myAlarm);

    UIUtil.invokeLaterIfNeeded(new Runnable() {
      @Override
      public void run() {
        if (project.isDisposed()) {
          return;
        }
        final ToolWindow toolWindow = toolWindowManager.registerToolWindow(TOOLWINDOW_ID, false, ToolWindowAnchor.BOTTOM, project, true);
        toolWindow.setIcon(DartIcons.Dart_13);
        final Content content = ContentFactory.SERVICE.getInstance().createContent(myPanel, "", false);
        toolWindow.getContentManager().addContent(content);
        Disposer.register(project, new Disposable() {
          @Override
          public void dispose() {
            toolWindow.getContentManager().removeAllContents(true);
          }
        });
      }
    });
  }

  public static DartProblemsViewImpl2 getInstance(@NotNull final Project project) {
    return ServiceManager.getService(project, DartProblemsViewImpl2.class);
  }

  public void updateErrorsForFile(@NotNull final String filePath, @NotNull final List<AnalysisError> errors) {
    final List<AnalysisError> subsetOfErrors = computeSubsetOfErrors(errors);
    synchronized (myLock) {
      if (myScheduledFilePathToErrors.isEmpty()) {
        final int cancelled = myAlarm.cancelAllRequests();
        LOG.assertTrue(cancelled == 0, cancelled + " requests cancelled");

        myAlarm.addRequest(myUpdateRunnable, TABLE_REFRESH_PERIOD, ModalityState.NON_MODAL);
      }

      myScheduledFilePathToErrors.put(filePath, subsetOfErrors);
    }
  }

  public void clearAll() {
    ApplicationManager.getApplication().assertIsDispatchThread();

    synchronized (myLock) {
      myAlarm.cancelAllRequests();
      myScheduledFilePathToErrors.clear();
    }

    myPanel.clearAll();
  }

  @NotNull
  private static List<AnalysisError> computeSubsetOfErrors(@NotNull final List<AnalysisError> errors) {
    List<AnalysisError> errorsSubsetList = new SmartList<AnalysisError>();
    for(AnalysisError e : errors) {
      if(!DartServerErrorsAnnotator.shouldIgnoreMessageFromDartAnalyzer(e)) {
        errorsSubsetList.add(e);
      }
    }
    return errorsSubsetList;
  }
}
