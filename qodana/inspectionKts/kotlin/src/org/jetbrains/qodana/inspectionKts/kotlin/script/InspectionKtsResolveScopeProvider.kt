package org.jetbrains.qodana.inspectionKts.kotlin.script

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.ResolveScopeProvider
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.idea.base.projectStructure.isKotlinBinary
import org.jetbrains.qodana.registry.QodanaRegistry

// QD-8717 FlexInspect (.inspection.kts): unresolved Kotlin PSI APIs if the Kotlin is present in the gradle kts project
internal class InspectionKtsResolveScopeProvider : ResolveScopeProvider() {
  override fun getResolveScope(file: VirtualFile, project: Project): GlobalSearchScope? {
    val fileType = file.fileType
    if (!fileType.isKotlinBinary && fileType != KotlinFileType.INSTANCE) return null
    val classpathProvider = inspectionKtsClasspathProvider(project, doInitialize = false)
    val currentDependenciesScope = classpathProvider.currentDependenciesScope()

    if (currentDependenciesScope == null || !QodanaRegistry.limitedInspectionKtsDependencies) {
      return null
    }
    return currentDependenciesScope.getResolveScopeForFile(project, file)
  }
}