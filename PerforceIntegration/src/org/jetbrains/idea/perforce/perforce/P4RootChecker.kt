// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.idea.perforce.perforce

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vcs.VcsException
import com.intellij.openapi.vcs.VcsKey
import com.intellij.openapi.vcs.VcsRootChecker
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.annotations.SystemIndependent
import org.jetbrains.idea.perforce.application.PerforceVcs
import org.jetbrains.idea.perforce.perforce.connections.*
import java.io.IOException
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes

internal class P4RootChecker : VcsRootChecker() {
  var project : Project? = null

  override fun getSupportedVcs(): VcsKey {
    return PerforceVcs.getKey()
  }

  override fun isRoot(file: VirtualFile): Boolean {
    if (project == null) {
      project = ProjectUtil.getActiveProject() ?: ProjectUtil.getOpenProjects().firstOrNull()
      Disposer.register(project!!) { project = null }
    }

    val configHelper = P4ConfigHelper.getConfigHelper(project)
    val p4ConfigFileName = configHelper.p4Config ?: return false
    val path = file.toNioPath()
    val p4Config = path.resolve(p4ConfigFileName)

    return try {
      val attributes = Files.readAttributes(p4Config, BasicFileAttributes::class.java)
      attributes.isRegularFile
    }
    catch (ignore: IOException) {
      false
    }
  }

  override fun validateRoot(file: VirtualFile): Boolean {
    return true
  }

  override fun detectProjectMappings(project: Project,
                                     projectRoots: Collection<VirtualFile>,
                                     mappedDirs: Set<VirtualFile>): Collection<VirtualFile> {
    if (!Registry.`is`("p4.new.project.mappings.handling")) {
      // emulate 'needsLegacyDefaultMappings=true' mode
      // leave checking to P4ConnectionCalculator
      return projectRoots
    }

    try {
      val connectionManager = PerforceConnectionManager.getInstance(project)
      if (connectionManager.isSingletonConnectionUsed) {
        LOG.debug("detecting for singleton connection")
        return detectSingletonConnectionMappings(project, projectRoots, mappedDirs)
      }
      else {
        LOG.debug("detecting for context connection")
        return detectContextConnectionMappings(project, projectRoots, mappedDirs)
      }
    }
    catch (e: VcsException) {
      LOG.warn(e)
      return emptyList()
    }
  }

  private fun detectSingletonConnectionMappings(project: Project,
                                                projectRoots: Collection<VirtualFile>,
                                                mappedDirs: Set<VirtualFile>): Collection<VirtualFile> {
    val unmappedRoots = collectUnmappedDirs(mappedDirs, projectRoots)
    if (unmappedRoots.isEmpty()) return emptyList()

    val settings = PerforceSettings.getSettings(project)
    val connection: P4Connection = SingletonConnection.getInstance(project)

    return detectMappingsForConnection(connection, settings, unmappedRoots)
  }

  private fun detectContextConnectionMappings(project: Project,
                                              projectRoots: Collection<VirtualFile>,
                                              mappedDirs: Set<VirtualFile>): Collection<VirtualFile> {
    val baseDir = project.basePath ?: return emptyList()

    val unmappedRoots = collectUnmappedDirs(mappedDirs, projectRoots)
    if (unmappedRoots.isEmpty()) return emptyList()

    val settings = PerforceSettings.getSettings(project)
    val parameters = detectEnvConnectionParametersFor(baseDir, settings)
    LOG.debug("detected connection parameters: $parameters")

    val testConnection = P4ParametersConnection(parameters, ConnectionId(null, baseDir))
    return detectMappingsForConnection(testConnection, settings, unmappedRoots)
  }

  private fun detectEnvConnectionParametersFor(baseDir: @SystemIndependent String,
                                               settings: PerforceSettings): P4ConnectionParameters {
    val localConnection: P4Connection = PerforceLocalConnection(baseDir)
    val p4SetOut = localConnection.runP4CommandLine(settings, arrayOf("set"), null)

    val defaultParameters = P4ConnectionParameters()
    val parameters = P4ConnectionParameters()
    P4ConnectionCalculator.parseSetOutput(defaultParameters, parameters, p4SetOut.stdout)
    return parameters
  }

  private fun detectMappingsForConnection(connection: P4Connection,
                                          settings: PerforceSettings,
                                          unmappedRoots: List<VirtualFile>): List<VirtualFile> {
    val p4InfoOut = connection.runP4CommandLine(settings, arrayOf("info"), null)
    val root = parseP4Info(p4InfoOut.stdout) ?: return emptyList()
    val rootFile = LocalFileSystem.getInstance().findFileByPath(root) ?: return emptyList()
    LOG.debug("found root file: ${rootFile.path}")

    return unmappedRoots.filter { VfsUtil.isAncestor(rootFile, it, false) }
  }

  private fun parseP4Info(output: String): String? {
    var result: String? = null
    for (line in output.lineSequence()) {
      if (line.startsWith(PerforceRunner.CLIENT_ROOT)) {
        val path = FileUtil.toSystemIndependentName(line.removePrefix(PerforceRunner.CLIENT_ROOT).trim())
        if (result != null) {
          LOG.warn("unexpected: two roots in output: '$result' and '$path'")
          return null
        }
        result = path
      }
    }
    LOG.debug("parseP4Info - root path: $result")
    return result
  }

  private fun collectUnmappedDirs(mappedDirs: Set<VirtualFile>,
                                  projectRoots: Collection<VirtualFile>): List<VirtualFile> {
    return projectRoots.filter { root ->
      generateSequence(root) { it.parent }
        .none { parent -> mappedDirs.contains(parent) }
    }
  }

  companion object {
    private val LOG = Logger.getInstance(P4RootChecker::class.java)
  }
}