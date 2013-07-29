package com.intellij.javascript.karma.server;

import com.intellij.execution.actions.StopProcessAction;
import com.intellij.execution.filters.TextConsoleBuilderImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.execution.ui.layout.PlaceInGrid;
import com.intellij.javascript.karma.util.ArchivedOutputListener;
import com.intellij.javascript.karma.util.ProcessOutputArchive;
import com.intellij.javascript.nodejs.BaseNodeJSFilter;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
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

  private final ProcessOutputArchive myProcessOutputArchive;
  private final ConsoleView myConsole;
  private final KarmaServer myKarmaServer;
  private ActionGroup myActionGroup;

  public KarmaServerLogComponent(@NotNull Project project,
                                 @NotNull KarmaServer karmaServer) {
    myKarmaServer = karmaServer;
    myProcessOutputArchive = karmaServer.getProcessOutputArchive();
    GlobalSearchScope scope = GlobalSearchScope.allScope(project);
    TextConsoleBuilderImpl builder = new TextConsoleBuilderImpl(project, scope);
    builder.setUsePredefinedMessageFilter(false);
    builder.addFilter(new BaseNodeJSFilter(project));
    myConsole = builder.getConsole();
    if (myConsole == null) {
      throw new RuntimeException("Console shouldn't be null!");
    }
  }

  private void start(@NotNull final Content consoleContent) {
    ArchivedOutputListener outputListener = new ArchivedOutputListener() {
      @Override
      public void onOutputAvailable(@NotNull String text, Key outputType, boolean archived) {
        ConsoleViewContentType contentType = ConsoleViewContentType.getConsoleViewType(outputType);
        myConsole.print(text, contentType);
        if (!archived && text.startsWith("ERROR ")) {
          ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
              consoleContent.fireAlert();
            }
          }, ModalityState.any());
        }
      }
    };
    myProcessOutputArchive.addOutputListener(outputListener);
  }

  @Nullable
  @Override
  public ActionGroup getToolbarActions() {
    if (myActionGroup != null) {
      return myActionGroup;
    }
    DefaultActionGroup group = new DefaultActionGroup();
    group.add(new StopProcessAction("Stop Karma Server", null, myProcessOutputArchive.getProcessHandler()));

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

  public void installOn(@NotNull final RunnerLayoutUi ui) {
    final Content consoleContent = ui.createContent("KarmaServer",
                                                    this,
                                                    "Karma Server",
                                                    null,
                                                    myConsole.getPreferredFocusableComponent());
    consoleContent.setCloseable(false);
    ui.addContent(consoleContent, 4, PlaceInGrid.bottom, false);
    if (!myKarmaServer.isReady()) {
      ui.selectAndFocus(consoleContent, false, false);
    }
    myKarmaServer.doWhenTerminated(new KarmaServerTerminatedListener() {
      @Override
      public void onTerminated(int exitCode) {
        if (!ui.isDisposed()) {
          ui.selectAndFocus(consoleContent, false, false);
        }
      }
    });
    start(consoleContent);
  }

}
