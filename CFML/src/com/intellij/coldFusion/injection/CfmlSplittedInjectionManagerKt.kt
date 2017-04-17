package com.intellij.coldFusion.injection

import com.intellij.openapi.extensions.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.psi.impl.PsiManagerEx
import com.intellij.util.containers.ContainerUtil

/**
 * @author Sergey Karashevich
 *
 * The reason to have this manager is to support SQL injection inside <cfquery> tag splitted by <cfqueryparam> or <cfif> tags. We want to
 * allow code completion and highlighting support inside these kind of blocks
 *
 * inspired by @see com.intellij.psi.impl.source.tree.injected.JavaConcatenationInjectorManager
 */
open class CfmlSplittedInjectionManagerKt(project: Project, psiManagerEx: PsiManagerEx): SimpleModificationTracker() {

  val SPLITTED_INJECTOR_EP_NAME: ExtensionPointName<SplittedInjector> = ExtensionPointName.create("CFML Support.splittedInjector")
  val mySplittedInjectors: MutableList<SplittedInjector>

  init {
    mySplittedInjectors = ContainerUtil.createLockFreeCopyOnWriteList()
    val concatPoint: ExtensionPoint<SplittedInjector> = Extensions.getArea(project).getExtensionPoint(SPLITTED_INJECTOR_EP_NAME)
    concatPoint.addExtensionPointListener(object : ExtensionPointListener<SplittedInjector> {
      override fun extensionAdded(injector: SplittedInjector, pluginDescriptor: PluginDescriptor?) = registerSplittedInjection(injector)

      override fun extensionRemoved(injector: SplittedInjector, pluginDescriptor: PluginDescriptor?) = unregisterSplittedInjection(injector)
    })
    psiManagerEx.registerRunnableToRunOnAnyChange({ incModificationCount() /* clear caches even on non-physical changes */ })
  }


  fun splittedInjectorsChanged() { incModificationCount() }

  fun registerSplittedInjection(injector: SplittedInjector): Unit {
    mySplittedInjectors.add(injector)
    splittedInjectorsChanged()
  }

  fun unregisterSplittedInjection(injector: SplittedInjector): Unit {
    mySplittedInjectors.remove(injector)
    splittedInjectorsChanged()
  }

}
