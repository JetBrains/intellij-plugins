package com.intellij.coldFusion.injection

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.extensions.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.psi.impl.PsiManagerEx
import com.intellij.util.containers.ContainerUtil

/**
 * @author Sergey Karashevich
 *
 * The reason to have this manager is to support SQL injection inside <cfquery> tag splitted by <cfqueryparam> or <cfif> tags. We want to
 * allow code completion and highlighting support inside these kind of blocks.
 *
 * Inspired by {@link com.intellij.psi.impl.source.tree.injected.JavaConcatenationInjectorManager}
 */
class CfmlSplittedInjectionManager(project: Project, psiManagerEx: PsiManagerEx): SimpleModificationTracker() {

  private val SPLITTED_INJECTOR_EP_NAME: ExtensionPointName<SplittedInjector> = ExtensionPointName.create("CFML Support.splittedInjector")
  val mySplittedInjectors: MutableList<SplittedInjector> = ContainerUtil.createLockFreeCopyOnWriteList()

  companion object {
    @JvmStatic
    fun getInstance(project: Project): CfmlSplittedInjectionManager {
      return ServiceManager.getService(project, CfmlSplittedInjectionManager::class.java)
    }
  }

  init {
    val concatPoint: ExtensionPoint<SplittedInjector> = Extensions.getArea(project).getExtensionPoint(SPLITTED_INJECTOR_EP_NAME)
    concatPoint.addExtensionPointListener(object : ExtensionPointListener<SplittedInjector> {
      override fun extensionAdded(injector: SplittedInjector, pluginDescriptor: PluginDescriptor?) = registerSplittedInjection(injector)

      override fun extensionRemoved(injector: SplittedInjector, pluginDescriptor: PluginDescriptor?) = unregisterSplittedInjection(injector)
    })
    psiManagerEx.registerRunnableToRunOnAnyChange({ incModificationCount() /* clear caches even on non-physical changes */ })
  }

  fun registerSplittedInjection(injector: SplittedInjector) {
    mySplittedInjectors.add(injector)
    incModificationCount()
  }

  fun unregisterSplittedInjection(injector: SplittedInjector) {
    mySplittedInjectors.remove(injector)
    incModificationCount()
  }

}
