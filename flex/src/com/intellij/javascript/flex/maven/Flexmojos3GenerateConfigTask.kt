// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.maven

import com.intellij.flex.model.bc.OutputType
import com.intellij.ide.IdeBundle
import com.intellij.lang.javascript.flex.FlexBundle
import com.intellij.lang.javascript.flex.FlexUtils
import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor
import com.intellij.openapi.module.Module
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.runBlockingMaybeCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportRawProgress
import com.intellij.util.ArrayUtil
import org.jetbrains.idea.maven.buildtool.MavenLogEventHandler
import org.jetbrains.idea.maven.model.MavenWorkspaceMap
import org.jetbrains.idea.maven.project.*
import org.jetbrains.idea.maven.project.MavenEmbeddersManager.EmbedderTask
import org.jetbrains.idea.maven.server.MavenGoalExecutionRequest
import org.jetbrains.idea.maven.utils.MavenLog
import org.jetbrains.idea.maven.utils.MavenProcessCanceledException
import org.jetbrains.idea.maven.utils.MavenUtil
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.math.max

internal class Flexmojos3GenerateConfigTask(private val myModule: Module,
                                   mavenProject: MavenProject?,
                                   mavenTree: MavenProjectsTree?,
                                   private val myConfigFilePath: String,
                                   private val myFlexConfigInformer: FlexConfigInformer) : MavenProjectsProcessorBasicTask(mavenProject,
                                                                                                                           mavenTree) {
  @Throws(MavenProcessCanceledException::class)
  override fun perform(project: Project,
                       embeddersManager: MavenEmbeddersManager,
                       indicator: ProgressIndicator) {
    if (myModule.isDisposed) return

    indicator.setText(FlexBundle.message("generating.flex.config.for", myMavenProject.displayName))

    val task = EmbedderTask { embedder ->
      var temporaryFiles: List<VirtualFile>? = null
      try {
        val workspaceMap = MavenWorkspaceMap()
        temporaryFiles = mavenIdToOutputFileMapping(workspaceMap, project, myTree.projects)

        val generateConfigGoal = FlexmojosImporter.FLEXMOJOS_GROUP_ID + ":" + FlexmojosImporter.FLEXMOJOS_ARTIFACT_ID +
                                 ":generate-config-" + myMavenProject.packaging
        val profilesIds = myMavenProject.activatedProfilesIds
        val request = MavenGoalExecutionRequest(File(myMavenProject.path), profilesIds)
        val result = runBlockingMaybeCancellable {
          withBackgroundProgress(project, MavenProjectBundle.message("maven.updating.folders"), true) {
            reportRawProgress { reporter ->
              embedder.executeGoal(listOf(request), generateConfigGoal, reporter, MavenLogEventHandler)[0]
            }
          }
        }
        if (!result.success) {
          myFlexConfigInformer.showFlexConfigWarningIfNeeded(project)
        }

        MavenUtil.invokeAndWaitWriteAction(project) {
          // need to refresh externally created file
          val file = LocalFileSystem.getInstance().refreshAndFindFileByPath(myConfigFilePath)
          if (file != null) {
            file.refresh(false, false)

            updateMainClass(myModule, file)
          }
        }
      }
      catch (e: MavenProcessCanceledException) {
        throw e
      }
      catch (e: Exception) {
        myFlexConfigInformer.showFlexConfigWarningIfNeeded(project)
        MavenLog.LOG.warn(e)
      }
      finally {
        if (temporaryFiles != null && !temporaryFiles.isEmpty()) {
          removeTemporaryFiles(project, temporaryFiles)
        }
      }
    }

    embeddersManager.execute(myMavenProject, MavenEmbeddersManager.FOR_POST_PROCESSING, task)
  }

  companion object {
    private const val TEMPORARY_FILE_CONTENT = "Remove this file"

    /**
     * For SWF- and SWC-packaged maven projects returned result contains mapping to respective SWF/SWC target file.
     * If such SWF/SWC file doesn't exist - temporary file is created.
     * Caller of this method is responsible for removing placeholder files
     * (see [.removeTemporaryFiles]).<br></br>
     * For not SWF/SWC projects - reference to pom.xml file is placed in result map.
     */
    @Throws(IOException::class)
    private fun mavenIdToOutputFileMapping(workspaceMap: MavenWorkspaceMap, project: Project,
                                           mavenProjects: Collection<MavenProject>): List<VirtualFile> {
      val exception = Ref<IOException>()
      val temporaryFiles: MutableList<VirtualFile> = ArrayList()
      MavenUtil.invokeAndWaitWriteAction(project) {
        try {
          for (mavenProject in mavenProjects) {
            if (ArrayUtil.contains(mavenProject.packaging, *FlexmojosImporter.SUPPORTED_PACKAGINGS)) {
              val outputFilePath = FlexmojosImporter.getOutputFilePath(mavenProject)
              val lastSlashIndex = outputFilePath.lastIndexOf("/")
              val outputFileName = outputFilePath.substring(lastSlashIndex + 1)
              val outputFolderPath = outputFilePath.substring(0, max(0, lastSlashIndex))

              var outputFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(outputFilePath)
              if (outputFile == null) {
                val outputDir = VfsUtil.createDirectoryIfMissing(outputFolderPath)
                if (outputDir == null) throw IOException(IdeBundle.message("error.failed.to.create.directory", outputFolderPath))
                // if maven project is not compiled and output file doesn't exist flexmojos fails to generate Flex compiler configuration file.
                // Workaround is to create empty placeholder file.
                outputFile = FlexUtils.addFileWithContent(outputFileName, TEMPORARY_FILE_CONTENT, outputDir)
                temporaryFiles.add(outputFile)
              }
              workspaceMap.register(mavenProject.mavenId, File(mavenProject.file.path), File(outputFile.path))
            }
            else {
              workspaceMap.register(mavenProject.mavenId, File(mavenProject.file.path))
            }
          }
        }
        catch (e: IOException) {
          exception.set(e)
        }
      }
      if (!exception.isNull) throw exception.get()
      return temporaryFiles
    }

    private fun removeTemporaryFiles(project: Project, files: Collection<VirtualFile>) {
      MavenUtil.invokeAndWaitWriteAction(project) {
        for (file in files) {
          try {
            if (file.isValid && file.length == TEMPORARY_FILE_CONTENT.length.toLong() && String(file.contentsToByteArray(),
                                                                                                StandardCharsets.UTF_8) == TEMPORARY_FILE_CONTENT) {
              file.delete(Flexmojos3GenerateConfigTask::class.java)
            }
          }
          catch (e: IOException) { /*ignore*/
          }
        }
      }
    }

    @JvmStatic
    fun updateMainClass(module: Module, configFile: VirtualFile) {
      if (FlexBuildConfigurationsExtension.getInstance().configurator.configEditor != null) return  // Project Structure open


      try {
        val mainClassPath = FlexUtils.findXMLElement(configFile.inputStream, "<flex-config><file-specs><path-element>")
        val mainClassFile = if (mainClassPath == null) null else LocalFileSystem.getInstance().findFileByPath(mainClassPath)
        if (mainClassFile == null || mainClassFile.isDirectory) return

        val sourceRoot = ProjectRootManager.getInstance(module.project).fileIndex.getSourceRootForFile(mainClassFile)
        val relativePath = if (sourceRoot == null) null else VfsUtilCore.getRelativePath(mainClassFile, sourceRoot, '/')
        val mainClass = if (relativePath == null
        ) mainClassFile.nameWithoutExtension
        else FileUtilRt.getNameWithoutExtension(relativePath).replace('/', '.')

        val modifiableModel = ModuleRootManager.getInstance(module).modifiableModel
        val librariesModel = LibraryTablesRegistrar.getInstance().getLibraryTable(module.project).modifiableModel
        val flexEditor = FlexProjectConfigurationEditor
          .createEditor(module.project, Collections.singletonMap(module, modifiableModel), librariesModel, null)

        val bcs = flexEditor.getConfigurations(module)
        val mainBC = bcs.find { it.outputType == OutputType.Application && module.name == it.name }

        if (mainBC != null) {
          mainBC.mainClass = mainClass
        }

        flexEditor.commit()
        Disposer.dispose(librariesModel)
        modifiableModel.dispose()
      }
      catch (ignore: IOException) { /**/
      }
      catch (ignore: ConfigurationException) {
      }
    }
  }
}
