package org.jetbrains.qodana.staticAnalysis.sarif

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.util.io.URLUtil
import com.jetbrains.qodana.sarif.model.PropertyBag
import com.jetbrains.qodana.sarif.model.Run
import com.jetbrains.qodana.sarif.model.VersionControlDetails
import git4idea.history.GitHistoryUtils
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryManager
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.qodanaEnv
import java.net.URI
import java.net.URISyntaxException

private val LOG = logger<GitReportContributor>()

class GitReportContributor : SarifReportContributor {

  override fun contribute(run: Run, project: Project, config: QodanaConfig) {
    val projectDir = project.guessProjectDir() ?: return
    val repository = GitRepositoryManager.getInstance(project).getRepositoryForFileQuick(projectDir)
    if (repository != null) {
      LOG.debug("Found git repository: $repository")
    }
    val branch = qodanaEnv().QODANA_BRANCH.value ?: repository?.currentBranch?.name
    if (branch == null) {
      LOG.warn("Unable to find the branch name, please set the environment variable ${qodanaEnv().QODANA_BRANCH.key}")
    }
    LOG.debug("Found git branch: $branch")
    val remoteUrl = getRemoteUrl(branch, repository)
    if (remoteUrl == null) {
      LOG.warn("Unable to find the remote url, please set the environment variable ${qodanaEnv().QODANA_REMOTE_URL.key}")
    }
    LOG.debug("Found git remote url: $remoteUrl")
    val revision = qodanaEnv().QODANA_REVISION.value ?: repository?.currentRevision
    if (revision == null) {
      LOG.warn("Unable to find the revision, please set the environment variable ${qodanaEnv().QODANA_REVISION.key}")
    }
    LOG.debug("Found git revision: $revision")
    val properties = PropertyBag()
      .also { it["vcsType"] = "Git" }
      .also {
        it["repoUrl"] = qodanaEnv().QODANA_REPO_URL.value?.takeIf { url ->
          url.startsWith("https://")
        } ?: remoteUrl?.toString().takeIf { url ->
          url != null && url.startsWith("https://")
        } ?: ""
        if (it["repoUrl"] == "") {
          LOG.warn("Unable to parse the repository url, please set the environment variable ${qodanaEnv().QODANA_REPO_URL.key}, should start with https://")
        }
      }
    val vcsDetails = VersionControlDetails()
      .withRepositoryUri(remoteUrl)
      .withBranch(branch)
      .withRevisionId(revision)
      .withProperties(properties)

    try {
      if (revision != null) {
        val hashParameters = GitHistoryUtils.formHashParameters(project, setOf(revision))
        val gitCommits = GitHistoryUtils.history(project, projectDir, *hashParameters)

        if (gitCommits.size > 0) {
          val commit = gitCommits[0]
          properties["lastAuthorName"] = commit.author.name
          properties["lastAuthorEmail"] = commit.author.email
        }
      }
    }
    catch (e: Exception) {
      LOG.warn("Unable to obtain the author from VCS", e)
    }

    run.versionControlProvenance = setOf(vcsDetails)
  }

  private fun getRemoteUrl(name: String?, repository: GitRepository?): URI? {
    var url = qodanaEnv().QODANA_REMOTE_URL.value
    if (url == null) {
      name ?: return null
      val branchTrackInfo = repository?.getBranchTrackInfo(name) ?: return null
      url = branchTrackInfo.remote.firstUrl ?: return null
    }
    try {
      return if (URLUtil.containsScheme(url)) URI(url) else convertScp(url)
    }
    catch (e: URISyntaxException) {
      LOG.warn("Unable to create a URI from the extracted remote URL: $url", e)
      return null
    }
  }

  private fun convertScp(url: String): URI? {
    val uri = URI("ssh://$url")
    val authority = uri.authority ?: return null

    val fields = authority.split(":")
    if (fields.size != 2) {
      LOG.warn("Can't parse authority in git origin uri '$url'")
      return null
    }
    val authAndHost = fields[0]
    val path = fields[1] + (uri.path ?: "")
    return URI("ssh://$authAndHost/$path")
  }
}