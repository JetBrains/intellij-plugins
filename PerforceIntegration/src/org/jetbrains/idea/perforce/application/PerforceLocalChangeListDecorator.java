package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.changes.ChangeListDecorator;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.PerforceBundle;

import java.util.Collection;

final class PerforceLocalChangeListDecorator implements ChangeListDecorator {
  private final Project myProject;

  public PerforceLocalChangeListDecorator(final Project project) {
    myProject = project;
  }

  @Override
  public void decorateChangeList(@NotNull LocalChangeList changeList,
                                 @NotNull ColoredTreeCellRenderer cellRenderer,
                                 boolean selected,
                                 boolean expanded,
                                 boolean hasFocus) {
    if (changeList.hasDefaultName()) return;

    final Collection<? extends Long> collNumbers = PerforceNumberNameSynchronizer.getInstance(myProject).getAllNumbers(changeList.getName()).values();
    if (collNumbers.isEmpty()) return;

    String text = PerforceBundle.message("change.list.decoration", StringUtil.join(ContainerUtil.sorted(collNumbers), ", "));
    int shelved = PerforceManager.getInstance(myProject).getShelf().getShelvedChanges(changeList).size();
    if (shelved > 0) {
      text += PerforceBundle.message("change.list.decoration.suffix", shelved);
    }

    cellRenderer.append(text, SimpleTextAttributes.GRAY_ATTRIBUTES);
  }
}
