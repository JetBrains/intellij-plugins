package org.jetbrains.qodana.ui

import com.intellij.ide.impl.getOriginFromUrl
import com.intellij.ide.impl.getProjectOriginUrl
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import kotlinx.coroutines.*
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.extensions.RepositoryInfoProvider
import kotlin.io.path.Path

interface ProjectVcsDataProvider {
  val projectName: String

  suspend fun originUrl(): String?

  suspend fun projectBranches(): List<String>

  suspend fun currentBranch(): String?
}

suspend fun ProjectVcsDataProvider.originHost(): String? {
  return originUrl()?.let { getOriginFromUrl(it) }?.host
}

suspend fun ProjectVcsDataProvider.ciRelevantBranches(): List<String> {
  val commonBranches = setOf("master", "main", "dev", "develop", "release")
  val currentBranch = currentBranch() ?: "main"
  return (projectBranches().filter { it in commonBranches } + currentBranch).distinct()
}

class ProjectVcsDataProviderImpl(private val project: Project, scope: CoroutineScope) : ProjectVcsDataProvider {
  override val projectName: String
    get() = project.name

  private val originUrl: Deferred<String?> = scope.async(QodanaDispatchers.Default, start = CoroutineStart.LAZY) {
    computeOriginUrl()
  }

  private val projectBranches: Deferred<List<String>> = scope.async(QodanaDispatchers.Default, start = CoroutineStart.LAZY) {
    RepositoryInfoProvider.getProjectBranches(project)
  }

  private val currentBranch: Deferred<String?> = scope.async(QodanaDispatchers.Default, start = CoroutineStart.LAZY) {
    RepositoryInfoProvider.getBranch(project)
  }

  private suspend fun computeOriginUrl(): String? {
    val projectPath = project.projectFile?.toNioPath() ?: project.guessProjectDir()?.toNioPath() ?: project.basePath?.let { Path(it) }  ?: return null
    return withContext(QodanaDispatchers.IO) {
      getProjectOriginUrl(projectPath)
    }
  }

  override suspend fun originUrl(): String? {
    return originUrl.await()
  }

  override suspend fun projectBranches(): List<String> {
    return projectBranches.await()
  }

  override suspend fun currentBranch(): String? {
    return currentBranch.await()
  }
}