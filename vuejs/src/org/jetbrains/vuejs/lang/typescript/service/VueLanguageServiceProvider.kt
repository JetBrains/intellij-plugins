// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.typescript.service

import com.intellij.ide.plugins.DynamicPlugins
import com.intellij.lang.javascript.service.JSLanguageService
import com.intellij.lang.javascript.service.JSLanguageServiceProvider
import com.intellij.lang.typescript.compiler.TypeScriptLanguageServiceProvider
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.AtomicNotNullLazyValue
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.vuejs.lang.html.VueFileType

class VueLanguageServiceProvider(project: Project) : JSLanguageServiceProvider {
  private val myLanguageService: AtomicNotNullLazyValue<VueTypeScriptService>

  init {
    myLanguageService = AtomicNotNullLazyValue.createValue<VueTypeScriptService> {
      val service = VueTypeScriptService(project)
      Disposer.register(project, service)
      return@createValue service
    }

    Disposer.register(DynamicPlugins.pluginDisposable(VueLanguageServiceProvider::class.java, project), Disposable {
      if (myLanguageService.isComputed) {
        Disposer.dispose(myLanguageService.value)
      }
    })
  }

  override fun getAllServices(): List<JSLanguageService> {
    val value = myLanguageService.value
    return listOf(value)
  }

  override fun getService(file: VirtualFile): JSLanguageService? {
    val value = myLanguageService.value
    return if (value.isAcceptable(file)) value else null
  }

  override fun isCandidate(file: VirtualFile): Boolean {
    val type = file.fileType
    return TypeScriptLanguageServiceProvider.isJavaScriptOrTypeScriptFileType(type) || type == VueFileType.INSTANCE
  }
}
