package com.intellij.javascript.flex.maven;

import com.intellij.ide.IdeBundle;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.IgnoredBeanFactory;
import com.intellij.openapi.vcs.changes.IgnoredFileBean;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.idea.maven.model.MavenWorkspaceMap;
import org.jetbrains.idea.maven.project.*;
import org.jetbrains.idea.maven.server.MavenEmbedderWrapper;
import org.jetbrains.idea.maven.server.MavenServerExecutionResult;
import org.jetbrains.idea.maven.utils.MavenLog;
import org.jetbrains.idea.maven.utils.MavenProcessCanceledException;
import org.jetbrains.idea.maven.utils.MavenProgressIndicator;
import org.jetbrains.idea.maven.utils.MavenUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Flexmojos3GenerateConfigTask extends MavenProjectsProcessorBasicTask {
  private static final String TEMPORARY_FILE_CONTENT = "Remove this file";

  private final String myConfigFilePath;
  private final FlexConfigInformer myFlexConfigInformer;

  public Flexmojos3GenerateConfigTask(final MavenProject mavenProject,
                                      final MavenProjectsTree mavenTree,
                                      final String configFilePath,
                                      final FlexConfigInformer flexConfigInformer) {
    super(mavenProject, mavenTree);
    this.myConfigFilePath = configFilePath;
    myFlexConfigInformer = flexConfigInformer;
  }

  public void perform(final Project project,
                      final MavenEmbeddersManager embeddersManager,
                      final MavenConsole console,
                      final MavenProgressIndicator indicator) throws MavenProcessCanceledException {
    indicator.setText(FlexBundle.message("generating.flex.config.for", myMavenProject.getDisplayName()));

    final MavenProjectsTree.EmbedderTask task = new MavenProjectsTree.EmbedderTask() {
      public void run(MavenEmbedderWrapper embedder) throws MavenProcessCanceledException {
        List<VirtualFile> temporaryFiles = null;
        final IgnoredFileBean[] filesToIgnoreOriginal = ChangeListManager.getInstance(project).getFilesToIgnore();

        try {
          MavenWorkspaceMap workspaceMap = new MavenWorkspaceMap();
          temporaryFiles = mavenIdToOutputFileMapping(workspaceMap, project, myTree.getProjects());

          embedder.customizeForStrictResolve(workspaceMap, console, indicator);
          final String generateConfigGoal = FlexmojosImporter.FLEXMOJOS_GROUP_ID + ":" + FlexmojosImporter.FLEXMOJOS_ARTIFACT_ID +
                                            ":generate-config-" + myMavenProject.getPackaging();
          MavenServerExecutionResult result = embedder
            .execute(myMavenProject.getFile(), myMavenProject.getActivatedProfilesIds(), Collections.singletonList(generateConfigGoal));
          if (result.projectData == null) {
            myFlexConfigInformer.showFlexConfigWarningIfNeeded(project);
          }

          MavenUtil.invokeAndWaitWriteAction(project, new Runnable() {
            public void run() {
              // todo set main class
              // need to refresh externally created file
              final VirtualFile file =
                LocalFileSystem.getInstance().refreshAndFindFileByPath(myConfigFilePath);
              if (file != null) {
                file.refresh(false, false);
              }
            }
          });
        }
        catch (MavenProcessCanceledException e) {
          throw e;
        }
        catch (Exception e) {
          myFlexConfigInformer.showFlexConfigWarningIfNeeded(project);
          console.printException(e);
          MavenLog.LOG.warn(e);
        }
        finally {
          ChangeListManager.getInstance(project).setFilesToIgnore(filesToIgnoreOriginal);

          if (temporaryFiles != null && !temporaryFiles.isEmpty()) {
            removeTemporaryFiles(project, temporaryFiles);
          }
        }
      }
    };

    myTree.executeWithEmbedder(myMavenProject, embeddersManager, MavenEmbeddersManager.FOR_POST_PROCESSING, console, indicator, task);
  }

  /**
   * For SWF- and SWC-packaged maven projects returned result contains mapping to respective SWF/SWC target file.
   * If such SWF/SWC file doesn't not exist - temporary file is created.
   * Caller of this method is responsible for removing placeholder files
   * (see {@link #removeTemporaryFiles(com.intellij.openapi.project.Project, java.util.Collection)}).<br>
   * For not SWF/SWC projects - reference to pom.xml file is placed in result map.
   */
  private static List<VirtualFile> mavenIdToOutputFileMapping(final MavenWorkspaceMap workspaceMap, final Project project,
                                                              final Collection<MavenProject> mavenProjects) throws IOException {
    final Ref<IOException> exception = new Ref<IOException>();
    final List<VirtualFile> temporaryFiles = new ArrayList<VirtualFile>();
    MavenUtil.invokeAndWaitWriteAction(project, new Runnable() {
      public void run() {
        try {
          for (MavenProject mavenProject : mavenProjects) {
            final String packaging = mavenProject.getPackaging();
            if ("swf".equalsIgnoreCase(packaging) || "swc".equalsIgnoreCase(packaging)) {
              final String outputFilePath = Flexmojos3Configurator.getOutputFilePath(mavenProject);
              final int lastSlashIndex = outputFilePath.lastIndexOf("/");
              final String outputFileName = outputFilePath.substring(lastSlashIndex + 1);
              final String outputFolderPath = outputFilePath.substring(0, Math.max(0, lastSlashIndex));

              VirtualFile outputFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(outputFilePath);
              if (outputFile == null) {
                final VirtualFile outputDir = VfsUtil.createDirectoryIfMissing(outputFolderPath);
                if (outputDir == null) throw new IOException(IdeBundle.message("error.failed.to.create.directory", outputFolderPath));
                // if maven project is not compiled and output file doesn't exist flexmojos fails to generate Flex compiler configuration file.
                // Workaround is to create empty placeholder file.
                ChangeListManager.getInstance(project).addFilesToIgnore(IgnoredBeanFactory.ignoreFile(outputFilePath, project));
                outputFile = FlexUtils.addFileWithContent(outputFileName, TEMPORARY_FILE_CONTENT, outputDir);
                if (outputFile == null) throw new IOException(IdeBundle.message("error.message.unable.to.create.file", outputFileName));
                temporaryFiles.add(outputFile);
              }
              workspaceMap.register(mavenProject.getMavenId(), new File(mavenProject.getFile().getPath()), new File(outputFile.getPath()));
            }
            else {
              workspaceMap.register(mavenProject.getMavenId(), new File(mavenProject.getFile().getPath()));
            }
          }
        }
        catch (IOException e) {
          exception.set(e);
        }
      }
    });
    if (!exception.isNull()) throw exception.get();
    return temporaryFiles;
  }

  private static void removeTemporaryFiles(final Project project, final Collection<VirtualFile> files) {
    MavenUtil.invokeAndWaitWriteAction(project, new Runnable() {
      public void run() {
        for (VirtualFile file : files) {
          try {
            if (file.isValid() &&
                file.getLength() == TEMPORARY_FILE_CONTENT.length() &&
                new String(file.contentsToByteArray()).equals(TEMPORARY_FILE_CONTENT)) {
              file.delete(Flexmojos3GenerateConfigTask.class);
            }
          }
          catch (IOException e) {/*ignore*/}
        }
      }
    });
  }
}
