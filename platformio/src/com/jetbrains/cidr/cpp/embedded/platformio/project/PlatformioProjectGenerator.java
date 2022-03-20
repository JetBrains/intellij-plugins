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
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
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
import com.jetbrains.cidr.cpp.embedded.platformio.CustomTool;
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioBaseConfiguration;
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioConfigurationType;
import com.jetbrains.cidr.cpp.execution.CMakeBuildConfigurationHelper;
import com.jetbrains.cidr.cpp.execution.CMakeRunConfigurationType;
import com.jetbrains.cidr.execution.BuildTargetAndConfigurationData;
import com.jetbrains.cidr.execution.BuildTargetData;
import com.jetbrains.cidr.execution.ExecutableData;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.util.List;

import static com.intellij.execution.configurations.ConfigurationTypeUtil.findConfigurationType;
import static com.intellij.notification.NotificationType.WARNING;
import static com.intellij.openapi.actionSystem.impl.SimpleDataContext.getProjectContext;
import static com.intellij.openapi.application.ApplicationManager.getApplication;
import static com.intellij.openapi.progress.PerformInBackgroundOption.DEAF;
import static com.intellij.openapi.vfs.VfsUtilCore.virtualToIoFile;
import static com.intellij.util.ExceptionUtil.getThrowableText;
import static com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspaceListener.TOPIC;
import static com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle.message;
import static com.jetbrains.cidr.cpp.embedded.platformio.PlatformioBaseConfiguration.findPlatformio;
import static com.jetbrains.cidr.cpp.embedded.platformio.project.BoardInfo.EMPTY;
import static com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioService.NOTIFICATION_GROUP;
import static com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioService.notifyPlatformioNotFound;
import static com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioActionBase.FUS_COMMAND.CREATE_PROJECT;
import static com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioActionBase.fusLog;
import static com.jetbrains.cidr.cpp.embedded.stm32cubemx.CMakeSTM32CubeMXProjectGenerator.EMBEDDED_PROJECTS_GROUP_DISPLAY_NAME;
import static com.jetbrains.cidr.cpp.embedded.stm32cubemx.CMakeSTM32CubeMXProjectGenerator.EMBEDDED_PROJECTS_GROUP_NAME;
import static icons.ClionEmbeddedPlatformioIcons.Platformio;

