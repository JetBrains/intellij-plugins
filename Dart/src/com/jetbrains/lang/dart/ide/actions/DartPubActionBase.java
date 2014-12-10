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
import com.intellij.ide.actions.CloseActiveTabAction;
import com.intellij.ide.actions.ShowSettingsUtilImpl;
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
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
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
import com.intellij.ui.content.tabs.PinToolwindowTabAction;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartProjectComponent;
import com.jetbrains.lang.dart.ide.runner.DartConsoleFilter;
import com.jetbrains.lang.dart.ide.runner.DartRelativePathsConsoleFilter;
import com.jetbrains.lang.dart.sdk.DartConfigurable;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.jetbrains.lang.dart.util.PubspecYamlUtil.PUBSPEC_YAML;

abstract public class DartPubActionBase extends AnAction implements DumbAware {
  private static final String GROUP_DISPLAY_ID = "Dart Pub Tool";
  private static final Key<PubToolWindowContentInfo> PUB_TOOL_WINDOW_CONTENT_INFO_KEY = Key.create("PUB_TOOL_WINDOW_CONTENT_INFO_KEY");

  private static final AtomicBoolean ourInProgress = new AtomicBoolean(false);

  public DartPubActionBase() {
    super(DartIcons.Dart_16);
  }

  @Override
  public void update(@NotNull final AnActionEvent e) {
    //e.getPresentation().setText(getTitle());  "Pub: Build..." action name set in plugin.xml is different from its "Pub: Build" title
    final boolean visible = getModuleAndPubspecYamlFile(e) != null;
    e.getPresentation().setVisible(visible);
    e.getPresentation().setEnabled(visible && !ourInProgress.get());
  }

  @Nullable
  private static Pair<Module, VirtualFile> getModuleAndPubspecYamlFile(final AnActionEvent e) {
    final Module module = LangDataKeys.MODULE.getData(e.getDataContext());
    final PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(e.getDataContext());

    if (module != null && psiFile != null && psiFile.getName().equalsIgnoreCase(PUBSPEC_YAML)) {
      final VirtualFile file = psiFile.getOriginalFile().getVirtualFile();
      return file != null ? Pair.create(module, file) : null;
    }
    return null;
  }

  @NotNull
  protected abstract String getTitle(@NotNull final VirtualFile pubspecYamlFile);

  @Nullable
  protected abstract String[] calculatePubParameters(final Project project);

  @Override
  public void actionPerformed(@NotNull final AnActionEvent e) {
    final Pair<Module, VirtualFile> moduleAndPubspecYamlFile = getModuleAndPubspecYamlFile(e);
    if (moduleAndPubspecYamlFile == null) return;

    final Module module = moduleAndPubspecYamlFile.first;
    final VirtualFile pubspecYamlFile = moduleAndPubspecYamlFile.second;

    performPubAction(module, pubspecYamlFile, true);
  }

  public void performPubAction(final @NotNull Module module, final @NotNull VirtualFile pubspecYamlFile, final boolean allowModalDialogs) {
    DartSdk sdk = DartSdk.getGlobalDartSdk();

    if (sdk == null && allowModalDialogs) {
      final int answer = Messages.showDialog(module.getProject(),
                                             DartBundle.message("dart.sdk.is.not.configured"),
                                             getTitle(pubspecYamlFile),
                                             new String[]{DartBundle.message("setup.dart.sdk"), CommonBundle.getCancelButtonText()},
                                             Messages.OK,
                                             Messages.getErrorIcon());
      if (answer != Messages.OK) return;

      ShowSettingsUtilImpl.showSettingsDialog(module.getProject(), DartConfigurable.DART_SETTINGS_PAGE_ID, "");
      sdk = DartSdk.getGlobalDartSdk();
    }

    if (sdk == null) return;

    File pubFile = new File(DartSdkUtil.getPubPath(sdk));
    if (!pubFile.isFile() && allowModalDialogs) {
      final int answer =
        Messages.showDialog(module.getProject(),
                            DartBundle.message("dart.sdk.bad.dartpub.path", pubFile.getPath()),
                            getTitle(pubspecYamlFile),
                            new String[]{DartBundle.message("setup.dart.sdk"), CommonBundle.getCancelButtonText()},
                            Messages.OK,
                            Messages.getErrorIcon());
      if (answer != Messages.OK) return;

      ShowSettingsUtilImpl.showSettingsDialog(module.getProject(), DartConfigurable.DART_SETTINGS_PAGE_ID, "");

      sdk = DartSdk.getGlobalDartSdk();
      if (sdk == null) return;

      pubFile = new File(DartSdkUtil.getPubPath(sdk));
    }

    if (!pubFile.isFile()) return;

    final String[] pubParameters = calculatePubParameters(module.getProject());

    if (pubParameters != null) {
      final GeneralCommandLine command = new GeneralCommandLine().withWorkDirectory(pubspecYamlFile.getParent().getPath());
      command.setExePath(pubFile.getPath());
      command.addParameters(pubParameters);

      doPerformPubAction(module, pubspecYamlFile, command, getTitle(pubspecYamlFile));
    }
  }

