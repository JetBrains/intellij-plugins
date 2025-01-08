package org.jetbrains.qodana.inspectionKts

import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.IdeaPluginDescriptorImpl
import com.intellij.ide.plugins.PluginManagerCore.plugins
import com.intellij.openapi.util.text.StringHash
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.containers.JBIterable
import java.io.IOException
import java.net.URL
import java.util.*
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
    val hash = StringHash.calc(base)

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
    return JBIterable.of(*plugins)
      .map { obj: IdeaPluginDescriptor -> obj.classLoader }
      .unique()
      .flatMap { o: ClassLoader ->
        try {
          return@flatMap o.javaClass.getMethod("getUrls").invoke(o) as List<URL>
        }
        catch (e: Exception) {
          return@flatMap emptyList<URL>()
        }
      }
      .unique()
      .toList()
      .filterNotNull()
  }

  companion object {
    private fun isAllowedPluginResource(name: String): Boolean {
      // allow plugin engines but suppress all other resources
      return "META-INF/services/javax.script.ScriptEngineFactory" == name
    }
  }
}

private class InspectionKtsPluginWithSubModulesClassLoader(
  val mainPluginClassLoader: ClassLoader,
  val subModulesClassLoaders: List<SubModuleClassLoader>
) : ClassLoader(null) {
  class SubModuleClassLoader(
    val modulePackage: String,
    val classLoader: ClassLoader
  )

  companion object {
    fun forPlugin(plugin: IdeaPluginDescriptor): InspectionKtsPluginWithSubModulesClassLoader? {
      val pluginClassLoader = plugin.pluginClassLoader ?: return null
      val pluginImpl = (plugin as? IdeaPluginDescriptorImpl) ?: return null
      val subModulesClassLoaders = pluginImpl.content.modules.mapNotNull {
        val subModulePluginDescriptor = try {
          it.requireDescriptor()
        }
        catch (_ : IllegalStateException) {
          null
        } ?: return@mapNotNull null
        val modulePackage = subModulePluginDescriptor.packagePrefix ?: return@mapNotNull null
        SubModuleClassLoader(modulePackage, subModulePluginDescriptor.classLoader)
      }
      return InspectionKtsPluginWithSubModulesClassLoader(pluginClassLoader, subModulesClassLoaders)
    }
  }

  override fun loadClass(name: String?): Class<*> {
    val matchingModule = subModulesClassLoaders.find { name?.startsWith(it.modulePackage) ?: false }
    if (matchingModule != null) {
      try {
        return matchingModule.classLoader.loadClass(name)
      }
      catch (_ : ClassNotFoundException) {
      }
    }
    return mainPluginClassLoader.loadClass(name)
  }
}