package com.intellij.lang.javascript.linter.eslint.service

import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.lang.javascript.linter.JSLinterFileLevelAnnotation
import com.intellij.lang.javascript.linter.ServiceInactivityTracker
import com.intellij.lang.javascript.linter.eslint.EslintConfiguration
import com.intellij.lang.javascript.linter.eslint.EslintState
import com.intellij.lang.javascript.linter.eslint.service.protocol.ESLintLanguageServiceInitialState
import com.intellij.lang.javascript.service.protocol.LocalFilePath
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile

class ESLintLanguageService(
  project: Project,
  nodePackage: NodePackage,
  workingDirectory: VirtualFile,
) : ESLintBasedLanguageService<EslintState>(project, nodePackage, workingDirectory) {

  private val inactivityTracker: ServiceInactivityTracker
  var fileLevelAnnotation: JSLinterFileLevelAnnotation? = null

  init {
    val manager = EslintLanguageServiceManager.getInstance(project)
    inactivityTracker = ServiceInactivityTracker.startTracking(manager.coroutineScope, manager.inactivityTimeoutMs) {
      EslintLanguageServiceManager.getInstance(project).terminateInactiveService(this)
    }
  }

  override fun getConfigurationClass(): Class<EslintConfiguration> = EslintConfiguration::class.java

  override fun fillInitialProtocolState(protocolState: ESLintLanguageServiceInitialState, storedState: EslintState) {
    super.fillInitialProtocolState(protocolState, storedState)
    protocolState.additionalRootDirectory = LocalFilePath.create(storedState.additionalRulesDirPath)
    protocolState.includeSourceText = Registry.`is`("eslint.language.service.full.log", false)
  }

  fun <T, E : Throwable> useService(computable: ThrowableComputable<T, E>): T =
    inactivityTracker.useService { computable.compute() }


  override fun dispose() {
    inactivityTracker.stopTracking()
    super.dispose()
  }
}
