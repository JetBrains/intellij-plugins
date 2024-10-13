package org.jetbrains.qodana.ui

class ProjectVcsDataProviderMock(
  override val projectName: String = "project-name",
  private val originUrl: String? = null,
  private val projectBranches: List<String> = emptyList(),
  private val currentBranch: String? = null
) : ProjectVcsDataProvider {
  override suspend fun originUrl(): String? {
    return originUrl
  }

  override suspend fun projectBranches(): List<String> {
    return projectBranches
  }

  override suspend fun currentBranch(): String? {
    return currentBranch
  }
}