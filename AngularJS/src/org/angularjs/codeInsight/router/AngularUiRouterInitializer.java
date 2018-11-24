package org.angularjs.codeInsight.router;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;

/**
 * @author Irina.Chernushina on 7/4/2016.
 */
public class AngularUiRouterInitializer {
  public AngularUiRouterInitializer(ActionManager actionManager) {
    final AnAction action = actionManager.getAction("UML.EditorGroup");
    final AnAction showUiRouter = actionManager.getAction("AngularJS.Show.Ui.Router.States.Diagram.Action");
    if (action instanceof DefaultActionGroup) {
      ((DefaultActionGroup)action).add(showUiRouter);
    }
  }
}
