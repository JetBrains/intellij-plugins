// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.codeInsight.router;

import com.intellij.diagram.DiagramProvider;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBList;
import com.intellij.uml.core.actions.ShowDiagram;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Consumer;
import icons.AngularJSIcons;
import org.angularjs.AngularJSBundle;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

final class ShowUiRouterStatesNewDiagramAction extends ShowDiagram {
  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    final Project project = e.getProject();
    if (project == null) return;

    DiagramProvider<DiagramObject> diagramProvider = DiagramProvider.findByID(AngularUiRouterDiagramProvider.ANGULAR_UI_ROUTER);
    if (diagramProvider == null) {
      return;
    }

    List<Pair<String, AngularUiRouterGraphBuilder>> graphBuilders = new ArrayList<>();
    ProgressManager.getInstance().runProcessWithProgressSynchronously(
      () -> ApplicationManager.getApplication().runReadAction(() -> {
        final AngularUiRouterDiagramBuilder builder = new AngularUiRouterDiagramBuilder(project);
        builder.build();
        final Map<VirtualFile, RootTemplate> rootTemplates = builder.getRootTemplates();

        for (Map.Entry<VirtualFile, Map<String, UiRouterState>> entry : builder.getDefiningFiles2States().entrySet()) {
          final AngularUiRouterGraphBuilder graphBuilder =
            new AngularUiRouterGraphBuilder(project, entry.getValue(), builder.getTemplatesMap(), null, entry.getKey());
          graphBuilders.add(Pair.create(entry.getKey().getName(), graphBuilder));
        }
        for (Map.Entry<VirtualFile, Map<String, UiRouterState>> entry : builder.getRootTemplates2States().entrySet()) {
          final AngularUiRouterGraphBuilder graphBuilder =
            new AngularUiRouterGraphBuilder(project, entry.getValue(), builder.getTemplatesMap(), rootTemplates.get(entry.getKey()),
                                            entry.getKey());
          graphBuilders.add(Pair.create(entry.getKey().getName(), graphBuilder));
        }
      }), AngularJSBundle.message("angularjs.ui.router.diagram.action.new.diagram.progress", diagramProvider.getPresentableName()), false,
      project);

    final AngularUiRouterProviderContext routerProviderContext = AngularUiRouterProviderContext.getInstance(project);
    routerProviderContext.reset();
    final Consumer<AngularUiRouterGraphBuilder> consumer = graphBuilder -> {
      final AngularUiRouterGraphBuilder.GraphNodesBuilder nodesBuilder = graphBuilder.createDataModel(diagramProvider);
      routerProviderContext.registerNodesBuilder(nodesBuilder);
      final DiagramObject element = nodesBuilder.getRootNode().getIdentifyingElement();

      //noinspection unchecked
      final Runnable callback =
        show(project, ((DiagramProvider)diagramProvider), element, Collections.emptyList(), getLocation(e.getDataContext(), e));
      if (callback != null) {
        callback.run();
      }
    };
    if (graphBuilders.isEmpty()) {
      //noinspection DialogTitleCapitalization
      Messages.showInfoMessage(project,
                               AngularJSBundle.message("angularjs.ui.router.diagram.action.new.diagram.info.no.router.states.found"),
                               AngularJSBundle.message("angularjs.ui.router.diagram.action.new.diagram.name"));
      return;
    }
    if (graphBuilders.size() == 1) {
      consumer.consume(graphBuilders.get(0).getSecond());
    }
    else {
      filterGraphBuilders(project, graphBuilders, consumer);
    }
  }

  private static void filterGraphBuilders(Project project, List<? extends Pair<String, AngularUiRouterGraphBuilder>> builders,
                                          Consumer<? super AngularUiRouterGraphBuilder> consumer) {
    final JBList list = new JBList();
    final List<Object> data = new ArrayList<>();
    for (Pair<String, AngularUiRouterGraphBuilder> builder : builders) {
      data.add(builder.getSecond().getKey().getPath());
    }
    list.setListData(ArrayUtil.toObjectArray(data));
    JBPopupFactory.getInstance().createListPopupBuilder(list)
      .setTitle(AngularJSBundle.message("angularjs.ui.router.diagram.action.new.diagram.select.main.file"))
      .setItemChoosenCallback(() -> {
        final int index = list.getSelectedIndex();
        if (index >= 0) {
          consumer.consume(builders.get(index).getSecond());
        }
      })
      .createPopup().showCenteredInCurrentWindow(project);
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    final Project project = e.getProject();
    try {
      e.getPresentation().setEnabledAndVisible(
        project != null
        && ApplicationManager.getApplication()
             .executeOnPooledThread(() -> {
               return AngularIndexUtil.hasAngularJS(project);
             })
             .get(5, TimeUnit.MILLISECONDS) == Boolean.TRUE);
    }
    catch (Exception exception) {
      e.getPresentation().setEnabledAndVisible(false);
    }

    //noinspection DialogTitleCapitalization
    e.getPresentation().setText(AngularJSBundle.message("angularjs.ui.router.diagram.action.new.diagram.name"));
    e.getPresentation().setDescription(AngularJSBundle.message("angularjs.ui.router.diagram.action.new.diagram.description"));
    e.getPresentation().setIcon(AngularJSIcons.AngularJS);
  }
}
