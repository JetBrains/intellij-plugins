// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.psi

import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.lang.ecmascript6.psi.ES6ExportSpecifierAlias
import com.intellij.lang.ecmascript6.psi.ES6ImportExportSpecifier
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.ecmascript6.TypeScriptQualifiedItemProcessor
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.resolve.ResolveResultSink
import com.intellij.lang.javascript.ui.NodeModuleNamesUtil.PACKAGE_JSON
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.Pair.create
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.ObjectUtils.tryCast
import com.intellij.util.asSafely
import org.angular2.entities.metadata.Angular2MetadataFileType.D_TS_SUFFIX
import org.angular2.entities.metadata.Angular2MetadataFileType.METADATA_SUFFIX
import org.angular2.entities.metadata.stubs.Angular2MetadataModuleExportStub
import org.angular2.entities.metadata.stubs.Angular2MetadataNodeModuleStub
import org.angular2.lang.metadata.psi.MetadataElement

class Angular2MetadataNodeModule(element: Angular2MetadataNodeModuleStub) : Angular2MetadataElement<Angular2MetadataNodeModuleStub>(
  element) {

  val isPackageTypingsRoot: Boolean
    get() = CachedValuesManager.getCachedValue(this) {
      CachedValueProvider.Result.create(
        checkPackageJson(PACKAGE_JSON, "./") || checkPackageJson(
          containingFile.name.removeSuffix(METADATA_SUFFIX) + "/" + PACKAGE_JSON,
          "../"),
        VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
    }

  fun <T : PsiElement> locateFileAndMember(memberName: String, memberClass: Class<T>): Pair<PsiFile, T> {
    val definitionPsi = loadRelativeFile(containingFile.name.removeSuffix(METADATA_SUFFIX), D_TS_SUFFIX)
    var result: T? = null
    if (definitionPsi is JSFile) {
      val sink = ResolveResultSink(definitionPsi, memberName, true)
      ES6PsiUtil.processExportDeclarationInScope(definitionPsi, TypeScriptQualifiedItemProcessor(sink, definitionPsi))

      val results = sink.results
      if (results != null) {
        for (res in results) {
          val resolved = when (res) {
            is ES6ExportSpecifierAlias -> res.findAliasedElement()
            is ES6ImportExportSpecifier -> res.resolve()
            else -> res
          }
          result = tryCast(resolved, memberClass)
          if (result != null) {
            break
          }
        }
      }
    }
    return create(definitionPsi, result)
  }

  override fun findMember(name: String): MetadataElement<*>? {
    return super.findMember(name)
           ?: stub.childrenStubs.firstNotNullOfOrNull {
             it.asSafely<Angular2MetadataModuleExportStub>()
               ?.psi.asSafely<Angular2MetadataModuleExport>()
               ?.findExport(name)
           }
  }

  override fun getName(): String? {
    return stub.importAs
  }

  override fun toString(): String {
    return (if (stub.importAs != null) stub.importAs!! + " " else "") + "<metadata node module>"
  }

  private fun checkPackageJson(path: String, prefix: String): Boolean {
    val sourceFile = containingFile.viewProvider.virtualFile
    val packageFile = sourceFile.parent?.findFileByRelativePath(path) ?: return false
    val mainFile = PackageJsonData.getOrCreate(packageFile).defaultMain ?: return false
    return containingFile.name.removeSuffix(METADATA_SUFFIX) + D_TS_SUFFIX == mainFile.removePrefix(prefix)
  }
}
