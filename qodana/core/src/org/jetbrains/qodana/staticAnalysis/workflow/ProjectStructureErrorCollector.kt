package org.jetbrains.qodana.staticAnalysis.workflow

import com.intellij.codeInspection.InspectionsBundle
import com.intellij.openapi.components.service
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.project.modules
import com.intellij.openapi.roots.JdkOrderEntry
import com.intellij.openapi.roots.LibraryOrderEntry
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.containers.ContainerUtil
import com.jetbrains.qodana.sarif.model.Message
import com.jetbrains.qodana.sarif.model.Notification
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunContext
import org.jetbrains.qodana.staticAnalysis.profile.SanityInspectionGroup.Companion.SANITY_FAILURE_NOTIFICATION
import org.jetbrains.qodana.staticAnalysis.sarif.notifications.RuntimeNotificationCollector
import org.jetbrains.qodana.staticAnalysis.sarif.withKind
import java.time.Instant

private class ProjectStructureErrorCollector : QodanaWorkflowExtension {
  override suspend fun beforeLaunch(context: QodanaRunContext) {
    val project = context.project
    for (module in project.modules) {
      val rootManager = ModuleRootManager.getInstance(module)
      val entries = rootManager.getOrderEntries()
      for (entry in entries) {
        if (entry is JdkOrderEntry) {
          if (!ModuleType.get(module).isValidSdk(module, null)) {
            System.err.println(InspectionsBundle.message("offline.inspections.module.jdk.not.found", entry.getJdkName(),
                                                         module.getName()))

          }
        }
        else if (entry is LibraryOrderEntry) {
          val library = entry.getLibrary()
          if (library == null) {
            System.err.println(QodanaBundle.message("offline.inspections.library.was.not.resolved",
                                                  entry.getPresentableName(), module.getName()))
          }
          else {
            val detectedUrls = library.getFiles(OrderRootType.CLASSES).map { obj: VirtualFile -> obj.url }.toSet()
            val declaredUrls: MutableSet<String?> = ContainerUtil.newHashSet<String?>(*library.getUrls(OrderRootType.CLASSES))
            declaredUrls.removeAll(detectedUrls)
            declaredUrls.removeIf { url: String? -> library.isJarDirectory(url!!) }
            if (!declaredUrls.isEmpty()) {
              project.service<RuntimeNotificationCollector>()
                .add(
                  Notification().withLevel(Notification.Level.ERROR)
                    .withTimeUtc(Instant.now())
                    .withMessage(
                      Message()
                        .withText(QodanaBundle.message("offline.inspections.library.urls.were.not.resolved",
                                                       StringUtil.join(declaredUrls, ", "),
                                                       entry.getPresentableName(), module.getName())
                        )
                    )
                    .withKind(SANITY_FAILURE_NOTIFICATION)
                )
            }
          }
        }
      }
    }
  }
}