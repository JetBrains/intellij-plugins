package org.jetbrains.qodana.inspectionKts.kotlin.script

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.ide.plugins.PluginManager
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.ide.plugins.cl.PluginClassLoader
import com.intellij.idea.AppMode
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.components.serviceIfCreated
import com.intellij.openapi.extensions.PluginDescriptor
import com.intellij.openapi.progress.checkCanceled
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.impl.PsiManagerEx
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.NonClasspathDirectoriesScope
import com.intellij.util.application
import com.intellij.util.io.URLUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import org.jetbrains.kotlin.idea.base.plugin.artifacts.KotlinArtifacts
import org.jetbrains.kotlin.idea.base.projectStructure.scope.KotlinSourceFilterScope
import org.jetbrains.kotlin.idea.base.projectStructure.toKaLibraryModule
import org.jetbrains.kotlin.idea.core.script.ScriptDependencyAware
import org.jetbrains.qodana.QodanaIntelliJYamlService
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.inspectionKts.CustomPluginsForKtsClasspathProvider
import org.jetbrains.qodana.inspectionKts.isInspectionKtsEnabled
import org.jetbrains.qodana.registry.QodanaRegistry
import java.io.File
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicReference
import kotlin.io.path.*
import kotlin.script.experimental.jvm.util.scriptCompilationClasspathFromContext

@Service
internal class InspectionKtsClasspathService(scope: CoroutineScope) {
  companion object {
    fun getInstance(): InspectionKtsClasspathService = service()

    fun getInstanceIfCreated(): InspectionKtsClasspathService? = serviceIfCreated()
  }

  internal class DependenciesScope(
    private val roots: Set<VirtualFile>,
    private val jarsDependenciesScope: NonClasspathDirectoriesScope,
  ) {
    fun getResolveScopeForFile(project: Project, file: VirtualFile): GlobalSearchScope? {
      if (!isUnderDependenciesRoot(file)) return null

      return GlobalSearchScope.union(
        listOfNotNull(
          ScriptDependencyAware.getInstance(project).getFirstScriptsSdk()?.toKaLibraryModule(project)?.contentScope,
          KotlinSourceFilterScope.libraryClasses(jarsDependenciesScope, project),
          GlobalSearchScope.fileScope(project, file),
        ).toTypedArray()
      )
    }

    private fun isUnderDependenciesRoot(file: VirtualFile): Boolean {
      if (roots.isEmpty()) {
        return false
      }

      val isJar = file.fileSystem is JarFileSystem
      val jarFile = if (isJar) {
        VfsUtilCore.getVirtualFileForJar(file) ?: file
      } else {
        file
      }
      return VfsUtilCore.isUnder(jarFile, roots)
    }
  }

  fun collectClassPath(): List<File> {
    return classPath
  }

  fun currentDependenciesScope(): DependenciesScope? {
    initializeDependenciesTask.start()
    return dependenciesScope.get()
  }

  private val dependenciesScope = AtomicReference<DependenciesScope?>(null)

  private val classPath: List<File> by lazy {
    if (isInspectionKtsResolveDisabled()) {
      return@lazy emptyList()
    }
    val useAllDistributionForDependencies = QodanaRegistry.useAllDistributionForInspectionKtsDependencies ||
                                            PluginManagerCore.isRunningFromSources()
    val classPath = mutableSetOf<File>()

    val platformClassLoader: ClassLoader = application::class.java.classLoader
    val qodanaPluginClassLoader: ClassLoader = QodanaRegistry::class.java.classLoader
    val pluginsWithLanguageParserClassLoaders = LanguageParserDefinitions.INSTANCE.point?.extensionList?.mapNotNull { it.instance.javaClass.classLoader }
    val distributionPluginsClassLoaders = PluginManager.getPlugins()
      .filter { useAllDistributionForDependencies || !it.isJetBrainsOrBundledPlugin }.map { it.classLoader }
    val customPluginClassLoaders = CustomPluginsForKtsClasspathProvider.provide().map { it.classLoader }

    listOf(
      listOf(platformClassLoader, qodanaPluginClassLoader),
      pluginsWithLanguageParserClassLoaders ?: emptyList(),
      distributionPluginsClassLoaders,
      customPluginClassLoaders
    ).flatten().toSet().forEach { classLoader ->
      val jars = when {
        useAllDistributionForDependencies -> scriptCompilationClasspathFromContext(classLoader = classLoader, wholeClasspath = true)
        classLoader is PluginClassLoader -> pluginJars(classLoader)
        else -> platformJars(classLoader)
      }
      classPath.addAll(jars)
    }

    val kotlinClasspath = listOf(KotlinArtifacts.kotlinReflect, KotlinArtifacts.kotlinStdlib, KotlinArtifacts.kotlinScriptRuntime)
    classPath.addAll(kotlinClasspath)

    classPath.toList()
  }

  private val initializeDependenciesTask = scope.launch(context = QodanaDispatchers.Default, start = CoroutineStart.LAZY) {
    val dependenciesRoots = collectDependenciesRoots()
    val dependenciesScope = collectDependenciesScope()
    this@InspectionKtsClasspathService.dependenciesScope.set(DependenciesScope(dependenciesRoots, dependenciesScope))

    rehighlightProjectsFiles()
  }

  private fun rehighlightProjectsFiles() {
    ProjectManager.getInstance().openProjects.forEach { project ->
      PsiManagerEx.getInstanceEx(project).dropResolveCaches()
      DaemonCodeAnalyzer.getInstance(project).restart()
    }
  }

