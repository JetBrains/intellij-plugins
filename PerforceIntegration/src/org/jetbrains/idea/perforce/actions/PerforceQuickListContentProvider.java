package org.jetbrains.idea.perforce.actions;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.vcs.actions.VcsQuickListContentProviderBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.application.PerforceVcs;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Roman.Chernyatchik
 */
public class PerforceQuickListContentProvider extends VcsQuickListContentProviderBase {
  @NotNull
  @Override
  protected String getVcsName() {
    return PerforceVcs.NAME;
  }

  @Override
  protected List<AnAction> collectVcsSpecificActions(@NotNull ActionManager manager) {
    final List<AnAction> actions = new ArrayList<>();
    add("ChangesView.Move", manager, actions);
    add("PerforceDirect.Edit", manager, actions);
    add("RevertUnchanged", manager, actions);
    add("PerforceEnableIntegration", manager, actions);
    return actions;
  }
}
