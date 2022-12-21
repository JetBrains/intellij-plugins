// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.psi

import com.intellij.javascript.nodejs.NodeModuleSearchUtil
import com.intellij.lang.javascript.library.JSLibraryUtil.NODE_MODULES
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.containers.MultiMap
import org.angular2.entities.metadata.Angular2MetadataFileType.Companion.METADATA_SUFFIX
import org.angular2.index.Angular2MetadataNodeModuleIndex
import org.jetbrains.annotations.NonNls

class ExternalNodeModuleResolver(private val mySource: Angular2MetadataElement<*>,
                                 private val myModuleName: String,
                                 private val myMemberName: String?) {

  fun resolve(): Angular2MetadataElement<*>? {
    val memberExtractor: (Angular2MetadataNodeModule) -> Angular2MetadataElement<*>? =
      if (myMemberName == null)
        { nodeModule -> nodeModule }
      else
        { nodeModule -> nodeModule.findMember(myMemberName) as? Angular2MetadataElement<*> }
    if (myModuleName.startsWith("./") || myModuleName.startsWith("../")) {
      val module = mySource.loadRelativeFile(myModuleName, METADATA_SUFFIX)?.let { file ->
        PsiTreeUtil.getStubChildOfType(file, Angular2MetadataNodeModule::class.java)
      }
      return if (module != null) memberExtractor(module) else null
    }
    val result = resolveFromFileSystem(memberExtractor)
    return result ?: resolveFromIndex(memberExtractor)
  }

  private fun resolveFromFileSystem(memberExtractor: (Angular2MetadataNodeModule) -> Angular2MetadataElement<*>?): Angular2MetadataElement<*>? {
    return NodeModuleSearchUtil.findAncestorNodeModulesDir(mySource.containingFile.virtualFile)?.let { dir ->
      val module = mySource.loadRelativeFile(dir, myModuleName, METADATA_SUFFIX)
        ?.let { file -> PsiTreeUtil.getStubChildOfType(file, Angular2MetadataNodeModule::class.java) }
      if (module != null) memberExtractor(module) else null
    }
  }

  private fun resolveFromIndex(memberExtractor: (Angular2MetadataNodeModule) -> Angular2MetadataElement<*>?): Angular2MetadataElement<*>? {
    val candidates = MultiMap.createSet<Angular2MetadataElement<*>, Angular2MetadataNodeModule>()
    StubIndex.getInstance().processElements(
      Angular2MetadataNodeModuleIndex.KEY, myModuleName, mySource.project,
      GlobalSearchScope.allScope(mySource.project), Angular2MetadataNodeModule::class.java
    ) { nodeModule ->
      if (nodeModule.isValid) {
        memberExtractor(nodeModule)?.let { candidates.putValue(it, nodeModule) }
      }
      true
    }
    if (candidates.size() > 1) {
      retainReachableNodeModulesFolders(candidates)
    }
    if (candidates.size() > 1) {
      retainPackageTypingRoots(candidates)
    }
    return if (candidates.size() > 1) {
      // in case of multiple candidates, ensure deterministic outcome by using file path with lowest lexical order
      candidates.keySet().minBy { it.containingFile.virtualFile.path }
    }
    else candidates.keySet().firstOrNull()
  }

  private fun retainReachableNodeModulesFolders(candidates: MultiMap<Angular2MetadataElement<*>, Angular2MetadataNodeModule>) {
    val path = getNodeModulesPath(mySource) ?: return
    val sameFolder = HashSet<Angular2MetadataNodeModule>()
    val parentFolder = HashSet<Angular2MetadataNodeModule>()
    candidates.values().forEach { nodeModule ->
      val path1 = getNodeModulesPath(nodeModule)
      if (path1 != null) {
        if (path == path1) {
          sameFolder.add(nodeModule)
        }
        else if (path.startsWith(path1)) {
          parentFolder.add(nodeModule)
        }
      }
    }
    if (!sameFolder.isEmpty()) {
      retain(candidates, sameFolder)
    }
    else if (!parentFolder.isEmpty()) {
      retain(candidates, parentFolder)
    }
  }

  companion object {
    @NonNls
    private val LOG = Logger.getInstance(ExternalNodeModuleResolver::class.java)

    private val ourReportedErrors = HashSet<String>()
    private val NODE_MODULES_SEGMENT = "/$NODE_MODULES/"

    private fun retainPackageTypingRoots(candidates: MultiMap<Angular2MetadataElement<*>, Angular2MetadataNodeModule>) {
      val packageTypingsRoots = HashSet<Angular2MetadataNodeModule>()
      candidates.values().forEach { nodeModule ->
        if (nodeModule.isPackageTypingsRoot) {
          packageTypingsRoots.add(nodeModule)
        }
      }
      if (!packageTypingsRoots.isEmpty()) {
        retain(candidates, packageTypingsRoots)
      }
    }

    private fun retain(candidates: MultiMap<Angular2MetadataElement<*>, Angular2MetadataNodeModule>,
                       nodeModules: Set<Angular2MetadataNodeModule>) {
      val iterator = candidates.entrySet().iterator()
      while (iterator.hasNext()) {
        val (_, value) = iterator.next()
        val listIterator = value.iterator()
        while (listIterator.hasNext()) {
          if (!nodeModules.contains(listIterator.next())) {
            listIterator.remove()
          }
        }
        if (value.isEmpty()) {
          iterator.remove()
        }
      }
    }

    private fun getNodeModulesPath(element: Angular2MetadataElement<*>): String? {
      return stripNodeModulesPath(element.containingFile.originalFile.virtualFile?.path)
    }

    private fun stripNodeModulesPath(path: String?): String? {
      val index = path?.lastIndexOf(NODE_MODULES_SEGMENT) ?: -1
      return if (index >= 0) path!!.substring(0, index + 1) else null
    }

    private fun renderFileName(element: Angular2MetadataElement<*>): String {
      val name = element.containingFile.virtualFile.path
      var index = name.lastIndexOf(NODE_MODULES_SEGMENT)
      if (index > 1) {
        index = name.lastIndexOf("/", index - 1)
        if (index > 0) {
          return name.substring(index)
        }
      }
      return name
    }
  }
}
