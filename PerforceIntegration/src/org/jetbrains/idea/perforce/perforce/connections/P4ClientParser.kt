package org.jetbrains.idea.perforce.perforce.connections

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vcs.VcsException
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.vcsUtil.VcsFileUtil
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.idea.perforce.perforce.ConnectionId
import org.jetbrains.idea.perforce.perforce.PerforcePhysicalConnectionParametersI
import org.jetbrains.idea.perforce.perforce.PerforceSettings

@ApiStatus.Internal
@Service(Service.Level.PROJECT)
class P4ClientParser {

  fun getPerforceClients(project: Project,
                         parametersSet: Set<P4ConnectionParameters>,
                         contentRoots: Collection<VirtualFile>): Map<VirtualFile, PerforceClient> {
    val settings = PerforceSettings.getSettings(project).getPhysicalSettings(false)
    for (parameters in parametersSet) {
      val perforceClients = getPerforceClients(project, settings, parameters)
      val perforceClientsForRoots = contentRoots.mapNotNull { root ->
        val contentRootPath = FileUtil.toSystemDependentName(root.path)
        perforceClients
          .sortedByDescending { client -> client.lastUpdate } //TODO ensure format is "yyyy/mm/dd", otherwise will not work
          .firstOrNull { client -> VcsFileUtil.isAncestor(client.workspaceRootPath, contentRootPath, false) }
          ?.let { root to it }
      }.toMap()

      if (perforceClientsForRoots.isNotEmpty()) return perforceClientsForRoots
    }

    return emptyMap()
  }

  private fun getPerforceClients(project: Project,
                                 settings: PerforcePhysicalConnectionParametersI,
                                 parameters: P4ConnectionParameters): List<PerforceClient> {
    val clients = mutableListOf<PerforceClient>()
    try {
      if (parameters.user == null) return emptyList()
      val workingDir = project.basePath ?: return emptyList()
      val user = parameters.user ?: return emptyList()
      P4ParametersConnection(parameters, ConnectionId(null, workingDir))
        .runP4CommandLine(settings, parameters.toConnectArgs(), arrayOf<String>("clients", "-u", user), null)
        .stdout.lines().forEach { line ->
          parseClientLine(line, parameters)?.let { clients.add(it) }
        }
    }
    catch (e: VcsException) {
      LOG.warn("Error while getting perforce clients: ", e)
    }

    return clients
  }

  private fun P4ConnectionParameters.toConnectArgs(): Array<String> {
      val args = arrayListOf<String>()
      val server = getServer()
      if (!server.isNullOrBlank()) {
        args.add("-p");
        args.add(server);
      }
      val client = getClient()
      if (!client.isNullOrBlank()) {
        args.add("-c");
        args.add(client);
      }
      val user = getUser()
      if (!user.isNullOrBlank()) {
        args.add("-u");
        args.add(user);
      }
      val pass = password;
      if (!pass.isNullOrBlank()) {
        args.add("-P");
        args.add(pass);
      }

      return args.toTypedArray()
  }

  private fun parseClientLine(line: String, parameters: P4ConnectionParameters): PerforceClient? {
    val matchResult = CLIENT_REGEX.find(line)
    if (matchResult == null) return null

    val (clientName, lastUpdate, rootPath, description) = matchResult.destructured
    return PerforceClient(clientName, lastUpdate, rootPath, description, parameters)
  }

  data class PerforceClient(
    val clientName: String,
    val lastUpdate: String,
    val workspaceRootPath: String,
    val description: String,

    val parameters: P4ConnectionParameters,
  )

  companion object {
    private val LOG = logger<P4ClientParser>()
    private val CLIENT_REGEX = """Client (\S+)\s+(\d+/\d+/\d+)\s+root (\S+) '(.+)'""".toRegex()
  }
}
