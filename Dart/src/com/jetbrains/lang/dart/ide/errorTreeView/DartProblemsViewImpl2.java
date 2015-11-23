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
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.concurrency.SequentialTaskExecutor;
import com.intellij.util.ui.UIUtil;
import com.jetbrains.lang.dart.DartBundle;
import icons.DartIcons;
import org.dartlang.analysis.server.protocol.AnalysisError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.ide.PooledThreadExecutor;

import java.util.List;

public class DartProblemsViewImpl2 {
  public static final String TOOLWINDOW_ID = DartBundle.message("dart.analysis.tool.window2");
  private static final Logger LOG = Logger.getInstance("#com.jetbrains.lang.dart.ide.errorTreeView.DartProblemsViewImpl2");
  private final DartProblemsViewPanel2 myPanel;
  private final Project myProject;
  private final SequentialTaskExecutor myViewUpdater = new SequentialTaskExecutor(PooledThreadExecutor.INSTANCE);

  public DartProblemsViewImpl2(@NotNull final Project project, @NotNull final ToolWindowManager wm) {
    myProject = project;
    myPanel = new DartProblemsViewPanel2(project);
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
    // TODO(scheglov) implement
    //myViewUpdater.execute(new Runnable() {
    //  @Override
    //  public void run() {
    //    myPanel.getErrorViewStructure().clear();
    //    myPanel.updateTree();
    //  }
    //});
  }

  public void updateErrorsForFile(@NotNull final String filePath, @NotNull final List<AnalysisError> errors) {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        // TODO(scheglov) batch multiple invocations for 100 ms or so?
        myPanel.setErrors(filePath, errors);
      }
    });
  }

  public static DartProblemsViewImpl2 getInstance(@NotNull final Project project) {
    return ServiceManager.getService(project, DartProblemsViewImpl2.class);
  }
}
