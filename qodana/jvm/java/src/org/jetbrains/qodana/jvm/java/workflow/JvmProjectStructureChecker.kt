package org.jetbrains.qodana.jvm.java.workflow

import com.intellij.codeInspection.InspectionsBundle
import com.intellij.openapi.components.serviceAsync
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
import org.jetbrains.qodana.staticAnalysis.sarif.notifications.RuntimeNotificationCollector
import org.jetbrains.qodana.staticAnalysis.sarif.notifications.createSanityNotification
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension

class JvmProjectStructureChecker : QodanaWorkflowExtension {
  override suspend fun beforeLaunch(context: QodanaRunContext) {
    val project = context.project
    val notificationCollector = project.serviceAsync<RuntimeNotificationCollector>()
    for (module in project.modules) {
      val rootManager = ModuleRootManager.getInstance(module)
      val entries = rootManager.getOrderEntries()
      for (entry in entries) {
        when {
          entry is JdkOrderEntry && !ModuleType.get(module).isValidSdk(module, null) -> {
            val message = InspectionsBundle.message("offline.inspections.module.jdk.not.found",
                                                    entry.getJdkName(),
                                                    module.getName()
            )
            notificationCollector.add(createSanityNotification(message))
          }
          entry is LibraryOrderEntry -> {
            val library = entry.getLibrary()
            if (library == null) {
              val message = QodanaBundle.message("offline.inspections.library.was.not.resolved",
                                                 entry.getPresentableName(), module.getName())
              notificationCollector.add(createSanityNotification(message))
            }
            else {
              val detectedUrls = library.getFiles(OrderRootType.CLASSES).map { obj: VirtualFile -> obj.url }.toSet()
              val declaredUrls = library.getUrls(OrderRootType.CLASSES).toMutableSet()
              declaredUrls.removeAll(detectedUrls)
              declaredUrls.removeIf { url: String -> library.isJarDirectory(url) }
              if (!declaredUrls.isEmpty()) {
                val message = QodanaBundle.message("offline.inspections.library.urls.were.not.resolved",
                                                   StringUtil.join(declaredUrls, ", "),
                                                   entry.getPresentableName(), module.getName())
                notificationCollector.add(createSanityNotification(message))
              }
            }
          }
        }
      }
    }
  }
}