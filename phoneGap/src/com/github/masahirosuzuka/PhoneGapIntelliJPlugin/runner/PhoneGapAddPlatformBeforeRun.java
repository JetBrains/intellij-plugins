// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapBundle;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommandLine;
import com.intellij.execution.BeforeRunTask;
import com.intellij.execution.BeforeRunTaskProvider;
import com.intellij.execution.ExecutionHelper;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.MessageView;
import com.intellij.util.concurrency.Semaphore;
import com.intellij.util.containers.ContainerUtil;
import icons.PhoneGapIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PhoneGapAddPlatformBeforeRun extends BeforeRunTaskProvider<PhoneGapAddPlatformBeforeRun.PhoneGapAddPlatformTask> {

  public static final Key<PhoneGapAddPlatformTask> ID = Key.create("PhonegapTask");
  public static final long INIT_TIMEOUT = TimeUnit.MINUTES.toMillis(10);
  public static final long LOCK_TIMEOUT = TimeUnit.MINUTES.toMillis(11);

  @Override
  public Key<PhoneGapAddPlatformTask> getId() {
    return ID;
  }

  @Override
  public String getName() {
    return PhoneGapBundle.message("phonegap.before.task.init.default.title");
  }

  @Override
  public String getDescription(PhoneGapAddPlatformTask task) {
    return PhoneGapBundle.message("phonegap.before.task.init.default.title");
  }

  @Override
  public Icon getIcon() {
    return PhoneGapIcons.PhonegapIntegration;
  }

  @Nullable
  @Override
  public PhoneGapAddPlatformTask createTask(@NotNull RunConfiguration runConfiguration) {
    return runConfiguration instanceof PhoneGapRunConfiguration ? new PhoneGapAddPlatformTask(ID) : null;
  }

  @Override
  public boolean canExecuteTask(@NotNull RunConfiguration configuration, @NotNull PhoneGapAddPlatformTask task) {
    return configuration instanceof PhoneGapRunConfiguration;
  }

  @Override
  public boolean executeTask(@NotNull DataContext context,
                             @NotNull final RunConfiguration configuration,
                             @NotNull ExecutionEnvironment env,
                             @NotNull PhoneGapAddPlatformTask task) {

    final PhoneGapRunConfiguration phoneGapRunConfiguration = (PhoneGapRunConfiguration)configuration;
    final PhoneGapCommandLine line = phoneGapRunConfiguration.getCommandLine();

    if (!line.needAddPlatform()) {
      return true;
    }

    final Project project = configuration.getProject();
    final Semaphore targetDone = new Semaphore();
    final Ref<Boolean> result = new Ref<>(true);
    final List<Exception> exceptions = new ArrayList<>();
    final String tabText = PhoneGapBundle.message("phonegap.before.task.init.title", line.getPlatformName());
    targetDone.down();

    ApplicationManager.getApplication().invokeLater(() -> {

      //Save all opened documents
      FileDocumentManager.getInstance().saveAllDocuments();

      JPanel panel = new JPanel(new BorderLayout());
      ConsoleViewImpl console = new ConsoleViewImpl(project, true);
      panel.add(console.getComponent(), BorderLayout.CENTER);
      createViewForOutput(console, panel, project, tabText);

      new Task.Backgroundable(project, tabText, true) {

        @Override
        public void run(@NotNull final ProgressIndicator indicator) {
          try {
            String platform = phoneGapRunConfiguration.getPlatform();
            assert platform != null;

            CapturingProcessHandler handler = line.platformAdd(platform);
            console.attachToProcess(handler);


            ProcessOutput output = handler.runProcessWithProgressIndicator(indicator, (int)INIT_TIMEOUT);
            if (output.isCancelled()) {
              result.set(false);
            }
            else {
              String stdout = output.getStdout();
              String stderr = output.getStderr();
              if (outputContains(stdout, stderr, "Platform " + platform + " already exists") ||
                  outputContains(stdout, stderr, "Platform " + platform + " already added")) {
                ApplicationManager.getApplication().invokeLater(() -> {
                  final MessageView messageView = project.getService(MessageView.class);
                  removeContents(messageView, null, tabText);
                });
              }
            }
          }
          catch (final Exception e) {
            exceptions.add(e);
            result.set(false);
          }
          finally {
            targetDone.up();
          }
        }
      }.queue();
    }, ModalityState.NON_MODAL);

    if (!targetDone.waitFor(LOCK_TIMEOUT)) {
      ExecutionHelper.showErrors(project, ContainerUtil.createMaybeSingletonList(new RuntimeException("Timeout")), tabText, null);
    }
    else if (!exceptions.isEmpty()) {
      ExecutionHelper.showErrors(project, exceptions, PhoneGapBundle.message("phonegap.before.task.init.error"), null);
    }


    return result.get();
  }

  public boolean outputContains(String stdout, String stderr, String text1) {
    return StringUtil.contains(stdout, text1) || StringUtil.contains(stderr, text1);
  }


  private static void createViewForOutput(ConsoleViewImpl console,
                                          @NotNull final JComponent component,
                                          @NotNull final Project myProject,
                                          @NotNull @NlsContexts.TabTitle final String tabDisplayName) {
    CommandProcessor commandProcessor = CommandProcessor.getInstance();
    commandProcessor.executeCommand(myProject, () -> {
      final MessageView messageView = myProject.getService(MessageView.class);
      final Content content = ContentFactory.SERVICE.getInstance().createContent(component, tabDisplayName, true);
      messageView.getContentManager().addContent(content);
      messageView.getContentManager().setSelectedContent(content);
      Disposer.register(content, console);
      removeContents(messageView, content, tabDisplayName);
    }, PhoneGapBundle.message("command.name.open", tabDisplayName), null);
  }

  private static void removeContents(@NotNull MessageView messageView,
                                     @Nullable final Content skip,
                                     @NotNull final String tabDisplayName) {
    Content[] contents = messageView.getContentManager().getContents();
    for (Content content : contents) {
      if (content.isPinned()) continue;
      if (tabDisplayName.equals(content.getDisplayName()) && content != skip) {
        if (messageView.getContentManager().removeContent(content, true)) {
          content.release();
        }
      }
    }
  }


  public static class PhoneGapAddPlatformTask extends BeforeRunTask<PhoneGapAddPlatformTask> {
    protected PhoneGapAddPlatformTask(@NotNull Key<PhoneGapAddPlatformTask> providerId) {
      super(providerId);
      setEnabled(true);
    }
  }
}
