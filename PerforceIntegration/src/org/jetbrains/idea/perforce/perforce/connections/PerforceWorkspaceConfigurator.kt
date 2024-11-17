package org.jetbrains.idea.perforce.perforce.connections

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.idea.perforce.perforce.PerforceSettings
import org.jetbrains.idea.perforce.perforce.connections.P4ClientParser.PerforceClient
import org.jetbrains.idea.perforce.perforce.connections.P4ConfigFields.*
import java.io.File
import java.io.IOException

@ApiStatus.Internal
@Service(Service.Level.PROJECT)
class PerforceWorkspaceConfigurator(private val project: Project) {

  @RequiresBackgroundThread
  fun configure(projectRoots: Collection<VirtualFile>): Collection<P4Config> {
    if (projectRoots.isEmpty()) return emptyList()

    val p4ClientParser = project.service<P4ClientParser>()
    val parametersSet =
      P4ConnectionParametersProvider.EP_NAME.extensionList.flatMap { provider -> provider.getConnectionParameters(project) }.toSet()

    if (parametersSet.isEmpty()) return emptyList()

    val clients = p4ClientParser.getPerforceClients(project, parametersSet, projectRoots)
    val configs = mutableListOf<P4Config>()
    for ((projectRoot, client) in clients) {
      val workspaceRoot = client.workspaceRootPath.let(::File)
      findOrGenerateP4Config(workspaceRoot, client)?.also { config ->
        configs.add(P4Config(config, projectRoot))
      }
    }

    return configs
  }

  private fun findOrGenerateP4Config(workspaceRoot: File, client: PerforceClient): File? {
    if (!workspaceRoot.exists()) {
      LOG.info("Workspace root doesn't exist: $workspaceRoot")
      return null
    }
    val configFile = workspaceRoot.resolve(getP4ConfigFileName())
    if (configFile.exists()) return configFile

    val parameters = client.parameters
    val configContent = mutableListOf<String>().apply {
      add("${P4PORT.name}=${parameters.server}")
      add("${P4USER.name}=${parameters.user}")
      add("${P4CLIENT.name}=${client.clientName}")
      add("${P4IGNORE.name}=$P4IGNORE_NAME;$GITIGNORE_NAME")
    }.joinToString("\n")

    try {
      FileUtil.writeToFile(configFile, configContent)
      if (PerforceSettings.getSettings(project).useP4CONFIG) {
        PerforceConnectionManager.getInstance(project).updateConnections()
      }
    }
    catch (e: IOException) {
      LOG.warn("Unable to generate p4config for $workspaceRoot", e)
    }

    return configFile
  }

  private fun getP4ConfigFileName() = P4EnvHelper.getConfigHelper(project).p4Config ?: P4CONFIG_NAME

  data class P4Config(val configFile: File, val contentRoot: VirtualFile)

  companion object {
    private val LOG = logger<PerforceWorkspaceConfigurator>()

    const val P4CONFIG_NAME = "p4config.txt"
    const val P4IGNORE_NAME = ".p4ignore.txt"

    /**
     *.gitignore exist by default in all .idea directory and already contains project configuration files which should be ignored
     */
    const val GITIGNORE_NAME = ".gitignore"
  }
}
