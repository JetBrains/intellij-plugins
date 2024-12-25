package com.jetbrains.plugins.meteor.projectGenerator;

import com.dmarcotte.handlebars.config.HbConfig;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.execution.util.ExecUtil;
import com.intellij.ide.util.projectWizard.WebProjectTemplate;
import com.intellij.lang.javascript.boilerplate.GithubDownloadUtil;
import com.intellij.lang.javascript.boilerplate.GithubProjectGeneratorPeer;
import com.intellij.lang.javascript.dialects.JSLanguageLevel;
import com.intellij.lang.javascript.settings.JSRootConfiguration;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.templates.github.ZipUtil;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.plugins.meteor.MeteorBundle;
import com.jetbrains.plugins.meteor.MeteorFacade;
import com.jetbrains.plugins.meteor.runner.MeteorConfigurationType;
import com.jetbrains.plugins.meteor.runner.MeteorRunConfiguration;
import com.jetbrains.plugins.meteor.settings.MeteorSettings;
import icons.MeteorIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.jetbrains.plugins.meteor.MeteorProjectStartupActivityKt.initMeteorProject;

final class MeteorProjectTemplateGenerator extends WebProjectTemplate<MeteorProjectTemplateGenerator.MeteorProjectSettings> {
  private static final Logger LOG = Logger.getInstance(MeteorProjectTemplateGenerator.class);
  public static final String DEFAULT_TEMPLATE_NAME = "hello";

  @Override
  public String getId() {
    return "Meteor";
  }

  @Override
  public @Nls @NotNull String getName() {
    return MeteorBundle.message("settings.meteor.project.generator.name");
  }

  @Override
  public String getDescription() {
    return MeteorBundle.message("settings.meteor.project.generator.descr");
  }

  private static void setProjectLanguageLevel(final @NotNull Project project, MeteorProjectSettings settings) {
    String type = settings.myType;
    JSLanguageLevel level = JSLanguageLevel.ES6;
    if (type.contains("react")) {
      level = JSLanguageLevel.getLevelForJSX();
    }
    JSRootConfiguration.getInstance(project).storeLanguageLevelAndUpdateCaches(level);
  }

  private static void setHandlebarsSettings(final @NotNull Project project, MeteorProjectSettings settings) {
    if (settings.myType.contains("angular")) {
      HbConfig.setShouldOpenHtmlAsHandlebars(false, project);
    }
  }

  @Override
  public void generateProject(final @NotNull Project project,
                              final @NotNull VirtualFile baseDir,
                              final @NotNull MeteorProjectSettings settings,
                              @NotNull Module module) {
    final Ref<Boolean> noErrorOnProjectCreating = Ref.create(false);
    setProjectLanguageLevel(project, settings);
    setHandlebarsSettings(project, settings);
    MeteorFacade.getInstance().setIsMeteorProject(project);

    if (MeteorProjectPeer.EMPTY_PROJECT_TYPE.equals(settings.myType)) {
      createDefaultProject(project, baseDir, settings, noErrorOnProjectCreating);
    }
    else {
      createGithubProject(project, baseDir, settings, noErrorOnProjectCreating);
    }

    ApplicationManager.getApplication().runWriteAction(() -> {
      if (noErrorOnProjectCreating.get()) {
        initMeteorProject(project, false);
        createRunConfiguration(project, baseDir);
        if (!StringUtil.equals(settings.myMeteorExecutablePath, MeteorSettings.getInstance().getExecutablePath())) {
          MeteorSettings.getInstance().setExecutablePath(settings.myMeteorExecutablePath);
        }
      }
      baseDir.refresh(true, true);
    });
  }

