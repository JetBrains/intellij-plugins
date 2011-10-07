package com.intellij.javascript.flex.maven;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineBuilder;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.impl.JavaAwareProjectJdkTableImpl;
import com.intellij.util.StringBuilderSpinAllocator;
import com.intellij.util.io.ReadWriteMappedBufferWrapper;
import org.jetbrains.idea.maven.project.*;
import org.jetbrains.idea.maven.utils.MavenLog;
import org.jetbrains.idea.maven.utils.MavenProcessCanceledException;
import org.jetbrains.idea.maven.utils.MavenProgressIndicator;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Flexmojos4GenerateFlexConfigTask extends MavenProjectsProcessorBasicTask {
  private static final byte UNLOCKED = 0;
  private static final byte LOCKED = 1;

  private static final byte SERVER_MUST_READ = 2;
  private static final byte CLIENT_MUST_READ = 3;

  private static final byte READ_PROJECT = 1;

  private static final Pattern MAVEN_ERROR_PATTERN = Pattern.compile("^\\[ERROR\\] (.*)$", Pattern.MULTILINE);

  private MappedByteBuffer mem;
  private int projectsToImportCount;
  private Process generatorProcess;
  private MavenProgressIndicator indicator;
  private final List<MavenProject> pendingProjects = new ArrayList<MavenProject>();

  public Flexmojos4GenerateFlexConfigTask(MavenProject mavenProject, MavenProjectsTree tree, int projectsToImportCount) {
    //noinspection NullableProblems
    super(null, tree);
    this.projectsToImportCount = projectsToImportCount;
  }

  private static String getSettingsFilePath(File settingsFile) {
    return settingsFile == null || !settingsFile.exists() ? " " : settingsFile.getAbsolutePath();
  }

  @Override
  public void perform(Project project, MavenEmbeddersManager embeddersManager, MavenConsole console, MavenProgressIndicator indicator)
    throws MavenProcessCanceledException {
    this.indicator = indicator;

    indicator.getIndicator().setIndeterminate(false);

    final MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(project);
    if (generatorProcess == null) {
      try {
        runGeneratorServer(project, mavenProjectsManager);
      }
      catch (IOException e) {
        showWarning(project);
        MavenLog.LOG.error(e);
      }
      catch (ExecutionException e) {
        showWarning(project);
      }
    }

    if (!pendingProjects.isEmpty()) {
      final MavenProject[] projects = pendingProjects.toArray(new MavenProject[pendingProjects.size()]);
      pendingProjects.clear();
      for (MavenProject pendingProject : projects) {
        if (!generate(pendingProject)) {
          return;
        }
      }
    }

    while (true) {
      try {
        while (mem.get(0) != CLIENT_MUST_READ) {
          if (checkCanceled()) {
            return;
          }

          try {
            //noinspection BusyWait
            Thread.sleep(100);
          }
          catch (InterruptedException e) {
            break;
          }
        }
      }
      finally {
        mem.put(0, UNLOCKED);
      }

      indicator.setFraction(100 / projectsToImportCount--);
      if (projectsToImportCount <= 0) {
        break;
      }
    }
  }

  private boolean checkCanceled() {
    if (indicator.isCanceled()) {
      generatorProcess.destroy();
      return true;
    }
    else {
      return false;
    }
  }

  boolean generate(MavenProject mavenProject) {
    if (mem == null) {
      pendingProjects.add(mavenProject);
      return false;
    }

    while (mem.get(0) != UNLOCKED) {
      if (checkCanceled()) {
        return false;
      }

      try {
        //noinspection BusyWait
        Thread.sleep(5);
      }
      catch (InterruptedException e) {
        return false;
      }
    }

    mem.clear();
    mem.put(LOCKED);
    mem.put(READ_PROJECT);

    final byte[] bytes = mavenProject.getFile().getPath().getBytes(Charset.forName("utf-8"));
    mem.putShort((short)bytes.length);
    mem.put(bytes);

    mem.put(0, SERVER_MUST_READ);

    return true;
  }

  private void runGeneratorServer(final Project project, MavenProjectsManager mavenProjectsManager)
    throws IOException, ExecutionException, MavenProcessCanceledException {
    if (mem == null) {
      //noinspection AssignmentToStaticFieldFromInstanceMethod
      mem = new ReadWriteMappedBufferWrapper(
        new File(System.getProperty("java.io.tmpdir"), "com.intellij.flex.maven.FlexMojos4FacetImporter"), 0, 8192).map();
    }
    else {
      mem.clear();
      mem.put(0, UNLOCKED);
    }

    final JavaParameters params = new JavaParameters();
    params.setJdk(JavaAwareProjectJdkTableImpl.getInstanceEx().getInternalJdk());

    final MavenGeneralSettings mavenGeneralSettings = mavenProjectsManager.getGeneralSettings();
    final ParametersList programParametersList = params.getProgramParametersList();

    programParametersList.add(getSettingsFilePath(mavenGeneralSettings.getEffectiveGlobalSettingsIoFile()));
    programParametersList.add(getSettingsFilePath(mavenGeneralSettings.getEffectiveUserSettingsIoFile()));

    programParametersList.add(mavenGeneralSettings.getEffectiveLocalRepository().getAbsolutePath());
    programParametersList.add(mavenGeneralSettings.isWorkOffline() ? "t" : "f");

    final Collection<String> explicitProfiles = mavenProjectsManager.getExplicitProfiles();
    programParametersList.add(String.valueOf(explicitProfiles.size()));
    for (String explicitProfile : explicitProfiles) {
      programParametersList.add(explicitProfile);
    }

    params.getVMParametersList().addParametersString("-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5011");
    params.getVMParametersList().add("-jar", "/Users/develar/Documents/flexmojos-idea-configurator/out/artifacts/FlexConfigGeneratorServer/generator_server.jar");

    final GeneralCommandLine commandLine = CommandLineBuilder.createFromJavaParameters(params);
    commandLine.setRedirectErrorStream(true);

    indicator.checkCanceled();

    generatorProcess = commandLine.createProcess();
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        StringBuilder stringBuilder = null;
        final InputStreamReader reader = new InputStreamReader(generatorProcess.getInputStream());
        try {
          stringBuilder = StringBuilderSpinAllocator.alloc();
          char[] buf = new char[1024];
          int read;
          while ((read = reader.read(buf, 0, buf.length)) >= 0) {
            stringBuilder.append(buf, 0, read);

            if (indicator.isCanceled()) {
              generatorProcess.destroy();
            }
          }

          try {
            generatorProcess.waitFor();
          }
          catch (InterruptedException ignored) {
          }

          int exitCode = generatorProcess.exitValue();
          final String result = stringBuilder.toString();
          if (exitCode != 0) {
            MavenLog.LOG.warn("Generating flex configs exited with exit code " + exitCode);

            final Matcher matcher = MAVEN_ERROR_PATTERN.matcher(result);
            stringBuilder.setLength(0);
            while (matcher.find()) {
              stringBuilder.append("<br>").append(matcher.group(1));
            }

            showWarning(stringBuilder.toString(), project);
          }

          MavenLog.LOG.info("Generating flex configs out:\n" + result);
        }
        catch (IOException e) {
          generatorProcess.destroy();
          MavenLog.LOG.warn(stringBuilder.toString(), e);
        }
        finally {
          if (stringBuilder != null) {
            StringBuilderSpinAllocator.dispose(stringBuilder);
          }
          try {
            reader.close();
          }
          catch (IOException ignored) {
          }
        }
      }
    });
  }

  private static void showWarning(String text, Project project) {
    new Notification("Maven", FlexBundle.message("flexmojos.project.import"), FlexBundle.message("flexmojos4.warning", text),
                     NotificationType.WARNING).notify(project);
  }

  private static void showWarning(Project project) {
    new Notification("Maven", FlexBundle.message("flexmojos.project.import"), FlexBundle.message("flexmojos4.warning", ""),
                     NotificationType.WARNING).notify(project);
  }
}