  private suspend fun collectDependenciesRoots(): Set<VirtualFile> {
    if (isInspectionKtsResolveDisabled()) {
      return emptySet()
    }

    val compiledFromSourcesRoot = if (AppMode.isDevServer() || PluginManagerCore.isRunningFromSources()) {
      commonRoot(classPath)
    } else {
      null
    }
    checkCanceled()
    val homePath = runInterruptible(QodanaDispatchers.IO) {
      StandardFileSystems.local().findFileByPath(PathManager.getHomePath())
    }
    checkCanceled()
    val configPath = runInterruptible(QodanaDispatchers.IO) {
      StandardFileSystems.local().findFileByPath(PathManager.getConfigPath())
    }
    return setOfNotNull(homePath, configPath, compiledFromSourcesRoot)
  }

  private suspend fun collectDependenciesScope(): NonClasspathDirectoriesScope {
    val dependencies = classPath.mapNotNull { file ->
      val nioFile = file.toPath()
      checkCanceled()
      val virtualFile = when {
        nioFile.isDirectory() -> StandardFileSystems.local().findFileByPath(nioFile.pathString)
        nioFile.isRegularFile() -> StandardFileSystems.jar().findFileByPath(nioFile.pathString + URLUtil.JAR_SEPARATOR)
        else -> null
      }
      virtualFile
    }
    return NonClasspathDirectoriesScope(dependencies)
  }
}

private fun pluginJars(pluginClassLoader: PluginClassLoader): List<File> {
  val plugin = pluginClassLoader.pluginDescriptor

  // blacklist: ignore some stuff from specific plugins
  fun isPluginAdditionalJarAccepted(jarName: String): Boolean {
    if (!jarName.endsWith(".jar")) return true

    val pluginId = plugin.pluginId.idString
    val isJavaPlugin = pluginId == "com.intellij.java"
    val isKotlinPlugin = pluginId == "org.jetbrains.kotlin"
    val isDatabasePlugin = pluginId == "com.intellij.database"
    val isYamlPlugin = pluginId == "org.jetbrains.plugins.yaml"
    return when {
      isJavaPlugin -> {
        jarName.startsWith("java-impl-frontend") // part of Java's PSI
      }
      isKotlinPlugin -> {
        jarName.startsWith("kotlinc.kotlin-compiler-common") || // Kotlin PSI
        jarName.startsWith("kotlinc.high-level-api") || // Semantics of Kotlin PSI (old JAR naming)
        jarName.startsWith("kotlinc.analysis-api") // Semantics of Kotlin PSI
      }
      isDatabasePlugin -> {
        jarName.startsWith("database-openapi")
      }
      isYamlPlugin -> {
        jarName.startsWith("intellij.yaml") // YAML PSI
      }
      else -> true
    }
  }

  val pluginJars = pluginClassLoader.baseUrls.toSet()

  val pluginXmlPath = pluginClassLoader.getResource("META-INF/plugin.xml")?.let { pluginXml ->
    Path(URLDecoder.decode(pluginXml.path.substringAfterLast(":"), StandardCharsets.UTF_8)).toAbsolutePath()
  }

  val (pluginMainJar, pluginRestJars) = pluginJars.partition { jar -> 
    pluginXmlPath != null && pluginXmlPath.toString().startsWith(jar.toString()) 
  }
  val pluginAdditionalJars = pluginRestJars.filter { jar -> isPluginAdditionalJarAccepted(jar.name) }

  return (pluginMainJar + pluginAdditionalJars).map { it.toFile() }
}

private fun platformJars(classLoader: ClassLoader): List<File> {
  fun isPlatformJarAccepted(jarName: String): Boolean {
    return !jarName.endsWith(".jar") ||
           jarName.startsWith("util-") ||
           jarName.startsWith("app") ||
           jarName.contains("annotation", ignoreCase = true)
  }

  return scriptCompilationClasspathFromContext(classLoader = classLoader, wholeClasspath = true).filter { isPlatformJarAccepted(it.name) }
}

private val PluginDescriptor.isJetBrainsOrBundledPlugin: Boolean
  get() = isBundled || vendor?.contains("jetbrains", ignoreCase = true) ?: false

private suspend fun commonRoot(dependenciesJars: List<File>): VirtualFile? {
  if (dependenciesJars.isEmpty()) return null

  val pathLists: List<List<String>> = dependenciesJars
    .map { it.absolutePath.split(File.separator).filter { part -> part.isNotEmpty() } }

  val minSize = pathLists.minOf { it.size }

  val commonRoot: MutableList<String> = mutableListOf()
  for (i in 0 until minSize) {
    checkCanceled()
    val currentPart = pathLists[0][i]
    if (pathLists.all {
      checkCanceled()
      it[i] == currentPart
    }) {
      commonRoot.add(currentPart)
    } else {
      break
    }
  }
  if (commonRoot.isEmpty()) return null

  val rootFile = File(File.separator + commonRoot.joinToString(File.separator))
  val root = runInterruptible(QodanaDispatchers.IO) {
    StandardFileSystems.local().findFileByPath(rootFile.path) ?: return@runInterruptible null
  }
  return root
}

private fun isInspectionKtsResolveDisabled(): Boolean {
  return application.isHeadlessEnvironment ||
         !isInspectionKtsEnabled() ||
         isInspectionKtsResolveDisabledByProjectSettings()
}

private fun isInspectionKtsResolveDisabledByProjectSettings(): Boolean {
  return ProjectManager.getInstance().openProjects.any { project ->
    QodanaIntelliJYamlService.getInstance(project).disableInspectionKtsResolve
  }
}