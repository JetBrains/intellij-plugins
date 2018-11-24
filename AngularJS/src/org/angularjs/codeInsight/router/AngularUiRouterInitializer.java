package org.angularjs.codeInsight.router;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/**
 * @author Irina.Chernushina on 7/4/2016.
 */
public class AngularUiRouterInitializer implements StartupActivity {
  private volatile boolean myInitialized;

  @Override
  public void runActivity(@NotNull Project project) {
    if (myInitialized) return;
    final ActionManager actionManager = ActionManager.getInstance();
    final AnAction action = actionManager.getAction("UML.EditorGroup");
    final AnAction showUiRouter = actionManager.getAction("AngularJS.Show.Ui.Router.States.Diagram.Action");
    if (action instanceof DefaultActionGroup) {
      ((DefaultActionGroup)action).add(showUiRouter);
    }
    myInitialized = true;
  }
}
