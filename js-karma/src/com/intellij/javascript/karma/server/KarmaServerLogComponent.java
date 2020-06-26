// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.karma.server;

import com.intellij.execution.actions.StopProcessAction;
import com.intellij.execution.filters.TextConsoleBuilderImpl;
import com.intellij.execution.process.NopProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.execution.ui.layout.PlaceInGrid;
import com.intellij.ide.browsers.OpenUrlHyperlinkInfo;
import com.intellij.javascript.karma.KarmaBundle;
import com.intellij.javascript.karma.util.ArchivedOutputListener;
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
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.Alarm;
import com.intellij.util.ui.EmptyIcon;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicLong;

public final class KarmaServerLogComponent implements ComponentWithActions {

  @NonNls public static final String KARMA_SERVER_CONTENT_ID = "KarmaServer";
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
    group.add(new StopProcessAction(KarmaBundle.message("karma.server.stop.action.name"), null, myServer.getProcessHandler()));

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
                              @NotNull KarmaServer server,
                              @NotNull RunnerLayoutUi ui) {
    ConsoleView console = createConsole(project);
    KarmaServerLogComponent component = new KarmaServerLogComponent(console, server);
    Icon emptyIcon = EmptyIcon.create(JBUIScale.scale(4));
    final Content content = ui.createContent(KARMA_SERVER_CONTENT_ID,
                                             component,
                                             KarmaBundle.message("karma.server.tab.title"),
                                             ExecutionUtil.getLiveIndicator(emptyIcon),
                                             console.getPreferredFocusableComponent());
    content.setCloseable(false);
    ui.addContent(content, 4, PlaceInGrid.bottom, false);
    NopProcessHandler wrapperProcessHandler = new NopProcessHandler();
    // we can't attach console to real process handler to not lose any messages
    console.attachToProcess(wrapperProcessHandler);
    KarmaServerTerminatedListener terminationCallback = new KarmaServerTerminatedListener() {
      @Override
      public void onTerminated(int exitCode) {
        wrapperProcessHandler.destroyProcess();
        content.setIcon(emptyIcon);
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
    component.foldCommandLine();
    component.registerPrintingBrowserCapturingSuggestion();
    Disposer.register(content, console);
    Disposer.register(console, new Disposable() {
      @Override
      public void dispose() {
        server.removeTerminatedListener(terminationCallback);
        server.getProcessOutputManager().removeOutputListener(outputListener);
      }
    });
  }

  private void foldCommandLine() {
    myServer.getCommandLineFolder().foldCommandLine(myConsole, myServer.getProcessHandler());
  }

  private void registerPrintingBrowserCapturingSuggestion() {
    myServer.onPortBound(() -> {
      if (myServer.getServerSettings().isDebug() || myServer.areBrowsersReady() || Disposer.isDisposed(myConsole)) {
        return;
      }
      final int DELAY_MILLIS = 10000;
      Alarm alarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, myConsole);
      AtomicLong lastPrintTimeMillis = new AtomicLong(0);
      alarm.addRequest(new Runnable() {
        @Override
        public void run() {
          if (myServer.getProcessHandler().isProcessTerminated()) {
            Disposer.dispose(alarm);
            return;
          }
          long timeoutMillis = lastPrintTimeMillis.get() + DELAY_MILLIS - System.currentTimeMillis();
          if (timeoutMillis > 0) {
            alarm.addRequest(this, timeoutMillis + 100, ModalityState.any());
          }
          else {
            myConsole.print(KarmaBundle.message("waiting.for.captured.browser.text") + " ", ConsoleViewContentType.SYSTEM_OUTPUT);
            String url = myServer.formatUrl("/");
            myConsole.printHyperlink(url, new OpenUrlHyperlinkInfo(url));
            myConsole.print("\n", ConsoleViewContentType.SYSTEM_OUTPUT);
            Disposer.dispose(alarm);
          }
        }
      }, DELAY_MILLIS, ModalityState.any());
      myServer.getProcessHandler().addProcessListener(new ProcessAdapter() {
        @Override
        public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
          lastPrintTimeMillis.set(System.currentTimeMillis());
        }
      }, alarm);
      myServer.onBrowsersReady(() -> Disposer.dispose(alarm));
    });
  }

  private static boolean startsWithMessage(@NotNull String line, @NonNls @NotNull String message) {
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
