package com.intellij.deno.modules

import com.intellij.deno.DenoSettings
import com.intellij.lang.ecmascript6.psi.impl.JSImportPathConfiguration
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.frameworks.modules.JSBaseModuleReferenceContributor
import com.intellij.lang.javascript.modules.JSModuleNameInfo
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
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

  override fun getModuleInfo(configuration: JSImportPathConfiguration,
                             moduleFileOrDirectory: VirtualFile,
                             resolvedModuleFile: VirtualFile): JSModuleNameInfo? {
    if (!TypeScriptUtil.isTypeScriptFile(resolvedModuleFile) ||
       TypeScriptUtil.isDefinitionFile(resolvedModuleFile)
    ) return null
    
    val place = configuration.place
    var externalModuleName = VfsUtilCore.findRelativePath(place.containingFile.virtualFile, moduleFileOrDirectory, '/')
    if (externalModuleName == null) return null
    if (!externalModuleName.startsWith(".") && !externalModuleName.startsWith(File.separator)) {
      externalModuleName = "./$externalModuleName"
    }

    val quote = JSCodeStyleSettings.getQuote(place)

    return object : JSModuleNameInfo {
      override fun getPath(): String {
        return quote + moduleName + quote
      }

      override fun getResolvedFile(): VirtualFile {
        return resolvedModuleFile
      }

      override fun getModuleName(): String {
        return externalModuleName
      }

      override fun isValid(): Boolean {
        return true
      }

      override fun getModuleFileOrDirectory(): VirtualFile {
        return moduleFileOrDirectory
      }

    }
  }
}