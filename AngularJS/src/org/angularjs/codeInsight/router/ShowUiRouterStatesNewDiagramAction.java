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
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Irina.Chernushina on 3/23/2016.
 */
public class ShowUiRouterStatesNewDiagramAction extends ShowDiagram {
  public static final String USAGE_KEY = "angular.js.ui.router.show.diagram";
  public static final String DESCRIPTION = "Show AngularJS ui-router State Diagram";

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    final Project project = e.getProject();
    if (project == null) return;

    final AngularUiRouterDiagramProvider diagramProvider =
      (AngularUiRouterDiagramProvider)DiagramProvider.findByID(AngularUiRouterDiagramProvider.ANGULAR_UI_ROUTER);
    if (diagramProvider == null) return;
    List<Pair<String, AngularUiRouterGraphBuilder>> graphBuilders = new ArrayList<>();
    ProgressManager.getInstance().runProcessWithProgressSynchronously(
      () -> {
        ApplicationManager.getApplication().runReadAction(() -> {
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
        });
      }, "Building " + diagramProvider.getPresentableName() + " diagram", false, project);

    final AngularUiRouterProviderContext routerProviderContext = AngularUiRouterProviderContext.getInstance(project);
    routerProviderContext.reset();
    final Consumer<AngularUiRouterGraphBuilder> consumer = graphBuilder -> {
      final AngularUiRouterGraphBuilder.GraphNodesBuilder nodesBuilder = graphBuilder.createDataModel(diagramProvider);
      routerProviderContext.registerNodesBuilder(nodesBuilder);
      final DiagramObject element = nodesBuilder.getRootNode().getIdentifyingElement();

      final Runnable callback = show(element, diagramProvider, project, null, Collections.emptyList());
      if (callback != null) {
        callback.run();
      }
    };
    if (graphBuilders.isEmpty()) {
      Messages.showInfoMessage(project, "No router states found.", DESCRIPTION);
      return;
    }
    if (graphBuilders.size() == 1) consumer.consume(graphBuilders.get(0).getSecond());
    else filterGraphBuilders(project, graphBuilders, consumer);
  }

  private static void filterGraphBuilders(Project project, List<Pair<String, AngularUiRouterGraphBuilder>> builders,
                                                                                     Consumer<AngularUiRouterGraphBuilder> consumer) {
    final JBList list = new JBList();
    final List<Object> data = new ArrayList<>();
    for (Pair<String, AngularUiRouterGraphBuilder> builder : builders) {
      data.add(builder.getSecond().getKey().getPath());
    }
    list.setListData(ArrayUtil.toObjectArray(data));
    JBPopupFactory.getInstance().createListPopupBuilder(list)
      .setTitle("Select Main Template File")
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
    e.getPresentation().setEnabledAndVisible(project != null && AngularIndexUtil.hasAngularJS(project));

    e.getPresentation().setText(DESCRIPTION);
    e.getPresentation().setDescription(DESCRIPTION);
    e.getPresentation().setIcon(AngularJSIcons.AngularJS);
  }
}
