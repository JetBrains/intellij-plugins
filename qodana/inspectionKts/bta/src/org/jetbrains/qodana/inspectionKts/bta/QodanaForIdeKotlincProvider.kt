package org.jetbrains.qodana.inspectionKts.bta

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.util.io.Decompressor
import org.jetbrains.kotlin.idea.base.plugin.artifacts.KotlinArtifactConstants
import org.jetbrains.kotlin.idea.compiler.configuration.KotlinPluginLayout
import java.io.IOException
import java.net.URI
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

/**
 * Mirrors the IDE's JSR223 kotlinc provider
 */
internal object QodanaForIdeKotlincProvider {
  private const val KOTLIN_DIST_FOR_IDE_ARTIFACT_ID = "kotlin-dist-for-ide"
  private const val KOTLIN_MAVEN_GROUP_PATH = "org/jetbrains/kotlin"
  private const val DOWNLOADS_DIR_NAME = "downloads"
  private const val BUILD_TXT = "build.txt"
  private const val LIB_DIR_NAME = "lib"
  private const val GET_URLS_METHOD_NAME = "getUrls"
  private const val FILE_URL_PROTOCOL = "file"

  val kotlincHome: Path by lazy { resolveKotlincHome() }

  private fun resolveKotlincHome(): Path {
    val version = KotlinPluginLayout.ideCompilerVersion.rawVersion
    val home = KotlinArtifactConstants.KOTLIN_DIST_LOCATION_PREFIX_PATH.resolve(version)
    if (isKotlincHome(home)) return home
    val distJar = findDistJarOnClasspath(version)
                  ?: downloadDistJar(version)
                  ?: error("kotlin-dist-for-ide:$version is unavailable — not unpacked at $home, not on the classpath, and could not be downloaded.")
    Files.createDirectories(home)
    Decompressor.Zip(distJar).overwrite(true).extract(home)
    check(isKotlincHome(home)) { "kotlin-dist-for-ide:$version unpacked incompletely at $home" }
    return home
  }

  private fun isKotlincHome(home: Path): Boolean =
    Files.exists(home.resolve(BUILD_TXT)) && Files.isDirectory(home.resolve(LIB_DIR_NAME))

  private fun distJarName(version: String): String = "$KOTLIN_DIST_FOR_IDE_ARTIFACT_ID-$version.jar"

  private fun findDistJarOnClasspath(version: String): Path? {
    val jarName = distJarName(version)
    val classLoaders = sequenceOf(javaClass.classLoader) +
                       PluginManagerCore.loadedPlugins.asSequence().mapNotNull { it.pluginClassLoader }
    return classLoaders
      .flatMap { classpathUrlsOf(it) }
      .mapNotNull { url -> if (url.protocol == FILE_URL_PROTOCOL) runCatching { Paths.get(url.toURI()) }.getOrNull() else null }
      .firstOrNull { it.fileName?.toString() == jarName && Files.exists(it) }
  }

  private fun classpathUrlsOf(classLoader: ClassLoader): Sequence<URL> = sequence {
    if (classLoader is URLClassLoader) yieldAll(classLoader.urLs.asSequence())
    val reflected = runCatching {
      @Suppress("UNCHECKED_CAST")
      classLoader.javaClass.getMethod(GET_URLS_METHOD_NAME).invoke(classLoader) as? Iterable<URL>
    }.getOrNull()
    if (reflected != null) yieldAll(reflected)
  }

  private fun downloadDistJar(version: String): Path? {
    val jarName = distJarName(version)
    val target = KotlinArtifactConstants.KOTLIN_DIST_LOCATION_PREFIX_PATH.resolve(DOWNLOADS_DIR_NAME).resolve(jarName)
    if (Files.exists(target)) return target
    Files.createDirectories(target.parent)
    val artifactPath = "$KOTLIN_MAVEN_GROUP_PATH/$KOTLIN_DIST_FOR_IDE_ARTIFACT_ID/$version/$jarName"
    val mirrors = listOf(
      "https://cache-redirector.jetbrains.com/packages.jetbrains.team/maven/p/ij/intellij-dependencies/$artifactPath",
      "https://cache-redirector.jetbrains.com/intellij-dependencies/$artifactPath",
      "https://repo1.maven.org/maven2/$artifactPath",
    )
    val temp = target.resolveSibling("${target.fileName}.tmp")
    for (mirror in mirrors) {
      try {
        URI.create(mirror).toURL().openStream().use { Files.copy(it, temp, StandardCopyOption.REPLACE_EXISTING) }
        Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING)
        return target
      }
      catch (_: IOException) {
        Files.deleteIfExists(temp)
      }
    }
    return null
  }
}
