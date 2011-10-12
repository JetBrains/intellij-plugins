package com.intellij.javascript.flex.maven;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineBuilder;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.configurations.SimpleJavaParameters;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.SimpleJavaSdkType;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathsList;
import com.intellij.util.StringBuilderSpinAllocator;
import com.intellij.util.SystemProperties;
import org.jetbrains.idea.maven.execution.MavenExternalParameters;
import org.jetbrains.idea.maven.model.MavenId;
import org.jetbrains.idea.maven.project.*;
import org.jetbrains.idea.maven.utils.MavenProcessCanceledException;
import org.jetbrains.idea.maven.utils.MavenProgressIndicator;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jetbrains.idea.maven.utils.MavenLog.LOG;

class Flexmojos4GenerateFlexConfigTask extends MavenProjectsProcessorBasicTask {
  private static final Pattern RESULT_PATTERN = Pattern.compile("^\\[fcg\\] generated: (.*):(.*)\\[/fcg\\]$", Pattern.MULTILINE);
  private static final Pattern MAVEN_ERROR_PATTERN = Pattern.compile("^\\[ERROR\\] (.*)$", Pattern.MULTILINE);

  private DataOutputStream out;

  private Process process;
  private MavenProgressIndicator indicator;
  private List<MavenProject> projects = new ArrayList<MavenProject>();

  public Flexmojos4GenerateFlexConfigTask(MavenProjectsTree tree) {
    //noinspection NullableProblems
    super(null, tree);
  }

  private static String getSettingsFilePath(File settingsFile) {
    return settingsFile == null || !settingsFile.exists() ? " " : settingsFile.getAbsolutePath();
  }

  @Override
  public void perform(Project project, MavenEmbeddersManager embeddersManager, MavenConsole console, final MavenProgressIndicator indicator)
    throws MavenProcessCanceledException {
    final long start = System.currentTimeMillis();
    this.indicator = indicator;

    indicator.setText(FlexBundle.message("generating.flex.configs"));

    try {
      runGeneratorServer(MavenProjectsManager.getInstance(project), project);
      writeProjects();
    }
    catch (IOException e) {
      showWarning(project);
      LOG.error(e);
    }
    catch (ExecutionException e) {
      showWarning(e.getMessage(), project);
      LOG.error(e);
    }

    if (process == null) {
      return;
    }

    //noinspection WhileLoopSpinsOnField
    while (process != null) {
      try {
        //noinspection BusyWait
        Thread.sleep(500);
      }
      catch (InterruptedException e) {
        break;
      }
      if (indicator.isCanceled()) {
        LOG.warn("Generating flex configs canceled");
        process.destroy();
        break;
      }
    }

    final long duration = System.currentTimeMillis() - start;
    LOG.info("Generating flex configs took " + duration + " ms: " + duration / 60000 + " min " + (duration % 60000) / 1000 + "sec");
  }

  private void writeProjects() throws IOException {
    assert !projects.isEmpty();
    out.writeShort(projects.size());
    for (MavenProject pendingProject : projects) {
      out.writeUTF(pendingProject.getFile().getPath());
    }
    out.flush();

    projects = null;
  }

  void submit(MavenProject mavenProject) {
    assert out == null;
    projects.add(mavenProject);
  }

  private void runGeneratorServer(MavenProjectsManager mavenProjectsManager, Project project)
    throws IOException, ExecutionException, MavenProcessCanceledException {
    final SimpleJavaParameters params = new SimpleJavaParameters();
    params.setJdk(new SimpleJavaSdkType().createJdk("tmp", SystemProperties.getJavaHome()));

    final MavenGeneralSettings mavenGeneralSettings = mavenProjectsManager.getGeneralSettings();
    final ParametersList programParametersList = params.getProgramParametersList();

    programParametersList.add(getSettingsFilePath(mavenGeneralSettings.getEffectiveGlobalSettingsIoFile()));
    programParametersList.add(getSettingsFilePath(mavenGeneralSettings.getEffectiveUserSettingsIoFile()));

    programParametersList.add(mavenGeneralSettings.getEffectiveLocalRepository().getAbsolutePath());
    programParametersList.add(mavenGeneralSettings.isWorkOffline() ? "t" : "f");
    //noinspection ConstantConditions
    programParametersList.add(project.getBaseDir().getPath() + "/.idea/flexmojos");

    configureMavenClassPath(mavenGeneralSettings, params.getClassPath());

    params.getVMParametersList().addParametersString("-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5011");
    //params.getVMParametersList().addParametersString("-agentpath:/Applications/Idea.app/bin/libyjpagent.jnilib=onexit=snapshot,disablej2ee,disablealloc,disablecounts,sampling,sessionname=mavenFCG");
    //params.getVMParametersList().addParametersString("-agentpath:/Applications/Idea.app/bin/libyjpagent.jnilib=onexit=snapshot,disablej2ee,disablealloc,tracing,sessionname=mavenFCG");
    params.setMainClass("com.intellij.flex.maven.GeneratorServer");

    final GeneralCommandLine commandLine = CommandLineBuilder.createFromJavaParameters(params);
    commandLine.setRedirectErrorStream(true);
    LOG.info("Generate Flex Configs Task:" + commandLine.getCommandLineString());

    indicator.checkCanceled();

    process = commandLine.createProcess();
    ApplicationManager.getApplication().executeOnPooledThread(new OutputReader(project));

    //noinspection IOResourceOpenedButNotSafelyClosed
    out = new DataOutputStream(new BufferedOutputStream(process.getOutputStream()));
    writeExplicitProfiles(mavenProjectsManager.getExplicitProfiles());
    writeWorkspaceMap(myTree.getProjects());

    out.writeUTF(FlexUtils.getPathToBundledJar("flexmojos-idea-configurator.jar"));
    out.writeUTF("com.intellij.flex.maven.IdeaConfigurator");
  }

