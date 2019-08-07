package tanvd.grazi.remote

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.progress.impl.CoreProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
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
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.graph.DependencyNode
import org.eclipse.aether.graph.Exclusion
import org.eclipse.aether.impl.DefaultServiceLocator
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.transport.file.FileTransporterFactory
import org.eclipse.aether.transport.http.HttpTransporterFactory
import org.eclipse.aether.util.artifact.JavaScopes
import org.languagetool.Language
import org.languagetool.Languages
import org.slf4j.LoggerFactory
import tanvd.grazi.GraziConfig
import tanvd.grazi.GraziPlugin
import tanvd.grazi.ide.ui.components.dsl.msg
import tanvd.grazi.language.Lang
import java.io.File
import java.net.URL
import java.nio.file.Paths

object LangDownloader {
    private val logger = LoggerFactory.getLogger(LangDownloader::class.java)

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

    private val proxy = JreProxySelector

    private val MAVEN_CENTRAL_REPOSITORY =
            RemoteRepository.Builder("central", "default", msg("grazi.maven.repo.url"))
                    .setProxy(proxy.getProxy(msg("grazi.maven.repo.url"))).build()

    private fun Artifact.createDependency() = Dependency(this, JavaScopes.COMPILE, false, listOf(
            Exclusion("org.languagetool", "languagetool-core", "", "jar"),
            Exclusion("org.carrot2", "morfologik-fsa", "", "jar"),
            Exclusion("org.carrot2", "morfologik-stemming", "", "jar"),
            Exclusion("com.google.guava", "guava", "", "jar")
    ))

    private val Artifact.name
        get() = "$artifactId-$version.jar"

    private val Artifact.url
        get() = "${MAVEN_CENTRAL_REPOSITORY.url}${groupId.replace(".", "/")}/$artifactId/$version/$name"

    private val downloader by lazy { DownloadableFileService.getInstance() }

    private fun DependencyNode.traverse(action: (DependencyNode) -> Unit) {
        action(this)
        this.children.forEach(action)
    }

    fun downloadMissingLanguages(project: Project?) {
        val state = GraziConfig.get()

        if (state.hasMissedLanguages()) {
            state.enabledLanguages.filter { it.jLanguage == null }.forEach {
                with(LangDownloader) { it.downloadLanguage(project) }
            }

            if (state.nativeLanguage.jLanguage == null) {
                with(LangDownloader) { state.nativeLanguage.downloadLanguage(project) }
            }

            ProjectManager.getInstance().openProjects.forEach {
                DaemonCodeAnalyzer.getInstance(it).restart()
            }
        }
    }

    private fun Lang.registerInLanguageTool() {
        with(Languages::class.java.getDeclaredField("dynLanguages")) {
            isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val langs = get(null) as MutableList<Language>

            descriptor.langsClasses.forEach {
                langs.add(GraziPlugin.loadClass("org.languagetool.language.$it")!!.newInstance() as Language)
            }
        }
    }

    fun Lang.downloadLanguage(project: Project?): Boolean {
        // check if language lib already loaded
        if (GraziLibResolver.hasAllLibs(this)) {
            return true
        }

        val jars: MutableList<Artifact> = ArrayList()
        val isNotCancelled = CoreProgressManager.getInstance().runProcessWithProgressSynchronously({
            val artifact = DefaultArtifact("org.languagetool", "language-$shortCode", "jar", msg("grazi.languagetool.version"))
            val request = CollectRequest(artifact.createDependency(), listOf(MAVEN_CENTRAL_REPOSITORY))
            try {
                repository.collectDependencies(session, request).root.traverse { jars.add(it.artifact) }
            } catch (e: Throwable) {
                logger.trace("Download error", e)
            }
        }, msg("grazi.ui.settings.language.searching.title"), true, project)

        // jars must have at least one jar with language
        if (jars.isEmpty()) {
            // FIXME very ugly
            Messages.showWarningDialog(project, "Failed to download $displayName", "Download error")
        }

        if (isNotCancelled && jars.isNotEmpty()) {
            val descriptions = jars.map { downloader.createFileDescription(it.url, it.name) }.toList()

            val result = downloader.createDownloader(descriptions, "$displayName language")
                    .downloadFilesWithProgress(GraziPlugin.installationFolder.absolutePath + "/lib", project, null)

            // null if canceled or failed, zero result if nothing found
            if (result != null && result.size > 0) {
                with(UrlClassLoader::class.java.getDeclaredMethod("addURL", URL::class.java)) {
                    isAccessible = true
                    result.forEach {
                        invoke(GraziPlugin.classLoader, Paths.get(it.presentableUrl).toUri().toURL())
                    }
                }

                registerInLanguageTool()

                return true
            }
        }

        return false
    }
}
