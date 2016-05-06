package org.angularjs;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.filters.TextConsoleBuilderImpl;
import com.intellij.execution.process.*;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.ide.projectView.actions.MarkRootActionBase;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.ide.util.projectWizard.WebProjectTemplate;
import com.intellij.lang.javascript.buildTools.npm.NpmScriptsService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.ProjectTemplate;
import com.intellij.ui.content.Content;
import icons.AngularJSIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * @author Dennis.Ushakov
 */
public class AngularCLIProjectGenerator extends WebProjectTemplate<Object> implements ProjectTemplate {
  private static final String ID = "none";
  private static final Logger LOG = Logger.getInstance(AngularCLIProjectGenerator.class);

  @Nls
  @NotNull
  @Override
  public String getName() {
    return "AngularCLI";
  }

  @Override
  public String getDescription() {
    return "The Angular2 CLI makes it easy to create an application that already works, right out of the box. It already follows Angular best practices!";
  }

  @Override
  public Icon getIcon() {
    return AngularJSIcons.AngularJS;
  }

  @Override
  public void generateProject(@NotNull Project project,
                              @NotNull VirtualFile baseDir,
                              @NotNull Object settings,
                              @NotNull Module module) {
    final String ng = "/usr/local/bin/ng";
    StartupManager.getInstance(project).runWhenProjectIsInitialized(new Runnable() {
      public void run() {
        try {
          generateApp(ng, baseDir, project, module);
          final ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
          final ContentEntry entry = MarkRootActionBase.findContentEntry(model, baseDir);
          if (entry != null) {
            entry.addExcludeFolder(baseDir.getUrl() + "/dist");
            entry.addExcludeFolder(baseDir.getUrl() + "/tmp");
          }
          ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
              model.commit();
              project.save();
            }
          });
        }
        catch (IOException e) {
          LOG.error(e);
        }
        catch (ExecutionException e) {
          LOG.error(e);
        }
      }
    });
  }

  protected void generateApp(String ng, @NotNull final VirtualFile baseDir, @NotNull Project project, Module module)
    throws IOException, ExecutionException {
    final File temp = FileUtilRt.createTempDirectory("angularCli", null);
    final GeneralCommandLine commandLine = new GeneralCommandLine(ng, "new", baseDir.getName());
    commandLine.setWorkDirectory(temp);
    final KillableColoredProcessHandler handler = new KillableColoredProcessHandler(commandLine);
    TextConsoleBuilderImpl builder = new TextConsoleBuilderImpl(project);
    builder.setUsePredefinedMessageFilter(false);
    final ConsoleView console = builder.getConsole();
    console.attachToProcess(handler);
    handler.addProcessListener(new ProcessAdapter() {
      @Override
      public void processTerminated(ProcessEvent event) {
        try {
          handler.notifyTextAvailable("\nMoving items into place...", ProcessOutputTypes.SYSTEM);
          FileUtil.copyDirContent(temp.listFiles()[0], VfsUtilCore.virtualToIoFile(baseDir));
          baseDir.refresh(false, true);
          baseDir.getChildren();
          handler.notifyTextAvailable("Done\n", ProcessOutputTypes.SYSTEM);
          final NpmScriptsService instance = NpmScriptsService.getInstance();
          for (VirtualFile file : instance.detectAllBuildfiles(project)) {
            instance.getFileManager(project).addBuildfile(file);
          }
          ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
              instance.getToolWindowManager(project).setAvailable();
            }
          });
        } catch (IOException e) {
          LOG.error(e);
        }
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
  public GeneratorPeer<Object> createPeer() {
    return new GeneratorPeer<Object>() {
      @NotNull
      @Override
      public JComponent getComponent() {
        return new JPanel();
      }

      @Override
      public void buildUI(@NotNull SettingsStep settingsStep) {
        settingsStep.addSettingsComponent(getComponent());
      }

      @NotNull
      @Override
      public Object getSettings() {
        return new Object();
      }

      @Nullable
      @Override
      public ValidationInfo validate() {
        return null;
      }

      @Override
      public boolean isBackgroundJobRunning() {
        return false;
      }

      @Override
      public void addSettingsStateListener(@NotNull SettingsStateListener listener) {
      }
    };
  }
}
