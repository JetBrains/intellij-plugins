package com.intellij.javascript.karma.server;

import com.intellij.diagnostic.logging.AdditionalTabComponent;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * @author Sergey Simonchik
 */
public class KarmaServerLogComponent extends AdditionalTabComponent {

  private final ConsoleView myConsole;
  private ActionGroup myActionGroup;

  public KarmaServerLogComponent(@NotNull Project project, @NotNull KarmaServer karmaServer) {
    super(new BorderLayout());
    GlobalSearchScope scope = GlobalSearchScope.allScope(project);
    TextConsoleBuilder builder = TextConsoleBuilderFactory.getInstance().createBuilder(project, scope);
    myConsole = builder.getConsole();
    if (myConsole == null) {
      throw new RuntimeException("Console shouldn't be null!");
    }
    myConsole.attachToProcess(karmaServer.getProcessHandler());
    add(myConsole.getComponent(), BorderLayout.CENTER);
    Disposer.register(this, myConsole);
  }

  @Override
  public String getTabTitle() {
    return "Karma Server";
  }

  @Override
  public JComponent getPreferredFocusableComponent() {
    return myConsole.getPreferredFocusableComponent();
  }

  @Nullable
  @Override
  public ActionGroup getToolbarActions() {
    if (myActionGroup != null) {
      return myActionGroup;
    }
    DefaultActionGroup group = new DefaultActionGroup();

    final AnAction[] actions = myConsole.createConsoleActions();
    for (AnAction action : actions) {
      group.add(action);
    }

    group.addSeparator();

    myActionGroup = group;

    return myActionGroup;
  }

  @Nullable
  @Override
  public JComponent getSearchComponent() {
    return null;
  }

  @Nullable
  @Override
  public String getToolbarPlace() {
    return ActionPlaces.UNKNOWN;
  }

  @Nullable
  @Override
  public JComponent getToolbarContextComponent() {
    final ConsoleView console = myConsole;
    return console == null ? null : console.getComponent();
  }

  @Override
  public boolean isContentBuiltIn() {
    return false;
  }

  @Override
  public void dispose() {
    System.out.println("Disposing karma server tab");
  }

}
