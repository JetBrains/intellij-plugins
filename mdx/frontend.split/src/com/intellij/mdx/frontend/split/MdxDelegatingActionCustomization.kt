package com.intellij.mdx.frontend.split

import com.intellij.openapi.fileTypes.FileType
import com.jetbrains.rdclient.actions.base.FrontendSpeculativeActionCallStrategyCustomization
import org.intellij.plugin.mdx.lang.MdxFileType

internal class MdxDelegatingActionCustomization : FrontendSpeculativeActionCallStrategyCustomization() {
  override val fileTypes: Set<FileType> = setOf(MdxFileType)
}