  public static void createDefaultProject(final @NotNull Project project,
                                          final @NotNull VirtualFile baseDir,
                                          final @NotNull MeteorProjectSettings settings,
                                          final Ref<Boolean> noErrorOnProjectCreating) {
    ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {

      try {

        ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
        indicator.setText(MeteorBundle.message("progress.text.creating.meteor.project"));
        executeCommandLineForDefaultProject(indicator, settings, baseDir, noErrorOnProjectCreating);
      }
      catch (Exception e) {
        LOG.warn(e);
        showErrorMessage(e.getMessage());
      }
    }, MeteorBundle.message("progress.title.meteor.project.generator"), false, project);
  }

  public void createGithubProject(@NotNull Project project,
                                  @NotNull VirtualFile baseDir,
                                  @NotNull MeteorProjectSettings settings,
                                  Ref<Boolean> noErrorOnProjectCreating) {
    File zipArchiveFile = null;
    try {
      String ghUserName = settings.getOwner();
      String ghRepo = settings.getGithubRepositoryName();
      String ghBranch = settings.getGithubRepositoryBranch();
      zipArchiveFile = GithubDownloadUtil.findCacheFile(ghUserName, ghRepo, "meteor-" + ghRepo + ".zip");
      GithubDownloadUtil.downloadContentToFileWithProgressSynchronously(
        project,
        GithubProjectGeneratorPeer.getGithubZipballUrl(ghUserName, ghRepo, ghBranch),
        getName(),
        zipArchiveFile,
        ghUserName,
        ghRepo,
        false
      );

      ZipUtil.unzipWithProgressSynchronously(project,
                                             getName(),
                                             zipArchiveFile,
                                             VfsUtilCore.virtualToIoFile(baseDir),
                                             null, true);
      noErrorOnProjectCreating.set(true);
    }
    catch (Exception e) {
      if (zipArchiveFile != null) {
        FileUtil.delete(zipArchiveFile);
      }
      LOG.warn(e);
      showErrorMessage(e.getMessage());
    }
  }

  public static void executeCommandLineForDefaultProject(ProgressIndicator indicator,
                                                         @NotNull MeteorProjectSettings settings,
                                                         @NotNull VirtualFile baseDir,
                                                         Ref<Boolean> noErrorOnProjectCreating)
    throws IOException, ExecutionException {
    File tempProject = createTemp();
    List<String> params = ContainerUtil.prepend(settings.params(), settings.getCommand());
    ProcessOutput output = ExecUtil.execAndGetOutput(new GeneralCommandLine(params).withWorkDirectory(tempProject));
    if (output.getExitCode() != 0) {
      showErrorMessage(output.getStderr());
      deleteTemp(tempProject);
      return;
    }

    indicator.setText(MeteorBundle.message("prepare.project.directory"));
    File[] array = new File(tempProject.toURI()).listFiles();
    if (array != null && array.length != 0) {
      File from = array[0];
      assert from != null;

      FileUtil.copyDir(from, new File(baseDir.getPath()));
    }
    noErrorOnProjectCreating.set(true);
    deleteTemp(tempProject);
  }

  private static void createRunConfiguration(@NotNull Project project,
                                             @NotNull VirtualFile baseDir) {
    final RunManager runManager = RunManager.getInstance(project);
    MeteorConfigurationType configurationType = ConfigurationTypeUtil.findConfigurationType(MeteorConfigurationType.class);
    RunnerAndConfigurationSettings configuration =
      runManager.createConfiguration("Run Meteor", configurationType.getConfigurationFactories()[0]);

    MeteorRunConfiguration runConfiguration = (MeteorRunConfiguration)configuration.getConfiguration();
    runConfiguration.setWorkingDirectory(baseDir.getCanonicalPath());
    runManager.addConfiguration(configuration);
    runManager.setSelectedConfiguration(configuration);
  }

  private static File createTemp() throws IOException {
    return FileUtil.createTempDirectory("intellij-meteor-generator", null, false);
  }

  private static void deleteTemp(File tempProject) {
    if (!FileUtil.delete(tempProject)) {
      LOG.warn("Cannot delete " + tempProject);
    }
    else {
      LOG.info("Successfully deleted " + tempProject);
    }
  }


  @Override
  public @NotNull MeteorProjectPeer createPeer() {
    return new MeteorProjectPeer();
  }

  @Override
  public Icon getIcon() {
    return MeteorIcons.Meteor2;
  }

  public static final class MeteorProjectSettings {
    public void setType(String type) {
      myType = type;
    }

    public void setMeteorExecutablePath(String meteorExecutablePath) {
      myMeteorExecutablePath = meteorExecutablePath;
    }

    private String myType;
    private String myMeteorExecutablePath;
    private String myName;

    public void setName(String name) {
      myName = name;
    }

    public String getCommand() {
      return myMeteorExecutablePath;
    }

    public List<String> params() {
      if (!MeteorProjectPeer.EMPTY_PROJECT_TYPE.equals(myType)) {
        return ContainerUtil.emptyList();
      }

      return List.of("create", StringUtil.isEmpty(myName) ? DEFAULT_TEMPLATE_NAME : myName);
    }

    public String getGithubRepositoryBranch() {
      MeteorProjectPeer.RepoInfo info = MeteorProjectPeer.PROJECT_TYPES.get(myType);
      return info != null ? info.myBranch : "master";
    }

    public String getGithubRepositoryName() {
      MeteorProjectPeer.RepoInfo info = MeteorProjectPeer.PROJECT_TYPES.get(myType);
      return info != null ? info.myRepo : myType;
    }

    public String getOwner() {
      MeteorProjectPeer.RepoInfo info = MeteorProjectPeer.PROJECT_TYPES.get(myType);
      return info != null ? info.myUser : MeteorProjectPeer.OWNER_NAME;
    }
  }


  private static void showErrorMessage(final @NotNull String message) {
    Notifications.Bus.notify(new Notification(MeteorBundle.message("notification.group.meteor.js.generator"),
                                              MeteorBundle.message("notification.title.meteor.project"),
                                              MeteorBundle.message("notification.content.error.creating.meteor.app", message), NotificationType.ERROR));
  }
}
