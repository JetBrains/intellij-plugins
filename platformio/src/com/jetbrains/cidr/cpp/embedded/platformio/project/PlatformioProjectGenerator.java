package com.jetbrains.cidr.cpp.embedded.platformio.project;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.ide.util.projectWizard.AbstractNewProjectStep;
import com.intellij.ide.util.projectWizard.CustomStepProjectGenerator;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.impl.welcomeScreen.AbstractActionWithPanel;
import com.intellij.platform.DirectoryProjectGenerator;
import com.intellij.platform.GeneratorPeerImpl;
import com.intellij.platform.ProjectGeneratorPeer;
import com.intellij.util.ExceptionUtil;
import com.intellij.util.concurrency.Semaphore;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.messages.MessageBusConnection;
import com.jetbrains.cidr.cpp.cmake.CMakeSettings;
import com.jetbrains.cidr.cpp.cmake.model.CMakeModelConfigurationData;
import com.jetbrains.cidr.cpp.cmake.model.CMakeTarget;
import com.jetbrains.cidr.cpp.cmake.projectWizard.generators.CLionProjectGenerator;
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace;
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspaceListener;
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle;
import com.jetbrains.cidr.cpp.embedded.platformio.CustomTool;
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioBaseConfiguration;
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioConfigurationType;
import com.jetbrains.cidr.cpp.execution.CMakeBuildConfigurationHelper;
import com.jetbrains.cidr.cpp.execution.CMakeRunConfigurationType;
import com.jetbrains.cidr.execution.BuildTargetAndConfigurationData;
import com.jetbrains.cidr.execution.BuildTargetData;
import com.jetbrains.cidr.execution.ExecutableData;
import icons.ClionEmbeddedPlatformioIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.util.List;

import static com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioActionBase.FUS_COMMAND.CREATE_PROJECT;
import static com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioActionBase.fusLog;
import static com.jetbrains.cidr.cpp.embedded.stm32cubemx.CMakeSTM32CubeMXProjectGenerator.EMBEDDED_PROJECTS_GROUP_DISPLAY_NAME;
import static com.jetbrains.cidr.cpp.embedded.stm32cubemx.CMakeSTM32CubeMXProjectGenerator.EMBEDDED_PROJECTS_GROUP_NAME;