public class PlatformioProjectGenerator extends CLionProjectGenerator<Ref<BoardInfo>>
  implements CustomStepProjectGenerator<Ref<BoardInfo>> {

  @Override
  public AbstractActionWithPanel createStep(
          final @NotNull DirectoryProjectGenerator<Ref<BoardInfo>> projectGenerator,
          final @NotNull AbstractNewProjectStep.AbstractCallback<Ref<BoardInfo>> callback) {
    return new PlatformioProjectSettingsStep(projectGenerator, callback);
  }

  @Nullable
  @Override
  public Icon getLogo() {
    return Platformio;
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
    return message("platformio.project.type");
  }

  @Override
  public String getDescription() {
    return message("platformio.project.description");
  }

  @NotNull
  @Override
  public ProjectGeneratorPeer<Ref<BoardInfo>> createPeer() {
    return new GeneratorPeerImpl<>(new Ref<>(EMPTY), new JPanel());
  }

  @Override
  public void generateProject(final @NotNull Project project,
                              final @NotNull VirtualFile baseDir,
                              final @NotNull Ref<BoardInfo> settings,
                              final @NotNull Module module) {
    super.generateProject(project, baseDir, settings, module);
    final var pioCmdLineTail = new StringBuilder();
    for (final var s : settings.get().getParameters()) {
      pioCmdLineTail.append(' ').append(s);
    }
    doGenerateProject(project, baseDir, pioCmdLineTail, settings.get().getTemplate());
  }

  public void doGenerateProject(final @NotNull Project project,
                                final @NotNull VirtualFile baseDir,
                                final @NotNull CharSequence pioCmdLineTail,
                                final @NotNull SourceTemplate template) {
  /* This method starts multi-stage process
     1. PlatformIO utility is started asynchronously under progress indicator
     2. When it's done, another asynchronous code writes empty source code stub if no main.c or main.cpp is generated
     3. When it's done, CMake workspace is initialized asynchronously, and a listener is set to watch the process
     4. When it's done, CMake run configurations and build profiles are created
   */
    final var myPioUtility = findPlatformio();
    if (myPioUtility == null) {
      notifyPlatformioNotFound(project);
      return;
    }
    fusLog(null, CREATE_PROJECT);
    final var initTool = new CustomTool(message("platformio.init.title"));
    initTool.setProgram(myPioUtility);
    initTool.setWorkingDirectory(baseDir.getCanonicalPath());
    initTool.setParameters("init --ide clion" + pioCmdLineTail);
    final var projectContext = getProjectContext(project);
    final var semaphore = new Semaphore(1);
    final var success = new Ref<Boolean>(false);
    final ProcessAdapter processListener = new ProcessAdapter() {
      @Override
      public void processTerminated(@NotNull ProcessEvent event) {
        success.set(event.getExitCode() == 0);
        semaphore.up();
      }
    };
    if (initTool.executeIfPossible(null, projectContext, -1, processListener)) {
      new Task.Backgroundable(project, message("initializing"), true, DEAF) {
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

  private void finishFileStructure(final @NotNull Project project,
                                   final @NotNull VirtualFile baseDir,
                                   final @NotNull SourceTemplate template) {
    baseDir.refresh(false, true);
    if (template != SourceTemplate.NONE) {
      final var srcFolder = baseDir.findChild("src");
      if (srcFolder == null || !srcFolder.isDirectory()) {
        showError(message("src.not.found"));
        return;
      }
      if (srcFolder.findChild("main.cpp") == null && srcFolder.findChild("main.c") == null) {
        try {
          final var virtualFile = srcFolder.createChildData(this, template.getFileName());
          virtualFile.setBinaryContent(template.getContent());
          getApplication().invokeLater(() -> {
            if (!project.isDisposed()) {
              final var descriptor = new OpenFileDescriptor(project, virtualFile);
              FileEditorManager.getInstance(project).openEditor(descriptor, true);
            }
          });
        }
        catch (final IOException e) {
          showError(getThrowableText(e));
          return;
        }
      }
    }
    updateCMakeProjectInformation(project, baseDir);
  }

  public static void updateCMakeProjectInformation(@NotNull Project project, @NotNull VirtualFile baseDir) {
    if (project.isInitialized()) {
      final var cmakeWorkspace = CMakeWorkspace.getInstance(project);
      final var busConnection = project.getMessageBus().connect();
      busConnection.subscribe(TOPIC, new CMakeWorkspaceListener() {

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
      getApplication().invokeLaterOnWriteThread(
        () -> cmakeWorkspace.selectProjectDir(virtualToIoFile(baseDir)) //Phase 3 started
      );
    }
  }

  private static void configureRunConfigurations(@NotNull Project project) {
    final var runManager = RunManager.getInstance(project);
    final CMakeBuildConfigurationHelper helper = CMakeRunConfigurationType.getHelper(project);

    final var factories = findConfigurationType(PlatformioConfigurationType.class).getNewProjectFactories();
    for (int i = 0; i < factories.length; i++) {
      final var factory = factories[i];
      String name = factory.getName();
      if (runManager.findConfigurationByName(name) == null) {
        final var runSettings = runManager.createConfiguration(name, factory);

        final var configuration = (PlatformioBaseConfiguration)runSettings.getConfiguration();
        final var target = helper.findFirstSuitableTarget(configuration.getCmakeBuildTarget());
        if (target != null) {
          final var buildTargetData = new BuildTargetData(project.getName(), target.getName());
          final var data = new BuildTargetAndConfigurationData(buildTargetData, null);
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

  private static void configureBuildTypes(final @NotNull CMakeWorkspace cmakeWorkspace) {
    final var settings = cmakeWorkspace.getSettings();
    final var cMakeModelConfigurationData = cmakeWorkspace.getModelConfigurationData();
    if (!cMakeModelConfigurationData.isEmpty()) {
      final var buildTypes = cMakeModelConfigurationData.get(0).getRegisteredBuildTypes();
      settings.setProfiles(ContainerUtil.map(buildTypes, CMakeSettings.Profile::new));
    }
  }

  private static void showError(final @NotNull @NlsContexts.NotificationContent String message) {
    NOTIFICATION_GROUP.createNotification(message("project.init.failed"), message, WARNING).notify(null);
  }
}