  private final class OutputReader implements Runnable {
    private final Project project;

    public OutputReader(Project project) {
      this.project = project;
    }

    @Override
    public void run() {
      StringBuilder stringBuilder = null;
      int exitCode = -1;
      @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
      final InputStreamReader reader = new InputStreamReader(process.getInputStream());
      final List<String> filesForRefresh = new ArrayList<String>();
      try {
        stringBuilder = StringBuilderSpinAllocator.alloc();
        char[] buf = new char[64];
        int read;
        final Matcher matcher = RESULT_PATTERN.matcher(stringBuilder);
        while ((read = reader.read(buf, 0, buf.length)) >= 0) {
          final int startForResultParse = stringBuilder.length();
          stringBuilder.append(buf, 0, read);

          if (indicator.isCanceled()) {
            process.destroy();
          }

          if (matcher.find(startForResultParse)) {
            indicator.setText2(matcher.group(1));
            filesForRefresh.add(matcher.group(2));
          }
        }

        try {
          process.waitFor();
        }
        catch (InterruptedException ignored) {
        }

        exitCode = process.exitValue();
      }
      catch (IOException e) {
        if (stringBuilder != null) {
          LOG.warn(stringBuilder.toString(), e);
        }
      }
      finally {
        process.destroy();
        process = null;

        if (stringBuilder != null) {
          final String result = stringBuilder.toString();
          StringBuilderSpinAllocator.dispose(stringBuilder);
          if (exitCode != 0) {
            LOG.warn("Generating flex configs exited with exit code " + exitCode);
            showWarning(project);
          }
          LOG.info("Generating flex configs out:\n" + result);
        }

        final Matcher matcher = MAVEN_ERROR_PATTERN.matcher(stringBuilder);
        if (matcher.find()) {
          stringBuilder = StringBuilderSpinAllocator.alloc();
          try {
            do {
              stringBuilder.append("<br>").append(matcher.group(1));
            }
            while (matcher.find());
            showWarning(stringBuilder.toString(), project);
          }
          finally {
            StringBuilderSpinAllocator.dispose(stringBuilder);
          }
        }

        ApplicationManager.getApplication()
          .invokeLater(new RefreshConfigFiles(filesForRefresh, FlexMojos4FacetImporter.getCompilerConfigsDir(project)));
      }
    }
  }

  private final static class RefreshConfigFiles implements Runnable {
    private final List<String> filesForRefresh;
    private final String compilerConfigsDir;

    public RefreshConfigFiles(List<String> filesForRefresh, String compilerConfigsDir) {
      this.filesForRefresh = filesForRefresh;
      this.compilerConfigsDir = compilerConfigsDir;
    }

    public void run() {
      AccessToken token = WriteAction.start();
      try {
        // need to refresh externally created file
        final VirtualFile p = LocalFileSystem.getInstance().refreshAndFindFileByPath(compilerConfigsDir);
        if (p == null) {
          return;
        }

        for (String path : filesForRefresh) {
          final VirtualFile file = p.findChild(path);
          if (file != null) {
            file.refresh(false, false);
          }
        }
      }
      finally {
        token.finish();
      }
    }
  }

  private void writeExplicitProfiles(Collection<String> explicitProfiles) throws IOException {
    out.writeShort(explicitProfiles.size());
    if (explicitProfiles.isEmpty()) {
      return;
    }

    for (String explicitProfile : explicitProfiles) {
      out.writeUTF(explicitProfile);
    }
  }

  private void writeWorkspaceMap(final Collection<MavenProject> mavenProjects) throws IOException {
    int actualLength = 0;
    for (MavenProject mavenProject : mavenProjects) {
      final String packaging = mavenProject.getPackaging();
      if ("swf".equalsIgnoreCase(packaging) || "swc".equalsIgnoreCase(packaging)) {
        actualLength++;
      }
    }

    out.writeShort(actualLength);
    @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
    ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
    for (MavenProject mavenProject : mavenProjects) {
      final String packaging = mavenProject.getPackaging();
      if (!("swf".equalsIgnoreCase(packaging) || "swc".equalsIgnoreCase(packaging))) {
        continue;
      }

      final MavenId mavenId = mavenProject.getMavenId();
      objectOutputStream.writeObject(mavenId.getGroupId());
      objectOutputStream.writeObject(mavenId.getArtifactId());
      objectOutputStream.writeObject(mavenId.getVersion());
      objectOutputStream.writeObject(mavenProject.getFile().getPath());
    }

    objectOutputStream.flush();
  }

  private static void configureMavenClassPath(MavenGeneralSettings mavenGeneralSettings, PathsList classPath) throws ExecutionException {
    classPath.add(FlexUtils.getPathToBundledJar("flexmojos-flex-configs-generator-server.jar"));

    final String mavenHome = MavenExternalParameters.resolveMavenHome(mavenGeneralSettings) + File.separator;
    final String libDirPath = mavenHome + "lib";
    for (String s : new File(libDirPath).list()) {
      if (s.endsWith(".jar") && !s.startsWith("maven-embedder-") && !s.startsWith("commons-cli-") && !s.startsWith("nekohtml-")) {
        classPath.add(libDirPath + File.separator + s);
      }
    }

    // plexus-classworlds
    final String libBootDirPath = mavenHome + "boot";
    for (String s : new File(libBootDirPath).list()) {
      if (s.endsWith(".jar")) {
        classPath.add(libBootDirPath + File.separator + s);
      }
    }
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
