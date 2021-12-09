package org.jetbrains.idea.perforce.actions;

import com.intellij.icons.AllIcons;
import com.intellij.idea.ActionsBundle;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.PerforceChangeProvider;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;

public class ForceRefreshAction extends DumbAwareAction {
  public ForceRefreshAction() {
    super(ActionsBundle.messagePointer("action.ForceRefresh.text"),
          PerforceBundle.messagePointer("refresh.force.description"),
          AllIcons.Actions.ForceRefresh);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    final Project project = e.getProject();
    if (project == null) return;
    final PerforceVcs vcs = PerforceVcs.getInstance(project);
    // mark everything dirty is inside
    ((PerforceChangeProvider) vcs.getChangeProvider()).discardCache();
  }

  /**
   * Updates the state of the action. Default implementation does nothing.
   * Override this method to provide the ability to dynamically change action's
   * state and(or) presentation depending on the context (For example
   * when your action state depends on the selection you can check for
   * selection and change the state accordingly).
   * This method can be called frequently, for instance, if an action is added to a toolbar,
   * it will be updated twice a second. This means that this method is supposed to work really fast,
   * no real work should be done at this phase. For example, checking selection in a tree or a list,
   * is considered valid, but working with a file system is not. If you cannot understand the state of
   * the action fast you should do it in the {@link #actionPerformed(AnActionEvent)} method and notify
   * the user that action cannot be executed if it's the case.
   *
   * @param e Carries information on the invocation place and data available
   */
  @Override
  public void update(@NotNull AnActionEvent e) {
    final Project project = e.getProject();
    if (project == null || !PerforceSettings.getSettings(project).ENABLED) {
      e.getPresentation().setVisible(false);
      return;
    }
    AbstractVcs[] vcss = ProjectLevelVcsManager.getInstance(project).getAllActiveVcss();
    boolean shouldBeVisible = false;
    VcsKey key = PerforceVcs.getKey();
    for (AbstractVcs vcs : vcss) {
      if (vcs == null) continue;
      shouldBeVisible = key.equals(vcs.getKeyInstanceMethod());
      if (shouldBeVisible) break;
    }
    e.getPresentation().setVisible(shouldBeVisible);
  }
}
