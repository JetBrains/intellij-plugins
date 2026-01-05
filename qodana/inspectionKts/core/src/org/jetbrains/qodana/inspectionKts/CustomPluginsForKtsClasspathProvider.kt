package org.jetbrains.qodana.inspectionKts

import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.openapi.extensions.ExtensionPointName

@Suppress("UnresolvedPluginConfigReference")
interface CustomPluginsForKtsClasspathProvider {
  companion object {
    private val EP_NAME: ExtensionPointName<CustomPluginsForKtsClasspathProvider> = ExtensionPointName.create("org.intellij.qodana.customPluginsForKtsClasspathProvider")

    fun provide(): Collection<IdeaPluginDescriptor> = EP_NAME.extensionList.flatMap { it.provide() }
  }

  fun provide(): Collection<IdeaPluginDescriptor>
}