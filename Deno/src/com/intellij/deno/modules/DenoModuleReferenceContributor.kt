package com.intellij.deno.modules

import com.intellij.deno.DenoSettings
import com.intellij.lang.ecmascript6.psi.impl.JSImportPathConfiguration
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil
import com.intellij.lang.javascript.frameworks.modules.JSBaseModuleReferenceContributor
import com.intellij.lang.javascript.modules.JSModuleNameInfo
import com.intellij.lang.javascript.modules.JSModuleNameInfo.ExtensionSettings
import com.intellij.lang.javascript.modules.JSModuleNameInfoImpl
import com.intellij.lang.javascript.modules.imports.JSImportDescriptor
import com.intellij.lang.javascript.modules.imports.JSSimpleImportDescriptor
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import java.io.File


class DenoModuleReferenceContributor : JSBaseModuleReferenceContributor() {
  override fun isApplicable(host: PsiElement): Boolean {
    return DenoSettings.getService(host.project).isUseDeno()
  }

  override fun getReferences(unquotedRefText: String,
                             host: PsiElement,
                             offset: Int,
                             provider: PsiReferenceProvider?,
                             isCommonJS: Boolean): Array<PsiReference> {
    return emptyArray()
  }

  override fun getImportDescriptors(configuration: JSImportPathConfiguration,
                                    baseDescriptor: JSImportDescriptor): List<JSImportDescriptor> {
    val moduleDescriptor = baseDescriptor.moduleDescriptor
    if (moduleDescriptor !is JSModuleNameInfo) return emptyList()
    val resolvedModuleFile = moduleDescriptor.resolvedFile
    val moduleFileOrDirectory = moduleDescriptor.moduleFileOrDirectory

    if (!TypeScriptUtil.isTypeScriptFile(resolvedModuleFile) ||
        TypeScriptUtil.isDefinitionFile(resolvedModuleFile)
    ) return emptyList()

    val place = configuration.place
    val contextFile = place.containingFile?.virtualFile
    if (contextFile == null) return emptyList()
    
    var externalModuleName = VfsUtilCore.findRelativePath(contextFile, moduleFileOrDirectory, '/')
    if (externalModuleName == null) return emptyList()
    if (!externalModuleName.startsWith(".") && !externalModuleName.startsWith(File.separator)) {
      externalModuleName = "./$externalModuleName"
    }

    val newInfo = JSModuleNameInfoImpl(externalModuleName, moduleFileOrDirectory, resolvedModuleFile, place, emptyArray(),
                                       ExtensionSettings.EXACT)
    return listOf(JSSimpleImportDescriptor(newInfo, baseDescriptor))
  }
}