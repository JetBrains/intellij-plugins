package com.intellij.javascript.karma.server;

import com.intellij.diagnostic.logging.AdditionalTabComponent;
import com.intellij.execution.actions.StopProcessAction;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.javascript.karma.util.ProcessEventStore;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * @author Sergey Simonchik
 */
public class KarmaServerLogComponent extends AdditionalTabComponent {

  private final ProcessEventStore myProcessEventStore;
  private final ConsoleView myConsole;
  private final ProcessAdapter myProcessListener;
  private ActionGroup myActionGroup;

  public KarmaServerLogComponent(@NotNull Project project, @NotNull KarmaServer karmaServer) {
    super(new BorderLayout());
    myProcessEventStore = karmaServer.getProcessEventStore();
    GlobalSearchScope scope = GlobalSearchScope.allScope(project);
    TextConsoleBuilder builder = TextConsoleBuilderFactory.getInstance().createBuilder(project, scope);
    myConsole = builder.getConsole();
    if (myConsole == null) {
      throw new RuntimeException("Console shouldn't be null!");
    }
    myProcessListener = new ProcessAdapter() {
      @Override
      public void onTextAvailable(ProcessEvent event, Key outputType) {
        myConsole.print(event.getText(), ConsoleViewContentType.getConsoleViewType(outputType));
      }
    };
    myProcessEventStore.addProcessListener(myProcessListener);
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
    group.add(new StopProcessAction("Stop Karma Server", null, myProcessEventStore.getProcessHandler()));

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
    myProcessEventStore.removeProcessListener(myProcessListener);
    System.out.println("Disposing karma server tab");
  }

}
