package org.angularjs.codeInsight.router;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.containers.ContainerUtil;
import icons.AngularJSIcons;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Irina.Chernushina on 3/8/2016.
 */
public class ShowUiRouterStatesDiagramAction extends AnAction {
  private static final String ANGULAR_UI_ROUTER = "Angular ui-router";

  public ShowUiRouterStatesDiagramAction() {
    super("Show ui-router State Diagram");
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    final Project project = CommonDataKeys.PROJECT.getData(e.getDataContext());
    if (project == null) return;

    final ToolWindow window = getToolWindow(project);
    final ContentManager cm = window.getContentManager();
    final Content content = cm.getContent(0);
    if (content != null) {
      window.activate(new Runnable() {
        @Override
        public void run() {
          cm.setSelectedContent(content, true);
        }
      });
      return;
    }

    final AngularUiRouterDiagramBuilder builder = new AngularUiRouterDiagramBuilder(project);
    builder.build();
    final List<Pair<String, AngularUiRouterGraphBuilder>> graphBuilders = new ArrayList<>();
    final Map<String, UiRouterState> statesMap = builder.getStatesMap();
    final Map<VirtualFile, RootTemplate> rootTemplates = builder.getRootTemplates();

    for (Map.Entry<VirtualFile, Collection<String>> entry : builder.getRootTemplates2States().entrySet()) {
      final Map<String, UiRouterState> localStatesMap = ContainerUtil.filter(statesMap, new Condition<String>() {
        @Override
        public boolean value(String key) {
          return entry.getValue().contains(key);
        }
      });

      final AngularUiRouterGraphBuilder graphBuilder =
        new AngularUiRouterGraphBuilder(project, localStatesMap, builder.getTemplatesMap(), rootTemplates.get(entry.getKey()));
      graphBuilders.add(Pair.create(entry.getKey().getName(), graphBuilder));
    }
    // todo refactor for clarity
    for (Map.Entry<VirtualFile, Collection<String>> entry : builder.getDefiningFiles2States().entrySet()) {
      final Map<String, UiRouterState> localStatesMap = ContainerUtil.filter(statesMap, new Condition<String>() {
        @Override
        public boolean value(String key) {
          return entry.getValue().contains(key);
        }
      });

      final AngularUiRouterGraphBuilder graphBuilder =
        new AngularUiRouterGraphBuilder(project, localStatesMap, builder.getTemplatesMap(), null);
      graphBuilders.add(Pair.create(entry.getKey().getName(), graphBuilder));
    }
    //final AngularUiRouterGraphBuilder graphBuilder =
    //  new AngularUiRouterGraphBuilder(project, builder.getStatesMap(), builder.getTemplatesMap(), builder.getRootTemplates());
    final Disposable disposable = new Disposable() {
      @Override
      public void dispose() {
      }
    };
    // todo look on mac
    for (Pair<String, AngularUiRouterGraphBuilder> pair : graphBuilders) {
      final String tabName = pair.getFirst();
      final AngularUiRouterGraphBuilder graphBuilder = pair.getSecond();
      final JComponent component = graphBuilder.build(disposable);
      final JPanel wrapper = new JPanel(new BorderLayout());
      wrapper.add(ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, graphBuilder.buildActions(), true).getComponent(), BorderLayout.NORTH);
      wrapper.add(component, BorderLayout.CENTER);
      final Content content1 = cm.getFactory().createContent(wrapper, tabName, false);
      cm.addContent(content1);
      Disposer.register(content1, disposable);
    }

    //final FormBuilder formBuilder = FormBuilder.createFormBuilder()
    //  .addComponent(ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, graphBuilder.buildActions(), true).getComponent())
    //  .addComponent(component);

    window.activate(new Runnable() {
      @Override
      public void run() {
        if (cm.getContentCount() > 0) {
          final Content content1 = cm.getContent(0);
          cm.setSelectedContent(content1);
        }
      }
    });

    /*final DialogBuilder builder1 = new DialogBuilder(project);
    builder1.setNorthPanel(ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, graphBuilder.buildActions(), true).getComponent());
    builder1.centerPanel(new JBScrollPane(component));
    builder1.setTitle("Angular-ui-router states and views");
    builder1.show();*/
  }

  @NotNull
  public static ToolWindow getToolWindow(Project project) {
    final ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
    ToolWindow window = toolWindowManager.getToolWindow(ANGULAR_UI_ROUTER);
    if (window == null) {
      window = toolWindowManager.registerToolWindow(ANGULAR_UI_ROUTER, true, ToolWindowAnchor.BOTTOM);
      window.installWatcher(window.getContentManager());
      window.setIcon(AngularJSIcons.AngularJS);
    }
    return window;
  }

  @Override
  public void update(AnActionEvent e) {
    final Project project = CommonDataKeys.PROJECT.getData(e.getDataContext());
    e.getPresentation().setEnabled(project != null && AngularIndexUtil.hasAngularJS(project));
  }
}
