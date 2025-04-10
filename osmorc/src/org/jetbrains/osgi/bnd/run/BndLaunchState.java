// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.osgi.bnd.run;

import aQute.bnd.build.ProjectLauncher;
import com.intellij.debugger.ui.HotSwapUI;
import com.intellij.debugger.ui.HotSwapVetoableListener;
import com.intellij.execution.CantRunException;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.compiler.CompilationStatusListener;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompilerTopics;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileAttributes;
import com.intellij.openapi.util.io.FileSystemUtil;
import com.intellij.task.ProjectTaskContext;
import com.intellij.util.messages.SimpleMessageBusConnection;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.intellij.openapi.util.Pair.pair;
import static org.osmorc.i18n.OsmorcBundle.message;

public class BndLaunchState extends JavaCommandLineState implements CompilationStatusListener, HotSwapVetoableListener {
  private static final Logger LOG = Logger.getInstance(BndLaunchState.class);
  private static final Pair<Long, Long> MISSING_BUNDLE = pair(0L, 0L);

  private static final Map<String, NotificationGroup> ourNotificationGroups = new HashMap<>();

  private final BndRunConfigurationBase.Launch myConfiguration;
  private final Project myProject;
  private final NotificationGroup myNotifications;
  private final ProjectLauncher myLauncher;
  private final Map<String, Pair<Long, Long>> myBundleStamps;

  public BndLaunchState(@NotNull ExecutionEnvironment environment, @NotNull BndRunConfigurationBase.Launch configuration) throws ExecutionException {
    super(environment);

    myConfiguration = configuration;
    myProject = myConfiguration.getProject();

    String toolWindowId = environment.getExecutor().getToolWindowId();
    NotificationGroup notificationGroup = ourNotificationGroups.get(toolWindowId);
    if (notificationGroup == null) {
      String name = BndRunConfigurationType.getInstance().getDisplayName() + " (" + toolWindowId + ")";
      notificationGroup = NotificationGroup.toolWindowGroup(name, toolWindowId);
      ourNotificationGroups.put(toolWindowId, notificationGroup);
    }
    myNotifications = notificationGroup;

    String bndRunFile = myConfiguration.getOptions().getBndRunFile();
    File runFile = bndRunFile == null ? null : new File(bndRunFile);
    if (runFile == null || !runFile.isFile()) {
      throw new CantRunException(message("bnd.run.configuration.invalid", bndRunFile));
    }

    try {
      String title = message("bnd.run.configuration.progress");
      myLauncher = ProgressManager.getInstance().run(new Task.WithResult<ProjectLauncher, Exception>(myProject, title, false) {
        @Override
        protected ProjectLauncher compute(@NotNull ProgressIndicator indicator) throws Exception {
          indicator.setIndeterminate(true);
          ProjectLauncher launcher = BndLaunchUtil.getRun(runFile).getProjectLauncher();
          launcher.prepare();
          return launcher;
        }
      });
    }
    catch (Throwable t) {
      LOG.info(t);
      throw new CantRunException(message("bnd.run.configuration.cannot.run", runFile, BndLaunchUtil.message(t)));
    }

    myBundleStamps = new HashMap<>();
    bundlesChanged();
  }

  @Override
  protected JavaParameters createJavaParameters() throws ExecutionException {
    return BndLaunchUtil.createJavaParameters(myConfiguration, myLauncher);
  }

  @Override
  protected @NotNull OSProcessHandler startProcess() throws ExecutionException {
    OSProcessHandler handler = super.startProcess();

    SimpleMessageBusConnection connection = myProject.getMessageBus().simpleConnect();
    connection.subscribe(CompilerTopics.COMPILATION_STATUS, this);

    HotSwapUI hotSwapManager = HotSwapUI.getInstance(myProject);
    hotSwapManager.addListener(this);

    handler.addProcessListener(new ProcessListener() {
      @Override
      public void processTerminated(@NotNull ProcessEvent event) {
        connection.disconnect();
        hotSwapManager.removeListener(BndLaunchState.this);
        myLauncher.cleanup();
      }
    });

    return handler;
  }

  @Override
  public void compilationFinished(boolean aborted, int errors, int warnings, @NotNull CompileContext context) {
    if (!aborted && errors == 0 && bundlesChanged()) {
      try {
        myLauncher.update();
        myNotifications.createNotification(message("bnd.run.reloaded.text"), NotificationType.INFORMATION).notify(myProject);
      }
      catch (Exception e) {
        LOG.error(e);
      }
    }
  }

  private boolean bundlesChanged() {
    boolean changed = false;

    for (String bundle : myLauncher.getRunBundles()) {
      FileAttributes attributes = FileSystemUtil.getAttributes(bundle);
      Pair<Long, Long> current = attributes != null ? pair(attributes.lastModified, attributes.length) : MISSING_BUNDLE;
      if (!current.equals(myBundleStamps.get(bundle))) {
        myBundleStamps.put(bundle, current);
        changed = true;
      }
    }

    return changed;
  }

  @Override
  public boolean shouldHotSwap(@NotNull ProjectTaskContext context) {
    return false;
  }
}