  private static void doPerformPubAction(@NotNull final Module module,
                                         @NotNull final VirtualFile pubspecYamlFile,
                                         @NotNull final GeneralCommandLine command,
                                         @NotNull final String actionTitle) {
    FileDocumentManager.getInstance().saveAllDocuments();

    try {
      if (ourInProgress.compareAndSet(false, true)) {
        final OSProcessHandler processHandler = new OSProcessHandler(command);

        processHandler.addProcessListener(new ProcessAdapter() {
          @Override
          public void processTerminated(final ProcessEvent event) {
            ourInProgress.set(false);

            ApplicationManager.getApplication().invokeLater(new Runnable() {
              public void run() {
                DartProjectComponent.excludeBuildAndPackagesFolders(module, pubspecYamlFile);
                // refresh later than exclude, otherwise IDE may start indexing excluded folders
                VfsUtil.markDirtyAndRefresh(true, true, true, pubspecYamlFile.getParent());
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

  private static void showPubOutputConsole(@NotNull final Module module,
                                           @NotNull final GeneralCommandLine command,
                                           @NotNull final OSProcessHandler processHandler,
                                           @NotNull final VirtualFile pubspecYamlFile,
                                           @NotNull final String actionTitle) {
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
      public void processTerminated(final ProcessEvent event) {
        console.print(IdeBundle.message("finished.with.exit.code.text.message", event.getExitCode()), ConsoleViewContentType.SYSTEM_OUTPUT);
      }
    });

    console.print(DartBundle.message("working.dir.0", FileUtil.toSystemDependentName(pubspecYamlFile.getParent().getPath())) + "\n",
                  ConsoleViewContentType.SYSTEM_OUTPUT);
    console.attachToProcess(processHandler);
    processHandler.startNotify();
  }

  @Nullable
  private static PubToolWindowContentInfo findExistingInfoForCommand(final Project project, @NotNull final GeneralCommandLine command) {
    for (Content content : MessageView.SERVICE.getInstance(project).getContentManager().getContents()) {
      final PubToolWindowContentInfo info = content.getUserData(PUB_TOOL_WINDOW_CONTENT_INFO_KEY);
      if (info != null && info.command == command) {
        return info;
      }
    }
    return null;
  }

  @NotNull
  private static ConsoleView createConsole(@NotNull final Project project, @NotNull final VirtualFile pubspecYamlFile) {
    final TextConsoleBuilder consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(project);
    consoleBuilder.setViewer(true);
    consoleBuilder.addFilter(new DartConsoleFilter(project, pubspecYamlFile));
    consoleBuilder.addFilter(new DartRelativePathsConsoleFilter(project, pubspecYamlFile.getParent().getPath()));
    consoleBuilder.addFilter(new UrlFilter());
    return consoleBuilder.getConsole();
  }

  @NotNull
  private static ActionToolbar createToolWindowActionsBar(@NotNull final PubToolWindowContentInfo info) {
    final DefaultActionGroup actionGroup = new DefaultActionGroup();

    final RerunPubCommandAction rerunPubCommandAction = new RerunPubCommandAction(info);
    info.rerunPubCommandAction = rerunPubCommandAction;
    actionGroup.addAction(rerunPubCommandAction);

    final StopProcessAction stopProcessAction = new StopProcessAction(DartBundle.message("stop.pub.process.action"),
                                                                      DartBundle.message("stop.pub.process.action"),
                                                                      null);
    info.stopProcessAction = stopProcessAction;
    actionGroup.addAction(stopProcessAction);

    actionGroup.add(PinToolwindowTabAction.getPinAction());

    final AnAction closeContentAction = new CloseActiveTabAction();
    closeContentAction.getTemplatePresentation().setIcon(AllIcons.Actions.Cancel);
    closeContentAction.getTemplatePresentation().setText(UIBundle.message("tabbed.pane.close.tab.action.name"));
    actionGroup.add(closeContentAction);

    final ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, actionGroup, false);
    toolbar.setTargetComponent(info.console.getComponent());
    return toolbar;
  }

  private static void removeOldTabs(@NotNull final ContentManager contentManager) {
    for (Content content : contentManager.getContents()) {
      if (!content.isPinned() && content.isCloseable() && content.getUserData(PUB_TOOL_WINDOW_CONTENT_INFO_KEY) != null) {
        contentManager.removeContent(content, false);
      }
    }
  }

  private static class PubToolWindowContentInfo {
    private @NotNull final Module module;
    private @NotNull final VirtualFile pubspecYamlFile;
    private @NotNull final GeneralCommandLine command;
    private @NotNull final String actionTitle;
    private @NotNull final ConsoleView console;
    private RerunPubCommandAction rerunPubCommandAction;
    private StopProcessAction stopProcessAction;

    public PubToolWindowContentInfo(@NotNull final Module module,
                                    @NotNull final VirtualFile pubspecYamlFile,
                                    @NotNull final GeneralCommandLine command,
                                    @NotNull final String actionTitle,
                                    @NotNull final ConsoleView console) {
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
    @NotNull private final PubToolWindowContentInfo myInfo;
    private OSProcessHandler myProcessHandler;

    public RerunPubCommandAction(@NotNull final PubToolWindowContentInfo info) {
      super(DartBundle.message("rerun.pub.command.action.name"),
            DartBundle.message("rerun.pub.command.action.description"),
            AllIcons.Actions.Execute);
      myInfo = info;

      registerCustomShortcutSet(CommonShortcuts.getRerun(), info.console.getComponent());
    }

    public void setProcessHandler(@NotNull final OSProcessHandler processHandler) {
      myProcessHandler = processHandler;
    }

    @Override
    public void update(@NotNull final AnActionEvent e) {
      e.getPresentation().setEnabled(!ourInProgress.get() && myProcessHandler != null && myProcessHandler.isProcessTerminated());
    }

    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {
      doPerformPubAction(myInfo.module, myInfo.pubspecYamlFile, myInfo.command, myInfo.actionTitle);
    }
  }
}
