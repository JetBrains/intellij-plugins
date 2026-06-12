package org.jetbrains.qodana.inspectionKts.bta

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import org.jetbrains.kotlin.buildtools.api.ExecutionPolicy
import org.jetbrains.kotlin.buildtools.api.ExperimentalBuildToolsApi
import org.jetbrains.kotlin.buildtools.api.KotlinToolchains
import org.jetbrains.kotlin.idea.compiler.configuration.KotlinPluginLayout
import org.jetbrains.kotlin.utils.KotlinPaths
import org.jetbrains.kotlin.utils.KotlinPathsFromHomeDir
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Owns the long-lived BTA toolchain stack (URL classloader + [KotlinToolchains] + execution policy)
 * shared by every `.inspection.kts` compilation in this IDE process.
 */
@OptIn(ExperimentalBuildToolsApi::class)
@Service(Service.Level.APP)
internal class QodanaBtaToolchainService {
  companion object {
    fun getInstance(): QodanaBtaToolchainService = service()
  }

  private val cached: Lazy<CachedBtaToolchain> = lazy(LazyThreadSafetyMode.SYNCHRONIZED) { buildCachedToolchain() }

  fun get(): CachedBtaToolchain = cached.value

  private fun buildCachedToolchain(): CachedBtaToolchain {
    val toolchainClasspath = buildBtaToolchainClasspath()
    val ideJvmClasspath = toolchainClasspath.ideJvmOrder
    val daemonJvmClasspath = toolchainClasspath.daemonJvmOrder

    val kotlinApiCl = KotlinToolchains::class.java.classLoader
    val btaToolchainCl = HybridOrderUrlClassLoader(
      ideJvmOrder = ideJvmClasspath.map { it.toUri().toURL() }.toTypedArray(),
      daemonJvmOrder = daemonJvmClasspath.map { it.toUri().toURL() }.toTypedArray(),
      parent = BtaSharedClassesClassLoader(
        sharedPrefix = "org.jetbrains.kotlin.buildtools.api.",
        delegate = kotlinApiCl,
      ),
    )
    val toolchains = KotlinToolchains.loadImplementation(btaToolchainCl)
    val executionPolicy = toolchains.daemonExecutionPolicyBuilder().build()
    return CachedBtaToolchain(toolchains, executionPolicy, btaToolchainCl)
  }
}

@OptIn(ExperimentalBuildToolsApi::class)
internal class CachedBtaToolchain(
  val toolchains: KotlinToolchains,
  val executionPolicy: ExecutionPolicy,
  @Suppress("unused") // strong reference: keeps the URL classloader (and its classpath URLs) alive for the daemon's lifetime
  val toolchainClassLoader: ClassLoader,
)

private data class BtaToolchainClasspath(val ideJvmOrder: List<Path>, val daemonJvmOrder: List<Path>)

@OptIn(ExperimentalBuildToolsApi::class)
private fun buildBtaToolchainClasspath(): BtaToolchainClasspath {
  val toolchainJar = locateJarOf(KotlinToolchains::class.java)

  val bundledCompilerCp = kotlinHomeClasspath(KotlinPluginLayout.kotlincPath)
  val ideOrder = listOf(toolchainJar) + bundledCompilerCp

  // Daemon JVM `-cp` = the matching for-ide standalone dist (must match the in-process BTA-impl
  // version; mixing it with the bundled `kotlinc` makes the compiler-cli look up extension points the
  // other version's environment never registers)
  val daemonOrder = kotlinHomeClasspath(QodanaForIdeKotlincProvider.kotlincHome)

  return BtaToolchainClasspath(ideOrder, daemonOrder)
}

@Suppress("IO_FILE_USAGE")
internal fun kotlinHomeClasspath(home: Path): List<Path> =
  KotlinPathsFromHomeDir(home.toFile()).classPath(KotlinPaths.ClassPaths.CompilerWithScripting).map { it.toPath() }

internal fun locateJarOf(cls: Class<*>): Path =
  PathManager.getJarPathForClass(cls)?.let { Paths.get(it) }
  ?: error("Cannot locate JAR for ${cls.name}")

/**
 * Parent classloader for the BTA toolchain's URL classloader: it delegates a single package prefix
 * to [delegate] and loads nothing else itself. The shared prefix is
 * `org.jetbrains.kotlin.buildtools.api.*` → the IDE Kotlin plugin's classloader, so the ServiceLoaded
 * impl is type-compatible with the `KotlinToolchains` our code uses.
 */
private class BtaSharedClassesClassLoader(
  private val sharedPrefix: String,
  private val delegate: ClassLoader,
) : ClassLoader(getPlatformClassLoader()) {
  override fun loadClass(name: String, resolve: Boolean): Class<*> =
    if (name.startsWith(sharedPrefix)) delegate.loadClass(name)
    else super.loadClass(name, resolve)
}

/**
 * URLClassLoader that resolves classes in [ideJvmOrder] but reports [daemonJvmOrder] from
 * `getURLs()`. BTA reads the compilation operation's classloader `getURLs()` to build the daemon
 * JVM's `-cp`, so overriding `getURLs()` is how we hand the daemon a clean for-ide dist distinct
 * from the in-process classpath.
 */
private class HybridOrderUrlClassLoader(
  ideJvmOrder: Array<URL>,
  private val daemonJvmOrder: Array<URL>,
  parent: ClassLoader?,
) : URLClassLoader(ideJvmOrder, parent) {
  override fun getURLs(): Array<URL> = daemonJvmOrder.copyOf()
}
