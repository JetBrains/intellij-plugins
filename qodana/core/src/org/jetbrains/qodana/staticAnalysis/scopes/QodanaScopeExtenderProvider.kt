package org.jetbrains.qodana.staticAnalysis.scopes

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.util.progress.reportProgress
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import java.util.concurrent.ConcurrentHashMap

private val SCOPE_EXTENDER = Key.create<Set<String>>("qodana.applied.scope.extenders")

/**
 * Provides the ability to map certain inspections to specific scope extenders.
 * A single inspection can have only one scope extender available. A new one should be created if logic has to combine both.
 * Scope extenders are responsible for adding files to the scope of PR analysis for inspections that could benefit from it.
 */
interface QodanaScopeExtenderProvider {
  companion object {
    val EP_NAME: ExtensionPointName<QodanaScopeExtenderProvider> =
      ExtensionPointName.create("org.intellij.qodana.scopeExtenderProvider")

    fun getExtender(inspectionId: String): InspectionToolScopeExtender? {
      var extender: InspectionToolScopeExtender? = null
      for (prv in EP_NAME.extensionList) {
        if (prv.isApplicable(inspectionId)) {
          if (extender != null) {
             throw QodanaException("Only one extender can be applicable for inspection $inspectionId")
          }
          extender = InspectionToolScopeExtender.getExtender(prv.name) ?: throw QodanaException("Extender ${prv.name} not found")
        }
      }
      return extender
    }


    fun shouldSkip(extendedFiles: Map<VirtualFile, Set<String>>, file: VirtualFile, inspectionId: String): Boolean {
      val fileExtenders = extendedFiles[file] ?: return false
      val extender = getExtender(inspectionId) ?: return true
      return !fileExtenders.contains(extender.name)
    }
  }

  val name: String

  fun isApplicable(inspectionId: String): Boolean
}

/**
 * Defines a named algorithm, given a file - return files that somehow depend on that one.
 * Good example - when declarations in one file could be used in others. Therefore, if a file
 * gets changed, other files might be affected by this change.
 */
interface InspectionToolScopeExtender {
  companion object {
    val EP_NAME: ExtensionPointName<InspectionToolScopeExtender> =
      ExtensionPointName.create("org.intellij.qodana.scopeExtender")

    fun getExtender(extenderId: String): InspectionToolScopeExtender? {
      for (prv in EP_NAME.extensionList) {
        if (prv.name == extenderId) return prv
      }
      return null
    }
  }

  val name: String

  suspend fun extendScope(virtualFile: VirtualFile, project: Project, acceptedFiles: Map<VirtualFile, Set<String>>): List<VirtualFile>
}

suspend fun collectExtendedFiles(
  project: Project,
  fileToExtenders: Map<VirtualFile, List<InspectionToolScopeExtender>>
): Map<VirtualFile, Set<String>> {
  val scopeExtendedMap = ConcurrentHashMap<VirtualFile, Set<String>>(
    fileToExtenders.mapValues { emptySet() }
  )
  suspend fun InspectionToolScopeExtender.extendFrom(fromFile: VirtualFile) {
    extendScope(fromFile, project, scopeExtendedMap).forEach { file ->
      synchronized(scopeExtendedMap) {
        scopeExtendedMap.putIfAbsent(file, setOf(name))?.also { list ->
          scopeExtendedMap[file] = list + name
        }
      }
    }
  }
  val queueSize = fileToExtenders.values.sumOf { it.size }
  reportProgress(queueSize) { reporter ->
    coroutineScope {
      fileToExtenders.forEach { (file, extenders) ->
        extenders.forEach { extender ->
          launch {
            reporter.itemStep { extender.extendFrom(file) }
          }
        }
      }
    }
  }
  return scopeExtendedMap.filterKeys { it !in fileToExtenders }.toMap()
}