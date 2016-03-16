package org.angularjs.codeInsight.router;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import icons.AngularJSIcons;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

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
    final AngularUiRouterGraphBuilder graphBuilder =
      new AngularUiRouterGraphBuilder(project, builder.getStatesMap(), builder.getTemplatesMap());
    final Disposable disposable = new Disposable() {
      @Override
      public void dispose() {
      }
    };
    final JComponent component = graphBuilder.build(disposable);
    final JPanel wrapper = new JPanel(new BorderLayout());
    wrapper.add(ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, graphBuilder.buildActions(), true).getComponent(), BorderLayout.NORTH);
    wrapper.add(component, BorderLayout.CENTER);

    //final FormBuilder formBuilder = FormBuilder.createFormBuilder()
    //  .addComponent(ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, graphBuilder.buildActions(), true).getComponent())
    //  .addComponent(component);

    final Content content1 = cm.getFactory().createContent(wrapper, "", false);
    cm.addContent(content1);

    Disposer.register(content1, disposable);
    window.activate(new Runnable() {
      @Override
      public void run() {
        cm.setSelectedContent(content1);
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