public class PlatformioProjectGenerator extends CLionProjectGenerator<Ref<BoardInfo>>
  implements CustomStepProjectGenerator<Ref<BoardInfo>> {

  @Override
  public AbstractActionWithPanel createStep(DirectoryProjectGenerator<Ref<BoardInfo>> projectGenerator,
                                            AbstractNewProjectStep.AbstractCallback<Ref<BoardInfo>> callback) {
    return new PlatformioProjectSettingsStep(projectGenerator, callback);
  }

  @Nullable
  @Override
  public Icon getLogo() {
    return ClionEmbeddedPlatformioIcons.Platformio;
  }

  @NotNull
  @Override
  public String getGroupName() {
    return EMBEDDED_PROJECTS_GROUP_NAME;
  }

  @Override
  public int getGroupOrder() {
    return GroupOrders.EMBEDDED.order + 1;
  }

  @Nls
  @NotNull
  @Override
  public String getGroupDisplayName() {
    return EMBEDDED_PROJECTS_GROUP_DISPLAY_NAME.get();
  }

  @NotNull
  @Override
  public String getName() {
    return ClionEmbeddedPlatformioBundle.message("platformio.project.type");
  }

  @Override
  public String getDescription() {
    return ClionEmbeddedPlatformioBundle.message("platformio.project.description");
  }

  @NotNull
  @Override
  public ProjectGeneratorPeer<Ref<BoardInfo>> createPeer() {
    return new GeneratorPeerImpl<>(new Ref<>(BoardInfo.EMPTY), new JPanel());
  }

  @Override
  public void generateProject(@NotNull Project project,
                              @NotNull VirtualFile baseDir,
                              @NotNull Ref<@NotNull BoardInfo> settings,
                              @NotNull Module module) {
    super.generateProject(project, baseDir, settings, module);
    StringBuilder pioCmdLineTail = new StringBuilder();
    for (String s : settings.get().getParameters()) {
      pioCmdLineTail.append(' ').append(s);
    }
    doGenerateProject(project, baseDir, pioCmdLineTail, settings.get().getTemplate());
  }

  public void doGenerateProject(@NotNull Project project,
                                @NotNull VirtualFile baseDir,
                                @NotNull CharSequence pioCmdLineTail,
                                @NotNull SourceTemplate template) {
  /* This method starts multi-stage process
     1. PlatformIO utility is started asynchronously under progress indicator
     2. When it's done, another asynchronous code writes empty source code stub if no main.c or main.cpp is generated
     3. When it's done, CMake workspace is initialized asynchronously, and a listener is set to watch the process
     4. When it's done, CMake run configurations and build profiles are created
   */
    String myPioUtility = PlatformioBaseConfiguration.findPlatformio();
    if (myPioUtility == null) {
      PlatformioService.notifyPlatformioNotFound(project);
      return;
    }
    fusLog(null, CREATE_PROJECT);
    CustomTool initTool = new CustomTool(ClionEmbeddedPlatformioBundle.message("platformio.init.title"));
    initTool.setProgram(myPioUtility);
    initTool.setWorkingDirectory(baseDir.getCanonicalPath());
    initTool.setParameters("init --ide clion" + pioCmdLineTail);
    DataContext projectContext = SimpleDataContext.getProjectContext(project);
    Semaphore semaphore = new Semaphore(1);
    Ref<Boolean> success = new Ref<>(false);
    ProcessAdapter processListener = new ProcessAdapter() {
      @Override
      public void processTerminated(@NotNull ProcessEvent event) {
        success.set(event.getExitCode() == 0);
        semaphore.up();
      }
    };
    if (initTool.executeIfPossible(null, projectContext, -1, processListener)) {
      new Task.Backgroundable(project, ClionEmbeddedPlatformioBundle.message("initializing"), true, PerformInBackgroundOption.DEAF) {
        @Override
        public void run(@NotNull ProgressIndicator indicator) {
          while (!semaphore.waitFor(200)) {
            indicator.checkCanceled();
          }
          baseDir.refresh(false, true);
        }

        @Override
        public void onFinished() {
          if (success.get()) {
            // Phase 2 started
            WriteAction.run(() -> finishFileStructure(project, baseDir, template));
          }
        }
      }.queue(); // Phase 1 started
    }
  }

  private void finishFileStructure(@NotNull Project project,
                                   @NotNull VirtualFile baseDir,
                                   @NotNull SourceTemplate template) {
    baseDir.refresh(false, true);
    if (template != SourceTemplate.NONE) {
      VirtualFile srcFolder = baseDir.findChild("src");
      if (srcFolder == null || !srcFolder.isDirectory()) {
        showError(ClionEmbeddedPlatformioBundle.message("src.not.found"));
        return;
      }
      if (srcFolder.findChild("main.cpp") == null && srcFolder.findChild("main.c") == null) {
        try {
          VirtualFile virtualFile = srcFolder.createChildData(this, template.getFileName());
          virtualFile.setBinaryContent(template.getContent());
          ApplicationManager.getApplication().invokeLater(() -> {
            if (!project.isDisposed()) {
              OpenFileDescriptor descriptor = new OpenFileDescriptor(project, virtualFile);
              FileEditorManager.getInstance(project).openEditor(descriptor, true);
            }
          });
        }
        catch (IOException e) {
          showError(ExceptionUtil.getThrowableText(e));
          return;
        }
      }
    }
    updateCMakeProjectInformation(project, baseDir);
  }

  public static void updateCMakeProjectInformation(@NotNull Project project, @NotNull VirtualFile baseDir) {
    if (project.isInitialized()) {
      CMakeWorkspace cmakeWorkspace = CMakeWorkspace.getInstance(project);
      MessageBusConnection busConnection = project.getMessageBus().connect();
      busConnection.subscribe(CMakeWorkspaceListener.TOPIC, new CMakeWorkspaceListener() {

        @Override
        public void reloadingFinished(boolean canceled) {
          busConnection.disconnect();
          if (!canceled && project.isInitialized()) {
            //Phase 4
            configureBuildTypes(cmakeWorkspace);
            configureRunConfigurations(project);
          }
        }
      });
      ApplicationManager.getApplication().invokeLaterOnWriteThread(
        () -> cmakeWorkspace.selectProjectDir(VfsUtilCore.virtualToIoFile(baseDir)) //Phase 3 started
      );
    }
  }

  private static void configureRunConfigurations(@NotNull Project project) {
    RunManager runManager = RunManager.getInstance(project);

    final CMakeBuildConfigurationHelper helper = CMakeRunConfigurationType.getHelper(project);

    ConfigurationFactory[] factories =
      ConfigurationTypeUtil.findConfigurationType(PlatformioConfigurationType.class).getNewProjectFactories();
    for (int i = 0; i < factories.length; i++) {
      ConfigurationFactory factory = factories[i];
      String name = factory.getName();
      if (runManager.findConfigurationByName(name) == null) {
        RunnerAndConfigurationSettings runSettings = runManager.createConfiguration(name, factory);

        PlatformioBaseConfiguration configuration = (PlatformioBaseConfiguration)runSettings.getConfiguration();
        CMakeTarget target = helper.findFirstSuitableTarget(configuration.getCmakeBuildTarget());
        if (target != null) {
          final BuildTargetData buildTargetData = new BuildTargetData(project.getName(), target.getName());
          final BuildTargetAndConfigurationData data = new BuildTargetAndConfigurationData(buildTargetData, null);
          configuration.setTargetAndConfigurationData(data);
          configuration.setExecutableData(new ExecutableData(buildTargetData));
          runManager.addConfiguration(runSettings);
          if (i == 0) {
            runManager.setSelectedConfiguration(runSettings);
          }
        }
      }
    }
  }

  private static void configureBuildTypes(@NotNull CMakeWorkspace cmakeWorkspace) {
    CMakeSettings settings = cmakeWorkspace.getSettings();
    List<CMakeModelConfigurationData> cMakeModelConfigurationData = cmakeWorkspace.getModelConfigurationData();
    if (!cMakeModelConfigurationData.isEmpty()) {
      List<String> buildTypes = cMakeModelConfigurationData.get(0).getRegisteredBuildTypes();
      settings.setProfiles(ContainerUtil.map(buildTypes, CMakeSettings.Profile::new));
    }
  }

  private static void showError(@NotNull @NlsContexts.NotificationContent String message) {
    PlatformioService.NOTIFICATION_GROUP
      .createNotification(ClionEmbeddedPlatformioBundle.message("project.init.failed"), message, NotificationType.WARNING)
      .notify(null);
  }
}
