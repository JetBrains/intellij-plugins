// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex.maven;

import com.intellij.flex.model.bc.OutputType;
import com.intellij.ide.IdeBundle;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.idea.maven.model.MavenExplicitProfiles;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Flexmojos3GenerateConfigTask extends MavenProjectsProcessorBasicTask {
  private static final String TEMPORARY_FILE_CONTENT = "Remove this file";

  private final Module myModule;
  private final String myConfigFilePath;
  private final FlexConfigInformer myFlexConfigInformer;

  public Flexmojos3GenerateConfigTask(final Module module,
                                      final MavenProject mavenProject,
                                      final MavenProjectsTree mavenTree,
                                      final String configFilePath,
                                      final FlexConfigInformer flexConfigInformer) {
    super(mavenProject, mavenTree);
    myModule = module;
    myConfigFilePath = configFilePath;
    myFlexConfigInformer = flexConfigInformer;
  }

  @Override
  public void perform(final Project project,
                      final MavenEmbeddersManager embeddersManager,
                      final MavenConsole console,
                      final MavenProgressIndicator indicator) throws MavenProcessCanceledException {
    if (myModule.isDisposed()) return;

    indicator.setText(FlexBundle.message("generating.flex.config.for", myMavenProject.getDisplayName()));

    final MavenProjectResolver.EmbedderTask task = new MavenProjectResolver.EmbedderTask() {
      @Override
      public void run(MavenEmbedderWrapper embedder) throws MavenProcessCanceledException {
        List<VirtualFile> temporaryFiles = null;
        try {
          MavenWorkspaceMap workspaceMap = new MavenWorkspaceMap();
          temporaryFiles = mavenIdToOutputFileMapping(workspaceMap, project, myTree.getProjects());

          embedder.customizeForResolve(workspaceMap, console, indicator);
          final String generateConfigGoal = FlexmojosImporter.FLEXMOJOS_GROUP_ID + ":" + FlexmojosImporter.FLEXMOJOS_ARTIFACT_ID +
                                            ":generate-config-" + myMavenProject.getPackaging();
          final MavenExplicitProfiles profilesIds = myMavenProject.getActivatedProfilesIds();
          MavenServerExecutionResult result = embedder
            .execute(myMavenProject.getFile(), profilesIds.getEnabledProfiles(), profilesIds.getDisabledProfiles(), Collections.singletonList(generateConfigGoal));
          if (result.projectData == null) {
            myFlexConfigInformer.showFlexConfigWarningIfNeeded(project);
          }

          MavenUtil.invokeAndWaitWriteAction(project, () -> {
            // need to refresh externally created file
            final VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByPath(myConfigFilePath);
            if (file != null) {
              file.refresh(false, false);

              updateMainClass(myModule, file);
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
          if (temporaryFiles != null && !temporaryFiles.isEmpty()) {
            removeTemporaryFiles(project, temporaryFiles);
          }
        }
      }
    };

    myResolver.executeWithEmbedder(myMavenProject, embeddersManager, MavenEmbeddersManager.FOR_POST_PROCESSING, console, indicator, task);
  }

  /**
   * For SWF- and SWC-packaged maven projects returned result contains mapping to respective SWF/SWC target file.
   * If such SWF/SWC file doesn't exist - temporary file is created.
   * Caller of this method is responsible for removing placeholder files
   * (see {@link #removeTemporaryFiles(Project, Collection)}).<br>
   * For not SWF/SWC projects - reference to pom.xml file is placed in result map.
   */
  private static List<VirtualFile> mavenIdToOutputFileMapping(final MavenWorkspaceMap workspaceMap, final Project project,
                                                              final Collection<MavenProject> mavenProjects) throws IOException {
    final Ref<IOException> exception = new Ref<>();
    final List<VirtualFile> temporaryFiles = new ArrayList<>();
    MavenUtil.invokeAndWaitWriteAction(project, () -> {
      try {
        for (MavenProject mavenProject : mavenProjects) {
          if (ArrayUtil.contains(mavenProject.getPackaging(), FlexmojosImporter.SUPPORTED_PACKAGINGS)) {
            final String outputFilePath = FlexmojosImporter.getOutputFilePath(mavenProject);
            final int lastSlashIndex = outputFilePath.lastIndexOf("/");
            final String outputFileName = outputFilePath.substring(lastSlashIndex + 1);
            final String outputFolderPath = outputFilePath.substring(0, Math.max(0, lastSlashIndex));

            VirtualFile outputFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(outputFilePath);
            if (outputFile == null) {
              final VirtualFile outputDir = VfsUtil.createDirectoryIfMissing(outputFolderPath);
              if (outputDir == null) throw new IOException(IdeBundle.message("error.failed.to.create.directory", outputFolderPath));
              // if maven project is not compiled and output file doesn't exist flexmojos fails to generate Flex compiler configuration file.
              // Workaround is to create empty placeholder file.
              outputFile = FlexUtils.addFileWithContent(outputFileName, TEMPORARY_FILE_CONTENT, outputDir);
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
    });
    if (!exception.isNull()) throw exception.get();
    return temporaryFiles;
  }

  private static void removeTemporaryFiles(final Project project, final Collection<VirtualFile> files) {
    MavenUtil.invokeAndWaitWriteAction(project, () -> {
      for (VirtualFile file : files) {
        try {
          if (file.isValid() &&
              file.getLength() == TEMPORARY_FILE_CONTENT.length() &&
              new String(file.contentsToByteArray(), StandardCharsets.UTF_8).equals(TEMPORARY_FILE_CONTENT)) {
            file.delete(Flexmojos3GenerateConfigTask.class);
          }
        }
        catch (IOException e) {/*ignore*/}
      }
    });
  }

  public static void updateMainClass(final Module module, final VirtualFile configFile) {
    if (FlexBuildConfigurationsExtension.getInstance().getConfigurator().getConfigEditor() != null) return; // Project Structure open

    try {
      final String mainClassPath = FlexUtils.findXMLElement(configFile.getInputStream(), "<flex-config><file-specs><path-element>");
      final VirtualFile mainClassFile = mainClassPath == null ? null : LocalFileSystem.getInstance().findFileByPath(mainClassPath);
      if (mainClassFile == null || mainClassFile.isDirectory()) return;

      final VirtualFile sourceRoot = ProjectRootManager.getInstance(module.getProject()).getFileIndex().getSourceRootForFile(mainClassFile);
      final String relativePath = sourceRoot == null ? null : VfsUtilCore.getRelativePath(mainClassFile, sourceRoot, '/');
      final String mainClass = relativePath == null
                               ? mainClassFile.getNameWithoutExtension()
                               : FileUtilRt.getNameWithoutExtension(relativePath).replace('/', '.');

      final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
      final LibraryTable.ModifiableModel librariesModel = LibraryTablesRegistrar.getInstance().getLibraryTable(module.getProject()).getModifiableModel();
      final FlexProjectConfigurationEditor flexEditor = FlexProjectConfigurationEditor
        .createEditor(module.getProject(), Collections.singletonMap(module, modifiableModel), librariesModel, null);

      final ModifiableFlexBuildConfiguration[] bcs = flexEditor.getConfigurations(module);
      final ModifiableFlexBuildConfiguration mainBC = ContainerUtil.find(bcs, bc -> bc.getOutputType() == OutputType.Application && module.getName().equals(bc.getName()));

      if (mainBC != null) {
        mainBC.setMainClass(mainClass);
      }

      flexEditor.commit();
      Disposer.dispose(librariesModel);
      modifiableModel.dispose();
    }
    catch (IOException | ConfigurationException ignore) {/**/}
  }
}
