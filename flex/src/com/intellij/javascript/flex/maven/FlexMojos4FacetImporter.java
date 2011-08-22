package com.intellij.javascript.flex.maven;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexFacet;
import com.intellij.lang.javascript.flex.build.FlexCompilerProjectConfiguration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.IgnoredBeanFactory;
import com.intellij.util.StringBuilderSpinAllocator;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.project.*;
import org.jetbrains.idea.maven.utils.MavenLog;
import org.jetbrains.idea.maven.utils.MavenProcessCanceledException;
import org.jetbrains.idea.maven.utils.MavenProgressIndicator;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

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
    public GenerateFlexConfigTask(MavenProject mavenProject, MavenProjectsTree tree) {
      super(mavenProject, tree);
    }

    @Override
    public void perform(Project project, MavenEmbeddersManager embeddersManager, MavenConsole console, MavenProgressIndicator indicator)
      throws MavenProcessCanceledException {
      final long start = System.currentTimeMillis();
      indicator.setText(FlexBundle.message("generating.flex.configs"));
      
      final ProcessBuilder processBuilder = new ProcessBuilder("mvn", "com.intellij.flex.maven:idea-flexmojos-maven-plugin:1.4.2:generate", "-o");
      //noinspection ConstantConditions
      processBuilder.directory(new File(project.getBaseDir().getPath()));
      processBuilder.redirectErrorStream(true);
      final Process process;
      try {
        process = processBuilder.start();
      }
      catch (IOException e) {
        MavenLog.LOG.warn(e);
        return;
      }

      final StringBuilder stringBuilder = StringBuilderSpinAllocator.alloc();
      final InputStreamReader reader = new InputStreamReader(process.getInputStream());
      try {
        char[] buf = new char[1024];
        int read;
        while ((read = reader.read(buf, 0, buf.length)) >= 0) {
          stringBuilder.append(buf, 0, read);
        }

        try {
          process.waitFor();
        }
        catch (InterruptedException ignored) {
        }

        int exitCode = process.exitValue();
        if (exitCode != 0) {
          MavenLog.LOG.warn("idea flexmojos generator exited with exit code " + exitCode + ", out:\n" + stringBuilder.toString());
        }
      }
      catch (IOException e) {
        process.destroy();
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
  }
}