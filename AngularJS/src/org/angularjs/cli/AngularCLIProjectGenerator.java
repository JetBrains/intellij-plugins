package org.angularjs.cli;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.filters.TextConsoleBuilderImpl;
import com.intellij.execution.process.KillableColoredProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.ide.projectView.actions.MarkRootActionBase;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.ide.util.projectWizard.WebProjectTemplate;
import com.intellij.javascript.nodejs.CompletionModuleInfo;
import com.intellij.javascript.nodejs.NodeModuleSearchUtil;
import com.intellij.javascript.nodejs.NodeSettings;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterField;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager;
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter;
import com.intellij.lang.javascript.buildTools.npm.NpmScriptsService;
import com.intellij.lang.javascript.modules.ConsoleProgress;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.ProjectTemplate;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;
import com.intellij.ui.content.Content;
import com.intellij.util.NotNullProducer;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.SwingHelper;
import com.intellij.util.ui.UIUtil;
import icons.AngularJSIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class AngularCLIProjectGenerator extends WebProjectTemplate<Pair<NodeJsInterpreter,String>> implements ProjectTemplate {
  private static final String ID = "none";
  private static final Logger LOG = Logger.getInstance(AngularCLIProjectGenerator.class);

  @Nls
  @NotNull
  @Override
  public String getName() {
    return "Angular CLI";
  }

  @Override
  public String getDescription() {
    return "The Angular2 CLI makes it easy to create an application that already works, right out of the box. It already follows Angular best practices!";
  }

  @Override
  public Icon getIcon() {
    return AngularJSIcons.Angular2;
  }

  @Override
  public void generateProject(@NotNull Project project,
                              @NotNull VirtualFile baseDir,
                              @NotNull Pair<NodeJsInterpreter,String> settings,
                              @NotNull Module module) {
    final NodeJsInterpreter interpreter = settings.first;
    final NodeJsLocalInterpreter local = NodeJsLocalInterpreter.tryCast(interpreter);
    assert local != null;

    StartupManager.getInstance(project).runWhenProjectIsInitialized(() -> {
      try {
        generateApp(local, settings.second, baseDir, project);
        final ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
        final ContentEntry entry = MarkRootActionBase.findContentEntry(model, baseDir);
        if (entry != null) {
          entry.addExcludeFolder(baseDir.getUrl() + "/dist");
        }
        ApplicationManager.getApplication().runWriteAction(() -> {
          model.commit();
          project.save();
        });
      }
      catch (IOException e) {
        LOG.error(e);
      }
      catch (ExecutionException e) {
        LOG.error(e);
      }
    });
  }

  protected void generateApp(@NotNull NodeJsLocalInterpreter node, String path,
                             @NotNull final VirtualFile baseDir, @NotNull Project project)
    throws IOException, ExecutionException {
    final String moduleExe = path + File.separator + "bin" + File.separator + "ng";
    final GeneralCommandLine commandLine = new GeneralCommandLine(node.getInterpreterSystemDependentPath(),
                                                                  moduleExe,
                                                                  "init", "--name=" + baseDir.getName());
    commandLine.setWorkDirectory(baseDir.getPath());
    final KillableColoredProcessHandler handler = new KillableColoredProcessHandler(commandLine);
    TextConsoleBuilderImpl builder = new TextConsoleBuilderImpl(project);
    builder.setUsePredefinedMessageFilter(false);
    builder.addFilter(new AngularCLIFilter(project, baseDir.getPath()));

    final ConsoleView console = builder.getConsole();
    console.attachToProcess(handler);
    ConsoleProgress.install(console, handler);
    handler.addProcessListener(new ProcessAdapter() {
      @Override
      public void processTerminated(ProcessEvent event) {
        baseDir.refresh(false, true);
        baseDir.getChildren();
        handler.notifyTextAvailable("Done\n", ProcessOutputTypes.SYSTEM);
        ApplicationManager.getApplication().runReadAction(() -> {
          if (!project.isDisposed()) {
            NpmScriptsService instance = NpmScriptsService.getInstance();
            List<VirtualFile> buildfiles = instance.detectAllBuildfiles(project);
            ApplicationManager.getApplication().invokeLater(() -> {
              for (VirtualFile buildfile : buildfiles) {
                instance.getFileManager(project).addBuildfile(buildfile);
              }
              NodeJsInterpreterManager.getInstance(project).setDefault(node);
              instance.getToolWindowManager(project).setAvailable();
            });
          }
        });
      }
    });
    final Executor defaultExecutor = DefaultRunExecutor.getRunExecutorInstance();

    final String title = "Generating " + baseDir.getName();
    final RunnerLayoutUi ui = RunnerLayoutUi.Factory.getInstance(project).create(ID, title, title, project);
    final Content consoleContent = ui.createContent(ID,
                                                    console.getComponent(),
                                                    title, null,
                                                    console.getPreferredFocusableComponent());
    ui.addContent(consoleContent);

    final RunContentDescriptor descriptor = new RunContentDescriptor(console, handler, console.getComponent(), title);
    ExecutionManager.getInstance(project).getContentManager().showRunContent(defaultExecutor, descriptor);
    handler.startNotify();
  }

  @NotNull
  @Override
  public GeneratorPeer<Pair<NodeJsInterpreter,String>> createPeer() {
    return new GeneratorPeer<Pair<NodeJsInterpreter,String>>() {
      private NodeJsInterpreterField myInterpreter;
      private TextFieldWithHistoryWithBrowseButton myModule;

      @NotNull
      @Override
      public JComponent getComponent() {
        final Project project = ProjectManager.getInstance().getDefaultProject();
        final JPanel panel = new JPanel(new VerticalFlowLayout());

        myInterpreter = new NodeJsInterpreterField(project, false) {
          @Override
          public boolean isDefaultProjectInterpreterField() {
            return true;
          }
        };
        myInterpreter.setInterpreter(NodeJsInterpreterManager.getInstance(project).getDefault());
        LabeledComponent component = LabeledComponent.create(myInterpreter, "Node &interpreter:");
        component.setLabelLocation(BorderLayout.WEST);
        panel.add(component);

        final NotNullProducer<List<String>> producer = () -> {
          List<String> dirs = new ArrayList<String>();
          final NodeJsLocalInterpreter local = NodeJsLocalInterpreter.tryCast(myInterpreter.getInterpreter());
          List<CompletionModuleInfo> modules = ContainerUtil.newArrayList();
          NodeModuleSearchUtil.findModulesWithName(modules,
                                                   "angular-cli",
                                                   null,
                                                   NodeSettings.create(local),
                                                   true);
          for (CompletionModuleInfo module : modules) {
            VirtualFile dir = module.getVirtualFile();
            if (dir != null && dir.isDirectory()) {
              dirs.add(FileUtil.toSystemDependentName(dir.getPath()));
            }
          }
          Collections.sort(dirs);
          return dirs;
        };
        myModule = SwingHelper.createTextFieldWithHistoryWithBrowseButton(project, "Select angular-cli package",
          FileChooserDescriptorFactory.createSingleFolderDescriptor(), producer);
        final String item = ContainerUtil.getFirstItem(producer.produce());
        if (item != null) myModule.setText(item);
        component = LabeledComponent.create(myModule, "_Angular CLI:");
        component.setAnchor((JComponent)panel.getComponent(0));
        component.setLabelLocation(BorderLayout.WEST);
        panel.add(component);

        return panel;
      }

      @Override
      public void buildUI(@NotNull SettingsStep settingsStep) {
        getComponent();
        settingsStep.addSettingsField(UIUtil.replaceMnemonicAmpersand("Node &interpreter:"), myInterpreter);
        settingsStep.addSettingsField(UIUtil.replaceMnemonicAmpersand("Angular &CLI:"), myModule);
      }

      @NotNull
      @Override
      public Pair<NodeJsInterpreter,String> getSettings() {
        return Pair.create(myInterpreter.getInterpreter(), myModule.getText());
      }

      @Nullable
      @Override
      public ValidationInfo validate() {
        final NodeJsInterpreter interpreter = myInterpreter.getInterpreter();
        final String error = NodeJsLocalInterpreter.getErrorMessage(interpreter);
        if (error != null) {
          return new ValidationInfo(error);
        }
        final String packageDir = myModule.getText();
        if (StringUtil.isEmptyOrSpaces(packageDir)) {
          return new ValidationInfo("Please specify angular-cli package");
        }
        File file = new File(packageDir);
        if (!file.isAbsolute() || !file.isDirectory()) {
          return new ValidationInfo("Please specify angular-cli package correctly");
        }
        return null;
      }

      @Override
      public boolean isBackgroundJobRunning() {
        return false;
      }

      @Override
      public void addSettingsStateListener(@NotNull SettingsStateListener listener) {
        myInterpreter.addChangeListener((interpreter) -> listener.stateChanged(validate() == null));
        myModule.getChildComponent().addItemListener((module) -> listener.stateChanged(validate() == null));
        listener.stateChanged(validate() == null);
      }
    };
  }
}
