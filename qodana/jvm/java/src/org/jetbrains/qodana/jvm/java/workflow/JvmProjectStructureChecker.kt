package org.jetbrains.qodana.jvm.java.workflow

import com.intellij.codeInspection.InspectionsBundle
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.project.modules
import com.intellij.openapi.roots.JdkOrderEntry
import com.intellij.openapi.roots.LibraryOrderEntry
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunContext
import org.jetbrains.qodana.staticAnalysis.sarif.notifications.addSanityNotification
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension

class JvmProjectStructureChecker : QodanaWorkflowExtension {
  override suspend fun beforeLaunch(context: QodanaRunContext) {
    val project = context.project
    for (module in project.modules) {
      val rootManager = ModuleRootManager.getInstance(module)
      val entries = rootManager.getOrderEntries()
      for (entry in entries) {
        if (entry is JdkOrderEntry) {
          if (!ModuleType.get(module).isValidSdk(module, null)) {
            addSanityNotification(project, InspectionsBundle.message("offline.inspections.module.jdk.not.found", entry.getJdkName(),
                                                                     module.getName()))

          }
        }
        else if (entry is LibraryOrderEntry) {
          val library = entry.getLibrary()
          if (library == null) {
            addSanityNotification(project, QodanaBundle.message("offline.inspections.library.was.not.resolved",
                                                                entry.getPresentableName(), module.getName()))
          }
          else {
            val detectedUrls = library.getFiles(OrderRootType.CLASSES).map { obj: VirtualFile -> obj.url }.toSet()
            val declaredUrls = library.getUrls(OrderRootType.CLASSES).toMutableSet()
            declaredUrls.removeAll(detectedUrls)
            declaredUrls.removeIf { url: String -> library.isJarDirectory(url) }
            if (!declaredUrls.isEmpty()) {
              addSanityNotification(project, QodanaBundle.message("offline.inspections.library.urls.were.not.resolved",
                                                                  StringUtil.join(declaredUrls, ", "),
                                                                  entry.getPresentableName(), module.getName()))
            }
          }
        }
      }
    }
  }
}