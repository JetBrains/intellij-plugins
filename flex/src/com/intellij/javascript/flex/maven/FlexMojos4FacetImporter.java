package com.intellij.javascript.flex.maven;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineBuilder;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexFacet;
import com.intellij.lang.javascript.flex.build.FlexCompilerProjectConfiguration;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.IgnoredBeanFactory;
import com.intellij.util.StringBuilderSpinAllocator;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FlexMojos4FacetImporter extends FlexMojos3FacetImporter {
  @Override
  protected boolean isApplicable(char majorVersion) {
    return majorVersion >= '4';
  }

  @Override
  protected String getCompilerConfigXmlSuffix() {
    return "-configs.xml";
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
      postTasks.add(new GenerateFlexConfigTask(mavenProject, mavenTree));
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
    private static final Pattern MAVEN_ERROR_PATTERN = Pattern.compile("^\\[ERROR\\] (.*)$", Pattern.MULTILINE);

    public static final String CONFIGURATOR_VERSION = "1.4.4";
    public static final String CONFIGURATOR_GOAL = "com.intellij.flex.maven:idea-flexmojos-maven-plugin:" + CONFIGURATOR_VERSION + ":generate";

    public GenerateFlexConfigTask(MavenProject mavenProject, MavenProjectsTree tree) {
      super(mavenProject, tree);
    }

    @Override
    public void perform(Project project, MavenEmbeddersManager embeddersManager, MavenConsole console, MavenProgressIndicator indicator)
      throws MavenProcessCanceledException {
      try {
        copyConfiguratorToLocalRepository();
      }
      catch (IOException e) {
        console.printException(e);
        showWarning("", project);
        MavenLog.LOG.error(e);
        return;
      }

      indicator.checkCanceled();

      final List<MavenProject> rootProjects = myTree.getRootProjects();
      final String workingDirPath;
      if (rootProjects.size() > 1) {
        // todo
        MavenLog.LOG.warn("Why root projects list > 1");
      }

      workingDirPath = rootProjects.get(0).getDirectory();

      final long start = System.currentTimeMillis();
      indicator.setText(FlexBundle.message("generating.flex.configs"));

      final MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(project);
      MavenRunnerSettings mavenRunnerSettings = new MavenRunnerSettings();
      MavenRunnerParameters runnerParameters = new MavenRunnerParameters(false, workingDirPath,
                                                                         Collections.singletonList(CONFIGURATOR_GOAL),
                                                                         mavenProjectsManager.getExplicitProfiles());
      final GeneralCommandLine commandLine;
      try {
        JavaParameters javaParameters = MavenExternalParameters.createJavaParameters(project, runnerParameters,
                                                                                     mavenProjectsManager.getGeneralSettings(),
                                                                                     mavenRunnerSettings);
        commandLine = CommandLineBuilder.createFromJavaParameters(javaParameters);
      }
      catch (ExecutionException e) {
        // resolve maven home
        new Notification("Maven", FlexBundle.message("flexmojos.project.import"), e.getMessage(), NotificationType.ERROR).notify(project);
        MavenLog.LOG.warn(e);
        return;
      }

      commandLine.setWorkDirectory(workingDirPath);
      commandLine.setRedirectErrorStream(true);

      indicator.checkCanceled();
      
      final Process process;
      try {
        process = commandLine.createProcess();
      }
      catch (ExecutionException e) {
        console.printException(e);
        MavenLog.LOG.error(e);
        showWarning("", project);
        return;
      }

      final StringBuilder stringBuilder = StringBuilderSpinAllocator.alloc();
      final InputStreamReader reader = new InputStreamReader(process.getInputStream());
      try {
        char[] buf = new char[1024];
        int read;
        while ((read = reader.read(buf, 0, buf.length)) >= 0) {
          stringBuilder.append(buf, 0, read);

          if (indicator.isCanceled()) {
            process.destroy();
          }
          indicator.checkCanceled();
        }

        try {
          process.waitFor();
        }
        catch (InterruptedException ignored) {
        }

        int exitCode = process.exitValue();
        final String result = stringBuilder.toString();
        if (exitCode != 0) {
          MavenLog.LOG.warn("idea flexmojos generator exited with exit code " + exitCode);

          final Matcher matcher = MAVEN_ERROR_PATTERN.matcher(result);
          stringBuilder.setLength(0);
          while (matcher.find()) {
            stringBuilder.append("<br>").append(matcher.group(1));
          }

          showWarning(stringBuilder.toString(), project);
        }

        MavenLog.LOG.info("idea flexmojos generator out:\n" + result);
      }
      catch (IOException e) {
        process.destroy();
        console.printException(e);
        MavenLog.LOG.warn(stringBuilder.toString(), e);
      }
      finally {
        StringBuilderSpinAllocator.dispose(stringBuilder);
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

    private static void showWarning(String text, Project project) {
      new Notification("Maven", FlexBundle.message("flexmojos.project.import"), FlexBundle.message("flexmojos4.warning", text),
                       NotificationType.WARNING).notify(project);
    }

    private void copyConfiguratorToLocalRepository() throws IOException {
      final File localRepo = new File(myMavenProject.getLocalRepository(), "com/intellij/flex/maven");
      if (new File(localRepo, "idea-configurator/" + CONFIGURATOR_VERSION).isDirectory()) {
        return;
      }

      if (localRepo.exists()) {
        FileUtil.delete(localRepo);
      }

      final ZipInputStream zipInputStream = new ZipInputStream(getClass().getResource("/flexmojos-configurator.zip").openStream());
      try {
        ZipEntry entry;
        while ((entry = zipInputStream.getNextEntry()) != null) {
          final File file = new File(localRepo, entry.getName());
          FileUtil.createParentDirs(file);
          if (entry.isDirectory()) {
            //noinspection ResultOfMethodCallIgnored
            file.mkdir();
          }
          else {
            final FileOutputStream outputStream = new FileOutputStream(file);
            try {
              FileUtil.copy(zipInputStream, outputStream);
            }
            finally {
              outputStream.close();
            }
          }
        }
      }
      finally {
        zipInputStream.close();
      }
    }
  }
}