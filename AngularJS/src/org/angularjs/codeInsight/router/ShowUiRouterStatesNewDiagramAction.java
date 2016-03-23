package org.angularjs.codeInsight.router;

import com.intellij.diagram.DiagramProvider;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.uml.core.actions.ShowDiagram;
import icons.AngularJSIcons;
import org.angularjs.index.AngularIndexUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Irina.Chernushina on 3/23/2016.
 */
public class ShowUiRouterStatesNewDiagramAction extends ShowDiagram {
  @Override
  public void actionPerformed(AnActionEvent e) {
    final Project project = CommonDataKeys.PROJECT.getData(e.getDataContext());
    if (project == null) return;

    // todo read action?
    final AngularUiRouterDiagramBuilder builder = new AngularUiRouterDiagramBuilder(project);
    builder.build();
    final List<Pair<String, AngularUiRouterGraphBuilder>> graphBuilders = new ArrayList<>();
    final Map<VirtualFile, RootTemplate> rootTemplates = builder.getRootTemplates();

    for (Map.Entry<VirtualFile, Map<String, UiRouterState>> entry : builder.getRootTemplates2States().entrySet()) {
      final AngularUiRouterGraphBuilder graphBuilder =
        new AngularUiRouterGraphBuilder(project, entry.getValue(), builder.getTemplatesMap(), rootTemplates.get(entry.getKey()),
                                        entry.getKey());
      graphBuilders.add(Pair.create(entry.getKey().getName(), graphBuilder));
    }
    for (Map.Entry<VirtualFile, Map<String, UiRouterState>> entry : builder.getDefiningFiles2States().entrySet()) {
      final AngularUiRouterGraphBuilder graphBuilder =
        new AngularUiRouterGraphBuilder(project, entry.getValue(), builder.getTemplatesMap(), null, entry.getKey());
      graphBuilders.add(Pair.create(entry.getKey().getName(), graphBuilder));
    }
    // todo look on mac
    final AngularUiRouterDiagramProvider diagramProvider =
      (AngularUiRouterDiagramProvider)DiagramProvider.findByID(AngularUiRouterDiagramProvider.ANGULAR_UI_ROUTER);
    if (diagramProvider == null) return;
    diagramProvider.reset();
    for (Pair<String, AngularUiRouterGraphBuilder> pair : graphBuilders) {
      final AngularUiRouterGraphBuilder graphBuilder = pair.getSecond();

      final AngularUiRouterGraphBuilder.GraphNodesBuilder nodesBuilder = graphBuilder.createDataModel(diagramProvider);
      diagramProvider.registerNodesBuilder(nodesBuilder);
      final Runnable callback = show(nodesBuilder.getRootNode().getIdentifyingElement(), diagramProvider, project, null, Collections.emptyList());
      if (callback != null) {
        callback.run();
      }
    }
  }

  @Override
  public void update(AnActionEvent e) {
    final Project project = CommonDataKeys.PROJECT.getData(e.getDataContext());
    e.getPresentation().setEnabled(project != null && AngularIndexUtil.hasAngularJS(project));

    e.getPresentation().setText("Show ui-router State Diagram");
    e.getPresentation().setDescription("Show ui-router State Diagram");
    e.getPresentation().setIcon(AngularJSIcons.AngularJS);
  }
}
