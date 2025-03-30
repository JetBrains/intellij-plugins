// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.uml;

import com.intellij.diagram.DiagramBuilder;
import com.intellij.diagram.DiagramBuilderFactory;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataSink;
import com.intellij.openapi.actionSystem.UiDataProvider;
import com.intellij.openapi.graph.services.GraphLayoutService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.JBColor;
import com.intellij.uml.components.UmlGraphZoomableViewport;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

import static com.intellij.jhipster.uml.JdlUmlElementManager.getRootData;
import static com.intellij.openapi.graph.services.GraphLayoutService.GraphLayoutQueryParams.FitContentOption.AFTER;

@SuppressWarnings("UnstableApiUsage")
final class JdlDiagramPanel implements Disposable {
  private @Nullable DiagramBuilder builder;

  private final MyPanel chartPanel = new MyPanel();
  private final JdlUmlProvider umlProvider = new JdlUmlProvider();
  private final JdlPreviewFileEditor fileEditor;

  JdlDiagramPanel(JdlPreviewFileEditor fileEditor) {
    this.fileEditor = fileEditor;
  }

  @Override
  public void dispose() {
  }

  JComponent getComponent() {
    return chartPanel;
  }

  public void draw() {
    if (builder == null) {
      Project project = fileEditor.getProject();
      VirtualFile virtualFile = fileEditor.getFile();

      builder = DiagramBuilderFactory.getInstance()
        .create(project, umlProvider, getRootData(project, virtualFile), null);
      Disposer.register(this, builder);
      builder.getView().setFitContentOnResize(true);
      JComponent graphView = createSimpleGraphView(builder);
      chartPanel.add(graphView, BorderLayout.CENTER);

      var actionsProvider = builder.getProvider().getExtras().getToolbarActionsProvider();
      var actionGroup = actionsProvider.createToolbarActions(builder);
      var actionToolbar = ActionManager.getInstance().createActionToolbar("JDL.UML", actionGroup, true);
      actionToolbar.setTargetComponent(graphView);
      actionToolbar.getComponent().setBorder(JBUI.Borders.customLine(JBColor.border(), 0, 0, 1, 0));

      chartPanel.add(actionToolbar.getComponent(), BorderLayout.NORTH);

      builder.queryUpdate()
        .withDataReload()
        .withPresentationUpdate()
        .withRelayout()
        .runAsync();
    }
  }

  private static JComponent createSimpleGraphView(@NotNull DiagramBuilder builder) {
    builder.getPresentationModel().registerActions();
    var view = builder.getView();
    view.getCanvasComponent().setBackground(JBColor.GRAY);
    GraphLayoutService.getInstance().queryLayout(builder.getGraphBuilder()).withFitContent(AFTER).run();
    return new UmlGraphZoomableViewport(builder);
  }

  private class MyPanel extends JPanel implements UiDataProvider {
    MyPanel() {
      super(new BorderLayout());
    }

    @Override
    public void uiDataSnapshot(@NotNull DataSink sink) {
      if (builder == null) return;
      sink.lazy(CommonDataKeys.VIRTUAL_FILE, () -> {
        return fileEditor.getFile();
      });
      sink.lazy(CommonDataKeys.PSI_FILE, () -> {
        return PsiManager.getInstance(fileEditor.getProject()).findFile(fileEditor.getFile());
      });
    }
  }
}
