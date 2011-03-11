package com.intellij.javascript.flex.maven;

import com.intellij.ide.IdeBundle;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexFacet;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.IgnoredBeanFactory;
import com.intellij.openapi.vcs.changes.IgnoredFileBean;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.THashMap;
import org.jetbrains.idea.maven.model.MavenId;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class GenerateFlexConfigTask extends MavenProjectsProcessorBasicTask {
  private final FlexFacet myFlexFacet;
  private final String flexmojosGroupId;
  private final String flexmojosArtifactId;
  private final FlexConfigInformer myFlexConfigInformer;
  private static final String TEMPORARY_FILE_CONTENT = "Remove this file";

  public GenerateFlexConfigTask(final FlexFacet flexFacet,
                                final MavenProject mavenProject,
                                final MavenProjectsTree tree,
                                final String flexmojosGroupId,
                                final String flexmojosArtifactId,
                                final FlexConfigInformer flexConfigInformer) {
    super(mavenProject, tree);
    myFlexFacet = flexFacet;
    this.flexmojosGroupId = flexmojosGroupId;
    this.flexmojosArtifactId = flexmojosArtifactId;
    myFlexConfigInformer = flexConfigInformer;
  }

  public void perform(final Project project,
                      final MavenEmbeddersManager embeddersManager,
                      final MavenConsole console,
                      final MavenProgressIndicator indicator) throws MavenProcessCanceledException {
    indicator.setText(FlexBundle.message("generating.flex.config.for", myMavenProject.getDisplayName()));

    final MavenProjectsTree.EmbedderTask task = new MavenProjectsTree.EmbedderTask() {
      public void run(MavenEmbedderWrapper embedder) throws MavenProcessCanceledException {
        Map<MavenId, VirtualFile> mavenIdToFileMapping = null;
        final IgnoredFileBean[] filesToIgnoreOriginal = ChangeListManager.getInstance(project).getFilesToIgnore();

        try {
          mavenIdToFileMapping = getMavenIdToOutputFileMapping(project, myTree.getProjects());

          embedder.customizeForStrictResolve(convertFileMap(mavenIdToFileMapping), console, indicator);
          final String generateConfigGoal =
            flexmojosGroupId + ":" + flexmojosArtifactId + ":generate-config-" + myMavenProject.getPackaging();
          MavenServerExecutionResult result = embedder
            .execute(myMavenProject.getFile(), myMavenProject.getActivatedProfilesIds(), Collections.singletonList(generateConfigGoal));
          if (result.projectData == null) {
            myFlexConfigInformer.showFlexConfigWarningIfNeeded(project, myMavenProject, myFlexFacet);
          }

          MavenUtil.invokeAndWaitWriteAction(project, new Runnable() {
            public void run() {
              // need to refresh externally created file
              final VirtualFile file =
                LocalFileSystem.getInstance().refreshAndFindFileByPath(FlexBuildConfiguration.getInstance(myFlexFacet).CUSTOM_CONFIG_FILE);
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
          myFlexConfigInformer.showFlexConfigWarningIfNeeded(project, myMavenProject, myFlexFacet);
          console.printException(e);
          MavenLog.LOG.warn(e);
        }
        finally {
          ChangeListManager.getInstance(project).setFilesToIgnore(filesToIgnoreOriginal);

          if (mavenIdToFileMapping != null) {
            removeTemporaryFiles(project, mavenIdToFileMapping.values());
          }
        }
      }
    };

    myTree.executeWithEmbedder(myMavenProject, embeddersManager, MavenEmbeddersManager.FOR_POST_PROCESSING, console, indicator, task);
  }

  private MavenWorkspaceMap convertFileMap(Map<MavenId, VirtualFile> map) {
    MavenWorkspaceMap result = new MavenWorkspaceMap();
    for (Map.Entry<MavenId, VirtualFile> each : map.entrySet()) {
      result.register(each.getKey(), new File(each.getValue().getPath()));
    }
    return result;
  }

  /**
   * For SWF- and SWC-packaged maven projects returned result contains mapping to respective SWF/SWC target file.
   * If such SWF/SWC file doesn't not exist - temporary file is created.
   * Caller of this method is responsible for removing placeholder files
   * (see {@link #removeTemporaryFiles(com.intellij.openapi.project.Project, java.util.Collection)}).<br>
   * For not SWF/SWC projects - reference to pom.xml file is placed in result map.
   */
  private static Map<MavenId, VirtualFile> getMavenIdToOutputFileMapping(final Project project,
                                                                         final Collection<MavenProject> mavenProjects) throws IOException {
    final Map<MavenId, VirtualFile> mavenIdToOutputFile = new THashMap<MavenId, VirtualFile>(mavenProjects.size());

    final Ref<IOException> exception = new Ref<IOException>();
    MavenUtil.invokeAndWaitWriteAction(project, new Runnable() {
      public void run() {
        try {
          for (MavenProject mavenProject : mavenProjects) {
            final String packaging = mavenProject.getPackaging();
            if ("swf".equalsIgnoreCase(packaging) || "swc".equalsIgnoreCase(packaging)) {
              final String outputFilePath = FlexMojos3FacetImporter.getOutputFilePath(mavenProject);
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
              }
              mavenIdToOutputFile.put(mavenProject.getMavenId(), outputFile);
            }
            else {
              mavenIdToOutputFile.put(mavenProject.getMavenId(), mavenProject.getFile());
            }
          }
        }
        catch (IOException e) {
          exception.set(e);
        }
      }
    });
    if (!exception.isNull()) throw exception.get();
    return mavenIdToOutputFile;
  }

  private static void removeTemporaryFiles(final Project project, final Collection<VirtualFile> files) {
    MavenUtil.invokeAndWaitWriteAction(project, new Runnable() {
      public void run() {
        for (VirtualFile file : files) {
          try {
            if (file.isValid() &&
                file.getLength() == TEMPORARY_FILE_CONTENT.length() &&
                new String(file.contentsToByteArray()).equals(TEMPORARY_FILE_CONTENT)) {
              file.delete(GenerateFlexConfigTask.class);
            }
          }
          catch (IOException e) {/*ignore*/}
        }
      }
    });
  }
}
