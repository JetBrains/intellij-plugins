// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.actions;

import com.intellij.CommonBundle;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.actions.StopProcessAction;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.filters.UrlFilter;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.IdeCoreBundle;
import com.intellij.ide.actions.CloseActiveTabAction;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.UIBundle;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.content.MessageView;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartStartupActivity;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.flutter.FlutterUtil;
import com.jetbrains.lang.dart.ide.runner.DartConsoleFilter;
import com.jetbrains.lang.dart.ide.runner.DartRelativePathsConsoleFilter;
import com.jetbrains.lang.dart.sdk.DartConfigurable;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkLibUtil;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.jetbrains.lang.dart.util.PubspecYamlUtil.PUBSPEC_YAML;

public abstract class DartPubActionBase extends AnAction implements DumbAware {
  public static final String PUB_ENV_VAR_NAME = "PUB_ENVIRONMENT";

  private static final @NonNls String GROUP_DISPLAY_ID = "Dart Pub Tool";
  private static final Key<PubToolWindowContentInfo> PUB_TOOL_WINDOW_CONTENT_INFO_KEY = Key.create("PUB_TOOL_WINDOW_CONTENT_INFO_KEY");

  private static final String DART_PUB_MIN_SDK_VERSION = "2.10";

  private static final AtomicBoolean ourInProgress = new AtomicBoolean(false);

  public static boolean isUseDartTestInsteadOfPubRunTest(@NotNull DartSdk dartSdk) {
    return StringUtil.compareVersionNumbers(dartSdk.getVersion(), DART_PUB_MIN_SDK_VERSION) >= 0;
  }

  public static void setupPubExePath(@NotNull GeneralCommandLine commandLine, @NotNull DartSdk dartSdk) {
    boolean useDartPub = StringUtil.compareVersionNumbers(dartSdk.getVersion(), DART_PUB_MIN_SDK_VERSION) >= 0;
    if (useDartPub) {
      commandLine.setExePath(FileUtil.toSystemDependentName(DartSdkUtil.getDartExePath(dartSdk)));
      commandLine.addParameter("pub");
    }
    else {
      commandLine.setExePath(FileUtil.toSystemDependentName(DartSdkUtil.getPubPath(dartSdk)));
    }
  }

  public static String getPubEnvValue() {
    String existingVar = System.getenv(PUB_ENV_VAR_NAME);
    if (existingVar == null) {
      return DartAnalysisServerService.getClientId();
    }
    else {
      return existingVar + ":" + DartAnalysisServerService.getClientId();
    }
  }

  public static boolean isInProgress() {
    return ourInProgress.get();
  }

  @SuppressWarnings("unused") // Used by Flutter Plugin.
  public static void setIsInProgress(boolean inProgress) {
    ourInProgress.set(inProgress);
  }

  @Override
  public void update(final @NotNull AnActionEvent e) {
    final Pair<Module, VirtualFile> moduleAndPubspec = getModuleAndPubspecYamlFile(e);
    // Defer to the Flutter plugin if appropriate.
    final boolean visible = moduleAndPubspec != null &&
                            !(FlutterUtil.isFlutterPluginInstalled() && FlutterUtil.isPubspecDeclaringFlutter(moduleAndPubspec.second));
    e.getPresentation().setVisible(visible);
    e.getPresentation().setEnabled(visible && !isInProgress());
  }

  private static @Nullable Pair<Module, VirtualFile> getModuleAndPubspecYamlFile(final AnActionEvent e) {
    final Module module = e.getData(LangDataKeys.MODULE);
    final PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);

