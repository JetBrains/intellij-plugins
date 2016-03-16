package org.angularjs.codeInsight.router;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import org.angularjs.index.AngularIndexUtil;

import javax.swing.*;

/**
 * @author Irina.Chernushina on 3/8/2016.
 */
public class ShowUiRouterStatesDiagramAction extends AnAction {
  public ShowUiRouterStatesDiagramAction() {
    super("Show ui-router State Diagram");
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    final Project project = CommonDataKeys.PROJECT.getData(e.getDataContext());
    if (project == null) return;
    final AngularUiRouterDiagramBuilder builder = new AngularUiRouterDiagramBuilder(project);
    builder.build();
    final AngularUiRouterGraphBuilder graphBuilder =
      new AngularUiRouterGraphBuilder(project, builder.getStatesMap(), builder.getTemplatesMap());
    final JComponent component = graphBuilder.build();

    final DialogBuilder builder1 = new DialogBuilder(project);
    builder1.centerPanel(component);
    builder1.setTitle("Angular-ui-router states and views");
    builder1.show();
  }

  @Override
  public void update(AnActionEvent e) {
    final Project project = CommonDataKeys.PROJECT.getData(e.getDataContext());
    e.getPresentation().setEnabled(project != null && AngularIndexUtil.hasAngularJS(project));
  }
}
