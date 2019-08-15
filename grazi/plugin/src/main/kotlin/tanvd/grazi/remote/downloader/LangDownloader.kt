package tanvd.grazi.remote.downloader

import com.intellij.openapi.progress.impl.CoreProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.util.download.DownloadableFileService
import com.intellij.util.lang.UrlClassLoader
import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory
import org.eclipse.aether.impl.DefaultServiceLocator
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.transport.file.FileTransporterFactory
import org.eclipse.aether.transport.http.HttpTransporterFactory
import org.slf4j.LoggerFactory
import tanvd.grazi.GraziConfig
import tanvd.grazi.GraziPlugin
import tanvd.grazi.ide.ui.components.dsl.msg
import tanvd.grazi.language.Lang
import tanvd.grazi.remote.GraziRemote
import tanvd.grazi.utils.LangToolInstrumentation
import tanvd.grazi.utils.addUrls
import java.io.File
import java.nio.file.Paths

object LangDownloader {
    private val logger = LoggerFactory.getLogger(LangDownloader::class.java)

    val MAVEN_CENTRAL_REPOSITORY = RemoteRepository.Builder("central", "default", msg("grazi.maven.repo.url"))
            .setProxy(JreProxySelector.getProxy(msg("grazi.maven.repo.url"))).build()

    private val repository: RepositorySystem by lazy {
        with(MavenRepositorySystemUtils.newServiceLocator()) {
            addService(RepositoryConnectorFactory::class.java, BasicRepositoryConnectorFactory::class.java)
            addService(TransporterFactory::class.java, FileTransporterFactory::class.java)
            addService(TransporterFactory::class.java, HttpTransporterFactory::class.java)
            setErrorHandler(object : DefaultServiceLocator.ErrorHandler() {
                override fun serviceCreationFailed(type: Class<*>?, impl: Class<*>?, exception: Throwable?) {
                    exception?.let { throw RuntimeException(it) }
                }
            })

            getService(RepositorySystem::class.java)
        }
    }

    private val session: RepositorySystemSession by lazy {
        with(MavenRepositorySystemUtils.newSession()) {
            localRepositoryManager = repository.newLocalRepositoryManager(this, LocalRepository(File(GraziPlugin.installationFolder, "poms")))
            setProxySelector(JreProxySelector)
        }
    }

    private val downloader by lazy { DownloadableFileService.getInstance() }

    fun download(lang: Lang, project: Project?): Boolean {
        // check if language lib already loaded
        if (GraziRemote.isAvailableLocally(lang)) return true

        val jars: MutableList<Artifact> = ArrayList()
        val isNotCancelled = CoreProgressManager.getInstance().runProcessWithProgressSynchronously({
            val artifact = DefaultArtifact("org.languagetool", "language-${lang.shortCode}", "jar", msg("grazi.languagetool.version"))
            val request = CollectRequest(artifact.createDependency(), listOf(MAVEN_CENTRAL_REPOSITORY))
            try {
                repository.collectDependencies(session, request).root.traverse { jars.add(it.artifact) }
            } catch (e: Throwable) {
                logger.trace("Download error", e)
            }
        }, msg("grazi.ui.settings.language.searching.title"), true, project)

        // jars must have at least one jar with language
        if (jars.isEmpty()) {
            Messages.showWarningDialog(project, "Failed to download ${lang.displayName}", "Download error")
        }

        if (isNotCancelled && jars.isNotEmpty()) {
            val descriptions = jars.map { downloader.createFileDescription(it.url, it.name) }.toList()

            val result = downloader.createDownloader(descriptions, "${lang.displayName} language")
                    .downloadFilesWithProgress(GraziPlugin.installationFolder.absolutePath + "/lib", project, null)

            // null if canceled or failed, zero result if nothing found
            if (result != null && result.size > 0) {
                (GraziPlugin.classLoader as UrlClassLoader).addUrls(result.map { Paths.get(it.presentableUrl).toUri().toURL() }.toList())
                LangToolInstrumentation.registerLanguage(lang)
                GraziConfig.update { state -> state.update() }
                return true
            }
        }
        return false
    }
}