    if (module != null && psiFile != null && psiFile.getName().equalsIgnoreCase(PUBSPEC_YAML)) {
      final VirtualFile file = psiFile.getOriginalFile().getVirtualFile();
      return file != null ? Pair.create(module, file) : null;
    }
    return null;
  }

  protected abstract @NotNull @NlsContexts.DialogTitle String getTitle(final @NotNull Project project,
                                                                       final @NotNull VirtualFile pubspecYamlFile);

  protected abstract String @Nullable [] calculatePubParameters(final @NotNull Project project, final @NotNull VirtualFile pubspecYamlFile);

  @Override
  public void actionPerformed(final @NotNull AnActionEvent e) {
    final Pair<Module, VirtualFile> moduleAndPubspecYamlFile = getModuleAndPubspecYamlFile(e);
    if (moduleAndPubspecYamlFile == null) return;

    final Module module = moduleAndPubspecYamlFile.first;
    final VirtualFile pubspecYamlFile = moduleAndPubspecYamlFile.second;

    performPubAction(module, pubspecYamlFile, true);
  }

  public void performPubAction(final @NotNull Module module, final @NotNull VirtualFile pubspecYamlFile, final boolean allowModalDialogs) {
    DartSdk sdk = DartSdk.getDartSdk(module.getProject());

    if (sdk == null && allowModalDialogs) {
      final int answer = Messages.showDialog(module.getProject(),
                                             DartBundle.message("dart.sdk.is.not.configured"),
                                             getTitle(module.getProject(), pubspecYamlFile),
                                             new String[]{DartBundle.message("setup.dart.sdk"), CommonBundle.getCancelButtonText()},
                                             Messages.OK,
                                             Messages.getErrorIcon());
      if (answer != Messages.OK) return;

      DartConfigurable.openDartSettings(module.getProject());
      sdk = DartSdk.getDartSdk(module.getProject());
    }

    if (sdk == null) return;

    boolean useDartPub = StringUtil.compareVersionNumbers(sdk.getVersion(), DART_PUB_MIN_SDK_VERSION) >= 0;
    File exeFile = useDartPub ? new File(DartSdkUtil.getDartExePath(sdk))
                              : new File(DartSdkUtil.getPubPath(sdk));
    if (!exeFile.isFile()) {
      if (allowModalDialogs) {
        final int answer =
          Messages.showDialog(module.getProject(),
                              DartBundle.message("dart.sdk.bad.dartpub.path", exeFile.getPath()),
                              getTitle(module.getProject(), pubspecYamlFile),
                              new String[]{DartBundle.message("setup.dart.sdk"), CommonBundle.getCancelButtonText()},
                              Messages.OK,
                              Messages.getErrorIcon());
        if (answer == Messages.OK) {
          DartConfigurable.openDartSettings(module.getProject());
        }
      }

      return;
    }

    final String[] pubParameters = calculatePubParameters(module.getProject(), pubspecYamlFile);

    if (pubParameters != null) {
      final GeneralCommandLine command = new GeneralCommandLine().withWorkDirectory(pubspecYamlFile.getParent().getPath());

      if (FlutterUtil.isFlutterModule(module)) {
        final String flutterRoot = FlutterUtil.getFlutterRoot(sdk.getHomePath());
        if (flutterRoot != null) {
          command.getEnvironment().put("FLUTTER_ROOT", FileUtil.toSystemDependentName(flutterRoot));
        }
      }

      setupPubExePath(command, sdk);
      command.addParameters(pubParameters);

      doPerformPubAction(module, pubspecYamlFile, command, getTitle(module.getProject(), pubspecYamlFile));
    }
  }

  private static void doPerformPubAction(final @NotNull Module module,
                                         final @NotNull VirtualFile pubspecYamlFile,
                                         final @NotNull GeneralCommandLine command,
                                         final @NotNull @NlsContexts.NotificationTitle String actionTitle) {
    FileDocumentManager.getInstance().saveAllDocuments();

    try {
      if (ourInProgress.compareAndSet(false, true)) {
        command.withEnvironment(PUB_ENV_VAR_NAME, getPubEnvValue());

        final OSProcessHandler processHandler = new OSProcessHandler(command);

        processHandler.addProcessListener(new ProcessAdapter() {
          @Override
          public void processTerminated(final @NotNull ProcessEvent event) {
            ourInProgress.set(false);

            ApplicationManager.getApplication().invokeLater(() -> {
              if (!module.isDisposed()) {
                DartStartupActivity.excludeBuildAndToolCacheFolders(module, pubspecYamlFile);
                // refresh later than exclude, otherwise IDE may start indexing excluded folders
                VfsUtil.markDirtyAndRefresh(true, true, true, pubspecYamlFile.getParent());

                if (DartSdkLibUtil.isDartSdkEnabled(module)) {
                  DartAnalysisServerService.getInstance(module.getProject()).serverReadyForRequest();
                }
              }
            });
          }
        });

        showPubOutputConsole(module, command, processHandler, pubspecYamlFile, actionTitle);
      }
    }
    catch (ExecutionException e) {
      ourInProgress.set(false);

      // may be better show it in Messages tool window console?
      Notifications.Bus.notify(
        new Notification(GROUP_DISPLAY_ID, actionTitle, DartBundle.message("dart.pub.exception", e.getMessage()), NotificationType.ERROR));
    }
  }

  private static void showPubOutputConsole(@NotNull Module module,
                                           @NotNull GeneralCommandLine command,
                                           @NotNull OSProcessHandler processHandler,
                                           @NotNull VirtualFile pubspecYamlFile,
                                           @NotNull @NlsContexts.NotificationTitle String actionTitle) {
    final ConsoleView console;
    PubToolWindowContentInfo info = findExistingInfoForCommand(module.getProject(), command);

    if (info != null) {
      // rerunning the same pub command in the same tool window tab (corresponding tool window action invoked)
      console = info.console;
      console.clear();
    }
    else {
      console = createConsole(module.getProject(), pubspecYamlFile);
      info = new PubToolWindowContentInfo(module, pubspecYamlFile, command, actionTitle, console);

      final ActionToolbar actionToolbar = createToolWindowActionsBar(info);

      final SimpleToolWindowPanel toolWindowPanel = new SimpleToolWindowPanel(false, true);
      toolWindowPanel.setContent(console.getComponent());
      toolWindowPanel.setToolbar(actionToolbar.getComponent());

      final Content content = ContentFactory.SERVICE.getInstance().createContent(toolWindowPanel.getComponent(), actionTitle, true);
      content.putUserData(PUB_TOOL_WINDOW_CONTENT_INFO_KEY, info);
      Disposer.register(content, console);

      final ContentManager contentManager = MessageView.SERVICE.getInstance(module.getProject()).getContentManager();
      removeOldTabs(contentManager);
      contentManager.addContent(content);
      contentManager.setSelectedContent(content);

      final ToolWindow toolWindow = ToolWindowManager.getInstance(module.getProject()).getToolWindow(ToolWindowId.MESSAGES_WINDOW);
      toolWindow.activate(null, true);
    }

    info.rerunPubCommandAction.setProcessHandler(processHandler);
    info.stopProcessAction.setProcessHandler(processHandler);

    processHandler.addProcessListener(new ProcessAdapter() {
      @Override
      public void processTerminated(final @NotNull ProcessEvent event) {
        console.print(IdeCoreBundle.message("finished.with.exit.code.text.message", event.getExitCode()), ConsoleViewContentType.SYSTEM_OUTPUT);
      }
    });

    console.print(DartBundle.message("working.dir.0", FileUtil.toSystemDependentName(pubspecYamlFile.getParent().getPath())) + "\n",
                  ConsoleViewContentType.SYSTEM_OUTPUT);
    console.attachToProcess(processHandler);
    processHandler.startNotify();
  }

  private static @Nullable PubToolWindowContentInfo findExistingInfoForCommand(final Project project,
                                                                               final @NotNull GeneralCommandLine command) {
    for (Content content : MessageView.SERVICE.getInstance(project).getContentManager().getContents()) {
      final PubToolWindowContentInfo info = content.getUserData(PUB_TOOL_WINDOW_CONTENT_INFO_KEY);
      if (info != null && info.command == command) {
        return info;
      }
    }
    return null;
  }

  private static @NotNull ConsoleView createConsole(final @NotNull Project project, final @NotNull VirtualFile pubspecYamlFile) {
    final TextConsoleBuilder consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(project);
    consoleBuilder.setViewer(true);
    consoleBuilder.addFilter(new DartConsoleFilter(project, pubspecYamlFile));
    consoleBuilder.addFilter(new DartRelativePathsConsoleFilter(project, pubspecYamlFile.getParent().getPath()));
    consoleBuilder.addFilter(new UrlFilter());
    return consoleBuilder.getConsole();
  }

  private static @NotNull ActionToolbar createToolWindowActionsBar(final @NotNull PubToolWindowContentInfo info) {
    final DefaultActionGroup actionGroup = new DefaultActionGroup();

    final RerunPubCommandAction rerunPubCommandAction = new RerunPubCommandAction(info);
    info.rerunPubCommandAction = rerunPubCommandAction;
    actionGroup.addAction(rerunPubCommandAction);

    final StopProcessAction stopProcessAction =
      new StopProcessAction(DartBundle.message("stop.pub.process.action"), DartBundle.message("stop.pub.process.action"), null);
    info.stopProcessAction = stopProcessAction;
    actionGroup.addAction(stopProcessAction);

    actionGroup.add(ActionManager.getInstance().getAction(IdeActions.ACTION_PIN_ACTIVE_TAB));

    final AnAction closeContentAction = new CloseActiveTabAction();
    closeContentAction.getTemplatePresentation().setIcon(AllIcons.Actions.Cancel);
    closeContentAction.getTemplatePresentation().setText(UIBundle.messagePointer("tabbed.pane.close.tab.action.name"));
    actionGroup.add(closeContentAction);

    final ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("DartPubAction", actionGroup, false);
    toolbar.setTargetComponent(info.console.getComponent());
    return toolbar;
  }

  private static void removeOldTabs(final @NotNull ContentManager contentManager) {
    for (Content content : contentManager.getContents()) {
      if (!content.isPinned() && content.isCloseable() && content.getUserData(PUB_TOOL_WINDOW_CONTENT_INFO_KEY) != null) {
        contentManager.removeContent(content, false);
      }
    }
  }

  private static class PubToolWindowContentInfo {
    private final @NotNull Module module;
    private final @NotNull VirtualFile pubspecYamlFile;
    private final @NotNull GeneralCommandLine command;
    private final @NotNull @NlsContexts.NotificationTitle String actionTitle;
    private final @NotNull ConsoleView console;
    private RerunPubCommandAction rerunPubCommandAction;
    private StopProcessAction stopProcessAction;

    PubToolWindowContentInfo(@NotNull Module module,
                             @NotNull VirtualFile pubspecYamlFile,
                             @NotNull GeneralCommandLine command,
                             @NotNull @NlsContexts.NotificationTitle String actionTitle,
                             @NotNull ConsoleView console) {
      this.module = module;
      this.pubspecYamlFile = pubspecYamlFile;
      this.command = command;
      this.actionTitle = actionTitle;
      this.console = console;
    }

    @Override
    public boolean equals(final Object o) {
      return o instanceof PubToolWindowContentInfo && command == ((PubToolWindowContentInfo)o).command;
    }

    @Override
    public int hashCode() {
      return command.hashCode();
    }
  }

  private static class RerunPubCommandAction extends DumbAwareAction {
    private final @NotNull PubToolWindowContentInfo myInfo;
    private OSProcessHandler myProcessHandler;

    RerunPubCommandAction(final @NotNull PubToolWindowContentInfo info) {
      super(DartBundle.message("rerun.pub.command.action.name"),
            DartBundle.message("rerun.pub.command.action.description"),
            AllIcons.Actions.Execute);
      myInfo = info;

      registerCustomShortcutSet(CommonShortcuts.getRerun(), info.console.getComponent());
    }

    public void setProcessHandler(final @NotNull OSProcessHandler processHandler) {
      myProcessHandler = processHandler;
    }

    @Override
    public void update(final @NotNull AnActionEvent e) {
      e.getPresentation().setEnabled(!isInProgress() && myProcessHandler != null && myProcessHandler.isProcessTerminated());
    }

    @Override
    public void actionPerformed(final @NotNull AnActionEvent e) {
      doPerformPubAction(myInfo.module, myInfo.pubspecYamlFile, myInfo.command, myInfo.actionTitle);
    }
  }
}
