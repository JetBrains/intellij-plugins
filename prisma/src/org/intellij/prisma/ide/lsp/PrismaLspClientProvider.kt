package org.intellij.prisma.ide.lsp

import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.lang.typescript.lsp.LspServerLoader
import com.intellij.lang.typescript.lsp.LspServerPackageDescriptor
import com.intellij.lang.typescript.lsp.PackageVersion
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspClient
import com.intellij.platform.lsp.api.LspClientManager
import com.intellij.platform.lsp.api.LspClientProvider
import com.intellij.platform.lsp.api.lsWidget.LspClientWidgetItem
import com.intellij.util.text.SemVer
import org.intellij.prisma.PrismaIcons
import org.intellij.prisma.ide.ui.PrismaSettingsConfigurable

private object PrismaLspServerPackageDescriptor : LspServerPackageDescriptor("@prisma/language-server",
                                                                             PackageVersion.downloadable("31.11.0"),
                                                                             "/dist/bin.js") {
  private val sinceNewServiceLayoutVersion = SemVer.parseFromText("5.15.0")!! // inclusive

  override val registryVersion: String get() = Registry.stringValue("prisma.language.server.default.version")

  override fun getPackageRelativePath(project: Project, pkg: NodePackage): String {
    val version = pkg.version
    if (version != null && !version.isGreaterOrEqualThan(sinceNewServiceLayoutVersion)) {
      return "/dist/src/bin.js"
    }

    return super.getPackageRelativePath(project, pkg)
  }
}

class PrismaLspClientProvider : LspClientProvider {
  override fun fileOpened(project: Project, file: VirtualFile, clientStarter: LspClientProvider.LspClientStarter) {
    if (PrismaLspServerActivationRule.isEnabledAndAvailable(project, file)) {
      clientStarter.ensureClientStarted(PrismaLspClientDescriptor(project))
    }
  }

  override fun createWidgetItem(lspClient: LspClient, currentFile: VirtualFile?): LspClientWidgetItem =
    LspClientWidgetItem(lspClient, currentFile, PrismaIcons.PRISMA, settingsPageClass = PrismaSettingsConfigurable::class.java)
}

fun restartPrismaServerAsync(project: Project) {
  ApplicationManager.getApplication().invokeLater(Runnable {
    LspClientManager.getInstance(project).stopAndRestartClientsIfNeeded(PrismaLspClientProvider::class.java)
  }, project.disposed)
}

object PrismaLspServerLoader : LspServerLoader(PrismaLspServerPackageDescriptor) {
  override fun getSelectedPackage(project: Project): NodePackage =
    PrismaServiceSettings.getInstance(project).lspServerPackage
}