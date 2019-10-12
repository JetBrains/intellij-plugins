// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie

import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.extensions.PluginId
import java.io.File

object GraziePlugin {
  const val id: String = "tanvd.grazi"

  private val descriptor: IdeaPluginDescriptor
    get() = PluginManager.getPlugin(PluginId.getId(id))!!

  val version: String
    get() = descriptor.version

  val classLoader: ClassLoader
    get() = descriptor.pluginClassLoader

  val libFolder: File
    get() = descriptor.path.resolve("lib")
}
