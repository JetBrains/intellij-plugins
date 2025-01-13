package org.jetbrains.qodana.inspectionKts.kotlin.script

import com.intellij.openapi.progress.isInCancellableContext
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.ResolveScopeProvider
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.qodana.registry.QodanaRegistry

// QD-8717 FlexInspect (.inspection.kts): unresolved Kotlin PSI APIs if the Kotlin is present in the gradle kts project
internal class InspectionKtsResolveScopeProvider : ResolveScopeProvider() {
  override fun getResolveScope(file: VirtualFile, project: Project): GlobalSearchScope? {
    // QD-9518 Debugger calls getResolveScope on EDT.
    // We don't want to do non-cancellable runBlocking because it can lead to deadlock,
    // so instead we just don't compute anything when there is no indicator (when the call is not cancellable).
    // Regular resolve is cancellable, so it's fine
    if (!isInCancellableContext()) return null

    val inspectionKtsDependenciesService = InspectionKtsClasspathService.getInstanceIfCreated() ?: return null
    if (!QodanaRegistry.limitedInspectionKtsDependencies) return null

    val dependenciesScope = runBlockingCancellable {
      if (!inspectionKtsDependenciesService.isUnderDependenciesRoot(file)) return@runBlockingCancellable null

      inspectionKtsDependenciesService.collectDependenciesScope(project)
    } ?: return null

    return GlobalSearchScope.union(
      arrayOf(
        GlobalSearchScope.fileScope(project, file),
        dependenciesScope
      )
    )
  }
}