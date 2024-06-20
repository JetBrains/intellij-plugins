// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.lang.resolve

import com.intellij.lang.javascript.psi.util.JSProjectUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.GlobalSearchScopes
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.util.CachedValueProvider.Result.create
import com.intellij.psi.util.CachedValuesManager
import org.intellij.prisma.ide.indexing.PRISMA_ENTITIES_INDEX_KEY
import org.intellij.prisma.lang.psi.PrismaEntityDeclaration

fun processEntityDeclarations(processor: PrismaProcessor, state: ResolveState, element: PsiElement) {
  if (!processLocalFileDeclarations(processor, state, element)) return
  if (!processGlobalEntityDeclarations(processor, state, element, getSchemaScopeWithoutCurrentFile(element))) return
}

fun processLocalFileDeclarations(
  processor: PrismaProcessor,
  state: ResolveState,
  element: PsiElement,
): Boolean {
  val file = element.containingFile
  return file.processDeclarations(processor, state, null, element)
}

fun processGlobalEntityDeclarations(
  processor: PrismaProcessor,
  state: ResolveState,
  element: PsiElement,
  scope: GlobalSearchScope = getSchemaScope(element),
): Boolean {
  if (processor is PrismaResolveProcessor) {
    return processGlobalEntityDeclarations(processor, state, element, processor.name, scope)
  }
  else {
    for (key in StubIndex.getInstance().getAllKeys(PRISMA_ENTITIES_INDEX_KEY, element.project)) {
      if (!processGlobalEntityDeclarations(processor, state, element, key, scope)) return false
    }
  }
  return true
}

private fun processGlobalEntityDeclarations(
  processor: PrismaProcessor,
  state: ResolveState,
  element: PsiElement,
  key: String,
  scope: GlobalSearchScope,
): Boolean {
  for (declaration in StubIndex.getElements(PRISMA_ENTITIES_INDEX_KEY, key, element.project, scope,
                                            PrismaEntityDeclaration::class.java)) {
    if (!processor.execute(declaration, state)) return false
  }
  return true
}

fun getSchemaScope(context: PsiElement): GlobalSearchScope {
  return CachedValuesManager.getCachedValue(context) {
    val psiFile = context.containingFile.originalFile
    val file = psiFile.viewProvider.virtualFile
    val root = findSchemaRoot(context.project, file)
    create(root?.let { GlobalSearchScopes.directoryScope(context.project, it, true) }
           ?: GlobalSearchScope.projectScope(context.project), VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
  }
}

fun getSchemaScopeWithoutCurrentFile(context: PsiElement): GlobalSearchScope {
  val psiFile = context.containingFile.originalFile
  return GlobalSearchScope.notScope(GlobalSearchScope.fileScope(psiFile)).intersectWith(getSchemaScope(context))
}

private fun findSchemaRoot(project: Project, file: VirtualFile): VirtualFile? =
  JSProjectUtil.processDirectoriesUpToContentRootAndFindFirst(project, file) {
    // TODO: not the best way obviously, but I don't see any other option yet
    if (it.isDirectory && (it.name == "prisma" || it.name == "schema")) it else null
  }