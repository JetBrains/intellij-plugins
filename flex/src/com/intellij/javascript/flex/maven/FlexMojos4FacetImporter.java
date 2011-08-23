package com.intellij.javascript.flex.maven;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineBuilder;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexFacet;
import com.intellij.lang.javascript.flex.build.FlexCompilerProjectConfiguration;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.IgnoredBeanFactory;
import com.intellij.util.StringBuilderSpinAllocator;
import com.intellij.util.io.ZipUtil;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.execution.MavenExternalParameters;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.execution.MavenRunnerSettings;
import org.jetbrains.idea.maven.project.*;
import org.jetbrains.idea.maven.utils.MavenLog;
import org.jetbrains.idea.maven.utils.MavenProcessCanceledException;
import org.jetbrains.idea.maven.utils.MavenProgressIndicator;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipFile;

public class FlexMojos4FacetImporter extends FlexMojos3FacetImporter {
  @Override
  protected boolean isApplicable(char majorVersion) {
    return majorVersion >= '4';
  }

  @Override
  protected String getCompilerConfigXmlSuffix() {
    return "-configs.xml";
  }

  @Override
  protected String getFlexmojosWarningDetailed() {
    return FlexBundle.message("flexmojos4.warning.detailed");
  }

  protected @Nullable Element getLocalesElement(MavenProject mavenProject, boolean compiled) {
    return getConfig(mavenProject, "locales" + (compiled ? "Compiled" : "Runtime"));
  }

  @Override
  protected void addGenerateFlexConfigTask(List<MavenProjectsProcessorTask> postTasks, FlexFacet facet,
                                           MavenProject mavenProject, MavenProjectsTree mavenTree) {
    final Project project = facet.getModule().getProject();
    if (FlexCompilerProjectConfiguration.getInstance(project).GENERATE_FLEXMOJOS_CONFIGS) {
      // run only once for import list
      for (MavenProjectsProcessorTask postTask : postTasks) {
        if (postTask instanceof GenerateFlexConfigTask) {
          return;
        }
      }

      ChangeListManager.getInstance(project).addFilesToIgnore(IgnoredBeanFactory.ignoreUnderDirectory(getCompilerConfigsDir(project),
                                                                                                      project));
      postTasks.add(new GenerateFlexConfigTask(mavenProject, mavenTree, this));
    }
  }
  
  private static String getCompilerConfigsDir(Project project) {
    //noinspection ConstantConditions
    return project.getBaseDir().getPath() + "/.idea/flexmojos";
  }

  @Override
  protected boolean isGenerateFlexConfigFilesForMxModules() {
    return false;
  }

  @Override
  protected String getCompilerConfigFile(Module module, MavenProject mavenProject, String suffix) {
    return getCompilerConfigsDir(module.getProject()) + "/" + mavenProject.getMavenId().getArtifactId() + "-" +
           mavenProject.getMavenId().getGroupId() + suffix + "-config.xml";
  }

  private static class GenerateFlexConfigTask extends MavenProjectsProcessorBasicTask {
    private final FlexConfigInformer flexConfigInformer;

    public GenerateFlexConfigTask(MavenProject mavenProject, MavenProjectsTree tree, FlexConfigInformer flexConfigInformer) {
      super(mavenProject, tree);
      this.flexConfigInformer = flexConfigInformer;
    }

    @Override
    public void perform(Project project, MavenEmbeddersManager embeddersManager, MavenConsole console, MavenProgressIndicator indicator)
      throws MavenProcessCanceledException {

      final File localRepo = new File(myMavenProject.getLocalRepository(), "com/intellij/flex/maven");
      if (!new File(localRepo, "idea-configurator/1.4.2").exists()) {
        ZipFile zipFile = null;
        try {
          //noinspection IOResourceOpenedButNotSafelyClosed
          zipFile = new ZipFile(getClass().getResource("/flexmojos-configurator.zip").getFile());
          ZipUtil.extract(zipFile, localRepo, null);
        }
        catch (IOException e) {
          console.printException(e);
          MavenLog.LOG.warn(e);
        }
        finally {
          if (zipFile != null) {
            try {
              zipFile.close();
            }
            catch (IOException ignored) {
            }
          }
        }
      }

      final long start = System.currentTimeMillis();
      indicator.setText(FlexBundle.message("generating.flex.configs"));

      final MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(project);
      MavenRunnerSettings mavenRunnerSettings = new MavenRunnerSettings();
      @SuppressWarnings("ConstantConditions")
      MavenRunnerParameters mavenRunnerParameters = new MavenRunnerParameters(false, project.getBaseDir().getPath(),
                                                                              Collections.singletonList(
                                                                                "com.intellij.flex.maven:idea-flexmojos-maven-plugin:1.4.2:generate"),
                                                                              mavenProjectsManager.getExplicitProfiles());
      final ProcessBuilder processBuilder;
      try {
        JavaParameters javaParameters = MavenExternalParameters.createJavaParameters(project, mavenRunnerParameters,
                                                                                     mavenProjectsManager.getGeneralSettings(),
                                                                                     mavenRunnerSettings);
        processBuilder = new ProcessBuilder(CommandLineBuilder.createFromJavaParameters(javaParameters).getCommands());
      }
      catch (ExecutionException e) {
        // resolve maven home
        new Notification("Maven", FlexBundle.message("flexmojos.project.import"), e.getMessage(), NotificationType.ERROR).notify(project);
        console.printException(e);
        MavenLog.LOG.warn(e);
        return;
      }

      processBuilder.directory((new File(mavenRunnerParameters.getWorkingDirPath())));
      processBuilder.redirectErrorStream(true);
      final Process process;
      try {
        process = processBuilder.start();
      }
      catch (IOException e) {
        console.printException(e);
        MavenLog.LOG.warn(e);
        flexConfigInformer.showFlexConfigWarningIfNeeded(project);
        return;
      }

      final StringBuilder processOutputString = StringBuilderSpinAllocator.alloc();
      final InputStreamReader reader = new InputStreamReader(process.getInputStream());
      try {
        char[] buf = new char[1024];
        int read;
        while ((read = reader.read(buf, 0, buf.length)) >= 0) {
          processOutputString.append(buf, 0, read);
        }

        try {
          process.waitFor();
        }
        catch (InterruptedException ignored) {
        }

        int exitCode = process.exitValue();
        if (exitCode != 0) {
          MavenLog.LOG.warn("idea flexmojos generator exited with exit code " + exitCode);
          flexConfigInformer.showFlexConfigWarningIfNeeded(project);
        }

        MavenLog.LOG.info("idea flexmojos generator out:\n" + processOutputString.toString());
      }
      catch (IOException e) {
        process.destroy();
        console.printException(e);
        MavenLog.LOG.warn(processOutputString.toString(), e);
      }
      finally {
        StringBuilderSpinAllocator.dispose(processOutputString);
        try {
          reader.close();
        }
        catch (IOException ignored) {
        }
      }

      final long duration = System.currentTimeMillis() - start;
      MavenLog.LOG.info(
        "Generating flex configs took " + duration + " ms: " + duration / 60000 + " min " + (duration % 60000) / 1000 + "sec");
    }
  }
}