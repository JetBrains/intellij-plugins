package com.intellij.javascript.karma.server;

import com.intellij.execution.actions.StopProcessAction;
import com.intellij.execution.filters.TextConsoleBuilderImpl;
import com.intellij.execution.process.NopProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.execution.ui.layout.PlaceInGrid;
import com.intellij.javascript.karma.util.ArchivedOutputListener;
import com.intellij.javascript.karma.util.KarmaUtil;
import com.intellij.javascript.nodejs.NodeStackTraceFilter;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithActions;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class KarmaServerLogComponent implements ComponentWithActions {

  private final ConsoleView myConsole;
  private final KarmaServer myServer;
  private ActionGroup myActionGroup;

  private KarmaServerLogComponent(@NotNull ConsoleView console, @NotNull KarmaServer server) {
    myConsole = console;
    myServer = server;
  }

  @Nullable
  @Override
  public ActionGroup getToolbarActions() {
    if (myActionGroup != null) {
      return myActionGroup;
    }
    DefaultActionGroup group = new DefaultActionGroup();
    group.add(new StopProcessAction("Stop Karma Server", null, myServer.getProcessHandler()));

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
    return myConsole.getComponent();
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

  @NotNull
  private static ConsoleView createConsole(@NotNull Project project) {
    GlobalSearchScope scope = GlobalSearchScope.allScope(project);
    TextConsoleBuilderImpl builder = new TextConsoleBuilderImpl(project, scope);
    builder.setUsePredefinedMessageFilter(false);
    builder.addFilter(new NodeStackTraceFilter(project));
    builder.setViewer(false);
    return builder.getConsole();
  }

  public static void register(@NotNull Project project,
                              @NotNull final KarmaServer server,
                              @NotNull final RunnerLayoutUi ui,
                              boolean requestFocus) {
    final ConsoleView console = createConsole(project);
    KarmaServerLogComponent component = new KarmaServerLogComponent(console, server);
    final Content content = ui.createContent("KarmaServer",
                                             component,
                                             "Karma Server",
                                             null,
                                             console.getPreferredFocusableComponent());
    content.setCloseable(false);
    ui.addContent(content, 4, PlaceInGrid.bottom, false);
    if (requestFocus && !server.isPortBound()) {
      ui.selectAndFocus(content, false, false);
    }
    NopProcessHandler wrapperProcessHandler = new NopProcessHandler();
    // we can't attach console to real process handler to not lose any messages
    console.attachToProcess(wrapperProcessHandler);
    KarmaServerTerminatedListener terminationCallback = new KarmaServerTerminatedListener() {
      @Override
      public void onTerminated(int exitCode) {
        wrapperProcessHandler.destroyProcess();
        KarmaUtil.selectAndFocusIfNotDisposed(ui, content, false, false);
      }
    };
    server.onTerminated(terminationCallback);
    ArchivedOutputListener outputListener = new ArchivedOutputListener() {
      @Override
      public void onOutputAvailable(@NotNull String text, Key outputType, boolean archived) {
        ConsoleViewContentType contentType = ConsoleViewContentType.getConsoleViewType(outputType);
        console.print(text, contentType);
        if (!archived && (startsWithMessage(text, ":ERROR ") || startsWithMessage(text, ":WARN [launcher]"))) {
          ApplicationManager.getApplication().invokeLater(() -> content.fireAlert(), ModalityState.any());
        }
      }
    };
    server.getProcessOutputManager().addOutputListener(outputListener);
    Disposer.register(content, console);
    Disposer.register(console, new Disposable() {
      @Override
      public void dispose() {
        server.removeTerminatedListener(terminationCallback);
        server.getProcessOutputManager().removeOutputListener(outputListener);
      }
    });
  }

  private static boolean startsWithMessage(@NotNull String line, @NotNull String message) {
    int ind = line.indexOf(message);
    if (ind == -1) {
      return false;
    }
    for (int i = 0; i < ind; i++) {
      char ch = line.charAt(i);
      if (!Character.isDigit(ch) && " :.".indexOf(ch) == -1) {
        return false;
      }
    }
    return true;
  }
}
