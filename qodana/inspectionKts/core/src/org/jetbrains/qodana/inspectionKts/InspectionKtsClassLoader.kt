package org.jetbrains.qodana.inspectionKts

import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginManagerCore.plugins
import com.intellij.ide.plugins.PluginMainDescriptor
import com.intellij.openapi.util.text.StringHash
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.containers.JBIterable
import java.io.IOException
import java.net.URL
import java.util.Collections
import java.util.Enumeration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.math.max

/**
 * Copy of [com.intellij.ide.script.IdeScriptEngineManagerImpl.AllPluginsLoader] but
 * with delegating of loading some classes to plugins's submodules, see [InspectionKtsPluginWithSubModulesClassLoader]
 */
internal class InspectionKtsClassLoader : ClassLoader(null) {
  private val myLuckyGuess: ConcurrentMap<Long, ClassLoader> = ConcurrentHashMap()

  @Throws(ClassNotFoundException::class)
  override fun findClass(name: String): Class<*> {
    //long ts = System.currentTimeMillis();

    val p0 = name.indexOf("$")
    val hasBase = p0 > 0
    val p1 = if (hasBase) name.indexOf("$", p0 + 1) else -1
    val base = if (hasBase) name.substring(0, max(p0, p1)) else name
    val hash = StringHash.buz(base)

    var c: Class<*>? = null
    val guess1 = myLuckyGuess[hash] // cached loader or "this" if not found
    val guess2 = myLuckyGuess[0L] // last recently used
    for (loader in JBIterable.of<ClassLoader?>(guess1, guess2)) {
      if (loader === this) throw ClassNotFoundException(name)
      if (loader == null) continue
      try {
        c = loader.loadClass(name)
        break
      }
      catch (ignored: ClassNotFoundException) {
      }
    }
    if (c == null) {
      for (descriptor in plugins) {
        val l = InspectionKtsPluginWithSubModulesClassLoader.forPlugin(descriptor)
        if (l == null || !hasBase && (l === guess1 || l === guess2)) continue
        try {
          if (hasBase) {
            l.loadClass(base)
            myLuckyGuess.putIfAbsent(hash, l)
          }
          try {
            c = l.loadClass(name)
            myLuckyGuess.putIfAbsent(hash, l)
            myLuckyGuess[0L] = l
            break
          }
          catch (e: ClassNotFoundException) {
            if (hasBase) break
            if (name.startsWith("java.") || name.startsWith("groovy.")) break
          }
        }
        catch (ignored: ClassNotFoundException) {
        }
      }
    }

    //LOG.info("AllPluginsLoader [" + StringUtil.formatDuration(System.currentTimeMillis() - ts) + "]: " + (c != null ? "+" : "-") + name);
    if (c != null) {
      return c
    }
    else {
      myLuckyGuess.putIfAbsent(hash, this)
      throw ClassNotFoundException(name)
    }
  }

  override fun findResource(name: String): URL {
    if (isAllowedPluginResource(name)) {
      for (descriptor in plugins) {
        val l = descriptor.pluginClassLoader
        val url = l?.getResource(name)
        if (url != null) return url
      }
    }
    return javaClass.classLoader.getResource(name)
  }

  @Throws(IOException::class)
  override fun findResources(name: String): Enumeration<URL> {
    if (isAllowedPluginResource(name)) {
      var result: MutableSet<URL>? = null
      for (descriptor in plugins) {
        val l = descriptor.pluginClassLoader
        val urls = l?.getResources(name)
        if (urls == null || !urls.hasMoreElements()) continue
        if (result == null) result = LinkedHashSet()
        ContainerUtil.addAll(result, urls)
      }
      if (result != null) {
        return Collections.enumeration(result)
      }
    }
    return javaClass.classLoader.getResources(name)
  }


  // used by kotlin engine
  @Suppress("unused")
  fun getUrls(): List<URL> {
    return plugins
      .flatMap { plugin -> plugin.pluginAndContentModuleClassLoaders() }
      .distinct()
      .flatMap { classLoader ->
        @Suppress("UNCHECKED_CAST")
        runCatching { classLoader.javaClass.getMethod("getUrls").invoke(classLoader) as Iterable<URL> }
          .getOrElse { emptyList() }
      }
      .distinct()
      .toList()
  }

  companion object {
    private fun isAllowedPluginResource(name: String): Boolean {
      // allow plugin engines but suppress all other resources
      return "META-INF/services/javax.script.ScriptEngineFactory" == name
    }
  }
}

private fun IdeaPluginDescriptor.pluginAndContentModuleClassLoaders(): List<ClassLoader> {
  return buildList {
    pluginClassLoader?.let(::add)
    if (this@pluginAndContentModuleClassLoaders is PluginMainDescriptor) {
      contentModules.mapNotNullTo(this) { it.pluginClassLoader }
    }
  }
}

private class InspectionKtsPluginWithSubModulesClassLoader(
  val mainPluginClassLoader: ClassLoader?,
  val subModulesClassLoaders: List<SubModuleClassLoader>
) : ClassLoader(null) {
  class SubModuleClassLoader(
    val modulePackage: String?,
    val classLoader: ClassLoader
  )

  companion object {
    fun forPlugin(plugin: IdeaPluginDescriptor): InspectionKtsPluginWithSubModulesClassLoader? {
      val pluginClassLoader = plugin.pluginClassLoader
      val subModulesClassLoaders = (plugin as? PluginMainDescriptor)?.contentModules?.mapNotNull {
        SubModuleClassLoader(it.packagePrefix, it.pluginClassLoader ?: return@mapNotNull null)
      }?.sortedBy { -(it.modulePackage?.length ?: 0) } ?: emptyList()
      if (pluginClassLoader == null && subModulesClassLoaders.isEmpty()) return null
      return InspectionKtsPluginWithSubModulesClassLoader(pluginClassLoader, subModulesClassLoaders)
    }
  }

  override fun loadClass(name: String?): Class<*> {
    val matchingModule = subModulesClassLoaders.find { module ->
      val modulePackage = module.modulePackage ?: return@find false
      name?.startsWith(modulePackage) ?: false
    }
    if (matchingModule != null) {
      try {
        return matchingModule.classLoader.loadClass(name)
      }
      catch (_: ClassNotFoundException) {
      }
    }
    mainPluginClassLoader?.let {
      try {
        return it.loadClass(name)
      }
      catch (_: ClassNotFoundException) {
      }
    }
    for (subModule in subModulesClassLoaders) {
      if (subModule == matchingModule) continue
      try {
        return subModule.classLoader.loadClass(name)
      }
      catch (_: ClassNotFoundException) {
      }
    }
    throw ClassNotFoundException(name)
  }
}
