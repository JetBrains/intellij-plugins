package com.intellij.javascript.karma.server;

import com.intellij.execution.actions.StopProcessAction;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.execution.ui.layout.PlaceInGrid;
import com.intellij.javascript.karma.util.ProcessEventStore;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithActions;
import com.intellij.openapi.util.Key;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Sergey Simonchik
 */
public class KarmaServerLogComponent implements ComponentWithActions {

  private final ProcessEventStore myProcessEventStore;
  private final ConsoleView myConsole;
  private final KarmaServer myKarmaServer;
  private ActionGroup myActionGroup;

  public KarmaServerLogComponent(@NotNull Project project,
                                 @NotNull KarmaServer karmaServer) {
    myKarmaServer = karmaServer;
    myProcessEventStore = karmaServer.getProcessEventStore();
    GlobalSearchScope scope = GlobalSearchScope.allScope(project);
    TextConsoleBuilder builder = TextConsoleBuilderFactory.getInstance().createBuilder(project, scope);
    myConsole = builder.getConsole();
    if (myConsole == null) {
      throw new RuntimeException("Console shouldn't be null!");
    }
    ProcessAdapter processListener = new ProcessAdapter() {
      @Override
      public void onTextAvailable(ProcessEvent event, Key outputType) {
        myConsole.print(event.getText(), ConsoleViewContentType.getConsoleViewType(outputType));
      }
    };
    myProcessEventStore.addProcessListener(processListener);
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

  @NotNull
  @Override
  public JComponent getComponent() {
    return myConsole.getComponent();
  }

  @Override
  public boolean isContentBuiltIn() {
    return false;
  }

  public void installOn(@NotNull RunnerLayoutUi ui) {
    Content consoleContent = ui.createContent("KarmaServer",
                                              this,
                                              "Karma Server",
                                              null,
                                              myConsole.getPreferredFocusableComponent());
    consoleContent.setCloseable(false);
    ui.addContent(consoleContent, 4, PlaceInGrid.bottom, false);
    if (!myKarmaServer.isReady()) {
      ui.selectAndFocus(consoleContent, false, false);
    }
  }

}
